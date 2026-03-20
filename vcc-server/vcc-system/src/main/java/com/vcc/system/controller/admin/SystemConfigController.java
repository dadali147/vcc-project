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
import com.vcc.system.constant.SystemConfigConstants;
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
        // 白名单校验
        if (!SystemConfigConstants.isAllowedKey(configKey))
        {
            return AjaxResult.error(400, "不允许修改该配置项");
        }
        // value 格式校验
        String validateMsg = SystemConfigConstants.validateValue(configKey, configValue);
        if (validateMsg != null)
        {
            return AjaxResult.error(400, validateMsg);
        }
        return toAjax(systemConfigService.set(configKey, configValue));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping
    public AjaxResult add(@RequestBody SystemConfig systemConfig)
    {
        // 白名单校验
        if (!SystemConfigConstants.isAllowedKey(systemConfig.getConfigKey()))
        {
            return AjaxResult.error(400, "不允许修改该配置项");
        }
        // value 格式校验
        String validateMsg = SystemConfigConstants.validateValue(systemConfig.getConfigKey(), systemConfig.getConfigValue());
        if (validateMsg != null)
        {
            return AjaxResult.error(400, validateMsg);
        }
        return toAjax(systemConfigService.insertSystemConfig(systemConfig));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PutMapping
    public AjaxResult edit(@RequestBody SystemConfig systemConfig)
    {
        // 白名单校验
        if (!SystemConfigConstants.isAllowedKey(systemConfig.getConfigKey()))
        {
            return AjaxResult.error(400, "不允许修改该配置项");
        }
        // value 格式校验
        String validateMsg = SystemConfigConstants.validateValue(systemConfig.getConfigKey(), systemConfig.getConfigValue());
        if (validateMsg != null)
        {
            return AjaxResult.error(400, validateMsg);
        }
        return toAjax(systemConfigService.updateSystemConfig(systemConfig));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        // 删除前查询 configKey，校验白名单
        SystemConfig existing = systemConfigService.selectSystemConfigById(id);
        if (existing == null)
        {
            return AjaxResult.error(400, "配置项不存在");
        }
        if (!SystemConfigConstants.isAllowedKey(existing.getConfigKey()))
        {
            return AjaxResult.error(400, "不允许修改该配置项");
        }
        return toAjax(systemConfigService.deleteSystemConfigById(id));
    }
}
