package com.andao.skincare.module.order.entity;

/**
 * 订单状态使用枚举集中维护，避免业务代码散落无含义的数字，
 * 同时确保状态码、名称和中文含义之间保持稳定映射。
 */
public enum OrderStatus {

    ORDER_CREATED(0, "已创建"),
    ORDER_PAID(1, "已支付"),
    ORDER_SHIPPED(2, "已发货"),
    ORDER_COMPLETED(3, "已完成"),
    ORDER_CANCELLED(4, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
