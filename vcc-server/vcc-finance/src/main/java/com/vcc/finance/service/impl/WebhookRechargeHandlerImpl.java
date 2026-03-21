package com.vcc.finance.service.impl;

import java.math.BigDecimal;
import java.util.Date;

import com.vcc.card.webhook.WebhookRechargeHandler;
import com.vcc.finance.domain.Recharge;
import com.vcc.finance.mapper.RechargeMapper;
import com.vcc.user.service.IUserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Webhook 充值回调处理实现
 * 放在 vcc-finance 模块，因为此模块同时依赖 vcc-card 和 vcc-user
 */
@Service
public class WebhookRechargeHandlerImpl implements WebhookRechargeHandler
{
    private static final Logger log = LoggerFactory.getLogger(WebhookRechargeHandlerImpl.class);

    @Autowired
    private RechargeMapper rechargeMapper;

    @Autowired
    private IUserAccountService userAccountService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRechargeSuccess(String orderNo)
    {
        Recharge recharge = rechargeMapper.selectRechargeByOrderNo(orderNo);
        if (recharge == null)
        {
            log.error("充值成功回调：未找到充值记录 orderNo={}", orderNo);
            return false;
        }

        // 仅处理待处理状态（幂等保护）
        if (recharge.getStatus() != Recharge.STATUS_PENDING)
        {
            log.warn("充值记录已处理，跳过: orderNo={}, currentStatus={}", orderNo, recharge.getStatus());
            return true;
        }

        int updated = rechargeMapper.updateRechargeStatus(
                recharge.getId(),
                Recharge.STATUS_SUCCESS,
                Recharge.STATUS_PENDING,
                null,
                new Date());

        if (updated > 0)
        {
            log.info("充值成功: orderNo={}, amount={}", orderNo, recharge.getAmount());
            return true;
        }
        else
        {
            log.warn("充值状态更新失败（乐观锁冲突）: orderNo={}", orderNo);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleRechargeFailure(String orderNo, String failReason)
    {
        Recharge recharge = rechargeMapper.selectRechargeByOrderNo(orderNo);
        if (recharge == null)
        {
            log.error("充值失败回调：未找到充值记录 orderNo={}", orderNo);
            return false;
        }

        // 快速幂等检查（无锁，减少不必要的锁竞争）
        if (recharge.getStatus() != Recharge.STATUS_PENDING)
        {
            log.warn("充值记录已处理，跳过: orderNo={}, currentStatus={}", orderNo, recharge.getStatus());
            return true;
        }

        // BUG-002 fix: SELECT FOR UPDATE 行级锁，确保并发补偿幂等性
        // 防止两个线程同时通过快速检查后双重补偿用户余额
        Recharge locked = rechargeMapper.selectRechargeForUpdateById(recharge.getId());
        if (locked == null || locked.getStatus() != Recharge.STATUS_PENDING)
        {
            log.warn("充值记录已被并发处理，跳过: orderNo={}", orderNo);
            return true;
        }

        int updated = rechargeMapper.updateRechargeStatus(
                locked.getId(),
                Recharge.STATUS_FAILED,
                Recharge.STATUS_PENDING,
                failReason,
                new Date());

        if (updated > 0)
        {
            // 补偿用户余额（使用锁定后的记录，确保数据一致性）
            BigDecimal compensateAmount = locked.getAmount();
            String currency = locked.getCurrency();
            Long userId = locked.getUserId();

            if (userId != null && compensateAmount != null && compensateAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                userAccountService.addBalance(userId, currency, compensateAmount);
                log.info("充值失败已补偿余额: orderNo={}, userId={}, amount={}, currency={}",
                        orderNo, userId, compensateAmount, currency);
            }
            else
            {
                log.warn("充值失败但无法补偿余额（数据不完整）: orderNo={}, userId={}, amount={}",
                        orderNo, userId, compensateAmount);
            }
            return true;
        }
        else
        {
            log.warn("充值状态更新失败（乐观锁冲突）: orderNo={}", orderNo);
            return false;
        }
    }
}
