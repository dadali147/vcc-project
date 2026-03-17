package com.vcc.web.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.common.utils.poi.ExcelUtil;
import com.vcc.card.domain.Transaction;
import com.vcc.card.service.ITransactionService;

/**
 * 管理端-交易管理 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/transaction")
public class AdminTransactionController extends BaseController
{
    @Autowired
    private ITransactionService transactionService;

    /**
     * 分页查询所有交易记录
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(Transaction transaction)
    {
        startPage();
        List<Transaction> list = transactionService.selectTransactionList(transaction);
        return getDataTable(list);
    }

    /**
     * 交易详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(transactionService.selectTransactionById(id));
    }

    /**
     * 今日统计（笔数/金额/成功/失败）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        // TODO: Service层补全今日交易统计方法
        Map<String, Object> stats = new HashMap<>();
        List<Transaction> allTxns = transactionService.selectTransactionList(new Transaction());
        stats.put("totalCount", allTxns.size());

        long successCount = allTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count();
        long failedCount = allTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == 2).count();

        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        return success(stats);
    }

    /**
     * 导出交易记录
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "交易管理-导出", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, Transaction transaction)
    {
        List<Transaction> list = transactionService.selectTransactionList(transaction);
        ExcelUtil<Transaction> util = new ExcelUtil<Transaction>(Transaction.class);
        util.exportExcel(response, list, "交易记录");
    }
}
