package com.vcc.card.webhook;

import java.util.Map;

/**
 * Webhook 回调处理 服务层
 */
public interface WebhookService
{
    /**
     * 异步处理 YeeVCC 回调
     *
     * @param webhookType 回调类型
     * @param payload     原始报文（JSON字符串）
     * @param signature   签名
     * @param data        解析后的数据
     */
    void processWebhookAsync(String webhookType, String payload, String signature, Map<String, Object> data);
}
