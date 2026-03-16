package com.vcc.card.controller.admin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.service.IRechargeService;

/**
 * 管理后台 - 充值管理 Controller
 */
@RestController
@RequestMapping("/admin/recharge")
public class AdminRechargeController extends BaseController
{
    @Autowired
    private IRechargeService rechargeService;

    /**
     * 充值记录（分页+筛选）
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
     * 充值统计
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        // 查询所有充值记录进行统计
        Recharge query = new Recharge();
        List<Recharge> allList = rechargeService.selectRechargeList(query);

        long totalCount = allList.size();
        long successCount = allList.stream().filter(r -> r.getStatus() == Recharge.STATUS_SUCCESS).count();
        long pendingCount = allList.stream().filter(r -> r.getStatus() == Recharge.STATUS_PENDING).count();
        long failedCount = allList.stream().filter(r -> r.getStatus() == Recharge.STATUS_FAILED).count();

        BigDecimal totalAmount = allList.stream()
                .filter(r -> r.getStatus() == Recharge.STATUS_SUCCESS)
                .map(Recharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFee = allList.stream()
                .filter(r -> r.getStatus() == Recharge.STATUS_SUCCESS && r.getFee() != null)
                .map(Recharge::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("successCount", successCount);
        stats.put("pendingCount", pendingCount);
        stats.put("failedCount", failedCount);
        stats.put("totalAmount", totalAmount);
        stats.put("totalFee", totalFee);

        return success(stats);
    }
}
