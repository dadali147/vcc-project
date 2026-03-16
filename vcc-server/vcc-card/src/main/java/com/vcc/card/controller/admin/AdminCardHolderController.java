package com.vcc.card.controller.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.service.ICardHolderService;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;

/**
 * 管理后台 - 持卡人管理 Controller
 */
@RestController
@RequestMapping("/admin/card-holder")
public class AdminCardHolderController extends BaseController
{
    @Autowired
    private ICardHolderService cardHolderService;

    /**
     * 所有持卡人（分页）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/list")
    public TableDataInfo list(CardHolder cardHolder)
    {
        startPage();
        List<CardHolder> list = cardHolderService.selectCardHolderList(cardHolder);
        return getDataTable(list);
    }

    /**
     * 持卡人详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(cardHolderService.selectCardHolderById(id));
    }
}
