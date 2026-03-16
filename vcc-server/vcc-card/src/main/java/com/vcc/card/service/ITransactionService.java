package com.vcc.card.service;

import java.util.List;
import com.vcc.card.domain.Transaction;

/**
 * 交易记录 服务层
 */
public interface ITransactionService
{
    public Transaction selectTransactionById(Long id);

    public Transaction selectTransactionByTxnId(String txnId);

    public List<Transaction> selectTransactionList(Transaction transaction);

    public int insertTransaction(Transaction transaction);

    public int updateTransaction(Transaction transaction);
}
