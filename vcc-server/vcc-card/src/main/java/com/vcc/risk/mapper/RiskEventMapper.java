package com.vcc.risk.mapper;

import com.vcc.risk.domain.RiskEvent;

import java.util.List;

/**
 * 风控事件 数据层
 */
public interface RiskEventMapper
{
    public RiskEvent selectRiskEventById(Long id);

    public List<RiskEvent> selectRiskEventList(RiskEvent riskEvent);

    public int insertRiskEvent(RiskEvent riskEvent);

    public int updateRiskEvent(RiskEvent riskEvent);

    public int deleteRiskEventById(Long id);

    public int deleteRiskEventByIds(Long[] ids);
}
