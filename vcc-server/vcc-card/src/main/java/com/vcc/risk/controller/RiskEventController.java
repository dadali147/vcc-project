package com.vcc.risk.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.risk.domain.RiskEvent;
import com.vcc.risk.service.IRiskEventService;

/**
 * 风控事件 Controller
 */
@RestController
@RequestMapping("/admin/risk/event")
public class RiskEventController extends BaseController
{
    @Autowired
    private IRiskEventService riskEventService;

    /**
     * 查询风控事件列表
     */
    @PreAuthorize("@ss.hasPermi('risk:event:list')")
    @GetMapping("/list")
    public TableDataInfo list(RiskEvent riskEvent)
    {
        startPage();
        List<RiskEvent> list = riskEventService.selectRiskEventList(riskEvent);
        return getDataTable(list);
    }

    /**
     * 获取风控事件详情
     */
    @PreAuthorize("@ss.hasPermi('risk:event:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(riskEventService.selectRiskEventById(id));
    }

    /**
     * 处理风控事件
     */
    @PreAuthorize("@ss.hasPermi('risk:event:handle')")
    @Log(title = "风控事件", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/handle")
    public AjaxResult handle(@PathVariable Long id, @RequestBody Map<String, Object> params)
    {
        String handleResult = params.get("handleResult") != null ? params.get("handleResult").toString() : "";
        String status = params.get("status") != null ? params.get("status").toString() : "";
        return toAjax(riskEventService.handleRiskEvent(id, getUserId(), handleResult, status));
    }
}
