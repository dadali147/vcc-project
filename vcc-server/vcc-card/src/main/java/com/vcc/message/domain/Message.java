package com.vcc.message.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 站内消息 vcc_message
 */
public class Message extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 接收者类型：商户 */
    public static final String RECEIVER_TYPE_MERCHANT = "MERCHANT";
    /** 接收者类型：用户 */
    public static final String RECEIVER_TYPE_USER = "USER";
    /** 接收者类型：子账户 */
    public static final String RECEIVER_TYPE_SUB_ACCOUNT = "SUB_ACCOUNT";

    /** 消息类型：系统通知 */
    public static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    /** 消息类型：业务通知 */
    public static final String MESSAGE_TYPE_BUSINESS = "BUSINESS";
    /** 消息类型：风控通知 */
    public static final String MESSAGE_TYPE_RISK = "RISK";
    /** 消息类型：KYC通知 */
    public static final String MESSAGE_TYPE_KYC = "KYC";

    /** 未读 */
    public static final String IS_READ_NO = "0";
    /** 已读 */
    public static final String IS_READ_YES = "1";

    /** 未删除 */
    public static final String DEL_FLAG_NORMAL = "0";
    /** 已删除 */
    public static final String DEL_FLAG_DELETED = "1";

    private Long id;

    /** 接收者类型：MERCHANT/USER/SUB_ACCOUNT */
    private String receiverType;

    /** 接收者ID */
    private Long receiverId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型：SYSTEM/BUSINESS/RISK/KYC */
    private String messageType;

    /** 是否已读（0未读 1已读） */
    private String isRead;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    /** 关联业务类型 */
    private String businessType;

    /** 关联业务ID */
    private String businessId;

    /** 删除标志（0正常 1删除） */
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

    public String getReceiverType()
    {
        return receiverType;
    }

    public void setReceiverType(String receiverType)
    {
        this.receiverType = receiverType;
    }

    public Long getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(Long receiverId)
    {
        this.receiverId = receiverId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getIsRead()
    {
        return isRead;
    }

    public void setIsRead(String isRead)
    {
        this.isRead = isRead;
    }

    public Date getReadTime()
    {
        return readTime;
    }

    public void setReadTime(Date readTime)
    {
        this.readTime = readTime;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public String getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(String businessId)
    {
        this.businessId = businessId;
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
                .append("receiverType", getReceiverType())
                .append("receiverId", getReceiverId())
                .append("title", getTitle())
                .append("messageType", getMessageType())
                .append("isRead", getIsRead())
                .append("businessType", getBusinessType())
                .append("businessId", getBusinessId())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
