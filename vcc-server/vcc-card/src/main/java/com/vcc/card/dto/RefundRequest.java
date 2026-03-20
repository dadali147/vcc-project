package com.vcc.card.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 退款请求
 */
public record RefundRequest(
    @NotNull(message = "原始交易ID不能为空")
    Long originalTxnId,

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    BigDecimal refundAmount,

    @Size(max = 500, message = "退款原因不能超过500字")
    String reason
) {}
