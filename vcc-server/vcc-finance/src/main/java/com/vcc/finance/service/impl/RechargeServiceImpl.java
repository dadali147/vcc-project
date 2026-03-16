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
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.finance.service.IRechargeService;
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
    private YeeVccClient yeeVccClient;

    @Autowired
    private IUserAccountService userAccountService;

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
        String orderNo = generateOrderNo();
        BigDecimal actualAmount = amount.subtract(fee != null ? fee : BigDecimal.ZERO);

        // 创建充值记录
        Recharge recharge = new Recharge();
        recharge.setOrderNo(orderNo);
        recharge.setUserId(userId);
        recharge.setCardId(cardId);
        recharge.setAmount(amount);
        recharge.setCurrency(currency);
        recharge.setFee(fee != null ? fee : BigDecimal.ZERO);
        recharge.setActualAmount(actualAmount);
        recharge.setStatus(Recharge.STATUS_PENDING);
        recharge.setRechargeType(Recharge.TYPE_PREPAID);

        // 调用上游充值
        YeeVccRequests.RechargeRequest request = new YeeVccRequests.RechargeRequest();
        request.setCardId(String.valueOf(cardId));
        request.setAmount(actualAmount);
        request.setCurrency(currency);
        request.setOrderId(orderNo);

        try
        {
            YeeVccApiResponse<YeeVccModels.OperationData> response = yeeVccClient.recharge(request);
            if (response.isSuccess())
            {
                recharge.setStatus(Recharge.STATUS_SUCCESS);
                recharge.setCompletedAt(new Date());
                if (response.getData() != null)
                {
                    recharge.setUpstreamOrderNo(response.getData().getOrderId());
                }
                log.info("充值成功, orderNo={}, amount={}", orderNo, actualAmount);
            }
            else
            {
                recharge.setStatus(Recharge.STATUS_FAILED);
                recharge.setFailReason(response.getMessage());
                log.warn("充值失败, orderNo={}, msg={}", orderNo, response.getMessage());
            }
        }
        catch (Exception e)
        {
            recharge.setStatus(Recharge.STATUS_FAILED);
            recharge.setFailReason(e.getMessage());
            log.error("充值异常, orderNo={}", orderNo, e);
        }

        rechargeMapper.insertRecharge(recharge);
        return recharge;
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
}
