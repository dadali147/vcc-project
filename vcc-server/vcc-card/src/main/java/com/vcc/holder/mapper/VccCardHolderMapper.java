package com.vcc.holder.mapper;

import com.vcc.holder.domain.VccCardHolder;

import java.util.List;

public interface VccCardHolderMapper
{
    VccCardHolder selectById(Long id);

    List<VccCardHolder> selectList(VccCardHolder query);

    int insert(VccCardHolder holder);

    int update(VccCardHolder holder);

    int updateStatus(Long id, Long merchantId, String status);

    int logicDelete(Long id, Long merchantId);
}
