package com.vcc.channel.service;

import com.vcc.channel.domain.VccChannel;

import java.util.List;

/**
 * 渠道管理 Service 接口
 */
public interface IChannelService
{
    VccChannel getById(Long id);

    VccChannel getByChannelCode(String channelCode);

    List<VccChannel> listChannels(VccChannel query);

    int createChannel(VccChannel channel);

    int updateChannel(VccChannel channel);

    int enableChannel(Long id);

    int disableChannel(Long id);

    int deleteChannel(Long id);
}
