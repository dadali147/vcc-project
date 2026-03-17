package com.vcc.card.webhook;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.Transaction;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.TransactionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * VCC-014: Webhook 交易处理服务（独立 Bean，确保事务生效）
 */
@Service
public class WebhookTransactionService
{
    private static final Logger log = LoggerFactory.getLogger(WebhookTransactionService.class);

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CardMapper cardMapper;

    /**
     * TRANSACTION：交易通知 → 写入 vcc_transaction 表，更新卡余额（储值卡）
     */
    @Transactional
    public void handleTransaction(Map<String, Object> data)
    {
        String tranId = getString(data, "tranId");
        String cardId = getString(data, "cardId");
        String direction = getString(data, "transactionDirection");
        String status = getString(data, "status");

        // 幂等：检查 txn_id 是否已存在
        if (transactionMapper.selectTransactionByTxnId(tranId) != null)
        {
            log.info("交易记录已存在，跳过: txnId={}", tranId);
            return;
        }

        // 查找本地卡
        Card card = cardMapper.selectCardByUpstreamCardId(cardId);
        if (card == null)
        {
            log.warn("Webhook交易通知：本地未找到卡, upstreamCardId={}", cardId);
            return;
        }

        // 解析交易类型
        String txnType = resolveTxnType(direction, getString(data, "originalTranId"));

        // 写入交易记录
        Transaction txn = new Transaction();
        txn.setTxnId(tranId);
        txn.setCardId(card.getId());
        txn.setUserId(card.getUserId());
        txn.setTxnType(txnType);
        txn.setAmount(getBigDecimal(data, "merchantAmount"));
        txn.setCurrency(getString(data, "merchantCurrency"));
        txn.setMerchantName(getString(data, "merchantName"));
        txn.setMerchantMcc(getString(data, "merchantMcc"));
        txn.setMerchantCountry(getString(data, "merchantCountry"));
        txn.setStatus("SUCCESS".equalsIgnoreCase(status)
                ? Transaction.STATUS_SUCCESS : Transaction.STATUS_FAILED);
        txn.setAuthCode(getString(data, "authCode"));
        txn.setFailReason(getString(data, "failReason"));
        transactionMapper.insertTransaction(txn);

        // 储值卡：更新余额
        if (card.getCardType() != null && card.getCardType() == Card.TYPE_PREPAID)
        {
            BigDecimal closingAmount = getBigDecimal(data, "closingAmount");
            if (closingAmount != null)
            {
                Card updateCard = new Card();
                updateCard.setId(card.getId());
                updateCard.setBalance(closingAmount);
                cardMapper.updateCard(updateCard);
            }
        }

        log.info("交易记录已写入: txnId={}, cardId={}, type={}, amount={}",
                tranId, card.getId(), txnType, txn.getAmount());
    }

    private String resolveTxnType(String direction, String originalTranId)
    {
        if ("C".equalsIgnoreCase(direction))
        {
            return (originalTranId != null && !originalTranId.isEmpty())
                    ? Transaction.TYPE_REVERSE : Transaction.TYPE_REFUND;
        }
        return Transaction.TYPE_AUTH;
    }

    private String getString(Map<String, Object> data, String key)
    {
        Object val = data.get(key);
        return val != null ? val.toString() : null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> data, String key)
    {
        Object val = data.get(key);
        if (val == null) return null;
        try
        {
            return new BigDecimal(val.toString());
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
