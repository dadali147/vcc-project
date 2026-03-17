package com.vcc.card.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.service.ICardHolderService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;

/**
 * 持卡人 Controller
 */
@RestController
@RequestMapping("/merchant/card-holder")
public class CardHolderController extends BaseController
{
    @Autowired
    private ICardHolderService cardHolderService;

    @PreAuthorize("@ss.hasPermi('card:holder:list')")
    @GetMapping("/list")
    public TableDataInfo list(CardHolder cardHolder)
    {
        // VCC-009: 强制只能查询当前用户的持卡人
        cardHolder.setUserId(getUserId());
        startPage();
        List<CardHolder> list = cardHolderService.selectCardHolderList(cardHolder);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('card:holder:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        // VCC-009: 校验归属，只能查询自己的持卡人
        CardHolder holder = cardHolderService.selectCardHolderById(id);
        if (holder == null || !holder.getUserId().equals(getUserId()))
        {
            return error("持卡人不存在或无权访问");
        }
        return success(holder);
    }

    @PreAuthorize("@ss.hasPermi('card:holder:add')")
    @PostMapping
    public AjaxResult add(@RequestBody CardHolder cardHolder)
    {
        cardHolder.setUserId(getUserId());
        return success(cardHolderService.addCardHolder(cardHolder));
    }

    @PreAuthorize("@ss.hasPermi('card:holder:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody CardHolder cardHolder)
    {
        return toAjax(cardHolderService.updateCardHolder(cardHolder));
    }

    @PreAuthorize("@ss.hasPermi('card:holder:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cardHolderService.deleteCardHolderByIds(ids));
    }
}
