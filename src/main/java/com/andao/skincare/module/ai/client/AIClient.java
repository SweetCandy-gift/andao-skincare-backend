package com.andao.skincare.module.ai.client;

import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;

import java.util.List;

/**
 * 隔离具体大模型供应商的调用协议。业务层只依赖该接口，未来接入真实模型时只需新增实现，
 * 无需修改 Controller、护肤分析流程或商品推荐逻辑。
 */
public interface AIClient {

    AIAdvice analyze(SkinAnalysisDTO request);

    record AIAdvice(String analysis, List<String> suggestions) {
    }
}
