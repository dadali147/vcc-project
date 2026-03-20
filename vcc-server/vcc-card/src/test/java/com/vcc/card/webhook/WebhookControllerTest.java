package com.vcc.card.webhook;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson2.JSON;
import com.vcc.system.service.ISysConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WebhookController 单元测试
 *
 * 覆盖范围：
 * - HMAC-SHA256 验签成功
 * - 签名错误返回 401 (WebhookAuthenticationException)
 * - 时间戳超窗口返回 401
 * - 缺少签名头返回 401
 */
@ExtendWith(MockitoExtension.class)
class WebhookControllerTest
{
    private static final String WEBHOOK_SECRET = "test-webhook-secret-key-2026";

    @Mock
    private WebhookService webhookService;

    @Mock
    private ISysConfigService sysConfigService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private WebhookController webhookController;

    @BeforeEach
    void setUp()
    {
        // sysConfigService 返回固定的 webhook secret（部分测试在到达 HMAC 计算前就失败，因此用 lenient）
        lenient().when(sysConfigService.selectConfigByKey("vcc.webhook.secret")).thenReturn(WEBHOOK_SECRET);
    }

    // ==================== 验签成功 ====================

    @Nested
    @DisplayName("验签成功")
    class SignatureValidTests
    {
        @Test
        @DisplayName("合法签名：正常通过验签，入队成功返回 ok")
        void yeevcc_validSignature_returnsOk()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-001", "cardId", "card-x");
            String body = JSON.toJSONString(data);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = computeHmacSignature(timestamp, body, WEBHOOK_SECRET);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(timestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=" + signature);
            when(webhookService.enqueueWebhook(anyString(), anyString(), any(), any())).thenReturn(true);

            // when
            String result = webhookController.yeevcc(data, "v1=" + signature, httpServletRequest);

            // then
            assertThat(result).isEqualTo("ok");
            verify(webhookService).enqueueWebhook(eq("TRANSACTION"), eq(body), eq("v1=" + signature), eq(data));
        }

        @Test
        @DisplayName("OTP 接口合法签名通过")
        void otp_validSignature_returnsOk()
        {
            // given
            Map<String, Object> data = Map.of("cardId", "card-001", "otp", "123456");
            String body = JSON.toJSONString(data);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = computeHmacSignature(timestamp, body, WEBHOOK_SECRET);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(timestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=" + signature);
            when(webhookService.enqueueWebhook(anyString(), anyString(), any(), any())).thenReturn(true);

            // when
            Map<String, Object> result = webhookController.otp(data, "v1=" + signature, httpServletRequest);

            // then
            assertThat(result).containsEntry("method", 2);
            assertThat(result).containsKey("destination");
            verify(webhookService).enqueueWebhook(eq("OTP"), eq(body), eq("v1=" + signature), eq(data));
        }
    }

    // ==================== 签名错误 ====================

    @Nested
    @DisplayName("签名错误")
    class SignatureInvalidTests
    {
        @Test
        @DisplayName("签名不匹配：返回 401 WebhookAuthenticationException")
        void yeevcc_wrongSignature_throws401()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-002");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(timestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=wrong_signature_hex");

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, "v1=wrong_signature_hex", httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("缺少 X-Webhook-Signature 头：返回 401")
        void yeevcc_missingSignatureHeader_throws401()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-003");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(timestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, null, httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("缺少 X-Webhook-Timestamp 头：返回 401")
        void yeevcc_missingTimestampHeader_throws401()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-004");
            String signature = "v1=some_hex_value";

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(null);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn(signature);

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, signature, httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("签名格式不正确（无 v1= 前缀）：返回 401")
        void yeevcc_badSignatureFormat_throws401()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-005");
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(timestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("bad_format_no_v1_prefix");

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, "bad_format_no_v1_prefix", httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }
    }

    // ==================== 时间戳超窗口 ====================

    @Nested
    @DisplayName("时间戳超窗口")
    class TimestampWindowTests
    {
        @Test
        @DisplayName("时间戳超过 300 秒（过去）：返回 401")
        void yeevcc_timestampTooOld_throws401()
        {
            // given: 时间戳在 10 分钟前
            Map<String, Object> data = Map.of("tranId", "txn-old");
            String body = JSON.toJSONString(data);
            String oldTimestamp = String.valueOf(System.currentTimeMillis() / 1000 - 600);
            String signature = computeHmacSignature(oldTimestamp, body, WEBHOOK_SECRET);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(oldTimestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=" + signature);

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, "v1=" + signature, httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("时间戳超过 300 秒（未来）：返回 401")
        void yeevcc_timestampTooFuture_throws401()
        {
            // given: 时间戳在 10 分钟后
            Map<String, Object> data = Map.of("tranId", "txn-future");
            String body = JSON.toJSONString(data);
            String futureTimestamp = String.valueOf(System.currentTimeMillis() / 1000 + 600);
            String signature = computeHmacSignature(futureTimestamp, body, WEBHOOK_SECRET);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(futureTimestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=" + signature);

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, "v1=" + signature, httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }

        @Test
        @DisplayName("时间戳格式不合法：返回 401")
        void yeevcc_timestampBadFormat_throws401()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-bad-ts");

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn("not-a-number");
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=some_hex");

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, "v1=some_hex", httpServletRequest))
                    .isInstanceOf(WebhookAuthenticationException.class);
            verify(webhookService, never()).enqueueWebhook(anyString(), anyString(), any(), any());
        }
    }

    // ==================== 入队失败 ====================

    @Nested
    @DisplayName("入队失败")
    class EnqueueFailureTests
    {
        @Test
        @DisplayName("验签通过但入队失败：返回 500 WebhookProcessingException")
        void yeevcc_enqueueFails_throws500()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-fail");
            String body = JSON.toJSONString(data);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = computeHmacSignature(timestamp, body, WEBHOOK_SECRET);

            when(httpServletRequest.getHeader("X-Webhook-Timestamp")).thenReturn(timestamp);
            when(httpServletRequest.getHeader("X-Webhook-Signature")).thenReturn("v1=" + signature);
            when(webhookService.enqueueWebhook(anyString(), anyString(), any(), any())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> webhookController.yeevcc(data, "v1=" + signature, httpServletRequest))
                    .isInstanceOf(WebhookProcessingException.class);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算 HMAC-SHA256 签名（与 Controller 中 verifyWebhookSignature 对称）
     */
    private static String computeHmacSignature(String timestamp, String body, String secret)
    {
        try
        {
            String payload = timestamp + "." + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Test HMAC computation failed", e);
        }
    }
}
