package com.vcc.user.domain;

import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 用户账户 vcc_user_account
 */
public class UserAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private BigDecimal balance;

    private BigDecimal frozenAmount;

    private String currency;

    private BigDecimal totalRecharge;

    private BigDecimal totalWithdraw;

    /** 状态（1正常 0停用） */
    private Integer status;

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

    public BigDecimal getBalance()
    {
        return balance;
    }

    public void setBalance(BigDecimal balance)
    {
        this.balance = balance;
    }

    public BigDecimal getFrozenAmount()
    {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount)
    {
        this.frozenAmount = frozenAmount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public BigDecimal getTotalRecharge()
    {
        return totalRecharge;
    }

    public void setTotalRecharge(BigDecimal totalRecharge)
    {
        this.totalRecharge = totalRecharge;
    }

    public BigDecimal getTotalWithdraw()
    {
        return totalWithdraw;
    }

    public void setTotalWithdraw(BigDecimal totalWithdraw)
    {
        this.totalWithdraw = totalWithdraw;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("balance", getBalance())
                .append("frozenAmount", getFrozenAmount())
                .append("currency", getCurrency())
                .append("status", getStatus())
                .toString();
    }
}
