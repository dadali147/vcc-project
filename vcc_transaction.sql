-- vcc_transaction.sql
-- 交易记录表

CREATE TABLE `vcc_transaction` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `txn_id` VARCHAR(64) NOT NULL COMMENT '交易ID（上游）',
    `card_id` BIGINT UNSIGNED NOT NULL COMMENT '卡ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `txn_type` VARCHAR(20) NOT NULL COMMENT '交易类型：AUTH-授权，CAPTURE-扣款，REFUND-退款，REVERSE-撤销',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '交易金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '交易币种',
    `merchant_name` VARCHAR(255) DEFAULT NULL COMMENT '商户名称',
    `merchant_mcc` VARCHAR(10) DEFAULT NULL COMMENT '商户MCC码',
    `merchant_country` VARCHAR(10) DEFAULT NULL COMMENT '商户国家',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-处理中，1-成功，2-失败',
    `auth_code` VARCHAR(50) DEFAULT NULL COMMENT '授权码',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `txn_time` TIMESTAMP NULL DEFAULT NULL COMMENT '交易时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_txn_id` (`txn_id`),
    KEY `idx_card_id` (`card_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_txn_type` (`txn_type`),
    KEY `idx_status` (`status`),
    KEY `idx_txn_time` (`txn_time`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';
