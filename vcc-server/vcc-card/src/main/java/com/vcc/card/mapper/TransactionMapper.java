package com.vcc.card.mapper;

import java.util.List;
import com.vcc.card.domain.Transaction;

/**
 * 交易记录 数据层
 */
public interface TransactionMapper
{
    public Transaction selectTransactionById(Long id);

    public Transaction selectTransactionByTxnId(String txnId);

    public List<Transaction> selectTransactionList(Transaction transaction);

    public int insertTransaction(Transaction transaction);

    public int updateTransaction(Transaction transaction);

    public int deleteTransactionById(Long id);
}
