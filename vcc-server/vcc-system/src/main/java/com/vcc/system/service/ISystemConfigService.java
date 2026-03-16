package com.vcc.system.service;

import java.util.List;
import com.vcc.system.domain.SystemConfig;

/**
 * 系统配置 服务层
 */
public interface ISystemConfigService
{
    public SystemConfig selectSystemConfigById(Long id);

    public List<SystemConfig> selectSystemConfigList(SystemConfig systemConfig);

    /**
     * 查询配置值
     */
    public String get(String key);

    /**
     * 更新配置值（敏感配置自动加密）
     */
    public int set(String key, String value);

    /**
     * 获取当前 AES 密钥
     */
    public String getYeeVccAesKey();

    /**
     * 获取当前 API 地址
     */
    public String getYeeVccBaseUrl();

    public int insertSystemConfig(SystemConfig systemConfig);

    public int updateSystemConfig(SystemConfig systemConfig);

    public int deleteSystemConfigById(Long id);
}
