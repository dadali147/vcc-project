package com.vcc.finance.controller;

import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.finance.domain.CardDebt;
import com.vcc.finance.service.IDebtService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 欠费管理 Controller
 */
@RestController
@RequestMapping("/merchant/v3/debt")
public class DebtController extends BaseController
{
    private final IDebtService debtService;

    public DebtController(IDebtService debtService)
    {
        this.debtService = debtService;
    }

    /**
     * 查询欠费记录列表
     */
    @PreAuthorize("@ss.hasPermi('finance:debt:list')")
    @GetMapping("/list")
    public TableDataInfo list(CardDebt cardDebt)
    {
        startPage();
        List<CardDebt> list = debtService.selectCardDebtList(cardDebt);
        return getDataTable(list);
    }

    /**
     * 查询欠费记录详情
     */
    @PreAuthorize("@ss.hasPermi('finance:debt:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(debtService.selectCardDebtById(id));
    }

    /**
     * 查询商户未结清欠费列表
     */
    @PreAuthorize("@ss.hasPermi('finance:debt:list')")
    @GetMapping("/outstanding/{merchantId}")
    public AjaxResult listOutstanding(@PathVariable Long merchantId)
    {
        return success(debtService.listOutstanding(merchantId));
    }

    /**
     * 查询卡片欠费历史
     */
    @PreAuthorize("@ss.hasPermi('finance:debt:list')")
    @GetMapping("/card/{cardId}")
    public AjaxResult listByCard(@PathVariable Long cardId)
    {
        return success(debtService.listByCard(cardId));
    }

    /**
     * 结清欠费
     */
    @PreAuthorize("@ss.hasPermi('finance:debt:edit')")
    @Log(title = "欠费结清", businessType = BusinessType.UPDATE)
    @PostMapping("/settle")
    public AjaxResult settle(@RequestBody Map<String, Object> params)
    {
        Long debtId = Long.valueOf(params.get("debtId").toString());
        BigDecimal settleAmount = new BigDecimal(params.get("settleAmount").toString());
        return toAjax(debtService.settle(debtId, settleAmount));
    }

    /**
     * 删除欠费记录
     */
    @PreAuthorize("@ss.hasPermi('finance:debt:remove')")
    @Log(title = "删除欠费记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(debtService.deleteCardDebtByIds(ids));
    }
}
