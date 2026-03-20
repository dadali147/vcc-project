package com.vcc.risk.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 风控事件 vcc_risk_event
 */
public class RiskEvent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：待处理 */
    public static final String STATUS_PENDING = "PENDING";
    /** 状态：处理中 */
    public static final String STATUS_PROCESSING = "PROCESSING";
    /** 状态：已解决 */
    public static final String STATUS_RESOLVED = "RESOLVED";
    /** 状态：已忽略 */
    public static final String STATUS_IGNORED = "IGNORED";

    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 规则ID */
    private Long ruleId;

    /** 交易ID */
    private Long transactionId;

    /** 触发详情（JSON） */
    private String triggerDetail;

    /** 状态 */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理结果 */
    private String handleResult;

    /** 删除标志 */
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public Long getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(Long transactionId)
    {
        this.transactionId = transactionId;
    }

    public String getTriggerDetail()
    {
        return triggerDetail;
    }

    public void setTriggerDetail(String triggerDetail)
    {
        this.triggerDetail = triggerDetail;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getHandlerId()
    {
        return handlerId;
    }

    public void setHandlerId(Long handlerId)
    {
        this.handlerId = handlerId;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public String getHandleResult()
    {
        return handleResult;
    }

    public void setHandleResult(String handleResult)
    {
        this.handleResult = handleResult;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt()
    {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("merchantId", getMerchantId())
                .append("ruleId", getRuleId())
                .append("transactionId", getTransactionId())
                .append("status", getStatus())
                .append("handlerId", getHandlerId())
                .append("handleTime", getHandleTime())
                .toString();
    }
}
