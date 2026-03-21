package com.vcc.web.controller.merchant;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.card.domain.Transaction;
import com.vcc.card.service.ITransactionService;

@RestController
@RequestMapping("/transactions")
public class MerchantTransactionController extends BaseController
{
    @Autowired
    private ITransactionService transactionService;

    @GetMapping
    public TableDataInfo list(Transaction transaction)
    {
        Long userId = getUserId();
        transaction.setUserId(userId);
        startPage();
        List<Transaction> list = transactionService.selectTransactionList(transaction);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id)
    {
        Transaction transaction = transactionService.selectTransactionById(id);
        if (transaction == null || !transaction.getUserId().equals(getUserId()))
        {
            return error("交易不存在或无权访问");
        }
        return success(transactionService.getTransactionDetail(id, getUserId()));
    }
}
