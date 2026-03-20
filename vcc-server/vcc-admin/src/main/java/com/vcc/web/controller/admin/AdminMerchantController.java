package com.vcc.web.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.domain.entity.SysRole;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.common.utils.SecurityUtils;
import com.vcc.system.service.ISysRoleService;
import com.vcc.system.service.ISysUserService;

/**
 * 管理端-商户KYC与注册审批 Controller
 *
 * 提供商户管理的后端审批业务逻辑，包括：
 * - 商户列表查询、详情查看
 * - KYC 审批流程（List -> Review -> Approve/Reject）
 * - 手工创建并激活子运营账号
 * - 商户角色与运营角色隔离
 *
 * 角色约定：
 * - merchant: 商户主账号角色
 * - operator: 商户子运营账号角色
 * - admin: 系统管理员角色
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/merchant")
public class AdminMerchantController extends BaseController
{
    /** 商户角色 roleKey */
    private static final String ROLE_KEY_MERCHANT = "merchant";
    /** 运营角色 roleKey */
    private static final String ROLE_KEY_OPERATOR = "operator";

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    // ==================== 商户列表与审批 ====================

    /**
     * 商户列表查询（分页）
     * 列出所有商户角色的用户
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
     * 查询商户详情（含角色信息）
     *
     * @param userId 商户用户ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("商户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roleService.selectRolesByUserId(userId));
        result.put("roleGroup", userService.selectUserRoleGroup(user.getUserName()));
        return success(result);
    }

    /**
     * KYC 审核 - 通过
     * 将商户状态设为正常（status=0），并在 remark 中记录审核信息
     *
     * @param userId 商户用户ID
     * @param params 包含: remark(审核备注)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-KYC审核通过", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/approve")
    public AjaxResult approve(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("商户不存在");
        }

        String remark = params.get("remark");

        // 设置状态为正常
        SysUser updateUser = new SysUser();
        updateUser.setUserId(userId);
        updateUser.setStatus("0"); // 0=正常
        updateUser.setRemark("KYC审核通过" + (remark != null ? " - " + remark : ""));
        updateUser.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(updateUser));
    }

    /**
     * KYC 审核 - 拒绝
     * 将商户状态设为停用（status=1），并记录拒绝原因
     *
     * @param userId 商户用户ID
     * @param params 包含: reason(拒绝原因, 必填)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-KYC审核拒绝", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/reject")
    public AjaxResult reject(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("商户不存在");
        }

        String reason = params.get("reason");
        if (reason == null || reason.trim().isEmpty())
        {
            return error("拒绝原因不能为空");
        }

        SysUser updateUser = new SysUser();
        updateUser.setUserId(userId);
        updateUser.setStatus("1"); // 1=停用
        updateUser.setRemark("KYC审核拒绝: " + reason);
        updateUser.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(updateUser));
    }

    /**
     * 商户启用/禁用
     *
     * @param userId 商户用户ID
     * @param params 包含: status(0=正常, 1=停用)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-修改状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/status")
    public AjaxResult changeStatus(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        String status = params.get("status");
        if (status == null || (!"0".equals(status) && !"1".equals(status)))
        {
            return error("状态参数无效，仅支持 0(正常) 或 1(停用)");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    // ==================== 子运营账号管理 ====================

    /**
     * 手工创建子运营账号
     * 创建一个新用户并分配 operator 角色，关联到指定商户
     *
     * @param params 包含: userName(登录名), nickName(昵称), password(密码),
     *               email(邮箱, 可选), phonenumber(手机号, 可选),
     *               parentMerchantId(所属商户ID, 可选, 用于 deptId 关联)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-创建子运营账号", businessType = BusinessType.INSERT)
    @PostMapping("/operator/create")
    public AjaxResult createOperator(@RequestBody Map<String, Object> params)
    {
        String userName = (String) params.get("userName");
        String nickName = (String) params.get("nickName");
        String password = (String) params.get("password");

        if (userName == null || userName.trim().isEmpty())
        {
            return error("登录名不能为空");
        }
        if (password == null || password.trim().isEmpty())
        {
            return error("密码不能为空");
        }

        // 校验用户名唯一性
        SysUser checkUser = new SysUser();
        checkUser.setUserName(userName);
        if (!userService.checkUserNameUnique(checkUser))
        {
            return error("新增用户'" + userName + "'失败，登录账号已存在");
        }

        // 构建新用户
        SysUser newUser = new SysUser();
        newUser.setUserName(userName);
        newUser.setNickName(nickName != null ? nickName : userName);
        newUser.setPassword(SecurityUtils.encryptPassword(password));
        newUser.setStatus("0"); // 直接激活
        newUser.setCreateBy(getUsername());

        if (params.get("email") != null)
        {
            newUser.setEmail((String) params.get("email"));
        }
        if (params.get("phonenumber") != null)
        {
            newUser.setPhonenumber((String) params.get("phonenumber"));
        }
        if (params.get("parentMerchantId") != null)
        {
            newUser.setDeptId(Long.valueOf(params.get("parentMerchantId").toString()));
        }

        // 查找 operator 角色
        Long operatorRoleId = findRoleIdByKey(ROLE_KEY_OPERATOR);
        if (operatorRoleId != null)
        {
            newUser.setRoleIds(new Long[]{operatorRoleId});
        }

        int rows = userService.insertUser(newUser);
        if (rows > 0)
        {
            // 如果角色不存在，尝试创建后再分配
            if (operatorRoleId == null)
            {
                operatorRoleId = ensureOperatorRole();
                if (operatorRoleId != null)
                {
                    userService.insertUserAuth(newUser.getUserId(), new Long[]{operatorRoleId});
                }
            }
        }
        return toAjax(rows);
    }

    /**
     * 查询指定商户下的子运营账号列表（分页）
     * 通过 deptId 关联查找子运营
     *
     * @param merchantId 商户用户ID（作为 deptId 过滤）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/operator/list/{merchantId}")
    public TableDataInfo listOperators(@PathVariable Long merchantId)
    {
        startPage();
        SysUser query = new SysUser();
        query.setDeptId(merchantId);
        List<SysUser> list = userService.selectUserList(query);
        return getDataTable(list);
    }

    /**
     * 激活/禁用子运营账号
     *
     * @param userId 子运营用户ID
     * @param params 包含: status(0=正常, 1=停用)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-子运营账号状态变更", businessType = BusinessType.UPDATE)
    @PutMapping("/operator/{userId}/status")
    public AjaxResult changeOperatorStatus(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        String status = params.get("status");
        if (status == null || (!"0".equals(status) && !"1".equals(status)))
        {
            return error("状态参数无效，仅支持 0(正常) 或 1(停用)");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 重置子运营账号密码
     *
     * @param userId 子运营用户ID
     * @param params 包含: password(新密码)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-重置运营密码", businessType = BusinessType.UPDATE)
    @PutMapping("/operator/{userId}/reset-pwd")
    public AjaxResult resetOperatorPwd(@PathVariable Long userId, @RequestBody Map<String, String> params)
    {
        String password = params.get("password");
        if (password == null || password.trim().isEmpty())
        {
            return error("新密码不能为空");
        }
        return toAjax(userService.resetUserPwd(userId, SecurityUtils.encryptPassword(password)));
    }

    // ==================== 角色管理辅助 ====================

    /**
     * 查询系统中的商户与运营角色信息
     * 返回 merchant 和 operator 角色的详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/roles")
    public AjaxResult getRoles()
    {
        Map<String, Object> result = new HashMap<>();
        List<SysRole> allRoles = roleService.selectRoleAll();

        SysRole merchantRole = allRoles.stream()
                .filter(r -> ROLE_KEY_MERCHANT.equals(r.getRoleKey()))
                .findFirst().orElse(null);
        SysRole operatorRole = allRoles.stream()
                .filter(r -> ROLE_KEY_OPERATOR.equals(r.getRoleKey()))
                .findFirst().orElse(null);

        result.put("merchantRole", merchantRole);
        result.put("operatorRole", operatorRole);
        result.put("allRoles", allRoles);
        return success(result);
    }

    /**
     * 给用户分配角色
     *
     * @param userId 用户ID
     * @param params 包含: roleIds(角色ID数组)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "商户管理-分配角色", businessType = BusinessType.GRANT)
    @PutMapping("/{userId}/assign-roles")
    public AjaxResult assignRoles(@PathVariable Long userId, @RequestBody Map<String, Object> params)
    {
        Object roleIdsObj = params.get("roleIds");
        if (roleIdsObj == null)
        {
            return error("角色ID列表不能为空");
        }

        @SuppressWarnings("unchecked")
        List<Number> roleIdList = (List<Number>) roleIdsObj;
        Long[] roleIds = roleIdList.stream().map(Number::longValue).toArray(Long[]::new);

        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    // ==================== 私有方法 ====================

    /**
     * 根据 roleKey 查找角色ID
     */
    private Long findRoleIdByKey(String roleKey)
    {
        List<SysRole> allRoles = roleService.selectRoleAll();
        return allRoles.stream()
                .filter(r -> roleKey.equals(r.getRoleKey()))
                .map(SysRole::getRoleId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 确保 operator 角色存在，如不存在则自动创建
     *
     * @return operator 角色ID
     */
    private Long ensureOperatorRole()
    {
        Long roleId = findRoleIdByKey(ROLE_KEY_OPERATOR);
        if (roleId != null)
        {
            return roleId;
        }

        // 自动创建 operator 角色
        SysRole role = new SysRole();
        role.setRoleName("运营员");
        role.setRoleKey(ROLE_KEY_OPERATOR);
        role.setRoleSort(10);
        role.setStatus("0"); // 正常状态
        role.setCreateBy(getUsername());
        int rows = roleService.insertRole(role);
        return rows > 0 ? role.getRoleId() : null;
    }
}
