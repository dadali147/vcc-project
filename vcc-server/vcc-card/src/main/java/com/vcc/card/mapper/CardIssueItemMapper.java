package com.vcc.card.mapper;

import com.vcc.card.domain.CardIssueItem;

import java.util.List;

/**
 * 开卡申请明细 Mapper
 */
public interface CardIssueItemMapper
{
    CardIssueItem selectById(Long id);

    List<CardIssueItem> selectByRequestId(Long requestId);

    int insert(CardIssueItem item);

    int batchInsert(List<CardIssueItem> items);

    int updateStatus(CardIssueItem item);
}
