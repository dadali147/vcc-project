package com.vcc.finance.mapper;

import com.vcc.finance.domain.CardDebt;

import java.util.List;

/**
 * 欠费记录 Mapper
 */
public interface CardDebtMapper
{
    CardDebt selectById(Long id);

    List<CardDebt> selectCardDebtList(CardDebt cardDebt);

    List<CardDebt> selectByCardId(Long cardId);

    List<CardDebt> selectOutstandingByMerchant(Long merchantId);

    int insert(CardDebt debt);

    int updateCardDebt(CardDebt debt);

    int settle(CardDebt debt);

    int deleteCardDebtById(Long id);

    int deleteCardDebtByIds(Long[] ids);
}
