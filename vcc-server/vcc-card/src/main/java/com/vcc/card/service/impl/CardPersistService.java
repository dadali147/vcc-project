package com.vcc.card.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import com.vcc.card.domain.Card;
import com.vcc.card.mapper.CardMapper;
import com.vcc.upstream.dto.YeeVccModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 卡片持久化服务（独立 Bean，确保 @Transactional 代理生效）
 */
@Service
public class CardPersistService
{
    private static final Logger log = LoggerFactory.getLogger(CardPersistService.class);

    @Autowired
    private CardMapper cardMapper;

    @Transactional
    public Card saveCardInTransaction(Long holderId, Long userId, String cardBinId, String currency,
                                       Integer cardType, BigDecimal amount, YeeVccModels.CardData cardData)
    {
        Card card = new Card();
        card.setHolderId(holderId);
        card.setUserId(userId);
        card.setCardNoMask(cardData.getMaskedCardNumber());
        card.setCardBinId(cardBinId);
        card.setCardType(cardType);
        card.setCurrency(currency);
        card.setUpstreamCardId(cardData.getCardId());
        card.setBalance(cardData.getBalance() != null ? cardData.getBalance() : BigDecimal.ZERO);

        if (cardType == Card.TYPE_BUDGET && amount != null)
        {
            card.setBudgetAmount(amount);
        }

        // 判断状态
        String upstreamStatus = cardData.getCardStatus() != null ? cardData.getCardStatus() : cardData.getStatus();
        if ("ACTIVE".equalsIgnoreCase(upstreamStatus))
        {
            card.setStatus(Card.STATUS_ACTIVE);
            card.setActivatedAt(new Date());
        }
        else
        {
            card.setStatus(Card.STATUS_INACTIVE);
        }

        cardMapper.insertCard(card);
        log.info("开卡成功, cardId={}, upstreamCardId={}", card.getId(), card.getUpstreamCardId());
        return card;
    }
}
