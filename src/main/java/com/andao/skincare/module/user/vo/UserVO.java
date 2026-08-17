package com.andao.skincare.module.user.vo;

import java.time.LocalDateTime;

public record UserVO(
        Long id,
        String username,
        String nickname,
        LocalDateTime createdAt
) {
}
