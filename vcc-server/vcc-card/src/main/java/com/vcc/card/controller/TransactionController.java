package com.vcc.card.controller;

import com.vcc.card.domain.Transaction;
import com.vcc.card.dto.RefundRequest;
import com.vcc.card.dto.ReverseRequest;
import com.vcc.card.service.ITransactionService;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 交易记录 Controller
 */
@RestController
@RequestMapping("/merchant/v3/transactions")
public class TransactionController extends BaseController
{
    @Autowired
    private ITransactionService transactionService;

    @PreAuthorize("@ss.hasPermi('transaction:v3:list')")
    @GetMapping("/list")
    public TableDataInfo list(Transaction query)
    {
        query.setMerchantId(getUserId());
        startPage();
        List<Transaction> list = transactionService.selectTransactionList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('transaction:v3:query')")
    @GetMapping("/{id}")
    public AjaxResult getDetail(@PathVariable Long id)
    {
        return success(transactionService.getTransactionDetail(id, getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('transaction:v3:list')")
    @GetMapping("/card/{cardId}")
    public TableDataInfo listByCard(@PathVariable Long cardId)
    {
        startPage();
        List<Transaction> list = transactionService.selectTransactionsByCardId(cardId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('transaction:v3:query')")
    @GetMapping("/related/{txnId}")
    public AjaxResult getRelated(@PathVariable String txnId)
    {
        return success(transactionService.selectRelatedTransactions(txnId));
    }

    @PreAuthorize("@ss.hasPermi('transaction:v3:refund')")
    @Log(title = "交易退款", businessType = BusinessType.UPDATE)
    @PostMapping("/refund")
    public AjaxResult refund(@Validated @RequestBody RefundRequest request)
    {
        return success(transactionService.processRefund(getUserId(),
                request.originalTxnId(), request.refundAmount(), request.reason()));
    }

    @PreAuthorize("@ss.hasPermi('transaction:v3:reverse')")
    @Log(title = "交易撤销", businessType = BusinessType.UPDATE)
    @PostMapping("/reverse")
    public AjaxResult reverse(@Validated @RequestBody ReverseRequest request)
    {
        return success(transactionService.processReverse(getUserId(),
                request.originalTxnId(), request.reason()));
    }
}
