package com.vcc.card.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.FeeConfig;
import com.vcc.card.domain.Transaction;
import com.vcc.card.dto.CardUpdateRemarkRequest;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.FeeConfigMapper;
import com.vcc.card.service.ICardService;
import com.vcc.card.service.ITransactionService;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;

/**
 * 卡片管理 V3 Controller
 */
@RestController
@RequestMapping("/merchant/v3/cards")
public class CardManageController extends BaseController
{
    @Autowired
    private ICardService cardService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private FeeConfigMapper feeConfigMapper;

    @Autowired
    private CardMapper cardMapper;

    /**
     * 查询当前商户的卡片列表
     */
    @PreAuthorize("@ss.hasPermi('card:v3:list')")
    @GetMapping("/list")
    public TableDataInfo list(Card query)
    {
        query.setUserId(getUserId());
        startPage();
        List<Card> list = cardService.selectCardList(query);
        return getDataTable(list);
    }

    /**
     * 获取卡片详情
     */
    @PreAuthorize("@ss.hasPermi('card:v3:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        return success(card);
    }

    /**
     * 获取卡片交易历史
     */
    @PreAuthorize("@ss.hasPermi('card:v3:query')")
    @GetMapping("/{id}/transactions")
    public AjaxResult transactions(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        List<Transaction> list = transactionService.selectTransactionsByCardId(id);
        return success(list);
    }

    /**
     * 获取卡片适用的费用配置
     */
    @PreAuthorize("@ss.hasPermi('card:v3:query')")
    @GetMapping("/{id}/fee-config")
    public AjaxResult feeConfig(@PathVariable Long id)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        FeeConfig openFee = feeConfigMapper.selectEffectiveFeeConfig(getUserId(), FeeConfig.FEE_TYPE_OPEN, null, null);
        FeeConfig txnFee = feeConfigMapper.selectEffectiveFeeConfig(getUserId(), FeeConfig.FEE_TYPE_TXN, null, null);
        Map<String, FeeConfig> result = new HashMap<>();
        result.put("openFee", openFee);
        result.put("txnFee", txnFee);
        return success(result);
    }

    /**
     * 更新卡片备注
     */
    @PreAuthorize("@ss.hasPermi('card:v3:edit')")
    @Log(title = "卡片备注", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/remark")
    public AjaxResult updateRemark(@PathVariable Long id, @Validated @RequestBody CardUpdateRemarkRequest request)
    {
        Card card = cardService.selectCardById(id);
        if (card == null || !card.getUserId().equals(getUserId()))
        {
            return error("卡片不存在或无权访问");
        }
        Card update = new Card();
        update.setId(id);
        update.setCardRemark(request.remark());
        return toAjax(cardMapper.updateCard(update));
    }

    /**
     * 冻结卡片
     */
    @PreAuthorize("@ss.hasPermi('card:v3:edit')")
    @Log(title = "冻结卡片", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/freeze")
    public AjaxResult freeze(@PathVariable Long id)
    {
        return toAjax(cardService.freezeCard(id, getUserId()));
    }

    /**
     * 解冻卡片
     */
    @PreAuthorize("@ss.hasPermi('card:v3:edit')")
    @Log(title = "解冻卡片", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unfreeze")
    public AjaxResult unfreeze(@PathVariable Long id)
    {
        return toAjax(cardService.unfreezeCard(id, getUserId()));
    }

    /**
     * 销卡
     */
    @PreAuthorize("@ss.hasPermi('card:v3:edit')")
    @Log(title = "销卡", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id)
    {
        return toAjax(cardService.cancelCard(id, getUserId()));
    }
}
