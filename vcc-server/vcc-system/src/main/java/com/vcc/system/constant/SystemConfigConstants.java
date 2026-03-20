package com.vcc.system.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统配置常量 - 白名单与校验规则
 *
 * @author vcc
 */
public class SystemConfigConstants
{
    private SystemConfigConstants() {}

    /** 允许通过管理后台更新的 config_key 白名单 */
    public static final Set<String> ALLOWED_CONFIG_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "vcc.webhook.secret",
            "yeevcc.aes.key.v1",
            "yeevcc.aes.key.v2",
            "vcc.fee.default",
            "vcc.risk.enabled"
    )));

    /**
     * 校验 configKey 是否在白名单内
     */
    public static boolean isAllowedKey(String configKey)
    {
        return configKey != null && ALLOWED_CONFIG_KEYS.contains(configKey);
    }

    /**
     * 校验 configValue 格式是否合法
     *
     * @return 错误信息，null 表示校验通过
     */
    public static String validateValue(String configKey, String configValue)
    {
        if (configValue == null || configValue.trim().isEmpty())
        {
            return "配置值不能为空";
        }

        switch (configKey)
        {
            case "vcc.webhook.secret":
                if (configValue.length() < 8 || configValue.length() > 256)
                {
                    return "webhook secret 长度必须在 8-256 之间";
                }
                break;
            case "vcc.risk.enabled":
                if (!"true".equals(configValue) && !"false".equals(configValue))
                {
                    return "vcc.risk.enabled 只允许 true 或 false";
                }
                break;
            default:
                // 其他白名单内的 key：非空即可（已在上面检查）
                break;
        }
        return null;
    }
}
