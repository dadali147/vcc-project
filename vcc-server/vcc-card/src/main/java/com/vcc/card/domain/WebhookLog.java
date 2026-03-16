package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * Webhook回调日志 vcc_webhook_log
 */
public class WebhookLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 未处理 */
    public static final int PROCESSED_NO = 0;
    /** 已处理 */
    public static final int PROCESSED_YES = 1;

    private Long id;

    /** 回调类型：AUTH_TRANSACTION/CARD_OPERATION/OTP/THIRD_PARTY_AUTH */
    private String webhookType;

    /** 上游交易ID */
    private String upstreamTxnId;

    /** 回调原始数据（JSON） */
    private String payload;

    /** 签名 */
    private String signature;

    /** 是否已处理：0-否，1-是 */
    private Integer processed;

    /** 处理结果（JSON） */
    private String processResult;

    /** 错误信息 */
    private String errorMsg;

    /** 重试次数 */
    private Integer retryCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date processedAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getWebhookType()
    {
        return webhookType;
    }

    public void setWebhookType(String webhookType)
    {
        this.webhookType = webhookType;
    }

    public String getUpstreamTxnId()
    {
        return upstreamTxnId;
    }

    public void setUpstreamTxnId(String upstreamTxnId)
    {
        this.upstreamTxnId = upstreamTxnId;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public String getSignature()
    {
        return signature;
    }

    public void setSignature(String signature)
    {
        this.signature = signature;
    }

    public Integer getProcessed()
    {
        return processed;
    }

    public void setProcessed(Integer processed)
    {
        this.processed = processed;
    }

    public String getProcessResult()
    {
        return processResult;
    }

    public void setProcessResult(String processResult)
    {
        this.processResult = processResult;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public Integer getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount)
    {
        this.retryCount = retryCount;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getProcessedAt()
    {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt)
    {
        this.processedAt = processedAt;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("webhookType", getWebhookType())
                .append("upstreamTxnId", getUpstreamTxnId())
                .append("processed", getProcessed())
                .append("errorMsg", getErrorMsg())
                .toString();
    }
}
