package com.vcc.upstream.dto;

/**
 * YeeVCC 通用请求基类。
 */
public abstract class YeeVccBaseRequest
{
    private String requestNo;

    public String getRequestNo()
    {
        return requestNo;
    }

    public void setRequestNo(String requestNo)
    {
        this.requestNo = requestNo;
    }
}
