package com.vcc.card.controller.admin;

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
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.service.IFeeConfigService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;

/**
 * 费率配置 Controller
 */
@RestController
@RequestMapping("/admin/fee-config")
public class FeeConfigController extends BaseController
{
    @Autowired
    private IFeeConfigService feeConfigService;

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(FeeConfig feeConfig)
    {
        startPage();
        List<FeeConfig> list = feeConfigService.selectFeeConfigList(feeConfig);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(feeConfigService.selectFeeConfigById(id));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/type/{feeType}")
    public AjaxResult getByFeeType(@PathVariable String feeType)
    {
        return success(feeConfigService.selectFeeConfigByFeeType(feeType));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping
    public AjaxResult add(@RequestBody FeeConfig feeConfig)
    {
        return toAjax(feeConfigService.insertFeeConfig(feeConfig));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @PutMapping
    public AjaxResult edit(@RequestBody FeeConfig feeConfig)
    {
        return toAjax(feeConfigService.updateFeeConfig(feeConfig));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(feeConfigService.deleteFeeConfigByIds(ids));
    }
}
