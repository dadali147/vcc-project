package com.vcc.channel.service.impl;

import com.vcc.channel.domain.VccChannel;
import com.vcc.channel.mapper.VccChannelMapper;
import com.vcc.channel.service.IChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 渠道管理 Service 实现
 */
@Service
public class ChannelServiceImpl implements IChannelService
{
    private static final Logger log = LoggerFactory.getLogger(ChannelServiceImpl.class);

    @Autowired
    private VccChannelMapper channelMapper;

    @Override
    public VccChannel getById(Long id)
    {
        return channelMapper.selectById(id);
    }

    @Override
    public VccChannel getByChannelCode(String channelCode)
    {
        return channelMapper.selectByChannelCode(channelCode);
    }

    @Override
    public List<VccChannel> listChannels(VccChannel query)
    {
        return channelMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createChannel(VccChannel channel)
    {
        if (channel.getStatus() == null)
        {
            channel.setStatus(VccChannel.STATUS_ENABLED);
        }
        log.info("[AUDIT] 创建渠道: channelCode={}", channel.getChannelCode());
        return channelMapper.insert(channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateChannel(VccChannel channel)
    {
        log.info("[AUDIT] 更新渠道: id={}", channel.getId());
        return channelMapper.update(channel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableChannel(Long id)
    {
        log.info("[AUDIT] 启用渠道: id={}", id);
        VccChannel update = new VccChannel();
        update.setId(id);
        update.setStatus(VccChannel.STATUS_ENABLED);
        return channelMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableChannel(Long id)
    {
        log.info("[AUDIT] 停用渠道: id={}", id);
        VccChannel update = new VccChannel();
        update.setId(id);
        update.setStatus(VccChannel.STATUS_DISABLED);
        return channelMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteChannel(Long id)
    {
        log.info("[AUDIT] 删除渠道: id={}", id);
        return channelMapper.deleteById(id);
    }
}
