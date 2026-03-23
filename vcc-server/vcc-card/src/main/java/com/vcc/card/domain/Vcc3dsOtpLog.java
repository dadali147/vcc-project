package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vcc.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 3DS OTP验证码记录 vcc_3ds_otp_log
 */
public class Vcc3dsOtpLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：待验证 */
    public static final int STATUS_PENDING = 0;
    /** 状态：已验证 */
    public static final int STATUS_VERIFIED = 1;
    /** 状态：已过期 */
    public static final int STATUS_EXPIRED = 2;

    private Long id;

    /** 归属用户ID */
    private Long userId;

    /** 上游卡ID */
    private String cardId;

    /** 脱敏卡号 */
    private String cardNoMask;

    /** 持卡人ID */
    private Long holderId;

    /** 验证码（不对外暴露） */
    @JsonIgnore
    private String otpCode;

    /** 商户名称 */
    private String merchantName;

    /** 交易金额 */
    private BigDecimal transactionAmount;

    /** 币种 */
    private String currency;

    /** 发送邮箱（脱敏存储） */
    private String destinationEmail;

    /** 状态(0待验证 1已验证 2已过期) */
    private Integer status;

    /** 原始回调JSON（不对外暴露） */
    @JsonIgnore
    private String webhookPayload;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifiedAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getCardId()
    {
        return cardId;
    }

    public void setCardId(String cardId)
    {
        this.cardId = cardId;
    }

    public String getCardNoMask()
    {
        return cardNoMask;
    }

    public void setCardNoMask(String cardNoMask)
    {
        this.cardNoMask = cardNoMask;
    }

    public Long getHolderId()
    {
        return holderId;
    }

    public void setHolderId(Long holderId)
    {
        this.holderId = holderId;
    }

    public String getOtpCode()
    {
        return otpCode;
    }

    public void setOtpCode(String otpCode)
    {
        this.otpCode = otpCode;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public BigDecimal getTransactionAmount()
    {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount)
    {
        this.transactionAmount = transactionAmount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getDestinationEmail()
    {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail)
    {
        this.destinationEmail = destinationEmail;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getWebhookPayload()
    {
        return webhookPayload;
    }

    public void setWebhookPayload(String webhookPayload)
    {
        this.webhookPayload = webhookPayload;
    }

    public Date getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt)
    {
        this.createdAt = createdAt;
    }

    public Date getVerifiedAt()
    {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt)
    {
        this.verifiedAt = verifiedAt;
    }
}
