package com.vcc.card.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.vcc.card.domain.Vcc3dsOtpLog;
import com.vcc.card.mapper.Vcc3dsOtpLogMapper;
import com.vcc.card.service.IVcc3dsOtpLogService;

/**
 * 3DS OTP验证码记录 服务实现层
 */
@Service
public class Vcc3dsOtpLogServiceImpl implements IVcc3dsOtpLogService
{
    @Autowired
    private Vcc3dsOtpLogMapper otpLogMapper;

    @Override
    public Vcc3dsOtpLog selectVcc3dsOtpLogById(Long id)
    {
        return otpLogMapper.selectVcc3dsOtpLogById(id);
    }

    @Override
    public List<Vcc3dsOtpLog> selectVcc3dsOtpLogList(Vcc3dsOtpLog log)
    {
        return otpLogMapper.selectVcc3dsOtpLogList(log);
    }

    @Override
    public int insertVcc3dsOtpLog(Vcc3dsOtpLog log)
    {
        return otpLogMapper.insertVcc3dsOtpLog(log);
    }
}
