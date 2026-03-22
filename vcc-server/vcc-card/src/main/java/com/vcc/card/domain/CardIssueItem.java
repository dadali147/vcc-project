package com.vcc.card.domain;

import com.vcc.common.core.domain.BaseEntity;

/**
 * 开卡申请明细 vcc_card_issue_item
 */
public class CardIssueItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING    = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS    = "SUCCESS";
    public static final String STATUS_FAILED     = "FAILED";

    private Long id;

    private Long requestId;

    private Long holderId;

    private String cardProductId;

    private Integer quantity;

    /** 状态：PENDING/PROCESSING/SUCCESS/FAILED */
    private String status;

    /** YeeVCC 订单号 */
    private String yeevccOrderNo;

    /** 生成卡片ID */
    private Long cardId;

    private String failReason;

    /** DDL 基线：失败原因码 */
    private String failReasonCode;

    /** DDL 基线：标准化失败原因文案 */
    private String failReasonText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getHolderId() { return holderId; }
    public void setHolderId(Long holderId) { this.holderId = holderId; }

    public String getCardProductId() { return cardProductId; }
    public void setCardProductId(String cardProductId) { this.cardProductId = cardProductId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getYeevccOrderNo() { return yeevccOrderNo; }
    public void setYeevccOrderNo(String yeevccOrderNo) { this.yeevccOrderNo = yeevccOrderNo; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }

    public String getFailReasonCode() { return failReasonCode; }
    public void setFailReasonCode(String failReasonCode) { this.failReasonCode = failReasonCode; }

    public String getFailReasonText() { return failReasonText; }
    public void setFailReasonText(String failReasonText) { this.failReasonText = failReasonText; }
}
