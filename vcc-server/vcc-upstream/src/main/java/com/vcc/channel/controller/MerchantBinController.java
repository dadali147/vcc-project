package com.vcc.channel.controller;

import com.vcc.channel.domain.VccMerchantBin;
import com.vcc.channel.service.IMerchantBinService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商户BIN分配 Controller（仅管理端）
 */
@RestController
@RequestMapping("/admin/merchantbin")
public class MerchantBinController extends BaseController
{
    @Autowired
    private IMerchantBinService merchantBinService;

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:list')")
    @GetMapping("/list")
    public TableDataInfo list(VccMerchantBin query)
    {
        startPage();
        List<VccMerchantBin> list = merchantBinService.listMerchantBins(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:list')")
    @GetMapping("/merchant/{merchantId}")
    public AjaxResult listByMerchant(@PathVariable Long merchantId)
    {
        return success(merchantBinService.listByMerchantId(merchantId));
    }

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(merchantBinService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:add')")
    @PostMapping("/assign")
    public AjaxResult assign(@RequestBody Map<String, Object> params)
    {
        Long merchantId = Long.valueOf(params.get("merchantId").toString());
        Long binId = Long.valueOf(params.get("binId").toString());
        String remark = params.containsKey("remark") ? params.get("remark").toString() : null;
        return toAjax(merchantBinService.assignBin(merchantId, binId, getUserId(), remark));
    }

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:edit')")
    @PutMapping("/{id}/revoke")
    public AjaxResult revoke(@PathVariable Long id)
    {
        return toAjax(merchantBinService.revokeBin(id, getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:edit')")
    @PutMapping("/{id}/enable")
    public AjaxResult enable(@PathVariable Long id)
    {
        return toAjax(merchantBinService.enableAssignment(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:merchantbin:edit')")
    @PutMapping("/{id}/disable")
    public AjaxResult disable(@PathVariable Long id)
    {
        return toAjax(merchantBinService.disableAssignment(id));
    }
}
