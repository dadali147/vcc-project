package com.vcc.channel.service;

import com.vcc.channel.domain.VccMerchantBin;
import com.vcc.channel.mapper.VccMerchantBinMapper;
import com.vcc.channel.service.impl.MerchantBinServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantBinServiceImplTest
{
    @Mock
    private VccMerchantBinMapper merchantBinMapper;

    @InjectMocks
    private MerchantBinServiceImpl merchantBinService;

    @Test
    void assignBin_success()
    {
        when(merchantBinMapper.selectByMerchantAndBin(100L, 1L)).thenReturn(null);
        when(merchantBinMapper.insert(any())).thenReturn(1);

        int rows = merchantBinService.assignBin(100L, 1L, 999L, "初始分配");

        assertThat(rows).isEqualTo(1);
        verify(merchantBinMapper).insert(argThat(mb ->
                mb.getMerchantId().equals(100L)
                && mb.getBinId().equals(1L)
                && VccMerchantBin.STATUS_ASSIGNED.equals(mb.getStatus())
                && mb.getAssignedBy().equals(999L)));
    }

    @Test
    void assignBin_throwsWhenAlreadyAssigned()
    {
        VccMerchantBin existing = new VccMerchantBin();
        existing.setMerchantId(100L);
        existing.setBinId(1L);
        when(merchantBinMapper.selectByMerchantAndBin(100L, 1L)).thenReturn(existing);

        assertThatThrownBy(() -> merchantBinService.assignBin(100L, 1L, 999L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已分配");
    }

    @Test
    void revokeBin_setsDisabledStatus()
    {
        when(merchantBinMapper.update(any())).thenReturn(1);
        merchantBinService.revokeBin(5L, 999L);
        verify(merchantBinMapper).update(argThat(mb ->
                VccMerchantBin.STATUS_DISABLED.equals(mb.getStatus()) && mb.getId().equals(5L)));
    }

    @Test
    void listByMerchantId_delegatesToMapper()
    {
        VccMerchantBin mb = new VccMerchantBin();
        mb.setMerchantId(100L);
        when(merchantBinMapper.selectByMerchantId(100L)).thenReturn(List.of(mb));

        List<VccMerchantBin> result = merchantBinService.listByMerchantId(100L);
        assertThat(result).hasSize(1);
    }

    @Test
    void enableAssignment_setsAssignedStatus()
    {
        when(merchantBinMapper.update(any())).thenReturn(1);
        merchantBinService.enableAssignment(5L);
        verify(merchantBinMapper).update(argThat(mb ->
                VccMerchantBin.STATUS_ASSIGNED.equals(mb.getStatus()) && mb.getId().equals(5L)));
    }
}
