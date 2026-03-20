package com.vcc.finance.mapper;

import com.vcc.finance.domain.LimitHistory;

import java.util.List;

/**
 * 限额调整历史 Mapper
 */
public interface LimitHistoryMapper
{
    LimitHistory selectById(Long id);

    List<LimitHistory> selectByCardId(Long cardId);

    List<LimitHistory> selectList(LimitHistory query);

    int insert(LimitHistory history);
}
