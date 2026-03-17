package com.vcc.web.controller.admin;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.system.service.ISysUserService;

/**
 * 管理端-用户验证管理 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/user-auth")
public class AdminUserAuthController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    /**
     * 用户列表（含 2FA/3DS 状态）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        // TODO: Service层补全查询含2FA/3DS状态的用户列表方法
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 查询用户 2FA 详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}/2fa")
    public AjaxResult get2faInfo(@PathVariable Long userId)
    {
        // TODO: Service层补全查询用户2FA详情方法
        return success();
    }

    /**
     * 重置 2FA（管理员操作）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户验证管理-重置2FA", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{userId}/2fa")
    public AjaxResult reset2fa(@PathVariable Long userId)
    {
        // TODO: Service层补全重置2FA方法
        return success();
    }

    /**
     * 查询当前 3DS OTP（需 reason 参数，记录操作日志）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户验证管理-查询3DS OTP", businessType = BusinessType.OTHER)
    @GetMapping("/{userId}/3ds-otp")
    public AjaxResult get3dsOtp(@PathVariable Long userId, @RequestParam String reason)
    {
        // TODO: Service层补全查询3DS OTP方法，需记录操作日志含reason
        return success();
    }

    /**
     * 强制解绑 2FA
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户验证管理-强制解绑2FA", businessType = BusinessType.UPDATE)
    @PostMapping("/{userId}/force-unbind")
    public AjaxResult forceUnbind(@PathVariable Long userId)
    {
        // TODO: Service层补全强制解绑2FA方法
        return success();
    }
}
