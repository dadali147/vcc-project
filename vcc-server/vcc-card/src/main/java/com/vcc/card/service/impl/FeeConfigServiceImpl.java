package com.vcc.card.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.mapper.FeeConfigMapper;
import com.vcc.card.service.IFeeConfigService;
import com.vcc.system.service.ISystemConfigService;

/**
 * 费率配置 服务实现
 */
@Service
public class FeeConfigServiceImpl implements IFeeConfigService
{
    private static final Logger log = LoggerFactory.getLogger(FeeConfigServiceImpl.class);

    @Autowired
    private FeeConfigMapper feeConfigMapper;

    @Autowired
    private ISystemConfigService systemConfigService;

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

    @Override
    public List<Map<String, Object>> selectUserFeeConfigList(Map<String, Object> params)
    {
        // 查询 vcc_fee_config 中 user_id > 0 的记录（用户专属费率）
        FeeConfig query = new FeeConfig();
        if (params != null && params.get("feeType") != null)
        {
            query.setFeeType(params.get("feeType").toString());
        }
        List<FeeConfig> allConfigs = feeConfigMapper.selectFeeConfigList(query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FeeConfig fc : allConfigs)
        {
            // 仅返回用户专属费率（userId > 0），userId=0 为默认配置
            if (fc.getUserId() != null && fc.getUserId() > 0)
            {
                Map<String, Object> map = new HashMap<>();
                map.put("id", fc.getId());
                map.put("userId", fc.getUserId());
                map.put("cardBinId", fc.getCardBinId());
                map.put("feeType", fc.getFeeType());
                map.put("rate", fc.getRate());
                map.put("fixedAmount", fc.getFixedAmount());
                map.put("minFee", fc.getMinFee());
                map.put("maxFee", fc.getMaxFee());
                map.put("status", fc.getStatus());
                map.put("effectiveDate", fc.getEffectiveDate());
                map.put("expiryDate", fc.getExpiryDate());
                result.add(map);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserFeeConfig(Long userId)
    {
        Map<String, Object> result = new HashMap<>();

        // 从 system_configs 读取用户专属费率
        String rechargeRate = systemConfigService.get("fee.user." + userId + ".recharge.rate");
        String openRate = systemConfigService.get("fee.user." + userId + ".open.rate");
        String txnRate = systemConfigService.get("fee.user." + userId + ".txn.rate");

        result.put("userId", userId);
        result.put("rechargeRate", rechargeRate);
        result.put("openRate", openRate);
        result.put("txnRate", txnRate);

        // 如果用户没有专属费率，标记使用默认
        if (rechargeRate == null && openRate == null && txnRate == null)
        {
            result.put("isDefault", true);
        }
        else
        {
            result.put("isDefault", false);
        }

        return result;
    }

    @Override
    public boolean setUserFeeConfig(Long userId, Map<String, Object> feeConfig)
    {
        if (feeConfig == null)
        {
            return false;
        }
        if (feeConfig.containsKey("rechargeRate") && feeConfig.get("rechargeRate") != null)
        {
            systemConfigService.set("fee.user." + userId + ".recharge.rate", feeConfig.get("rechargeRate").toString());
        }
        if (feeConfig.containsKey("openRate") && feeConfig.get("openRate") != null)
        {
            systemConfigService.set("fee.user." + userId + ".open.rate", feeConfig.get("openRate").toString());
        }
        if (feeConfig.containsKey("txnRate") && feeConfig.get("txnRate") != null)
        {
            systemConfigService.set("fee.user." + userId + ".txn.rate", feeConfig.get("txnRate").toString());
        }
        log.info("更新用户费率配置, userId={}, config={}", userId, feeConfig);
        return true;
    }
}
