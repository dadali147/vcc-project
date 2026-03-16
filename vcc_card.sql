-- vcc_card.sql
-- 虚拟卡信息表
-- 三要素（卡号、CVV、有效期）不落库，仅保存脱敏信息

CREATE TABLE `vcc_card` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `holder_id` BIGINT UNSIGNED NOT NULL COMMENT '持卡人ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（冗余）',
    `card_no_mask` VARCHAR(20) NOT NULL COMMENT '脱敏卡号（如：493875******1234）',
    `card_bin_id` VARCHAR(64) NOT NULL COMMENT '卡Bin ID',
    `card_type` TINYINT NOT NULL DEFAULT 1 COMMENT '卡类型：1-储值卡，2-预算卡',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-注销，1-正常，2-冻结',
    `balance` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '卡余额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
    `budget_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '预算额度（预算卡专用）',
    `upstream_card_id` VARCHAR(64) DEFAULT NULL COMMENT '上游卡ID',
    `expiry_date` VARCHAR(10) DEFAULT NULL COMMENT '有效期（MM/YY，脱敏）',
    `is_auto_activate` TINYINT(1) DEFAULT 0 COMMENT '是否自动激活：0-否，1-是',
    `activated_at` TIMESTAMP NULL DEFAULT NULL COMMENT '激活时间',
    `cancelled_at` TIMESTAMP NULL DEFAULT NULL COMMENT '注销时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_holder_id` (`holder_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_card_bin_id` (`card_bin_id`),
    KEY `idx_status` (`status`),
    KEY `idx_card_type` (`card_type`),
    KEY `idx_upstream_card_id` (`upstream_card_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='虚拟卡信息表';
