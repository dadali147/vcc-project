package com.vcc.channel.service.impl;

import com.vcc.channel.domain.VccCardBin;
import com.vcc.channel.mapper.VccCardBinMapper;
import com.vcc.channel.service.ICardBinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 卡BIN管理 Service 实现
 */
@Service
public class CardBinServiceImpl implements ICardBinService
{
    private static final Logger log = LoggerFactory.getLogger(CardBinServiceImpl.class);

    @Autowired
    private VccCardBinMapper cardBinMapper;

    @Override
    public VccCardBin getById(Long id)
    {
        return cardBinMapper.selectById(id);
    }

    @Override
    public List<VccCardBin> listCardBins(VccCardBin query)
    {
        return cardBinMapper.selectList(query);
    }

    @Override
    public List<VccCardBin> listByChannelId(Long channelId)
    {
        return cardBinMapper.selectByChannelId(channelId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createCardBin(VccCardBin cardBin)
    {
        if (cardBin.getStatus() == null)
        {
            cardBin.setStatus(VccCardBin.STATUS_ENABLED);
        }
        log.info("[AUDIT] 创建卡BIN: bin={}, channelId={}", cardBin.getBin(), cardBin.getChannelId());
        return cardBinMapper.insert(cardBin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCardBin(VccCardBin cardBin)
    {
        log.info("[AUDIT] 更新卡BIN: id={}", cardBin.getId());
        return cardBinMapper.update(cardBin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableCardBin(Long id)
    {
        log.info("[AUDIT] 启用卡BIN: id={}", id);
        VccCardBin update = new VccCardBin();
        update.setId(id);
        update.setStatus(VccCardBin.STATUS_ENABLED);
        return cardBinMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableCardBin(Long id)
    {
        log.info("[AUDIT] 停用卡BIN: id={}", id);
        VccCardBin update = new VccCardBin();
        update.setId(id);
        update.setStatus(VccCardBin.STATUS_DISABLED);
        return cardBinMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCardBin(Long id)
    {
        log.info("[AUDIT] 删除卡BIN: id={}", id);
        return cardBinMapper.deleteById(id);
    }
}
