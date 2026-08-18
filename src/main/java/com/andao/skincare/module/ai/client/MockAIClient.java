package com.andao.skincare.module.ai.client;

import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 本阶段的本地模拟实现，不发起任何网络请求，也不依赖真实模型密钥。
 */
@Component
public class MockAIClient implements AIClient {

    @Override
    public AIAdvice analyze(SkinAnalysisDTO request) {
        String skinType = request.skinType().trim();
        String problem = request.problem().trim();
        String analysis = "根据你提供的信息：" + request.age() + "岁、" + skinType
                + "，目前主要关注“" + problem + "”。建议先以温和护理和稳定皮肤状态为主。";
        List<String> suggestions = List.of(
                "选择温和清洁产品，避免过度清洁和频繁去角质。",
                "做好基础保湿，并根据使用后的皮肤感受逐步调整产品。",
                "白天坚持防晒；如果问题持续或明显加重，建议咨询专业皮肤科医生。"
        );
        return new AIAdvice(analysis, suggestions);
    }
}
