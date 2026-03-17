package com.vcc.card.webhook;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Webhook 处理失败异常
 * 返回 500 触发上游重试
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class WebhookProcessingException extends RuntimeException
{
    public WebhookProcessingException(String message)
    {
        super(message);
    }

    public WebhookProcessingException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
