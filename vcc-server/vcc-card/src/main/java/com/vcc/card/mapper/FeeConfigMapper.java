package com.vcc.card.mapper;

import java.util.List;
import com.vcc.card.domain.FeeConfig;
import org.apache.ibatis.annotations.Param;

/**
 * 费率配置 数据层
 */
public interface FeeConfigMapper
{
    public FeeConfig selectFeeConfigById(Long id);

    public List<FeeConfig> selectFeeConfigList(FeeConfig feeConfig);

    public List<FeeConfig> selectFeeConfigByFeeType(@Param("feeType") String feeType);

    public int insertFeeConfig(FeeConfig feeConfig);

    public int updateFeeConfig(FeeConfig feeConfig);

    public int deleteFeeConfigById(Long id);

    public int deleteFeeConfigByIds(Long[] ids);
}
