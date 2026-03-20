CREATE TABLE IF NOT EXISTS `vcc_card_operation_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `card_id` BIGINT UNSIGNED NOT NULL COMMENT '卡片ID',
    `holder_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '持卡人ID',
    `operation_type` VARCHAR(30) NOT NULL COMMENT '操作类型：FREEZE/UNFREEZE/CANCEL/LIMIT_CHANGE/REMARK_UPDATE/ACTIVATE/STATUS_CHANGE',
    `before_value` VARCHAR(500) DEFAULT NULL COMMENT '操作前值',
    `after_value` VARCHAR(500) DEFAULT NULL COMMENT '操作后值',
    `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_card_id` (`card_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡片操作记录';
