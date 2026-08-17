package com.andao.skincare.module.cart.vo;

import java.math.BigDecimal;
import java.util.List;

public record CartVO(
        Long userId,
        List<CartItemVO> items,
        Integer totalQuantity,
        BigDecimal totalAmount
) {
}
