package com.vcc.card.mapper;

import java.util.List;
import com.vcc.card.domain.Card;

/**
 * 卡片 数据层
 */
public interface CardMapper
{
    public Card selectCardById(Long id);

    public Card selectCardByUpstreamCardId(String upstreamCardId);

    public List<Card> selectCardList(Card card);

    public int insertCard(Card card);

    public int updateCard(Card card);

    public int deleteCardById(Long id);

    public int deleteCardByIds(Long[] ids);
}
