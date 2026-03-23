package com.vcc.system.service.impl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.vcc.common.utils.StringUtils;
import com.vcc.system.domain.SystemConfig;
import com.vcc.system.mapper.SystemConfigMapper;
import com.vcc.system.service.ISystemConfigService;

/**
 * 系统配置 服务实现
 */
@Service
public class SystemConfigServiceImpl implements ISystemConfigService
{
    private static final Logger log = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    /** 内部加密密钥（用于加密存储敏感配置值） */
    @Value("${vcc.config.encrypt-key}")
    private String encryptKey;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Override
    public SystemConfig selectSystemConfigById(Long id)
    {
        SystemConfig config = systemConfigMapper.selectSystemConfigById(id);
        decryptIfNeeded(config);
        return config;
    }

    @Override
    public List<SystemConfig> selectSystemConfigList(SystemConfig systemConfig)
    {
        List<SystemConfig> list = systemConfigMapper.selectSystemConfigList(systemConfig);
        for (SystemConfig config : list)
        {
            if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1)
            {
                // 列表展示敏感配置时脱敏
                config.setConfigValue("******");
            }
        }
        return list;
    }

    @Override
    public String get(String key)
    {
        SystemConfig config = systemConfigMapper.selectSystemConfigByKey(key);
        if (config == null)
        {
            return null;
        }
        decryptIfNeeded(config);
        return config.getConfigValue();
    }

    @Override
    public int set(String key, String value)
    {
        SystemConfig existing = systemConfigMapper.selectSystemConfigByKey(key);
        if (existing == null)
        {
            SystemConfig config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType("STRING");
            config.setIsEncrypted(0);
            return systemConfigMapper.insertSystemConfig(config);
        }

        SystemConfig update = new SystemConfig();
        update.setConfigKey(key);
        // 敏感配置加密存储
        if (existing.getIsEncrypted() != null && existing.getIsEncrypted() == 1)
        {
            update.setConfigValue(encrypt(value));
        }
        else
        {
            update.setConfigValue(value);
        }
        return systemConfigMapper.updateSystemConfigByKey(update);
    }

    @Override
    public String getYeeVccAesKey()
    {
        return get("yeevcc.aes.key.v1");
    }

    @Override
    public String getYeeVccBaseUrl()
    {
        // 先取测试环境地址，生产环境可通过配置切换
        String url = get("yeevcc.api.baseurl.prod");
        if (StringUtils.isEmpty(url))
        {
            url = get("yeevcc.api.baseurl.test");
        }
        return url;
    }

    @Override
    public int insertSystemConfig(SystemConfig systemConfig)
    {
        if (systemConfig.getIsEncrypted() != null && systemConfig.getIsEncrypted() == 1
                && StringUtils.isNotEmpty(systemConfig.getConfigValue()))
        {
            systemConfig.setConfigValue(encrypt(systemConfig.getConfigValue()));
        }
        return systemConfigMapper.insertSystemConfig(systemConfig);
    }

    @Override
    public int updateSystemConfig(SystemConfig systemConfig)
    {
        // 如果更新的是加密字段且值不是脱敏值，需要加密
        if (systemConfig.getId() != null)
        {
            SystemConfig existing = systemConfigMapper.selectSystemConfigById(systemConfig.getId());
            if (existing != null && existing.getIsEncrypted() != null && existing.getIsEncrypted() == 1
                    && StringUtils.isNotEmpty(systemConfig.getConfigValue())
                    && !"******".equals(systemConfig.getConfigValue()))
            {
                systemConfig.setConfigValue(encrypt(systemConfig.getConfigValue()));
            }
        }
        return systemConfigMapper.updateSystemConfig(systemConfig);
    }

    @Override
    public int deleteSystemConfigById(Long id)
    {
        return systemConfigMapper.deleteSystemConfigById(id);
    }

    private void decryptIfNeeded(SystemConfig config)
    {
        if (config != null && config.getIsEncrypted() != null && config.getIsEncrypted() == 1
                && StringUtils.isNotEmpty(config.getConfigValue()))
        {
            try
            {
                config.setConfigValue(decrypt(config.getConfigValue()));
            }
            catch (Exception e)
            {
                log.warn("解密配置失败, key={}, 可能未加密", config.getConfigKey());
            }
        }
    }

    private String encrypt(String plainText)
    {
        try
        {
            byte[] keyBytes = padKey(encryptKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            // VCC-SEC-001: 使用 AES/GCM/NoPadding，每次生成随机 IV
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 格式: IV(12字节) + 密文(含GCM tag)
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());
        }
        catch (Exception e)
        {
            log.error("加密失败", e);
            throw new RuntimeException("加密配置值失败", e);
        }
    }

    private String decrypt(String cipherText)
    {
        try
        {
            byte[] keyBytes = padKey(encryptKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // VCC-SEC-001: 尝试新格式 GCM (IV 12字节 + 密文)
            if (decoded.length > 12)
            {
                try
                {
                    ByteBuffer buffer = ByteBuffer.wrap(decoded);
                    byte[] iv = new byte[12];
                    buffer.get(iv);
                    byte[] cipherBytes = new byte[buffer.remaining()];
                    buffer.get(cipherBytes);

                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                    GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
                    byte[] decrypted = cipher.doFinal(cipherBytes);
                    return new String(decrypted, StandardCharsets.UTF_8);
                }
                catch (Exception e)
                {
                    // GCM 解密失败，尝试旧格式 ECB
                    log.debug("GCM解密失败，尝试旧格式ECB");
                }
            }

            // 兼容旧格式 ECB
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new RuntimeException("解密配置值失败", e);
        }
    }

    private byte[] padKey(String key)
    {
        byte[] keyBytes = new byte[16];
        byte[] src = key.getBytes();
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 16));
        return keyBytes;
    }
}
