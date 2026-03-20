package com.vcc.channel.controller;

import com.vcc.channel.domain.VccCardBin;
import com.vcc.channel.service.ICardBinService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 卡BIN管理 Controller（仅管理端）
 */
@RestController
@RequestMapping("/admin/cardbin")
public class CardBinController extends BaseController
{
    @Autowired
    private ICardBinService cardBinService;

    @PreAuthorize("@ss.hasPermi('channel:cardbin:list')")
    @GetMapping("/list")
    public TableDataInfo list(VccCardBin query)
    {
        startPage();
        List<VccCardBin> list = cardBinService.listCardBins(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(cardBinService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:query')")
    @GetMapping("/channel/{channelId}")
    public AjaxResult listByChannel(@PathVariable Long channelId)
    {
        return success(cardBinService.listByChannelId(channelId));
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:add')")
    @PostMapping
    public AjaxResult add(@RequestBody VccCardBin cardBin)
    {
        return toAjax(cardBinService.createCardBin(cardBin));
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody VccCardBin cardBin)
    {
        return toAjax(cardBinService.updateCardBin(cardBin));
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:edit')")
    @PutMapping("/{id}/enable")
    public AjaxResult enable(@PathVariable Long id)
    {
        return toAjax(cardBinService.enableCardBin(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:edit')")
    @PutMapping("/{id}/disable")
    public AjaxResult disable(@PathVariable Long id)
    {
        return toAjax(cardBinService.disableCardBin(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:cardbin:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(cardBinService.deleteCardBin(id));
    }
}
