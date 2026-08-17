package com.andao.skincare.module.product.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailVO(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String subtitle,
        String description,
        String coverUrl,
        BigDecimal price,
        Integer stock,
        Integer sales,
        LocalDateTime createdAt
) {
}
