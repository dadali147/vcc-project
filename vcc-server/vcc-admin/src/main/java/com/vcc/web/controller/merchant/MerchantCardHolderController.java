package com.vcc.web.controller.merchant;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.service.ICardHolderService;

@RestController
@RequestMapping("/cardholders")
public class MerchantCardHolderController extends BaseController
{
    @Autowired
    private ICardHolderService cardHolderService;

    @GetMapping
    public TableDataInfo list(CardHolder cardHolder)
    {
        Long userId = getUserId();
        cardHolder.setUserId(userId);
        startPage();
        List<CardHolder> list = cardHolderService.selectCardHolderList(cardHolder);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id)
    {
        CardHolder holder = cardHolderService.selectCardHolderById(id);
        if (holder == null || !holder.getUserId().equals(getUserId()))
        {
            return error("持卡人不存在或无权访问");
        }
        return success(holder);
    }

    @PostMapping
    public AjaxResult create(@RequestBody CardHolder cardHolder)
    {
        cardHolder.setUserId(getUserId());
        CardHolder result = cardHolderService.addCardHolder(cardHolder);
        return success(result);
    }

    @PutMapping("/{id}")
    public AjaxResult update(@PathVariable Long id, @RequestBody CardHolder cardHolder)
    {
        CardHolder existing = cardHolderService.selectCardHolderById(id);
        if (existing == null || !existing.getUserId().equals(getUserId()))
        {
            return error("持卡人不存在或无权访问");
        }
        cardHolder.setId(id);
        cardHolder.setUserId(getUserId());
        return toAjax(cardHolderService.updateCardHolder(cardHolder));
    }

    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id)
    {
        CardHolder existing = cardHolderService.selectCardHolderById(id);
        if (existing == null || !existing.getUserId().equals(getUserId()))
        {
            return error("持卡人不存在或无权访问");
        }
        return toAjax(cardHolderService.deleteCardHolderById(id));
    }
}
