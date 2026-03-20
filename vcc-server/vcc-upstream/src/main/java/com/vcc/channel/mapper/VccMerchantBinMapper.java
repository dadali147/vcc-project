package com.vcc.channel.mapper;

import com.vcc.channel.domain.VccMerchantBin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户BIN分配 Mapper
 */
@Mapper
public interface VccMerchantBinMapper
{
    VccMerchantBin selectById(@Param("id") Long id);

    VccMerchantBin selectByMerchantAndBin(@Param("merchantId") Long merchantId, @Param("binId") Long binId);

    List<VccMerchantBin> selectByMerchantId(@Param("merchantId") Long merchantId);

    List<VccMerchantBin> selectList(VccMerchantBin query);

    int insert(VccMerchantBin merchantBin);

    int update(VccMerchantBin merchantBin);

    int deleteById(@Param("id") Long id);
}
