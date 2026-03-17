package com.vcc.card.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.card.domain.Card;
import com.vcc.card.domain.CardHolder;
import com.vcc.card.domain.Transaction;
import com.vcc.card.mapper.CardMapper;
import com.vcc.card.mapper.CardHolderMapper;
import com.vcc.card.mapper.TransactionMapper;
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

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CardPersistService cardPersistService;

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

        // VCC-010: 校验归属，只能使用自己的持卡人
        if (!holder.getUserId().equals(userId))
        {
            throw new RuntimeException("无权使用该持卡人: " + holderId);
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

        // VCC-010: 异步轮询开卡结果（在事务外执行，避免长时间占用连接）
        YeeVccModels.OpenCardTaskData taskResult = pollOpenCardResult(taskId);
        if (taskResult == null || taskResult.getCardList() == null || taskResult.getCardList().isEmpty())
        {
            throw new RuntimeException("开卡超时或失败, taskId=" + taskId);
        }

        // 取第一张卡
        YeeVccModels.CardData cardData = taskResult.getCardList().get(0);

        // 通过独立 Bean 保存卡片信息，确保 @Transactional 代理生效
        return cardPersistService.saveCardInTransaction(holderId, userId, cardBinId, currency, cardType, amount, cardData);
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

    /**
     * 校验卡片归属
     */
    private Card checkCardOwnership(Long cardId, Long userId)
    {
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }
        if (!card.getUserId().equals(userId))
        {
            throw new RuntimeException("无权操作该卡片: " + cardId);
        }
        return card;
    }

    @Override
    @Transactional
    public int activateCard(Long cardId, Long userId)
    {
        Card card = checkCardOwnership(cardId, userId);
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
    public int freezeCard(Long cardId, Long userId)
    {
        Card card = checkCardOwnership(cardId, userId);
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
    public int unfreezeCard(Long cardId, Long userId)
    {
        Card card = checkCardOwnership(cardId, userId);
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
    public int cancelCard(Long cardId, Long userId)
    {
        Card card = checkCardOwnership(cardId, userId);
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
    public Map<String, String> getCardKeyInfo(Long cardId, Long userId)
    {
        Card card = checkCardOwnership(cardId, userId);

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

    @Override
    public List<Card> selectCardListAdmin(Card card)
    {
        // 管理端不加 userId 过滤，直接查全部
        return cardMapper.selectCardList(card);
    }

    @Override
    public Map<String, Object> getCardStats()
    {
        Map<String, Object> stats = new HashMap<>();

        // 查询全部卡片
        List<Card> allCards = cardMapper.selectCardList(new Card());
        stats.put("totalCards", allCards.size());

        // 按状态统计
        long activeCards = allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_ACTIVE).count();
        long frozenCards = allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_FROZEN).count();
        long cancelledCards = allCards.stream().filter(c -> c.getStatus() != null && c.getStatus() == Card.STATUS_CANCELLED).count();
        stats.put("activeCards", activeCards);
        stats.put("frozenCards", frozenCards);
        stats.put("cancelledCards", cancelledCards);

        // 按卡类型统计
        long prepaidCards = allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_PREPAID).count();
        long budgetCards = allCards.stream().filter(c -> c.getCardType() != null && c.getCardType() == Card.TYPE_BUDGET).count();
        stats.put("prepaidCards", prepaidCards);
        stats.put("budgetCards", budgetCards);

        // 按 cardBinId 分组统计
        Map<String, Long> cardBinStats = allCards.stream()
                .filter(c -> c.getCardBinId() != null)
                .collect(Collectors.groupingBy(Card::getCardBinId, Collectors.counting()));
        stats.put("cardBinStats", cardBinStats);

        // 近30天每天开卡数
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> dailyOpenCards = new LinkedHashMap<>();
        for (int i = 29; i >= 0; i--)
        {
            dailyOpenCards.put(today.minusDays(i).format(fmt), 0L);
        }
        LocalDate thirtyDaysAgo = today.minusDays(29);
        for (Card c : allCards)
        {
            if (c.getCreateTime() != null)
            {
                LocalDate createDate = new java.sql.Date(c.getCreateTime().getTime()).toLocalDate();
                if (!createDate.isBefore(thirtyDaysAgo) && !createDate.isAfter(today))
                {
                    String dateKey = createDate.format(fmt);
                    dailyOpenCards.merge(dateKey, 1L, Long::sum);
                }
            }
        }
        stats.put("dailyOpenCards", dailyOpenCards);

        return stats;
    }

    @Override
    public List<Map<String, Object>> selectTransactionListAdmin(Map<String, Object> params)
    {
        Transaction query = new Transaction();
        if (params.get("userId") != null)
        {
            query.setUserId(Long.valueOf(params.get("userId").toString()));
        }
        if (params.get("cardId") != null)
        {
            query.setCardId(Long.valueOf(params.get("cardId").toString()));
        }
        if (params.get("txnType") != null)
        {
            query.setTxnType(params.get("txnType").toString());
        }
        if (params.get("status") != null)
        {
            query.setStatus(Integer.valueOf(params.get("status").toString()));
        }

        List<Transaction> txnList = transactionMapper.selectTransactionList(query);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction txn : txnList)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("id", txn.getId());
            map.put("txnId", txn.getTxnId());
            map.put("cardId", txn.getCardId());
            map.put("userId", txn.getUserId());
            map.put("txnType", txn.getTxnType());
            map.put("amount", txn.getAmount());
            map.put("currency", txn.getCurrency());
            map.put("merchantName", txn.getMerchantName());
            map.put("merchantMcc", txn.getMerchantMcc());
            map.put("merchantCountry", txn.getMerchantCountry());
            map.put("status", txn.getStatus());
            map.put("authCode", txn.getAuthCode());
            map.put("failReason", txn.getFailReason());
            map.put("txnTime", txn.getTxnTime());
            map.put("createTime", txn.getCreateTime());
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> getTodayTransactionStats()
    {
        Map<String, Object> stats = new HashMap<>();
        Transaction query = new Transaction();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("beginTxnTime", today + " 00:00:00");
        queryParams.put("endTxnTime", today + " 23:59:59");
        query.setParams(queryParams);

        List<Transaction> todayTxns = transactionMapper.selectTransactionList(query);

        stats.put("totalCount", todayTxns.size());
        BigDecimal totalAmount = todayTxns.stream()
                .filter(t -> t.getAmount() != null)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAmount", totalAmount);

        long successCount = todayTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == Transaction.STATUS_SUCCESS).count();
        long failedCount = todayTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == Transaction.STATUS_FAILED).count();
        long processingCount = todayTxns.stream().filter(t -> t.getStatus() != null && t.getStatus() == Transaction.STATUS_PROCESSING).count();
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("processingCount", processingCount);

        // 按交易类型分组统计
        Map<String, Long> byType = todayTxns.stream()
                .filter(t -> t.getTxnType() != null)
                .collect(Collectors.groupingBy(Transaction::getTxnType, Collectors.counting()));
        stats.put("byType", byType);

        return stats;
    }
}
