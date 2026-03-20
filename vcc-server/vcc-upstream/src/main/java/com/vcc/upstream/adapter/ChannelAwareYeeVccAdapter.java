package com.vcc.upstream.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcc.channel.domain.VccChannel;
import com.vcc.channel.service.IChannelService;
import com.vcc.upstream.YeeVccClient;
import com.vcc.upstream.config.YeeVccConfig;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import com.vcc.upstream.exception.YeeVccException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 渠道感知的 YeeVCC 适配器。
 * <p>
 * 从数据库 vcc_channel 表动态加载 API 配置，替代静态 YeeVccConfig 硬编码。
 * 支持多渠道扩展——目前仅 YEEVCC，未来可按 channelCode 路由不同渠道。
 */
@Component
public class ChannelAwareYeeVccAdapter
{
    private static final Logger log = LoggerFactory.getLogger(ChannelAwareYeeVccAdapter.class);

    public static final String CHANNEL_CODE_YEEVCC = "YEEVCC";

    /**
     * YeeVccClient 实例缓存，key 为 channelCode。
     * <p>
     * 避免每次请求都创建新的 YeeVccClient（含 RestTemplate），高并发下显著降低 GC 压力和对象创建开销。
     * 当渠道配置变更（如 RSA 密钥轮换）时，调用 {@link #invalidateCache(String)} 或 {@link #invalidateAllCache()} 清除缓存。
     */
    private final ConcurrentHashMap<String, YeeVccClient> clientCache = new ConcurrentHashMap<>();

    @Autowired
    private IChannelService channelService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private YeeVccConfig staticConfig;

    /**
     * 获取基于数据库渠道配置的 YeeVccClient（带缓存）。
     * <p>
     * 优先从缓存获取；缓存未命中时创建新实例并放入缓存。
     * 若数据库无有效渠道记录，则回退到 application.yml 静态配置（同样缓存）。
     */
    public YeeVccClient buildClient(String channelCode)
    {
        return clientCache.computeIfAbsent(channelCode, this::doCreateClient);
    }

    /**
     * 清除指定渠道的客户端缓存。
     * <p>
     * 当渠道配置发生变更（如 RSA 密钥轮换、API 地址变更）时调用，
     * 下次 buildClient 会重新创建客户端实例。
     *
     * @param channelCode 渠道编码
     */
    public void invalidateCache(String channelCode)
    {
        YeeVccClient removed = clientCache.remove(channelCode);
        if (removed != null)
        {
            log.info("已清除渠道[{}]的 YeeVccClient 缓存", channelCode);
        }
    }

    /**
     * 清除所有渠道的客户端缓存。
     */
    public void invalidateAllCache()
    {
        int size = clientCache.size();
        clientCache.clear();
        log.info("已清除全部 YeeVccClient 缓存，共 {} 个", size);
    }

    private YeeVccClient doCreateClient(String channelCode)
    {
        VccChannel channel = channelService.getByChannelCode(channelCode);
        if (channel == null || !VccChannel.STATUS_ENABLED.equals(channel.getStatus()))
        {
            log.warn("渠道[{}]未找到或已停用，回退到静态配置", channelCode);
            return new YeeVccClient(staticConfig, objectMapper);
        }
        YeeVccConfig dbConfig = buildConfigFromChannel(channel);
        log.info("为渠道[{}]创建新的 YeeVccClient 实例", channelCode);
        return new YeeVccClient(dbConfig, objectMapper);
    }

    /**
     * 获取默认 YEEVCC 渠道客户端（最常用的入口）。
     */
    public YeeVccClient getYeeVccClient()
    {
        return buildClient(CHANNEL_CODE_YEEVCC);
    }

    // -----------------------------------------------------------------------
    // 持卡人操作
    // -----------------------------------------------------------------------

    public YeeVccApiResponse<YeeVccModels.CardHolderData> addCardHolder(
            YeeVccRequests.AddCardHolderRequest request)
    {
        return getYeeVccClient().addCardHolder(request);
    }

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.CardHolderData>> getCardHolder(
            YeeVccRequests.GetCardHolderRequest request)
    {
        return getYeeVccClient().getCardHolder(request);
    }

    // -----------------------------------------------------------------------
    // 开卡操作
    // -----------------------------------------------------------------------

    public YeeVccApiResponse<YeeVccModels.OpenCardTaskData> openCard(
            YeeVccRequests.OpenCardRequest request)
    {
        return getYeeVccClient().openCard(request);
    }

    public YeeVccApiResponse<YeeVccModels.OpenCardTaskData> queryOpenCardResult(
            YeeVccRequests.QueryOpenCardResultRequest request)
    {
        return getYeeVccClient().queryOpenCardResult(request);
    }

    // -----------------------------------------------------------------------
    // 卡片操作
    // -----------------------------------------------------------------------

