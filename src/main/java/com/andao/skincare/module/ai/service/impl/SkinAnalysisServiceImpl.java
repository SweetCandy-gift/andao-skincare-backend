package com.andao.skincare.module.ai.service.impl;

import com.andao.skincare.module.ai.client.AIClient;
import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.entity.AiAnalysisRecord;
import com.andao.skincare.module.ai.mapper.AiAnalysisRecordMapper;
import com.andao.skincare.module.ai.service.SkinAnalysisService;
import com.andao.skincare.module.ai.vo.SkinAnalysisHistoryVO;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;
import com.andao.skincare.module.product.dto.ProductQueryDTO;
import com.andao.skincare.module.product.service.ProductService;
import com.andao.skincare.module.product.vo.ProductListVO;
import com.andao.skincare.module.user.service.CurrentUserProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class SkinAnalysisServiceImpl implements SkinAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(SkinAnalysisServiceImpl.class);
    private static final String AI_CACHE_KEY_PREFIX = "ai:skin:";
    private static final Duration AI_CACHE_TTL = Duration.ofMinutes(30);
    private static final int MAX_RECOMMENDED_PRODUCTS = 3;

    private final AIClient aiClient;
    private final ProductService productService;
    private final AiAnalysisRecordMapper recordMapper;
    private final CurrentUserProvider currentUserProvider;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SkinAnalysisServiceImpl(AIClient aiClient,
                                   ProductService productService,
                                   AiAnalysisRecordMapper recordMapper,
                                   CurrentUserProvider currentUserProvider,
                                   StringRedisTemplate redisTemplate,
                                   ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.productService = productService;
        this.recordMapper = recordMapper;
        this.currentUserProvider = currentUserProvider;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkinAnalysisVO analyze(SkinAnalysisDTO request) {
        String cacheKey = buildCacheKey(request);
        AIClient.AIAdvice advice = getCachedAdvice(cacheKey);
        if (advice == null) {
            advice = aiClient.analyze(request);
            cacheAdvice(cacheKey, advice);
        }

        /*
         * 缓存用于减少相同输入的重复 AI 计算，但不能替代历史记录：缓存会过期且不区分用户，
         * 历史记录仍需绑定当前 JWT 用户，作为用户可长期查询的业务数据。
         */
        saveAnalysisRecord(request, advice.analysis());
        // 推荐商品始终来自商品模块，AI 层不复制价格、库存等可能变化的商城数据。
        List<ProductListVO> recommendedProducts = productService.list(new ProductQueryDTO())
                .stream()
                .limit(MAX_RECOMMENDED_PRODUCTS)
                .toList();
        return new SkinAnalysisVO(
                advice.analysis(), advice.suggestions(), recommendedProducts);
    }

    private String buildCacheKey(SkinAnalysisDTO request) {
        String skinType = request.skinType().trim();
        String problem = request.problem().trim();
        String canonicalInput = skinType.length() + ":" + skinType
                + "|" + request.age() + "|" + problem.length() + ":" + problem;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalInput.getBytes(StandardCharsets.UTF_8));
            return AI_CACHE_KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前Java环境不支持SHA-256", exception);
        }
    }

    private AIClient.AIAdvice getCachedAdvice(String cacheKey) {
        try {
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue == null || cachedValue.isBlank()) {
                return null;
            }
            return objectMapper.readValue(cachedValue, AIClient.AIAdvice.class);
        } catch (JsonProcessingException | RuntimeException exception) {
            // 缓存是性能优化；Redis 暂时不可用或缓存内容损坏时，降级为重新调用 AIClient。
            log.warn("读取AI分析缓存失败，cacheKey={}", cacheKey, exception);
            return null;
        }
    }

    private void cacheAdvice(String cacheKey, AIClient.AIAdvice advice) {
        try {
            String cacheValue = objectMapper.writeValueAsString(advice);
            redisTemplate.opsForValue().set(cacheKey, cacheValue, AI_CACHE_TTL);
        } catch (JsonProcessingException | RuntimeException exception) {
            // 缓存写入失败不影响分析结果和用户历史记录的保存。
            log.warn("写入AI分析缓存失败，cacheKey={}", cacheKey, exception);
        }
    }

    @Override
    public List<SkinAnalysisHistoryVO> history() {
        Long userId = currentUserProvider.getCurrentUserId();
        return recordMapper.selectList(new LambdaQueryWrapper<AiAnalysisRecord>()
                        .eq(AiAnalysisRecord::getUserId, userId)
                        .orderByDesc(AiAnalysisRecord::getCreateTime)
                        .orderByDesc(AiAnalysisRecord::getId))
                .stream()
                .map(this::toHistoryVO)
                .toList();
    }

    private void saveAnalysisRecord(SkinAnalysisDTO request, String analysis) {
        AiAnalysisRecord record = new AiAnalysisRecord();
        /*
         * 历史记录绑定 JWT 用户 ID，既能让用户回顾肤质变化，也确保查询时只返回自己的数据，
         * 不依赖客户端提交可被伪造的 userId。
         */
        record.setUserId(currentUserProvider.getCurrentUserId());
        record.setSkinType(request.skinType().trim());
        record.setAge(request.age());
        record.setProblem(request.problem().trim());
        record.setAnalysisResult(analysis);
        record.setCreateTime(LocalDateTime.now());
        if (recordMapper.insert(record) != 1) {
            throw new IllegalStateException("AI分析记录保存失败");
        }
    }

    private SkinAnalysisHistoryVO toHistoryVO(AiAnalysisRecord record) {
        return new SkinAnalysisHistoryVO(
                record.getId(), record.getSkinType(), record.getAge(), record.getProblem(),
                record.getAnalysisResult(), record.getCreateTime());
    }
}
