package com.vcc.risk.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.common.exception.ServiceException;
import com.vcc.risk.domain.RiskEvent;
import com.vcc.risk.mapper.RiskEventMapper;
import com.vcc.risk.service.IRiskEventService;

/**
 * 风控事件 服务实现
 */
@Service
public class RiskEventServiceImpl implements IRiskEventService
{
    private static final Logger log = LoggerFactory.getLogger(RiskEventServiceImpl.class);

    @Autowired
    private RiskEventMapper riskEventMapper;

    @Override
    public RiskEvent selectRiskEventById(Long id)
    {
        return riskEventMapper.selectRiskEventById(id);
    }

    @Override
    public List<RiskEvent> selectRiskEventList(RiskEvent riskEvent)
    {
        return riskEventMapper.selectRiskEventList(riskEvent);
    }

    @Override
    @Transactional
    public int insertRiskEvent(RiskEvent riskEvent)
    {
        return riskEventMapper.insertRiskEvent(riskEvent);
    }

    @Override
    @Transactional
    public int updateRiskEvent(RiskEvent riskEvent)
    {
        return riskEventMapper.updateRiskEvent(riskEvent);
    }

    @Override
    @Transactional
    public int handleRiskEvent(Long id, Long handlerId, String handleResult, String status)
    {
        RiskEvent event = riskEventMapper.selectRiskEventById(id);
        if (event == null)
        {
            throw new ServiceException("风控事件不存在");
        }
        // 校验状态流转
        if (RiskEvent.STATUS_RESOLVED.equals(event.getStatus()) || RiskEvent.STATUS_IGNORED.equals(event.getStatus()))
        {
            throw new ServiceException("该风控事件已处理，不可重复操作");
        }
        if (!RiskEvent.STATUS_RESOLVED.equals(status) && !RiskEvent.STATUS_IGNORED.equals(status))
        {
            throw new ServiceException("目标状态不合法，仅支持RESOLVED或IGNORED");
        }

        RiskEvent update = new RiskEvent();
        update.setId(id);
        update.setHandlerId(handlerId);
        update.setHandleTime(new Date());
        update.setHandleResult(handleResult);
        update.setStatus(status);

        log.info("处理风控事件, eventId={}, handlerId={}, status={}, result={}", id, handlerId, status, handleResult);
        return riskEventMapper.updateRiskEvent(update);
    }
}
