package com.vcc.card.webhook;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Webhook 验签失败异常
 * 返回 401 触发上游重试
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class WebhookAuthenticationException extends RuntimeException
{
    public WebhookAuthenticationException(String message)
    {
        super(message);
    }

    public WebhookAuthenticationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
