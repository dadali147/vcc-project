package com.vcc.web.controller.merchant;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.system.service.ISysUserService;
import com.vcc.web.controller.merchant.dto.UpdateProfileRequest;

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
    public AjaxResult update(@RequestBody UpdateProfileRequest req)
    {
        SysUser user = new SysUser();
        user.setUserId(getUserId());
        user.setNickName(req.getNickName());
        user.setEmail(req.getEmail());
        user.setPhonenumber(req.getPhonenumber());
        user.setSex(req.getSex());
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
