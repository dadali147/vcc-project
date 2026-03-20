package com.vcc.web.controller.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.Transaction;
import com.vcc.card.service.ICardService;
import com.vcc.card.service.ITransactionService;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.service.IDebtService;
import com.vcc.finance.service.IRechargeService;
import com.vcc.risk.domain.RiskEvent;
import com.vcc.risk.service.IRiskEventService;

/**
 * 管理端-全局仪表盘聚合 Controller
 *
 * 提供管理后台首页所需的全局统计数据，包括：
 * - 全局卡片发行总量 / 冻结量概览
 * - 全局 USDT 入金与支出双边统计
 * - 风控警告卡列表
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController extends BaseController
{
    @Autowired
    private ICardService cardService;

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private IRechargeService rechargeService;

    @Autowired
    private IDebtService debtService;

    @Autowired
    private IRiskEventService riskEventService;

    /**
     * 首页聚合概览
     * 返回全局卡片统计、交易统计、入金统计的汇总信息
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        Map<String, Object> data = new HashMap<>();

        // ---- 卡片概览 ----
        List<Card> allCards = cardService.selectCardList(new Card());
        Map<String, Object> cardOverview = new HashMap<>();
        cardOverview.put("total", allCards.size());
        cardOverview.put("active", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_ACTIVE).count());
        cardOverview.put("inactive", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_INACTIVE).count());
        cardOverview.put("frozen", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_FROZEN).count());
        cardOverview.put("cancelled", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_CANCELLED).count());
        cardOverview.put("prepaid", allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_PREPAID).count());
        cardOverview.put("budget", allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_BUDGET).count());
        data.put("card", cardOverview);

        // ---- 交易概览（全量） ----
        List<Transaction> allTxns = transactionService.selectTransactionList(new Transaction());
        Map<String, Object> txnOverview = new HashMap<>();
        txnOverview.put("totalCount", allTxns.size());
        txnOverview.put("successCount", allTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == 1).count());
        txnOverview.put("failedCount", allTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == 2).count());

        BigDecimal totalAmount = allTxns.stream()
                .filter(t -> t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        txnOverview.put("totalAmount", totalAmount);
        data.put("transaction", txnOverview);

        // ---- 入金概览 ----
        List<Recharge> allRecharges = rechargeService.selectRechargeList(new Recharge());
        Map<String, Object> rechargeOverview = new HashMap<>();
        rechargeOverview.put("totalCount", allRecharges.size());
        rechargeOverview.put("successCount", allRecharges.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == Recharge.STATUS_SUCCESS).count());

        BigDecimal totalRechargeAmount = allRecharges.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == Recharge.STATUS_SUCCESS && r.getAmount() != null)
                .map(Recharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        rechargeOverview.put("totalAmount", totalRechargeAmount);
        data.put("recharge", rechargeOverview);

        // ---- 风控待处理事件数 ----
        RiskEvent riskQuery = new RiskEvent();
        riskQuery.setStatus(RiskEvent.STATUS_PENDING);
        List<RiskEvent> pendingRiskEvents = riskEventService.selectRiskEventList(riskQuery);
        data.put("pendingRiskEvents", pendingRiskEvents.size());

        return success(data);
    }

    /**
     * 卡片发行统计概览
     * 返回按卡类型、状态的分组统计
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/card-stats")
    public AjaxResult cardStats()
    {
        List<Card> allCards = cardService.selectCardList(new Card());
        Map<String, Object> stats = new HashMap<>();

        // 按状态分组
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("active", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_ACTIVE).count());
        byStatus.put("inactive", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_INACTIVE).count());
        byStatus.put("frozen", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_FROZEN).count());
        byStatus.put("cancelled", allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_CANCELLED).count());
        stats.put("byStatus", byStatus);

        // 按卡类型分组
        Map<String, Long> byType = new HashMap<>();
        byType.put("prepaid", allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_PREPAID).count());
        byType.put("budget", allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_BUDGET).count());
        stats.put("byType", byType);

        // 总余额
        BigDecimal totalBalance = allCards.stream()
                .filter(c -> c.getBalance() != null)
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalBalance", totalBalance);
        stats.put("total", allCards.size());

        return success(stats);
    }

    /**
     * USDT 入金与支出双边统计
     * 返回成功入金总额、支出总额（交易扣款）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/finance-stats")
    public AjaxResult financeStats()
    {
        Map<String, Object> stats = new HashMap<>();

        // 入金（成功的充值）
        List<Recharge> allRecharges = rechargeService.selectRechargeList(new Recharge());
        BigDecimal totalDeposit = allRecharges.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == Recharge.STATUS_SUCCESS && r.getAmount() != null)
                .map(Recharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalDeposit", totalDeposit);
        stats.put("depositCount", allRecharges.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == Recharge.STATUS_SUCCESS).count());

        // 支出（成功的交易消费）
        List<Transaction> allTxns = transactionService.selectTransactionList(new Transaction());
        BigDecimal totalExpense = allTxns.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 1 && t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalExpense", totalExpense);
        stats.put("expenseCount", allTxns.stream()
                .filter(t -> t.getStatus() != null && t.getStatus() == 1).count());

        // 净额
        stats.put("netAmount", totalDeposit.subtract(totalExpense));

        return success(stats);
    }

    /**
     * 风控警告卡列表（分页）
     * 查询所有待处理的风控事件
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/risk-alerts")
    public TableDataInfo riskAlerts(RiskEvent riskEvent)
    {
        startPage();
        if (riskEvent.getStatus() == null)
        {
            riskEvent.setStatus(RiskEvent.STATUS_PENDING);
        }
        List<RiskEvent> list = riskEventService.selectRiskEventList(riskEvent);
        return getDataTable(list);
    }
}
