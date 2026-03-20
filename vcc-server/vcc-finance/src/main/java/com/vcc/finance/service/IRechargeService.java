package com.vcc.finance.service;

import java.math.BigDecimal;
import java.util.List;
import com.vcc.finance.domain.Recharge;

/**
 * 充值 服务层
 */
public interface IRechargeService
{
    public Recharge selectRechargeById(Long id);

    public Recharge selectRechargeByOrderNo(String orderNo);

    public List<Recharge> selectRechargeList(Recharge recharge);

    /**
     * 发起充值（储值卡充值/预算卡设额度）
     * P1 修复：增加 orderNo 参数用于幂等校验，移除 fee 参数（服务端计算）
     */
    public Recharge submitRecharge(Long userId, Long cardId, BigDecimal amount, String currency, String orderNo);

    /**
     * 查询充值结果（需传入userId做归属校验）
     */
    public Recharge queryRechargeResult(Long userId, String orderNo);

    /**
     * USDT 到账处理（账户充值）
     */
    public Recharge handleUsdtArrival(Long userId, BigDecimal amount, String currency, String txHash);
}
