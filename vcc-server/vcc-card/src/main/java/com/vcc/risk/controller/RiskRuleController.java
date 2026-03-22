package com.vcc.risk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.risk.domain.RiskRule;
import com.vcc.risk.service.IRiskRuleService;

/**
 * 风控规则 Controller
 */
@RestController
@RequestMapping("/admin/risk/rule")
public class RiskRuleController extends BaseController
{
    @Autowired
    private IRiskRuleService riskRuleService;

    /**
     * 查询风控规则列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(RiskRule riskRule)
    {
        startPage();
        List<RiskRule> list = riskRuleService.selectRiskRuleList(riskRule);
        return getDataTable(list);
    }

    /**
     * 获取风控规则详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(riskRuleService.selectRiskRuleById(id));
    }

    /**
     * 新增风控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "风控规则", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RiskRule riskRule)
    {
        return toAjax(riskRuleService.insertRiskRule(riskRule));
    }

    /**
     * 修改风控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "风控规则", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RiskRule riskRule)
    {
        return toAjax(riskRuleService.updateRiskRule(riskRule));
    }

    /**
     * 启用/禁用风控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "风控规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/toggle")
    public AjaxResult toggle(@PathVariable Long id)
    {
        return toAjax(riskRuleService.toggleRiskRule(id));
    }

    /**
     * 删除风控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "风控规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(riskRuleService.deleteRiskRuleByIds(ids));
    }
}
