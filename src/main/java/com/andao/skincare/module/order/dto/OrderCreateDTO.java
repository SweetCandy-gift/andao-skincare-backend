package com.andao.skincare.module.order.dto;

import jakarta.validation.constraints.Size;

public record OrderCreateDTO(
        @Size(max = 200, message = "订单备注不能超过200个字符")
        String remark
) {
}
