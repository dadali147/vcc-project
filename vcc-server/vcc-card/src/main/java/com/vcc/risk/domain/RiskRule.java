package com.vcc.risk.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 风控规则 vcc_risk_rule
 */
public class RiskRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则类型：单笔限额 */
    public static final String RULE_TYPE_SINGLE_LIMIT = "SINGLE_LIMIT";
    /** 规则类型：日限额 */
    public static final String RULE_TYPE_DAILY_LIMIT = "DAILY_LIMIT";
    /** 规则类型：月限额 */
    public static final String RULE_TYPE_MONTHLY_LIMIT = "MONTHLY_LIMIT";

    /** 规则类型：单笔金额 */
    public static final String RULE_TYPE_SINGLE_AMOUNT = "single_amount";
    /** 规则类型：日累计金额 */
    public static final String RULE_TYPE_DAILY_AMOUNT = "daily_amount";
    /** 规则类型：日累计次数 */
    public static final String RULE_TYPE_DAILY_COUNT = "daily_count";

    /** 作用域：全局 */
    public static final String SCOPE_GLOBAL = "GLOBAL";
    /** 作用域：商户 */
    public static final String SCOPE_MERCHANT = "MERCHANT";

    /** 动作：拒绝（兼容旧值） */
    public static final String ACTION_REJECT = "REJECT";
    /** 动作：人工审核（兼容旧值） */
    public static final String ACTION_MANUAL_REVIEW = "MANUAL_REVIEW";

    /** 动作：拦截 */
    public static final String ACTION_BLOCK = "BLOCK";
    /** 动作：告警不拦截 */
    public static final String ACTION_ALERT = "ALERT";
    /** 动作：人工审核 */
    public static final String ACTION_REVIEW = "REVIEW";

    /** 合法 action 枚举集合 */
    public static final java.util.Set<String> VALID_ACTIONS = java.util.Set.of(
            ACTION_BLOCK, ACTION_ALERT, ACTION_REVIEW,
            ACTION_REJECT, ACTION_MANUAL_REVIEW);

    /** 卡类型：储值卡 */
    public static final String CARD_TYPE_PREPAID = "PREPAID";
    /** 卡类型：预算卡 */
    public static final String CARD_TYPE_BUDGET = "BUDGET";

    /** 状态：启用 */
    public static final String STATUS_ENABLED = "ENABLED";
    /** 状态：禁用 */
    public static final String STATUS_DISABLED = "DISABLED";

    private Long id;

    /** 规则编码 */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 规则类型 */
    private String ruleType;

    /** 作用域 */
    private String scope;

    /** 商户ID */
    private Long merchantId;

    /** 阈值金额 */
    private BigDecimal thresholdAmount;

    /** 阈值次数（用于 daily_count 规则） */
    private Integer thresholdCount;

    /** 优先级（数值越小越优先） */
    private Integer priority;

    /** 适用卡类型：PREPAID / BUDGET，null 表示全部 */
    private String cardType;

    /** 触发动作 */
    private String action;

    /** 状态 */
    private String status;

    /** 备注 */
    private String remark;

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

    public String getRuleCode()
    {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode)
    {
        this.ruleCode = ruleCode;
    }

    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
    }

    public String getRuleType()
    {
        return ruleType;
    }

    public void setRuleType(String ruleType)
    {
        this.ruleType = ruleType;
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

    public BigDecimal getThresholdAmount()
    {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount)
    {
        this.thresholdAmount = thresholdAmount;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
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
                .append("ruleCode", getRuleCode())
                .append("ruleName", getRuleName())
                .append("ruleType", getRuleType())
                .append("scope", getScope())
                .append("merchantId", getMerchantId())
                .append("thresholdAmount", getThresholdAmount())
                .append("thresholdCount", getThresholdCount())
                .append("priority", getPriority())
                .append("cardType", getCardType())
                .append("action", getAction())
                .append("status", getStatus())
                .toString();
    }

    public Integer getThresholdCount()
    {
        return thresholdCount;
    }

    public void setThresholdCount(Integer thresholdCount)
    {
        this.thresholdCount = thresholdCount;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public String getCardType()
    {
        return cardType;
    }

    public void setCardType(String cardType)
    {
        this.cardType = cardType;
    }
}
