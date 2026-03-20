package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 开卡申请单 vcc_card_issue_request
 */
public class CardIssueRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING     = "PENDING";
    public static final String STATUS_PROCESSING  = "PROCESSING";
    public static final String STATUS_COMPLETED   = "COMPLETED";
    public static final String STATUS_PARTIAL     = "PARTIAL";
    public static final String STATUS_FAILED      = "FAILED";
    public static final String STATUS_CANCELLED   = "CANCELLED";

    public static final String CARD_TYPE_PREPAID  = "PREPAID";
    public static final String CARD_TYPE_BUDGET   = "BUDGET";

    private Long id;

    private Long merchantId;

    /** 申请批次号 */
    private String batchNo;

    /** 卡类型：PREPAID/BUDGET */
    private String cardType;

    private Long binId;

    private Integer totalCount;

    private Integer successCount;

    private Integer failCount;

    /** 状态：PENDING/PROCESSING/COMPLETED/PARTIAL/FAILED/CANCELLED */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public Long getBinId() { return binId; }
    public void setBinId(Long binId) { this.binId = binId; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getFailCount() { return failCount; }
    public void setFailCount(Integer failCount) { this.failCount = failCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }

    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
}
