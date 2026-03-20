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

    /** 交易分类 */
    public static final String CATEGORY_PURCHASE = "PURCHASE";
    public static final String CATEGORY_REFUND = "REFUND";
    public static final String CATEGORY_REVERSE = "REVERSE";
    public static final String CATEGORY_FEE = "FEE";
    public static final String CATEGORY_ADJUSTMENT = "ADJUSTMENT";

    /** 状态：处理中 */
    public static final int STATUS_PROCESSING = 0;
    /** 状态：成功 */
    public static final int STATUS_SUCCESS = 1;
    /** 状态：失败 */
    public static final int STATUS_FAILED = 2;

    /** 展示模式 */
    public static final String DISPLAY_NORMAL = "NORMAL";
    public static final String DISPLAY_HIDDEN = "HIDDEN";

    private Long id;

    /** 交易ID（上游） */
    private String txnId;

    private Long cardId;

    private Long userId;

    /** 商户ID */
    private Long merchantId;

    /** 持卡人ID */
    private Long holderId;

    /** 交易类型：AUTH/CAPTURE/REFUND/REVERSE */
    private String txnType;

    /** 交易分类：PURCHASE/REFUND/REVERSE/FEE/ADJUSTMENT */
    private String txnCategory;

    /** 关联交易ID */
    private String relatedTxnId;

    private BigDecimal amount;

    private String currency;

    private String merchantName;

    private String merchantMcc;

    private String merchantCountry;

    /** 状态（0处理中 1成功 2失败） */
    private Integer status;

    private String authCode;

    private String failReason;

    /** 失败原因编码 */
    private String failReasonCode;

    /** 失败原因描述 */
    private String failReasonText;

    /** 展示模式：NORMAL/HIDDEN */
    private String displayMode;

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

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Long getHolderId()
    {
        return holderId;
    }

    public void setHolderId(Long holderId)
    {
        this.holderId = holderId;
    }

    public String getTxnCategory()
    {
        return txnCategory;
    }

    public void setTxnCategory(String txnCategory)
    {
        this.txnCategory = txnCategory;
    }

    public String getRelatedTxnId()
    {
        return relatedTxnId;
    }

    public void setRelatedTxnId(String relatedTxnId)
    {
        this.relatedTxnId = relatedTxnId;
    }

    public String getFailReasonCode()
    {
        return failReasonCode;
    }

    public void setFailReasonCode(String failReasonCode)
    {
        this.failReasonCode = failReasonCode;
    }

    public String getFailReasonText()
    {
        return failReasonText;
    }

    public void setFailReasonText(String failReasonText)
    {
        this.failReasonText = failReasonText;
    }

    public String getDisplayMode()
    {
        return displayMode;
    }

    public void setDisplayMode(String displayMode)
    {
        this.displayMode = displayMode;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("txnId", getTxnId())
                .append("cardId", getCardId())
                .append("userId", getUserId())
                .append("merchantId", getMerchantId())
                .append("holderId", getHolderId())
                .append("txnType", getTxnType())
                .append("txnCategory", getTxnCategory())
                .append("amount", getAmount())
                .append("currency", getCurrency())
                .append("status", getStatus())
                .toString();
    }
}
