package com.vcc.card.mapper;

import com.vcc.card.domain.CardOperationLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡片操作记录 数据层
 */
public interface CardOperationLogMapper
{
    public List<CardOperationLog> selectByCardId(@Param("cardId") Long cardId);

    public List<CardOperationLog> selectByMerchantId(@Param("merchantId") Long merchantId);

    public List<CardOperationLog> selectList(CardOperationLog query);

    public int insert(CardOperationLog log);
}
