package com.vcc.user.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.user.domain.UserAccount;
import com.vcc.user.service.IUserAccountService;

/**
 * 用户账户 Controller
 */
@RestController
@RequestMapping("/merchant/account")
public class UserAccountController extends BaseController
{
    @Autowired
    private IUserAccountService userAccountService;

    @PreAuthorize("@ss.hasPermi('user:account:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserAccount userAccount)
    {
        startPage();
        List<UserAccount> list = userAccountService.selectUserAccountList(userAccount);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('user:account:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(userAccountService.selectUserAccountById(id));
    }

    @PreAuthorize("@ss.hasPermi('user:account:query')")
    @GetMapping("/balance")
    public AjaxResult getBalance(@RequestParam(defaultValue = "USD") String currency)
    {
        UserAccount account = userAccountService.selectUserAccountByUserId(getUserId(), currency);
        return success(account);
    }

    @PreAuthorize("@ss.hasPermi('user:account:add')")
    @PostMapping
    public AjaxResult add(@RequestBody UserAccount userAccount)
    {
        return toAjax(userAccountService.insertUserAccount(userAccount));
    }
}
