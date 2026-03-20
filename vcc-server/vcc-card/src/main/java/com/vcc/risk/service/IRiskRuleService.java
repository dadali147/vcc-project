package com.vcc.risk.service;

import com.vcc.risk.domain.RiskRule;

import java.math.BigDecimal;
import java.util.List;

/**
 * 风控规则 服务层
 */
public interface IRiskRuleService
{
    public RiskRule selectRiskRuleById(Long id);

    public List<RiskRule> selectRiskRuleList(RiskRule riskRule);

    public int insertRiskRule(RiskRule riskRule);

    public int updateRiskRule(RiskRule riskRule);

    public int deleteRiskRuleByIds(Long[] ids);

    /**
     * 启用/禁用规则
     *
     * @param id 规则ID
     * @return 影响行数
     */
    public int toggleRiskRule(Long id);

    /**
     * 检查交易是否违反风控规则
     * <p>
     * 支持的规则类型：
     * - single_amount: 单笔金额限制
     * - daily_amount: 当日累计金额限制
     * - daily_count: 当日累计次数限制
     * </p>
     *
     * @param merchantId 商户ID
     * @param amount 交易金额
     * @param ruleType 规则类型（single_amount/daily_amount/daily_count）
     * @param cardType 卡类型（PREPAID/BUDGET），可为 null
     * @return 触发动作（BLOCK/ALERT/REVIEW），未触发返回null
     */
    public String checkTransaction(Long merchantId, BigDecimal amount, String ruleType, String cardType);
}
