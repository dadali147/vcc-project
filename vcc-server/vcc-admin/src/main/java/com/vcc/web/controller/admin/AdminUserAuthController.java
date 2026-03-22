package com.vcc.web.controller.admin;

import java.util.HashMap;
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
 * 负责管理端 2FA / 3DS 相关的管理操作，包括：
 * - 查询用户 2FA/3DS 状态列表
 * - 查看 2FA 绑定详情
 * - 管理员强制重置/解绑 2FA
 * - 管理员紧急查询 3DS OTP（需审计）
 *
 * 依赖说明：
 * - SysUser 实体已扩展 twoFaEnabled / twoFaBindTime / twoFaDevice 字段
 * - 2FA 密钥存储需依赖 vcc-security 模块的 TOTP / HOTP 实现（待开发）
 * - 3DS OTP 数据来源于上游 YeeVCC 回调的 webhook 记录（待 vcc-webhook 模块落库）
 * - 所有敏感操作必须记录审计日志（@Log 注解 + SysOperLog）
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
     * 通过 SysUser 的 twoFaEnabled 字段反映 2FA 状态
     * 3DS 状态需后续从 webhook OTP 表关联查询
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
     * 查询用户 2FA 详情
     *
     * TODO: 依赖 vcc-security 模块 TOTP 实现
     * 当前返回 SysUser 中的 twoFaEnabled / twoFaBindTime / twoFaDevice
     * 后续需补充：密钥创建时间、备用恢复码数量、最近验证时间
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}/2fa")
    public AjaxResult get2faInfo(@PathVariable Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("enabled", "1".equals(user.getTwoFaEnabled()));
        info.put("bindTime", user.getTwoFaBindTime());
        info.put("device", user.getTwoFaDevice());
        // TODO: 补充密钥创建时间、恢复码数量、最近验证时间（依赖 vcc-security）
        return success(info);
    }

    /**
     * 重置 2FA（管理员操作）
     * 清除用户 2FA 绑定，用户下次登录需重新设置
     *
     * TODO: 依赖 vcc-security 模块
     * 当前实现：清除 SysUser.twoFaEnabled / twoFaBindTime / twoFaDevice
     * 后续需补充：清除 TOTP 密钥存储、生成新恢复码、通知用户
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户验证管理-重置2FA", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{userId}/2fa")
    public AjaxResult reset2fa(@PathVariable Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        user.setTwoFaEnabled("0");
        user.setTwoFaBindTime(null);
        user.setTwoFaDevice(null);
        // TODO: 调用 vcc-security 清除 TOTP 密钥 + 生成新恢复码
        return toAjax(userService.updateUser(user));
    }

    /**
     * 查询当前 3DS OTP（需 reason 参数，记录操作日志）
     *
     * TODO: 依赖 vcc-webhook 模块落库的 OTP 记录
     * 当前为占位实现，返回提示信息
     * 后续需从 webhook_otp 表查询最新有效 OTP：
     *   - 条件：user_id = userId AND expire_time > NOW() ORDER BY received_time DESC LIMIT 1
     *   - 必须记录操作日志含 reason（@Log 注解已覆盖）
     *   - 必须标记该 OTP 为已查看状态，避免重复查询
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户验证管理-查询3DS OTP", businessType = BusinessType.OTHER)
    @GetMapping("/{userId}/3ds-otp")
    public AjaxResult get3dsOtp(@PathVariable Long userId, @RequestParam String reason)
    {
        if (reason == null || reason.trim().isEmpty())
        {
            return error("查询原因不能为空，此操作将被记录审计日志");
        }
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        // TODO: 从 vcc-webhook 模块查询最新有效 OTP
        // 当前占位返回提示，待 webhook OTP 表就绪后补全
        Map<String, Object> placeholder = new HashMap<>();
        placeholder.put("status", "NOT_AVAILABLE");
        placeholder.put("message", "3DS OTP 查询功能待 webhook OTP 表就绪后补全");
        return success(placeholder);
    }

    /**
     * 强制解绑 2FA
     * 与 reset2fa 语义一致：清除 2FA 绑定，区别在于操作路径（DELETE vs POST）
     *
     * TODO: 依赖 vcc-security 模块，逻辑同 reset2fa
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户验证管理-强制解绑2FA", businessType = BusinessType.UPDATE)
    @PostMapping("/{userId}/force-unbind")
    public AjaxResult forceUnbind(@PathVariable Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        user.setTwoFaEnabled("0");
        user.setTwoFaBindTime(null);
        user.setTwoFaDevice(null);
        // TODO: 调用 vcc-security 清除 TOTP 密钥 + 生成新恢复码
        return toAjax(userService.updateUser(user));
    }
}
