package com.vcc.card.mapper;

import com.vcc.card.domain.CardIssueRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 开卡申请单 Mapper
 */
public interface CardIssueRequestMapper
{
    CardIssueRequest selectById(Long id);

    CardIssueRequest selectByBatchNo(String batchNo);

    List<CardIssueRequest> selectList(CardIssueRequest query);

    int insert(CardIssueRequest request);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("successCount") Integer successCount,
                     @Param("failCount") Integer failCount,
                     @Param("completeTime") java.util.Date completeTime);

    int incrementSuccessCount(Long id);

    int incrementFailCount(Long id);
}
