package com.vcc.card.mapper;

import java.util.List;
import com.vcc.card.domain.WebhookLog;

/**
 * Webhook回调日志 数据层
 */
public interface WebhookLogMapper
{
    public WebhookLog selectWebhookLogById(Long id);

    public WebhookLog selectWebhookLogByUpstreamTxnId(String upstreamTxnId);

    public List<WebhookLog> selectWebhookLogList(WebhookLog webhookLog);

    public int insertWebhookLog(WebhookLog webhookLog);

    public int updateWebhookLog(WebhookLog webhookLog);
}
