package com.andao.skincare.module.ai.service;

import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;

public interface SkinAnalysisService {

    SkinAnalysisVO analyze(SkinAnalysisDTO request);
}
