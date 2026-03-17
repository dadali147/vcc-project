package com.vcc.finance.service;

import com.vcc.card.domain.Card;
import com.vcc.card.mapper.CardMapper;
import com.vcc.common.TestUtils;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.finance.service.impl.RechargeServiceImpl;
import com.vcc.system.service.ISystemConfigService;
import com.vcc.upstream.YeeVccClient;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import com.vcc.user.service.IUserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RechargeServiceImplTest
{
    @Mock
    private RechargeMapper rechargeMapper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private YeeVccClient yeeVccClient;

    @Mock
    private IUserAccountService userAccountService;

    @Mock
    private ISystemConfigService systemConfigService;

    @InjectMocks
    private RechargeServiceImpl rechargeService;

    @Test
    @DisplayName("testSubmitRecharge_成功路径（mock上游返回成功）")
    void testSubmitRecharge_成功路径_mock上游返回成功()
    {
        // given: 风控放行、卡片归属正确且上游返回成功
        Card card = mockRechargePrerequisites(100L, new BigDecimal("0.00"));
        when(userAccountService.deductBalance(100L, "USD", TestUtils.amount("100.00"))).thenReturn(true);
        when(yeeVccClient.recharge(any(YeeVccRequests.RechargeRequest.class))).thenReturn(successRechargeResponse("upstream-001"));
        ArgumentCaptor<Recharge> rechargeCaptor = ArgumentCaptor.forClass(Recharge.class);
        ArgumentCaptor<YeeVccRequests.RechargeRequest> requestCaptor = ArgumentCaptor.forClass(YeeVccRequests.RechargeRequest.class);

        // when: 发起充值
        Recharge recharge = rechargeService.submitRecharge(100L, card.getId(), TestUtils.amount("100.00"), "USD", null);

        // then: 本地保持 pending，且上游请求金额扣除了手续费
        verify(yeeVccClient).recharge(requestCaptor.capture());
        verify(rechargeMapper).insertRecharge(rechargeCaptor.capture());
        assertThat(recharge.getStatus()).isEqualTo(Recharge.STATUS_PENDING);
        assertThat(recharge.getUpstreamOrderNo()).isEqualTo("upstream-001");
        assertThat(rechargeCaptor.getValue().getFee()).isEqualByComparingTo("1.00");
        assertThat(rechargeCaptor.getValue().getActualAmount()).isEqualByComparingTo("99.00");
        assertThat(requestCaptor.getValue().getCardId()).isEqualTo(card.getUpstreamCardId());
        assertThat(requestCaptor.getValue().getAmount()).isEqualByComparingTo("99.00");
        verify(userAccountService, never()).addBalance(anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("testSubmitRecharge_余额不足抛异常")
    void testSubmitRecharge_余额不足抛异常()
    {
        // given: 前置校验通过，但账户扣款失败
        Card card = mockRechargePrerequisites(100L, new BigDecimal("0.00"));
        when(userAccountService.deductBalance(100L, "USD", TestUtils.amount("100.00"))).thenReturn(false);

        // when / then: 应直接拒绝充值
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, card.getId(), TestUtils.amount("100.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("账户余额不足");
        verify(rechargeMapper, never()).insertRecharge(any(Recharge.class));
    }

    @Test
    @DisplayName("testSubmitRecharge_卡片不存在抛异常")
    void testSubmitRecharge_卡片不存在抛异常()
    {
        // given: 充值前置校验里查不到卡片
        mockCommonRiskConfig("true", "", "", new BigDecimal("0.00"));
        when(cardMapper.selectCardById(200L)).thenReturn(null);

        // when / then: 应提示卡片不存在
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("20.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("卡片不存在");
        verify(userAccountService, never()).deductBalance(anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("testSubmitRecharge_越权操作抛异常（cardUserId != currentUserId）")
    void testSubmitRecharge_越权操作抛异常_cardUserId不等于currentUserId()
    {
        // given: 卡片存在，但归属其他用户
        mockCommonRiskConfig("true", "", "", new BigDecimal("0.00"));
        Card card = TestUtils.buildCard(200L, 999L, Card.STATUS_ACTIVE);
        when(cardMapper.selectCardById(200L)).thenReturn(card);

        // when / then: 应拒绝越权充值
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("20.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("无权操作该卡片");
        verify(userAccountService, never()).deductBalance(anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("testSubmitRecharge_超日限额抛异常")
    void testSubmitRecharge_超日限额抛异常()
    {
        // given: 今日已充值金额与本次金额之和超过日限额
        mockCommonRiskConfig("true", "", "100.00", new BigDecimal("85.00"));

        // when / then: 应按日限额拒绝
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("20.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("今日充值总额将超过上限");
        verify(cardMapper, never()).selectCardById(anyLong());
    }

    @Test
    @DisplayName("testHandleRechargeResult_PENDING转SUCCESS补偿不重复（幂等）")
    void testHandleRechargeResult_PENDING转SUCCESS补偿不重复_幂等()
    {
        // given: 待处理充值经查询转为成功
        Recharge recharge = TestUtils.buildRecharge(1L, 100L, 200L, "RCH001", TestUtils.amount("100.00"), Recharge.STATUS_PENDING);
        Recharge lockedRecharge = TestUtils.buildRecharge(1L, 100L, 200L, "RCH001", TestUtils.amount("100.00"), Recharge.STATUS_PENDING);
        when(rechargeMapper.selectRechargeByOrderNo("RCH001")).thenReturn(recharge);
        when(rechargeMapper.selectRechargeForUpdateById(1L)).thenReturn(lockedRecharge);
        when(yeeVccClient.queryRechargeResult(any(YeeVccRequests.QueryRechargeResultRequest.class))).thenReturn(queryResponse("SUCCESS", null));
        when(rechargeMapper.updateRechargeStatus(eq(1L), eq(Recharge.STATUS_SUCCESS), eq(Recharge.STATUS_PENDING), isNull(), any()))
                .thenReturn(1);

        // when: 连续两次触发结果处理
        Recharge firstResult = rechargeService.queryRechargeResult(100L, "RCH001");
        Recharge secondResult = rechargeService.queryRechargeResult(100L, "RCH001");

        // then: 仅首次发生状态流转，余额不会重复补偿
        assertThat(firstResult.getStatus()).isEqualTo(Recharge.STATUS_SUCCESS);
        assertThat(secondResult.getStatus()).isEqualTo(Recharge.STATUS_SUCCESS);
        verify(yeeVccClient).queryRechargeResult(any(YeeVccRequests.QueryRechargeResultRequest.class));
        verify(userAccountService, never()).addBalance(anyLong(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("testHandleRechargeResult_FAILED终态补偿余额")
    void testHandleRechargeResult_FAILED终态补偿余额()
    {
        // given: 待处理充值被上游确认失败
        Recharge recharge = TestUtils.buildRecharge(2L, 100L, 200L, "RCH002", TestUtils.amount("80.00"), Recharge.STATUS_PENDING);
        Recharge lockedRecharge = TestUtils.buildRecharge(2L, 100L, 200L, "RCH002", TestUtils.amount("80.00"), Recharge.STATUS_PENDING);
        when(rechargeMapper.selectRechargeByOrderNo("RCH002")).thenReturn(recharge);
        when(rechargeMapper.selectRechargeForUpdateById(2L)).thenReturn(lockedRecharge);
        when(yeeVccClient.queryRechargeResult(any(YeeVccRequests.QueryRechargeResultRequest.class))).thenReturn(queryResponse("FAILED", "issuer reject"));
        when(rechargeMapper.updateRechargeStatus(eq(2L), eq(Recharge.STATUS_FAILED), eq(Recharge.STATUS_PENDING), eq("issuer reject"), any()))
                .thenReturn(1);

        // when: 查询待处理充值结果
        Recharge result = rechargeService.queryRechargeResult(100L, "RCH002");

        // then: 本地应转失败并补偿余额
        assertThat(result.getStatus()).isEqualTo(Recharge.STATUS_FAILED);
        assertThat(result.getFailReason()).isEqualTo("issuer reject");
        verify(userAccountService).addBalance(100L, "USD", TestUtils.amount("80.00"));
    }

    @Test
    @DisplayName("testHandleRechargeResult_已FAILED再次触发不重复补偿")
    void testHandleRechargeResult_已FAILED再次触发不重复补偿()
    {
        // given: 订单已经是失败终态
        Recharge failedRecharge = TestUtils.buildRecharge(3L, 100L, 200L, "RCH003", TestUtils.amount("60.00"), Recharge.STATUS_FAILED);
        when(rechargeMapper.selectRechargeByOrderNo("RCH003")).thenReturn(failedRecharge);

        // when: 再次触发结果处理
        Recharge result = rechargeService.queryRechargeResult(100L, "RCH003");

        // then: 直接返回现有终态，不再访问上游，也不再补偿余额
        assertThat(result.getStatus()).isEqualTo(Recharge.STATUS_FAILED);
        verify(yeeVccClient, never()).queryRechargeResult(any(YeeVccRequests.QueryRechargeResultRequest.class));
        verify(userAccountService, never()).addBalance(anyLong(), anyString(), any(BigDecimal.class));
    }

    private Card mockRechargePrerequisites(Long userId, BigDecimal todayTotal)
    {
        mockCommonRiskConfig("true", "", "", todayTotal);
        Card card = TestUtils.buildCard(200L, userId, Card.STATUS_ACTIVE);
        when(cardMapper.selectCardById(200L)).thenReturn(card);
        return card;
    }

    private void mockCommonRiskConfig(String riskSwitch, String singleLimit, String dailyLimit, BigDecimal todayTotal)
    {
        lenient().when(systemConfigService.get("risk.recharge.enabled")).thenReturn(riskSwitch);
        lenient().when(systemConfigService.get("risk.single.recharge.limit")).thenReturn(singleLimit);
        lenient().when(systemConfigService.get("risk.daily.recharge.limit")).thenReturn(dailyLimit);
        lenient().when(systemConfigService.get("fee.recharge.prepaid.rate")).thenReturn("0.01");
        lenient().when(systemConfigService.get("fee.recharge.budget.rate")).thenReturn("0.005");
        lenient().when(rechargeMapper.selectTodayRechargeTotal(eq(100L), anyString(), anyString())).thenReturn(todayTotal);
    }

    private YeeVccApiResponse<YeeVccModels.OperationData> successRechargeResponse(String upstreamOrderNo)
    {
        YeeVccModels.OperationData data = new YeeVccModels.OperationData();
        data.setOrderId(upstreamOrderNo);

        YeeVccApiResponse<YeeVccModels.OperationData> response = new YeeVccApiResponse<>();
        response.setSuccess(true);
        response.setStatus(200);
        response.setData(data);
        return response;
    }

    private YeeVccApiResponse<YeeVccModels.OperationData> queryResponse(String status, String message)
    {
        YeeVccModels.OperationData data = new YeeVccModels.OperationData();
        data.setStatus(status);
        data.setMessage(message);

        YeeVccApiResponse<YeeVccModels.OperationData> response = new YeeVccApiResponse<>();
        response.setSuccess(true);
        response.setStatus(200);
        response.setData(data);
        return response;
    }
}
