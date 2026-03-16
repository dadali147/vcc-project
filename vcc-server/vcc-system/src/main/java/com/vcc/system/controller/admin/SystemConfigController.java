package com.vcc.system.controller.admin;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.system.domain.SystemConfig;
import com.vcc.system.service.ISystemConfigService;

/**
 * 系统配置 Controller
 */
@RestController
@RequestMapping("/admin/system-config")
public class SystemConfigController extends BaseController
{
    @Autowired
    private ISystemConfigService systemConfigService;

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(SystemConfig systemConfig)
    {
        startPage();
        List<SystemConfig> list = systemConfigService.selectSystemConfigList(systemConfig);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(systemConfigService.selectSystemConfigById(id));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/key")
    public AjaxResult getByKey(@RequestParam String configKey)
    {
        return success(systemConfigService.get(configKey));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/set")
    public AjaxResult setValue(@RequestParam String configKey, @RequestParam String configValue)
    {
        return toAjax(systemConfigService.set(configKey, configValue));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping
    public AjaxResult add(@RequestBody SystemConfig systemConfig)
    {
        return toAjax(systemConfigService.insertSystemConfig(systemConfig));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PutMapping
    public AjaxResult edit(@RequestBody SystemConfig systemConfig)
    {
        return toAjax(systemConfigService.updateSystemConfig(systemConfig));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        return toAjax(systemConfigService.deleteSystemConfigById(id));
    }
}
