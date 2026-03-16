package com.vcc.upstream.dto;

import java.math.BigDecimal;

/**
 * YeeVCC 请求 DTO 集合。
 */
public final class YeeVccRequests
{
    private YeeVccRequests()
    {
    }

    public static class AddCardHolderRequest extends YeeVccBaseRequest
    {
        private String customerId;
        private String firstName;
        private String lastName;
        private String email;
        private String country;
        private String city;
        private String state;
        private String address;
        private String postCode;
        private String mobilePrefix;
        private String phone;
        private String nationality;
        private String birthday;

        public String getCustomerId()
        {
            return customerId;
        }

        public void setCustomerId(String customerId)
        {
            this.customerId = customerId;
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

        public String getEmail()
        {
            return email;
        }

        public void setEmail(String email)
        {
            this.email = email;
        }

        public String getCountry()
        {
            return country;
        }

        public void setCountry(String country)
        {
            this.country = country;
        }

        public String getCity()
        {
            return city;
        }

        public void setCity(String city)
        {
            this.city = city;
        }

        public String getState()
        {
            return state;
        }

        public void setState(String state)
        {
            this.state = state;
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

        public String getMobilePrefix()
        {
            return mobilePrefix;
        }

        public void setMobilePrefix(String mobilePrefix)
        {
            this.mobilePrefix = mobilePrefix;
        }

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }

        public String getNationality()
        {
            return nationality;
        }

        public void setNationality(String nationality)
        {
            this.nationality = nationality;
        }

        public String getBirthday()
        {
            return birthday;
        }

        public void setBirthday(String birthday)
        {
            this.birthday = birthday;
        }
    }

    public static class GetCardHolderRequest extends YeeVccBaseRequest
    {
        private String customerId;
        private String cardholderId;
        private String email;
        private Long current = 1L;
        private Long size = 20L;

        public String getCustomerId()
        {
            return customerId;
        }

        public void setCustomerId(String customerId)
        {
            this.customerId = customerId;
        }

        public String getCardholderId()
        {
            return cardholderId;
        }

        public void setCardholderId(String cardholderId)
        {
            this.cardholderId = cardholderId;
        }

        public String getEmail()
        {
            return email;
        }

        public void setEmail(String email)
        {
            this.email = email;
        }

        public Long getCurrent()
        {
            return current;
        }

        public void setCurrent(Long current)
        {
            this.current = current;
        }

        public Long getSize()
        {
            return size;
        }

        public void setSize(Long size)
        {
            this.size = size;
        }
    }

    public static class OpenCardRequest extends YeeVccBaseRequest
    {
        private String cardBinId;
        private String cardholderId;
        private String currency;
        private Integer cardType = 1;
        private BigDecimal amount;
        private String sharedAccountNo;

        public String getCardBinId()
        {
            return cardBinId;
        }

        public void setCardBinId(String cardBinId)
        {
            this.cardBinId = cardBinId;
        }

        public String getCardholderId()
        {
            return cardholderId;
        }

        public void setCardholderId(String cardholderId)
        {
            this.cardholderId = cardholderId;
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            this.currency = currency;
        }

        public Integer getCardType()
        {
            return cardType;
        }

        public void setCardType(Integer cardType)
        {
            this.cardType = cardType;
        }

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public String getSharedAccountNo()
        {
            return sharedAccountNo;
        }

        public void setSharedAccountNo(String sharedAccountNo)
        {
            this.sharedAccountNo = sharedAccountNo;
        }
    }

    public static class QueryOpenCardResultRequest extends YeeVccBaseRequest
    {
        private Long taskId;

        public Long getTaskId()
        {
            return taskId;
        }

        public void setTaskId(Long taskId)
        {
            this.taskId = taskId;
        }
    }

    public static class ActivateCardRequest extends YeeVccBaseRequest
    {
        private String cardId;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }
    }

    public static class GetCardKeyInfoRequest extends YeeVccBaseRequest
    {
        private String cardId;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }
    }

    public static class FreezeCardRequest extends YeeVccBaseRequest
    {
        private String cardId;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }
    }

    public static class UnfreezeCardRequest extends YeeVccBaseRequest
    {
        private String cardId;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }
    }

    public static class CancelCardRequest extends YeeVccBaseRequest
    {
        private String cardId;
        private String refundCurrency;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public String getRefundCurrency()
        {
            return refundCurrency;
        }

        public void setRefundCurrency(String refundCurrency)
        {
            this.refundCurrency = refundCurrency;
        }
    }

    public static class RechargeRequest extends YeeVccBaseRequest
    {
        private String cardId;
        private BigDecimal amount;
        private String currency;
        private String deductCurrency;
        private String orderId;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            this.currency = currency;
        }

        public String getDeductCurrency()
        {
            return deductCurrency;
        }

        public void setDeductCurrency(String deductCurrency)
        {
            this.deductCurrency = deductCurrency;
        }

        public String getOrderId()
        {
            return orderId;
        }

        public void setOrderId(String orderId)
        {
            this.orderId = orderId;
        }
    }

    public static class QueryRechargeResultRequest extends YeeVccBaseRequest
    {
        private String cardId;
        private String orderId;
        private String originalRequestNo;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public String getOrderId()
        {
            return orderId;
        }

        public void setOrderId(String orderId)
        {
            this.orderId = orderId;
        }

        public String getOriginalRequestNo()
        {
            return originalRequestNo;
        }

        public void setOriginalRequestNo(String originalRequestNo)
        {
            this.originalRequestNo = originalRequestNo;
        }
    }

    public static class WithdrawRequest extends YeeVccBaseRequest
    {
        private String cardId;
        private BigDecimal amount;
        private String currency;
        private String orderId;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            this.currency = currency;
        }

        public String getOrderId()
        {
            return orderId;
        }

        public void setOrderId(String orderId)
        {
            this.orderId = orderId;
        }
    }

    public static class GetTransactionsRequest extends YeeVccBaseRequest
    {
        private String cardId;
        private String startDate;
        private String endDate;
        private Long current = 1L;
        private Long size = 50L;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public String getStartDate()
        {
            return startDate;
        }

        public void setStartDate(String startDate)
        {
            this.startDate = startDate;
        }

        public String getEndDate()
        {
            return endDate;
        }

        public void setEndDate(String endDate)
        {
            this.endDate = endDate;
        }

        public Long getCurrent()
        {
            return current;
        }

        public void setCurrent(Long current)
        {
            this.current = current;
        }

        public Long getSize()
        {
            return size;
        }

        public void setSize(Long size)
        {
            this.size = size;
        }
    }

    public static class GetCardBinsRequest extends YeeVccBaseRequest
    {
        private String customerId;

        public String getCustomerId()
        {
            return customerId;
        }

        public void setCustomerId(String customerId)
        {
            this.customerId = customerId;
        }
    }
}
