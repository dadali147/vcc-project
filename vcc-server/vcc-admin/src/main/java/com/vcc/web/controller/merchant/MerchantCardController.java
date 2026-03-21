package com.vcc.web.controller.merchant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.card.domain.Card;
import com.vcc.card.service.ICardService;

@RestController
@RequestMapping("/cards")
public class MerchantCardController extends BaseController
{
    @Autowired
    private ICardService cardService;

    @GetMapping
    public TableDataInfo list(Card card)
    {
        Long userId = getUserId();
        card.setUserId(userId);
        startPage();
        List<Card> list = cardService.selectCardList(card);
        return getDataTable(list);
    }

    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        return success(card);
    }

    @PostMapping("/open")
    public AjaxResult open(@RequestBody Map<String, Object> params)
    {
        Long holderId = Long.valueOf(params.get("holderId").toString());
        String cardBinId = (String) params.get("cardBinId");
        String currency = (String) params.get("currency");
        Integer cardType = (Integer) params.get("cardType");
        BigDecimal amount = params.get("amount") != null ? new BigDecimal(params.get("amount").toString()) : null;

        Card card = cardService.openCard(holderId, cardBinId, currency, cardType, amount, getUserId());
        return success(card);
    }

    @GetMapping("/{id}/three-factors")
    public AjaxResult getThreeFactors(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        Map<String, String> keyInfo = cardService.getCardKeyInfo(id, getUserId());
        return success(keyInfo);
    }

    @PostMapping("/{id}/freeze")
    public AjaxResult freeze(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        return toAjax(cardService.freezeCard(id, getUserId()));
    }

    @PostMapping("/{id}/unfreeze")
    public AjaxResult unfreeze(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        return toAjax(cardService.unfreezeCard(id, getUserId()));
    }

    @PostMapping("/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        return toAjax(cardService.cancelCard(id, getUserId()));
    }
}
