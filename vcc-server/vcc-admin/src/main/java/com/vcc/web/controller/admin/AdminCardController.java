package com.vcc.web.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vcc.common.annotation.Log;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.common.enums.BusinessType;
import com.vcc.common.utils.poi.ExcelUtil;
import com.vcc.card.domain.Card;
import com.vcc.card.service.ICardService;

/**
 * 管理端-卡片管理 Controller
 *
 * @author vcc
 */
@RestController
@RequestMapping("/admin/card")
public class AdminCardController extends BaseController
{
    @Autowired
    private ICardService cardService;

    /**
     * 分页查询所有用户卡片
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
     * 查询卡片详情
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(cardService.selectCardById(id));
    }

    /**
     * 修改卡片状态
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "卡片管理-修改状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult changeStatus(@PathVariable Long id, @RequestBody Map<String, Object> params)
    {
        Integer status = (Integer) params.get("status");
        if (status == null)
        {
            return error("状态参数不能为空");
        }
        // 1=正常(激活), 2=冻结, 3=注销(取消)
        switch (status)
        {
            case Card.STATUS_ACTIVE:
                return toAjax(cardService.activateCard(id, getUserId()));
            case Card.STATUS_FROZEN:
                return toAjax(cardService.freezeCard(id, getUserId()));
            case Card.STATUS_CANCELLED:
                return toAjax(cardService.cancelCard(id, getUserId()));
            default:
                return error("无效的状态值");
        }
    }

    /**
     * 导出卡片数据
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "卡片管理-导出", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, Card card)
    {
        List<Card> list = cardService.selectCardList(card);
        ExcelUtil<Card> util = new ExcelUtil<Card>(Card.class);
        util.exportExcel(response, list, "卡片数据");
    }

    /**
     * 卡产品统计
     *
     * TODO [E1-010]: 当前全表加载后内存统计，数据量大时性能堪忧。
     * 应改为 ICardService 中新增 SQL 聚合查询方法，如：
     *   Map<String, Object> selectCardStats() → SELECT status, card_type, COUNT(*) GROUP BY
     *   避免将全量卡片数据加载到 JVM 内存中进行 stream 过滤。
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        // TODO [E1-010]: 当前全表加载后内存统计，数据量大时性能堪忧。
        // 应改为 ICardService 中新增 SQL 聚合查询方法，如：
        //   Map<String, Object> selectCardStats() → SELECT status, card_type, COUNT(*) GROUP BY
        Map<String, Object> stats = new HashMap<>();
        List<Card> allCards = cardService.selectCardList(new Card());
        stats.put("totalCards", allCards.size());

        long activeCount = allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_ACTIVE).count();
        long frozenCount = allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_FROZEN).count();
        long cancelledCount = allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_CANCELLED).count();
        long prepaidCount = allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_PREPAID).count();
        long budgetCount = allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_BUDGET).count();

        stats.put("activeCards", activeCount);
        stats.put("frozenCards", frozenCount);
        stats.put("cancelledCards", cancelledCount);
        stats.put("prepaidCards", prepaidCount);
        stats.put("budgetCards", budgetCount);
        return success(stats);
    }
}
