package com.vcc.upstream.exception;

/**
 * YeeVCC 调用异常。
 */
public class YeeVccException extends RuntimeException
{
    private final Integer statusCode;

    private final String rawResponse;

    public YeeVccException(String message)
    {
        this(message, null, null, null);
    }

    public YeeVccException(String message, Throwable cause)
    {
        this(message, null, null, cause);
    }

    public YeeVccException(String message, Integer statusCode, String rawResponse)
    {
        this(message, statusCode, rawResponse, null);
    }

    public YeeVccException(String message, Integer statusCode, String rawResponse, Throwable cause)
    {
        super(message, cause);
        this.statusCode = statusCode;
        this.rawResponse = rawResponse;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public String getRawResponse()
    {
        return rawResponse;
    }
}
