package com.vcc.web.controller.merchant;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.annotation.Anonymous;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.domain.entity.SysUser;
import com.vcc.common.core.domain.model.LoginUser;
import com.vcc.common.utils.DateUtils;
import com.vcc.common.utils.SecurityUtils;
import com.vcc.common.utils.ServletUtils;
import com.vcc.framework.service.EmailCodeService;
import com.vcc.framework.web.service.TokenService;
import com.vcc.system.service.ISysUserService;

/**
 * 商户认证接口
 * - POST /api/auth/send-code  发送注册验证码
 * - POST /api/auth/register   邮箱验证码注册
 * - POST /api/auth/login      邮箱密码登录
 * - POST /api/auth/logout     退出登录
 * - GET  /api/auth/profile    查看个人信息
 */
@RestController
@RequestMapping("/api/auth")
public class MerchantAuthController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(MerchantAuthController.class);

    // W1: 邮箱格式正则
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    // W4: 密码强度：最小8位，同时包含字母和数字
    private static final Pattern PASSWORD_LETTER = Pattern.compile("[a-zA-Z]");
    private static final Pattern PASSWORD_DIGIT  = Pattern.compile("[0-9]");

    @Autowired
    private EmailCodeService emailCodeService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    // W1: 公共邮箱格式校验（含 RFC 5321 长度限制）
    private boolean isValidEmail(String email)
    {
        return email != null && email.length() <= 254 && EMAIL_PATTERN.matcher(email).matches();
    }

    // W4: 密码强度校验
    private boolean isValidPassword(String password)
    {
        if (password == null || password.length() < 8 || password.length() > 20)
        {
            return false;
        }
        return PASSWORD_LETTER.matcher(password).find() && PASSWORD_DIGIT.matcher(password).find();
    }

    // 获取客户端真实IP（兼容反向代理）
    private String getClientIp(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip))
        {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip))
        {
            return ip;
        }
        return request.getRemoteAddr();
    }

    // 邮箱脱敏：a**@example.com
    private static String maskEmail(String email)
    {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 0) return "***";
        return email.charAt(0) + "**" + email.substring(at);
    }

    /**
     * 发送注册验证码
     * body: { "email": "xxx@example.com" }
     */
    @Anonymous
    @PostMapping("/send-code")
    public AjaxResult sendCode(@RequestBody Map<String, String> params, HttpServletRequest request)
    {
        String email = params.get("email");
        // W1: 邮箱格式校验（含长度）
        if (!isValidEmail(email))
        {
            return AjaxResult.error("邮箱格式不正确");
        }
        // IP 级别限流
        String ip = getClientIp(request);
        try
        {
            emailCodeService.checkIpRateLimit(ip);
        }
        catch (RuntimeException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        // S3/问题5: 不透露邮箱是否已注册，统一返回"已发送"提示
        SysUser checkUser = new SysUser();
        checkUser.setUserName(email);
        if (!userService.checkUserNameUnique(checkUser))
        {
            return AjaxResult.success("验证码已发送，如未收到请检查邮箱或重新获取");
        }
        try
        {
            emailCodeService.sendCode(email);
            return AjaxResult.success("验证码已发送，请查收邮件");
        }
        catch (RuntimeException e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 邮箱注册
     * body: { "email": "xxx@example.com", "code": "123456", "password": "your_password" }
     */
    @Anonymous
    @PostMapping("/register")
    public AjaxResult register(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");
        String code = params.get("code");
        String password = params.get("password");

        // W1: 邮箱格式校验
        if (!isValidEmail(email))
        {
            return AjaxResult.error("邮箱格式不正确");
        }
        if (code == null || code.isBlank())
        {
            return AjaxResult.error("验证码不能为空");
        }
        // W4: 密码强度校验
        if (!isValidPassword(password))
        {
            return AjaxResult.error("密码长度8-20位，且必须同时包含字母和数字");
        }

        // 校验验证码
        if (!emailCodeService.verifyCode(email, code))
        {
            return AjaxResult.error("验证码错误或已过期");
        }

        // 检查邮箱是否已注册（email 作为 userName）
        SysUser checkUser = new SysUser();
        checkUser.setUserName(email);
        if (!userService.checkUserNameUnique(checkUser))
        {
            return AjaxResult.error("该邮箱已被使用");
        }

        // 构建用户并注册
        SysUser sysUser = new SysUser();
        sysUser.setUserName(email);
        sysUser.setEmail(email);
        sysUser.setNickName(email);
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());

        boolean success = userService.registerUser(sysUser);
        if (!success)
        {
            return AjaxResult.error("注册失败，请联系管理员");
        }
        return AjaxResult.success("注册成功");
    }

    /**
     * 邮箱密码登录
     * body: { "email": "xxx@example.com", "password": "your_password" }
     */
    @Anonymous
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");
        String password = params.get("password");

        // W1: 邮箱格式校验
        if (!isValidEmail(email))
        {
            return AjaxResult.error("邮箱格式不正确");
        }
        if (password == null || password.isBlank())
        {
            return AjaxResult.error("密码不能为空");
        }

        Authentication authentication;
        try
        {
            // userName 字段存的就是 email
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        }
        catch (Exception e)
        {
            // C4: 统一返回模糊错误，详细信息只记日志
            log.error("登录失败: email={}, error={}", maskEmail(email), e.getMessage());
            return AjaxResult.error("登录失败，请检查邮箱和密码");
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = tokenService.createToken(loginUser);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return AjaxResult.success("登录成功", result);
    }

    /**
     * W5: 返回安全的用户信息 DTO，不含密码等敏感字段
     */
    @GetMapping("/profile")
    public AjaxResult getProfile()
    {
        Long userId = getUserId();
        SysUser user = userService.selectUserById(userId);
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("userName", user.getUserName());
        profile.put("nickName", user.getNickName());
        profile.put("email", user.getEmail());
        profile.put("avatar", user.getAvatar());
        return success(profile);
    }

    /**
     * S2: 退出登录，清除 token
     */
    @PostMapping("/logout")
    public AjaxResult logout()
    {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        if (loginUser != null)
        {
            tokenService.delLoginUser(loginUser.getToken());
        }
        return success("退出成功");
    }
}
