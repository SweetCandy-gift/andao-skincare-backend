package com.andao.skincare.common.exception;

/**
 * 表示调用方可以理解和处理的业务失败，与程序缺陷、数据库故障等系统异常分离。
 * 全局异常处理器据此返回稳定业务码，同时避免把内部堆栈和实现细节暴露给客户端。
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
