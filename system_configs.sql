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
('yeevcc.api.baseurl.prod', 'https://api.kun.global/yop-center', 'STRING', 'YeeVCC 生产环境地址', 0);
