package com.vcc.user.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.user.domain.UserAccount;
import com.vcc.user.service.IUserAccountService;

/**
 * 用户账户 Controller
 *
 * <p>B2B 设计说明：本系统中持卡人的 userId 即为 merchantId，
 * 上游接口的 customerId 对应本系统的 merchantId（userId）。</p>
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

    // -----------------------------------------------------------------------
    // 上游账户接口（对接 YeeVCC 账户体系）
    // -----------------------------------------------------------------------

    /**
     * 创建上游账户（预算卡需要先创建账户）
     * <p>B2B 设计：userId = merchantId，作为上游 customerId</p>
     */
    @Log(title = "创建上游账户", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('user:account:add')")
    @PostMapping("/create")
    public AjaxResult createUpstreamAccount(@RequestBody Map<String, Object> params)
    {
        // B2B 设计：当前登录用户即为商户，userId = merchantId
        Long merchantId = getUserId();
        String currency = params.getOrDefault("currency", "USD").toString();
        YeeVccApiResponse<YeeVccModels.AccountData> response =
                userAccountService.createUpstreamAccount(merchantId, currency);
        if (response.isSuccess())
        {
            return success(response.getData());
        }
        return error(response.getMessage());
    }

    /**
     * 账户充值（转入），支持 USDT/USD
     */
    @Log(title = "账户充值", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('user:account:edit')")
    @PostMapping("/transfer-in")
    public AjaxResult transferIn(@RequestBody Map<String, Object> params)
    {
        String accountNo = params.get("accountNo").toString();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = params.getOrDefault("currency", "USD").toString();
        String deductCurrency = params.getOrDefault("deductCurrency", currency).toString();
        String orderId = params.containsKey("orderId") ? params.get("orderId").toString() : null;
        YeeVccApiResponse<YeeVccModels.OperationData> response =
                userAccountService.upstreamTransferIn(accountNo, amount, currency, deductCurrency, orderId);
        if (response.isSuccess())
        {
            return success(response.getData());
        }
        return error(response.getMessage());
    }

    /**
     * 账户提现（转出）
     */
    @Log(title = "账户提现", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('user:account:edit')")
    @PostMapping("/transfer-out")
    public AjaxResult transferOut(@RequestBody Map<String, Object> params)
    {
        String accountNo = params.get("accountNo").toString();
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = params.getOrDefault("currency", "USD").toString();
        String deductCurrency = params.getOrDefault("deductCurrency", currency).toString();
        String orderId = params.containsKey("orderId") ? params.get("orderId").toString() : null;
        YeeVccApiResponse<YeeVccModels.OperationData> response =
                userAccountService.upstreamTransferOut(accountNo, amount, currency, deductCurrency, orderId);
        if (response.isSuccess())
        {
            return success(response.getData());
        }
        return error(response.getMessage());
    }

    /**
     * 查询上游账户信息（余额等）
     * <p>B2B 设计：userId = merchantId，作为上游 customerId</p>
     */
    @PreAuthorize("@ss.hasPermi('user:account:query')")
    @GetMapping("/info")
    public AjaxResult getUpstreamAccountInfo(
            @RequestParam(required = false) String accountNo)
    {
        // B2B 设计：当前登录用户即为商户，userId = merchantId
        Long merchantId = getUserId();
        YeeVccApiResponse<YeeVccModels.PageData<YeeVccModels.AccountData>> response =
                userAccountService.getUpstreamAccountInfo(merchantId, accountNo);
        if (response.isSuccess())
        {
            return success(response.getData());
        }
        return error(response.getMessage());
    }
}
