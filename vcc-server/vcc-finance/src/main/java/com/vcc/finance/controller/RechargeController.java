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
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
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

    @PreAuthorize("@ss.hasPermi('finance:recharge:add')")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> params)
    {
        Long cardId = Long.valueOf(params.get("cardId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = params.getOrDefault("currency", "USD").toString();
        BigDecimal fee = params.containsKey("fee") ? new BigDecimal(params.get("fee").toString()) : BigDecimal.ZERO;

        Recharge recharge = rechargeService.submitRecharge(getUserId(), cardId, amount, currency, fee);
        return success(recharge);
    }

    @PreAuthorize("@ss.hasPermi('finance:recharge:query')")
    @GetMapping("/query/{orderNo}")
    public AjaxResult queryResult(@PathVariable String orderNo)
    {
        return success(rechargeService.queryRechargeResult(orderNo));
    }

    @PreAuthorize("@ss.hasPermi('finance:recharge:add')")
    @PostMapping("/usdt-arrival")
    public AjaxResult usdtArrival(@RequestBody Map<String, Object> params)
    {
        Long userId = Long.valueOf(params.get("userId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = params.getOrDefault("currency", "USD").toString();
        String txHash = params.getOrDefault("txHash", "").toString();

        Recharge recharge = rechargeService.handleUsdtArrival(userId, amount, currency, txHash);
        return success(recharge);
    }
}
