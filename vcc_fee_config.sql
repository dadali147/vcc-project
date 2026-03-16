-- vcc_fee_config.sql
-- 费率配置表

CREATE TABLE `vcc_fee_config` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（0表示默认配置）',
    `card_bin_id` VARCHAR(64) DEFAULT NULL COMMENT '卡Bin ID（null表示所有Bin）',
    `fee_type` VARCHAR(20) NOT NULL COMMENT '费用类型：OPEN-开卡费，MONTHLY-月费，TXN-交易手续费',
    `rate` DECIMAL(5,4) NOT NULL DEFAULT '0.0000' COMMENT '费率（百分比，如0.015表示1.5%）',
    `fixed_amount` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '固定金额',
    `min_fee` DECIMAL(18,2) DEFAULT NULL COMMENT '最低手续费',
    `max_fee` DECIMAL(18,2) DEFAULT NULL COMMENT '最高手续费',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `effective_date` DATE DEFAULT NULL COMMENT '生效日期',
    `expiry_date` DATE DEFAULT NULL COMMENT '失效日期',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_card_bin_id` (`card_bin_id`),
    KEY `idx_fee_type` (`fee_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='费率配置表';
