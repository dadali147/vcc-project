package com.vcc.card.webhook;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.WebhookLog;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.WebhookLogMapper;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.upstream.config.YeeVccConfig;
import com.vcc.upstream.util.Rsa2048SignatureUtils;
import com.vcc.user.service.IUserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private WebhookTransactionService webhookTransactionService;

    @Autowired
    private WebhookAsyncService webhookAsyncService;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private RechargeMapper rechargeMapper;

    @Autowired
    private IUserAccountService userAccountService;

    @Autowired
    private YeeVccConfig yeeVccConfig;

    /**
     * VCC-011: 同步入队，保存到数据库后再返回成功
     */
    @Override
    public boolean enqueueWebhook(String webhookType, String payload, String signature, Map<String, Object> data)
    {
        try
        {
            // VCC-012: 生成唯一幂等键（webhookType + 业务唯一ID）
            String idempotencyKey = generateIdempotencyKey(webhookType, data);

            // 检查是否已存在（快速去重）
            if (idempotencyKey != null && webhookLogMapper.selectByIdempotencyKey(idempotencyKey) != null)
            {
                log.info("Webhook重复，已跳过: idempotencyKey={}", idempotencyKey);
                return true; // 视为成功，不重复处理
            }

            // 保存到数据库（同步执行，确保落盘后再返回 200）
            WebhookLog webhookLog = new WebhookLog();
            webhookLog.setWebhookType(webhookType);
            webhookLog.setUpstreamTxnId(extractUpstreamTxnId(webhookType, data));
            webhookLog.setIdempotencyKey(idempotencyKey);
            webhookLog.setPayload(payload);
            webhookLog.setSignature(signature);
            webhookLog.setProcessed(WebhookLog.PROCESSED_NO);
            webhookLog.setRetryCount(0);
            webhookLogMapper.insertWebhookLog(webhookLog);

            // 通过独立 Bean 异步处理，确保 @Async 代理生效
            webhookAsyncService.processWebhookAsync(webhookLog.getId(), webhookType, payload, signature, data);

            return true;
        }
        catch (Exception e)
        {
            log.error("Webhook入队失败", e);
            return false;
        }
    }

    @Override
    public void processWebhookAsync(String webhookType, String payload, String signature, Map<String, Object> data)
    {
        // 旧接口兼容，直接调用入队
        enqueueWebhook(webhookType, payload, signature, data);
    }

    /**
     * 分发 Webhook 到具体处理器（供 WebhookAsyncService 调用）
     */
    public void dispatchWebhook(String webhookType, Map<String, Object> data)
    {
        switch (webhookType)
        {
            case TYPE_TRANSACTION:
                // VCC-014: 使用独立 Service 确保事务生效
                webhookTransactionService.handleTransaction(data);
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
            // 充值失败，补偿用户余额（幂等：仅 PENDING 状态才执行，上面已检查）
            userAccountService.addBalance(recharge.getUserId(), recharge.getCurrency(), recharge.getAmount());
            log.info("充值失败余额补偿: orderNo={}, userId={}, amount={}",
                    recharge.getOrderNo(), recharge.getUserId(), recharge.getAmount());
        }
        rechargeMapper.updateRecharge(update);

        log.info("充值记录已更新: orderNo={}, status={}", recharge.getOrderNo(), status);
    }

    /**
     * 3DS_OTP：3DS 验证码回调 → 保存验证码，支持商户端展示
     */
    public void handle3dsOtp(Map<String, Object> data)
    {
        String cardId = getString(data, "cardId");
        String otpCode = getString(data, "otpCode");
        String expireTime = getString(data, "expireTime");
        String phone = getString(data, "phone");

        if (cardId == null || cardId.isEmpty() || otpCode == null || otpCode.isEmpty())
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
        log.info("3DS OTP已接收: cardId={}, otpCode={}, expireTime={}, phone={}",
                cardId, otpCode, expireTime, phone);
    }

    // ==================== 工具方法 ====================

    /**
     * VCC-012: 生成幂等键（webhookType + 业务唯一ID）
     */
    private String generateIdempotencyKey(String webhookType, Map<String, Object> data)
    {
        String businessId = switch (webhookType)
        {
            case TYPE_TRANSACTION -> getString(data, "tranId");
            case TYPE_CARD_STATUS_CHANGE -> {
                String cardId = getString(data, "cardId");
                String operateType = getString(data, "operateType");
                String timestamp = getString(data, "timestamp");
                yield cardId + ":" + operateType + ":" + timestamp;
            }
            case TYPE_RECHARGE_RESULT -> getString(data, "orderNo");
            case TYPE_3DS_OTP -> {
                String cardId = getString(data, "cardId");
                String otpCode = getString(data, "otpCode");
                yield cardId + ":" + otpCode;
            }
            default -> null;
        };

        if (businessId == null)
        {
            return null;
        }

        return webhookType + ":" + businessId;
    }

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
