package com.vcc.finance.service;

import com.vcc.finance.domain.CardDebt;

import java.math.BigDecimal;
import java.util.List;

/**
 * 欠费管理 服务层
 * PREPAID 卡余额负数时自动产生欠费记录
 */
public interface IDebtService
{
    /**
     * 检查卡片余额，若为负则自动创建欠费记录
     *
     * @param merchantId 商户ID
     * @param cardId     卡片ID
     * @param balance    当前余额（负数触发）
     * @return 创建的欠费记录（若未触发则返回 null）
     */
    CardDebt checkAndCreateDebt(Long merchantId, Long cardId, BigDecimal balance);

    /**
     * 结清欠费（充值到账后调用）
     *
     * @param debtId      欠费记录ID
     * @param settleAmount 结清金额
     */
    int settle(Long debtId, BigDecimal settleAmount);

    /**
     * 查询商户未结清欠费列表
     */
    List<CardDebt> listOutstanding(Long merchantId);

    /**
     * 查询卡片欠费历史
     */
    List<CardDebt> listByCard(Long cardId);

    /**
     * 分页查询欠费列表
     */
    List<CardDebt> selectCardDebtList(CardDebt cardDebt);

    /**
     * 根据ID查询欠费记录
     */
    CardDebt selectCardDebtById(Long id);

    /**
     * 删除欠费记录（逻辑删除）
     */
    int deleteCardDebtByIds(Long[] ids);
}
