package com.vcc.card.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 批量开卡请求 DTO
 */
public record CardIssueCreateRequest(
        @NotBlank(message = "卡类型不能为空")
        String cardType,

        Long binId,

        @NotEmpty(message = "开卡明细不能为空")
        @Valid
        List<ItemRequest> items,

        String remark
) {
    public record ItemRequest(
            @NotNull(message = "持卡人ID不能为空")
            Long holderId,

            String cardProductId,

            @Min(value = 1, message = "开卡数量最少1张")
            int quantity
    ) {}
}
