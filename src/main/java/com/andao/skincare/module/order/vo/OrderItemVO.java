package com.andao.skincare.module.order.vo;

import java.math.BigDecimal;

public record OrderItemVO(
        Long productId,
        String productName,
        String coverUrl,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
