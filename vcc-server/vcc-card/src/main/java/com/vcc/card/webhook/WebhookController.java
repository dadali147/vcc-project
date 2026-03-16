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
     * 接收 YeeVCC 上游回调（秒回 200，异步处理业务）
     */
    @PostMapping("/yeevcc")
    public String yeevcc(@RequestBody Map<String, Object> data,
                         @RequestHeader(value = "x-yop-signature", required = false) String signature)
    {
        String webhookType = resolveWebhookType(data);
        String payload = JSON.toJSONString(data);

        log.info("收到YeeVCC回调: type={}, data={}", webhookType, payload);

        // 异步处理，立即返回
        webhookService.processWebhookAsync(webhookType, payload, signature, data);

        return "ok";
    }

    /**
     * 根据回调数据判断事件类型
     */
    private String resolveWebhookType(Map<String, Object> data)
    {
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
