package com.vcc.finance.service;

import com.vcc.finance.domain.LimitHistory;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限额管理 服务层（预算卡额度调整）
 */
public interface ILimitService
{
    /**
     * 调整预算卡额度，自动记录历史
     *
     * @param merchantId 商户ID
     * @param cardId     卡片ID
     * @param limitType  限额类型：SINGLE/DAILY/MONTHLY
     * @param newAmount  调整后额度
     * @param operatorId 操作人ID
     * @param reason     操作原因
     */
    LimitHistory adjustLimit(Long merchantId, Long cardId, String limitType,
                             BigDecimal newAmount, Long operatorId, String reason);

    /**
     * 查询卡片的限额调整历史
     */
    List<LimitHistory> getHistoryByCard(Long merchantId, Long cardId);
}
