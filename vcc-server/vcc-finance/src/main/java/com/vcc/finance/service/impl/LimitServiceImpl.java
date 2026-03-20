package com.vcc.finance.service.impl;

import com.vcc.card.domain.Card;
import com.vcc.card.mapper.CardMapper;
import com.vcc.common.exception.ServiceException;
import com.vcc.finance.domain.LimitHistory;
import com.vcc.finance.mapper.LimitHistoryMapper;
import com.vcc.finance.service.ILimitService;
import com.vcc.upstream.adapter.ChannelAwareYeeVccAdapter;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限额管理服务实现（预算卡额度调整 + 审计日志）
 */
@Service
public class LimitServiceImpl implements ILimitService
{
    private static final Logger log = LoggerFactory.getLogger(LimitServiceImpl.class);

    private final LimitHistoryMapper limitHistoryMapper;
    private final CardMapper cardMapper;
    private final ChannelAwareYeeVccAdapter yeeVccAdapter;

    public LimitServiceImpl(LimitHistoryMapper limitHistoryMapper, CardMapper cardMapper, ChannelAwareYeeVccAdapter yeeVccAdapter)
    {
        this.limitHistoryMapper = limitHistoryMapper;
        this.cardMapper = cardMapper;
        this.yeeVccAdapter = yeeVccAdapter;
    }

    @Override
    @Transactional
    public LimitHistory adjustLimit(Long merchantId, Long cardId, String limitType,
                                    BigDecimal newAmount, Long operatorId, String reason)
    {
        // 校验卡片归属
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new ServiceException("卡片不存在: " + cardId);
        }
        // 仅允许预算卡调整额度
        if (card.getCardType() == null || card.getCardType() != Card.TYPE_BUDGET)
        {
            throw new ServiceException("仅预算卡支持额度调整");
        }
        if (card.getStatus() != null && card.getStatus() == Card.STATUS_CANCELLED)
        {
            throw new ServiceException("已注销的卡片不允许调整额度");
        }

        BigDecimal beforeAmount = card.getBudgetAmount() != null ? card.getBudgetAmount() : BigDecimal.ZERO;

        // 调用上游更新额度（通过充值接口实现）
        YeeVccRequests.RechargeRequest upstreamReq = new YeeVccRequests.RechargeRequest();
        upstreamReq.setCardId(card.getUpstreamCardId());
        upstreamReq.setAmount(newAmount);
        upstreamReq.setCurrency(card.getCurrency() != null ? card.getCurrency() : "USD");
        upstreamReq.setOrderId("LIMIT" + System.currentTimeMillis());

        YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccAdapter.recharge(upstreamReq);
        if (!response.isSuccess())
        {
            throw new ServiceException("上游额度调整失败: " + response.getMessage());
        }

        // 更新本地卡片 budgetAmount
        Card update = new Card();
        update.setId(cardId);
        update.setBudgetAmount(newAmount);
        cardMapper.updateCard(update);

        // 写入审计日志
        LimitHistory history = new LimitHistory();
        history.setMerchantId(merchantId);
        history.setCardId(cardId);
        history.setLimitType(limitType);
        history.setBeforeAmount(beforeAmount);
        history.setAfterAmount(newAmount);
        history.setOperatorId(operatorId);
        history.setReason(reason);
        limitHistoryMapper.insert(history);

        log.info("[AUDIT] 额度调整 cardId={} {} -> {} by operator={}", cardId, beforeAmount, newAmount, operatorId);
        return history;
    }

    @Override
    public List<LimitHistory> getHistoryByCard(Long merchantId, Long cardId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new ServiceException("卡片不存在: " + cardId);
        }
        return limitHistoryMapper.selectByCardId(cardId);
    }
}
