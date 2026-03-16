package com.vcc.finance.mapper;

import java.util.List;
import com.vcc.finance.domain.Recharge;

/**
 * 充值记录 数据层
 */
public interface RechargeMapper
{
    public Recharge selectRechargeById(Long id);

    public Recharge selectRechargeByOrderNo(String orderNo);

    public List<Recharge> selectRechargeList(Recharge recharge);

    public int insertRecharge(Recharge recharge);

    public int updateRecharge(Recharge recharge);

    public int deleteRechargeById(Long id);
}
