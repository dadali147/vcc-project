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

    /**
     * VCC-016: 查询用户今日充值总额（数据库层面，带日期条件）
     */
    public java.math.BigDecimal selectTodayRechargeTotal(Long userId, String startTime, String endTime);

    /**
     * SELECT FOR UPDATE 锁定充值记录（防止并发重复补偿）
     */
    public Recharge selectRechargeForUpdateById(Long id);

    /**
     * 乐观锁更新状态：仅当当前状态=expectedStatus时才更新
     */
    public int updateRechargeStatus(@org.apache.ibatis.annotations.Param("id") Long id,
                                    @org.apache.ibatis.annotations.Param("newStatus") Integer newStatus,
                                    @org.apache.ibatis.annotations.Param("expectedStatus") Integer expectedStatus,
                                    @org.apache.ibatis.annotations.Param("failReason") String failReason,
                                    @org.apache.ibatis.annotations.Param("completedAt") java.util.Date completedAt);

    public int insertRecharge(Recharge recharge);

    public int updateRecharge(Recharge recharge);

    public int deleteRechargeById(Long id);
}
