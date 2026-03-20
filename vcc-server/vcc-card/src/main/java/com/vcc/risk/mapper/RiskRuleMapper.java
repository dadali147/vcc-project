package com.vcc.risk.mapper;

import com.vcc.risk.domain.RiskRule;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风控规则 数据层
 */
public interface RiskRuleMapper
{
    public RiskRule selectRiskRuleById(Long id);

    public List<RiskRule> selectRiskRuleList(RiskRule riskRule);

    /**
     * 查询指定类型的启用规则（按 priority 优先级排序，scope MERCHANT 优先于 GLOBAL）
     */
    public List<RiskRule> selectEnabledRulesByType(@Param("ruleType") String ruleType,
                                                    @Param("merchantId") Long merchantId);

    /**
     * 按 ruleCode 查询规则（用于唯一性校验）
     */
    public RiskRule selectRiskRuleByCode(@Param("ruleCode") String ruleCode);

    /**
     * 查询商户当日交易次数
     */
    public int countDailyTransactions(@Param("merchantId") Long merchantId);

    /**
     * 查询商户当日累计交易金额（基于风控事件表）
     * 注意：充值日累计风控口径见 RechargeMapper.selectTodayRechargeTotal，只统计 PENDING+SUCCESS
     */
    public BigDecimal sumDailyAmount(@Param("merchantId") Long merchantId);

    public int insertRiskRule(RiskRule riskRule);

    public int updateRiskRule(RiskRule riskRule);

    public int deleteRiskRuleById(Long id);

    public int deleteRiskRuleByIds(Long[] ids);
}
