package com.vcc.card.mapper;

import com.vcc.card.domain.FeeConfig;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费率配置 数据层
 */
public interface FeeConfigMapper
{
    public FeeConfig selectFeeConfigById(Long id);

    public List<FeeConfig> selectFeeConfigList(FeeConfig feeConfig);

    public List<FeeConfig> selectFeeConfigByFeeType(@Param("feeType") String feeType);

    public FeeConfig selectEffectiveFeeConfig(@Param("merchantId") Long merchantId,
                                              @Param("feeType") String feeType,
                                              @Param("conditionType") String conditionType,
                                              @Param("amount") BigDecimal amount);

    public int insertFeeConfig(FeeConfig feeConfig);

    public int updateFeeConfig(FeeConfig feeConfig);

    public int deleteFeeConfigById(Long id);

    public int deleteFeeConfigByIds(Long[] ids);
}
