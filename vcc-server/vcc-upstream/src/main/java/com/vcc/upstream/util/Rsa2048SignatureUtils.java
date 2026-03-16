package com.vcc.upstream.util;

import com.vcc.common.utils.StringUtils;
import com.vcc.upstream.exception.YeeVccException;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RSA2048 签名与验签工具。
 */
public final class Rsa2048SignatureUtils
{
    private static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";

    private static final Pattern SIGNATURE_PATTERN = Pattern.compile("signature\\s*=\\s*\"?([^\",\\s]+)\"?",
            Pattern.CASE_INSENSITIVE);

    private Rsa2048SignatureUtils()
    {
    }

    public static String sign(String content, String privateKeyText)
    {
        return sign(content, privateKeyText, DEFAULT_SIGNATURE_ALGORITHM);
    }

    public static String sign(String content, String privateKeyText, String signatureAlgorithm)
    {
        if (StringUtils.isBlank(privateKeyText))
        {
            throw new YeeVccException("YeeVCC 私钥未配置");
        }
        try
        {
            Signature signature = Signature.getInstance(StringUtils.defaultIfBlank(signatureAlgorithm,
                    DEFAULT_SIGNATURE_ALGORITHM));
            signature.initSign(loadPrivateKey(privateKeyText));
            signature.update(StringUtils.defaultString(content).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        }
        catch (Exception ex)
        {
            throw new YeeVccException("YeeVCC RSA 签名失败", null, null, ex);
        }
    }

    public static boolean verify(String content, String signatureText, String publicKeyText)
    {
        return verify(content, signatureText, publicKeyText, DEFAULT_SIGNATURE_ALGORITHM);
    }

    public static boolean verify(String content, String signatureText, String publicKeyText, String signatureAlgorithm)
    {
        if (StringUtils.isAnyBlank(signatureText, publicKeyText))
        {
            return false;
        }
        try
        {
            Signature signature = Signature.getInstance(StringUtils.defaultIfBlank(signatureAlgorithm,
                    DEFAULT_SIGNATURE_ALGORITHM));
            signature.initVerify(loadPublicKey(publicKeyText));
            signature.update(StringUtils.defaultString(content).getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getMimeDecoder().decode(extractSignature(signatureText)));
        }
        catch (Exception ex)
        {
            throw new YeeVccException("YeeVCC RSA 验签失败", null, null, ex);
        }
    }

    public static String sha256Hex(String content)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(StringUtils.defaultString(content).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        }
        catch (Exception ex)
        {
            throw new YeeVccException("YeeVCC SHA-256 计算失败", null, null, ex);
        }
    }

    private static PrivateKey loadPrivateKey(String privateKeyText) throws Exception
    {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodeKey(privateKeyText)));
    }

    private static PublicKey loadPublicKey(String publicKeyText) throws Exception
    {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(decodeKey(publicKeyText)));
    }

    private static byte[] decodeKey(String keyText)
    {
        String normalized = StringUtils.defaultString(keyText)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        return Base64.getMimeDecoder().decode(normalized);
    }

    private static String extractSignature(String signatureText)
    {
        Matcher matcher = SIGNATURE_PATTERN.matcher(signatureText);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        int lastSpace = signatureText.lastIndexOf(' ');
        if (lastSpace >= 0 && lastSpace < signatureText.length() - 1)
        {
            return signatureText.substring(lastSpace + 1).replace("\"", "");
        }
        return signatureText.replace("\"", "");
    }
}
