package com.vcc.channel.service.impl;

import com.vcc.channel.domain.VccMerchantBin;
import com.vcc.channel.mapper.VccMerchantBinMapper;
import com.vcc.channel.service.IMerchantBinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商户BIN分配 Service 实现
 */
@Service
public class MerchantBinServiceImpl implements IMerchantBinService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantBinServiceImpl.class);

    @Autowired
    private VccMerchantBinMapper merchantBinMapper;

    @Override
    public VccMerchantBin getById(Long id)
    {
        return merchantBinMapper.selectById(id);
    }

    @Override
    public List<VccMerchantBin> listByMerchantId(Long merchantId)
    {
        return merchantBinMapper.selectByMerchantId(merchantId);
    }

    @Override
    public List<VccMerchantBin> listMerchantBins(VccMerchantBin query)
    {
        return merchantBinMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignBin(Long merchantId, Long binId, Long operatorId, String remark)
    {
        VccMerchantBin existing = merchantBinMapper.selectByMerchantAndBin(merchantId, binId);
        if (existing != null)
        {
            throw new IllegalStateException("该BIN已分配给此商户，merchantId=" + merchantId + ", binId=" + binId);
        }
        VccMerchantBin record = new VccMerchantBin();
        record.setMerchantId(merchantId);
        record.setBinId(binId);
        record.setStatus(VccMerchantBin.STATUS_ASSIGNED);
        record.setAssignedBy(operatorId);
        record.setRemark(remark);
        log.info("[AUDIT] 分配BIN给商户: merchantId={}, binId={}, operator={}", merchantId, binId, operatorId);
        return merchantBinMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeBin(Long id, Long operatorId)
    {
        log.info("[AUDIT] 撤销BIN分配: id={}, operator={}", id, operatorId);
        VccMerchantBin update = new VccMerchantBin();
        update.setId(id);
        update.setStatus(VccMerchantBin.STATUS_DISABLED);
        return merchantBinMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableAssignment(Long id)
    {
        log.info("[AUDIT] 启用BIN分配: id={}", id);
        VccMerchantBin update = new VccMerchantBin();
        update.setId(id);
        update.setStatus(VccMerchantBin.STATUS_ASSIGNED);
        return merchantBinMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableAssignment(Long id)
    {
        log.info("[AUDIT] 停用BIN分配: id={}", id);
        VccMerchantBin update = new VccMerchantBin();
        update.setId(id);
        update.setStatus(VccMerchantBin.STATUS_DISABLED);
        return merchantBinMapper.update(update);
    }
}
