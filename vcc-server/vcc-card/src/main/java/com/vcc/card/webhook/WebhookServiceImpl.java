package com.vcc.card.webhook;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vcc.card.domain.Card;
import com.vcc.card.domain.WebhookLog;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.WebhookLogMapper;
import com.vcc.common.core.redis.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook 回调处理 实现类
 */
@Service
public class WebhookServiceImpl implements WebhookService
{
    private static final Logger log = LoggerFactory.getLogger(WebhookServiceImpl.class);

    /** OTP Redis key 前缀 */
    private static final String OTP_REDIS_KEY_PREFIX = "vcc:otp:";

    /** OTP 有效期（分钟） */
    private static final int OTP_TTL_MINUTES = 10;

    @Autowired
    private WebhookLogMapper webhookLogMapper;

    @Autowired
    private WebhookAsyncService webhookAsyncService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private WebhookTransactionService webhookTransactionService;

    @Autowired
    private WebhookRechargeHandler webhookRechargeHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enqueueWebhook(String webhookType, String payload, String signature, Map<String, Object> data)
    {
        try
        {
            log.info("入队 Webhook，类型: {}", webhookType);

            // 构建幂等键
            String idempotencyKey = buildIdempotencyKey(webhookType, data);

            // 保存 Webhook 日志
            WebhookLog webhookLog = new WebhookLog();
            webhookLog.setWebhookType(webhookType);
            webhookLog.setPayload(payload);
            webhookLog.setSignature(signature);
            webhookLog.setIdempotencyKey(idempotencyKey);
            webhookLog.setProcessed(WebhookLog.PROCESSED_NO);
            webhookLog.setCreatedAt(new Date());

            webhookLogMapper.insertWebhookLog(webhookLog);

            // 异步处理
            webhookAsyncService.processWebhookAsync(webhookLog.getId(), webhookType, payload, signature, data);

            return true;
        }
        catch (Exception e)
        {
            log.error("Webhook 入队失败，类型: {}", webhookType, e);
            return false;
        }
    }

    @Override
    public void dispatchWebhook(String webhookType, Map<String, Object> data)
    {
        try
        {
            log.info("分派 Webhook 事件，类型: {}", webhookType);

            // 幂等检查：构建幂等键，查询是否已成功处理过
            String idempotencyKey = buildIdempotencyKey(webhookType, data);
            if (idempotencyKey != null)
            {
                WebhookLog existingLog = webhookLogMapper.selectByIdempotencyKey(idempotencyKey);
                if (existingLog != null && existingLog.getProcessed() == WebhookLog.PROCESSED_YES)
                {
                    log.warn("Webhook已处理过，跳过重复处理: idempotencyKey={}", idempotencyKey);
                    return;
                }
            }
            
            // 根据 webhookType 分派给不同的处理器
            // 事件类型定义来源：接口契约文档 §3.1
            switch (webhookType)
            {
                case "OTP":       // 文档定义值（接口契约文档 §3.1）
                case "3DS_OTP":   // 兼容旧写法（待上游确认后移除）
                    handleOtpWebhook(data);
                    break;
                case "TRANSACTION":
                case "AUTH_TRANSACTION":
                    handleTransactionWebhook(data);
                    break;
                case "CARD_STATUS_CHANGE":
                case "CARD_OPERATION":
                    handleCardStatusChangeWebhook(data);
                    break;
                case "RECHARGE_RESULT":
                case "TOPUP_RESULT":  // 文档定义值（接口契约文档 §3.1），RECHARGE_RESULT 为兼容写法
                    handleRechargeResultWebhook(data);
                    break;
                default:
                    log.warn("未知的 Webhook 类型: {}", webhookType);
            }
        }
        catch (Exception e)
        {
            log.error("分派 Webhook 事件失败，类型: {}", webhookType, e);
            throw new WebhookProcessingException("处理失败", e);
        }
    }

