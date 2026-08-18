package com.andao.skincare.common.result;

import com.andao.skincare.common.exception.ErrorCode;

/**
 * 所有 Controller 的统一响应结构。固定的 code、message、data 便于前端统一判断结果，
 * 避免不同业务模块各自定义成功和失败格式。
 */
public record Result<T>(int code, String message, T data) {

    private static final int SUCCESS_CODE = 0;
    private static final String SUCCESS_MESSAGE = "success";

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static Result<Void> success() {
        return success(null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
