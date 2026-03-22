package com.vcc.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 限额调整历史 vcc_limit_history
 */
public class LimitHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String LIMIT_TYPE_SINGLE  = "SINGLE";
    public static final String LIMIT_TYPE_DAILY   = "DAILY";
    public static final String LIMIT_TYPE_MONTHLY = "MONTHLY";

    private Long id;

    private Long merchantId;

    private Long cardId;

    /** DDL 基线：持卡人ID */
    private Long holderId;

    /** 限额类型：SINGLE/DAILY/MONTHLY */
    private String limitType;

    private BigDecimal beforeAmount;

    private BigDecimal afterAmount;

    /** DDL 基线：调整金额 */
    private BigDecimal adjustAmount;

    /** 操作人ID */
    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    /** DDL 基线：调整时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date adjustTime;

    /** 操作原因 */
    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public Long getHolderId() { return holderId; }
    public void setHolderId(Long holderId) { this.holderId = holderId; }

    public String getLimitType() { return limitType; }
    public void setLimitType(String limitType) { this.limitType = limitType; }

    public BigDecimal getBeforeAmount() { return beforeAmount; }
    public void setBeforeAmount(BigDecimal beforeAmount) { this.beforeAmount = beforeAmount; }

    public BigDecimal getAfterAmount() { return afterAmount; }
    public void setAfterAmount(BigDecimal afterAmount) { this.afterAmount = afterAmount; }

    public BigDecimal getAdjustAmount() { return adjustAmount; }
    public void setAdjustAmount(BigDecimal adjustAmount) { this.adjustAmount = adjustAmount; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Date getOperateTime() { return operateTime; }
    public void setOperateTime(Date operateTime) { this.operateTime = operateTime; }

    public Date getAdjustTime() { return adjustTime; }
    public void setAdjustTime(Date adjustTime) { this.adjustTime = adjustTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
