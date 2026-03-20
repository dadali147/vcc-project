package com.vcc.risk.service;

import com.vcc.risk.domain.RiskEvent;

import java.util.List;

/**
 * 风控事件 服务层
 */
public interface IRiskEventService
{
    public RiskEvent selectRiskEventById(Long id);

    public List<RiskEvent> selectRiskEventList(RiskEvent riskEvent);

    public int insertRiskEvent(RiskEvent riskEvent);

    public int updateRiskEvent(RiskEvent riskEvent);

    /**
     * 处理风控事件
     *
     * @param id 事件ID
     * @param handlerId 处理人ID
     * @param handleResult 处理结果描述
     * @param status 目标状态（RESOLVED/IGNORED）
     * @return 影响行数
     */
    public int handleRiskEvent(Long id, Long handlerId, String handleResult, String status);
}
