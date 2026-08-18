package com.andao.skincare.module.ai.service;

import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;
import com.andao.skincare.module.ai.vo.SkinAnalysisHistoryVO;

import java.util.List;

public interface SkinAnalysisService {

    SkinAnalysisVO analyze(SkinAnalysisDTO request);

    List<SkinAnalysisHistoryVO> history();
}
