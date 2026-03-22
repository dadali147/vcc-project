package com.vcc.web.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.user.domain.UserAccount;
import java.util.Map;
import com.vcc.user.service.IUserAccountService;

/**
 * 管理端-钱包管理 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/wallet")
public class AdminWalletController extends BaseController
{
    @Autowired
    private IUserAccountService userAccountService;

    /**
     * 分页查询用户钱包列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(UserAccount userAccount)
    {
        startPage();
        List<UserAccount> list = userAccountService.selectUserAccountList(userAccount);
        return getDataTable(list);
    }

    /**
     * 查询用户钱包详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId)
    {
        UserAccount query = new UserAccount();
        query.setUserId(userId);
        List<UserAccount> list = userAccountService.selectUserAccountList(query);
        return success(list);
    }

    /**
     * 调整余额
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "钱包管理-调整余额", businessType = BusinessType.UPDATE)
    @PostMapping("/{userId}/adjust")
    public AjaxResult adjust(@PathVariable Long userId, @RequestBody Map<String, Object> params)
    {
        String currency = (String) params.get("currency");
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String reason = (String) params.get("reason");

        if (amount.compareTo(BigDecimal.ZERO) == 0)
        {
            return error("调整金额不能为零");
        }
        if (reason == null || reason.trim().isEmpty())
        {
            return error("调账原因不能为空");
        }
        if (currency == null || currency.trim().isEmpty())
        {
            return error("币种不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0)
        {
            // 增加余额
            return toAjax(userAccountService.addBalance(userId, currency, amount));
        }
        else if (amount.compareTo(BigDecimal.ZERO) < 0)
        {
            // 减少余额
            boolean result = userAccountService.deductBalance(userId, currency, amount.abs());
            return toAjax(result);
        }
        else
        {
            return error("调整金额不能为零");
        }
    }

    /**
     * 查询动账明细（分页）
     * 调用 IUserAccountService.selectAccountTransactions()
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{userId}/transactions")
    public TableDataInfo transactions(@PathVariable Long userId)
    {
        startPage();
        com.github.pagehelper.Page<?> page = com.github.pagehelper.PageHelper.getLocalPage();
        int pageNum = page != null ? page.getPageNum() : 1;
        int pageSize = page != null ? page.getPageSize() : 10;
        List<Map<String, Object>> list = userAccountService.selectAccountTransactions(userId, pageNum, pageSize);
        return getDataTable(list);
    }
}
