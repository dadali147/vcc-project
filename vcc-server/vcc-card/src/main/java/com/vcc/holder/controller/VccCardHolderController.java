package com.vcc.holder.controller;

import com.vcc.card.domain.Card;
import com.vcc.card.mapper.CardMapper;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.holder.domain.VccCardHolder;
import com.vcc.holder.dto.CardHolderCreateRequest;
import com.vcc.holder.dto.CardHolderStatusRequest;
import com.vcc.holder.dto.CardHolderUpdateRequest;
import com.vcc.holder.service.VccCardHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant/v3/holders")
public class VccCardHolderController extends BaseController
{
    private final VccCardHolderService holderService;

    @Autowired
    private CardMapper cardMapper;

    public VccCardHolderController(VccCardHolderService holderService)
    {
        this.holderService = holderService;
    }

    @PreAuthorize("@ss.hasPermi('holder:v3:list')")
    @GetMapping("/list")
    public TableDataInfo list(VccCardHolder query)
    {
        startPage();
        List<VccCardHolder> list = holderService.list(getUserId(), query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('holder:v3:query')")
    @GetMapping("/{holderId}")
    public AjaxResult getInfo(@PathVariable Long holderId)
    {
        return success(holderService.getById(getUserId(), holderId));
    }

    /**
     * 查询持卡人详情（含卡片数量和卡片列表）
     */
    @PreAuthorize("@ss.hasPermi('holder:v3:query')")
    @GetMapping("/{holderId}/detail")
    public AjaxResult getDetail(@PathVariable Long holderId)
    {
        VccCardHolder holder = holderService.getById(getUserId(), holderId);
        int cardCount = cardMapper.countByHolderId(holderId);
        List<Card> cards = cardMapper.selectCardsByHolderId(holderId);

        Map<String, Object> detail = new HashMap<>();
        detail.put("holder", holder);
        detail.put("cardCount", cardCount);
        detail.put("cards", cards);
        return success(detail);
    }

    /**
     * 查询持卡人名下卡片
     */
    @PreAuthorize("@ss.hasPermi('holder:v3:query')")
    @GetMapping("/{holderId}/cards")
    public AjaxResult getHolderCards(@PathVariable Long holderId)
    {
        holderService.getById(getUserId(), holderId);
        List<Card> cards = cardMapper.selectCardsByHolderId(holderId);
        return success(cards);
    }

    @PreAuthorize("@ss.hasPermi('holder:v3:add')")
    @Log(title = "V3持卡人", businessType = BusinessType.INSERT, excludeParamNames = { "idCard" })
    @PostMapping
    public AjaxResult create(@Validated @RequestBody CardHolderCreateRequest request)
    {
        return success(holderService.create(getUserId(), request));
    }

    @PreAuthorize("@ss.hasPermi('holder:v3:edit')")
    @Log(title = "V3持卡人", businessType = BusinessType.UPDATE, excludeParamNames = { "idCard" })
    @PutMapping("/{holderId}")
    public AjaxResult update(@PathVariable Long holderId, @Validated @RequestBody CardHolderUpdateRequest request)
    {
        return success(holderService.update(getUserId(), holderId, request));
    }

    @PreAuthorize("@ss.hasPermi('holder:v3:edit')")
    @Log(title = "V3持卡人状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{holderId}/status")
    public AjaxResult changeStatus(@PathVariable Long holderId, @Validated @RequestBody CardHolderStatusRequest request)
    {
        return toAjax(holderService.changeStatus(getUserId(), holderId, request));
    }

    @PreAuthorize("@ss.hasPermi('holder:v3:remove')")
    @Log(title = "V3持卡人", businessType = BusinessType.DELETE)
    @DeleteMapping("/{holderId}")
    public AjaxResult remove(@PathVariable Long holderId)
    {
        return toAjax(holderService.delete(getUserId(), holderId));
    }
}
