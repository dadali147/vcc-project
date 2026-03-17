package com.vcc.web.admin.service;

import java.util.Map;

/**
 * Webhook 回调处理 服务接口 (从 vcc-card 迁移至 vcc-admin)
 */
public interface AdminWebhookService
{
    /**
     * 处理 Webhook 回调
     *
     * @param webhookType 回调类型
     * @param payload     原始报文（JSON字符串）
     * @param signature   签名
     * @param data        解析后的数据
     * @return 是否成功处理
     */
    boolean handleWebhook(String webhookType, String payload, String signature, Map<String, Object> data);

    /**
     * 验证 Webhook 签名
     *
     * @param payload   原始报文
     * @param signature 签名
     * @return 签名是否有效
     */
    boolean validateSignature(String payload, String signature);

    /**
     * 入队 Webhook 处理任务
     *
     * @param webhookType 回调类型
     * @param payload     原始报文（JSON字符串）
     * @param signature   签名
     * @param data        解析后的数据
     * @return 是否成功入队
     */
    boolean enqueueWebhook(String webhookType, String payload, String signature, Map<String, Object> data);
}
