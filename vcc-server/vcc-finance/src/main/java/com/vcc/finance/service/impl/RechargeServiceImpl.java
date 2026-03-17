package com.vcc.finance.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vcc.common.utils.StringUtils;
import com.vcc.card.domain.Card;
import com.vcc.card.mapper.CardMapper;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.finance.service.IRechargeService;
import com.vcc.system.service.ISystemConfigService;
import com.vcc.upstream.YeeVccClient;
import com.vcc.upstream.dto.YeeVccApiResponse;
import com.vcc.upstream.dto.YeeVccModels;
import com.vcc.upstream.dto.YeeVccRequests;
import com.vcc.user.service.IUserAccountService;

/**
 * 充值 服务实现
 */
@Service
public class RechargeServiceImpl implements IRechargeService
{
    private static final Logger log = LoggerFactory.getLogger(RechargeServiceImpl.class);

    @Autowired
    private RechargeMapper rechargeMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private YeeVccClient yeeVccClient;

    @Autowired
    private IUserAccountService userAccountService;

    @Autowired
    private ISystemConfigService systemConfigService;

    @Override
    public Recharge selectRechargeById(Long id)
    {
        return rechargeMapper.selectRechargeById(id);
    }

    @Override
    public Recharge selectRechargeByOrderNo(String orderNo)
    {
        return rechargeMapper.selectRechargeByOrderNo(orderNo);
    }

    @Override
    public List<Recharge> selectRechargeList(Recharge recharge)
    {
        return rechargeMapper.selectRechargeList(recharge);
    }

    @Override
    @Transactional
    public Recharge submitRecharge(Long userId, Long cardId, BigDecimal amount, String currency, BigDecimal fee)
    {
        // 风控：充值功能开关
        String rechargeEnabled = systemConfigService.get("risk.recharge.enabled");
        if ("false".equalsIgnoreCase(rechargeEnabled))
        {
            throw new RuntimeException("充值功能已关闭，请联系管理员");
        }

        // 风控：单笔充值上限
        String singleLimitStr = systemConfigService.get("risk.single.recharge.limit");
        if (StringUtils.isNotEmpty(singleLimitStr))
        {
            BigDecimal singleLimit = new BigDecimal(singleLimitStr);
            if (amount.compareTo(singleLimit) > 0)
            {
                throw new RuntimeException("单笔充值金额超过上限: " + singleLimit + " USD");
            }
        }

        // VCC-016: 风控：日充值上限（数据库层面查询，减少并发窗口）
        String dailyLimitStr = systemConfigService.get("risk.daily.recharge.limit");
        if (StringUtils.isNotEmpty(dailyLimitStr))
        {
            BigDecimal dailyLimit = new BigDecimal(dailyLimitStr);
            // 使用数据库查询今日充值总额（更精确，减少并发窗口）
            String[] todayRange = getTodayTimeRange();
            BigDecimal todayTotal = rechargeMapper.selectTodayRechargeTotal(userId, todayRange[0], todayRange[1]);
            if (todayTotal.add(amount).compareTo(dailyLimit) > 0)
            {
                throw new RuntimeException("今日充值总额将超过上限: " + dailyLimit + " USD，今日已充值: " + todayTotal + " USD");
            }
        }

        // VCC-008: 获取卡片信息，使用 upstreamCardId 调用上游
        Card card = cardMapper.selectCardById(cardId);
        if (card == null)
        {
            throw new RuntimeException("卡片不存在: " + cardId);
        }
        // 校验卡片归属，防止给别人的卡充值
        if (!card.getUserId().equals(userId))
        {
            throw new RuntimeException("无权操作该卡片: " + cardId);
        }
        if (StringUtils.isEmpty(card.getUpstreamCardId()))
        {
            throw new RuntimeException("卡片未同步到上游: " + cardId);
        }

        // VCC-008: 服务端计算手续费（根据卡类型和费率配置）
        BigDecimal calculatedFee = calculateRechargeFee(amount, card.getCardType());
        BigDecimal actualFee = (fee != null && fee.compareTo(calculatedFee) == 0) ? fee : calculatedFee;
        BigDecimal actualAmount = amount.subtract(actualFee);

        if (actualAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("充值金额不足以支付手续费");
        }

        // VCC-008: 先扣减用户账户余额（原子操作）
        boolean deducted = userAccountService.deductBalance(userId, currency, amount);
        if (!deducted)
        {
            throw new RuntimeException("账户余额不足，无法完成充值");
        }

        String orderNo = generateOrderNo();

        // 创建充值记录
        Recharge recharge = new Recharge();
        recharge.setOrderNo(orderNo);
        recharge.setUserId(userId);
        recharge.setCardId(cardId);
        recharge.setAmount(amount);
        recharge.setCurrency(currency);
        recharge.setFee(actualFee);
        recharge.setActualAmount(actualAmount);
        recharge.setStatus(Recharge.STATUS_PENDING);
        recharge.setRechargeType(Recharge.TYPE_PREPAID);

        // VCC-008: 使用 upstreamCardId 调用上游充值
        YeeVccRequests.RechargeRequest request = new YeeVccRequests.RechargeRequest();
        request.setCardId(card.getUpstreamCardId());
        request.setAmount(actualAmount);
        request.setCurrency(currency);
        request.setOrderId(orderNo);

        try
        {
            YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.recharge(request);
            if (response.isSuccess())
            {
                // VCC-008: 保持 PENDING 状态，等待回调或查询确认
                if (response.getData() != null)
                {
                    recharge.setUpstreamOrderNo(response.getData().getOrderId());
                }
                log.info("充值请求已提交, orderNo={}, amount={}, upstreamOrderNo={}", 
                        orderNo, actualAmount, recharge.getUpstreamOrderNo());
            }
            else
            {
                // 上游调用失败，回滚用户余额
                userAccountService.addBalance(userId, currency, amount);
                recharge.setStatus(Recharge.STATUS_FAILED);
                recharge.setFailReason(response.getMessage());
                log.warn("充值失败, orderNo={}, msg={}", orderNo, response.getMessage());
            }
        }
        catch (Exception e)
        {
            // 异常时回滚用户余额
            userAccountService.addBalance(userId, currency, amount);
            recharge.setStatus(Recharge.STATUS_FAILED);
            recharge.setFailReason(e.getMessage());
            log.error("充值异常, orderNo={}", orderNo, e);
        }

        rechargeMapper.insertRecharge(recharge);
        return recharge;
    }

