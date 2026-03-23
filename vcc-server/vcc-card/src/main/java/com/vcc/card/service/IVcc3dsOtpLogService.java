package com.vcc.card.service;

import java.util.List;
import com.vcc.card.domain.Vcc3dsOtpLog;

/**
 * 3DS OTP验证码记录 服务层
 */
public interface IVcc3dsOtpLogService
{
    Vcc3dsOtpLog selectVcc3dsOtpLogById(Long id);

    List<Vcc3dsOtpLog> selectVcc3dsOtpLogList(Vcc3dsOtpLog log);

    int insertVcc3dsOtpLog(Vcc3dsOtpLog log);
}
