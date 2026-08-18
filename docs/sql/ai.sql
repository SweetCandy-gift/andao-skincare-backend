CREATE TABLE IF NOT EXISTS `ai_analysis_record` (
    `id` BIGINT NOT NULL COMMENT 'AI分析记录ID（MyBatis-Plus雪花ID）',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `skin_type` VARCHAR(30) NOT NULL COMMENT '用户填写的肤质',
    `age` INT NOT NULL COMMENT '分析时填写的年龄',
    `problem` VARCHAR(500) NOT NULL COMMENT '用户填写的皮肤问题',
    `analysis_result` TEXT NOT NULL COMMENT 'AI返回的分析结果',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分析时间',
    PRIMARY KEY (`id`),
    KEY `idx_ai_analysis_record_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI护肤分析历史记录表';
