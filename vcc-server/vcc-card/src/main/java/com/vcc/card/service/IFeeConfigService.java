package com.vcc.card.service;

import java.util.List;
import com.vcc.card.domain.FeeConfig;

/**
 * 费率配置 服务层
 */
public interface IFeeConfigService
{
    public FeeConfig selectFeeConfigById(Long id);

    public List<FeeConfig> selectFeeConfigList(FeeConfig feeConfig);

    public List<FeeConfig> selectFeeConfigByFeeType(String feeType);

    public int insertFeeConfig(FeeConfig feeConfig);

    public int updateFeeConfig(FeeConfig feeConfig);

    public int deleteFeeConfigByIds(Long[] ids);
}
