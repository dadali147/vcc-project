package com.vcc.system.domain;

import com.vcc.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 系统配置 system_configs
 */
public class SystemConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    /** 配置键 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 值类型：STRING/NUMBER/JSON/BOOLEAN */
    private String configType;

    /** 配置说明 */
    private String description;

    /** 是否加密存储：0-否，1-是 */
    private Integer isEncrypted;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }

    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }

    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getIsEncrypted()
    {
        return isEncrypted;
    }

    public void setIsEncrypted(Integer isEncrypted)
    {
        this.isEncrypted = isEncrypted;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("configKey", getConfigKey())
                .append("configType", getConfigType())
                .append("description", getDescription())
                .append("isEncrypted", getIsEncrypted())
                .toString();
    }
}
