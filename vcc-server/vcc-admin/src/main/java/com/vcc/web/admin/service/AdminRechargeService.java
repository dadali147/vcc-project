package com.vcc.web.admin.service;

/**
 * 充值管理 服务接口
 */
public interface AdminRechargeService
{
    /**
     * 查询充值列表
     */
    Object selectRechargeList();

    /**
     * 根据ID查询充值详情
     */
    Object selectRechargeById(Long id);

    /**
     * 审批充值
     */
    void approveRecharge(Long rechargeId, String status);
}
