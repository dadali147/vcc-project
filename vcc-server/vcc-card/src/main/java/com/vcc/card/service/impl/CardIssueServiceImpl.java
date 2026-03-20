package com.vcc.card.service.impl;

import com.vcc.card.domain.Card;
import com.vcc.card.domain.CardIssueItem;
import com.vcc.card.domain.CardIssueRequest;
import com.vcc.card.dto.CardIssueCreateRequest;
import com.vcc.card.mapper.CardIssueItemMapper;
import com.vcc.card.mapper.CardIssueRequestMapper;
import com.vcc.card.service.ICardIssueService;
import com.vcc.common.exception.ServiceException;
import com.vcc.holder.domain.VccCardHolder;
import com.vcc.holder.mapper.VccCardHolderMapper;
import com.vcc.upstream.YeeVccClient;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 开卡流程 服务实现
 *
 * 状态流转：PENDING → PROCESSING → COMPLETED / PARTIAL / FAILED
 * 事务隔离：每张卡片单独事务，开卡失败不影响其他卡片
 */
@Service
public class CardIssueServiceImpl implements ICardIssueService
{
    private static final Logger log = LoggerFactory.getLogger(CardIssueServiceImpl.class);

    private final CardIssueRequestMapper requestMapper;
    private final CardIssueItemMapper itemMapper;
    private final VccCardHolderMapper holderMapper;
    private final YeeVccClient yeeVccClient;
    private final CardPersistService cardPersistService;

    public CardIssueServiceImpl(CardIssueRequestMapper requestMapper,
                                CardIssueItemMapper itemMapper,
                                VccCardHolderMapper holderMapper,
                                YeeVccClient yeeVccClient,
                                CardPersistService cardPersistService)
    {
        this.requestMapper = requestMapper;
        this.itemMapper = itemMapper;
        this.holderMapper = holderMapper;
        this.yeeVccClient = yeeVccClient;
        this.cardPersistService = cardPersistService;
    }

    @Override
    @Transactional
    public CardIssueRequest createAndProcess(Long merchantId, CardIssueCreateRequest dto)
    {
        // KYC 门控：检查商户KYC（简化：校验持卡人是否属于该商户）
        validateItems(merchantId, dto);

        // 计算实际总开卡数量（所有明细的 quantity 之和）
        int totalCardCount = 0;
        for (CardIssueCreateRequest.ItemRequest itemDto : dto.items())
        {
            totalCardCount += Math.max(itemDto.quantity(), 1);
        }

        // 1. 创建申请单
        CardIssueRequest issueRequest = new CardIssueRequest();
        issueRequest.setMerchantId(merchantId);
        issueRequest.setBatchNo(generateBatchNo());
        issueRequest.setCardType(dto.cardType());
        issueRequest.setBinId(dto.binId());
        issueRequest.setTotalCount(totalCardCount);
        issueRequest.setStatus(CardIssueRequest.STATUS_PENDING);
        issueRequest.setRemark(dto.remark());
        requestMapper.insert(issueRequest);

        // 2. 批量插入明细（PENDING 状态）
        List<CardIssueItem> items = new ArrayList<>();
        for (CardIssueCreateRequest.ItemRequest itemDto : dto.items())
        {
            CardIssueItem item = new CardIssueItem();
            item.setRequestId(issueRequest.getId());
            item.setHolderId(itemDto.holderId());
            item.setCardProductId(itemDto.cardProductId());
            item.setQuantity(itemDto.quantity());
            item.setStatus(CardIssueItem.STATUS_PENDING);
            items.add(item);
        }
        itemMapper.batchInsert(items);

        // 3. 变更申请单为 PROCESSING
        requestMapper.updateStatus(issueRequest.getId(), CardIssueRequest.STATUS_PROCESSING,
                0, 0, null);
        issueRequest.setStatus(CardIssueRequest.STATUS_PROCESSING);

        // 4. 逐条处理（每条明细按 quantity 开出多张卡，每张卡独立事务，失败不回滚其他）
        List<CardIssueItem> savedItems = itemMapper.selectByRequestId(issueRequest.getId());
        int successCount = 0;
        int failCount = 0;

        for (CardIssueItem item : savedItems)
        {
            int quantity = item.getQuantity() != null && item.getQuantity() > 0 ? item.getQuantity() : 1;
            int[] result = processOneItem(issueRequest, item, merchantId, quantity);
            successCount += result[0];
            failCount += result[1];
        }

        // 5. 更新申请单最终状态（successCount/failCount 按实际开卡张数统计）
        String finalStatus = determineFinalStatus(successCount, failCount, totalCardCount);
        requestMapper.updateStatus(issueRequest.getId(), finalStatus, successCount, failCount, new Date());
        issueRequest.setStatus(finalStatus);
        issueRequest.setSuccessCount(successCount);
        issueRequest.setFailCount(failCount);

        log.info("开卡申请处理完成, batchNo={}, totalCount={}, successCount={}, failCount={}, status={}",
                issueRequest.getBatchNo(), totalCardCount, successCount, failCount, finalStatus);

        return issueRequest;
    }