    /**
     * VCC-008: 计算充值手续费
     * 根据卡类型和系统费率配置计算
     */
    private BigDecimal calculateRechargeFee(BigDecimal amount, Integer cardType)
    {
        // 默认费率：储值卡 1%，预算卡 0.5%
        String feeRateKey = (cardType != null && cardType == Card.TYPE_BUDGET) 
                ? "fee.recharge.budget.rate" 
                : "fee.recharge.prepaid.rate";
        String feeRateStr = systemConfigService.get(feeRateKey);
        
        BigDecimal feeRate;
        if (StringUtils.isNotEmpty(feeRateStr))
        {
            feeRate = new BigDecimal(feeRateStr);
        }
        else
        {
            // 默认费率
            feeRate = (cardType != null && cardType == Card.TYPE_BUDGET) 
                    ? new BigDecimal("0.005") 
                    : new BigDecimal("0.01");
        }
        
        return amount.multiply(feeRate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public Recharge queryRechargeResult(String orderNo)
    {
        Recharge recharge = rechargeMapper.selectRechargeByOrderNo(orderNo);
        if (recharge == null)
        {
            throw new RuntimeException("充值记录不存在: " + orderNo);
        }

        // 如果状态还是待处理，查询上游
        if (recharge.getStatus() == Recharge.STATUS_PENDING)
        {
            YeeVccRequests.QueryRechargeResultRequest request = new YeeVccRequests.QueryRechargeResultRequest();
            request.setOrderId(orderNo);
            YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.queryRechargeResult(request);
            if (response.isSuccess() && response.getData() != null)
            {
                String status = response.getData().getStatus();
                if ("SUCCESS".equalsIgnoreCase(status))
                {
                    recharge.setStatus(Recharge.STATUS_SUCCESS);
                    recharge.setCompletedAt(new Date());
                }
                else if ("FAILED".equalsIgnoreCase(status))
                {
                    recharge.setStatus(Recharge.STATUS_FAILED);
                    recharge.setFailReason(response.getData().getMessage());
                    recharge.setCompletedAt(new Date());
                    // 充值失败，补偿用户余额
                    userAccountService.addBalance(recharge.getUserId(), recharge.getCurrency(), recharge.getAmount());
                    log.info("充值失败余额补偿: orderNo={}, userId={}, amount={}",
                            recharge.getOrderNo(), recharge.getUserId(), recharge.getAmount());
                }
                rechargeMapper.updateRecharge(recharge);
            }
        }
        return recharge;
    }

    @Override
    @Transactional
    public Recharge handleUsdtArrival(Long userId, BigDecimal amount, String currency, String txHash)
    {
        String orderNo = generateOrderNo();

        // 增加用户账户余额
        userAccountService.addBalance(userId, currency, amount);

        // 记录充值流水
        Recharge recharge = new Recharge();
        recharge.setOrderNo(orderNo);
        recharge.setUserId(userId);
        recharge.setAmount(amount);
        recharge.setCurrency(currency);
        recharge.setFee(BigDecimal.ZERO);
        recharge.setActualAmount(amount);
        recharge.setStatus(Recharge.STATUS_SUCCESS);
        recharge.setRechargeType(Recharge.TYPE_PREPAID);
        recharge.setTxHash(txHash);
        recharge.setCompletedAt(new Date());

        rechargeMapper.insertRecharge(recharge);
        log.info("USDT到账处理完成, userId={}, amount={}, txHash={}", userId, amount, txHash);
        return recharge;
    }

    private String generateOrderNo()
    {
        return "RCH" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private boolean isToday(java.util.Date date)
    {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal2.setTime(date);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
                && cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    /**
     * VCC-016: 获取今日时间范围（用于数据库查询）
     * @return [startTime, endTime] 格式：yyyy-MM-dd HH:mm:ss
     */
    private String[] getTodayTimeRange()
    {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        java.time.LocalDateTime endOfDay = startOfDay.plusDays(1);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new String[]{startOfDay.format(formatter), endOfDay.format(formatter)};
    }
}
