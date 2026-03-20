package com.vcc.channel.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vcc.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 商户BIN分配 vcc_merchant_bin
 */
public class VccMerchantBin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_DISABLED = "DISABLED";

    private Long id;

    /** 商户ID */
    private Long merchantId;

    /** BIN ID（关联vcc_card_bin.id） */
    private Long binId;

    /** 分配状态：ASSIGNED/DISABLED */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignedAt;

    /** 分配操作人ID */
    private Long assignedBy;

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

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Long getBinId()
    {
        return binId;
    }

    public void setBinId(Long binId)
    {
        this.binId = binId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getAssignedAt()
    {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt)
    {
        this.assignedAt = assignedAt;
    }

    public Long getAssignedBy()
    {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy)
    {
        this.assignedBy = assignedBy;
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
