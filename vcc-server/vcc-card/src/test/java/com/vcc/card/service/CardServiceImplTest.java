package com.vcc.card.service;

import com.vcc.card.domain.Card;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.mapper.CardHolderMapper;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.TransactionMapper;
import com.vcc.card.service.impl.CardPersistService;
import com.vcc.card.service.impl.CardServiceImpl;
import com.vcc.common.TestUtils;
import com.vcc.system.service.ISystemConfigService;
import com.vcc.upstream.adapter.ChannelAwareYeeVccAdapter;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest
{
    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardHolderMapper cardHolderMapper;

    @Mock
    private ChannelAwareYeeVccAdapter yeeVccAdapter;

    @Mock
    private ISystemConfigService systemConfigService;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private CardPersistService cardPersistService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    @DisplayName("testFreezeCard_归属校验通过")
    void testFreezeCard_归属校验通过()
    {
        // given: 当前用户持有一张激活卡
        Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
        when(cardMapper.selectCardById(200L)).thenReturn(card);
        when(yeeVccAdapter.freezeCard(any(YeeVccRequests.FreezeCardRequest.class))).thenReturn(successOperationResponse());
        when(cardMapper.updateCard(any(Card.class))).thenReturn(1);
        ArgumentCaptor<Card> updateCaptor = ArgumentCaptor.forClass(Card.class);

        // when: 冻结卡片
        int rows = cardService.freezeCard(200L, 100L);

        // then: 上游冻结成功后，本地状态应变为冻结
        assertThat(rows).isEqualTo(1);
        verify(cardMapper).updateCard(updateCaptor.capture());
        assertThat(updateCaptor.getValue().getId()).isEqualTo(200L);
        assertThat(updateCaptor.getValue().getStatus()).isEqualTo(Card.STATUS_FROZEN);
    }

    @Test
    @DisplayName("testFreezeCard_越权操作抛异常")
    void testFreezeCard_越权操作抛异常()
    {
        // given: 卡片归属于其他用户
        when(cardMapper.selectCardById(200L)).thenReturn(TestUtils.buildCard(200L, 999L, Card.STATUS_ACTIVE));

        // when / then: 应拒绝冻结
        assertThatThrownBy(() -> cardService.freezeCard(200L, 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权操作该卡片");
        verify(yeeVccAdapter, never()).freezeCard(any(YeeVccRequests.FreezeCardRequest.class));
    }

    @Test
    @DisplayName("testGetCardSecret_归属校验通过")
    void testGetCardSecret_归属校验通过()
    {
        // given: 当前用户查询自己的卡三要素
        Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
        when(cardMapper.selectCardById(200L)).thenReturn(card);
        when(yeeVccAdapter.getCardKeyInfo(any(YeeVccRequests.GetCardKeyInfoRequest.class))).thenReturn(cardKeyInfoResponse());

        // when: 查询卡三要素
        Map<String, String> secret = cardService.getCardKeyInfo(200L, 100L);

        // then: 返回卡号、cvv 和有效期
        assertThat(secret)
                .containsEntry("cardNumber", "4111111111111111")
                .containsEntry("cvv", "123")
                .containsEntry("expiryDate", "12/30");
    }

    @Test
    @DisplayName("testGetCardSecret_越权操作抛异常")
    void testGetCardSecret_越权操作抛异常()
    {
        // given: 卡片归属不匹配
        when(cardMapper.selectCardById(200L)).thenReturn(TestUtils.buildCard(200L, 999L, Card.STATUS_ACTIVE));

        // when / then: 不允许查询他人卡密
        assertThatThrownBy(() -> cardService.getCardKeyInfo(200L, 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权操作该卡片");
        verify(yeeVccAdapter, never()).getCardKeyInfo(any(YeeVccRequests.GetCardKeyInfoRequest.class));
    }

    @Test
    @DisplayName("testOpenCard_持卡人不属于当前用户抛异常")
    void testOpenCard_持卡人不属于当前用户抛异常()
    {
        // given: 持卡人存在，但归属于其他用户
        lenient().when(systemConfigService.get("risk.card.open.enabled")).thenReturn("true");
        CardHolder holder = TestUtils.buildCardHolder(300L, 999L);
        when(cardHolderMapper.selectCardHolderById(300L)).thenReturn(holder);

        // when / then: 应拒绝使用别人的持卡人开卡
        assertThatThrownBy(() -> cardService.openCard(300L, "BIN-001", "USD", Card.TYPE_PREPAID, TestUtils.amount("50.00"), 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权使用该持卡人");
        verify(yeeVccAdapter, never()).openCard(any(YeeVccRequests.OpenCardRequest.class));
    }

    @Test
    @DisplayName("testOpenCard_事务提交成功（使用独立CardPersistService）")
    void testOpenCard_事务提交成功_使用独立CardPersistService() 
    {
        // given: 持卡人归属正确，上游开卡任务首次查询即成功
        lenient().when(systemConfigService.get("risk.card.open.enabled")).thenReturn("true");
        CardHolder holder = TestUtils.buildCardHolder(300L, 100L);
        Card persistedCard = TestUtils.buildCard(500L, 100L, Card.STATUS_ACTIVE);
        when(cardHolderMapper.selectCardHolderById(300L)).thenReturn(holder);
        when(yeeVccAdapter.openCard(any(YeeVccRequests.OpenCardRequest.class))).thenReturn(openCardResponse(123L));
        when(yeeVccAdapter.queryOpenCardResult(any(YeeVccRequests.QueryOpenCardResultRequest.class))).thenReturn(openCardQueryResponse());
        when(cardPersistService.saveCardInTransaction(300L, 100L, "BIN-001", "USD", Card.TYPE_PREPAID, TestUtils.amount("50.00"), any(YeeVccModels.CardData.class)))
                .thenReturn(persistedCard);
        ArgumentCaptor<YeeVccModels.CardData> cardDataCaptor = ArgumentCaptor.forClass(YeeVccModels.CardData.class);

        // when: 发起开卡
        Card result = cardService.openCard(300L, "BIN-001", "USD", Card.TYPE_PREPAID, TestUtils.amount("50.00"), 100L);

        // then: 开卡结果应交由独立持久化服务提交事务保存
        assertThat(result).isSameAs(persistedCard);
        verify(cardPersistService).saveCardInTransaction(
                300L,
                100L,
                "BIN-001",
                "USD",
                Card.TYPE_PREPAID,
                TestUtils.amount("50.00"),
                cardDataCaptor.capture());
        assertThat(cardDataCaptor.getValue().getCardId()).isEqualTo("up-card-500");
        assertThat(cardDataCaptor.getValue().getCardStatus()).isEqualTo("ACTIVE");
    }

    private YeeVccApiResponse<YeeVccModels.OperationData> successOperationResponse()
    {
        YeeVccApiResponse<YeeVccModels.OperationData> response = new YeeVccApiResponse<>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(new YeeVccModels.OperationData());
        return response;
    }

    private YeeVccApiResponse<YeeVccModels.CardKeyInfoData> cardKeyInfoResponse()
    {
        YeeVccModels.CardKeyInfoData data = new YeeVccModels.CardKeyInfoData();
        data.setCardNumber("4111111111111111");
        data.setCvv("123");
        data.setExpiryDate("12/30");

        YeeVccApiResponse<YeeVccModels.CardKeyInfoData> response = new YeeVccApiResponse<>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    private YeeVccApiResponse<YeeVccModels.OpenCardTaskData> openCardResponse(Long taskId)
    {
        YeeVccModels.OpenCardTaskData data = new YeeVccModels.OpenCardTaskData();
        data.setTaskId(taskId);

        YeeVccApiResponse<YeeVccModels.OpenCardTaskData> response = new YeeVccApiResponse<>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    private YeeVccApiResponse<YeeVccModels.OpenCardTaskData> openCardQueryResponse()
    {
        YeeVccModels.CardData cardData = new YeeVccModels.CardData();
        cardData.setCardId("up-card-500");
        cardData.setMaskedCardNumber("4111********1111");
        cardData.setCardStatus("ACTIVE");
        cardData.setBalance(TestUtils.amount("50.00"));

        YeeVccModels.OpenCardTaskData data = new YeeVccModels.OpenCardTaskData();
        data.setStatus(1);
        data.setCardList(List.of(cardData));

        YeeVccApiResponse<YeeVccModels.OpenCardTaskData> response = new YeeVccApiResponse<>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}
