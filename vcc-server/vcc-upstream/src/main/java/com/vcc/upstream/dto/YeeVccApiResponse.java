package com.vcc.upstream.dto;

/**
 * YeeVCC 通用响应包装。
 *
 * @param <T> 业务数据类型
 */
public class YeeVccApiResponse<T>
{
    private Integer status;

    private String message;

    private boolean success;

    private boolean signatureVerified;

    private String requestNo;

    private String rawBody;

    private T data;

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public boolean isSignatureVerified()
    {
        return signatureVerified;
    }

    public void setSignatureVerified(boolean signatureVerified)
    {
        this.signatureVerified = signatureVerified;
    }

    public String getRequestNo()
    {
        return requestNo;
    }

    public void setRequestNo(String requestNo)
    {
        this.requestNo = requestNo;
    }

    public String getRawBody()
    {
        return rawBody;
    }

    public void setRawBody(String rawBody)
    {
        this.rawBody = rawBody;
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }
}
