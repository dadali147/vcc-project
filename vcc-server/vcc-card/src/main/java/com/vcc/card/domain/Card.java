package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡片 vcc_card
 */
public class Card extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 卡类型：储值卡 */
    public static final int TYPE_PREPAID = 1;
    /** 卡类型：预算卡 */
    public static final int TYPE_BUDGET = 2;

    /** 状态：激活 */
    public static final int STATUS_ACTIVE = 1;
    /** 状态：未激活 */
    public static final int STATUS_INACTIVE = 2;
    /** 状态：冻结 */
    public static final int STATUS_FROZEN = 3;
    /** 状态：已销卡 */
    public static final int STATUS_CANCELLED = 4;

    private Long id;

    private Long holderId;

    private Long userId;

    private String cardNoMask;

    private String cardBinId;

    /** 卡类型（1储值卡 2预算卡） */
    private Integer cardType;

    /** 状态（1激活 2未激活 3冻结 4已销卡） */
    private Integer status;

    private BigDecimal balance;

    private String currency;

    private BigDecimal budgetAmount;

    private String upstreamCardId;

    private String expiryDate;

    /** V3: 卡号前6位 */
    private String first6;

    /** V3: 卡号后4位 */
    private String last4;

    /** V3: 备注 */
    private String cardRemark;

    private Integer isAutoActivate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date activatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelledAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getHolderId()
    {
        return holderId;
    }

    public void setHolderId(Long holderId)
    {
        this.holderId = holderId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getCardNoMask()
    {
        return cardNoMask;
    }

    public void setCardNoMask(String cardNoMask)
    {
        this.cardNoMask = cardNoMask;
    }

    public String getCardBinId()
    {
        return cardBinId;
    }

    public void setCardBinId(String cardBinId)
    {
        this.cardBinId = cardBinId;
    }

    public Integer getCardType()
    {
        return cardType;
    }

    public void setCardType(Integer cardType)
    {
        this.cardType = cardType;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getBudgetAmount()
    {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount)
    {
        this.budgetAmount = budgetAmount;
    }

    public String getUpstreamCardId()
    {
        return upstreamCardId;
    }

    public void setUpstreamCardId(String upstreamCardId)
    {
        this.upstreamCardId = upstreamCardId;
    }

    public String getExpiryDate()
    {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate)
    {
        this.expiryDate = expiryDate;
    }

    public Integer getIsAutoActivate()
    {
        return isAutoActivate;
    }

    public void setIsAutoActivate(Integer isAutoActivate)
    {
        this.isAutoActivate = isAutoActivate;
    }

    public Date getActivatedAt()
    {
        return activatedAt;
    }

    public void setActivatedAt(Date activatedAt)
    {
        this.activatedAt = activatedAt;
    }

    public Date getCancelledAt()
    {
        return cancelledAt;
    }

    public void setCancelledAt(Date cancelledAt)
    {
        this.cancelledAt = cancelledAt;
    }

    public String getFirst6()
    {
        return first6;
    }

    public void setFirst6(String first6)
    {
        this.first6 = first6;
    }

    public String getLast4()
    {
        return last4;
    }

    public void setLast4(String last4)
    {
        this.last4 = last4;
    }

    public String getCardRemark()
    {
        return cardRemark;
    }

    public void setCardRemark(String cardRemark)
    {
        this.cardRemark = cardRemark;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("holderId", getHolderId())
                .append("userId", getUserId())
                .append("cardNoMask", getCardNoMask())
                .append("cardType", getCardType())
                .append("status", getStatus())
                .append("balance", getBalance())
                .append("currency", getCurrency())
                .toString();
    }
}
