package com.vcc.upstream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcc.upstream.config.YeeVccConfig;
import com.vcc.upstream.dto.YeeVccBaseRequest;
import com.vcc.upstream.exception.YeeVccException;
import com.vcc.upstream.util.Aes16CryptoUtils;
import com.vcc.upstream.util.Rsa2048SignatureUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class YeeVccClientTest
{
    private static final String AES_KEY = "1234567890ABCDEF";

    @Mock
    private YeeVccConfig config;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private YeeVccClient yeeVccClient;

    private KeyPair keyPair;
    private YeeVccConfig.HeaderNames headerNames;

    @BeforeEach
    void setUp() throws Exception
    {
        keyPair = generateKeyPair();
        headerNames = new YeeVccConfig.HeaderNames();

        lenient().when(config.getConnectTimeoutMillis()).thenReturn(1000);
        lenient().when(config.getReadTimeoutMillis()).thenReturn(1000);
        lenient().when(config.getPrivateKey()).thenReturn(toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
        lenient().when(config.getPlatformPublicKey()).thenReturn(toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()));
        lenient().when(config.getHeaders()).thenReturn(headerNames);
        lenient().when(config.getAppKey()).thenReturn("test-app");
        lenient().when(config.getSecurityReq()).thenReturn("YOP-RSA2048-SHA256");
        lenient().when(config.getAesKey()).thenReturn(AES_KEY);
        lenient().when(config.isEnabled()).thenReturn(true);
        lenient().when(config.getBaseUrl()).thenReturn("https://example.test");
    }

    @Test
    @DisplayName("testSignRequest_正常签名")
    void testSignRequest_正常签名()
    {
        // given: 构造一个包含查询串和请求体的上游请求
        String path = "/rest/v1.0/vcc/card-recharge";
        String query = "customerId=c001";
        String body = "{\"amount\":99.00}";
        String requestNo = "req-001";

        // when: 通过请求头构建逻辑生成签名
        HttpHeaders headers = ReflectionTestUtils.invokeMethod(
                yeeVccClient, "buildHeaders", HttpMethod.POST, path, query, body, requestNo);
        String canonical = ReflectionTestUtils.invokeMethod(
                yeeVccClient,
                "buildCanonicalRequest",
                HttpMethod.POST,
                path,
                query,
                body,
                requestNo,
                headers.getFirst(headerNames.getTimestamp()),
                headers.getFirst(headerNames.getNonce()));

        // then: 返回的签名可以被公钥正确验签
        String signature = headers.getFirst(headerNames.getSignature());
        assertThat(signature).isNotBlank();
        assertThat(headers.getFirst(headerNames.getAuthorization())).contains("requestId=\"req-001\"");
        assertThat(Rsa2048SignatureUtils.verify(canonical, signature, config.getPlatformPublicKey())).isTrue();
    }

    @Test
    @DisplayName("testSignRequest_空参数")
    void testSignRequest_空参数()
    {
        // given: 空查询串和空请求体
        String path = "/rest/v1.0/vcc/card-recharge";

        // when: 生成签名请求头
        HttpHeaders headers = ReflectionTestUtils.invokeMethod(
                yeeVccClient, "buildHeaders", HttpMethod.GET, path, "", "", "req-empty");

        // then: 空参数也应生成稳定的 sha256 和签名
        assertThat(headers.getFirst(headerNames.getContentSha256()))
                .isEqualTo(Rsa2048SignatureUtils.sha256Hex(""));
        assertThat(headers.getFirst(headerNames.getSignature())).isNotBlank();
    }

    @Test
    @DisplayName("testDecryptResponse_正常解密")
    void testDecryptResponse_正常解密()
    {
        // given: 使用测试 AES 密钥预先加密卡号
        String cipherText = Aes16CryptoUtils.encrypt("4111111111111111", AES_KEY);

        // when: 调用客户端内部解密逻辑
        String plainText = ReflectionTestUtils.invokeMethod(yeeVccClient, "tryDecrypt", cipherText);

        // then: 能还原出原始明文
        assertThat(plainText).isEqualTo("4111111111111111");
    }

    @Test
    @DisplayName("testDecryptResponse_密文损坏抛异常")
    void testDecryptResponse_密文损坏抛异常()
    {
        // given: 构造损坏的密文
        String brokenCipherText = "broken-cipher-text";

        // when / then: 解密应显式抛出业务异常
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(yeeVccClient, "tryDecrypt", brokenCipherText))
                .isInstanceOf(YeeVccException.class)
                .hasMessageContaining("卡三要素解密失败");
    }

    @Test
    @DisplayName("testBuildCanonicalQuery_多参数排序正确")
    void testBuildCanonicalQuery_多参数排序正确()
    {
        // given: 原始请求字段顺序被打乱
        QuerySortRequest request = new QuerySortRequest();
        request.setZKey("last");
        request.setAKey("first");
        request.setItems(List.of("A 1", "B"));

        // when: 先做请求对象排序清洗，再构建 canonical query
        Map<String, Object> payload = ReflectionTestUtils.invokeMethod(yeeVccClient, "toRequestMap", request);
        String canonicalQuery = ReflectionTestUtils.invokeMethod(yeeVccClient, "toCanonicalQuery", payload);

        // then: 参数顺序应按 key 排序，并保留 URL 编码结果
        assertThat(payload.keySet()).containsExactly("aKey", "items", "zKey");
        assertThat(canonicalQuery).isEqualTo("aKey=first&items=A%201&items=B&zKey=last");
    }

    private KeyPair generateKeyPair() throws Exception
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private String toPem(String type, byte[] content)
    {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(content);
        return "-----BEGIN " + type + "-----\n" + encoded + "\n-----END " + type + "-----";
    }

    private static class QuerySortRequest extends YeeVccBaseRequest
    {
        private String zKey;
        private String aKey;
        private List<String> items;

        public String getZKey()
        {
            return zKey;
        }

        public void setZKey(String zKey)
        {
            this.zKey = zKey;
        }

        public String getAKey()
        {
            return aKey;
        }

        public void setAKey(String aKey)
        {
            this.aKey = aKey;
        }

        public List<String> getItems()
        {
            return items;
        }

        public void setItems(List<String> items)
        {
            this.items = items;
        }
    }
}
