package com.vcc.card.controller.admin;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.card.domain.Transaction;
import com.vcc.card.service.ITransactionService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.page.TableDataInfo;

/**
 * 管理后台 - 交易记录 Controller
 */
@RestController
@RequestMapping("/admin/transaction")
public class AdminTransactionController extends BaseController
{
    @Autowired
    private ITransactionService transactionService;

    /**
     * 交易记录（分页+筛选）
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
     * 导出 CSV
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/export")
    public void export(Transaction transaction, HttpServletResponse response) throws IOException
    {
        List<Transaction> list = transactionService.selectTransactionList(transaction);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=transactions.csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // BOM

        StringBuilder sb = new StringBuilder();
        sb.append("ID,交易ID,卡片ID,用户ID,交易类型,金额,币种,商户名称,商户MCC,商户国家,状态,授权码,失败原因,交易时间\n");

        for (Transaction t : list)
        {
            sb.append(t.getId()).append(",");
            sb.append(csvValue(t.getTxnId())).append(",");
            sb.append(t.getCardId()).append(",");
            sb.append(t.getUserId()).append(",");
            sb.append(csvValue(t.getTxnType())).append(",");
            sb.append(t.getAmount()).append(",");
            sb.append(csvValue(t.getCurrency())).append(",");
            sb.append(csvValue(t.getMerchantName())).append(",");
            sb.append(csvValue(t.getMerchantMcc())).append(",");
            sb.append(csvValue(t.getMerchantCountry())).append(",");
            sb.append(t.getStatus()).append(",");
            sb.append(csvValue(t.getAuthCode())).append(",");
            sb.append(csvValue(t.getFailReason())).append(",");
            sb.append(t.getTxnTime() != null ? t.getTxnTime() : "").append("\n");
        }

        response.getOutputStream().write(sb.toString().getBytes("UTF-8"));
        response.getOutputStream().flush();
    }

    private String csvValue(String value)
    {
        if (value == null)
        {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
        {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
