package com.vcc.channel.service;

import com.vcc.channel.domain.VccMerchantBin;

import java.util.List;

/**
 * 商户BIN分配 Service 接口
 */
public interface IMerchantBinService
{
    VccMerchantBin getById(Long id);

    List<VccMerchantBin> listByMerchantId(Long merchantId);

    List<VccMerchantBin> listMerchantBins(VccMerchantBin query);

    /**
     * 分配BIN给商户（唯一约束：同一商户+BIN只能分配一次）
     */
    int assignBin(Long merchantId, Long binId, Long operatorId, String remark);

    int revokeBin(Long id, Long operatorId);

    int enableAssignment(Long id);

    int disableAssignment(Long id);
}
