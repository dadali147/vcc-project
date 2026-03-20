package com.vcc.card.controller;

import com.vcc.card.domain.CardIssueItem;
import com.vcc.card.domain.CardIssueRequest;
import com.vcc.card.dto.CardIssueCreateRequest;
import com.vcc.card.mapper.CardIssueItemMapper;
import com.vcc.card.service.ICardIssueService;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 开卡申请 Controller
 */
@RestController
@RequestMapping("/merchant/v3/card-issue")
public class CardIssueController extends BaseController
{
    private final ICardIssueService cardIssueService;
    private final CardIssueItemMapper itemMapper;

    public CardIssueController(ICardIssueService cardIssueService, CardIssueItemMapper itemMapper)
    {
        this.cardIssueService = cardIssueService;
        this.itemMapper = itemMapper;
    }

    @PreAuthorize("@ss.hasPermi('card:issue:list')")
    @GetMapping("/list")
    public TableDataInfo list(CardIssueRequest query)
    {
        startPage();
        List<CardIssueRequest> list = cardIssueService.list(getUserId(), query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('card:issue:query')")
    @GetMapping("/batch/{batchNo}")
    public AjaxResult getByBatchNo(@PathVariable String batchNo)
    {
        return success(cardIssueService.getByBatchNo(getUserId(), batchNo));
    }

    @PreAuthorize("@ss.hasPermi('card:issue:query')")
    @GetMapping("/{requestId}/items")
    public AjaxResult getItems(@PathVariable Long requestId)
    {
        List<CardIssueItem> items = itemMapper.selectByRequestId(requestId);
        return success(items);
    }

    @PreAuthorize("@ss.hasPermi('card:issue:add')")
    @Log(title = "批量开卡", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Validated @RequestBody CardIssueCreateRequest request)
    {
        CardIssueRequest result = cardIssueService.createAndProcess(getUserId(), request);
        return success(result);
    }

    @PreAuthorize("@ss.hasPermi('card:issue:edit')")
    @Log(title = "取消开卡申请单", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{requestId}")
    public AjaxResult cancel(@PathVariable Long requestId)
    {
        return toAjax(cardIssueService.cancel(getUserId(), requestId));
    }
}
