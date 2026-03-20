package com.vcc.card.mapper;

import java.util.List;
import com.vcc.card.domain.Transaction;
import org.apache.ibatis.annotations.Param;

/**
 * 交易记录 数据层
 */
public interface TransactionMapper
{
    public Transaction selectTransactionById(Long id);

    public Transaction selectTransactionByTxnId(String txnId);

    public List<Transaction> selectTransactionList(Transaction transaction);

    public List<Transaction> selectTransactionsByCardId(Long cardId);

    public List<Transaction> selectTransactionsByMerchantId(Long merchantId);

    public List<Transaction> selectRelatedTransactions(@Param("relatedTxnId") String relatedTxnId);

    public int insertTransaction(Transaction transaction);

    public int updateTransaction(Transaction transaction);

    public int updateTransactionStatus(@Param("id") Long id, @Param("status") Integer status,
                                        @Param("failReasonCode") String failReasonCode,
                                        @Param("failReasonText") String failReasonText);

    public int deleteTransactionById(Long id);
}
