package com.vcc.card.webhook;

import java.util.Date;
import java.util.Map;
import com.vcc.card.domain.WebhookLog;
import com.vcc.card.mapper.WebhookLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Webhook 异步处理服务（独立 Bean，确保 @Async 代理生效）
 */
@Service
public class WebhookAsyncService
{
    private static final Logger log = LoggerFactory.getLogger(WebhookAsyncService.class);

    @Autowired
    private WebhookLogMapper webhookLogMapper;

    @Autowired
    private WebhookServiceImpl webhookService;

    @Async("threadPoolTaskExecutor")
    public void processWebhookAsync(Long logId, String webhookType, String payload, String signature, Map<String, Object> data)
    {
        WebhookLog webhookLog = webhookLogMapper.selectWebhookLogById(logId);
        if (webhookLog == null || webhookLog.getProcessed() == WebhookLog.PROCESSED_YES)
        {
            log.warn("Webhook日志不存在或已处理: logId={}", logId);
            return;
        }

        try
        {
            webhookService.dispatchWebhook(webhookType, data);
            markSuccess(webhookLog);
        }
        catch (Exception e)
        {
            log.error("Webhook处理异常: type={}, logId={}", webhookType, logId, e);
            markFailed(webhookLog, e.getMessage());
        }
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
}
