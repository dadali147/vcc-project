package com.vcc.channel.controller;

import com.vcc.channel.domain.VccChannel;
import com.vcc.channel.service.IChannelService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 渠道管理 Controller（仅管理端）
 */
@RestController
@RequestMapping("/admin/channel")
public class ChannelController extends BaseController
{
    @Autowired
    private IChannelService channelService;

    @PreAuthorize("@ss.hasPermi('channel:channel:list')")
    @GetMapping("/list")
    public TableDataInfo list(VccChannel query)
    {
        startPage();
        List<VccChannel> list = channelService.listChannels(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('channel:channel:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(channelService.getById(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:channel:add')")
    @PostMapping
    public AjaxResult add(@RequestBody VccChannel channel)
    {
        return toAjax(channelService.createChannel(channel));
    }

    @PreAuthorize("@ss.hasPermi('channel:channel:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody VccChannel channel)
    {
        return toAjax(channelService.updateChannel(channel));
    }

    @PreAuthorize("@ss.hasPermi('channel:channel:edit')")
    @PutMapping("/{id}/enable")
    public AjaxResult enable(@PathVariable Long id)
    {
        return toAjax(channelService.enableChannel(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:channel:edit')")
    @PutMapping("/{id}/disable")
    public AjaxResult disable(@PathVariable Long id)
    {
        return toAjax(channelService.disableChannel(id));
    }

    @PreAuthorize("@ss.hasPermi('channel:channel:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(channelService.deleteChannel(id));
    }
}
