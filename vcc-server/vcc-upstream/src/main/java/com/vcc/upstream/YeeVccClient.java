package com.vcc.upstream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcc.common.utils.StringUtils;
import com.vcc.upstream.config.YeeVccConfig;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccBaseRequest;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import com.vcc.upstream.exception.YeeVccException;
import com.vcc.upstream.util.Aes16CryptoUtils;
import com.vcc.upstream.util.Rsa2048SignatureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * YeeVCC 上游客户端。
 */
@Component
public class YeeVccClient
{
    private static final Logger log = LoggerFactory.getLogger(YeeVccClient.class);

    private static final String PATH_ADD_CARD_HOLDER = "/rest/v1.0/vcc/user-account/create";
    private static final List<String> PATH_CARD_HOLDER_CANDIDATES = List.of(
            "/rest/v1.0/vcc/card-holders",
            "/rest/v1.0/vcc/card-holder"
    );
    private static final String PATH_OPEN_CARD = "/rest/v1.0/vcc/card-open";
    private static final String PATH_QUERY_OPEN_CARD = "/rest/v1.0/vcc/card-open-task";
    private static final String PATH_ACTIVATE_CARD = "/rest/v1.0/vcc/card-activate";
    private static final List<String> PATH_CARD_KEY_INFO_CANDIDATES = List.of(
            "/rest/v1.0/vcc/card-key-info",
            "/rest/v1.0/vcc/card-keyinfo"
    );
    private static final String PATH_FREEZE_CARD = "/rest/v1.0/vcc/card-freeze";
    private static final String PATH_UNFREEZE_CARD = "/rest/v1.0/vcc/card-unfreeze";
    private static final String PATH_CANCEL_CARD = "/rest/v1.0/vcc/card-cancel";
    private static final String PATH_RECHARGE = "/rest/v1.0/vcc/card-recharge";
    private static final String PATH_QUERY_RECHARGE = "/rest/v1.0/vcc/card-recharge-query";
    private static final String PATH_WITHDRAW = "/rest/v1.0/vcc/card-charge-out";
    private static final String PATH_TRANSACTIONS = "/rest/v1.0/vcc/transactions";
    private static final String PATH_CARD_BINS = "/rest/v1.0/vcc/card-bins";

    private final YeeVccConfig config;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public YeeVccClient(YeeVccConfig config, ObjectMapper objectMapper)
    {
        this.config = config;
        this.objectMapper = objectMapper;
        this.restTemplate = createRestTemplate(config);
    }

    public YeeVccApiResponse<YeeVccModels.CardHolderData> addCardHolder(YeeVccRequests.AddCardHolderRequest request)
    {
        YeeVccRequests.AddCardHolderRequest safeRequest = requireRequest(request, "addCardHolder");
        fillCustomerId(safeRequest::getCustomerId, safeRequest::setCustomerId);
        return execute(HttpMethod.POST, PATH_ADD_CARD_HOLDER, safeRequest,
                node -> mapNode(node, YeeVccModels.CardHolderData.class));
    }

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.CardHolderData>> getCardHolder(
            YeeVccRequests.GetCardHolderRequest request)
    {
        YeeVccRequests.GetCardHolderRequest safeRequest = requireRequest(request, "getCardHolder");
        fillCustomerId(safeRequest::getCustomerId, safeRequest::setCustomerId);
        return executeWithFallback(HttpMethod.GET, PATH_CARD_HOLDER_CANDIDATES, safeRequest,
                node -> mapPageData(node, YeeVccModels.CardHolderData.class, "records", "list", "items"));
    }

