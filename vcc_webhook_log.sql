-- vcc_webhook_log.sql
-- Webhook 回调日志表

CREATE TABLE `vcc_webhook_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `webhook_type` VARCHAR(50) NOT NULL COMMENT '回调类型：AUTH_TRANSACTION/CARD_OPERATION/OTP/THIRD_PARTY_AUTH',
    `upstream_txn_id` VARCHAR(64) DEFAULT NULL COMMENT '上游交易ID',
    `payload` JSON NOT NULL COMMENT '回调原始数据',
    `signature` VARCHAR(500) DEFAULT NULL COMMENT '签名',
    `processed` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已处理：0-否，1-是',
    `process_result` JSON DEFAULT NULL COMMENT '处理结果',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `processed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    KEY `idx_webhook_type` (`webhook_type`),
    KEY `idx_upstream_txn_id` (`upstream_txn_id`),
    KEY `idx_processed` (`processed`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook回调日志表';
