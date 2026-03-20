package com.vcc.channel.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡BIN管理 vcc_card_bin
 */
public class VccCardBin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";

    public static final String CARD_TYPE_PREPAID = "PREPAID";
    public static final String CARD_TYPE_BUDGET = "BUDGET";

    private Long id;

    /** BIN号（6位） */
    private String bin;

    /** 卡组织，如 VISA/MASTERCARD */
    private String cardOrg;

    /** 卡类型：PREPAID/BUDGET */
    private String cardType;

    /** 所属渠道ID */
    private Long channelId;

    /** 上游卡产品ID */
    private String cardProductId;

    /** 默认额度 */
    private BigDecimal defaultLimitAmount;

    /** 状态：ENABLED/DISABLED */
    private String status;

    /** 逻辑删除：0正常，1删除 */
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

    public String getBin()
    {
        return bin;
    }

    public void setBin(String bin)
    {
        this.bin = bin;
    }

    public String getCardOrg()
    {
        return cardOrg;
    }

    public void setCardOrg(String cardOrg)
    {
        this.cardOrg = cardOrg;
    }

    public String getCardType()
    {
        return cardType;
    }

    public void setCardType(String cardType)
    {
        this.cardType = cardType;
    }

    public Long getChannelId()
    {
        return channelId;
    }

    public void setChannelId(Long channelId)
    {
        this.channelId = channelId;
    }

    public String getCardProductId()
    {
        return cardProductId;
    }

    public void setCardProductId(String cardProductId)
    {
        this.cardProductId = cardProductId;
    }

    public BigDecimal getDefaultLimitAmount()
    {
        return defaultLimitAmount;
    }

    public void setDefaultLimitAmount(BigDecimal defaultLimitAmount)
    {
        this.defaultLimitAmount = defaultLimitAmount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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
}
