-- kyc_records.sql
-- KYC 认证记录表
-- 中国用户：腾讯云 API 认证
-- 海外用户：待定

CREATE TABLE `kyc_records` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `kyc_type` TINYINT NOT NULL DEFAULT 1 COMMENT '认证类型：1-个人，2-企业',
    `country_code` VARCHAR(10) DEFAULT 'CN' COMMENT '国家代码',
    `id_card_front` VARCHAR(255) DEFAULT NULL COMMENT '身份证正面照片URL',
    `id_card_back` VARCHAR(255) DEFAULT NULL COMMENT '身份证背面照片URL',
    `face_recognition_result` JSON DEFAULT NULL COMMENT '人脸识别结果',
    `verify_data` JSON DEFAULT NULL COMMENT '认证原始数据（腾讯云返回）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未认证，1-认证中，2-认证通过，3-认证失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `verified_at` TIMESTAMP NULL DEFAULT NULL COMMENT '认证通过时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_kyc_type` (`kyc_type`),
    KEY `idx_status` (`status`),
    KEY `idx_country_code` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='KYC认证记录表';