    public YeeVccApiResponse<YeeVccModels.OperationData> activateCard(
            YeeVccRequests.ActivateCardRequest request)
    {
        return getYeeVccClient().activateCard(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> freezeCard(
            YeeVccRequests.FreezeCardRequest request)
    {
        return getYeeVccClient().freezeCard(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> unfreezeCard(
            YeeVccRequests.UnfreezeCardRequest request)
    {
        return getYeeVccClient().unfreezeCard(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> cancelCard(
            YeeVccRequests.CancelCardRequest request)
    {
        return getYeeVccClient().cancelCard(request);
    }

    public YeeVccApiResponse<YeeVccModels.CardKeyInfoData> getCardKeyInfo(
            YeeVccRequests.GetCardKeyInfoRequest request)
    {
        return getYeeVccClient().getCardKeyInfo(request);
    }

    // -----------------------------------------------------------------------
    // 充值 / 提现 / 交易
    // -----------------------------------------------------------------------

    public YeeVccApiResponse<YeeVccModels.OperationData> recharge(
            YeeVccRequests.RechargeRequest request)
    {
        return getYeeVccClient().recharge(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> queryRechargeResult(
            YeeVccRequests.QueryRechargeResultRequest request)
    {
        return getYeeVccClient().queryRechargeResult(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> withdraw(
            YeeVccRequests.WithdrawRequest request)
    {
        return getYeeVccClient().withdraw(request);
    }

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.TransactionData>> getTransactions(
            YeeVccRequests.GetTransactionsRequest request)
    {
        return getYeeVccClient().getTransactions(request);
    }

    // -----------------------------------------------------------------------
    // 账户操作
    // -----------------------------------------------------------------------

    public YeeVccApiResponse<YeeVccModels.AccountData> createAccount(
            YeeVccRequests.CreateAccountRequest request)
    {
        return getYeeVccClient().createAccount(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> accountTransferIn(
            YeeVccRequests.AccountTransferRequest request)
    {
        return getYeeVccClient().accountTransferIn(request);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> accountTransferOut(
            YeeVccRequests.AccountTransferRequest request)
    {
        return getYeeVccClient().accountTransferOut(request);
    }

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.AccountData>> getAccountInfo(
            YeeVccRequests.GetAccountInfoRequest request)
    {
        return getYeeVccClient().getAccountInfo(request);
    }

    // -----------------------------------------------------------------------
    // BIN 查询
    // -----------------------------------------------------------------------

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.CardBinData>> getCardBins(
            YeeVccRequests.GetCardBinsRequest request)
    {
        return getYeeVccClient().getCardBins(request);
    }

    // -----------------------------------------------------------------------
    // 私有：从 VccChannel 构建 YeeVccConfig
    // -----------------------------------------------------------------------

    private YeeVccConfig buildConfigFromChannel(VccChannel channel)
    {
        YeeVccConfig config = new YeeVccConfig();
        config.setEnabled(true);
        config.setBaseUrl(channel.getApiBaseUrl());

        if (channel.getApiConfig() != null)
        {
            try
            {
                JsonNode node = objectMapper.readTree(channel.getApiConfig());
                config.setAppKey(textOrNull(node, "appKey"));
                config.setCustomerId(textOrNull(node, "customerId"));
                config.setPrivateKey(textOrNull(node, "privateKey"));
                config.setPlatformPublicKey(textOrNull(node, "platformPublicKey"));
                config.setAesKey(textOrNull(node, "aesKey"));
                config.setSecurityReq(node.has("securityReq") ? node.get("securityReq").asText(config.getSecurityReq()) : config.getSecurityReq());
                config.setSuccessCode(node.has("successCode") ? node.get("successCode").asInt(config.getSuccessCode()) : config.getSuccessCode());
                config.setConnectTimeoutMillis(node.has("connectTimeoutMillis") ? node.get("connectTimeoutMillis").asInt(config.getConnectTimeoutMillis()) : config.getConnectTimeoutMillis());
                config.setReadTimeoutMillis(node.has("readTimeoutMillis") ? node.get("readTimeoutMillis").asInt(config.getReadTimeoutMillis()) : config.getReadTimeoutMillis());
                config.setMaxRetries(node.has("maxRetries") ? node.get("maxRetries").asInt(config.getMaxRetries()) : config.getMaxRetries());
                config.setRetryIntervalMillis(node.has("retryIntervalMillis") ? node.get("retryIntervalMillis").asLong(config.getRetryIntervalMillis()) : config.getRetryIntervalMillis());
                config.setDemoMode(node.has("demoMode") && node.get("demoMode").asBoolean());
                config.setVerifyResponseSignature(node.has("verifyResponseSignature") && node.get("verifyResponseSignature").asBoolean());
                config.setFailOnMissingSignature(node.has("failOnMissingSignature") && node.get("failOnMissingSignature").asBoolean());
                config.setAutoDecryptKeyInfo(!node.has("autoDecryptKeyInfo") || node.get("autoDecryptKeyInfo").asBoolean(true));
            }
            catch (Exception e)
            {
                throw new YeeVccException("渠道[" + channel.getChannelCode() + "] api_config 解析失败: " + e.getMessage(), e);
            }
        }
        return config;
    }

    private String textOrNull(JsonNode node, String field)
    {
        return node.has(field) ? node.get(field).asText(null) : null;
    }
}
