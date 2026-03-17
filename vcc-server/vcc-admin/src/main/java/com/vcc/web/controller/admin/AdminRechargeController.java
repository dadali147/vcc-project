package com.vcc.web.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
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
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.common.utils.poi.ExcelUtil;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.service.IRechargeService;

/**
 * 管理端-充值管理 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/recharge")
public class AdminRechargeController extends BaseController
{
    @Autowired
    private IRechargeService rechargeService;

    /**
     * 分页查询充值记录
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(Recharge recharge)
    {
        startPage();
        List<Recharge> list = rechargeService.selectRechargeList(recharge);
        return getDataTable(list);
    }

    /**
     * 查询充值详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(rechargeService.selectRechargeById(id));
    }

    /**
     * 审核充值
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "充值管理-审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/audit")
    public AjaxResult audit(@PathVariable Long id, @RequestBody Map<String, String> params)
    {
        String result = params.get("result");
        String remark = params.get("remark");
        // TODO: Service层补全充值审核方法 auditRecharge(id, result, remark)
        return success();
    }

    /**
     * 手动充值
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "充值管理-手动充值", businessType = BusinessType.INSERT)
    @PostMapping("/manual")
    public AjaxResult manual(@RequestBody Map<String, Object> params)
    {
        Long userId = Long.valueOf(params.get("userId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String currency = (String) params.get("currency");
        String remark = (String) params.get("remark");
        // TODO: Service层补全手动充值方法 manualRecharge(userId, amount, currency, remark)
        return success();
    }

    /**
     * 导出充值记录
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "充值管理-导出", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, Recharge recharge)
    {
        List<Recharge> list = rechargeService.selectRechargeList(recharge);
        ExcelUtil<Recharge> util = new ExcelUtil<Recharge>(Recharge.class);
        util.exportExcel(response, list, "充值记录");
    }
}
