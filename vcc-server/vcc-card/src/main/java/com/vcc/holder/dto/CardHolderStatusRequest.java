package com.vcc.holder.dto;

import jakarta.validation.constraints.NotBlank;

public record CardHolderStatusRequest(@NotBlank(message = "状态不能为空") String status)
{
}
