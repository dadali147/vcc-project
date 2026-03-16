-- vcc_card_holder.sql
-- 持卡人信息表
-- 注意：一个用户可创建多个持卡人（user_id 无唯一索引）

CREATE TABLE `vcc_card_holder` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `holder_name` VARCHAR(100) NOT NULL COMMENT '持卡人姓名',
    `first_name` VARCHAR(50) NOT NULL COMMENT '名',
    `last_name` VARCHAR(50) NOT NULL COMMENT '姓',
    `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `id_card` VARCHAR(50) NOT NULL COMMENT '身份证号/护照号',
    `address` VARCHAR(255) NOT NULL COMMENT '地址',
    `post_code` VARCHAR(20) DEFAULT NULL COMMENT '邮编',
    `country` VARCHAR(50) DEFAULT 'CN' COMMENT '国家',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `upstream_holder_id` VARCHAR(64) DEFAULT NULL COMMENT '上游持卡人ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_mobile` (`mobile`),
    KEY `idx_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_upstream_holder_id` (`upstream_holder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='持卡人信息表';
