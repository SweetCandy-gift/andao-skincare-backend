CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL COMMENT '订单ID（MyBatis-Plus雪花ID）',
    `order_no` VARCHAR(40) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(12, 2) NOT NULL COMMENT '订单总金额',
    `total_quantity` INT NOT NULL COMMENT '商品总数量',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-已创建，1-已支付，2-已发货，3-已完成，4-已取消',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '订单备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_order_no` (`order_no`),
    KEY `idx_order_user_status_created` (`user_id`, `status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT NOT NULL COMMENT '订单明细ID（MyBatis-Plus雪花ID）',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '下单时商品名称快照',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '下单时商品封面快照',
    `product_price` DECIMAL(10, 2) NOT NULL COMMENT '下单时商品单价',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `subtotal` DECIMAL(12, 2) NOT NULL COMMENT '明细小计',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_item_order_id` (`order_id`),
    KEY `idx_order_item_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';
