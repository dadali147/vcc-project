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

    /**
     * 发送3DS OTP验证码邮件
     *
     * @param to              收件人邮箱
     * @param otpCode         验证码
     * @param merchantName    商户名称
     * @param amount          交易金额（可为null）
     * @param currency        币种（可为null）
     */
    public void send3dsOtp(String to, String otpCode, String merchantName, String amount, String currency)
    {
        String subject = "您的3DS验证码";

        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("您的3DS交易验证码是：").append(otpCode).append("\n\n");
        if (merchantName != null && !merchantName.isBlank())
        {
            textBuilder.append("商户：").append(merchantName).append("\n");
        }
        if (amount != null && !amount.isBlank() && currency != null && !currency.isBlank())
        {
            textBuilder.append("交易金额：").append(amount).append(" ").append(currency).append("\n");
        }
        textBuilder.append("\n验证码5分钟内有效，请勿泄露给他人。");

        Map<String, String> params = new LinkedHashMap<>();
        params.put("from", from);
        params.put("to", to);
        params.put("subject", subject);
        params.put("text", textBuilder.toString());

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
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try
        {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 202)
            {
                log.info("3DS OTP邮件发送成功: to={}", maskEmail(to));
            }
            else
            {
                log.error("3DS OTP邮件发送失败: to={}, status={}", maskEmail(to), response.statusCode());
                throw new RuntimeException("邮件发送失败，请稍后重试");
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("3DS OTP邮件发送异常: to={}, error={}", maskEmail(to), e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    // 邮箱脱敏：a**@example.com
    public static String maskEmail(String email)
    {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "**" + email.substring(at);
    }
}
