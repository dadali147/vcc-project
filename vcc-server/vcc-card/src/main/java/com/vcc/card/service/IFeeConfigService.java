package com.vcc.card.service;

import java.util.List;
import java.util.Map;
import com.vcc.card.domain.FeeConfig;

/**
 * 费率配置 服务层
 */
public interface IFeeConfigService
{
    public FeeConfig selectFeeConfigById(Long id);

    /**
     * 查询用户专属费率配置列表（管理端）
     */
    public List<Map<String, Object>> selectUserFeeConfigList(Map<String, Object> params);

    /**
     * 获取用户费率
     */
    public Map<String, Object> getUserFeeConfig(Long userId);

    /**
     * 更新用户费率
     */
    public boolean setUserFeeConfig(Long userId, Map<String, Object> feeConfig);

    public List<FeeConfig> selectFeeConfigList(FeeConfig feeConfig);

    public List<FeeConfig> selectFeeConfigByFeeType(String feeType);

    public int insertFeeConfig(FeeConfig feeConfig);

    public int updateFeeConfig(FeeConfig feeConfig);

    public int deleteFeeConfigByIds(Long[] ids);
}