    public YeeVccApiResponse<YeeVccModels.OpenCardTaskData> openCard(YeeVccRequests.OpenCardRequest request)
    {
        YeeVccRequests.OpenCardRequest safeRequest = requireRequest(request, "openCard");
        return execute(HttpMethod.POST, PATH_OPEN_CARD, safeRequest,
                node -> mapNode(node, YeeVccModels.OpenCardTaskData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OpenCardTaskData> queryOpenCardResult(
            YeeVccRequests.QueryOpenCardResultRequest request)
    {
        YeeVccRequests.QueryOpenCardResultRequest safeRequest = requireRequest(request, "queryOpenCardResult");
        return execute(HttpMethod.GET, PATH_QUERY_OPEN_CARD, safeRequest,
                node -> mapNode(node, YeeVccModels.OpenCardTaskData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> activateCard(YeeVccRequests.ActivateCardRequest request)
    {
        YeeVccRequests.ActivateCardRequest safeRequest = requireRequest(request, "activateCard");
        return execute(HttpMethod.POST, PATH_ACTIVATE_CARD, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.CardKeyInfoData> getCardKeyInfo(YeeVccRequests.GetCardKeyInfoRequest request)
    {
        YeeVccRequests.GetCardKeyInfoRequest safeRequest = requireRequest(request, "getCardKeyInfo");
        return executeWithFallback(HttpMethod.GET, PATH_CARD_KEY_INFO_CANDIDATES, safeRequest, this::mapCardKeyInfo);
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> freezeCard(YeeVccRequests.FreezeCardRequest request)
    {
        YeeVccRequests.FreezeCardRequest safeRequest = requireRequest(request, "freezeCard");
        return execute(HttpMethod.POST, PATH_FREEZE_CARD, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> unfreezeCard(YeeVccRequests.UnfreezeCardRequest request)
    {
        YeeVccRequests.UnfreezeCardRequest safeRequest = requireRequest(request, "unfreezeCard");
        return execute(HttpMethod.POST, PATH_UNFREEZE_CARD, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> cancelCard(YeeVccRequests.CancelCardRequest request)
    {
        YeeVccRequests.CancelCardRequest safeRequest = requireRequest(request, "cancelCard");
        return execute(HttpMethod.POST, PATH_CANCEL_CARD, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> recharge(YeeVccRequests.RechargeRequest request)
    {
        YeeVccRequests.RechargeRequest safeRequest = requireRequest(request, "recharge");
        if (StringUtils.isBlank(safeRequest.getDeductCurrency()))
        {
            safeRequest.setDeductCurrency(safeRequest.getCurrency());
        }
        return execute(HttpMethod.POST, PATH_RECHARGE, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> queryRechargeResult(
            YeeVccRequests.QueryRechargeResultRequest request)
    {
        YeeVccRequests.QueryRechargeResultRequest safeRequest = requireRequest(request, "queryRechargeResult");
        return execute(HttpMethod.GET, PATH_QUERY_RECHARGE, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.OperationData> withdraw(YeeVccRequests.WithdrawRequest request)
    {
        YeeVccRequests.WithdrawRequest safeRequest = requireRequest(request, "withdraw");
        return execute(HttpMethod.POST, PATH_WITHDRAW, safeRequest,
                node -> mapNode(node, YeeVccModels.OperationData.class));
    }

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.TransactionData>> getTransactions(
            YeeVccRequests.GetTransactionsRequest request)
    {
        YeeVccRequests.GetTransactionsRequest safeRequest = requireRequest(request, "getTransactions");
        return execute(HttpMethod.GET, PATH_TRANSACTIONS, safeRequest,
                node -> mapPageData(node, YeeVccModels.TransactionData.class, "records", "list", "items"));
    }

    public YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.CardBinData>> getCardBins(
            YeeVccRequests.GetCardBinsRequest request)
    {
        YeeVccRequests.GetCardBinsRequest safeRequest = requireRequest(request, "getCardBins");
        fillCustomerId(safeRequest::getCustomerId, safeRequest::setCustomerId);
        return execute(HttpMethod.GET, PATH_CARD_BINS, safeRequest,
                node -> mapPageData(node, YeeVccModels.CardBinData.class, "records", "list", "items", "cardBins"));
    }

    private <T extends YeeVccBaseRequest> T requireRequest(T request, String operation)
    {
        if (request == null)
        {
            throw new YeeVccException("YeeVCC 请求不能为空: " + operation);
        }
        return request;
    }

    private void fillCustomerId(Supplier<String> supplier, Consumer<String> consumer)
    {
        if (StringUtils.isBlank(supplier.get()))
        {
            if (StringUtils.isBlank(config.getCustomerId()))
            {
                throw new YeeVccException("YeeVCC customerId 未配置");
            }
            consumer.accept(config.getCustomerId());
        }
    }

    private <T> YeeVccApiResponse<T> executeWithFallback(HttpMethod method, List<String> paths,
                                                         YeeVccBaseRequest request, Function<JsonNode, T> mapper)
    {
        YeeVccException lastException = null;
        for (String path : paths)
        {
            try
            {
                return execute(method, path, request, mapper);
            }
            catch (YeeVccException ex)
            {
                lastException = ex;
                if (ex.getStatusCode() == null || ex.getStatusCode() != 404)
                {
                    throw ex;
                }
                log.warn("YeeVCC path fallback, path={} not found", path);
            }
        }
        throw lastException == null ? new YeeVccException("YeeVCC 请求失败") : lastException;
    }

    private <T> YeeVccApiResponse<T> execute(HttpMethod method, String path, YeeVccBaseRequest request,
                                             Function<JsonNode, T> mapper)
    {
        validateClientConfig();
        String requestNo = prepareRequestNo(request);
        Map<String, Object> payload = toRequestMap(request);
        URI uri = buildUri(path, method, payload);
        String queryString = method == HttpMethod.GET ? toCanonicalQuery(payload) : "";
        String body = method == HttpMethod.GET ? "" : toCanonicalJson(payload);
        HttpHeaders headers = buildHeaders(method, path, queryString, body, requestNo);
        HttpEntity<String> entity = method == HttpMethod.GET
                ? new HttpEntity<>(null, headers)
                : new HttpEntity<>(body, headers);
        ResponseEntity<String> response = exchangeWithRetry(method, uri, entity, requestNo);
        boolean signatureVerified = verifyResponseSignature(response.getHeaders(), response.getBody());
        return buildResponse(response.getBody(), requestNo, signatureVerified, mapper);
    }

    private String prepareRequestNo(YeeVccBaseRequest request)
    {
        if (StringUtils.isBlank(request.getRequestNo()))
        {
            request.setRequestNo(UUID.randomUUID().toString().replace("-", ""));
        }
        return request.getRequestNo();
    }

    private HttpHeaders buildHeaders(HttpMethod method, String path, String queryString, String body, String requestNo)
    {
        String timestamp = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String canonical = buildCanonicalRequest(method, path, queryString, body, requestNo, timestamp, nonce);
        
        // 调试日志：打印签名原文（便于排查签名问题）
        if (log.isDebugEnabled())
        {
            log.debug("YeeVCC 签名原文: method={}, path={}, canonical={}", method.name(), path, canonical);
        }
        
        String signature = Rsa2048SignatureUtils.sign(canonical, config.getPrivateKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(config.getHeaders().getAppKey(), config.getAppKey());
        headers.set(config.getHeaders().getRequestId(), requestNo);
        headers.set(config.getHeaders().getTimestamp(), timestamp);
        headers.set(config.getHeaders().getNonce(), nonce);
        headers.set(config.getHeaders().getContentSha256(), Rsa2048SignatureUtils.sha256Hex(body));
        headers.set(config.getHeaders().getSignature(), signature);
        headers.set(config.getHeaders().getSignatureAlg(), config.getSecurityReq());
        headers.set(config.getHeaders().getAuthorization(),
                config.getSecurityReq() + " appKey=\"" + config.getAppKey() + "\",requestId=\"" + requestNo
                        + "\",nonce=\"" + nonce + "\",signature=\"" + signature + "\"");
        return headers;
    }

    private String buildCanonicalRequest(HttpMethod method, String path, String queryString, String body, String requestNo,
                                         String timestamp, String nonce)
    {
        // 文档未公开完整 canonical 规则时，统一把签名串收敛在这里，便于后续按官方规范调整。
        StringBuilder builder = new StringBuilder();
        builder.append(method.name()).append('\n')
                .append(path).append('\n')
                .append(StringUtils.defaultString(queryString)).append('\n')
                .append(StringUtils.defaultString(requestNo)).append('\n')
                .append(timestamp).append('\n')
                .append(nonce).append('\n')
                .append(Rsa2048SignatureUtils.sha256Hex(body));
        if (StringUtils.isNotBlank(body))
        {
            builder.append('\n').append(body);
        }
        return builder.toString();
    }

    private URI buildUri(String path, HttpMethod method, Map<String, Object> payload)
    {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getBaseUrl() + path);
        if (method == HttpMethod.GET)
        {
            for (Map.Entry<String, Object> entry : payload.entrySet())
            {
                addQueryParam(builder, entry.getKey(), entry.getValue());
            }
        }
        return builder.build().encode().toUri();
    }

    private void addQueryParam(UriComponentsBuilder builder, String key, Object value)
    {
        if (value instanceof Collection<?> collection)
        {
            builder.queryParam(key, collection.toArray());
            return;
        }
        builder.queryParam(key, value);
    }

    private ResponseEntity<String> exchangeWithRetry(HttpMethod method, URI uri, HttpEntity<String> entity, String requestNo)
    {
        int maxAttempts = Math.max(config.getMaxRetries(), 1);
        for (int attempt = 1; attempt <= maxAttempts; attempt++)
        {
            try
            {
                log.info("YeeVCC request start, method={}, uri={}, requestNo={}, attempt={}",
                        method.name(), uri, requestNo, attempt);
                ResponseEntity<String> response = restTemplate.exchange(uri, method, entity, String.class);
                HttpStatusCode statusCode = response.getStatusCode();
                if (statusCode.is5xxServerError() && attempt < maxAttempts)
                {
                    sleepBeforeRetry(attempt, "server error " + statusCode.value());
                    continue;
                }
                return response;
            }
            catch (HttpStatusCodeException ex)
            {
                if (ex.getStatusCode().is5xxServerError() && attempt < maxAttempts)
                {
                    sleepBeforeRetry(attempt, "http " + ex.getStatusCode().value());
                    continue;
                }
                throw new YeeVccException("YeeVCC HTTP 请求失败: " + ex.getStatusCode().value(),
                        ex.getStatusCode().value(), ex.getResponseBodyAsString(), ex);
            }
            catch (ResourceAccessException ex)
            {
                if (attempt < maxAttempts)
                {
                    sleepBeforeRetry(attempt, ex.getMessage());
                    continue;
                }
                throw new YeeVccException("YeeVCC 连接超时或网络异常: " + ex.getMessage(), null, null, ex);
            }
            catch (RestClientException ex)
            {
                if (attempt < maxAttempts)
                {
                    sleepBeforeRetry(attempt, ex.getMessage());
                    continue;
                }
                throw new YeeVccException("YeeVCC 调用失败: " + ex.getMessage(), null, null, ex);
            }
        }
        throw new YeeVccException("YeeVCC 请求重试后仍失败");
    }

    /**
     * 验证 YeeVCC 平台响应签名。
     *
     * <h3>响应验签协议说明</h3>
     * <p>
     * <b>请求侧签名</b>使用 canonical request 拼串（见 {@link #buildCanonicalRequest}），包含
     * HTTP method、path、queryString、requestNo、timestamp、nonce、body SHA256 等字段，
     * 属于"请求方主动构造签名串"的模式。
     * </p>
     * <p>
     * <b>响应侧验签</b>则不同——平台在响应头中返回签名（通过 {@code x-yop-signature} 或
     * {@code Authorization} 头传递），签名内容为<b>响应体（response body）原文</b>，
     * 不涉及额外的 canonical 拼串规则。
     * </p>
     *
     * <h3>协议依据</h3>
     * <ul>
     *   <li>YeeVCC/YOP 平台响应签名标准做法：平台使用自身私钥对 response body 做
     *       {@code SHA256withRSA} 签名，调用方使用平台公钥验证。</li>
     *   <li>项目内无独立的响应 canonical 规则文档，且本项目 Review 文档
     *       ({@code docs/review-agent-b-upstream.md}) 中确认请求和响应的签名模式不对称
     *       是已知设计。</li>
     *   <li>当前实现与 YOP 系列 SDK 的响应验签行为一致：直接对 body 原文验签。</li>
     * </ul>
     *
     * <h3>安全说明</h3>
     * <ul>
     *   <li>验签功能由 {@code yeevcc.verify-response-signature} 配置控制，默认开启（true）。生产环境必须为 true，测试环境可关闭。</li>
     *   <li>启用后，若响应头缺少签名且 {@code fail-on-missing-signature=true}，将抛出异常。</li>
     *   <li>验签失败（签名不匹配）会直接抛出 {@link YeeVccException}，阻止不可信响应进入业务层。</li>
     * </ul>
     *
     * @param headers      HTTP 响应头
     * @param responseBody HTTP 响应体原文
     * @return true 如果验签通过；false 如果验签未启用或签名缺失（且配置允许缺失）
     * @throws YeeVccException 验签失败、签名缺失（strict 模式）、公钥未配置
     */
    private boolean verifyResponseSignature(HttpHeaders headers, String responseBody)
    {
        if (!config.isVerifyResponseSignature())
        {
            return false;
        }
        String signature = resolveResponseSignature(headers);
        if (StringUtils.isBlank(signature))
        {
            if (config.isFailOnMissingSignature())
            {
                throw new YeeVccException("YeeVCC 响应缺少签名");
            }
            return false;
        }
        if (StringUtils.isBlank(config.getPlatformPublicKey()))
        {
            throw new YeeVccException("YeeVCC 平台公钥未配置，无法验签");
        }
        // 响应验签：直接对 response body 原文验签（非 canonical 拼串）
        // 这与请求签名不同——请求签名使用 canonical request 串，响应签名仅签 body 原文
        boolean verified = Rsa2048SignatureUtils.verify(StringUtils.defaultString(responseBody), signature,
                config.getPlatformPublicKey());
        if (!verified)
        {
            log.error("YeeVCC 响应验签失败, signatureHeader={}", signature);
            throw new YeeVccException("YeeVCC 响应验签失败", null, responseBody);
        }
        return true;
    }

    private String resolveResponseSignature(HttpHeaders headers)
    {
        String signature = headers.getFirst(config.getHeaders().getSignature());
        if (StringUtils.isNotBlank(signature))
        {
            return signature;
        }
        return headers.getFirst(config.getHeaders().getAuthorization());
    }

    private <T> YeeVccApiResponse<T> buildResponse(String responseBody, String requestNo, boolean signatureVerified,
                                                   Function<JsonNode, T> mapper)
    {
        if (StringUtils.isBlank(responseBody))
        {
            throw new YeeVccException("YeeVCC 响应体为空");
        }
        try
        {
            JsonNode root = objectMapper.readTree(responseBody);
            YeeVccApiResponse<T> response = new YeeVccApiResponse<>();
            int status = root.path("status").asInt(config.getSuccessCode());
            response.setStatus(status);
            response.setMessage(root.path("message").asText(null));
            response.setSuccess(status == config.getSuccessCode());
            response.setSignatureVerified(signatureVerified);
            response.setRequestNo(requestNo);
            response.setRawBody(responseBody);
            JsonNode dataNode = root.get("data");
            response.setData(dataNode == null || dataNode.isNull() ? null : mapper.apply(dataNode));
            return response;
        }
        catch (JsonProcessingException ex)
        {
            throw new YeeVccException("YeeVCC 响应解析失败", null, responseBody, ex);
        }
    }

    private <T> T mapNode(JsonNode node, Class<T> targetType)
    {
        if (node == null || node.isNull())
        {
            return null;
        }
        return objectMapper.convertValue(node, targetType);
    }

    private YeeVccModels.CardKeyInfoData mapCardKeyInfo(JsonNode node)
    {
        YeeVccModels.CardKeyInfoData data = mapNode(node, YeeVccModels.CardKeyInfoData.class);
        if (data == null || !config.isAutoDecryptKeyInfo() || StringUtils.isBlank(config.getAesKey()))
        {
            return data;
        }
        data.setEncryptedCardNumber(data.getCardNumber());
        data.setEncryptedCvv(data.getCvv());
        data.setEncryptedExpiryDate(data.getExpiryDate());
        data.setCardNumber(tryDecrypt(data.getCardNumber()));
        data.setCvv(tryDecrypt(data.getCvv()));
        data.setExpiryDate(tryDecrypt(data.getExpiryDate()));
        return data;
    }

    private String tryDecrypt(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return value;
        }
        try
        {
            return Aes16CryptoUtils.decrypt(value, config.getAesKey());
        }
        catch (YeeVccException ex)
        {
            // VCC-005: 解密失败时显式抛错，不把密文当成功结果返回
            throw new YeeVccException("卡三要素解密失败，请检查 AES 密钥配置", ex);
        }
    }

    private <T> YeeVccModels.PageData<T> mapPageData(JsonNode node, Class<T> itemType, String... recordFieldCandidates)
    {
        YeeVccModels.PageData<T> page = new YeeVccModels.PageData<>();
        if (node == null || node.isNull())
        {
            return page;
        }
        if (node.isArray())
        {
            page.setRecords(mapArray(node, itemType));
            page.setTotal((long) page.getRecords().size());
            return page;
        }
        JavaType pageType = objectMapper.getTypeFactory().constructParametricType(YeeVccModels.PageData.class, itemType);
        page = objectMapper.convertValue(node, pageType);
        if (page.getRecords() == null || page.getRecords().isEmpty())
        {
            for (String field : recordFieldCandidates)
            {
                JsonNode arrayNode = node.get(field);
                if (arrayNode != null && arrayNode.isArray())
                {
                    page.setRecords(mapArray(arrayNode, itemType));
                    break;
                }
            }
        }
        if (page.getRecords() == null)
        {
            page.setRecords(new ArrayList<>());
        }
        if (page.getTotal() == null)
        {
            page.setTotal((long) page.getRecords().size());
        }
        return page;
    }

    private <T> List<T> mapArray(JsonNode arrayNode, Class<T> itemType)
    {
        List<T> records = new ArrayList<>();
        for (JsonNode itemNode : arrayNode)
        {
            records.add(objectMapper.convertValue(itemNode, itemType));
        }
        return records;
    }

    private Map<String, Object> toRequestMap(YeeVccBaseRequest request)
    {
        // 使用反射获取字段，保留原始大小写
        Map<String, Object> rawMap = new LinkedHashMap<>();
        Class<?> clazz = request.getClass();
        
        while (clazz != null && !clazz.equals(Object.class))
        {
            for (java.lang.reflect.Field field : clazz.getDeclaredFields())
            {
                field.setAccessible(true);
                try
                {
                    Object value = field.get(request);
                    if (value != null)
                    {
                        rawMap.put(field.getName(), value);
                    }
                }
                catch (IllegalAccessException e)
                {
                    // Skip fields that can't be accessed
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cleanMap = (Map<String, Object>) pruneEmptyValues(sortObject(rawMap));
        return cleanMap == null ? Map.of() : cleanMap;
    }

    private Object sortObject(Object value)
    {
        if (value instanceof Map<?, ?> rawMap)
        {
            Map<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet())
            {
                sorted.put(String.valueOf(entry.getKey()), sortObject(entry.getValue()));
            }
            return sorted;
        }
        if (value instanceof Collection<?> collection)
        {
            List<Object> sortedItems = new ArrayList<>();
            for (Object item : collection)
            {
                sortedItems.add(sortObject(item));
            }
            return sortedItems;
        }
        return value;
    }

    private Object pruneEmptyValues(Object value)
    {
        if (value instanceof Map<?, ?> rawMap)
        {
            Map<String, Object> cleaned = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet())
            {
                Object child = pruneEmptyValues(entry.getValue());
                if (child == null)
                {
                    continue;
                }
                if (child instanceof String stringValue && StringUtils.isBlank(stringValue))
                {
                    continue;
                }
                cleaned.put(String.valueOf(entry.getKey()), child);
            }
            return cleaned;
        }
        if (value instanceof Collection<?> collection)
        {
            List<Object> cleaned = new ArrayList<>();
            for (Object item : collection)
            {
                Object child = pruneEmptyValues(item);
                if (child != null)
                {
                    cleaned.add(child);
                }
            }
            return cleaned;
        }
        return value;
    }

    private String toCanonicalJson(Map<String, Object> payload)
    {
        try
        {
            return objectMapper.writeValueAsString(payload);
        }
        catch (JsonProcessingException ex)
        {
            throw new YeeVccException("YeeVCC 请求序列化失败", null, null, ex);
        }
    }

    private String toCanonicalQuery(Map<String, Object> payload)
    {
        if (payload.isEmpty())
        {
            return "";
        }
        // 使用 UriComponentsBuilder 进行 URL 编码，确保与 buildUri 中的编码一致
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        for (Map.Entry<String, Object> entry : payload.entrySet())
        {
            addQueryParam(builder, entry.getKey(), entry.getValue());
        }
        // 返回编码后的查询字符串（去掉开头的 ?）
        String queryString = builder.toUriString();
        return queryString.startsWith("?") ? queryString.substring(1) : queryString;
    }

    private void validateClientConfig()
    {
        if (!config.isEnabled())
        {
            throw new YeeVccException("YeeVCC 客户端未启用");
        }
        if (StringUtils.isAnyBlank(config.getBaseUrl(), config.getAppKey(), config.getPrivateKey()))
        {
            throw new YeeVccException("YeeVCC 基础配置不完整，请检查 baseUrl/appKey/privateKey");
        }
    }

    private void sleepBeforeRetry(int attempt, String reason)
    {
        log.warn("YeeVCC request retry, attempt={}, reason={}", attempt, reason);
        try
        {
            Thread.sleep(config.getRetryIntervalMillis());
        }
        catch (InterruptedException interruptedException)
        {
            Thread.currentThread().interrupt();
            throw new YeeVccException("YeeVCC 重试等待被中断", interruptedException);
        }
    }

    private RestTemplate createRestTemplate(YeeVccConfig properties)
    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMillis());
        factory.setReadTimeout(properties.getReadTimeoutMillis());
        return new RestTemplate(factory);
    }
}
