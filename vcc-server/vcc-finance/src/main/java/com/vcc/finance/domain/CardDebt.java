package com.vcc.finance.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 欠费记录 vcc_card_debt (PREPAID 卡余额负数时自动产生)
 */
public class CardDebt extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_OUTSTANDING = "OUTSTANDING";
    public static final String STATUS_SETTLED     = "SETTLED";

    /** DDL 基线：未结清 */
    public static final String STATUS_UNPAID      = "UNPAID";

    private Long id;

    private Long merchantId;

    private Long cardId;

    private BigDecimal debtAmount;

    /** DDL 基线：欠费明细JSON */
    private String feeDetails;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date occurredAt;

    /** 状态：OUTSTANDING/SETTLED */
    private String status;

    private BigDecimal settleAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date settleTime;

    /** DDL 基线：结清时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date settledAt;

    /** DDL 基线：结清操作人 */
    private Long settledBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public BigDecimal getDebtAmount() { return debtAmount; }
    public void setDebtAmount(BigDecimal debtAmount) { this.debtAmount = debtAmount; }

    public Date getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Date occurredAt) { this.occurredAt = occurredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getSettleAmount() { return settleAmount; }
    public void setSettleAmount(BigDecimal settleAmount) { this.settleAmount = settleAmount; }

    public Date getSettleTime() { return settleTime; }
    public void setSettleTime(Date settleTime) { this.settleTime = settleTime; }

    public String getFeeDetails() { return feeDetails; }
    public void setFeeDetails(String feeDetails) { this.feeDetails = feeDetails; }

    public Date getSettledAt() { return settledAt; }
    public void setSettledAt(Date settledAt) { this.settledAt = settledAt; }

    public Long getSettledBy() { return settledBy; }
    public void setSettledBy(Long settledBy) { this.settledBy = settledBy; }
}
