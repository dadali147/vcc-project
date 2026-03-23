package com.vcc.web.controller.merchant;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.vcc.common.core.controller.BaseController;
import com.vcc.common.core.domain.AjaxResult;
import com.vcc.common.core.page.TableDataInfo;
import com.vcc.card.domain.Vcc3dsOtpLog;
import com.vcc.card.service.IVcc3dsOtpLogService;

/**
 * 3DS OTP验证码记录查询接口（商户端）
 */
@RestController
@RequestMapping("/api/merchant/3ds")
public class Merchant3dsController extends BaseController
{
    @Autowired
    private IVcc3dsOtpLogService otpLogService;

    /**
     * 分页查询当前用户的3DS验证码记录
     * 参数: pageNum, pageSize, cardNoMask(可选), beginTime, endTime
     */
    @GetMapping("/list")
    public TableDataInfo list(Vcc3dsOtpLog query)
    {
        query.setUserId(getUserId());
        startPage();
        List<Vcc3dsOtpLog> list = otpLogService.selectVcc3dsOtpLogList(query);
        return getDataTable(list);
    }

    /**
     * 查询3DS记录详情（仅限当前用户自己的记录）
     */
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id)
    {
        Vcc3dsOtpLog log = otpLogService.selectVcc3dsOtpLogById(id);
        if (log == null || !getUserId().equals(log.getUserId()))
        {
            return error("记录不存在或无权访问");
        }
        return success(log);
    }
}
