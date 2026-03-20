package com.vcc.card.service;

import com.vcc.card.domain.CardIssueRequest;

import java.util.List;

/**
 * 开卡流程 服务层
 */
public interface ICardIssueService
{
    /**
     * 创建批量开卡申请单并立即执行开卡
     */
    CardIssueRequest createAndProcess(Long merchantId, com.vcc.card.dto.CardIssueCreateRequest request);

    /**
     * 查询申请单详情（含明细）
     */
    CardIssueRequest getByBatchNo(Long merchantId, String batchNo);

    /**
     * 列表查询
     */
    List<CardIssueRequest> list(Long merchantId, CardIssueRequest query);

    /**
     * 取消申请单（仅 PENDING 状态可取消）
     */
    int cancel(Long merchantId, Long requestId);
}
