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
@RequestMapping("/auth")
public class MerchantAuthController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody SysUser user)
    {
        int result = userService.insertUser(user);
        return toAjax(result);
    }

    @PostMapping("/send-reset-code")
    public AjaxResult sendResetCode(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");
        return success();
    }

    @PostMapping("/reset-password")
    public AjaxResult resetPassword(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");
        return success();
    }

    @GetMapping("/profile")
    public AjaxResult getProfile()
    {
        Long userId = getUserId();
        SysUser user = userService.selectUserById(userId);
        return success(user);
    }

    @PostMapping("/logout")
    public AjaxResult logout()
    {
        return success();
    }

    @PostMapping("/refresh")
    public AjaxResult refreshToken()
    {
        Map<String, String> result = new HashMap<>();
        result.put("token", "new_token");
        return success(result);
    }
}
