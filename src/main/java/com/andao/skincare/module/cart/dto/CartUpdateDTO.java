package com.andao.skincare.module.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartUpdateDTO(
        @NotNull(message = "商品ID不能为空")
        @Positive(message = "商品ID必须为正数")
        Long productId,

        @NotNull(message = "商品数量不能为空")
        @Positive(message = "商品数量必须为正数")
        @Max(value = 999, message = "商品数量不能超过999")
        Integer quantity
) {
}
