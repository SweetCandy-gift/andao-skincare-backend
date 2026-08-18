package com.andao.skincare.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    PARAM_ERROR(40000, HttpStatus.BAD_REQUEST, "请求参数错误"),
    STOCK_QUANTITY_INVALID(40001, HttpStatus.BAD_REQUEST, "扣减库存数量必须为正数"),
    STOCK_INSUFFICIENT(40002, HttpStatus.BAD_REQUEST, "商品库存不足"),
    CART_QUANTITY_LIMIT(40003, HttpStatus.BAD_REQUEST, "单个商品数量不能超过999"),
    CART_EMPTY(40004, HttpStatus.BAD_REQUEST, "购物车为空"),
    CART_PRODUCT_UNAVAILABLE(40005, HttpStatus.BAD_REQUEST, "购物车包含不可用或库存不足的商品"),
    PRODUCT_UNAVAILABLE(40006, HttpStatus.BAD_REQUEST, "商品不存在或已下架"),

    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "未认证或Token无效"),
    INVALID_CREDENTIALS(40101, HttpStatus.UNAUTHORIZED, "用户名或密码错误"),
    ACCESS_DENIED(40300, HttpStatus.FORBIDDEN, "无权限访问"),
    USER_DISABLED(40301, HttpStatus.FORBIDDEN, "用户已被禁用"),
    ORDER_ACCESS_DENIED(40302, HttpStatus.FORBIDDEN, "无权限操作该订单"),

    PRODUCT_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "商品不存在或已下架"),
    CART_ITEM_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "购物车中不存在该商品"),
    ORDER_NOT_FOUND(40403, HttpStatus.NOT_FOUND, "订单不存在"),

    USERNAME_ALREADY_EXISTS(40901, HttpStatus.CONFLICT, "用户名已存在"),
    STOCK_CONFLICT(40902, HttpStatus.CONFLICT, "商品库存已变化，请重新确认后下单"),
    ORDER_CANNOT_CANCEL(40903, HttpStatus.CONFLICT, "只有已创建订单可以取消"),
    ORDER_STATUS_CHANGED(40904, HttpStatus.CONFLICT, "订单状态已发生变化，请刷新后重试"),

    SYSTEM_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "系统内部错误");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
