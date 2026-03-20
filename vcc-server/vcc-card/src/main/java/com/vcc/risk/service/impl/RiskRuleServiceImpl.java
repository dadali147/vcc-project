package com.vcc.risk.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vcc.common.exception.ServiceException;
import com.vcc.risk.domain.RiskEvent;
import com.vcc.risk.domain.RiskRule;
import com.vcc.risk.mapper.RiskEventMapper;
import com.vcc.risk.mapper.RiskRuleMapper;
import com.vcc.risk.service.IRiskRuleService;

/**
 * 风控规则 服务实现
 */
@Service
public class RiskRuleServiceImpl implements IRiskRuleService
{
    private static final Logger log = LoggerFactory.getLogger(RiskRuleServiceImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RiskRuleMapper riskRuleMapper;

    @Autowired
    private RiskEventMapper riskEventMapper;

    @Override
    public RiskRule selectRiskRuleById(Long id)
    {
        return riskRuleMapper.selectRiskRuleById(id);
    }

    @Override
    public List<RiskRule> selectRiskRuleList(RiskRule riskRule)
    {
        return riskRuleMapper.selectRiskRuleList(riskRule);
    }

    @Override
    @Transactional
    public int insertRiskRule(RiskRule riskRule)
    {
        // === 业务校验 ===
        validateRiskRule(riskRule, true);

        // merchantId 为 null 时自动设置为全局规则
        if (riskRule.getMerchantId() == null)
        {
            riskRule.setScope(RiskRule.SCOPE_GLOBAL);
            if (riskRule.getRemark() == null || riskRule.getRemark().isEmpty())
            {
                riskRule.setRemark("全局规则（merchantId 为空自动标注）");
            }
        }

        return riskRuleMapper.insertRiskRule(riskRule);
    }

    @Override
    @Transactional
    public int updateRiskRule(RiskRule riskRule)
    {
        // === 业务校验 ===
        validateRiskRule(riskRule, false);

        // merchantId 为 null 时自动设置为全局规则
        if (riskRule.getMerchantId() == null && riskRule.getScope() == null)
        {
            riskRule.setScope(RiskRule.SCOPE_GLOBAL);
        }

        return riskRuleMapper.updateRiskRule(riskRule);
    }

    /**
     * 风控规则业务校验
     *
     * @param riskRule 规则对象
     * @param isInsert 是否为新增操作
     */
    private void validateRiskRule(RiskRule riskRule, boolean isInsert)
    {
        // 1. ruleCode 唯一性校验
        if (riskRule.getRuleCode() != null && !riskRule.getRuleCode().isEmpty())
        {
            RiskRule existing = riskRuleMapper.selectRiskRuleByCode(riskRule.getRuleCode());
            if (existing != null)
            {
                if (isInsert || (riskRule.getId() != null && !existing.getId().equals(riskRule.getId())))
                {
                    throw new ServiceException("规则编码 [" + riskRule.getRuleCode() + "] 已存在，不允许重复");
                }
            }
        }

        // 2. thresholdAmount 不能为空或负数（daily_count 类型除外）
        String ruleType = riskRule.getRuleType();
        boolean isDailyCount = RiskRule.RULE_TYPE_DAILY_COUNT.equals(ruleType);
        if (!isDailyCount)
        {
            if (riskRule.getThresholdAmount() == null)
            {
                throw new ServiceException("阈值金额（thresholdAmount）不能为空");
            }
            if (riskRule.getThresholdAmount().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("阈值金额（thresholdAmount）不能为负数");
            }
        }

        // 3. daily_count 类型必须有 thresholdCount
        if (isDailyCount)
        {
            if (riskRule.getThresholdCount() == null || riskRule.getThresholdCount() <= 0)
            {
                throw new ServiceException("日累计次数规则（daily_count）必须设置正整数的 thresholdCount");
            }
        }

        // 4. action 必须在枚举范围内
        if (riskRule.getAction() != null && !riskRule.getAction().isEmpty())
        {
            if (!RiskRule.VALID_ACTIONS.contains(riskRule.getAction()))
            {
                throw new ServiceException("触发动作（action）必须为 BLOCK / ALERT / REVIEW，当前值: " + riskRule.getAction());
            }
        }
    }

    @Override
    @Transactional
    public int deleteRiskRuleByIds(Long[] ids)
    {
        return riskRuleMapper.deleteRiskRuleByIds(ids);
    }

    @Override
    @Transactional
    public int toggleRiskRule(Long id)
    {
        RiskRule rule = riskRuleMapper.selectRiskRuleById(id);
        if (rule == null)
        {
            throw new ServiceException("风控规则不存在");
        }
        RiskRule update = new RiskRule();
        update.setId(id);
        if (RiskRule.STATUS_ENABLED.equals(rule.getStatus()))
        {
            update.setStatus(RiskRule.STATUS_DISABLED);
        }
        else
        {
            update.setStatus(RiskRule.STATUS_ENABLED);
        }
        log.info("切换风控规则状态, ruleId={}, from={}, to={}", id, rule.getStatus(), update.getStatus());
        return riskRuleMapper.updateRiskRule(update);
    }

    @Override
    @Transactional
    public String checkTransaction(Long merchantId, BigDecimal amount, String ruleType, String cardType)
    {
        if (merchantId == null || amount == null || ruleType == null)
        {
            return null;
        }

        // 加载对应类型的启用规则（已按 priority 排序）
        List<RiskRule> rules = riskRuleMapper.selectEnabledRulesByType(ruleType, merchantId);
        if (rules == null || rules.isEmpty())
        {
            return null;
        }

        // 过滤卡类型维度：只保留匹配的规则（cardType 为 null 的规则适用所有卡类型）
        List<RiskRule> filteredRules = rules.stream()
                .filter(rule -> rule.getCardType() == null
                        || rule.getCardType().isEmpty()
                        || rule.getCardType().equals(cardType))
                .collect(Collectors.toList());

        if (filteredRules.isEmpty())
        {
            return null;
        }

        for (RiskRule rule : filteredRules)
        {
            boolean triggered = false;
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("merchantId", merchantId);
            detailMap.put("amount", amount.toPlainString());
            detailMap.put("ruleType", ruleType);
            detailMap.put("ruleCode", rule.getRuleCode());
            detailMap.put("cardType", cardType);

            switch (ruleType)
            {
                case RiskRule.RULE_TYPE_SINGLE_AMOUNT:
                    // 单笔金额限制
                    if (rule.getThresholdAmount() != null
                            && amount.compareTo(rule.getThresholdAmount()) > 0)
                    {
                        triggered = true;
                        detailMap.put("checkType", "single_amount");
                        detailMap.put("threshold", rule.getThresholdAmount().toPlainString());
                    }
                    break;

                case RiskRule.RULE_TYPE_DAILY_AMOUNT:
                    // 日累计金额限制
                    BigDecimal dailySum = riskRuleMapper.sumDailyAmount(merchantId);
                    BigDecimal totalAfter = dailySum.add(amount);
                    if (rule.getThresholdAmount() != null
                            && totalAfter.compareTo(rule.getThresholdAmount()) > 0)
                    {
                        triggered = true;
                        detailMap.put("checkType", "daily_amount");
                        detailMap.put("dailySum", dailySum.toPlainString());
                        detailMap.put("totalAfterThis", totalAfter.toPlainString());
                        detailMap.put("threshold", rule.getThresholdAmount().toPlainString());
                    }
                    break;

                case RiskRule.RULE_TYPE_DAILY_COUNT:
                    // 日累计次数限制
                    int dailyCount = riskRuleMapper.countDailyTransactions(merchantId);
                    if (rule.getThresholdCount() != null
                            && (dailyCount + 1) > rule.getThresholdCount())
                    {
                        triggered = true;
                        detailMap.put("checkType", "daily_count");
                        detailMap.put("currentDailyCount", dailyCount);
                        detailMap.put("thresholdCount", rule.getThresholdCount());
                    }
                    break;

                default:
                    // 兼容旧规则类型 SINGLE_LIMIT / DAILY_LIMIT 等，走原始 amount > threshold 逻辑
                    if (rule.getThresholdAmount() != null
                            && amount.compareTo(rule.getThresholdAmount()) > 0)
                    {
                        triggered = true;
                        detailMap.put("checkType", "legacy_amount");
                        detailMap.put("threshold", rule.getThresholdAmount().toPlainString());
                    }
                    break;
            }

            if (triggered)
            {
                log.warn("风控规则触发, merchantId={}, ruleId={}, ruleCode={}, ruleType={}, action={}, cardType={}",
                        merchantId, rule.getId(), rule.getRuleCode(), ruleType, rule.getAction(), cardType);

                // 创建风控事件 —— 使用 ObjectMapper 序列化 triggerDetail
                RiskEvent event = new RiskEvent();
                event.setMerchantId(merchantId);
                event.setRuleId(rule.getId());
                event.setStatus(RiskEvent.STATUS_PENDING);
                event.setTriggerDetail(serializeTriggerDetail(detailMap));
                riskEventMapper.insertRiskEvent(event);

                return rule.getAction();
            }
        }

        return null;
    }

    /**
     * 使用 ObjectMapper 将触发详情序列化为 JSON 字符串
     */
    private String serializeTriggerDetail(Map<String, Object> detailMap)
    {
        try
        {
            return objectMapper.writeValueAsString(detailMap);
        }
        catch (JsonProcessingException e)
        {
            log.error("序列化 triggerDetail 失败", e);
            // 降级：返回 toString，不再手拼 JSON
            return detailMap.toString();
        }
    }
}
