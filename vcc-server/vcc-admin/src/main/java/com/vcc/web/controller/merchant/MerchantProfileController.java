package com.vcc.web.controller.merchant;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.system.service.ISysUserService;

@RestController
@RequestMapping("/profile")
public class MerchantProfileController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @GetMapping
    public AjaxResult get()
    {
        Long userId = getUserId();
        SysUser user = userService.selectUserById(userId);
        return success(user);
    }

    @PutMapping
    public AjaxResult update(@RequestBody SysUser user)
    {
        user.setUserId(getUserId());
        return toAjax(userService.updateUser(user));
    }

    @PostMapping("/change-password")
    public AjaxResult changePassword(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return success();
    }

    @PutMapping("/notifications")
    public AjaxResult updateNotifications(@RequestBody Map<String, Object> params)
    {
        return success();
    }
}
