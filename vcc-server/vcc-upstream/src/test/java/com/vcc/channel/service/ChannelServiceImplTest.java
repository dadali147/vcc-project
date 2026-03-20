package com.vcc.channel.service;

import com.vcc.channel.domain.VccChannel;
import com.vcc.channel.mapper.VccChannelMapper;
import com.vcc.channel.service.impl.ChannelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceImplTest
{
    @Mock
    private VccChannelMapper channelMapper;

    @InjectMocks
    private ChannelServiceImpl channelService;

    private VccChannel sampleChannel;

    @BeforeEach
    void setUp()
    {
        sampleChannel = new VccChannel();
        sampleChannel.setId(1L);
        sampleChannel.setChannelCode("YEEVCC");
        sampleChannel.setChannelName("YeeVCC渠道");
        sampleChannel.setStatus(VccChannel.STATUS_ENABLED);
        sampleChannel.setApiBaseUrl("https://api.yeevcc.com");
    }

    @Test
    void getById_returnsChannel()
    {
        when(channelMapper.selectById(1L)).thenReturn(sampleChannel);
        VccChannel result = channelService.getById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getChannelCode()).isEqualTo("YEEVCC");
    }

    @Test
    void getByChannelCode_returnsChannel()
    {
        when(channelMapper.selectByChannelCode("YEEVCC")).thenReturn(sampleChannel);
        VccChannel result = channelService.getByChannelCode("YEEVCC");
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void createChannel_setsDefaultStatus()
    {
        VccChannel newChannel = new VccChannel();
        newChannel.setChannelCode("TEST");
        newChannel.setChannelName("测试渠道");
        newChannel.setApiBaseUrl("https://api.test.com");
        when(channelMapper.insert(any())).thenReturn(1);

        int rows = channelService.createChannel(newChannel);

        assertThat(rows).isEqualTo(1);
        assertThat(newChannel.getStatus()).isEqualTo(VccChannel.STATUS_ENABLED);
        verify(channelMapper).insert(newChannel);
    }

    @Test
    void enableChannel_updatesStatusToEnabled()
    {
        when(channelMapper.update(any())).thenReturn(1);
        channelService.enableChannel(1L);
        verify(channelMapper).update(argThat(c -> VccChannel.STATUS_ENABLED.equals(c.getStatus()) && c.getId().equals(1L)));
    }

    @Test
    void disableChannel_updatesStatusToDisabled()
    {
        when(channelMapper.update(any())).thenReturn(1);
        channelService.disableChannel(1L);
        verify(channelMapper).update(argThat(c -> VccChannel.STATUS_DISABLED.equals(c.getStatus()) && c.getId().equals(1L)));
    }

    @Test
    void deleteChannel_performsLogicalDelete()
    {
        when(channelMapper.deleteById(1L)).thenReturn(1);
        int rows = channelService.deleteChannel(1L);
        assertThat(rows).isEqualTo(1);
        verify(channelMapper).deleteById(1L);
    }

    @Test
    void listChannels_delegatesToMapper()
    {
        VccChannel query = new VccChannel();
        when(channelMapper.selectList(query)).thenReturn(List.of(sampleChannel));
        List<VccChannel> result = channelService.listChannels(query);
        assertThat(result).hasSize(1);
    }
}
