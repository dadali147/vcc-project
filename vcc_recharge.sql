-- vcc_recharge.sql
-- 充值记录表

CREATE TABLE `vcc_recharge` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `card_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '卡ID（储值卡必填）',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '充值金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
    `fee` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '手续费',
    `actual_amount` DECIMAL(18,2) NOT NULL COMMENT '实际到账金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-处理中，1-成功，2-失败',
    `recharge_type` TINYINT NOT NULL DEFAULT 1 COMMENT '充值类型：1-USDT，2-其他',
    `upstream_order_no` VARCHAR(64) DEFAULT NULL COMMENT '上游订单号',
    `tx_hash` VARCHAR(100) DEFAULT NULL COMMENT '区块链交易哈希（USDT充值）',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `completed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_card_id` (`card_id`),
    KEY `idx_status` (`status`),
    KEY `idx_upstream_order_no` (`upstream_order_no`),
    KEY `idx_tx_hash` (`tx_hash`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';
