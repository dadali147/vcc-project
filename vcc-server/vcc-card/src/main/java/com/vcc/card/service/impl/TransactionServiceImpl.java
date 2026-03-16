package com.vcc.card.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vcc.card.domain.Transaction;
import com.vcc.card.mapper.TransactionMapper;
import com.vcc.card.service.ITransactionService;

/**
 * 交易记录 服务实现
 */
@Service
public class TransactionServiceImpl implements ITransactionService
{
    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public Transaction selectTransactionById(Long id)
    {
        return transactionMapper.selectTransactionById(id);
    }

    @Override
    public Transaction selectTransactionByTxnId(String txnId)
    {
        return transactionMapper.selectTransactionByTxnId(txnId);
    }

    @Override
    public List<Transaction> selectTransactionList(Transaction transaction)
    {
        return transactionMapper.selectTransactionList(transaction);
    }

    @Override
    public int insertTransaction(Transaction transaction)
    {
        return transactionMapper.insertTransaction(transaction);
    }

    @Override
    public int updateTransaction(Transaction transaction)
    {
        return transactionMapper.updateTransaction(transaction);
    }
}
