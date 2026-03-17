package com.vcc.web.controller.admin;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.service.IFeeConfigService;
import com.vcc.system.service.ISysUserService;

/**
 * 管理端-用户管理 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private IFeeConfigService feeConfigService;

    /**
     * 分页查询用户列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 查询用户详情（含 KYC 信息）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId)
    {
        // TODO: Service层补全KYC信息查询
        return success(userService.selectUserById(userId));
    }

    /**
     * KYC 审核
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户管理-KYC审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/audit")
    public AjaxResult audit(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        String result = params.get("result");
        String remark = params.get("remark");
        // TODO: Service层补全KYC审核方法 auditKyc(userId, result, remark)
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        user.setRemark(remark);
        return toAjax(userService.updateUser(user));
    }

    /**
     * 用户账号启用/禁用
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户管理-修改状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/status")
    public AjaxResult changeStatus(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        String status = params.get("status");
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 查询用户费率配置
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}/fee")
    public AjaxResult getFee(@PathVariable Long userId)
    {
        FeeConfig query = new FeeConfig();
        query.setUserId(userId);
        List<FeeConfig> list = feeConfigService.selectFeeConfigList(query);
        return success(list);
    }

    /**
     * 更新用户费率配置
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户管理-更新费率", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/fee")
    public AjaxResult updateFee(@PathVariable Long userId, @RequestBody FeeConfig feeConfig)
    {
        feeConfig.setUserId(userId);
        return toAjax(feeConfigService.updateFeeConfig(feeConfig));
    }

    /**
     * 重置用户 2FA
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户管理-重置2FA", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{userId}/2fa")
    public AjaxResult reset2FA(@PathVariable Long userId)
    {
        // TODO: Service层补全重置2FA方法
        return success();
    }
}
