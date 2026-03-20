package com.vcc.card.service;

import com.vcc.card.domain.CardOperationLog;

import java.util.List;

/**
 * 卡片操作记录 服务层
 */
public interface ICardOperationLogService
{
    /**
     * 根据卡片ID查询操作记录
     */
    List<CardOperationLog> selectByCardId(Long cardId);

    /**
     * 根据商户ID查询操作记录
     */
    List<CardOperationLog> selectByMerchantId(Long merchantId);

    /**
     * 条件查询操作记录
     */
    List<CardOperationLog> selectList(CardOperationLog query);

    /**
     * 记录卡片操作
     */
    void recordOperation(Long merchantId, Long cardId, Long holderId, String operationType,
                         String beforeValue, String afterValue, Long operatorId, String remark);
}
