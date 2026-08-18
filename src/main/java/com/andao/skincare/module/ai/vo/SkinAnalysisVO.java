package com.andao.skincare.module.ai.vo;

import com.andao.skincare.module.product.vo.ProductListVO;

import java.util.List;

public record SkinAnalysisVO(
        String analysis,
        List<String> suggestions,
        List<ProductListVO> recommendedProducts
) {
}
