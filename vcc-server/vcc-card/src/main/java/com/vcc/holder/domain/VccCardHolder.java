package com.vcc.holder.domain;

import com.vcc.common.core.domain.BaseEntity;

public class VccCardHolder extends BaseEntity
{
    public static final String STATUS_ENABLED = "ENABLED";
    public static final String STATUS_DISABLED = "DISABLED";
    public static final String DELETED_FLAG_NORMAL = "0";
    public static final String DELETED_FLAG_DELETED = "1";

    private Long id;
    private Long userId;
    private Long merchantId;
    private String holderName;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String idCard;
    private String address;
    private String postCode;
    private String country;
    private String status;
    private String upstreamHolderId;
    private String deletedFlag;

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

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getHolderName()
    {
        return holderName;
    }

    public void setHolderName(String holderName)
    {
        this.holderName = holderName;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPostCode()
    {
        return postCode;
    }

    public void setPostCode(String postCode)
    {
        this.postCode = postCode;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getUpstreamHolderId()
    {
        return upstreamHolderId;
    }

    public void setUpstreamHolderId(String upstreamHolderId)
    {
        this.upstreamHolderId = upstreamHolderId;
    }

    public String getDeletedFlag()
    {
        return deletedFlag;
    }

    public void setDeletedFlag(String deletedFlag)
    {
        this.deletedFlag = deletedFlag;
    }
}
