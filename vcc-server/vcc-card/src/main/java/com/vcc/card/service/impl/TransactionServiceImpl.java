package com.vcc.card.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.domain.Transaction;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.FeeConfigMapper;
import com.vcc.card.mapper.TransactionMapper;
import com.vcc.card.service.ITransactionService;
import com.vcc.common.exception.ServiceException;

/**
 * 交易记录 服务实现
 */
@Service
public class TransactionServiceImpl implements ITransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private FeeConfigMapper feeConfigMapper;

    @Override
    public Transaction selectTransactionById(Long id) {
        return transactionMapper.selectTransactionById(id);
    }

    @Override
    public Transaction selectTransactionByTxnId(String txnId) {
        return transactionMapper.selectTransactionByTxnId(txnId);
    }

    @Override
    public List<Transaction> selectTransactionList(Transaction query) {
        return transactionMapper.selectTransactionList(query);
    }

    @Override
    public List<Transaction> selectTransactionsByCardId(Long cardId) {
        return transactionMapper.selectTransactionsByCardId(cardId);
    }

    @Override
    public List<Transaction> selectRelatedTransactions(String relatedTxnId) {
        return transactionMapper.selectRelatedTransactions(relatedTxnId);
    }

    @Override
    @Transactional
    public Transaction processAuth(Long cardId, String txnId, BigDecimal amount, String currency,
                                   String merchantName, String merchantMcc, String merchantCountry,
                                   String authCode) {
        log.info("处理授权交易: cardId={}, txnId={}, amount={}, currency={}", cardId, txnId, amount, currency);

        // 查询卡片信息
        Card card = cardMapper.selectCardById(cardId);
        if (card == null) {
            throw new ServiceException("卡片不存在: " + cardId);
        }
        if (card.getStatus() != Card.STATUS_ACTIVE) {
            throw new ServiceException("卡片状态异常，无法进行交易: cardId=" + cardId + ", status=" + card.getStatus());
        }

        // 检查余额是否充足
        if (card.getBalance() == null || card.getBalance().compareTo(amount) < 0) {
            throw new ServiceException("卡片余额不足: cardId=" + cardId + ", balance=" + card.getBalance() + ", amount=" + amount);
        }

        // 创建授权交易记录
        Transaction transaction = new Transaction();
        transaction.setTxnId(txnId);
        transaction.setCardId(cardId);
        transaction.setUserId(card.getUserId());
        transaction.setMerchantId(card.getHolderId());
        transaction.setHolderId(card.getHolderId());
        transaction.setTxnType(Transaction.TYPE_AUTH);
        transaction.setTxnCategory(Transaction.CATEGORY_PURCHASE);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setMerchantName(merchantName);
        transaction.setMerchantMcc(merchantMcc);
        transaction.setMerchantCountry(merchantCountry);
        transaction.setAuthCode(authCode);
        transaction.setStatus(Transaction.STATUS_SUCCESS);
        transaction.setDisplayMode(Transaction.DISPLAY_NORMAL);
        transaction.setTxnTime(new Date());

        // 扣减卡片余额
        Card cardUpdate = new Card();
        cardUpdate.setId(cardId);
        cardUpdate.setBalance(card.getBalance().subtract(amount));
        cardMapper.updateCard(cardUpdate);

        transactionMapper.insertTransaction(transaction);
        log.info("授权交易处理完成: txnId={}, cardId={}, amount={}", txnId, cardId, amount);
        return transaction;
    }

    @Override
    @Transactional
    public Transaction processCapture(String authTxnId, String captureTxnId, BigDecimal amount) {
        log.info("处理扣款交易: authTxnId={}, captureTxnId={}, amount={}", authTxnId, captureTxnId, amount);

        // 查询原始授权交易
        Transaction authTxn = transactionMapper.selectTransactionByTxnId(authTxnId);
        if (authTxn == null) {
            throw new ServiceException("原始授权交易不存在: " + authTxnId);
        }
        if (!Transaction.TYPE_AUTH.equals(authTxn.getTxnType())) {
            throw new ServiceException("关联交易不是授权交易: " + authTxnId);
        }

        // 创建扣款交易记录
        Transaction transaction = new Transaction();
        transaction.setTxnId(captureTxnId);
        transaction.setCardId(authTxn.getCardId());
        transaction.setUserId(authTxn.getUserId());
        transaction.setMerchantId(authTxn.getMerchantId());
        transaction.setHolderId(authTxn.getHolderId());
        transaction.setTxnType(Transaction.TYPE_CAPTURE);
        transaction.setTxnCategory(Transaction.CATEGORY_PURCHASE);
        transaction.setRelatedTxnId(authTxnId);
        transaction.setAmount(amount);
        transaction.setCurrency(authTxn.getCurrency());
        transaction.setMerchantName(authTxn.getMerchantName());
        transaction.setMerchantMcc(authTxn.getMerchantMcc());
        transaction.setMerchantCountry(authTxn.getMerchantCountry());
        transaction.setAuthCode(authTxn.getAuthCode());
        transaction.setStatus(Transaction.STATUS_SUCCESS);
        transaction.setDisplayMode(Transaction.DISPLAY_NORMAL);
        transaction.setTxnTime(new Date());

        // 如果扣款金额与授权金额不同，调整卡片余额
        BigDecimal authAmount = authTxn.getAmount();
        int cmp = amount.compareTo(authAmount);
        if (cmp != 0) {
            Card card = cardMapper.selectCardById(authTxn.getCardId());
            if (card == null) {
                throw new ServiceException("卡片不存在: " + authTxn.getCardId());
            }
            // 差额 = 授权金额 - 扣款金额（正数表示需要退回，负数表示需要额外扣减）
            BigDecimal diff = authAmount.subtract(amount);
            Card cardUpdate = new Card();
            cardUpdate.setId(card.getId());
            cardUpdate.setBalance(card.getBalance().add(diff));
            cardMapper.updateCard(cardUpdate);
            log.info("扣款金额与授权金额不同，调整余额: authAmount={}, captureAmount={}, diff={}", authAmount, amount, diff);
        }

        transactionMapper.insertTransaction(transaction);
        log.info("扣款交易处理完成: captureTxnId={}, authTxnId={}, amount={}", captureTxnId, authTxnId, amount);
        return transaction;
    }

    @Override
    @Transactional
    public Transaction processRefund(Long merchantId, Long originalTxnId, BigDecimal refundAmount, String reason) {
        log.info("处理退款: merchantId={}, originalTxnId={}, refundAmount={}", merchantId, originalTxnId, refundAmount);

        // 查询原始交易
        Transaction originalTxn = transactionMapper.selectTransactionById(originalTxnId);
        if (originalTxn == null) {
            throw new ServiceException("原始交易不存在: " + originalTxnId);
        }
        if (!originalTxn.getMerchantId().equals(merchantId)) {
            throw new ServiceException("无权操作此交易，商户ID不匹配");
        }
        if (originalTxn.getStatus() != Transaction.STATUS_SUCCESS) {
            throw new ServiceException("原始交易状态异常，无法退款: status=" + originalTxn.getStatus());
        }
        if (refundAmount.compareTo(originalTxn.getAmount()) > 0) {
            throw new ServiceException("退款金额不能超过原始交易金额: refundAmount=" + refundAmount + ", originalAmount=" + originalTxn.getAmount());
        }

        // 创建退款交易记录
        Transaction refundTxn = new Transaction();
        refundTxn.setTxnId(generateTxnId());
        refundTxn.setCardId(originalTxn.getCardId());
        refundTxn.setUserId(originalTxn.getUserId());
        refundTxn.setMerchantId(merchantId);
        refundTxn.setHolderId(originalTxn.getHolderId());
        refundTxn.setTxnType(Transaction.TYPE_REFUND);
        refundTxn.setTxnCategory(Transaction.CATEGORY_REFUND);
        refundTxn.setRelatedTxnId(originalTxn.getTxnId());
        refundTxn.setAmount(refundAmount);
        refundTxn.setCurrency(originalTxn.getCurrency());
        refundTxn.setMerchantName(originalTxn.getMerchantName());
        refundTxn.setMerchantMcc(originalTxn.getMerchantMcc());
        refundTxn.setMerchantCountry(originalTxn.getMerchantCountry());
        refundTxn.setStatus(Transaction.STATUS_SUCCESS);
        refundTxn.setDisplayMode(Transaction.DISPLAY_NORMAL);
        refundTxn.setTxnTime(new Date());
        if (reason != null) {
            refundTxn.setFailReason(reason);
        }

        // 退款金额加回卡片余额
        Card card = cardMapper.selectCardById(originalTxn.getCardId());
        if (card == null) {
            throw new ServiceException("卡片不存在: " + originalTxn.getCardId());
        }
        Card cardUpdate = new Card();
        cardUpdate.setId(card.getId());
        cardUpdate.setBalance(card.getBalance().add(refundAmount));
        cardMapper.updateCard(cardUpdate);

        transactionMapper.insertTransaction(refundTxn);

        // 计算并记录手续费
        recordFeeTransaction(merchantId, refundTxn, refundAmount);

        log.info("退款处理完成: refundTxnId={}, originalTxnId={}, refundAmount={}", refundTxn.getTxnId(), originalTxnId, refundAmount);
        return refundTxn;
    }

    @Override
    @Transactional
    public Transaction processReverse(Long merchantId, Long originalTxnId, String reason) {
        log.info("处理撤销: merchantId={}, originalTxnId={}", merchantId, originalTxnId);

        // 查询原始交易
        Transaction originalTxn = transactionMapper.selectTransactionById(originalTxnId);
        if (originalTxn == null) {
            throw new ServiceException("原始交易不存在: " + originalTxnId);
        }
        if (!originalTxn.getMerchantId().equals(merchantId)) {
            throw new ServiceException("无权操作此交易，商户ID不匹配");
        }
        if (originalTxn.getStatus() != Transaction.STATUS_SUCCESS) {
            throw new ServiceException("原始交易状态异常，无法撤销: status=" + originalTxn.getStatus());
        }

        // 创建撤销交易记录（全额撤销）
        BigDecimal reverseAmount = originalTxn.getAmount();
        Transaction reverseTxn = new Transaction();
        reverseTxn.setTxnId(generateTxnId());
        reverseTxn.setCardId(originalTxn.getCardId());
        reverseTxn.setUserId(originalTxn.getUserId());
        reverseTxn.setMerchantId(merchantId);
        reverseTxn.setHolderId(originalTxn.getHolderId());
        reverseTxn.setTxnType(Transaction.TYPE_REVERSE);
        reverseTxn.setTxnCategory(Transaction.CATEGORY_REVERSE);
        reverseTxn.setRelatedTxnId(originalTxn.getTxnId());
        reverseTxn.setAmount(reverseAmount);
        reverseTxn.setCurrency(originalTxn.getCurrency());
        reverseTxn.setMerchantName(originalTxn.getMerchantName());
        reverseTxn.setMerchantMcc(originalTxn.getMerchantMcc());
        reverseTxn.setMerchantCountry(originalTxn.getMerchantCountry());
        reverseTxn.setStatus(Transaction.STATUS_SUCCESS);
        reverseTxn.setDisplayMode(Transaction.DISPLAY_NORMAL);
        reverseTxn.setTxnTime(new Date());
        if (reason != null) {
            reverseTxn.setFailReason(reason);
        }

        // 撤销金额加回卡片余额
        Card card = cardMapper.selectCardById(originalTxn.getCardId());
        if (card == null) {
            throw new ServiceException("卡片不存在: " + originalTxn.getCardId());
        }
        Card cardUpdate = new Card();
        cardUpdate.setId(card.getId());
        cardUpdate.setBalance(card.getBalance().add(reverseAmount));
        cardMapper.updateCard(cardUpdate);

        transactionMapper.insertTransaction(reverseTxn);

        log.info("撤销处理完成: reverseTxnId={}, originalTxnId={}, amount={}", reverseTxn.getTxnId(), originalTxnId, reverseAmount);
        return reverseTxn;
    }

    @Override
    @Transactional
    public int updateTransactionStatus(Long id, Integer status, String failReasonCode, String failReasonText) {
        log.info("更新交易状态: id={}, status={}, failReasonCode={}", id, status, failReasonCode);
        return transactionMapper.updateTransactionStatus(id, status, failReasonCode, failReasonText);
    }

    @Override
    public Map<String, Object> getTransactionDetail(Long id, Long merchantId) {
        Transaction transaction = transactionMapper.selectTransactionById(id);
        if (transaction == null) {
            throw new ServiceException("交易不存在: " + id);
        }
        if (merchantId != null && !transaction.getMerchantId().equals(merchantId)) {
            throw new ServiceException("无权查看此交易");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("transaction", transaction);

        // 查询关联交易
        if (transaction.getTxnId() != null) {
            List<Transaction> relatedTransactions = transactionMapper.selectRelatedTransactions(transaction.getTxnId());
            detail.put("relatedTransactions", relatedTransactions);
        }

        // 查询适用的费率配置
        FeeConfig feeConfig = feeConfigMapper.selectEffectiveFeeConfig(
                transaction.getMerchantId(), FeeConfig.FEE_TYPE_TXN, null, transaction.getAmount());
        detail.put("feeConfig", feeConfig);

        return detail;
    }

    @Override
    public int insertTransaction(Transaction transaction) {
        return transactionMapper.insertTransaction(transaction);
    }

    @Override
    public int updateTransaction(Transaction transaction) {
        return transactionMapper.updateTransaction(transaction);
    }

    /**
     * 计算并记录手续费交易
     */
    private void recordFeeTransaction(Long merchantId, Transaction parentTxn, BigDecimal txnAmount) {
        FeeConfig feeConfig = feeConfigMapper.selectEffectiveFeeConfig(merchantId, FeeConfig.FEE_TYPE_TXN, null, txnAmount);
        if (feeConfig == null) {
            log.info("未找到适用的手续费配置: merchantId={}, amount={}", merchantId, txnAmount);
            return;
        }

        BigDecimal fee = calculateFee(feeConfig, txnAmount);
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("计算手续费为零，跳过记录: merchantId={}, amount={}", merchantId, txnAmount);
            return;
        }

        Transaction feeTxn = new Transaction();
        feeTxn.setTxnId(generateTxnId());
        feeTxn.setCardId(parentTxn.getCardId());
        feeTxn.setUserId(parentTxn.getUserId());
        feeTxn.setMerchantId(merchantId);
        feeTxn.setHolderId(parentTxn.getHolderId());
        feeTxn.setTxnType(parentTxn.getTxnType());
        feeTxn.setTxnCategory(Transaction.CATEGORY_FEE);
        feeTxn.setRelatedTxnId(parentTxn.getTxnId());
        feeTxn.setAmount(fee);
        feeTxn.setCurrency(parentTxn.getCurrency());
        feeTxn.setStatus(Transaction.STATUS_SUCCESS);
        feeTxn.setDisplayMode(Transaction.DISPLAY_HIDDEN);
        feeTxn.setTxnTime(new Date());

        transactionMapper.insertTransaction(feeTxn);
        log.info("手续费交易已记录: feeTxnId={}, parentTxnId={}, fee={}", feeTxn.getTxnId(), parentTxn.getTxnId(), fee);
    }

    /**
     * 根据费率配置计算手续费
     */
    private BigDecimal calculateFee(FeeConfig feeConfig, BigDecimal txnAmount) {
        BigDecimal fee = BigDecimal.ZERO;

        // 按费率计算
        if (feeConfig.getRate() != null && feeConfig.getRate().compareTo(BigDecimal.ZERO) > 0) {
            fee = txnAmount.multiply(feeConfig.getRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }

        // 加上固定金额
        if (feeConfig.getFixedAmount() != null && feeConfig.getFixedAmount().compareTo(BigDecimal.ZERO) > 0) {
            fee = fee.add(feeConfig.getFixedAmount());
        }

        // 限制最低手续费
        if (feeConfig.getMinFee() != null && fee.compareTo(feeConfig.getMinFee()) < 0) {
            fee = feeConfig.getMinFee();
        }

        // 限制最高手续费
        if (feeConfig.getMaxFee() != null && fee.compareTo(feeConfig.getMaxFee()) > 0) {
            fee = feeConfig.getMaxFee();
        }

        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成交易ID
     */
    private String generateTxnId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
