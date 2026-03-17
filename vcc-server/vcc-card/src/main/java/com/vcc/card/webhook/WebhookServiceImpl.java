package com.vcc.card.webhook;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.Transaction;
import com.vcc.card.domain.WebhookLog;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.TransactionMapper;
import com.vcc.card.mapper.WebhookLogMapper;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.upstream.config.YeeVccConfig;
import com.vcc.upstream.util.Rsa2048SignatureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook 回调处理 服务实现
 */
@Service
public class WebhookServiceImpl implements WebhookService
{
    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);

    private static final String TYPE_TRANSACTION = "TRANSACTION";
    private static final String TYPE_CARD_STATUS_CHANGE = "CARD_STATUS_CHANGE";
    private static final String TYPE_RECHARGE_RESULT = "RECHARGE_RESULT";
    private static final String TYPE_3DS_OTP = "3DS_OTP";

    @Autowired
    private WebhookLogMapper webhookLogMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private RechargeMapper rechargeMapper;

    @Autowired
    private YeeVccConfig yeeVccConfig;

    @Async("threadPoolTaskExecutor")
    @Override
    public void processWebhookAsync(String webhookType, String payload, String signature, Map<String, Object> data)
    {
        // 1. 提取上游交易ID
        String upstreamTxnId = extractUpstreamTxnId(webhookType, data);

        // 2. 保存原始日志
        WebhookLog webhookLog = new WebhookLog();
        webhookLog.setWebhookType(webhookType);
        webhookLog.setUpstreamTxnId(upstreamTxnId);
        webhookLog.setPayload(payload);
        webhookLog.setSignature(signature);
        webhookLog.setProcessed(WebhookLog.PROCESSED_NO);
        webhookLog.setRetryCount(0);
        webhookLogMapper.insertWebhookLog(webhookLog);

        // 3. 验签
        if (!verifySignature(payload, signature))
        {
            markFailed(webhookLog, "验签失败");
            return;
        }

        // 4. 幂等检查：同一 upstream_txn_id 是否已处理
        if (upstreamTxnId != null && isDuplicate(upstreamTxnId, webhookLog.getId()))
        {
            markFailed(webhookLog, "重复回调，已跳过");
            return;
        }

        // 5. 分发处理
        try
        {
            switch (webhookType)
            {
                case TYPE_TRANSACTION:
                    handleTransaction(data);
                    break;
                case TYPE_CARD_STATUS_CHANGE:
                    handleCardStatusChange(data);
                    break;
                case TYPE_RECHARGE_RESULT:
                    handleRechargeResult(data);
                    break;
                case TYPE_3DS_OTP:
                    handle3dsOtp(data);
                    break;
                default:
                    log.warn("未知的Webhook类型: {}", webhookType);
            }
            markSuccess(webhookLog);
        }
        catch (Exception e)
        {
            log.error("Webhook处理异常: type={}, upstreamTxnId={}", webhookType, upstreamTxnId, e);
            markFailed(webhookLog, e.getMessage());
        }
    }

    /**
     * 验签：RSA 验证上游签名
     */
    private boolean verifySignature(String payload, String signature)
    {
        if (signature == null || signature.isEmpty())
        {
            log.warn("Webhook回调缺少签名");
            return false;
        }
        try
        {
            return Rsa2048SignatureUtils.verify(payload, signature, yeeVccConfig.getPlatformPublicKey());
        }
        catch (Exception e)
        {
            log.error("Webhook验签异常", e);
            return false;
        }
    }

    /**
     * 幂等检查：同一 upstream_txn_id 是否已成功处理过
     */
    private boolean isDuplicate(String upstreamTxnId, Long currentLogId)
    {
        WebhookLog existing = webhookLogMapper.selectWebhookLogByUpstreamTxnId(upstreamTxnId);
        return existing != null && !existing.getId().equals(currentLogId)
                && WebhookLog.PROCESSED_YES == existing.getProcessed();
    }

    /**
     * TRANSACTION：交易通知 → 写入 vcc_transaction 表，更新卡余额（储值卡）
     */
    @Transactional
    public void handleTransaction(Map<String, Object> data)
    {
        String tranId = getString(data, "tranId");
        String cardId = getString(data, "cardId");
        String direction = getString(data, "transactionDirection");
        String status = getString(data, "status");

        // 幂等：检查 txn_id 是否已存在
        if (transactionMapper.selectTransactionByTxnId(tranId) != null)
        {
            log.info("交易记录已存在，跳过: txnId={}", tranId);
            return;
        }

        // 查找本地卡
        Card card = cardMapper.selectCardByUpstreamCardId(cardId);
        if (card == null)
        {
            log.warn("Webhook交易通知：本地未找到卡, upstreamCardId={}", cardId);
            return;
        }

        // 解析交易类型
        String txnType = resolveTxnType(direction, getString(data, "originalTranId"));

        // 写入交易记录
        Transaction txn = new Transaction();
        txn.setTxnId(tranId);
        txn.setCardId(card.getId());
        txn.setUserId(card.getUserId());
        txn.setTxnType(txnType);
        txn.setAmount(getBigDecimal(data, "merchantAmount"));
        txn.setCurrency(getString(data, "merchantCurrency"));
        txn.setMerchantName(getString(data, "merchantName"));
        txn.setMerchantMcc(getString(data, "merchantMcc"));
        txn.setMerchantCountry(getString(data, "merchantCountry"));
        txn.setStatus("SUCCESS".equalsIgnoreCase(status)
                ? Transaction.STATUS_SUCCESS : Transaction.STATUS_FAILED);
        txn.setAuthCode(getString(data, "authCode"));
        txn.setFailReason(getString(data, "failReason"));
        transactionMapper.insertTransaction(txn);

        // 储值卡：更新余额
        if (card.getCardType() != null && card.getCardType() == Card.TYPE_PREPAID)
        {
            BigDecimal closingAmount = getBigDecimal(data, "closingAmount");
            if (closingAmount != null)
            {
                Card updateCard = new Card();
                updateCard.setId(card.getId());
                updateCard.setBalance(closingAmount);
                cardMapper.updateCard(updateCard);
            }
        }

        log.info("交易记录已写入: txnId={}, cardId={}, type={}, amount={}",
                tranId, card.getId(), txnType, txn.getAmount());
    }

    /**
     * CARD_STATUS_CHANGE：卡状态变更 → 更新 vcc_card 表
     */
    public void handleCardStatusChange(Map<String, Object> data)
    {
        String upstreamCardId = getString(data, "cardId");
        String newStatus = getString(data, "cardStatus");

        Card card = cardMapper.selectCardByUpstreamCardId(upstreamCardId);
        if (card == null)
        {
            log.warn("Webhook卡状态变更：本地未找到卡, upstreamCardId={}", upstreamCardId);
            return;
        }

        Integer statusCode = mapCardStatus(newStatus);
        if (statusCode == null)
        {
            log.warn("Webhook卡状态变更：未知状态 {}", newStatus);
            return;
        }

        Card updateCard = new Card();
        updateCard.setId(card.getId());
        updateCard.setStatus(statusCode);
        if (statusCode == Card.STATUS_ACTIVE)
        {
            updateCard.setActivatedAt(new Date());
        }
        else if (statusCode == Card.STATUS_CANCELLED)
        {
            updateCard.setCancelledAt(new Date());
        }
        cardMapper.updateCard(updateCard);

        log.info("卡状态已更新: cardId={}, newStatus={}", card.getId(), newStatus);
    }

    /**
     * RECHARGE_RESULT：充值结果回调 → 更新 vcc_recharge 表状态
     */
    public void handleRechargeResult(Map<String, Object> data)
    {
        String upstreamOrderNo = getString(data, "orderNo");
        String status = getString(data, "status");

        if (upstreamOrderNo == null || upstreamOrderNo.isEmpty())
        {
            log.warn("Webhook充值回调：缺少 orderNo");
            return;
        }

        // 通过上游单号查找充值记录
        Recharge query = new Recharge();
        query.setUpstreamOrderNo(upstreamOrderNo);
        java.util.List<Recharge> list = rechargeMapper.selectRechargeList(query);
        if (list == null || list.isEmpty())
        {
            log.warn("Webhook充值回调：本地未找到充值记录, upstreamOrderNo={}", upstreamOrderNo);
            return;
        }
        Recharge recharge = list.get(0);

        // 已终态则跳过
        if (recharge.getStatus() != null && recharge.getStatus() != Recharge.STATUS_PENDING)
        {
            log.info("充值记录已处理，跳过: orderNo={}", recharge.getOrderNo());
            return;
        }

        Recharge update = new Recharge();
        update.setId(recharge.getId());
        update.setCompletedAt(new Date());

        if ("SUCCESS".equalsIgnoreCase(status))
        {
            update.setStatus(Recharge.STATUS_SUCCESS);
        }
        else
        {
            update.setStatus(Recharge.STATUS_FAILED);
            update.setFailReason(getString(data, "failReason"));
        }
        rechargeMapper.updateRecharge(update);

        log.info("充值记录已更新: orderNo={}, status={}", recharge.getOrderNo(), status);
    }

    /**
     * 3DS_OTP：3DS 验证码回调 → 保存验证码，支持商户端展示
     * 
     * 字段说明（根据 YeeVCC 文档）：
     * - cardId: 上游卡号
     * - otpCode: 验证码
     * - expireTime: 过期时间（秒级时间戳）
     * - phone: 预留手机号（脱敏）
     */
    public void handle3dsOtp(Map<String, Object> data)
    {
        String cardId = getString(data, "cardId");
        String otpCode = getString(data, "otpCode");
        String expireTime = getString(data, "expireTime");
        String phone = getString(data, "phone");

        if (StringUtils.isBlank(cardId) || StringUtils.isBlank(otpCode))
        {
            log.warn("Webhook 3DS OTP回调：缺少必要字段, cardId={}, otpCode={}", cardId, otpCode);
            return;
        }

        // 查找本地卡
        Card card = cardMapper.selectCardByUpstreamCardId(cardId);
        if (card == null)
        {
            log.warn("Webhook 3DS OTP回调：本地未找到卡, upstreamCardId={}", cardId);
            return;
        }

        // TODO: 保存验证码到 Redis 或数据库，支持商户端查询展示
        // 建议存储结构：
        // Key: vcc:3ds:otp:{cardId}
        // Value: {otpCode, expireTime, phone, receivedAt}
        // TTL: 5 分钟（或根据 expireTime 计算）
        
        log.info("3DS OTP已接收: cardId={}, otpCode={}, expireTime={}, phone={}", 
                cardId, otpCode, expireTime, phone);
        
        // TODO: 推送通知到商户端（WebSocket 或消息队列）
        // 商户端需要展示验证码给用户
    }

    // ==================== 工具方法 ====================

    private String extractUpstreamTxnId(String webhookType, Map<String, Object> data)
    {
        return switch (webhookType)
        {
            case TYPE_TRANSACTION -> getString(data, "tranId");
            case TYPE_CARD_STATUS_CHANGE -> getString(data, "cardId");
            case TYPE_RECHARGE_RESULT -> getString(data, "orderNo");
            case TYPE_3DS_OTP -> getString(data, "cardId");
            default -> null;
        };
    }

    private String resolveTxnType(String direction, String originalTranId)
    {
        if ("C".equalsIgnoreCase(direction))
        {
            return (originalTranId != null && !originalTranId.isEmpty())
                    ? Transaction.TYPE_REVERSE : Transaction.TYPE_REFUND;
        }
        return Transaction.TYPE_AUTH;
    }

    private Integer mapCardStatus(String upstreamStatus)
    {
        if (upstreamStatus == null) return null;
        return switch (upstreamStatus.toUpperCase())
        {
            case "ACTIVE", "NORMAL" -> Card.STATUS_ACTIVE;
            case "INACTIVE" -> Card.STATUS_INACTIVE;
            case "FROZEN", "FREEZE" -> Card.STATUS_FROZEN;
            case "CANCELLED", "CLOSED" -> Card.STATUS_CANCELLED;
            default -> null;
        };
    }

    private void markSuccess(WebhookLog webhookLog)
    {
        webhookLog.setProcessed(WebhookLog.PROCESSED_YES);
        webhookLog.setProcessedAt(new Date());
        webhookLog.setProcessResult("{\"result\":\"success\"}");
        webhookLogMapper.updateWebhookLog(webhookLog);
    }

    private void markFailed(WebhookLog webhookLog, String errorMsg)
    {
        webhookLog.setProcessed(WebhookLog.PROCESSED_NO);
        webhookLog.setProcessedAt(new Date());
        webhookLog.setErrorMsg(errorMsg);
        webhookLogMapper.updateWebhookLog(webhookLog);
        log.warn("Webhook处理失败: id={}, error={}", webhookLog.getId(), errorMsg);
    }

    private String getString(Map<String, Object> data, String key)
    {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> data, String key)
    {
        Object val = data.get(key);
        if (val == null) return null;
        try
        {
            return new BigDecimal(val.toString());
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
