package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 费率配置 vcc_fee_config
 */
public class FeeConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 费用类型：开卡费 */
    public static final String FEE_TYPE_OPEN = "OPEN";
    /** 费用类型：月费 */
    public static final String FEE_TYPE_MONTHLY = "MONTHLY";
    /** 费用类型：交易手续费 */
    public static final String FEE_TYPE_TXN = "TXN";
    /** 费用类型：充值手续费 */
    public static final String FEE_TYPE_RECHARGE = "RECHARGE";

    private Long id;

    /** 用户ID（0表示默认配置） */
    private Long userId;

    /** 作用域：PLATFORM/MERCHANT */
    private String scope;

    /** 商户ID */
    private Long merchantId;

    /** 卡Bin ID（null表示所有Bin） */
    private String cardBinId;

    /** 费用类型：OPEN/MONTHLY/TXN */
    private String feeType;

    /** 条件类型 */
    private String conditionType;

    /** 阈值金额 */
    private BigDecimal thresholdAmount;

    /** 费率（百分比） */
    private BigDecimal rate;

    /** 固定金额 */
    private BigDecimal fixedAmount;

    /** 最低手续费 */
    private BigDecimal minFee;

    /** 最高手续费 */
    private BigDecimal maxFee;

    /** 扣费来源 */
    private String chargeSource;

    /** 版本号 */
    private Integer versionNo;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;

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

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getCardBinId()
    {
        return cardBinId;
    }

    public void setCardBinId(String cardBinId)
    {
        this.cardBinId = cardBinId;
    }

    public String getFeeType()
    {
        return feeType;
    }

    public void setFeeType(String feeType)
    {
        this.feeType = feeType;
    }

    public String getConditionType()
    {
        return conditionType;
    }

    public void setConditionType(String conditionType)
    {
        this.conditionType = conditionType;
    }

    public BigDecimal getThresholdAmount()
    {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount)
    {
        this.thresholdAmount = thresholdAmount;
    }

    public BigDecimal getRate()
    {
        return rate;
    }

    public void setRate(BigDecimal rate)
    {
        this.rate = rate;
    }

    public BigDecimal getFixedAmount()
    {
        return fixedAmount;
    }

    public void setFixedAmount(BigDecimal fixedAmount)
    {
        this.fixedAmount = fixedAmount;
    }

    public BigDecimal getMinFee()
    {
        return minFee;
    }

    public void setMinFee(BigDecimal minFee)
    {
        this.minFee = minFee;
    }

    public BigDecimal getMaxFee()
    {
        return maxFee;
    }

    public void setMaxFee(BigDecimal maxFee)
    {
        this.maxFee = maxFee;
    }

    public String getChargeSource()
    {
        return chargeSource;
    }

    public void setChargeSource(String chargeSource)
    {
        this.chargeSource = chargeSource;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Date getEffectiveDate()
    {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpiryDate()
    {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate)
    {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("scope", getScope())
                .append("merchantId", getMerchantId())
                .append("cardBinId", getCardBinId())
                .append("feeType", getFeeType())
                .append("conditionType", getConditionType())
                .append("rate", getRate())
                .append("fixedAmount", getFixedAmount())
                .append("status", getStatus())
                .toString();
    }
}
