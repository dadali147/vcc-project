package com.vcc.card.controller.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.card.domain.Card;
import com.vcc.card.service.ICardService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;

/**
 * 管理后台 - 卡片管理 Controller
 */
@RestController
@RequestMapping("/admin/card")
public class AdminCardController extends BaseController
{
    @Autowired
    private ICardService cardService;

    /**
     * 所有用户卡片列表（分页+筛选）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(Card card)
    {
        startPage();
        List<Card> list = cardService.selectCardList(card);
        return getDataTable(list);
    }

    /**
     * 卡片详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{cardId}")
    public AjaxResult getInfo(@PathVariable Long cardId)
    {
        return success(cardService.selectCardById(cardId));
    }

    /**
     * 管理员强制冻结
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/{cardId}/freeze")
    public AjaxResult freeze(@PathVariable Long cardId)
    {
        return toAjax(cardService.freezeCard(cardId));
    }

    /**
     * 管理员销卡
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/{cardId}/cancel")
    public AjaxResult cancel(@PathVariable Long cardId)
    {
        return toAjax(cardService.cancelCard(cardId));
    }
}
