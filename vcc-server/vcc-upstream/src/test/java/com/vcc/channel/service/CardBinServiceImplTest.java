package com.vcc.channel.service;

import com.vcc.channel.domain.VccCardBin;
import com.vcc.channel.mapper.VccCardBinMapper;
import com.vcc.channel.service.impl.CardBinServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardBinServiceImplTest
{
    @Mock
    private VccCardBinMapper cardBinMapper;

    @InjectMocks
    private CardBinServiceImpl cardBinService;

    private VccCardBin sampleBin;

    @BeforeEach
    void setUp()
    {
        sampleBin = new VccCardBin();
        sampleBin.setId(1L);
        sampleBin.setBin("426000");
        sampleBin.setCardOrg("VISA");
        sampleBin.setCardType(VccCardBin.CARD_TYPE_PREPAID);
        sampleBin.setChannelId(10L);
        sampleBin.setDefaultLimitAmount(new BigDecimal("5000.00"));
        sampleBin.setStatus(VccCardBin.STATUS_ENABLED);
    }

    @Test
    void getById_returnsCardBin()
    {
        when(cardBinMapper.selectById(1L)).thenReturn(sampleBin);
        VccCardBin result = cardBinService.getById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getBin()).isEqualTo("426000");
    }

    @Test
    void createCardBin_setsDefaultStatus()
    {
        VccCardBin newBin = new VccCardBin();
        newBin.setBin("520000");
        newBin.setCardOrg("MASTERCARD");
        newBin.setCardType(VccCardBin.CARD_TYPE_BUDGET);
        newBin.setChannelId(10L);
        when(cardBinMapper.insert(any())).thenReturn(1);

        int rows = cardBinService.createCardBin(newBin);

        assertThat(rows).isEqualTo(1);
        assertThat(newBin.getStatus()).isEqualTo(VccCardBin.STATUS_ENABLED);
    }

    @Test
    void listByChannelId_delegatesToMapper()
    {
        when(cardBinMapper.selectByChannelId(10L)).thenReturn(List.of(sampleBin));
        List<VccCardBin> result = cardBinService.listByChannelId(10L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChannelId()).isEqualTo(10L);
    }

    @Test
    void enableCardBin_updatesStatus()
    {
        when(cardBinMapper.update(any())).thenReturn(1);
        cardBinService.enableCardBin(1L);
        verify(cardBinMapper).update(argThat(b -> VccCardBin.STATUS_ENABLED.equals(b.getStatus()) && b.getId().equals(1L)));
    }

    @Test
    void disableCardBin_updatesStatus()
    {
        when(cardBinMapper.update(any())).thenReturn(1);
        cardBinService.disableCardBin(1L);
        verify(cardBinMapper).update(argThat(b -> VccCardBin.STATUS_DISABLED.equals(b.getStatus()) && b.getId().equals(1L)));
    }
}
