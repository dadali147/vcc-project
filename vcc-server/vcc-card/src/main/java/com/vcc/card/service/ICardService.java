package com.vcc.card.service;

import java.util.List;
import java.util.Map;
import com.vcc.card.domain.Card;

/**
 * 卡片 服务层
 */
public interface ICardService
{
    public Card selectCardById(Long id);

    public List<Card> selectCardList(Card card);

    /**
     * 管理端：查询所有卡片（支持按 userName 模糊搜索）
     */
    public List<Card> selectCardListAdmin(Card card);

    /**
     * 管理端：卡产品统计
     */
    public Map<String, Object> getCardStats();

    /**
     * 查询卡片交易记录（管理端）
     */
    public List<Map<String, Object>> selectTransactionListAdmin(Map<String, Object> params);

    /**
     * 查询今日交易统计
     */
    public Map<String, Object> getTodayTransactionStats();

    /**
     * 开卡（调用上游 + 异步轮询结果）
     */
    public Card openCard(Long holderId, String cardBinId, String currency, Integer cardType,
                         java.math.BigDecimal amount, Long userId);

    /**
     * 激活卡片
     */
    public int activateCard(Long cardId, Long userId);

    /**
     * 冻结卡片
     */
    public int freezeCard(Long cardId, Long userId);

    /**
     * 解冻卡片
     */
    public int unfreezeCard(Long cardId, Long userId);

    /**
     * 销卡
     */
    public int cancelCard(Long cardId, Long userId);

    /**
     * 查询卡片三要素（不落库）
     */
    public Map<String, String> getCardKeyInfo(Long cardId, Long userId);
}