    /**
     * 处理 3DS OTP 验证码回调
     * 将 OTP 验证码保存到 Redis，key: vcc:otp:{cardId}，TTL 10 分钟
     */
    private void handleOtpWebhook(Map<String, Object> data)
    {
        log.info("处理 3DS OTP Webhook: {}", data);

        String cardId = getStringValue(data, "cardId");
        String otp = getStringValue(data, "otp");

        if (cardId == null || otp == null)
        {
            log.error("OTP Webhook 缺少必要字段: cardId={}, otp={}", cardId, otp);
            throw new WebhookProcessingException("OTP回调缺少必要字段 cardId 或 otp");
        }

        // 保存 OTP 到 Redis，TTL 10 分钟
        String redisKey = OTP_REDIS_KEY_PREFIX + cardId;
        redisCache.setCacheObject(redisKey, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("OTP 验证码已保存到 Redis: key={}, ttl={}min", redisKey, OTP_TTL_MINUTES);
    }

    /**
     * 处理交易回调
     * 委托给 WebhookTransactionService（独立 Bean，事务生效）
     */
    private void handleTransactionWebhook(Map<String, Object> data)
    {
        log.info("处理交易 Webhook: {}", data);
        webhookTransactionService.handleTransaction(data);
    }

    /**
     * 处理卡状态变更回调
     * 更新本地 vcc_card 表的 status 字段
     */
    @Transactional(rollbackFor = Exception.class)
    private void handleCardStatusChangeWebhook(Map<String, Object> data)
    {
        log.info("处理卡状态变更 Webhook: {}", data);

        String upstreamCardId = getStringValue(data, "cardId");
        if (upstreamCardId == null)
        {
            log.error("卡状态变更 Webhook 缺少 cardId 字段");
            throw new WebhookProcessingException("卡状态变更回调缺少 cardId 字段");
        }

        // 通过上游 cardId 查找本地卡片
        Card card = cardMapper.selectCardByUpstreamCardId(upstreamCardId);
        if (card == null)
        {
            log.error("卡状态变更 Webhook: 未找到本地卡片 upstreamCardId={}", upstreamCardId);
            throw new WebhookProcessingException("未找到对应的本地卡片: " + upstreamCardId);
        }

        // 解析新状态
        Integer newStatus = resolveCardStatus(data);
        if (newStatus == null)
        {
            log.error("卡状态变更 Webhook: 无法解析新状态 data={}", data);
            throw new WebhookProcessingException("无法解析卡片新状态");
        }

        // 更新卡片状态
        Card updateCard = new Card();
        updateCard.setId(card.getId());
        updateCard.setStatus(newStatus);

        // 特殊处理：激活时间、注销时间
        if (newStatus == Card.STATUS_ACTIVE && card.getActivatedAt() == null)
        {
            updateCard.setActivatedAt(new Date());
        }
        if (newStatus == Card.STATUS_CANCELLED)
        {
            updateCard.setCancelledAt(new Date());
        }

        cardMapper.updateCard(updateCard);
        log.info("卡片状态已更新: cardId={}, upstreamCardId={}, oldStatus={}, newStatus={}",
                card.getId(), upstreamCardId, card.getStatus(), newStatus);
    }

    /**
     * 处理充值结果回调
     * 委托给 WebhookRechargeHandler（跨模块桥接，实现在 vcc-finance）
     *   - SUCCESS: 更新状态为成功，不补偿余额
     *   - FAILED: 更新状态为失败，补偿用户账户余额
     */
    private void handleRechargeResultWebhook(Map<String, Object> data)
    {
        log.info("处理充值结果 Webhook: {}", data);

        String orderNo = getStringValue(data, "orderNo");
        if (orderNo == null)
        {
            log.error("充值结果 Webhook 缺少 orderNo 字段");
            throw new WebhookProcessingException("充值结果回调缺少 orderNo 字段");
        }

        String resultStatus = getStringValue(data, "status");
        if (resultStatus == null)
        {
            resultStatus = getStringValue(data, "result");
        }

        if ("SUCCESS".equalsIgnoreCase(resultStatus))
        {
            boolean handled = webhookRechargeHandler.handleRechargeSuccess(orderNo);
            if (!handled)
            {
                throw new WebhookProcessingException("充值成功处理失败: " + orderNo);
            }
        }
        else if ("FAILED".equalsIgnoreCase(resultStatus) || "FAIL".equalsIgnoreCase(resultStatus))
        {
            String failReason = getStringValue(data, "failReason");
            if (failReason == null)
            {
                failReason = getStringValue(data, "message");
            }
            boolean handled = webhookRechargeHandler.handleRechargeFailure(orderNo, failReason);
            if (!handled)
            {
                throw new WebhookProcessingException("充值失败处理失败: " + orderNo);
            }
        }
        else
        {
            log.warn("充值结果 Webhook 未知状态: orderNo={}, status={}", orderNo, resultStatus);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 构建幂等键：webhookType + 业务唯一ID
     */
    private String buildIdempotencyKey(String webhookType, Map<String, Object> data)
    {
        String bizKey = null;
        switch (webhookType)
        {
            case "OTP":       // 文档定义值（接口契约文档 §3.1）
            case "3DS_OTP":   // 兼容旧写法（待上游确认后移除）
                // 使用 requestId 作为幂等 key（上游确认字段），fallback 到 cardId+otp
                String requestId = getStringValue(data, "requestId");
                String otpCardId = getStringValue(data, "cardId");
                String otpCode = getStringValue(data, "otp");
                bizKey = requestId != null ? requestId : (otpCardId + ":" + otpCode);
                break;
            case "TRANSACTION":
            case "AUTH_TRANSACTION":
                // 交易: tranId
                bizKey = getStringValue(data, "tranId");
                if (bizKey == null)
                {
                    bizKey = getStringValue(data, "txnId");
                }
                break;
            case "CARD_STATUS_CHANGE":
            case "CARD_OPERATION":
                // 卡状态: cardId + cardStatus/operateType
                String cardStatus = getStringValue(data, "cardStatus");
                if (cardStatus == null)
                {
                    cardStatus = getStringValue(data, "operateType");
                }
                bizKey = getStringValue(data, "cardId") + ":" + cardStatus;
                break;
            case "RECHARGE_RESULT":
            case "TOPUP_RESULT":  // 文档定义值（接口契约文档 §3.1）
                // 充值: orderNo
                bizKey = getStringValue(data, "orderNo");
                break;
            default:
                // 未知类型使用整个 data 的 hashCode
                bizKey = String.valueOf(data.hashCode());
        }
        return webhookType + ":" + bizKey;
    }

    /**
     * 解析卡片状态
     *
     * 上游 YeeVCC 文档定义的卡状态枚举值（来源：接口契约文档 §1.4.3 + 业务流程文档 §2.3）：
     *   ACTIVE       — 正常/已激活
     *   INACTIVE     — 待激活
     *   FROZEN       — 冻结
     *   CLOSED       — 已销卡
     *   SUSPENDED    — 停用（业务流程文档 §2.3）
     *
     * 以下兼容写法为防御性处理，待上游确认后可移除：
     *   ACTIVATED / ACTIVATE   → 映射为 ACTIVE
     *   DEACTIVATED            → 映射为 INACTIVE
     *   FREEZE / LOCKED        → 映射为 FROZEN
     *   CANCELLED / CANCEL     → 映射为 CLOSED
     */
    private Integer resolveCardStatus(Map<String, Object> data)
    {
        String cardStatus = getStringValue(data, "cardStatus");
        if (cardStatus == null)
        {
            cardStatus = getStringValue(data, "operateType");
        }
        if (cardStatus == null)
        {
            return null;
        }
        switch (cardStatus.toUpperCase())
        {
            // 文档定义值
            case "ACTIVE":
            // 防御性兼容（待上游确认）
            case "ACTIVATED":
            case "ACTIVATE":
                return Card.STATUS_ACTIVE;

            // 文档定义值
            case "INACTIVE":
            // 防御性兼容（待上游确认）
            case "DEACTIVATED":
                return Card.STATUS_INACTIVE;

            // 文档定义值
            case "FROZEN":
            // 防御性兼容（待上游确认）
            case "FREEZE":
            case "LOCKED":
                return Card.STATUS_FROZEN;

            // 文档定义值
            case "CLOSED":
            // 文档定义值（业务流程文档 §2.3 状态流转）
            case "SUSPENDED":
            // 防御性兼容（待上游确认）
            case "CANCELLED":
            case "CANCEL":
                return Card.STATUS_CANCELLED;

            default:
                log.warn("未知的卡片状态: {}", cardStatus);
                return null;
        }
    }

    /**
     * 安全地从 Map 中获取字符串值
     */
    private String getStringValue(Map<String, Object> data, String key)
    {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
}
