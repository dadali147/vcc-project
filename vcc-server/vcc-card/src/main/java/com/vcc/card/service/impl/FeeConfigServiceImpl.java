package com.vcc.card.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.mapper.FeeConfigMapper;
import com.vcc.card.service.IFeeConfigService;

/**
 * 费率配置 服务实现
 */
@Service
public class FeeConfigServiceImpl implements IFeeConfigService
{
    @Autowired
    private FeeConfigMapper feeConfigMapper;

    @Override
    public FeeConfig selectFeeConfigById(Long id)
    {
        return feeConfigMapper.selectFeeConfigById(id);
    }

    @Override
    public List<FeeConfig> selectFeeConfigList(FeeConfig feeConfig)
    {
        return feeConfigMapper.selectFeeConfigList(feeConfig);
    }

    @Override
    public List<FeeConfig> selectFeeConfigByFeeType(String feeType)
    {
        return feeConfigMapper.selectFeeConfigByFeeType(feeType);
    }

    @Override
    public int insertFeeConfig(FeeConfig feeConfig)
    {
        return feeConfigMapper.insertFeeConfig(feeConfig);
    }

    @Override
    public int updateFeeConfig(FeeConfig feeConfig)
    {
        return feeConfigMapper.updateFeeConfig(feeConfig);
    }

    @Override
    public int deleteFeeConfigByIds(Long[] ids)
    {
        return feeConfigMapper.deleteFeeConfigByIds(ids);
    }
}
