package com.vcc.card.service.impl;

import com.vcc.card.domain.CardOperationLog;
import com.vcc.card.mapper.CardOperationLogMapper;
import com.vcc.card.service.ICardOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CardOperationLogServiceImpl implements ICardOperationLogService
{
    @Autowired
    private CardOperationLogMapper operationLogMapper;

    @Override
    public List<CardOperationLog> selectByCardId(Long cardId)
    {
        return operationLogMapper.selectByCardId(cardId);
    }

    @Override
    public List<CardOperationLog> selectByMerchantId(Long merchantId)
    {
        return operationLogMapper.selectByMerchantId(merchantId);
    }

    @Override
    public List<CardOperationLog> selectList(CardOperationLog query)
    {
        return operationLogMapper.selectList(query);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOperation(Long merchantId, Long cardId, Long holderId, String operationType,
                                String beforeValue, String afterValue, Long operatorId, String remark)
    {
        CardOperationLog log = new CardOperationLog();
        log.setMerchantId(merchantId);
        log.setCardId(cardId);
        log.setHolderId(holderId);
        log.setOperationType(operationType);
        log.setBeforeValue(beforeValue);
        log.setAfterValue(afterValue);
        log.setOperatorId(operatorId);
        log.setOperateTime(new Date());
        log.setRemark(remark);
        log.setDelFlag("0");
        operationLogMapper.insert(log);
    }
}
