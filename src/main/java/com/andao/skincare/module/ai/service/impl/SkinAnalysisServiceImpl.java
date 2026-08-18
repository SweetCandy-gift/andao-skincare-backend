package com.andao.skincare.module.ai.service.impl;

import com.andao.skincare.module.ai.client.AIClient;
import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.service.SkinAnalysisService;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;
import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductListVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkinAnalysisServiceImpl implements SkinAnalysisService {

    private static final int MAX_RECOMMENDED_PRODUCTS = 3;

    private final AIClient aiClient;
    private final ProductService productService;

    public SkinAnalysisServiceImpl(AIClient aiClient, ProductService productService) {
        this.aiClient = aiClient;
        this.productService = productService;
    }

    @Override
    public SkinAnalysisVO analyze(SkinAnalysisDTO request) {
        AIClient.AIAdvice advice = aiClient.analyze(request);
        // 推荐商品始终来自商品模块，AI 层不复制价格、库存等可能变化的商城数据。
        List<ProductListVO> recommendedProducts = productService.list(new ProductQueryDTO())
                .stream()
                .limit(MAX_RECOMMENDED_PRODUCTS)
                .toList();
        return new SkinAnalysisVO(
                advice.analysis(), advice.suggestions(), recommendedProducts);
    }
}
