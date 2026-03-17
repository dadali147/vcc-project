package com.vcc.card.webhook;

import com.vcc.card.domain.Card;
import com.vcc.card.domain.WebhookLog;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.WebhookLogMapper;
import com.vcc.common.TestUtils;
import com.vcc.common.core.redis.RedisCache;
import com.vcc.upstream.config.YeeVccConfig;
import com.vcc.upstream.util.Rsa2048SignatureUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceImplTest
{
    @Mock
    private WebhookLogMapper webhookLogMapper;

    @Mock
    private WebhookTransactionService webhookTransactionService;

    @Mock
    private WebhookAsyncService webhookAsyncService;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private RedisCache redisCache;

    @Mock
    private YeeVccConfig yeeVccConfig;

    @InjectMocks
    private WebhookServiceImpl webhookService;

    private WebhookAsyncService asyncService;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception
    {
        keyPair = generateKeyPair();
        lenient().when(yeeVccConfig.getPlatformPublicKey()).thenReturn(toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()));

        asyncService = new WebhookAsyncService();
        ReflectionTestUtils.setField(asyncService, "webhookLogMapper", webhookLogMapper);
        ReflectionTestUtils.setField(asyncService, "webhookService", webhookService);
    }

    @Test
    @DisplayName("testEnqueueWebhook_正常入队")
    void testEnqueueWebhook_正常入队()
    {
        // given: 合法签名且幂等键不存在
        String payload = "{\"tranId\":\"txn-001\"}";
        Map<String, Object> data = Map.of("tranId", "txn-001");
        String signature = sign(payload);
        when(webhookLogMapper.selectByIdempotencyKey("AUTH_TRANSACTION:txn-001")).thenReturn(null);
        when(webhookLogMapper.insertWebhookLog(any(WebhookLog.class))).thenAnswer(invocation -> {
            WebhookLog webhookLog = invocation.getArgument(0);
            webhookLog.setId(1L);
            return 1;
        });
        ArgumentCaptor<WebhookLog> logCaptor = ArgumentCaptor.forClass(WebhookLog.class);

        // when: 同步验签并入队
        boolean queued = webhookService.enqueueWebhook("AUTH_TRANSACTION", payload, signature, data);

        // then: 日志写入成功并触发异步消费
        assertThat(queued).isTrue();
        verify(webhookLogMapper).insertWebhookLog(logCaptor.capture());
        verify(webhookAsyncService).processWebhookAsync(1L, "AUTH_TRANSACTION", payload, signature, data);
        assertThat(logCaptor.getValue().getIdempotencyKey()).isEqualTo("AUTH_TRANSACTION:txn-001");
        assertThat(logCaptor.getValue().getProcessed()).isEqualTo(WebhookLog.PROCESSED_NO);
    }

    @Test
    @DisplayName("testEnqueueWebhook_重复幂等键直接返回true")
    void testEnqueueWebhook_重复幂等键直接返回true()
    {
        // given: 幂等键已存在
        String payload = "{\"tranId\":\"txn-001\"}";
        Map<String, Object> data = Map.of("tranId", "txn-001");
        String signature = sign(payload);
        when(webhookLogMapper.selectByIdempotencyKey("AUTH_TRANSACTION:txn-001")).thenReturn(TestUtils.buildWebhookLog(8L, "AUTH_TRANSACTION"));

        // when: 重复回调再次入队
        boolean queued = webhookService.enqueueWebhook("AUTH_TRANSACTION", payload, signature, data);

        // then: 直接返回成功，不重复落库和异步处理
        assertThat(queued).isTrue();
        verify(webhookLogMapper, never()).insertWebhookLog(any(WebhookLog.class));
        verify(webhookAsyncService, never()).processWebhookAsync(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("testEnqueueWebhook_验签失败返回false")
    void testEnqueueWebhook_验签失败返回false()
    {
        // given: 签名与 payload 不匹配
        String payload = "{\"tranId\":\"txn-002\"}";

        // when: 入队请求验签失败
        boolean queued = webhookService.enqueueWebhook("AUTH_TRANSACTION", payload, "broken-signature", Map.of("tranId", "txn-002"));

        // then: 直接返回 false，且不访问数据库
        assertThat(queued).isFalse();
        verify(webhookLogMapper, never()).selectByIdempotencyKey(any());
        verify(webhookLogMapper, never()).insertWebhookLog(any(WebhookLog.class));
    }

    @Test
    @DisplayName("testProcessWebhookAsync_AUTH_TRANSACTION正常处理")
    void testProcessWebhookAsync_AUTH_TRANSACTION正常处理()
    {
        // given: 一条待处理的交易回调日志
        WebhookLog webhookLog = TestUtils.buildWebhookLog(10L, "AUTH_TRANSACTION");
        Map<String, Object> data = Map.of("tranId", "txn-010");
        when(webhookLogMapper.selectWebhookLogById(10L)).thenReturn(webhookLog);
        ArgumentCaptor<WebhookLog> updateCaptor = ArgumentCaptor.forClass(WebhookLog.class);

        // when: 异步消费 AUTH_TRANSACTION 回调
        asyncService.processWebhookAsync(10L, "AUTH_TRANSACTION", "{\"tranId\":\"txn-010\"}", sign("{\"tranId\":\"txn-010\"}"), data);

        // then: 应分发给交易处理器并把日志标记为成功
        verify(webhookTransactionService).handleTransaction(data);
        verify(webhookLogMapper).updateWebhookLog(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getProcessed()).isEqualTo(WebhookLog.PROCESSED_YES);
        assertThat(updateCaptor.getValue().getProcessResult()).isEqualTo("{\"result\":\"success\"}");
    }

    @Test
    @DisplayName("testProcessWebhookAsync_CARD_OPERATION状态变更")
    void testProcessWebhookAsync_CARD_OPERATION状态变更()
    {
        // given: 卡状态变更回调对应到本地卡片
        WebhookLog webhookLog = TestUtils.buildWebhookLog(11L, "CARD_OPERATION");
        Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
        Map<String, Object> data = Map.of("cardId", card.getUpstreamCardId(), "status", "FROZEN");
        when(webhookLogMapper.selectWebhookLogById(11L)).thenReturn(webhookLog);
        when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);
        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);

        // when: 异步处理 CARD_OPERATION 回调
        asyncService.processWebhookAsync(11L, "CARD_OPERATION", "{\"cardId\":\"" + card.getUpstreamCardId() + "\"}", sign("{\"cardId\":\"" + card.getUpstreamCardId() + "\"}"), data);

        // then: 本地卡状态应更新为冻结
        verify(cardMapper).updateCard(cardCaptor.capture());
        assertThat(cardCaptor.getValue().getId()).isEqualTo(card.getId());
        assertThat(cardCaptor.getValue().getStatus()).isEqualTo(Card.STATUS_FROZEN);
    }

    @Test
    @DisplayName("testProcessWebhookAsync_OTP_3DS存储验证码")
    void testProcessWebhookAsync_OTP_3DS存储验证码()
    {
        // given: 3DS OTP 回调命中本地卡
        WebhookLog webhookLog = TestUtils.buildWebhookLog(12L, "OTP_3DS");
        Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
        Map<String, Object> data = Map.of(
                "cardId", card.getUpstreamCardId(),
                "otpCode", "123456",
                "expireTime", "2026-03-17T12:00:00Z",
                "phone", "+8613800138000");
        when(webhookLogMapper.selectWebhookLogById(12L)).thenReturn(webhookLog);
        when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);
        ArgumentCaptor<Map<String, Object>> otpCaptor = ArgumentCaptor.forClass(Map.class);

        // when: 异步处理 OTP_3DS 回调
        asyncService.processWebhookAsync(12L, "OTP_3DS", "{\"otpCode\":\"123456\"}", sign("{\"otpCode\":\"123456\"}"), data);

        // then: 验证码应写入缓存，供后续展示或查询
        verify(redisCache).setCacheObject(eq("vcc:3ds:otp:100"), otpCaptor.capture(), eq(10), eq(TimeUnit.MINUTES));
        assertThat(otpCaptor.getValue())
                .containsEntry("userId", 100L)
                .containsEntry("otpCode", "123456")
                .containsEntry("phone", "+8613800138000");
    }

    private String sign(String payload)
    {
        return Rsa2048SignatureUtils.sign(payload, toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
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
}
