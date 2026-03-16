package com.vcc.upstream.config;

import com.vcc.common.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * YeeVCC 上游配置。
 */
@Component
@ConfigurationProperties(prefix = "yeevcc")
public class YeeVccConfig
{
    private boolean enabled;

    private String baseUrl;

    private String appKey;

    private String customerId;

    private String privateKey;

    private String platformPublicKey;

    private String aesKey;

    private int connectTimeoutMillis = 5000;

    private int readTimeoutMillis = 15000;

    private int maxRetries = 2;

    private long retryIntervalMillis = 500L;

    private boolean verifyResponseSignature;

    private boolean failOnMissingSignature;

    private boolean autoDecryptKeyInfo = true;

    private int successCode = 200;

    private String securityReq = "YOP-RSA2048-SHA256";

    private HeaderNames headers = new HeaderNames();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getBaseUrl()
    {
        if (StringUtils.isBlank(baseUrl))
        {
            return baseUrl;
        }
        return StringUtils.removeEnd(baseUrl.trim(), "/");
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getAppKey()
    {
        return appKey;
    }

    public void setAppKey(String appKey)
    {
        this.appKey = appKey;
    }

    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public String getPlatformPublicKey()
    {
        return platformPublicKey;
    }

    public void setPlatformPublicKey(String platformPublicKey)
    {
        this.platformPublicKey = platformPublicKey;
    }

    public String getAesKey()
    {
        return aesKey;
    }

    public void setAesKey(String aesKey)
    {
        this.aesKey = aesKey;
    }

    public int getConnectTimeoutMillis()
    {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis)
    {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis()
    {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis)
    {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getMaxRetries()
    {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    public long getRetryIntervalMillis()
    {
        return retryIntervalMillis;
    }

    public void setRetryIntervalMillis(long retryIntervalMillis)
    {
        this.retryIntervalMillis = retryIntervalMillis;
    }

    public boolean isVerifyResponseSignature()
    {
        return verifyResponseSignature;
    }

    public void setVerifyResponseSignature(boolean verifyResponseSignature)
    {
        this.verifyResponseSignature = verifyResponseSignature;
    }

    public boolean isFailOnMissingSignature()
    {
        return failOnMissingSignature;
    }

    public void setFailOnMissingSignature(boolean failOnMissingSignature)
    {
        this.failOnMissingSignature = failOnMissingSignature;
    }

    public boolean isAutoDecryptKeyInfo()
    {
        return autoDecryptKeyInfo;
    }

    public void setAutoDecryptKeyInfo(boolean autoDecryptKeyInfo)
    {
        this.autoDecryptKeyInfo = autoDecryptKeyInfo;
    }

    public int getSuccessCode()
    {
        return successCode;
    }

    public void setSuccessCode(int successCode)
    {
        this.successCode = successCode;
    }

    public String getSecurityReq()
    {
        return securityReq;
    }

    public void setSecurityReq(String securityReq)
    {
        this.securityReq = securityReq;
    }

    public HeaderNames getHeaders()
    {
        return headers;
    }

    public void setHeaders(HeaderNames headers)
    {
        this.headers = headers;
    }

    public static class HeaderNames
    {
        private String appKey = "x-yop-appkey";

        private String requestId = "x-yop-request-id";

        private String timestamp = "x-yop-date";

        private String nonce = "x-yop-nonce";

        private String contentSha256 = "x-yop-content-sha256";

        private String signature = "x-yop-signature";

        private String signatureAlg = "x-yop-sign-alg";

        private String authorization = "Authorization";

        public String getAppKey()
        {
            return appKey;
        }

        public void setAppKey(String appKey)
        {
            this.appKey = appKey;
        }

        public String getRequestId()
        {
            return requestId;
        }

        public void setRequestId(String requestId)
        {
            this.requestId = requestId;
        }

        public String getTimestamp()
        {
            return timestamp;
        }

        public void setTimestamp(String timestamp)
        {
            this.timestamp = timestamp;
        }

        public String getNonce()
        {
            return nonce;
        }

        public void setNonce(String nonce)
        {
            this.nonce = nonce;
        }

        public String getContentSha256()
        {
            return contentSha256;
        }

        public void setContentSha256(String contentSha256)
        {
            this.contentSha256 = contentSha256;
        }

        public String getSignature()
        {
            return signature;
        }

        public void setSignature(String signature)
        {
            this.signature = signature;
        }

        public String getSignatureAlg()
        {
            return signatureAlg;
        }

        public void setSignatureAlg(String signatureAlg)
        {
            this.signatureAlg = signatureAlg;
        }

        public String getAuthorization()
        {
            return authorization;
        }

        public void setAuthorization(String authorization)
        {
            this.authorization = authorization;
        }
    }
}
