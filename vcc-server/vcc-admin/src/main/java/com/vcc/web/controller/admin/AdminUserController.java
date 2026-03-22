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
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        // 返回包含 KYC 信息的完整用户对象
        // kycStatus / kycRejectReason / kycReviewTime / kycReviewer / kycSubmitTime 已在 SysUser 实体中
        return success(user);
    }

    /**
     * KYC 审核
     * 状态流转（对齐状态字典 01.3 kycStatus）：
     *   SUBMITTED / UNDER_REVIEW → APPROVED（通过）
     *   SUBMITTED / UNDER_REVIEW → REJECTED（拒绝，必须提供 rejectReason）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "用户管理-KYC审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/audit")
    public AjaxResult audit(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        String result = params.get("result"); // "approve" or "reject"
        String remark = params.get("remark");
        if (result == null || (!"approve".equalsIgnoreCase(result) && !"reject".equalsIgnoreCase(result)))
        {
            return error("审核结果参数错误，必须为 approve 或 reject");
        }
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("用户不存在");
        }
        // 仅允许从 SUBMITTED / UNDER_REVIEW 状态发起审核
        String currentKycStatus = user.getKycStatus();
        if (!"SUBMITTED".equals(currentKycStatus) && !"UNDER_REVIEW".equals(currentKycStatus))
        {
            return error("当前 KYC 状态不允许审核操作，当前状态：" + (currentKycStatus != null ? currentKycStatus : "未提交"));
        }
        if ("approve".equalsIgnoreCase(result))
        {
            user.setKycStatus("APPROVED");
            user.setKycReviewer(getUsername());
            user.setKycReviewTime(new java.util.Date());
            user.setKycRejectReason(null);
        }
        else
        {
            if (remark == null || remark.trim().isEmpty())
            {
                return error("拒绝审核必须提供拒绝原因");
            }
            user.setKycStatus("REJECTED");
            user.setKycReviewer(getUsername());
            user.setKycReviewTime(new java.util.Date());
            user.setKycRejectReason(remark);
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
