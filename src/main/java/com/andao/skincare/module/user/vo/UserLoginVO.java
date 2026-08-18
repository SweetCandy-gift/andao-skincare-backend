package com.andao.skincare.module.user.vo;

public record UserLoginVO(
        String token,
        UserVO userInfo
) {
}
