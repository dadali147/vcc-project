package com.vcc.finance.service.impl;

import com.vcc.common.exception.ServiceException;
import com.vcc.finance.domain.CardDebt;
import com.vcc.finance.mapper.CardDebtMapper;
import com.vcc.finance.service.IDebtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 欠费管理服务实现
 */
@Service
public class DebtServiceImpl implements IDebtService
{
    private static final Logger log = LoggerFactory.getLogger(DebtServiceImpl.class);

    private final CardDebtMapper debtMapper;

    public DebtServiceImpl(CardDebtMapper debtMapper)
    {
        this.debtMapper = debtMapper;
    }

    @Override
    @Transactional
    public CardDebt checkAndCreateDebt(Long merchantId, Long cardId, BigDecimal balance)
    {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) >= 0)
        {
            return null;
        }
        // 余额为负，产生欠费记录（金额取绝对值）
        BigDecimal debtAmount = balance.negate();
        CardDebt debt = new CardDebt();
        debt.setMerchantId(merchantId);
        debt.setCardId(cardId);
        debt.setDebtAmount(debtAmount);
        debt.setStatus(CardDebt.STATUS_OUTSTANDING);
        debt.setRemark("卡片余额不足，自动产生欠费");
        debtMapper.insert(debt);

        log.info("[DEBT] 欠费记录创建 cardId={}, merchantId={}, debtAmount={}", cardId, merchantId, debtAmount);
        return debt;
    }

    @Override
    @Transactional
    public int settle(Long debtId, BigDecimal settleAmount)
    {
        CardDebt debt = debtMapper.selectById(debtId);
        if (debt == null)
        {
            throw new ServiceException("欠费记录不存在: " + debtId);
        }
        if (CardDebt.STATUS_SETTLED.equals(debt.getStatus()))
        {
            throw new ServiceException("欠费已结清，不可重复结清");
        }
        if (settleAmount == null || settleAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("结清金额必须大于0");
        }
        debt.setSettleAmount(settleAmount);
        int rows = debtMapper.settle(debt);
        log.info("[DEBT] 欠费结清 debtId={}, settleAmount={}", debtId, settleAmount);
        return rows;
    }

    @Override
    public List<CardDebt> listOutstanding(Long merchantId)
    {
        return debtMapper.selectOutstandingByMerchant(merchantId);
    }

    @Override
    public List<CardDebt> listByCard(Long cardId)
    {
        return debtMapper.selectByCardId(cardId);
    }

    @Override
    public List<CardDebt> selectCardDebtList(CardDebt cardDebt)
    {
        return debtMapper.selectCardDebtList(cardDebt);
    }

    @Override
    public CardDebt selectCardDebtById(Long id)
    {
        return debtMapper.selectById(id);
    }

    @Override
    public int deleteCardDebtByIds(Long[] ids)
    {
        return debtMapper.deleteCardDebtByIds(ids);
    }
}
