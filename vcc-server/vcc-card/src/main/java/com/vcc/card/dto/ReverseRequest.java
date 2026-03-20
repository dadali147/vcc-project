package com.vcc.card.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 撤销请求
 */
public record ReverseRequest(
    @NotNull(message = "原始交易ID不能为空")
    Long originalTxnId,

    @Size(max = 500, message = "撤销原因不能超过500字")
    String reason
) {}
