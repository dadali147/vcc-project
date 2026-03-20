package com.vcc.card.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 卡片操作记录 vcc_card_operation_log
 */
public class CardOperationLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 操作类型：冻结 */
    public static final String OP_FREEZE = "FREEZE";
    /** 操作类型：解冻 */
    public static final String OP_UNFREEZE = "UNFREEZE";
    /** 操作类型：销卡 */
    public static final String OP_CANCEL = "CANCEL";
    /** 操作类型：激活 */
    public static final String OP_ACTIVATE = "ACTIVATE";
    /** 操作类型：额度变更 */
    public static final String OP_LIMIT_CHANGE = "LIMIT_CHANGE";
    /** 操作类型：备注修改 */
    public static final String OP_REMARK_UPDATE = "REMARK_UPDATE";
    /** 操作类型：状态变更 */
    public static final String OP_STATUS_CHANGE = "STATUS_CHANGE";

    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** 卡片ID */
    private Long cardId;

    /** 持卡人ID */
    private Long holderId;

    /** 操作类型 */
    private String operationType;

    /** 操作前值 */
    private String beforeValue;

    /** 操作后值 */
    private String afterValue;

    /** 操作人ID */
    private Long operatorId;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    /** 备注 */
    private String remark;

    /** 逻辑删除 */
    private String delFlag;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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

    public Long getHolderId()
    {
        return holderId;
    }

    public void setHolderId(Long holderId)
    {
        this.holderId = holderId;
    }

    public String getOperationType()
    {
        return operationType;
    }

    public void setOperationType(String operationType)
    {
        this.operationType = operationType;
    }

    public String getBeforeValue()
    {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue)
    {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue()
    {
        return afterValue;
    }

    public void setAfterValue(String afterValue)
    {
        this.afterValue = afterValue;
    }

    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("merchantId", getMerchantId())
            .append("cardId", getCardId())
            .append("holderId", getHolderId())
            .append("operationType", getOperationType())
            .append("beforeValue", getBeforeValue())
            .append("afterValue", getAfterValue())
            .append("operatorId", getOperatorId())
            .append("operateTime", getOperateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
