package com.vcc.holder.service;

import com.vcc.holder.domain.VccCardHolder;
import com.vcc.holder.dto.CardHolderCreateRequest;
import com.vcc.holder.dto.CardHolderStatusRequest;
import com.vcc.holder.dto.CardHolderUpdateRequest;

import java.util.List;

public interface VccCardHolderService
{
    VccCardHolder create(Long merchantId, CardHolderCreateRequest request);

    VccCardHolder update(Long merchantId, Long holderId, CardHolderUpdateRequest request);

    int changeStatus(Long merchantId, Long holderId, CardHolderStatusRequest request);

    int delete(Long merchantId, Long holderId);

    VccCardHolder getById(Long merchantId, Long holderId);

    List<VccCardHolder> list(Long merchantId, VccCardHolder query);
}
