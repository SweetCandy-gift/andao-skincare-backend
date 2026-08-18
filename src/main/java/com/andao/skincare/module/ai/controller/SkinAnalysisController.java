package com.andao.skincare.module.ai.controller;

import com.andao.skincare.common.result.Result;
import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import com.andao.skincare.module.ai.service.SkinAnalysisService;
import com.andao.skincare.module.ai.vo.SkinAnalysisVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/skin")
public class SkinAnalysisController {

    private final SkinAnalysisService skinAnalysisService;

    public SkinAnalysisController(SkinAnalysisService skinAnalysisService) {
        this.skinAnalysisService = skinAnalysisService;
    }

    @PostMapping("/analyze")
    public Result<SkinAnalysisVO> analyze(@Valid @RequestBody SkinAnalysisDTO request) {
        return Result.success(skinAnalysisService.analyze(request));
    }
}
