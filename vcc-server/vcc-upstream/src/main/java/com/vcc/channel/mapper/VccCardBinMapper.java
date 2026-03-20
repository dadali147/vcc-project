package com.vcc.channel.mapper;

import com.vcc.channel.domain.VccCardBin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 卡BIN管理 Mapper
 */
@Mapper
public interface VccCardBinMapper
{
    VccCardBin selectById(@Param("id") Long id);

    List<VccCardBin> selectList(VccCardBin query);

    List<VccCardBin> selectByChannelId(@Param("channelId") Long channelId);

    int insert(VccCardBin cardBin);

    int update(VccCardBin cardBin);

    int deleteById(@Param("id") Long id);
}
