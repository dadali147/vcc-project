package com.vcc.card.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.card.domain.Card;
import com.vcc.card.service.ICardService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;

/**
 * 卡片 Controller
 */
@RestController
@RequestMapping("/merchant/card")
public class CardController extends BaseController
{
    @Autowired
    private ICardService cardService;

    @PreAuthorize("@ss.hasPermi('card:card:list')")
    @GetMapping("/list")
    public TableDataInfo list(Card card)
    {
        startPage();
        List<Card> list = cardService.selectCardList(card);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('card:card:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(cardService.selectCardById(id));
    }

    @PreAuthorize("@ss.hasPermi('card:card:add')")
    @PostMapping("/open")
    public AjaxResult openCard(@RequestBody Map<String, Object> params)
    {
        Long holderId = Long.valueOf(params.get("holderId").toString());
        String cardBinId = params.get("cardBinId").toString();
        String currency = params.getOrDefault("currency", "USD").toString();
        Integer cardType = Integer.valueOf(params.getOrDefault("cardType", "1").toString());
        BigDecimal amount = params.containsKey("amount") ? new BigDecimal(params.get("amount").toString()) : null;

        Card card = cardService.openCard(holderId, cardBinId, currency, cardType, amount, getUserId());
        return success(card);
    }

    @PreAuthorize("@ss.hasPermi('card:card:edit')")
    @PostMapping("/activate/{id}")
    public AjaxResult activate(@PathVariable Long id)
    {
        return toAjax(cardService.activateCard(id));
    }

    @PreAuthorize("@ss.hasPermi('card:card:edit')")
    @PostMapping("/freeze/{id}")
    public AjaxResult freeze(@PathVariable Long id)
    {
        return toAjax(cardService.freezeCard(id));
    }

    @PreAuthorize("@ss.hasPermi('card:card:edit')")
    @PostMapping("/unfreeze/{id}")
    public AjaxResult unfreeze(@PathVariable Long id)
    {
        return toAjax(cardService.unfreezeCard(id));
    }

    @PreAuthorize("@ss.hasPermi('card:card:edit')")
    @PostMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id)
    {
        return toAjax(cardService.cancelCard(id));
    }

    @PreAuthorize("@ss.hasPermi('card:card:query')")
    @GetMapping("/key-info/{id}")
    public AjaxResult keyInfo(@PathVariable Long id)
    {
        return success(cardService.getCardKeyInfo(id));
    }
}
