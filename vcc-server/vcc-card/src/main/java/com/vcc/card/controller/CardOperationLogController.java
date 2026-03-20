package com.vcc.card.controller;

import com.vcc.card.domain.CardOperationLog;
import com.vcc.card.service.ICardOperationLogService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 卡片操作记录 Controller
 */
@RestController
@RequestMapping("/merchant/v3/card-operations")
public class CardOperationLogController extends BaseController
{
    @Autowired
    private ICardOperationLogService operationLogService;

    /**
     * 查询操作记录列表（当前商户）
     */
    @PreAuthorize("@ss.hasPermi('card:operation:list')")
    @GetMapping("/list")
    public TableDataInfo list(CardOperationLog query)
    {
        query.setMerchantId(getUserId());
        startPage();
        List<CardOperationLog> list = operationLogService.selectList(query);
        return getDataTable(list);
    }

    /**
     * 查询指定卡片的操作记录
     */
    @PreAuthorize("@ss.hasPermi('card:operation:query')")
    @GetMapping("/card/{cardId}")
    public AjaxResult listByCard(@PathVariable Long cardId)
    {
        return success(operationLogService.selectByCardId(cardId));
    }

    /**
     * 查询当前商户所有操作记录
     */
    @PreAuthorize("@ss.hasPermi('card:operation:list')")
    @GetMapping("/merchant")
    public TableDataInfo listByMerchant()
    {
        startPage();
        List<CardOperationLog> list = operationLogService.selectByMerchantId(getUserId());
        return getDataTable(list);
    }
}
