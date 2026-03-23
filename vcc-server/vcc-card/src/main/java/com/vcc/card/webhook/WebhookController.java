package com.vcc.card.webhook;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vcc.card.domain.Card;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.domain.Vcc3dsOtpLog;
import com.vcc.card.mapper.CardHolderMapper;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.service.IVcc3dsOtpLogService;
import com.vcc.framework.service.MailgunEmailService;
import com.vcc.system.mapper.SysUserMapper;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.system.service.ISysConfigService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * YeeVCC Webhook 回调接收入口
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController
{
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    /** 时间戳容忍窗口（秒） */
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300;

    /** 系统配置key：webhook签名密钥 */
    private static final String CONFIG_KEY_WEBHOOK_SECRET = "vcc.webhook.secret";

    @Autowired
    private WebhookService webhookService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private CardHolderMapper cardHolderMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IVcc3dsOtpLogService otpLogService;

    @Autowired
    private MailgunEmailService mailgunEmailService;

    /** 找不到持卡人邮箱时的兜底邮箱 */
    @Value("${vcc.otp.default-email:otp@kimoox.com}")
    private String defaultOtpEmail;

    /**
     * 接收 YeeVCC 上游回调（同步验签 + 入队后再返回 200）
     * VCC-011: 先验签，成功后再返回 2xx，确保失败时能触发上游重试
     */
    @PostMapping("/yeevcc")
    public String yeevcc(@RequestBody Map<String, Object> data,
                         @RequestHeader(value = "x-yop-signature", required = false) String signature,
                         HttpServletRequest request)
    {
        String webhookType = resolveWebhookType(data);
        String payload = JSON.toJSONString(data);

        log.info("收到YeeVCC回调: type={}, data={}", webhookType, payload);

        // VCC-011: 同步验签，失败返回 401 触发上游重试
        if (!verifyWebhookSignature(request, payload))
        {
            log.error("Webhook验签失败，返回 401: type={}", webhookType);
            throw new WebhookAuthenticationException("验签失败");
        }

        // 验签通过后，保存到队列/数据库，再异步处理
        boolean queued = webhookService.enqueueWebhook(webhookType, payload, signature, data);
        if (!queued)
        {
            log.error("Webhook入队失败，返回 500: type={}", webhookType);
            throw new WebhookProcessingException("处理失败，请重试");
        }

        return "ok";
    }

    /**
     * 3DS 验证码回调接口
     * VCC-002: 收到 OTP 回调后，动态路由到持卡人邮箱，并记录3DS验证码
     * 返回格式：{"method": 2, "destination": "邮箱"} — YeeVcc 接口契约要求
     */
    @PostMapping(value = "/otp", produces = "application/json;charset=UTF-8")
    public Map<String, Object> otp(@RequestBody Map<String, Object> data,
                                   @RequestHeader(value = "x-yop-signature", required = false) String signature,
                                   HttpServletRequest request)
    {
        String payload = JSON.toJSONString(data);
        // 日志脱敏：不打印完整 payload（含 OTP 明文），只记录 cardId
        log.info("收到3DS验证码回调: cardId={}", data.get("cardId"));

        // 验签
        if (!verifyWebhookSignature(request, payload))
        {
            log.error("3DS OTP回调验签失败，返回 401");
            throw new WebhookAuthenticationException("验签失败");
        }

        // 入队处理
        boolean queued = webhookService.enqueueWebhook("OTP", payload, signature, data);
        if (!queued)
        {
            log.error("3DS OTP回调入队失败，返回 500");
            throw new WebhookProcessingException("处理失败，请重试");
        }

        // 从 payload 提取字段
        String cardId = data.get("cardId") != null ? data.get("cardId").toString() : null;
        String otpCode = data.get("otp") != null ? data.get("otp").toString() : null;
        String merchantName = data.get("merchantName") != null ? data.get("merchantName").toString() : null;
        BigDecimal transactionAmount = null;
        String currency = data.get("currency") != null ? data.get("currency").toString() : null;
        try
        {
            Object amt = data.get("amount");
            if (amt != null) transactionAmount = new BigDecimal(amt.toString());
        }
        catch (Exception e)
        {
            log.warn("3DS OTP: 解析交易金额失败, value={}", data.get("amount"));
        }

        // 动态路由：通过 cardId 查询持卡人邮箱
        String destination = defaultOtpEmail;
        Long userId = null;
        Long holderId = null;
        String cardNoMask = null;

        if (StringUtils.isNotBlank(cardId))
        {
            try
            {
                Card card = cardMapper.selectCardByUpstreamCardId(cardId);
                if (card != null)
                {
                    userId = card.getUserId();
                    holderId = card.getHolderId();
                    cardNoMask = card.getCardNoMask();

                    // 优先使用 CardHolder.email
                    if (holderId != null)
                    {
                        CardHolder holder = cardHolderMapper.selectCardHolderById(holderId);
                        if (holder != null && StringUtils.isNotBlank(holder.getEmail()))
                        {
                            destination = holder.getEmail();
                            log.info("3DS OTP: 路由到持卡人邮箱, cardId={}, dest={}", cardId, MailgunEmailService.maskEmail(destination));
                        }
                    }

                    // 回退到 SysUser.email（userName字段存邮箱）
                    if (destination.equals(defaultOtpEmail) && userId != null)
                    {
                        SysUser sysUser = sysUserMapper.selectUserById(userId);
                        if (sysUser != null && StringUtils.isNotBlank(sysUser.getUserName()))
                        {
                            destination = sysUser.getUserName();
                            log.info("3DS OTP: 回退到注册邮箱, cardId={}, dest={}", cardId, MailgunEmailService.maskEmail(destination));
                        }
                    }

                    if (destination.equals(defaultOtpEmail))
                    {
                        log.warn("3DS OTP: 未找到持卡人或注册邮箱，使用默认邮箱, cardId={}, userId={}", cardId, userId);
                    }
                }
                else
                {
                    log.warn("3DS OTP: 未找到卡记录, cardId={}", cardId);
                }
            }
            catch (Exception e)
            {
                log.error("3DS OTP: 查询持卡人邮箱异常, cardId={}", cardId, e);
            }
        }

        // 写入3DS OTP记录表
        try
        {
            Vcc3dsOtpLog otpLog = new Vcc3dsOtpLog();
            otpLog.setCardId(cardId != null ? cardId : "");
            otpLog.setUserId(userId != null ? userId : 0L);
            otpLog.setHolderId(holderId);
            otpLog.setCardNoMask(cardNoMask);
            otpLog.setOtpCode(otpCode);
            otpLog.setMerchantName(merchantName);
            otpLog.setTransactionAmount(transactionAmount);
            otpLog.setCurrency(currency);
            otpLog.setDestinationEmail(MailgunEmailService.maskEmail(destination));
            otpLog.setStatus(Vcc3dsOtpLog.STATUS_PENDING);
            otpLog.setWebhookPayload(payload);
            otpLogService.insertVcc3dsOtpLog(otpLog);
        }
        catch (Exception e)
        {
            log.error("3DS OTP: 写入记录失败, cardId={}", cardId, e);
        }

        // 发送OTP邮件（非阻塞失败：邮件失败不影响响应）
        try
        {
            String amountStr = transactionAmount != null ? transactionAmount.toPlainString() : null;
            mailgunEmailService.send3dsOtp(destination, otpCode, merchantName, amountStr, currency);
        }
        catch (Exception e)
        {
            log.error("3DS OTP: 发送邮件失败, dest={}, error={}", MailgunEmailService.maskEmail(destination), e.getMessage());
        }

        return Map.of("method", 2, "destination", destination);
    }

    /**
     * 第三方授权决策（YeeVcc 要求）
     * 收到授权请求时，统一返回批准（authResult=A）
     */
    @PostMapping(value = "/auth-decision", produces = "application/json;charset=UTF-8")
    public Map<String, Object> authDecision(@RequestBody Map<String, Object> data,
                                            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
                                            HttpServletRequest request)
    {
        String payload = JSON.toJSONString(data);
        log.info("收到授权决策请求: cardId={}", data.get("cardId"));

        // VCC-011: 授权决策端点也必须验签，防止伪造授权
        if (!verifyWebhookSignature(request, payload))
        {
            log.error("授权决策请求验签失败，返回 401");
            throw new WebhookAuthenticationException("验签失败");
        }

        return Map.of(
            "requestId", data.getOrDefault("requestId", ""),
            "cardId", data.getOrDefault("cardId", ""),
            "customerId", data.getOrDefault("customerId", ""),
            "merchantId", data.getOrDefault("merchantId", ""),
            "authResult", "A",
            "responseCode", "00",
            "message", "Approved"
        );
    }

    /**
     * 交易授权回调接口
     * VCC-002: 交易授权结果通知
     */
    @PostMapping("/authTransaction")
    public String authTransaction(@RequestBody Map<String, Object> data,
                                  @RequestHeader(value = "x-yop-signature", required = false) String signature,
                                  HttpServletRequest request)
    {
        String payload = JSON.toJSONString(data);
        log.info("收到交易授权回调: data={}", payload);

        // 验签
        if (!verifyWebhookSignature(request, payload))
        {
            log.error("交易授权回调验签失败，返回 401");
            throw new WebhookAuthenticationException("验签失败");
        }

        // 入队处理
        boolean queued = webhookService.enqueueWebhook("AUTH_TRANSACTION", payload, signature, data);
        if (!queued)
        {
            log.error("交易授权回调入队失败，返回 500");
            throw new WebhookProcessingException("处理失败，请重试");
        }

        return "ok";
    }

    /**
     * 卡片操作回调接口
     * VCC-002: 卡片操作结果通知（开卡、激活、冻结、注销等）
     */
    @PostMapping("/cardOperate")
    public String cardOperate(@RequestBody Map<String, Object> data,
                              @RequestHeader(value = "x-yop-signature", required = false) String signature,
                              HttpServletRequest request)
    {
        String payload = JSON.toJSONString(data);
        log.info("收到卡片操作回调: data={}", payload);

        // 验签
        if (!verifyWebhookSignature(request, payload))
        {
            log.error("卡片操作回调验签失败，返回 401");
            throw new WebhookAuthenticationException("验签失败");
        }

        // 入队处理
        boolean queued = webhookService.enqueueWebhook("CARD_OPERATION", payload, signature, data);
        if (!queued)
        {
            log.error("卡片操作回调入队失败，返回 500");
            throw new WebhookProcessingException("处理失败，请重试");
        }

        return "ok";
    }

    /**
     * Webhook 签名验证 — HMAC-SHA256
     * 
     * 算法：
     *   1. 从请求头读取 X-Webhook-Timestamp 和 X-Webhook-Signature（v1=xxxx 格式）
     *   2. 检查 timestamp 是否在 ±300 秒窗口内（防重放）
     *   3. 拼接 payload = timestamp + "." + body
     *   4. 使用 HMAC-SHA256(secret, payload) 生成签名，与请求头中的签名比对
     * 
     * @param request HTTP请求
     * @param body 请求体原始字符串
     * @return 验签结果
     */
    private boolean verifyWebhookSignature(HttpServletRequest request, String body)
    {
        // 从请求头获取签名相关参数
        String timestamp = request.getHeader("X-Webhook-Timestamp");
        String signatureHeader = request.getHeader("X-Webhook-Signature");
        
        log.debug("Webhook验签参数: timestamp={}, signature={}", timestamp, signatureHeader);
        
        // 1. 参数完整性检查
        if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(signatureHeader))
        {
            log.warn("Webhook验签失败: 缺少签名参数 timestamp={}, signature={}", timestamp, signatureHeader);
            return false;
        }
        
        // 2. 解析签名（格式：v1=hex_string）
        if (!signatureHeader.startsWith("v1="))
        {
            log.warn("Webhook验签失败: 签名格式错误，期望 v1=xxx，实际: {}", signatureHeader);
            return false;
        }
        String expectedSignature = signatureHeader.substring(3);
        
        // 3. 检查时间戳窗口（防重放攻击）
        long requestTimestamp;
        try
        {
            requestTimestamp = Long.parseLong(timestamp);
        }
        catch (NumberFormatException e)
        {
            log.warn("Webhook验签失败: 时间戳格式错误: {}", timestamp);
            return false;
        }
        
        long currentTimestamp = System.currentTimeMillis() / 1000;
        long timeDiff = Math.abs(currentTimestamp - requestTimestamp);
        if (timeDiff > TIMESTAMP_TOLERANCE_SECONDS)
        {
            log.warn("Webhook验签失败: 时间戳超出容忍窗口 diff={}s, tolerance={}s, reqTs={}, curTs={}",
                    timeDiff, TIMESTAMP_TOLERANCE_SECONDS, requestTimestamp, currentTimestamp);
            return false;
        }
        
        // 4. 获取 webhook secret
        String webhookSecret = sysConfigService.selectConfigByKey(CONFIG_KEY_WEBHOOK_SECRET);
        if (StringUtils.isBlank(webhookSecret))
        {
            log.error("Webhook验签失败: 未配置签名密钥 config_key={}", CONFIG_KEY_WEBHOOK_SECRET);
            return false;
        }
        
        // 5. 计算 HMAC-SHA256 签名
        try
        {
            String signPayload = timestamp + "." + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(signPayload.getBytes(StandardCharsets.UTF_8));
            
            // 转十六进制
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash)
            {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String computedSignature = hexString.toString();
            
            // 6. 恒时比较（防时序攻击）
            boolean valid = constantTimeEquals(computedSignature, expectedSignature);
            if (!valid)
            {
                log.warn("Webhook验签失败: 签名不匹配 computed={}, expected={}", computedSignature, expectedSignature);
            }
            return valid;
        }
        catch (Exception e)
        {
            log.error("Webhook验签异常", e);
            return false;
        }
    }

    /**
     * 恒时字符串比较（防时序攻击）
     */
    private boolean constantTimeEquals(String a, String b)
    {
        if (a == null || b == null || a.length() != b.length())
        {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++)
        {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * 根据回调数据判断事件类型
     * 事件类型定义来源：接口契约文档 §3.1
     *   OTP               — OTP验证码通知
     *   TRANSACTION        — 交易通知
     *   CARD_STATUS_CHANGE — 卡片状态变更
     *   TOPUP_RESULT       — 充值结果通知
     */
    private String resolveWebhookType(Map<String, Object> data)
    {
        // 包含 otp 字段 → OTP 验证码通知（文档定义: OTP）
        if (data.containsKey("otp"))
        {
            return "OTP";
        }
        // 包含 tranId 字段 → 交易通知
        if (data.containsKey("tranId"))
        {
            return "TRANSACTION";
        }
        // 包含 operateType 或 cardStatus 字段 → 卡状态变更
        if (data.containsKey("operateType") || data.containsKey("cardStatus"))
        {
            return "CARD_STATUS_CHANGE";
        }
        // 包含 orderNo + rechargeType 或单独有充值相关字段 → 充值结果
        // 注：文档定义为 TOPUP_RESULT（§3.1），此处内容检测返回 RECHARGE_RESULT，
        // dispatchWebhook 已同时支持两种写法
        if (data.containsKey("orderNo") && (data.containsKey("rechargeType") || data.containsKey("rechargeAmount")))
        {
            return "RECHARGE_RESULT";
        }
        // 兜底：使用 data 中显式的 type 字段
        Object type = data.get("type");
        if (type != null)
        {
            return type.toString();
        }
        return "UNKNOWN";
    }
}
