package com.vcc.framework.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.vcc.common.core.redis.RedisCache;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码服务：生成、存储(Redis)、校验
 */
@Service
public class EmailCodeService
{
    private static final Logger log = LoggerFactory.getLogger(EmailCodeService.class);

    /** Redis key 前缀：存储验证码 */
    private static final String CODE_KEY_PREFIX = "email:code:";

    /** Redis key 前缀：存储发送时间戳（用于60秒频率限制） */
    private static final String RATE_KEY_PREFIX = "email:rate:";

    /** Redis key 前缀：IP级别限流计数（每小时最多10次） */
    private static final String IP_RATE_KEY_PREFIX = "email:ip:rate:";

    /** 同一IP每小时最多发送次数 */
    private static final int IP_MAX_COUNT = 10;

    /** Redis key 前缀：存储验证失败计数（用于暴力破解防护） */
    private static final String FAIL_KEY_PREFIX = "email:fail:";

    /** 最大验证失败次数 */
    private static final int MAX_FAIL_COUNT = 5;

    // W2: 使用 SecureRandom 代替 Random
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${email-code.expire-minutes:10}")
    private int expireMinutes;

    @Value("${email-code.resend-interval-seconds:60}")
    private int resendIntervalSeconds;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private MailgunEmailService mailgunEmailService;

    /**
     * 发送验证码
     * - 60秒内不能重复发送
     * - 生成6位数字验证码，10分钟有效
     * - C3: 先发邮件成功后再写 Redis，保证一致性
     *
     * @param email 目标邮箱
     */
    public void sendCode(String email)
    {
        String rateKey = RATE_KEY_PREFIX + email;
        if (Boolean.TRUE.equals(redisCache.hasKey(rateKey)))
        {
            long ttl = redisCache.getExpire(rateKey);
            throw new RuntimeException("发送过于频繁，请 " + ttl + " 秒后重试");
        }

        String code = generateCode();

        // C3: 先发邮件，成功后再写 Redis；邮件失败直接抛异常，不写 Redis
        mailgunEmailService.sendVerificationCode(email, code);

        String codeKey = CODE_KEY_PREFIX + email;
        // 存验证码，10分钟过期
        redisCache.setCacheObject(codeKey, code, expireMinutes, TimeUnit.MINUTES);
        // 存频率限制标记，60秒过期
        redisCache.setCacheObject(rateKey, "1", resendIntervalSeconds, TimeUnit.SECONDS);
        // 发新码时重置失败计数
        redisCache.deleteObject(FAIL_KEY_PREFIX + email);
    }

    /**
     * 校验验证码
     * - C2: 失败超过5次后删除验证码并阻断
     * - 验证通过后删除，防止重复使用
     *
     * @param email 邮箱
     * @param code  用户输入的验证码
     * @return true=通过
     */
    public boolean verifyCode(String email, String code)
    {
        String failKey = FAIL_KEY_PREFIX + email;

        // C2: 先检查失败次数
        Integer failCount = redisCache.getCacheObject(failKey);
        if (failCount != null && failCount >= MAX_FAIL_COUNT)
        {
            // 已超限，验证码已被删除，直接拒绝
            return false;
        }

        String codeKey = CODE_KEY_PREFIX + email;
        String stored = redisCache.getCacheObject(codeKey);
        if (stored == null)
        {
            return false;
        }
        if (stored.equals(code))
        {
            redisCache.deleteObject(codeKey);
            redisCache.deleteObject(failKey);
            return true;
        }

        // C2: 验证失败，计数+1
        int newCount = (failCount == null ? 0 : failCount) + 1;
        // 失败计数与验证码同TTL（10分钟）
        redisCache.setCacheObject(failKey, newCount, expireMinutes, TimeUnit.MINUTES);
        if (newCount >= MAX_FAIL_COUNT)
        {
            // 超过5次，删除验证码，后续验证直接走超限分支
            redisCache.deleteObject(codeKey);
            log.warn("验证码暴力破解防护触发，已锁定: email={}", maskEmail(email));
        }
        return false;
    }

    // W2: 使用 SecureRandom
    private String generateCode()
    {
        int num = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(num);
    }

    /**
     * IP级别限流检查：同一IP每小时最多10次send-code请求
     *
     * @param ip 客户端IP
     * @throws RuntimeException 超限时抛出
     */
    public void checkIpRateLimit(String ip)
    {
        String key = IP_RATE_KEY_PREFIX + ip;
        Integer count = redisCache.getCacheObject(key);
        if (count != null && count >= IP_MAX_COUNT)
        {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }
        if (count == null)
        {
            redisCache.setCacheObject(key, 1, 1, TimeUnit.HOURS);
        }
        else
        {
            long ttl = redisCache.getExpire(key);
            redisCache.setCacheObject(key, count + 1, (int) Math.max(ttl, 1), TimeUnit.SECONDS);
        }
    }

    // 邮箱脱敏：a**@example.com
    private static String maskEmail(String email)
    {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "**" + email.substring(at);
    }
}
