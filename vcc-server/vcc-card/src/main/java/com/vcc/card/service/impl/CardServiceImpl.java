package com.vcc.card.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.CardHolderMapper;
import com.vcc.card.service.ICardService;
import com.vcc.system.service.ISystemConfigService;
import com.vcc.upstream.YeeVccClient;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;

/**
 * 卡片 服务实现
 */
@Service
public class CardServiceImpl implements ICardService
{
    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);

    /** 开卡轮询最大等待时间(ms) */
    private static final long OPEN_CARD_TIMEOUT_MS = 30_000L;
    /** 开卡轮询间隔(ms) */
    private static final long OPEN_CARD_POLL_INTERVAL_MS = 2_000L;
    /** 开卡任务状态：成功 */
    private static final int TASK_STATUS_SUCCESS = 1;
    /** 开卡任务状态：处理中 */
    private static final int TASK_STATUS_PROCESSING = 0;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private CardHolderMapper cardHolderMapper;

    @Autowired
    private YeeVccClient yeeVccClient;

    @Autowired
    private ISystemConfigService systemConfigService;

    @Override
    public Card selectCardById(Long id)
    {
        return cardMapper.selectCardById(id);
    }

    @Override
    public List<Card> selectCardList(Card card)
    {
        return cardMapper.selectCardList(card);
    }

    @Override
    @Transactional
    public Card openCard(Long holderId, String cardBinId, String currency, Integer cardType,
                         BigDecimal amount, Long userId)
    {
        // 风控：开卡功能开关
        String openEnabled = systemConfigService.get("risk.card.open.enabled");
        if ("false".equalsIgnoreCase(openEnabled))
        {
            throw new RuntimeException("开卡功能已关闭，请联系管理员");
        }

        // 校验持卡人
        CardHolder holder = cardHolderMapper.selectCardHolderById(holderId);
        if (holder == null)
        {
            throw new RuntimeException("持卡人不存在: " + holderId);
        }

        // 调用上游开卡
        YeeVccRequests.OpenCardRequest request = new YeeVccRequests.OpenCardRequest();
        request.setCardBinId(cardBinId);
        request.setCardholderId(holder.getUpstreamHolderId());
        request.setCurrency(currency);
        request.setCardType(cardType);
        if (amount != null)
        {
            request.setAmount(amount);
        }

        YeeVccApiResponse<YeeVccModels.OpenCardTaskData> openResponse = yeeVccClient.openCard(request);
        if (!openResponse.isSuccess())
        {
            throw new RuntimeException("上游开卡请求失败: " + openResponse.getMessage());
        }

        Long taskId = openResponse.getData().getTaskId();
        log.info("开卡任务已提交, taskId={}", taskId);

        // 异步轮询开卡结果
        YeeVccModels.OpenCardTaskData taskResult = pollOpenCardResult(taskId);
        if (taskResult == null || taskResult.getCardList() == null || taskResult.getCardList().isEmpty())
        {
            throw new RuntimeException("开卡超时或失败, taskId=" + taskId);
        }

        // 取第一张卡
        YeeVccModels.CardData cardData = taskResult.getCardList().get(0);

        // 保存卡片信息
        Card card = new Card();
        card.setHolderId(holderId);
        card.setUserId(userId);
        card.setCardNoMask(cardData.getMaskedCardNumber());
        card.setCardBinId(cardBinId);
        card.setCardType(cardType);
        card.setCurrency(currency);
        card.setUpstreamCardId(cardData.getCardId());
        card.setBalance(cardData.getBalance() != null ? cardData.getBalance() : BigDecimal.ZERO);

        if (cardType == Card.TYPE_BUDGET && amount != null)
        {
            card.setBudgetAmount(amount);
        }

        // 判断状态
        String upstreamStatus = cardData.getCardStatus() != null ? cardData.getCardStatus() : cardData.getStatus();
        if ("ACTIVE".equalsIgnoreCase(upstreamStatus))
        {
            card.setStatus(Card.STATUS_ACTIVE);
            card.setActivatedAt(new Date());
        }
        else
        {
            card.setStatus(Card.STATUS_INACTIVE);
        }

        cardMapper.insertCard(card);
        log.info("开卡成功, cardId={}, upstreamCardId={}", card.getId(), card.getUpstreamCardId());
        return card;
    }

    private YeeVccModels.OpenCardTaskData pollOpenCardResult(Long taskId)
    {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < OPEN_CARD_TIMEOUT_MS)
        {
            try
            {
                Thread.sleep(OPEN_CARD_POLL_INTERVAL_MS);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                throw new RuntimeException("开卡轮询被中断", e);
            }

            YeeVccRequests.QueryOpenCardResultRequest queryRequest = new YeeVccRequests.QueryOpenCardResultRequest();
            queryRequest.setTaskId(taskId);
            YeeVccApiResponse<YeeVccModels.OpenCardTaskData> queryResponse = yeeVccClient.queryOpenCardResult(queryRequest);

            if (!queryResponse.isSuccess())
            {
                log.warn("查询开卡结果失败, taskId={}, msg={}", taskId, queryResponse.getMessage());
                continue;
            }

            YeeVccModels.OpenCardTaskData data = queryResponse.getData();
            if (data != null && data.getStatus() != null && data.getStatus() == TASK_STATUS_SUCCESS)
            {
                return data;
            }

            if (data != null && data.getStatus() != null && data.getStatus() != TASK_STATUS_PROCESSING)
            {
                log.error("开卡任务失败, taskId={}, status={}", taskId, data.getStatus());
                return null;
            }
        }

        log.error("开卡轮询超时, taskId={}", taskId);
        return null;
    }

    @Override
    @Transactional
    public int activateCard(Long cardId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }
        if (card.getStatus() != Card.STATUS_INACTIVE)
        {
            throw new RuntimeException("卡片状态不允许激活, 当前状态: " + card.getStatus());
        }

        YeeVccRequests.ActivateCardRequest request = new YeeVccRequests.ActivateCardRequest();
        request.setCardId(card.getUpstreamCardId());
        YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.activateCard(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("上游激活卡片失败: " + response.getMessage());
        }

        Card update = new Card();
        update.setId(cardId);
        update.setStatus(Card.STATUS_ACTIVE);
        update.setActivatedAt(new Date());
        return cardMapper.updateCard(update);
    }

    @Override
    @Transactional
    public int freezeCard(Long cardId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }
        if (card.getStatus() != Card.STATUS_ACTIVE)
        {
            throw new RuntimeException("卡片状态不允许冻结, 当前状态: " + card.getStatus());
        }

        YeeVccRequests.FreezeCardRequest request = new YeeVccRequests.FreezeCardRequest();
        request.setCardId(card.getUpstreamCardId());
        YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.freezeCard(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("上游冻结卡片失败: " + response.getMessage());
        }

        Card update = new Card();
        update.setId(cardId);
        update.setStatus(Card.STATUS_FROZEN);
        return cardMapper.updateCard(update);
    }

    @Override
    @Transactional
    public int unfreezeCard(Long cardId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }
        if (card.getStatus() != Card.STATUS_FROZEN)
        {
            throw new RuntimeException("卡片状态不允许解冻, 当前状态: " + card.getStatus());
        }

        YeeVccRequests.UnfreezeCardRequest request = new YeeVccRequests.UnfreezeCardRequest();
        request.setCardId(card.getUpstreamCardId());
        YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.unfreezeCard(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("上游解冻卡片失败: " + response.getMessage());
        }

        Card update = new Card();
        update.setId(cardId);
        update.setStatus(Card.STATUS_ACTIVE);
        return cardMapper.updateCard(update);
    }

    @Override
    @Transactional
    public int cancelCard(Long cardId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }
        if (card.getStatus() == Card.STATUS_CANCELLED)
        {
            throw new RuntimeException("卡片已销卡");
        }

        YeeVccRequests.CancelCardRequest request = new YeeVccRequests.CancelCardRequest();
        request.setCardId(card.getUpstreamCardId());
        request.setRefundCurrency(card.getCurrency());
        YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.cancelCard(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("上游销卡失败: " + response.getMessage());
        }

        Card update = new Card();
        update.setId(cardId);
        update.setStatus(Card.STATUS_CANCELLED);
        update.setCancelledAt(new Date());
        return cardMapper.updateCard(update);
    }

    @Override
    public Map<String, String> getCardKeyInfo(Long cardId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }

        YeeVccRequests.GetCardKeyInfoRequest request = new YeeVccRequests.GetCardKeyInfoRequest();
        request.setCardId(card.getUpstreamCardId());
        YeeVccApiResponse<YeeVccModels.CardKeyInfoData> response = yeeVccClient.getCardKeyInfo(request);
        if (!response.isSuccess())
        {
            throw new RuntimeException("查询三要素失败: " + response.getMessage());
        }

        YeeVccModels.CardKeyInfoData data = response.getData();
        Map<String, String> result = new HashMap<>();
        result.put("cardNumber", data.getCardNumber());
        result.put("cvv", data.getCvv());
        result.put("expiryDate", data.getExpiryDate());
        return result;
    }
}
