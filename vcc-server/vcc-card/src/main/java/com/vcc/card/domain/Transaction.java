package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录 vcc_transaction
 */
public class Transaction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交易类型：授权 */
    public static final String TYPE_AUTH = "AUTH";
    /** 交易类型：扣款 */
    public static final String TYPE_CAPTURE = "CAPTURE";
    /** 交易类型：退款 */
    public static final String TYPE_REFUND = "REFUND";
    /** 交易类型：撤销 */
    public static final String TYPE_REVERSE = "REVERSE";

    /** 状态：处理中 */
    public static final int STATUS_PROCESSING = 0;
    /** 状态：成功 */
    public static final int STATUS_SUCCESS = 1;
    /** 状态：失败 */
    public static final int STATUS_FAILED = 2;

    private Long id;

    /** 交易ID（上游） */
    private String txnId;

    private Long cardId;

    private Long userId;

    /** 交易类型：AUTH/CAPTURE/REFUND/REVERSE */
    private String txnType;

    private BigDecimal amount;

    private String currency;

    private String merchantName;

    private String merchantMcc;

    private String merchantCountry;

    /** 状态（0处理中 1成功 2失败） */
    private Integer status;

    private String authCode;

    private String failReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date txnTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTxnId()
    {
        return txnId;
    }

    public void setTxnId(String txnId)
    {
        this.txnId = txnId;
    }

    public Long getCardId()
    {
        return cardId;
    }

    public void setCardId(Long cardId)
    {
        this.cardId = cardId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getTxnType()
    {
        return txnType;
    }

    public void setTxnType(String txnType)
    {
        this.txnType = txnType;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public String getMerchantMcc()
    {
        return merchantMcc;
    }

    public void setMerchantMcc(String merchantMcc)
    {
        this.merchantMcc = merchantMcc;
    }

    public String getMerchantCountry()
    {
        return merchantCountry;
    }

    public void setMerchantCountry(String merchantCountry)
    {
        this.merchantCountry = merchantCountry;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getAuthCode()
    {
        return authCode;
    }

    public void setAuthCode(String authCode)
    {
        this.authCode = authCode;
    }

    public String getFailReason()
    {
        return failReason;
    }

    public void setFailReason(String failReason)
    {
        this.failReason = failReason;
    }

    public Date getTxnTime()
    {
        return txnTime;
    }

    public void setTxnTime(Date txnTime)
    {
        this.txnTime = txnTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("txnId", getTxnId())
                .append("cardId", getCardId())
                .append("userId", getUserId())
                .append("txnType", getTxnType())
                .append("amount", getAmount())
                .append("currency", getCurrency())
                .append("status", getStatus())
                .toString();
    }
}
