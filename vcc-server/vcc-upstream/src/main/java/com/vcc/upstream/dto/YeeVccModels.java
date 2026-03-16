package com.vcc.upstream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * YeeVCC 响应 DTO 集合。
 */
public final class YeeVccModels
{
    private YeeVccModels()
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageData<T>
    {
        private Long current;
        private Long size;
        private Long total;
        private List<T> records = new ArrayList<>();

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

        public Long getTotal()
        {
            return total;
        }

        public void setTotal(Long total)
        {
            this.total = total;
        }

        public List<T> getRecords()
        {
            return records;
        }

        public void setRecords(List<T> records)
        {
            this.records = records;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardHolderData
    {
        private String id;
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
        private String status;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

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

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardData
    {
        private String cardId;
        private String cardNumber;
        private String maskedCardNumber;
        private String cardStatus;
        private String status;
        private String cardBinId;
        private String cardholderId;
        private String sharedAccountNo;
        private String currency;
        private BigDecimal balance;
        private BigDecimal availableBalance;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public String getCardNumber()
        {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber)
        {
            this.cardNumber = cardNumber;
        }

        public String getMaskedCardNumber()
        {
            return maskedCardNumber;
        }

        public void setMaskedCardNumber(String maskedCardNumber)
        {
            this.maskedCardNumber = maskedCardNumber;
        }

        public String getCardStatus()
        {
            return cardStatus;
        }

        public void setCardStatus(String cardStatus)
        {
            this.cardStatus = cardStatus;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

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

        public String getSharedAccountNo()
        {
            return sharedAccountNo;
        }

        public void setSharedAccountNo(String sharedAccountNo)
        {
            this.sharedAccountNo = sharedAccountNo;
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            this.currency = currency;
        }

        public BigDecimal getBalance()
        {
            return balance;
        }

        public void setBalance(BigDecimal balance)
        {
            this.balance = balance;
        }

        public BigDecimal getAvailableBalance()
        {
            return availableBalance;
        }

        public void setAvailableBalance(BigDecimal availableBalance)
        {
            this.availableBalance = availableBalance;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OpenCardTaskData
    {
        private Long taskId;
        private Integer status;
        private List<CardData> cardList = new ArrayList<>();

        public Long getTaskId()
        {
            return taskId;
        }

        public void setTaskId(Long taskId)
        {
            this.taskId = taskId;
        }

        public Integer getStatus()
        {
            return status;
        }

        public void setStatus(Integer status)
        {
            this.status = status;
        }

        public List<CardData> getCardList()
        {
            return cardList;
        }

        public void setCardList(List<CardData> cardList)
        {
            this.cardList = cardList;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardKeyInfoData
    {
        private String cardId;
        private String cardNumber;
        private String cvv;
        private String expiryDate;
        private String encryptedCardNumber;
        private String encryptedCvv;
        private String encryptedExpiryDate;

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public String getCardNumber()
        {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber)
        {
            this.cardNumber = cardNumber;
        }

        public String getCvv()
        {
            return cvv;
        }

        public void setCvv(String cvv)
        {
            this.cvv = cvv;
        }

        public String getExpiryDate()
        {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate)
        {
            this.expiryDate = expiryDate;
        }

        public String getEncryptedCardNumber()
        {
            return encryptedCardNumber;
        }

        public void setEncryptedCardNumber(String encryptedCardNumber)
        {
            this.encryptedCardNumber = encryptedCardNumber;
        }

        public String getEncryptedCvv()
        {
            return encryptedCvv;
        }

        public void setEncryptedCvv(String encryptedCvv)
        {
            this.encryptedCvv = encryptedCvv;
        }

        public String getEncryptedExpiryDate()
        {
            return encryptedExpiryDate;
        }

        public void setEncryptedExpiryDate(String encryptedExpiryDate)
        {
            this.encryptedExpiryDate = encryptedExpiryDate;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperationData
    {
        private String cardId;
        private String orderId;
        private String transactionId;
        private String status;
        private String currency;
        private BigDecimal amount;
        private BigDecimal balance;
        private String message;

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

        public String getTransactionId()
        {
            return transactionId;
        }

        public void setTransactionId(String transactionId)
        {
            this.transactionId = transactionId;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            this.currency = currency;
        }

        public BigDecimal getAmount()
        {
            return amount;
        }

        public void setAmount(BigDecimal amount)
        {
            this.amount = amount;
        }

        public BigDecimal getBalance()
        {
            return balance;
        }

        public void setBalance(BigDecimal balance)
        {
            this.balance = balance;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionData
    {
        private String txnId;
        private String cardId;
        private String cardNumber;
        private String transactionType;
        private String status;
        private String merchantName;
        private String merchantCountry;
        private String mcc;
        private BigDecimal amount;
        private String currency;
        private BigDecimal billingAmount;
        private String billingCurrency;
        private String authTime;
        private String settleTime;
        private String description;

        public String getTxnId()
        {
            return txnId;
        }

        public void setTxnId(String txnId)
        {
            this.txnId = txnId;
        }

        public String getCardId()
        {
            return cardId;
        }

        public void setCardId(String cardId)
        {
            this.cardId = cardId;
        }

        public String getCardNumber()
        {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber)
        {
            this.cardNumber = cardNumber;
        }

        public String getTransactionType()
        {
            return transactionType;
        }

        public void setTransactionType(String transactionType)
        {
            this.transactionType = transactionType;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

        public String getMerchantName()
        {
            return merchantName;
        }

        public void setMerchantName(String merchantName)
        {
            this.merchantName = merchantName;
        }

        public String getMerchantCountry()
        {
            return merchantCountry;
        }

        public void setMerchantCountry(String merchantCountry)
        {
            this.merchantCountry = merchantCountry;
        }

        public String getMcc()
        {
            return mcc;
        }

        public void setMcc(String mcc)
        {
            this.mcc = mcc;
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

        public BigDecimal getBillingAmount()
        {
            return billingAmount;
        }

        public void setBillingAmount(BigDecimal billingAmount)
        {
            this.billingAmount = billingAmount;
        }

        public String getBillingCurrency()
        {
            return billingCurrency;
        }

        public void setBillingCurrency(String billingCurrency)
        {
            this.billingCurrency = billingCurrency;
        }

        public String getAuthTime()
        {
            return authTime;
        }

        public void setAuthTime(String authTime)
        {
            this.authTime = authTime;
        }

        public String getSettleTime()
        {
            return settleTime;
        }

        public void setSettleTime(String settleTime)
        {
            this.settleTime = settleTime;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardBinData
    {
        private String id;
        private String bin;
        private String productCode;
        private String productName;
        private String currency;
        private BigDecimal openCardFee;
        private String scene;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
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

        public String getProductCode()
        {
            return productCode;
        }

        public void setProductCode(String productCode)
        {
            this.productCode = productCode;
        }

        public String getProductName()
        {
            return productName;
        }

        public void setProductName(String productName)
        {
            this.productName = productName;
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            this.currency = currency;
        }

        public BigDecimal getOpenCardFee()
        {
            return openCardFee;
        }

        public void setOpenCardFee(BigDecimal openCardFee)
        {
            this.openCardFee = openCardFee;
        }

        public String getScene()
        {
            return scene;
        }

        public void setScene(String scene)
        {
            this.scene = scene;
        }
    }
}
