package com.vcc.card.webhook;

import java.util.Map;

/**
 * Webhook 回调处理 服务层
 */
public interface WebhookService
{
    /**
     * VCC-011: 将 Webhook 入队（同步执行，成功后再返回 200）
     *
     * @param webhookType 回调类型
     * @param payload     原始报文（JSON字符串）
     * @param signature   签名
     * @param data        解析后的数据
     * @return 是否成功入队
     */
    boolean enqueueWebhook(String webhookType, String payload, String signature, Map<String, Object> data);

    /**
     * 分派 Webhook 事件进行处理
     *
     * @param webhookType 回调类型
     * @param data        解析后的数据
     */
    void dispatchWebhook(String webhookType, Map<String, Object> data);
}
