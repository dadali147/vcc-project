package com.vcc.finance.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.service.IRechargeService;

/**
 * 充值 Controller
 */
@RestController
@RequestMapping("/merchant/recharge")
public class RechargeController extends BaseController
{
    @Autowired
    private IRechargeService rechargeService;

    @PreAuthorize("@ss.hasPermi('finance:recharge:list')")
    @GetMapping("/list")
    public TableDataInfo list(Recharge recharge)
    {
        startPage();
        List<Recharge> list = rechargeService.selectRechargeList(recharge);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('finance:recharge:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(rechargeService.selectRechargeById(id));
    }

    @Log(title = "充值提交", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('finance:recharge:add')")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> params)
    {
        Long cardId = Long.valueOf(params.get("cardId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = params.getOrDefault("currency", "USD").toString();
        // P1 修复：禁止前端传 fee，完全由服务端计算
        String orderNo = params.containsKey("orderNo") ? params.get("orderNo").toString() : null;

        Recharge recharge = rechargeService.submitRecharge(getUserId(), cardId, amount, currency, orderNo);
        return success(recharge);
    }

    @PreAuthorize("@ss.hasPermi('finance:recharge:query')")
    @GetMapping("/query/{orderNo}")
    public AjaxResult queryResult(@PathVariable String orderNo)
    {
        return success(rechargeService.queryRechargeResult(getUserId(), orderNo));
    }

    @Log(title = "USDT到账处理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('finance:recharge:add')")
    @PostMapping("/usdt-arrival")
    public AjaxResult usdtArrival(@RequestBody Map<String, Object> params)
    {
        // P0 修复：userId 只能从当前登录 token 获取，禁止前端传入
        Long userId = getUserId();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = params.getOrDefault("currency", "USD").toString();
        String txHash = params.getOrDefault("txHash", "").toString();

        Recharge recharge = rechargeService.handleUsdtArrival(userId, amount, currency, txHash);
        return success(recharge);
    }
}
