package com.andao.skincare.module.ai.vo;

import java.time.LocalDateTime;

public record SkinAnalysisHistoryVO(
        Long id,
        String skinType,
        Integer age,
        String problem,
        String analysisResult,
        LocalDateTime createTime
) {
}
