package com.vcc.card.webhook;

import com.vcc.card.domain.Card;
import com.vcc.card.domain.WebhookLog;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.WebhookLogMapper;
import com.vcc.common.TestUtils;
import com.vcc.common.core.redis.RedisCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WebhookServiceImpl 单元测试
 *
 * 覆盖范围：
 * - enqueueWebhook: 正常入队 + 异常处理
 * - dispatchWebhook: 幂等检查 + 各事件类型分派
 *   - handleOtpWebhook: 正常写 Redis + 幂等跳过
 *   - handleTransactionWebhook: 正常委托 + 幂等跳过
 *   - handleCardStatusChangeWebhook: 正常更新卡状态 + 幂等跳过
 *   - handleRechargeResultWebhook: SUCCESS/FAILED 分支 + 幂等跳过
 */
@ExtendWith(MockitoExtension.class)
class WebhookServiceImplTest
{
    @Mock
    private WebhookLogMapper webhookLogMapper;

    @Mock
    private WebhookAsyncService webhookAsyncService;

    @Mock
    private RedisCache redisCache;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private WebhookTransactionService webhookTransactionService;

    @Mock
    private WebhookRechargeHandler webhookRechargeHandler;

    @InjectMocks
    private WebhookServiceImpl webhookService;

    // ==================== enqueueWebhook ====================

    @Nested
    @DisplayName("enqueueWebhook 入队测试")
    class EnqueueWebhookTests
    {
        @Test
        @DisplayName("正常入队：保存日志并触发异步处理")
        void enqueueWebhook_normalFlow()
        {
            // given
            String payload = "{\"tranId\":\"txn-001\"}";
            Map<String, Object> data = Map.of("tranId", "txn-001");
            String signature = "v1=abc123";
            when(webhookLogMapper.insertWebhookLog(any(WebhookLog.class))).thenAnswer(inv -> {
                WebhookLog log = inv.getArgument(0);
                log.setId(1L);
                return 1;
            });

            // when
            boolean result = webhookService.enqueueWebhook("TRANSACTION", payload, signature, data);

            // then
            assertThat(result).isTrue();

            ArgumentCaptor<WebhookLog> logCaptor = ArgumentCaptor.forClass(WebhookLog.class);
            verify(webhookLogMapper).insertWebhookLog(logCaptor.capture());
            WebhookLog saved = logCaptor.getValue();
            assertThat(saved.getWebhookType()).isEqualTo("TRANSACTION");
            assertThat(saved.getPayload()).isEqualTo(payload);
            assertThat(saved.getSignature()).isEqualTo(signature);
            assertThat(saved.getIdempotencyKey()).isEqualTo("TRANSACTION:txn-001");
            assertThat(saved.getProcessed()).isEqualTo(WebhookLog.PROCESSED_NO);
            assertThat(saved.getCreatedAt()).isNotNull();

            verify(webhookAsyncService).processWebhookAsync(
                    eq(1L), eq("TRANSACTION"), eq(payload), eq(signature), eq(data));
        }

        @Test
        @DisplayName("入队异常时返回 false")
        void enqueueWebhook_exceptionReturnsFalse()
        {
            // given
            when(webhookLogMapper.insertWebhookLog(any())).thenThrow(new RuntimeException("DB error"));

            // when
            boolean result = webhookService.enqueueWebhook("TRANSACTION",
                    "{}", "sig", Map.of("tranId", "txn-err"));

            // then
            assertThat(result).isFalse();
            verify(webhookAsyncService, never()).processWebhookAsync(
                    anyLong(), anyString(), anyString(), anyString(), any());
        }
    }

    // ==================== dispatchWebhook 幂等检查 ====================

    @Nested
    @DisplayName("dispatchWebhook 幂等检查")
    class IdempotencyTests
    {
        @Test
        @DisplayName("已处理过的 Webhook 直接跳过")
        void dispatchWebhook_idempotent_skip()
        {
            // given: 幂等键已存在且已处理
            Map<String, Object> data = Map.of("tranId", "txn-dup");
            WebhookLog existing = TestUtils.buildWebhookLog(99L, "TRANSACTION");
            existing.setProcessed(WebhookLog.PROCESSED_YES);
            when(webhookLogMapper.selectByIdempotencyKey("TRANSACTION:txn-dup")).thenReturn(existing);

            // when
            webhookService.dispatchWebhook("TRANSACTION", data);

            // then: 不应调用任何处理器
            verify(webhookTransactionService, never()).handleTransaction(any());
            verify(cardMapper, never()).updateCard(any());
            verify(redisCache, never()).setCacheObject(anyString(), any(), anyInt(), any());
        }

