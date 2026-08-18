package com.andao.skincare.module.ai.service.impl;

import com.andao.skincare.module.ai.client.AIClient;
import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.entity.AiAnalysisRecord;
import com.andao.skincare.module.ai.mapper.AiAnalysisRecordMapper;
import com.andao.skincare.module.ai.vo.SkinAnalysisHistoryVO;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;
import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductListVO;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkinAnalysisServiceImplTest {

    private final AIClient aiClient = mock(AIClient.class);
    private final ProductService productService = mock(ProductService.class);
    private final AiAnalysisRecordMapper recordMapper = mock(AiAnalysisRecordMapper.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SkinAnalysisServiceImpl service =
            new SkinAnalysisServiceImpl(
                    aiClient, productService, recordMapper, currentUserProvider,
                    redisTemplate, objectMapper);

    @Test
    void shouldCacheAIAdviceAndCombineItWithExistingProductModuleData() throws JsonProcessingException {
        SkinAnalysisDTO request = new SkinAnalysisDTO("油性皮肤", 24, "容易出油");
        AIClient.AIAdvice advice = new AIClient.AIAdvice(
                "模拟分析", List.of("建议一", "建议二"));
        List<ProductListVO> products = List.of(
                product(1L), product(2L), product(3L), product(4L));
        when(aiClient.analyze(request)).thenReturn(advice);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1001L);
        when(recordMapper.insert(any(AiAnalysisRecord.class))).thenReturn(1);
        when(productService.list(any(ProductQueryDTO.class))).thenReturn(products);

        SkinAnalysisVO result = service.analyze(request);

        assertThat(result.analysis()).isEqualTo(advice.analysis());
        assertThat(result.suggestions()).isEqualTo(advice.suggestions());
        assertThat(result.recommendedProducts()).containsExactlyElementsOf(products.subList(0, 3));
        ArgumentCaptor<AiAnalysisRecord> recordCaptor = ArgumentCaptor.forClass(AiAnalysisRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getUserId()).isEqualTo(1001L);
        assertThat(recordCaptor.getValue().getSkinType()).isEqualTo("油性皮肤");
        assertThat(recordCaptor.getValue().getProblem()).isEqualTo("容易出油");
        assertThat(recordCaptor.getValue().getAnalysisResult()).isEqualTo("模拟分析");
        verify(aiClient).analyze(request);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(
                keyCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(objectMapper.writeValueAsString(advice)),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(30)));
        assertThat(keyCaptor.getValue()).matches("ai:skin:[0-9a-f]{64}");
        verify(productService).list(any(ProductQueryDTO.class));
    }

    @Test
    void shouldUseCachedAdviceAndStillSaveUserHistory() throws JsonProcessingException {
        SkinAnalysisDTO request = new SkinAnalysisDTO("干性皮肤", 26, "容易干燥");
        AIClient.AIAdvice cachedAdvice = new AIClient.AIAdvice(
                "缓存分析", List.of("缓存建议"));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any(String.class)))
                .thenReturn(objectMapper.writeValueAsString(cachedAdvice));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1001L);
        when(recordMapper.insert(any(AiAnalysisRecord.class))).thenReturn(1);
        when(productService.list(any(ProductQueryDTO.class))).thenReturn(List.of(product(1L)));

        SkinAnalysisVO result = service.analyze(request);

        assertThat(result.analysis()).isEqualTo("缓存分析");
        assertThat(result.suggestions()).containsExactly("缓存建议");
        verify(aiClient, never()).analyze(any(SkinAnalysisDTO.class));
        verify(valueOperations, never()).set(any(String.class), any(String.class), any(Duration.class));
        ArgumentCaptor<AiAnalysisRecord> recordCaptor = ArgumentCaptor.forClass(AiAnalysisRecord.class);
        verify(recordMapper).insert(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getUserId()).isEqualTo(1001L);
        assertThat(recordCaptor.getValue().getAnalysisResult()).isEqualTo("缓存分析");
        verify(productService).list(any(ProductQueryDTO.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnCurrentUsersHistoryAsVO() {
        LocalDateTime latestTime = LocalDateTime.of(2026, 8, 18, 10, 0);
        AiAnalysisRecord latest = record(2L, "干性皮肤", 26, "干燥", "加强保湿", latestTime);
        AiAnalysisRecord earlier = record(1L, "油性皮肤", 25, "出油", "温和清洁", latestTime.minusDays(1));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1001L);
        when(recordMapper.selectList(any(Wrapper.class))).thenReturn(List.of(latest, earlier));

        List<SkinAnalysisHistoryVO> result = service.history();

        assertThat(result).extracting(SkinAnalysisHistoryVO::id).containsExactly(2L, 1L);
        assertThat(result.get(0).analysisResult()).isEqualTo("加强保湿");
        verify(currentUserProvider).getCurrentUserId();
        verify(recordMapper).selectList(any(Wrapper.class));
    }

    private AiAnalysisRecord record(Long id, String skinType, Integer age, String problem,
                                    String analysisResult, LocalDateTime createTime) {
        AiAnalysisRecord record = new AiAnalysisRecord();
        record.setId(id);
        record.setUserId(1001L);
        record.setSkinType(skinType);
        record.setAge(age);
        record.setProblem(problem);
        record.setAnalysisResult(analysisResult);
        record.setCreateTime(createTime);
        return record;
    }

    private ProductListVO product(Long id) {
        return new ProductListVO(
                id, 10L, "基础护肤", "商品" + id, null, null,
                new BigDecimal("99.00"), 100, 0);
    }
}
