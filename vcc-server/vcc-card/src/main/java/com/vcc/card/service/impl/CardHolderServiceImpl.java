package com.vcc.card.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.mapper.CardHolderMapper;
import com.vcc.card.service.ICardHolderService;
import com.vcc.upstream.adapter.ChannelAwareYeeVccAdapter;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;

/**
 * 持卡人 服务实现
 */
@Service
public class CardHolderServiceImpl implements ICardHolderService
{
    private static final Logger log = LoggerFactory.getLogger(CardHolderServiceImpl.class);

    @Autowired
    private CardHolderMapper cardHolderMapper;

    @Autowired
    private ChannelAwareYeeVccAdapter yeeVccAdapter;

    @Override
    public CardHolder selectCardHolderById(Long id)
    {
        return cardHolderMapper.selectCardHolderById(id);
    }

    @Override
    public List<CardHolder> selectCardHolderList(CardHolder cardHolder)
    {
        return cardHolderMapper.selectCardHolderList(cardHolder);
    }

    @Override
    @Transactional
    public CardHolder addCardHolder(CardHolder cardHolder)
    {
        // 调用上游创建持卡人
        YeeVccRequests.AddCardHolderRequest request = new YeeVccRequests.AddCardHolderRequest();
        request.setFirstName(cardHolder.getFirstName());
        request.setLastName(cardHolder.getLastName());
        request.setEmail(cardHolder.getEmail());
        request.setCountry(cardHolder.getCountry());
        request.setAddress(cardHolder.getAddress());
        request.setPostCode(cardHolder.getPostCode());
        request.setPhone(cardHolder.getMobile());

        YeeVccApiResponse<YeeVccModels.CardHolderData> response = yeeVccAdapter.addCardHolder(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("上游创建持卡人失败: " + response.getMessage());
        }

        YeeVccModels.CardHolderData data = response.getData();
        cardHolder.setUpstreamHolderId(data.getId());
        cardHolder.setStatus(1);

        // 拼接 holderName
        if (cardHolder.getHolderName() == null || cardHolder.getHolderName().isEmpty())
        {
            cardHolder.setHolderName(cardHolder.getFirstName() + " " + cardHolder.getLastName());
        }

        cardHolderMapper.insertCardHolder(cardHolder);
        log.info("持卡人创建成功, id={}, upstreamHolderId={}", cardHolder.getId(), cardHolder.getUpstreamHolderId());
        return cardHolder;
    }

    @Override
    public int updateCardHolder(CardHolder cardHolder)
    {
        return cardHolderMapper.updateCardHolder(cardHolder);
    }

    @Override
    public int deleteCardHolderById(Long id)
    {
        return cardHolderMapper.deleteCardHolderById(id);
    }

    @Override
    public int deleteCardHolderByIds(Long[] ids)
    {
        return cardHolderMapper.deleteCardHolderByIds(ids);
    }
}
