-- vcc_user_account.sql
-- 用户账户表
-- 支持多币种（目前仅展示 USD）

CREATE TABLE `vcc_user_account` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `balance` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '可用余额',
    `frozen_amount` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
    `total_recharge` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计充值',
    `total_withdraw` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '累计提现',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_currency` (`user_id`, `currency`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账户表';
