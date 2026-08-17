package com.andao.skincare.module.product.vo;

import java.math.BigDecimal;

public record ProductListVO(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String subtitle,
        String coverUrl,
        BigDecimal price,
        Integer stock,
        Integer sales
) {
}
