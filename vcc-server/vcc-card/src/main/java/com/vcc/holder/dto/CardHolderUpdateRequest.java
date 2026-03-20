package com.vcc.holder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CardHolderUpdateRequest(
        @Size(max = 100, message = "持卡人姓名长度不能超过100")
        String holderName,
        @Size(max = 50, message = "名字长度不能超过50")
        String firstName,
        @Size(max = 50, message = "姓氏长度不能超过50")
        String lastName,
        @Size(max = 20, message = "手机号长度不能超过20")
        String mobile,
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100")
        String email,
        @Size(max = 50, message = "证件号长度不能超过50")
        String idCard,
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
