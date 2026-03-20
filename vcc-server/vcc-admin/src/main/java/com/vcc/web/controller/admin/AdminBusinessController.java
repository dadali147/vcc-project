package com.vcc.web.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.domain.Transaction;
import com.vcc.card.service.ICardHolderService;
import com.vcc.card.service.ICardService;
import com.vcc.card.service.ITransactionService;
import com.vcc.finance.domain.CardDebt;
import com.vcc.finance.service.IDebtService;
import com.vcc.user.service.IUserAccountService;

/**
 * 管理端-高权实体操作 Controller
 *
 * 提供超级管理员专用的高权限操作接口，包括：
 * - 强力销卡/冻结：不受商户锁限制的全局卡片干预
 * - 全量持卡人穿透查询：跨商户全文检索（按 email/passport）
 * - 人工干预打款/调账接口
 * - 风控事件处理
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/business")
public class AdminBusinessController extends BaseController
{
    @Autowired
    private ICardService cardService;

    @Autowired
    private ICardHolderService cardHolderService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IDebtService debtService;

    @Autowired
    private IUserAccountService userAccountService;

    // ==================== 强力卡片干预（不受商户锁限制） ====================

    /**
     * 管理端强制冻结卡片
     * 不校验商户归属，直接按卡片ID冻结
     *
     * @param cardId 卡片ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-强制冻结卡片", businessType = BusinessType.UPDATE)
    @PutMapping("/card/{cardId}/force-freeze")
    public AjaxResult forceFreeze(@PathVariable Long cardId)
    {
        Card card = cardService.selectCardById(cardId);
        if (card == null)
        {
            return error("卡片不存在");
        }
        if (card.getStatus() != null && card.getStatus() == Card.STATUS_FROZEN)
        {
            return error("卡片已处于冻结状态");
        }
        if (card.getStatus() != null && card.getStatus() == Card.STATUS_CANCELLED)
        {
            return error("已销卡的卡片无法冻结");
        }
        // 使用管理员ID执行冻结，绕过商户归属校验
        return toAjax(cardService.freezeCard(cardId, getUserId()));
    }

    /**
     * 管理端强制解冻卡片
     *
     * @param cardId 卡片ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-强制解冻卡片", businessType = BusinessType.UPDATE)
    @PutMapping("/card/{cardId}/force-unfreeze")
    public AjaxResult forceUnfreeze(@PathVariable Long cardId)
    {
        Card card = cardService.selectCardById(cardId);
        if (card == null)
        {
            return error("卡片不存在");
        }
        if (card.getStatus() == null || card.getStatus() != Card.STATUS_FROZEN)
        {
            return error("卡片不在冻结状态，无法解冻");
        }
        return toAjax(cardService.unfreezeCard(cardId, getUserId()));
    }

    /**
     * 管理端强制销卡
     * 不受商户锁限制，直接按卡片ID销卡
     *
     * @param cardId 卡片ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-强制销卡", businessType = BusinessType.UPDATE)
    @PutMapping("/card/{cardId}/force-cancel")
    public AjaxResult forceCancel(@PathVariable Long cardId)
    {
        Card card = cardService.selectCardById(cardId);
        if (card == null)
        {
            return error("卡片不存在");
        }
        if (card.getStatus() != null && card.getStatus() == Card.STATUS_CANCELLED)
        {
            return error("卡片已销卡");
        }
        return toAjax(cardService.cancelCard(cardId, getUserId()));
    }

    /**
     * 管理端强制激活卡片
     *
     * @param cardId 卡片ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-强制激活卡片", businessType = BusinessType.UPDATE)
    @PutMapping("/card/{cardId}/force-activate")
    public AjaxResult forceActivate(@PathVariable Long cardId)
    {
        Card card = cardService.selectCardById(cardId);
        if (card == null)
        {
            return error("卡片不存在");
        }
        if (card.getStatus() != null && card.getStatus() == Card.STATUS_ACTIVE)
        {
            return error("卡片已处于激活状态");
        }
        if (card.getStatus() != null && card.getStatus() == Card.STATUS_CANCELLED)
        {
            return error("已销卡的卡片无法激活");
        }
        return toAjax(cardService.activateCard(cardId, getUserId()));
    }

    // ==================== 全量持卡人穿透查询 ====================

    /**
     * 跨商户全量持卡人查询（分页）
     * 支持按 email、证件号（idCard）、姓名模糊搜索，不限制 merchant_id
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/cardholder/search")
    public TableDataInfo searchCardHolders(CardHolder cardHolder)
    {
        startPage();
        // 不设置 userId，查询全量持卡人
        List<CardHolder> list = cardHolderService.selectCardHolderList(cardHolder);
        return getDataTable(list);
    }

    /**
     * 查询持卡人详情（含关联卡片列表）
     *
     * @param holderId 持卡人ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/cardholder/{holderId}")
    public AjaxResult getCardHolderDetail(@PathVariable Long holderId)
    {
        CardHolder holder = cardHolderService.selectCardHolderById(holderId);
        if (holder == null)
        {
            return error("持卡人不存在");
        }
        // 查询该持卡人名下所有卡片
        Card cardQuery = new Card();
        cardQuery.setHolderId(holderId);
        List<Card> cards = cardService.selectCardList(cardQuery);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("holder", holder);
        result.put("cards", cards);
        return success(result);
    }

    /**
     * 跨商户全量卡片查询（分页）
     * 管理端查询所有卡片，支持按 userName 搜索
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/card/search")
    public TableDataInfo searchCards(Card card)
    {
        startPage();
        List<Card> list = cardService.selectCardListAdmin(card);
        return getDataTable(list);
    }

    // ==================== 人工干预打款/调账 ====================

    /**
     * 管理员手动调账
     * 可对任意用户进行余额增减操作
     *
     * @param params 包含: userId(用户ID), currency(币种), amount(调整金额，正增负减), reason(原因)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-管理员调账", businessType = BusinessType.UPDATE)
    @PostMapping("/account/adjust")
    public AjaxResult adjustAccount(@RequestBody Map<String, Object> params)
    {
        Long userId = Long.valueOf(params.get("userId").toString());
        String currency = (String) params.get("currency");
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        String reason = (String) params.get("reason");

        if (userId == null || currency == null || amount == null)
        {
            return error("参数不完整：需要 userId、currency、amount");
        }
        if (amount.compareTo(BigDecimal.ZERO) == 0)
        {
            return error("调整金额不能为零");
        }
        if (reason == null || reason.trim().isEmpty())
        {
            return error("调账原因不能为空");
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0)
        {
            return toAjax(userAccountService.addBalance(userId, currency, amount));
        }
        else
        {
            boolean result = userAccountService.deductBalance(userId, currency, amount.abs());
            if (!result)
            {
                return error("扣减失败：余额不足");
            }
            return success();
        }
    }

    // ==================== 交易穿透查询 ====================

    /**
     * 跨商户全量交易查询（分页）
     * 不限制 merchantId
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/transaction/search")
    public TableDataInfo searchTransactions(Transaction transaction)
    {
        startPage();
        List<Transaction> list = transactionService.selectTransactionList(transaction);
        return getDataTable(list);
    }

    /**
     * 交易详情（管理端，不校验商户归属）
     *
     * @param id 交易ID
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/transaction/{id}")
    public AjaxResult getTransactionDetail(@PathVariable Long id)
    {
        return success(transactionService.selectTransactionById(id));
    }

    // ==================== 欠费管理 ====================

    /**
     * 全量欠费列表查询（分页）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/debt/list")
    public TableDataInfo listDebts(CardDebt cardDebt)
    {
        startPage();
        List<CardDebt> list = debtService.selectCardDebtList(cardDebt);
        return getDataTable(list);
    }

    /**
     * 管理员手动结清欠费
     *
     * @param debtId 欠费记录ID
     * @param params 包含: settleAmount(结清金额)
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-手动结清欠费", businessType = BusinessType.UPDATE)
    @PostMapping("/debt/{debtId}/settle")
    public AjaxResult settleDebt(@PathVariable Long debtId, @RequestBody Map<String, Object> params)
    {
        BigDecimal settleAmount = new BigDecimal(params.get("settleAmount").toString());
        if (settleAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            return error("结清金额必须大于零");
        }
        return toAjax(debtService.settle(debtId, settleAmount));
    }

    // ==================== 风控事件处理 ====================
    // TODO: vcc-risk module not implemented yet
    /*
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/risk/list")
    public TableDataInfo listRiskEvents(RiskEvent riskEvent)
    {
        startPage();
        List<RiskEvent> list = riskEventService.selectRiskEventList(riskEvent);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/risk/{id}")
    public AjaxResult getRiskEventDetail(@PathVariable Long id)
    {
        return success(riskEventService.selectRiskEventById(id));
    }

    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "高权操作-处理风控事件", businessType = BusinessType.UPDATE)
    @PutMapping("/risk/{id}/handle")
    public AjaxResult handleRiskEvent(@PathVariable Long id, @RequestBody Map<String, String> params)
    {
        String status = params.get("status");
        String handleResult = params.get("handleResult");
        if (status == null || (!RiskEvent.STATUS_RESOLVED.equals(status) && !RiskEvent.STATUS_IGNORED.equals(status)))
        {
            return error("状态参数无效，仅支持 RESOLVED 或 IGNORED");
        }
        return toAjax(riskEventService.handleRiskEvent(id, getUserId(), handleResult, status));
    }
    */
}
