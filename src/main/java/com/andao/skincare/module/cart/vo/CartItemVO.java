package com.andao.skincare.module.cart.vo;

import java.math.BigDecimal;

public record CartItemVO(
        Long productId,
        String productName,
        String coverUrl,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal,
        Integer stock,
        Boolean available
) {
}
