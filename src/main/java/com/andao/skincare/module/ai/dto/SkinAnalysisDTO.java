package com.andao.skincare.module.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkinAnalysisDTO(
        @NotBlank(message = "肤质不能为空")
        @Size(max = 30, message = "肤质描述不能超过30个字符")
        String skinType,

        @NotNull(message = "年龄不能为空")
        @Min(value = 1, message = "年龄必须大于0")
        @Max(value = 120, message = "年龄不能超过120")
        Integer age,

        @NotBlank(message = "皮肤问题不能为空")
        @Size(max = 500, message = "皮肤问题不能超过500个字符")
        String problem
) {
}
