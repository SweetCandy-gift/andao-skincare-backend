package com.andao.skincare.module.ai.service.impl;

import com.andao.skincare.module.ai.client.AIClient;
import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;
import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductListVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkinAnalysisServiceImplTest {

    private final AIClient aiClient = mock(AIClient.class);
    private final ProductService productService = mock(ProductService.class);
    private final SkinAnalysisServiceImpl service =
            new SkinAnalysisServiceImpl(aiClient, productService);

    @Test
    void shouldCombineAIAdviceWithExistingProductModuleData() {
        SkinAnalysisDTO request = new SkinAnalysisDTO("油性皮肤", 24, "容易出油");
        AIClient.AIAdvice advice = new AIClient.AIAdvice(
                "模拟分析", List.of("建议一", "建议二"));
        List<ProductListVO> products = List.of(
                product(1L), product(2L), product(3L), product(4L));
        when(aiClient.analyze(request)).thenReturn(advice);
        when(productService.list(any(ProductQueryDTO.class))).thenReturn(products);

        SkinAnalysisVO result = service.analyze(request);

        assertThat(result.analysis()).isEqualTo(advice.analysis());
        assertThat(result.suggestions()).isEqualTo(advice.suggestions());
        assertThat(result.recommendedProducts()).containsExactlyElementsOf(products.subList(0, 3));
        verify(aiClient).analyze(request);
        verify(productService).list(any(ProductQueryDTO.class));
    }

    private ProductListVO product(Long id) {
        return new ProductListVO(
                id, 10L, "基础护肤", "商品" + id, null, null,
                new BigDecimal("99.00"), 100, 0);
    }
}
