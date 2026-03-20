package com.vcc.channel.service;

import com.vcc.channel.domain.VccCardBin;

import java.util.List;

/**
 * 卡BIN管理 Service 接口
 */
public interface ICardBinService
{
    VccCardBin getById(Long id);

    List<VccCardBin> listCardBins(VccCardBin query);

    List<VccCardBin> listByChannelId(Long channelId);

    int createCardBin(VccCardBin cardBin);

    int updateCardBin(VccCardBin cardBin);

    int enableCardBin(Long id);

    int disableCardBin(Long id);

    int deleteCardBin(Long id);
}
