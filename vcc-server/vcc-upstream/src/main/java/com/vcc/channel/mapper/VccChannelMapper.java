package com.vcc.channel.mapper;

import com.vcc.channel.domain.VccChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 渠道管理 Mapper
 */
@Mapper
public interface VccChannelMapper
{
    VccChannel selectById(@Param("id") Long id);

    VccChannel selectByChannelCode(@Param("channelCode") String channelCode);

    List<VccChannel> selectList(VccChannel query);

    int insert(VccChannel channel);

    int update(VccChannel channel);

    int deleteById(@Param("id") Long id);
}
