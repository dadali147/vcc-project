package com.vcc.web.controller.admin;

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
import com.vcc.system.constant.SystemConfigConstants;
import com.vcc.system.domain.SysConfig;
import com.vcc.system.service.ISysConfigService;

/**
 * 管理端-系统配置 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/config")
public class AdminConfigController extends BaseController
{
    @Autowired
    private ISysConfigService configService;

    /**
     * 查询系统配置列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(SysConfig config)
    {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        return getDataTable(list);
    }

    /**
     * 查询单个配置值
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{configKey}")
    public AjaxResult getByKey(@PathVariable String configKey)
    {
        return success(configService.selectConfigByKey(configKey));
    }

    /**
     * 更新配置值
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "系统配置-更新", businessType = BusinessType.UPDATE)
    @PutMapping("/{configKey}")
    public AjaxResult updateByKey(@PathVariable String configKey, @RequestBody Map<String, String> params)
    {
        // 白名单校验
        if (!SystemConfigConstants.isAllowedKey(configKey))
        {
            return AjaxResult.error(400, "不允许修改该配置项");
        }
        String configValue = params.get("configValue");
        // value 格式校验
        String validateMsg = SystemConfigConstants.validateValue(configKey, configValue);
        if (validateMsg != null)
        {
            return AjaxResult.error(400, validateMsg);
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        config.setConfigValue(configValue);
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateConfig(config));
    }
}
