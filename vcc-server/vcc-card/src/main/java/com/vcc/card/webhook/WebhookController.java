package com.vcc.card.webhook;

import java.util.Map;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * YeeVCC Webhook 回调接收入口
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController
{
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private WebhookService webhookService;

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
     * VCC-002: 收到 OTP 回调后，将验证码保存并支持在商户端页面展示
     */
    @PostMapping("/otp")
    public String otp(@RequestBody Map<String, Object> data,
                      @RequestHeader(value = "x-yop-signature", required = false) String signature,
                      HttpServletRequest request)
    {
        String payload = JSON.toJSONString(data);
        log.info("收到3DS验证码回调: data={}", payload);

        // 验签
        if (!verifyWebhookSignature(request, payload))
        {
            log.error("3DS OTP回调验签失败，返回 401");
            throw new WebhookAuthenticationException("验签失败");
        }

        // 入队处理
        boolean queued = webhookService.enqueueWebhook("3DS_OTP", payload, signature, data);
        if (!queued)
        {
            log.error("3DS OTP回调入队失败，返回 500");
            throw new WebhookProcessingException("处理失败，请重试");
        }

        return "ok";
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
     * Webhook 签名验证（预留方法，后续补充完整逻辑）
     * 
     * @param request HTTP请求
     * @param body 请求体原始字符串
     * @return 验签结果（暂时始终返回 true）
     */
    private boolean verifyWebhookSignature(HttpServletRequest request, String body)
    {
        // 从请求头获取签名相关参数
        String timestamp = request.getHeader("X-Webhook-Timestamp");
        String signature = request.getHeader("X-Webhook-Signature");
        
        log.debug("Webhook验签参数: timestamp={}, signature={}", timestamp, signature);
        
        // TODO: 补充完整验签逻辑
        // 1. 检查 timestamp 是否在容忍窗口内（如 300 秒）
        // 2. 使用 webhookSecret 和 HMAC-SHA256 验证签名
        // 3. 参考: WebhookHmacV1.verifyV1Hex(webhookSecret, timestamp, body, signature, 300)
        
        // 暂时放行，后续补充完整验签逻辑
        return true;
    }

    /**
     * 根据回调数据判断事件类型
     */
    private String resolveWebhookType(Map<String, Object> data)
    {
        // 包含 otpCode 字段 → 3DS 验证码通知
        if (data.containsKey("otpCode"))
        {
            return "3DS_OTP";
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
