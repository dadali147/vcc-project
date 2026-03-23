package com.vcc.framework.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mailgun 邮件发送服务
 */
@Service
public class MailgunEmailService
{
    private static final Logger log = LoggerFactory.getLogger(MailgunEmailService.class);

    private static final String DOMAIN_PATTERN = "^[a-zA-Z0-9.-]+$";

    @Value("${mailgun.api-key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Value("${mailgun.from}")
    private String from;

    // W6: 连接超时5秒，请求超时10秒
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // W3: 启动时校验 domain 格式
    @PostConstruct
    public void validateConfig()
    {
        if (domain == null || !domain.matches(DOMAIN_PATTERN))
        {
            throw new IllegalStateException("mailgun.domain 配置不合法: " + domain);
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param to   收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCode(String to, String code)
    {
        String subject = "您的注册验证码";
        String text = "您的验证码是：" + code + "\n\n验证码10分钟内有效，请勿泄露给他人。";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("from", from);
        params.put("to", to);
        params.put("subject", subject);
        params.put("text", text);

        String formBody = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        String credentials = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
        String url = "https://api.mailgun.net/v3/" + domain + "/messages";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + credentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))  // W6: 请求超时10秒
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 202)
            {
                log.info("验证码邮件发送成功: to={}", maskEmail(to));
            }
            else
            {
                log.error("验证码邮件发送失败: to={}, status={}", maskEmail(to), response.statusCode());
                throw new RuntimeException("邮件发送失败，请稍后重试");
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("验证码邮件发送异常: to={}, error={}", maskEmail(to), e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
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
