package com.vcc.card.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新卡片备注
 */
public record CardUpdateRemarkRequest(
    @Size(max = 500, message = "备注不能超过500字")
    String remark
) {}
