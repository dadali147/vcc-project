package com.vcc.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 字段级 AES 加密工具类（用于 PII 数据加密存储）
 * VCC-SEC-001: 算法升级为 AES/GCM/NoPadding，向后兼容 ECB 格式
 */
public class FieldEncryptUtil
{
    private FieldEncryptUtil() {}

    /**
     * 加密明文字段值
     * VCC-SEC-001: 使用 AES/GCM/NoPadding，每次生成随机 IV
     *
     * @param plainText 明文
     * @param key       加密密钥（超过16字节取前16字节，不足补0）
     * @return Base64 编码的密文（格式: IV(12字节) + 密文 + GCM tag），plainText 为 null 时返回 null
     */
    public static String encrypt(String plainText, String key)
    {
        if (plainText == null)
        {
            return null;
        }
        try
        {
            byte[] keyBytes = padKey(key);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());
        }
        catch (Exception e)
        {
            throw new RuntimeException("字段加密失败", e);
        }
    }

    /**
     * 解密密文字段值
     * VCC-SEC-001: 优先尝试 GCM 格式，失败后兼容旧 ECB 格式
     *
     * @param cipherText Base64 编码的密文
     * @param key        加密密钥
     * @return 明文，cipherText 为 null 时返回 null
     * @throws RuntimeException 解密失败时抛出（可能是明文传入，调用方需处理）
     */
    public static String decrypt(String cipherText, String key)
    {
        if (cipherText == null)
        {
            return null;
        }
        try
        {
            byte[] keyBytes = padKey(key);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // 尝试新格式 GCM (IV 12字节 + 密文)
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
                    // GCM 失败，尝试旧格式
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
            throw new RuntimeException("字段解密失败", e);
        }
    }

    /**
     * 将密钥填充/截断到 16 字节（AES-128）
     */
    private static byte[] padKey(String key)
    {
        byte[] keyBytes = new byte[16];
        byte[] src = key == null ? new byte[0] : key.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 16));
        return keyBytes;
    }
}
