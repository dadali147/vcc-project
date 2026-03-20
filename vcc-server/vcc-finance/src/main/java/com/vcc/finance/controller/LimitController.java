package com.vcc.finance.controller;

import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.enums.BusinessType;
import com.vcc.finance.domain.LimitHistory;
import com.vcc.finance.service.ILimitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 限额管理 Controller
 */
@RestController
@RequestMapping("/merchant/v3/limit")
public class LimitController extends BaseController
{
    private final ILimitService limitService;

    public LimitController(ILimitService limitService)
    {
        this.limitService = limitService;
    }

    @PreAuthorize("@ss.hasPermi('finance:limit:query')")
    @GetMapping("/card/{cardId}/history")
    public AjaxResult getHistory(@PathVariable Long cardId)
    {
        return success(limitService.getHistoryByCard(getUserId(), cardId));
    }

    @PreAuthorize("@ss.hasPermi('finance:limit:edit')")
    @Log(title = "预算卡额度调整", businessType = BusinessType.UPDATE)
    @PostMapping("/card/{cardId}/adjust")
    public AjaxResult adjust(@PathVariable Long cardId, @RequestBody Map<String, Object> params)
    {
        String limitType = params.getOrDefault("limitType", LimitHistory.LIMIT_TYPE_SINGLE).toString();
        BigDecimal newAmount = new BigDecimal(params.get("newAmount").toString());
        String reason = params.getOrDefault("reason", "").toString();

        LimitHistory history = limitService.adjustLimit(getUserId(), cardId, limitType,
                newAmount, getUserId(), reason);
        return success(history);
    }
}
