package com.vcc.holder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CardHolderCreateRequest(
        @NotBlank(message = "持卡人姓名不能为空")
        @Size(max = 100, message = "持卡人姓名长度不能超过100")
        String holderName,
        @Size(max = 50, message = "名字长度不能超过50")
        String firstName,
        @Size(max = 50, message = "姓氏长度不能超过50")
        String lastName,
        @NotBlank(message = "手机号不能为空")
        @Size(max = 20, message = "手机号长度不能超过20")
        String mobile,
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100")
        String email,
        @NotBlank(message = "证件号不能为空")
        @Size(max = 50, message = "证件号长度不能超过50")
        String idCard,
        @NotBlank(message = "地址不能为空")
        @Size(max = 255, message = "地址长度不能超过255")
        String address,
        @Size(max = 20, message = "邮编长度不能超过20")
        String postCode,
        @Size(max = 50, message = "国家长度不能超过50")
        String country,
        @Size(max = 500, message = "备注长度不能超过500")
        String remark)
{
}