        @Test
        @DisplayName("未处理过的记录正常分派")
        void dispatchWebhook_notProcessed_dispatches()
        {
            // given: 幂等键存在但未处理
            Map<String, Object> data = Map.of("tranId", "txn-retry");
            WebhookLog existing = TestUtils.buildWebhookLog(99L, "TRANSACTION");
            existing.setProcessed(WebhookLog.PROCESSED_NO);
            when(webhookLogMapper.selectByIdempotencyKey("TRANSACTION:txn-retry")).thenReturn(existing);

            // when
            webhookService.dispatchWebhook("TRANSACTION", data);

            // then: 应正常分派到交易处理
            verify(webhookTransactionService).handleTransaction(data);
        }

        @Test
        @DisplayName("幂等键不存在时正常分派")
        void dispatchWebhook_noExisting_dispatches()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-new");
            when(webhookLogMapper.selectByIdempotencyKey("TRANSACTION:txn-new")).thenReturn(null);

            // when
            webhookService.dispatchWebhook("TRANSACTION", data);

            // then
            verify(webhookTransactionService).handleTransaction(data);
        }
    }

    // ==================== handleOtpWebhook ====================

    @Nested
    @DisplayName("handleOtpWebhook OTP 处理")
    class OtpWebhookTests
    {
        @Test
        @DisplayName("正常流程（OTP 类型）：OTP 写入 Redis，key=vcc:otp:{cardId}，TTL=10min")
        void handleOtpWebhook_normalFlow_otpType()
        {
            // given
            Map<String, Object> data = Map.of(
                    "cardId", "card-001",
                    "otpCode", "654321");
            // 文档定义类型为 "OTP"（接口契约文档 §3.1）
            when(webhookLogMapper.selectByIdempotencyKey("OTP:card-001:654321")).thenReturn(null);

            // when
            webhookService.dispatchWebhook("OTP", data);

            // then
            verify(redisCache).setCacheObject(
                    eq("vcc:otp:card-001"),
                    eq("654321"),
                    eq(10),
                    eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("兼容旧写法（3DS_OTP 类型）：OTP 写入 Redis")
        void handleOtpWebhook_normalFlow_3dsOtpType()
        {
            // given
            Map<String, Object> data = Map.of(
                    "cardId", "card-001",
                    "otpCode", "654321");
            when(webhookLogMapper.selectByIdempotencyKey("3DS_OTP:card-001:654321")).thenReturn(null);

            // when — 兼容旧写法，待上游确认后可移除此测试
            webhookService.dispatchWebhook("3DS_OTP", data);

            // then
            verify(redisCache).setCacheObject(
                    eq("vcc:otp:card-001"),
                    eq("654321"),
                    eq(10),
                    eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("幂等：重复 OTP 回调不重复写 Redis")
        void handleOtpWebhook_idempotent()
        {
            // given
            Map<String, Object> data = Map.of("cardId", "card-001", "otpCode", "654321");
            WebhookLog existing = TestUtils.buildWebhookLog(1L, "3DS_OTP");
            existing.setProcessed(WebhookLog.PROCESSED_YES);
            when(webhookLogMapper.selectByIdempotencyKey("3DS_OTP:card-001:654321")).thenReturn(existing);

            // when
            webhookService.dispatchWebhook("3DS_OTP", data);

            // then
            verify(redisCache, never()).setCacheObject(anyString(), any(), anyInt(), any());
        }

        @Test
        @DisplayName("缺少 cardId 时抛出异常")
        void handleOtpWebhook_missingCardId()
        {
            // given
            Map<String, Object> data = Map.of("otpCode", "654321");
            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("3DS_OTP", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }

        @Test
        @DisplayName("缺少 otpCode 时抛出异常")
        void handleOtpWebhook_missingOtpCode()
        {
            // given
            Map<String, Object> data = Map.of("cardId", "card-001");
            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("3DS_OTP", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }
    }

    // ==================== handleTransactionWebhook ====================

    @Nested
    @DisplayName("handleTransactionWebhook 交易处理")
    class TransactionWebhookTests
    {
        @Test
        @DisplayName("正常流程：委托给 WebhookTransactionService")
        void handleTransactionWebhook_normalFlow()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-100", "cardId", "card-x");
            when(webhookLogMapper.selectByIdempotencyKey("TRANSACTION:txn-100")).thenReturn(null);

            // when
            webhookService.dispatchWebhook("TRANSACTION", data);

            // then
            verify(webhookTransactionService).handleTransaction(data);
        }

        @Test
        @DisplayName("AUTH_TRANSACTION 类型也委托给 WebhookTransactionService")
        void handleAuthTransactionWebhook_normalFlow()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-200");
            when(webhookLogMapper.selectByIdempotencyKey("AUTH_TRANSACTION:txn-200")).thenReturn(null);

            // when
            webhookService.dispatchWebhook("AUTH_TRANSACTION", data);

            // then
            verify(webhookTransactionService).handleTransaction(data);
        }

        @Test
        @DisplayName("幂等：已处理的交易不重复处理")
        void handleTransactionWebhook_idempotent()
        {
            // given
            Map<String, Object> data = Map.of("tranId", "txn-100");
            WebhookLog existing = TestUtils.buildWebhookLog(5L, "TRANSACTION");
            existing.setProcessed(WebhookLog.PROCESSED_YES);
            when(webhookLogMapper.selectByIdempotencyKey("TRANSACTION:txn-100")).thenReturn(existing);

            // when
            webhookService.dispatchWebhook("TRANSACTION", data);

            // then
            verify(webhookTransactionService, never()).handleTransaction(any());
        }

        @Test
        @DisplayName("txnId 回退键：当 tranId 不存在时使用 txnId")
        void handleTransactionWebhook_fallbackTxnId()
        {
            // given: data 没有 tranId 但有 txnId
            Map<String, Object> data = Map.of("txnId", "alt-txn-300");
            when(webhookLogMapper.selectByIdempotencyKey("TRANSACTION:alt-txn-300")).thenReturn(null);

            // when
            webhookService.dispatchWebhook("TRANSACTION", data);

            // then
            verify(webhookTransactionService).handleTransaction(data);
        }
    }

    // ==================== handleCardStatusChangeWebhook ====================

    @Nested
    @DisplayName("handleCardStatusChangeWebhook 卡状态变更")
    class CardStatusChangeWebhookTests
    {
        @Test
        @DisplayName("正常流程：更新卡状态为冻结")
        void handleCardStatusChange_freeze()
        {
            // given
            Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", card.getUpstreamCardId());
            data.put("cardStatus", "FROZEN");

            when(webhookLogMapper.selectByIdempotencyKey("CARD_STATUS_CHANGE:" + card.getUpstreamCardId() + ":FROZEN"))
                    .thenReturn(null);
            when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);

            // when
            webhookService.dispatchWebhook("CARD_STATUS_CHANGE", data);

            // then
            ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
            verify(cardMapper).updateCard(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getId()).isEqualTo(200L);
            assertThat(cardCaptor.getValue().getStatus()).isEqualTo(Card.STATUS_FROZEN);
        }

        @Test
        @DisplayName("激活卡片时设置 activatedAt（首次激活）")
        void handleCardStatusChange_activate_setsActivatedAt()
        {
            // given: 卡片当前未激活，且 activatedAt 为 null
            Card card = TestUtils.buildCard(201L, 100L, Card.STATUS_INACTIVE);
            // activatedAt 默认为 null
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", card.getUpstreamCardId());
            data.put("cardStatus", "ACTIVE");

            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);
            when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);

            // when
            webhookService.dispatchWebhook("CARD_STATUS_CHANGE", data);

            // then
            ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
            verify(cardMapper).updateCard(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getStatus()).isEqualTo(Card.STATUS_ACTIVE);
            assertThat(cardCaptor.getValue().getActivatedAt()).isNotNull();
        }

        @Test
        @DisplayName("注销卡片时设置 cancelledAt")
        void handleCardStatusChange_cancelled_setsCancelledAt()
        {
            // given
            Card card = TestUtils.buildCard(202L, 100L, Card.STATUS_ACTIVE);
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", card.getUpstreamCardId());
            data.put("cardStatus", "CANCELLED");

            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);
            when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);

            // when
            webhookService.dispatchWebhook("CARD_STATUS_CHANGE", data);

            // then
            ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
            verify(cardMapper).updateCard(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getStatus()).isEqualTo(Card.STATUS_CANCELLED);
            assertThat(cardCaptor.getValue().getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("CARD_OPERATION 类型使用 operateType 字段")
        void handleCardOperation_usesOperateType()
        {
            // given
            Card card = TestUtils.buildCard(203L, 100L, Card.STATUS_ACTIVE);
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", card.getUpstreamCardId());
            data.put("operateType", "FREEZE");

            when(webhookLogMapper.selectByIdempotencyKey("CARD_OPERATION:" + card.getUpstreamCardId() + ":FREEZE"))
                    .thenReturn(null);
            when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);

            // when
            webhookService.dispatchWebhook("CARD_OPERATION", data);

            // then
            ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
            verify(cardMapper).updateCard(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getStatus()).isEqualTo(Card.STATUS_FROZEN);
        }

        @Test
        @DisplayName("幂等：重复的卡状态变更不重复处理")
        void handleCardStatusChange_idempotent()
        {
            // given
            Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", card.getUpstreamCardId());
            data.put("cardStatus", "FROZEN");

            WebhookLog existing = TestUtils.buildWebhookLog(10L, "CARD_STATUS_CHANGE");
            existing.setProcessed(WebhookLog.PROCESSED_YES);
            when(webhookLogMapper.selectByIdempotencyKey("CARD_STATUS_CHANGE:" + card.getUpstreamCardId() + ":FROZEN"))
                    .thenReturn(existing);

            // when
            webhookService.dispatchWebhook("CARD_STATUS_CHANGE", data);

            // then
            verify(cardMapper, never()).selectCardByUpstreamCardId(any());
            verify(cardMapper, never()).updateCard(any());
        }

        @Test
        @DisplayName("未找到本地卡片时抛出异常")
        void handleCardStatusChange_cardNotFound()
        {
            // given
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", "nonexistent-card");
            data.put("cardStatus", "ACTIVE");

            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);
            when(cardMapper.selectCardByUpstreamCardId("nonexistent-card")).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("CARD_STATUS_CHANGE", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }

        @Test
        @DisplayName("未知卡状态时抛出异常")
        void handleCardStatusChange_unknownStatus()
        {
            // given
            Card card = TestUtils.buildCard(204L, 100L, Card.STATUS_ACTIVE);
            Map<String, Object> data = new HashMap<>();
            data.put("cardId", card.getUpstreamCardId());
            data.put("cardStatus", "BOGUS_STATUS");

            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);
            when(cardMapper.selectCardByUpstreamCardId(card.getUpstreamCardId())).thenReturn(card);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("CARD_STATUS_CHANGE", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }
    }

    // ==================== handleRechargeResultWebhook ====================

    @Nested
    @DisplayName("handleRechargeResultWebhook 充值结果处理")
    class RechargeResultWebhookTests
    {
        @Test
        @DisplayName("SUCCESS：只更新状态，不补偿余额")
        void handleRechargeResult_success()
        {
            // given
            Map<String, Object> data = Map.of("orderNo", "ORD-001", "status", "SUCCESS");
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-001")).thenReturn(null);
            when(webhookRechargeHandler.handleRechargeSuccess("ORD-001")).thenReturn(true);

            // when
            webhookService.dispatchWebhook("RECHARGE_RESULT", data);

            // then
            verify(webhookRechargeHandler).handleRechargeSuccess("ORD-001");
            verify(webhookRechargeHandler, never()).handleRechargeFailure(anyString(), anyString());
        }

        @Test
        @DisplayName("FAILED：更新状态并补偿余额")
        void handleRechargeResult_failed()
        {
            // given
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", "ORD-002");
            data.put("status", "FAILED");
            data.put("failReason", "Insufficient upstream balance");
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-002")).thenReturn(null);
            when(webhookRechargeHandler.handleRechargeFailure("ORD-002", "Insufficient upstream balance"))
                    .thenReturn(true);

            // when
            webhookService.dispatchWebhook("RECHARGE_RESULT", data);

            // then
            verify(webhookRechargeHandler).handleRechargeFailure("ORD-002", "Insufficient upstream balance");
            verify(webhookRechargeHandler, never()).handleRechargeSuccess(anyString());
        }

        @Test
        @DisplayName("FAIL（小写变体）也走失败分支")
        void handleRechargeResult_failVariant()
        {
            // given
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", "ORD-003");
            data.put("status", "fail");
            data.put("message", "Network timeout");
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-003")).thenReturn(null);
            when(webhookRechargeHandler.handleRechargeFailure("ORD-003", "Network timeout"))
                    .thenReturn(true);

            // when
            webhookService.dispatchWebhook("RECHARGE_RESULT", data);

            // then
            verify(webhookRechargeHandler).handleRechargeFailure("ORD-003", "Network timeout");
        }

        @Test
        @DisplayName("使用 result 字段代替 status 字段")
        void handleRechargeResult_resultField()
        {
            // given: 没有 status 字段，但有 result 字段
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", "ORD-004");
            data.put("result", "SUCCESS");
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-004")).thenReturn(null);
            when(webhookRechargeHandler.handleRechargeSuccess("ORD-004")).thenReturn(true);

            // when
            webhookService.dispatchWebhook("RECHARGE_RESULT", data);

            // then
            verify(webhookRechargeHandler).handleRechargeSuccess("ORD-004");
        }

        @Test
        @DisplayName("SUCCESS 处理失败时抛出异常")
        void handleRechargeResult_successHandlerFails()
        {
            // given
            Map<String, Object> data = Map.of("orderNo", "ORD-005", "status", "SUCCESS");
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-005")).thenReturn(null);
            when(webhookRechargeHandler.handleRechargeSuccess("ORD-005")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("RECHARGE_RESULT", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }

        @Test
        @DisplayName("FAILED 处理失败时抛出异常")
        void handleRechargeResult_failureHandlerFails()
        {
            // given
            Map<String, Object> data = new HashMap<>();
            data.put("orderNo", "ORD-006");
            data.put("status", "FAILED");
            data.put("failReason", "some reason");
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-006")).thenReturn(null);
            when(webhookRechargeHandler.handleRechargeFailure("ORD-006", "some reason")).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("RECHARGE_RESULT", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }

        @Test
        @DisplayName("幂等：重复的充值回调不重复处理")
        void handleRechargeResult_idempotent()
        {
            // given
            Map<String, Object> data = Map.of("orderNo", "ORD-001", "status", "SUCCESS");
            WebhookLog existing = TestUtils.buildWebhookLog(20L, "RECHARGE_RESULT");
            existing.setProcessed(WebhookLog.PROCESSED_YES);
            when(webhookLogMapper.selectByIdempotencyKey("RECHARGE_RESULT:ORD-001")).thenReturn(existing);

            // when
            webhookService.dispatchWebhook("RECHARGE_RESULT", data);

            // then
            verify(webhookRechargeHandler, never()).handleRechargeSuccess(anyString());
            verify(webhookRechargeHandler, never()).handleRechargeFailure(anyString(), anyString());
        }

        @Test
        @DisplayName("缺少 orderNo 时抛出异常")
        void handleRechargeResult_missingOrderNo()
        {
            // given
            Map<String, Object> data = Map.of("status", "SUCCESS");
            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> webhookService.dispatchWebhook("RECHARGE_RESULT", data))
                    .isInstanceOf(WebhookProcessingException.class);
        }
    }

    // ==================== 边界场景 ====================

    @Nested
    @DisplayName("边界场景")
    class EdgeCaseTests
    {
        @Test
        @DisplayName("未知 webhookType 不抛出异常，只 warn 日志")
        void dispatchWebhook_unknownType_noException()
        {
            // given
            Map<String, Object> data = Map.of("something", "value");
            when(webhookLogMapper.selectByIdempotencyKey(anyString())).thenReturn(null);

            // when - 不应抛出异常
            webhookService.dispatchWebhook("UNKNOWN_TYPE", data);

            // then - 不调用任何处理器
            verify(webhookTransactionService, never()).handleTransaction(any());
            verify(cardMapper, never()).updateCard(any());
            verify(redisCache, never()).setCacheObject(anyString(), any(), anyInt(), any());
            verify(webhookRechargeHandler, never()).handleRechargeSuccess(anyString());
        }
    }
}
