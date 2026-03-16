package com.vcc.card.mapper;

import java.util.List;
import com.vcc.card.domain.CardHolder;

/**
 * 持卡人 数据层
 */
public interface CardHolderMapper
{
    public CardHolder selectCardHolderById(Long id);

    public List<CardHolder> selectCardHolderList(CardHolder cardHolder);

    public int insertCardHolder(CardHolder cardHolder);

    public int updateCardHolder(CardHolder cardHolder);

    public int deleteCardHolderById(Long id);

    public int deleteCardHolderByIds(Long[] ids);
}
