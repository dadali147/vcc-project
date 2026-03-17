package com.vcc.web.admin.controller;

import com.vcc.common.core.domain.AjaxResult;
import com.vcc.web.admin.service.AdminRechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 充值管理 API 接口
 */
@RestController
@RequestMapping("/admin/recharge")
public class AdminRechargeController
{
    @Autowired
    private AdminRechargeService adminRechargeService;

    /**
     * 查询充值列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasRole('admin')")
    public AjaxResult list()
    {
        return AjaxResult.success(adminRechargeService.selectRechargeList());
    }

    /**
     * 查询充值详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasRole('admin')")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return AjaxResult.success(adminRechargeService.selectRechargeById(id));
    }

    /**
     * 审批充值
     */
    @PostMapping("/approve")
    @PreAuthorize("@ss.hasRole('admin')")
    public AjaxResult approve(@RequestParam Long rechargeId, @RequestParam String status)
    {
        adminRechargeService.approveRecharge(rechargeId, status);
        return AjaxResult.success("审批成功");
    }
}

