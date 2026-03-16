package com.vcc.upstream.util;

import com.vcc.common.utils.StringUtils;
import com.vcc.upstream.exception.YeeVccException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 16 位 AES/CBC/PKCS5Padding 工具。
 */
public final class Aes16CryptoUtils
{
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    private Aes16CryptoUtils()
    {
    }

    public static String encrypt(String plainText, String aesKey)
    {
        if (StringUtils.isBlank(plainText))
        {
            return plainText;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildSecretKey(aesKey), buildIv(aesKey));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception ex)
        {
            throw new YeeVccException("YeeVCC AES 加密失败", null, null, ex);
        }
    }

    public static String decrypt(String encryptedText, String aesKey)
    {
        if (StringUtils.isBlank(encryptedText))
        {
            return encryptedText;
        }
        try
        {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildSecretKey(aesKey), buildIv(aesKey));
            byte[] decrypted = cipher.doFinal(Base64.getMimeDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        catch (Exception ex)
        {
            throw new YeeVccException("YeeVCC AES 解密失败", null, null, ex);
        }
    }

    private static SecretKeySpec buildSecretKey(String aesKey)
    {
        validateAesKey(aesKey);
        return new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private static IvParameterSpec buildIv(String aesKey)
    {
        validateAesKey(aesKey);
        return new IvParameterSpec(aesKey.getBytes(StandardCharsets.UTF_8), 0, 16);
    }

    private static void validateAesKey(String aesKey)
    {
        if (StringUtils.isBlank(aesKey) || aesKey.getBytes(StandardCharsets.UTF_8).length != 16)
        {
            throw new YeeVccException("YeeVCC AES Key 必须是 16 位字符串");
        }
    }
}
