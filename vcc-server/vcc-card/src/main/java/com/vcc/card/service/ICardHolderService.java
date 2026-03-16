package com.vcc.card.service;

import java.util.List;
import com.vcc.card.domain.CardHolder;

/**
 * 持卡人 服务层
 */
public interface ICardHolderService
{
    public CardHolder selectCardHolderById(Long id);

    public List<CardHolder> selectCardHolderList(CardHolder cardHolder);

    public CardHolder addCardHolder(CardHolder cardHolder);

    public int updateCardHolder(CardHolder cardHolder);

    public int deleteCardHolderById(Long id);

    public int deleteCardHolderByIds(Long[] ids);
}
