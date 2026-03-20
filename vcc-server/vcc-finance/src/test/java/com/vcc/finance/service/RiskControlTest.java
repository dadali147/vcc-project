package com.vcc.finance.service;

import com.vcc.card.domain.Card;
import com.vcc.card.mapper.CardMapper;
import com.vcc.common.TestUtils;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.finance.service.impl.RechargeServiceImpl;
import com.vcc.system.service.ISystemConfigService;
import com.vcc.upstream.adapter.ChannelAwareYeeVccAdapter;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.user.service.IUserAccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskControlTest
{
    @Mock
    private RechargeMapper rechargeMapper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private ChannelAwareYeeVccAdapter yeeVccAdapter;

    @Mock
    private IUserAccountService userAccountService;

    @Mock
    private ISystemConfigService systemConfigService;

    @InjectMocks
    private RechargeServiceImpl rechargeService;

    @Test
    @DisplayName("testDailyLimit_PENDING订单计入限额")
    void testDailyLimit_PENDING订单计入限额()
    {
        // given: Mapper 返回的今日累计金额已包含 pending 订单
        mockRiskConfig("true", "", "100.00", TestUtils.amount("70.00"));

        // when / then: 新充值会因为累计金额超限而被拒绝
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("40.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("今日充值总额将超过上限");
    }

    @Test
    @DisplayName("testDailyLimit_SUCCESS订单计入限额")
    void testDailyLimit_SUCCESS订单计入限额()
    {
        // given: Mapper 返回的今日累计金额已包含 success 订单
        mockRiskConfig("true", "", "100.00", TestUtils.amount("60.00"));

        // when / then: 累计金额超限时仍应拒绝
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("50.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("今日充值总额将超过上限");
    }

    @Test
    @DisplayName("testDailyLimit_超限拒绝充值")
    void testDailyLimit_超限拒绝充值()
    {
        // given: 日限额刚好会被本次充值突破
        mockRiskConfig("true", "", "100.00", TestUtils.amount("100.00"));

        // when / then: 超限金额应被拒绝
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("0.01"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("今日充值总额将超过上限");
    }

    @Test
    @DisplayName("testSingleLimit_超单笔限额拒绝")
    void testSingleLimit_超单笔限额拒绝()
    {
        // given: 单笔限额为 50
        mockRiskConfig("true", "50.00", "", BigDecimal.ZERO);

        // when / then: 本次充值超过单笔上限
        assertThatThrownBy(() -> rechargeService.submitRecharge(100L, 200L, TestUtils.amount("51.00"), "USD", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("单笔充值金额超过上限");
    }

    @Test
    @DisplayName("testRiskSwitch_风控关闭时不做限制")
    void testRiskSwitch_风控关闭时不做限制()
    {
        // given: 风控总开关关闭，但单笔/日限额仍配置得很低
        mockRiskConfig("false", "10.00", "10.00", TestUtils.amount("999.00"));
        Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
        when(cardMapper.selectCardById(200L)).thenReturn(card);
        when(userAccountService.deductBalance(100L, "USD", TestUtils.amount("1000.00"))).thenReturn(true);
        when(yeeVccAdapter.recharge(any())).thenReturn(successResponse());

        // when: 提交一笔本会被风控拦截的大额充值
        Recharge recharge = rechargeService.submitRecharge(100L, 200L, TestUtils.amount("1000.00"), "USD", null);

        // then: 风控关闭后不再按限额拦截，流程可继续执行
        assertThat(recharge.getStatus()).isEqualTo(Recharge.STATUS_PENDING);
        verify(rechargeMapper).insertRecharge(any(Recharge.class));
        verify(cardMapper).selectCardById(200L);
        verify(userAccountService, never()).addBalance(anyLong(), anyString(), any(BigDecimal.class));
    }

    private void mockRiskConfig(String riskSwitch, String singleLimit, String dailyLimit, BigDecimal todayTotal)
    {
        lenient().when(systemConfigService.get("risk.recharge.enabled")).thenReturn(riskSwitch);
        lenient().when(systemConfigService.get("risk.single.recharge.limit")).thenReturn(singleLimit);
        lenient().when(systemConfigService.get("risk.daily.recharge.limit")).thenReturn(dailyLimit);
        lenient().when(systemConfigService.get("fee.recharge.prepaid.rate")).thenReturn("0.01");
        lenient().when(systemConfigService.get("fee.recharge.budget.rate")).thenReturn("0.005");
        lenient().when(rechargeMapper.selectTodayRechargeTotal(eq(100L), anyString(), anyString())).thenReturn(todayTotal);
    }

    private YeeVccApiResponse<YeeVccModels.OperationData> successResponse()
    {
        YeeVccModels.OperationData data = new YeeVccModels.OperationData();
        data.setOrderId("risk-pass");

        YeeVccApiResponse<YeeVccModels.OperationData> response = new YeeVccApiResponse<>();
        response.setStatus(200);
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
}
