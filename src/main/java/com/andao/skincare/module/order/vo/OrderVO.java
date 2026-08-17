package com.andao.skincare.module.order.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderVO(
        Long id,
        String orderNo,
        Long userId,
        BigDecimal totalAmount,
        Integer totalQuantity,
        Integer status,
        String remark,
        LocalDateTime createdAt,
        List<OrderItemVO> items
) {
}
