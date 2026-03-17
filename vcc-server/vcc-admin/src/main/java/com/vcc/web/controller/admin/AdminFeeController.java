package com.vcc.web.controller.admin;

import java.util.List;
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
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.service.IFeeConfigService;

/**
 * 管理端-费率配置 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/fee")
public class AdminFeeController extends BaseController
{
    @Autowired
    private IFeeConfigService feeConfigService;

    /**
     * 分页查询用户费率配置列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/user/list")
    public TableDataInfo userFeeList(FeeConfig feeConfig)
    {
        startPage();
        // 查询用户级别费率（userId不为空的）
        List<FeeConfig> list = feeConfigService.selectFeeConfigList(feeConfig);
        return getDataTable(list);
    }

    /**
     * 更新用户费率配置
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "费率配置-更新用户费率", businessType = BusinessType.UPDATE)
    @PutMapping("/user/{userId}")
    public AjaxResult updateUserFee(@PathVariable Long userId, @RequestBody FeeConfig feeConfig)
    {
        feeConfig.setUserId(userId);
        return toAjax(feeConfigService.updateFeeConfig(feeConfig));
    }

    /**
     * 分页查询卡BIN费率列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/cardbin/list")
    public TableDataInfo cardBinFeeList(FeeConfig feeConfig)
    {
        startPage();
        // 查询卡BIN级别费率（cardBinId不为空的）
        List<FeeConfig> list = feeConfigService.selectFeeConfigList(feeConfig);
        return getDataTable(list);
    }

    /**
     * 更新卡BIN费率
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "费率配置-更新卡BIN费率", businessType = BusinessType.UPDATE)
    @PutMapping("/cardbin/{cardBinId}")
    public AjaxResult updateCardBinFee(@PathVariable String cardBinId, @RequestBody FeeConfig feeConfig)
    {
        feeConfig.setCardBinId(cardBinId);
        return toAjax(feeConfigService.updateFeeConfig(feeConfig));
    }
}
