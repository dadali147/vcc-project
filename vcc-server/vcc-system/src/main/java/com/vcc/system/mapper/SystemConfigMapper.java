package com.vcc.system.mapper;

import java.util.List;
import com.vcc.system.domain.SystemConfig;

/**
 * 系统配置 数据层
 */
public interface SystemConfigMapper
{
    public SystemConfig selectSystemConfigById(Long id);

    public SystemConfig selectSystemConfigByKey(String configKey);

    public List<SystemConfig> selectSystemConfigList(SystemConfig systemConfig);

    public int insertSystemConfig(SystemConfig systemConfig);

    public int updateSystemConfig(SystemConfig systemConfig);

    public int updateSystemConfigByKey(SystemConfig systemConfig);

    public int deleteSystemConfigById(Long id);
}
