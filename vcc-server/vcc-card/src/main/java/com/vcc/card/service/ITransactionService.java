package com.vcc.card.service;

import com.vcc.card.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 交易服务
 */
public interface ITransactionService
{
    Transaction selectTransactionById(Long id);

    Transaction selectTransactionByTxnId(String txnId);

    List<Transaction> selectTransactionList(Transaction query);

    List<Transaction> selectTransactionsByCardId(Long cardId);

    List<Transaction> selectRelatedTransactions(String relatedTxnId);

    /**
     * 处理授权交易（Webhook回调）
     */
    Transaction processAuth(Long cardId, String txnId, BigDecimal amount, String currency,
                            String merchantName, String merchantMcc, String merchantCountry,
                            String authCode);

    /**
     * 处理扣款/清算交易（Webhook回调）
     */
    Transaction processCapture(String authTxnId, String captureTxnId, BigDecimal amount);

    /**
     * 发起退款
     */
    Transaction processRefund(Long merchantId, Long originalTxnId, BigDecimal refundAmount, String reason);

    /**
     * 发起撤销
     */
    Transaction processReverse(Long merchantId, Long originalTxnId, String reason);

    /**
     * 更新交易状态
     */
    int updateTransactionStatus(Long id, Integer status, String failReasonCode, String failReasonText);

    /**
     * 查询交易详情（含手续费关联信息）
     */
    Map<String, Object> getTransactionDetail(Long id, Long merchantId);

    int insertTransaction(Transaction transaction);

    int updateTransaction(Transaction transaction);
}
