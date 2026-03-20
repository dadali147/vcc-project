package com.vcc.card.webhook;

import java.math.BigDecimal;

/**
 * Webhook 充值回调处理桥接接口
 * 
 * 由于 vcc-card 不能直接依赖 vcc-finance（循环依赖），
 * 通过此接口解耦：vcc-card 定义接口，vcc-finance 提供实现。
 */
public interface WebhookRechargeHandler
{
    /**
     * 充值成功处理：更新 vcc_recharge 状态为成功
     *
     * @param orderNo 订单号
     * @return 是否处理成功
     */
    boolean handleRechargeSuccess(String orderNo);

    /**
     * 充值失败处理：更新 vcc_recharge 状态为失败，并补偿用户余额
     *
     * @param orderNo    订单号
     * @param failReason 失败原因
     * @return 是否处理成功
     */
    boolean handleRechargeFailure(String orderNo, String failReason);
}
