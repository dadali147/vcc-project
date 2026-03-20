-- VCC business schema initialization
-- Generated from root SQL files on 2026-03-16

-- -----------------------------------------------------------------------------
-- Source: system_configs.sql
-- -----------------------------------------------------------------------------

-- system_configs.sql
-- 系统配置表
-- 用于存储 AES 密钥、费率配置等系统级配置

CREATE TABLE `system_configs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '值类型：STRING/NUMBER/JSON/BOOLEAN',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
    `is_encrypted` TINYINT(1) DEFAULT 0 COMMENT '是否加密存储：0-否，1-是',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_type` (`config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 初始化 AES 密钥配置
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `is_encrypted`) VALUES
('yeevcc.aes.key.v1', '', 'STRING', 'YeeVCC AES 密钥 v1', 1),
('yeevcc.aes.version', 'v1', 'STRING', '当前使用的 AES 密钥版本', 0),
('yeevcc.api.baseurl.test', 'https://apiqa.kun.global/yop-center', 'STRING', 'YeeVCC 测试环境地址', 0),
('yeevcc.api.baseurl.prod', 'https://api.kun.global/yop-center', 'STRING', 'YeeVCC 生产环境地址', 0),
('vcc.webhook.secret', '', 'STRING', 'YeeVCC Webhook 回调验签 Secret（HMAC-SHA256），上线前必须填入真实值', 1);


-- -----------------------------------------------------------------------------
-- Source: vcc_user_account.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: kyc_records.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: vcc_card.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: vcc_card_holder.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: vcc_fee_config.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: vcc_recharge.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: vcc_transaction.sql
-- -----------------------------------------------------------------------------

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


-- -----------------------------------------------------------------------------
-- Source: vcc_webhook_log.sql
-- -----------------------------------------------------------------------------

-- vcc_webhook_log.sql
-- Webhook 回调日志表

CREATE TABLE `vcc_webhook_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `webhook_type` VARCHAR(50) NOT NULL COMMENT '回调类型：AUTH_TRANSACTION/CARD_OPERATION/OTP/THIRD_PARTY_AUTH',
    `upstream_txn_id` VARCHAR(64) DEFAULT NULL COMMENT '上游交易ID',
    `idempotency_key` VARCHAR(200) DEFAULT NULL COMMENT '幂等键（webhookType + 业务唯一ID）',
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
    UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
    KEY `idx_upstream_txn_id` (`upstream_txn_id`),
    KEY `idx_processed` (`processed`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Webhook回调日志表';


