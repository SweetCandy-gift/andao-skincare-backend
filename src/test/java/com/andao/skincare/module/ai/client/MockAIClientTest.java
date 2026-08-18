package com.andao.skincare.module.ai.client;

import com.andao.skincare.module.ai.dto.SkinAnalysisDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAIClientTest {

    private final MockAIClient aiClient = new MockAIClient();

    @Test
    void shouldReturnDeterministicMockAdviceWithoutExternalCall() {
        AIClient.AIAdvice advice = aiClient.analyze(
                new SkinAnalysisDTO("干性皮肤", 26, "换季干燥"));

        assertThat(advice.analysis()).contains("26岁", "干性皮肤", "换季干燥");
        assertThat(advice.suggestions()).hasSize(3).allMatch(suggestion -> !suggestion.isBlank());
    }
}