    /**
     * 处理单条开卡明细，按 quantity 开出多张卡（独立事务，互不影响）。
     *
     * <p>P1-2 修复：按 quantity 循环调用上游开卡，每次开 1 张卡。
     * 上游返回的 cardList 全量落库，校验实际落库卡数量与 quantity 是否一致。</p>
     *
     * @return int[2]：[0] = 成功开卡数，[1] = 失败开卡数
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int[] processOneItem(CardIssueRequest issueRequest, CardIssueItem item, Long merchantId, int quantity)
    {
        int itemSuccess = 0;
        int itemFail = 0;

        try
        {
            // 获取持卡人上游ID
            VccCardHolder holder = holderMapper.selectById(item.getHolderId());
            if (holder == null || !merchantId.equals(holder.getMerchantId()))
            {
                markItemFailed(item, "持卡人不存在或无权访问: " + item.getHolderId());
                return new int[]{0, quantity};
            }
            if (VccCardHolder.STATUS_DISABLED.equals(holder.getStatus()))
            {
                markItemFailed(item, "持卡人已禁用");
                return new int[]{0, quantity};
            }

            // 标记处理中
            item.setStatus(CardIssueItem.STATUS_PROCESSING);
            itemMapper.updateStatus(item);

            String cardBinId;
            if (item.getCardProductId() != null)
            {
                cardBinId = item.getCardProductId();
            }
            else if (issueRequest.getBinId() != null)
            {
                cardBinId = String.valueOf(issueRequest.getBinId());
            }
            else
            {
                cardBinId = null;
            }

            int cardType = CardIssueRequest.CARD_TYPE_PREPAID.equals(issueRequest.getCardType())
                    ? Card.TYPE_PREPAID : Card.TYPE_BUDGET;

            List<Long> createdCardIds = new ArrayList<>();

            // 按 quantity 逐张开卡
            for (int i = 0; i < quantity; i++)
            {
                try
                {
                    // 调用上游开卡（每次开 1 张）
                    YeeVccRequests.OpenCardRequest openReq = new YeeVccRequests.OpenCardRequest();
                    openReq.setCardholderId(holder.getUpstreamHolderId());
                    openReq.setCardType(cardType);
                    if (cardBinId != null)
                    {
                        openReq.setCardBinId(cardBinId);
                    }
                    openReq.setCurrency("USD");

                    YeeVccApiResponse<YeeVccModels.OpenCardTaskData> response = yeeVccClient.openCard(openReq);
                    if (!response.isSuccess() || response.getData() == null)
                    {
                        log.error("上游开卡失败, itemId={}, holderId={}, cardIndex={}/{}, reason={}",
                                item.getId(), item.getHolderId(), i + 1, quantity, response.getMessage());
                        itemFail++;
                        continue;
                    }

                    // 轮询结果
                    Long taskId = response.getData().getTaskId();
                    YeeVccModels.OpenCardTaskData taskResult = pollOpenCardResult(taskId);
                    if (taskResult == null || taskResult.getCardList() == null || taskResult.getCardList().isEmpty())
                    {
                        log.error("开卡超时或无结果, itemId={}, holderId={}, cardIndex={}/{}, taskId={}",
                                item.getId(), item.getHolderId(), i + 1, quantity, taskId);
                        itemFail++;
                        continue;
                    }

                    // 按上游返回 cardList 全量落库（通常为 1 张，但兼容上游批量返回）
                    for (YeeVccModels.CardData cardData : taskResult.getCardList())
                    {
                        Card card = cardPersistService.saveCardInTransaction(
                                item.getHolderId(), merchantId,
                                cardBinId, "USD", cardType, null, cardData);
                        createdCardIds.add(card.getId());
                        itemSuccess++;
                    }
                }
                catch (Exception e)
                {
                    log.error("开卡异常, itemId={}, holderId={}, cardIndex={}/{}",
                            item.getId(), item.getHolderId(), i + 1, quantity, e);
                    itemFail++;
                }
            }

            // 回填明细状态
            if (itemSuccess == 0)
            {
                markItemFailed(item, "全部" + quantity + "张卡开卡失败");
            }
            else
            {
                // 回填第一张卡的ID（兼容旧逻辑），记录实际成功/失败数
                item.setCardId(createdCardIds.get(0));
                if (itemFail > 0)
                {
                    item.setStatus(CardIssueItem.STATUS_SUCCESS);
                    item.setFailReason("部分开卡失败: 期望" + quantity + "张, 成功" + itemSuccess + "张, 失败" + itemFail + "张");
                    log.warn("开卡明细部分成功, itemId={}, quantity={}, success={}, fail={}",
                            item.getId(), quantity, itemSuccess, itemFail);
                }
                else
                {
                    item.setStatus(CardIssueItem.STATUS_SUCCESS);
                }
                itemMapper.updateStatus(item);
            }

            // 校验实际落库卡数量与 quantity 是否一致
            if (itemSuccess != quantity)
            {
                log.warn("开卡数量不一致, itemId={}, holderId={}, expected={}, actual={}",
                        item.getId(), item.getHolderId(), quantity, itemSuccess);
            }

            return new int[]{itemSuccess, itemFail};
        }
        catch (Exception e)
        {
            log.error("开卡明细处理异常, itemId={}, holderId={}", item.getId(), item.getHolderId(), e);
            markItemFailed(item, e.getMessage());
            return new int[]{0, quantity};
        }
    }

    private void markItemFailed(CardIssueItem item, String reason)
    {
        item.setStatus(CardIssueItem.STATUS_FAILED);
        item.setFailReason(reason);
        itemMapper.updateStatus(item);
    }

    private YeeVccModels.OpenCardTaskData pollOpenCardResult(Long taskId)
    {
        long startTime = System.currentTimeMillis();
        long timeout = 30_000L;
        long interval = 2_000L;
        while (System.currentTimeMillis() - startTime < timeout)
        {
            try { Thread.sleep(interval); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return null; }

            YeeVccRequests.QueryOpenCardResultRequest qReq = new YeeVccRequests.QueryOpenCardResultRequest();
            qReq.setTaskId(taskId);
            YeeVccApiResponse<YeeVccModels.OpenCardTaskData> resp = yeeVccClient.queryOpenCardResult(qReq);
            if (!resp.isSuccess()) { continue; }

            YeeVccModels.OpenCardTaskData data = resp.getData();
            if (data != null && data.getStatus() != null)
            {
                if (data.getStatus() == 1) { return data; }
                if (data.getStatus() != 0) { return null; }
            }
        }
        return null;
    }

    private String determineFinalStatus(int success, int fail, int total)
    {
        if (success == total) { return CardIssueRequest.STATUS_COMPLETED; }
        if (fail == total)    { return CardIssueRequest.STATUS_FAILED; }
        return CardIssueRequest.STATUS_PARTIAL;
    }

    private void validateItems(Long merchantId, CardIssueCreateRequest dto)
    {
        if (!CardIssueRequest.CARD_TYPE_PREPAID.equals(dto.cardType())
                && !CardIssueRequest.CARD_TYPE_BUDGET.equals(dto.cardType()))
        {
            throw new ServiceException("不支持的卡类型: " + dto.cardType());
        }
        if (dto.items() == null || dto.items().isEmpty())
        {
            throw new ServiceException("开卡明细不能为空");
        }
        // KYC 门控：检查每个持卡人是否存在且属于该商户
        for (CardIssueCreateRequest.ItemRequest item : dto.items())
        {
            VccCardHolder holder = holderMapper.selectById(item.holderId());
            if (holder == null || !merchantId.equals(holder.getMerchantId()))
            {
                throw new ServiceException("持卡人不存在或无权访问: " + item.holderId());
            }
        }
    }

    @Override
    public CardIssueRequest getByBatchNo(Long merchantId, String batchNo)
    {
        CardIssueRequest req = requestMapper.selectByBatchNo(batchNo);
        if (req == null || !merchantId.equals(req.getMerchantId()))
        {
            throw new ServiceException("申请单不存在: " + batchNo);
        }
        return req;
    }

    @Override
    public List<CardIssueRequest> list(Long merchantId, CardIssueRequest query)
    {
        query.setMerchantId(merchantId);
        return requestMapper.selectList(query);
    }

    @Override
    @Transactional
    public int cancel(Long merchantId, Long requestId)
    {
        CardIssueRequest req = requestMapper.selectById(requestId);
        if (req == null || !merchantId.equals(req.getMerchantId()))
        {
            throw new ServiceException("申请单不存在或无权访问");
        }
        if (!CardIssueRequest.STATUS_PENDING.equals(req.getStatus()))
        {
            throw new ServiceException("仅 PENDING 状态的申请单可取消");
        }
        return requestMapper.updateStatus(requestId, CardIssueRequest.STATUS_CANCELLED, 0, 0, new Date());
    }

    private String generateBatchNo()
    {
        return "BATCH" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
