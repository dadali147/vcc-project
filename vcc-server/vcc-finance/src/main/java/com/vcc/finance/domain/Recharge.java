package com.vcc.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值记录 vcc_recharge
 */
public class Recharge extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：待处理 */
    public static final int STATUS_PENDING = 0;
    /** 状态：成功 */
    public static final int STATUS_SUCCESS = 1;
    /** 状态：失败 */
    public static final int STATUS_FAILED = 2;

    /** 充值类型：储值卡充值 */
    public static final int TYPE_PREPAID = 1;
    /** 充值类型：预算卡设额度 */
    public static final int TYPE_BUDGET = 2;

    /** V3 充值场景：账户充值 */
    public static final String SCENE_TOP_UP = "TOP_UP";
    /** V3 充值场景：充值到卡 */
    public static final String SCENE_CARD_LOAD = "CARD_LOAD";

    private Long id;

    private String orderNo;

    private Long userId;

    /** V3: 商户ID */
    private Long merchantId;

    private Long cardId;

    private BigDecimal amount;

    private String currency;

    private BigDecimal fee;

    private BigDecimal actualAmount;

    /** 状态（0待处理 1成功 2失败） */
    private Integer status;

    /** 充值类型（1储值卡充值 2预算卡设额度） */
    private Integer rechargeType;

    /** V3: 充值场景：TOP_UP/CARD_LOAD */
    private String rechargeScene;

    private String upstreamOrderNo;

    private String txHash;

    /** V3: 链类型：TRC20/ERC20/BEP20 */
    private String chainType;

    private String failReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedAt;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Long getCardId()
    {
        return cardId;
    }

    public void setCardId(Long cardId)
    {
        this.cardId = cardId;
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

    public BigDecimal getFee()
    {
        return fee;
    }

    public void setFee(BigDecimal fee)
    {
        this.fee = fee;
    }

    public BigDecimal getActualAmount()
    {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount)
    {
        this.actualAmount = actualAmount;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getRechargeType()
    {
        return rechargeType;
    }

    public void setRechargeType(Integer rechargeType)
    {
        this.rechargeType = rechargeType;
    }

    public String getRechargeScene()
    {
        return rechargeScene;
    }

    public void setRechargeScene(String rechargeScene)
    {
        this.rechargeScene = rechargeScene;
    }

    public String getUpstreamOrderNo()
    {
        return upstreamOrderNo;
    }

    public void setUpstreamOrderNo(String upstreamOrderNo)
    {
        this.upstreamOrderNo = upstreamOrderNo;
    }

    public String getTxHash()
    {
        return txHash;
    }

    public void setTxHash(String txHash)
    {
        this.txHash = txHash;
    }

    public String getChainType()
    {
        return chainType;
    }

    public void setChainType(String chainType)
    {
        this.chainType = chainType;
    }

    public String getFailReason()
    {
        return failReason;
    }

    public void setFailReason(String failReason)
    {
        this.failReason = failReason;
    }

    public Date getCompletedAt()
    {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt)
    {
        this.completedAt = completedAt;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("orderNo", getOrderNo())
                .append("userId", getUserId())
                .append("cardId", getCardId())
                .append("amount", getAmount())
                .append("status", getStatus())
                .append("rechargeType", getRechargeType())
                .toString();
    }
}
