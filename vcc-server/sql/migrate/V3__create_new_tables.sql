-- VCC 增量迁移脚本 V3 - 新增业务表
-- 执行前提：已执行 sql/vcc_init.sql 与 sql/migrate/V2__add_missing_fields.sql
-- 创建时间：2026-03-19

-- =============================================================================
-- 1. vcc_channel - 渠道管理
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_channel` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `channel_code` VARCHAR(50) NOT NULL COMMENT '渠道编码',
    `channel_name` VARCHAR(100) NOT NULL COMMENT '渠道名称',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `api_base_url` VARCHAR(255) NOT NULL COMMENT 'API基础地址',
    `api_config` JSON DEFAULT NULL COMMENT 'API配置',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道管理';

-- =============================================================================
-- 2. vcc_card_bin - 卡BIN管理
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_card_bin` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `bin` VARCHAR(10) NOT NULL COMMENT 'BIN号',
    `card_org` VARCHAR(50) NOT NULL COMMENT '卡组织',
    `card_type` VARCHAR(20) NOT NULL COMMENT '卡类型：PREPAID/BUDGET',
    `channel_id` BIGINT UNSIGNED NOT NULL COMMENT '渠道ID',
    `card_product_id` VARCHAR(64) DEFAULT NULL COMMENT '卡产品ID',
    `default_limit_amount` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '默认额度',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡BIN管理';

-- =============================================================================
-- 3. vcc_merchant_bin - 商户BIN分配
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_merchant_bin` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `bin_id` BIGINT UNSIGNED NOT NULL COMMENT 'BIN ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' COMMENT '分配状态：ASSIGNED/DISABLED',
    `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
    `assigned_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '分配操作人ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户BIN分配';

-- =============================================================================
-- 4. vcc_card_issue_request - 开卡申请单
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_card_issue_request` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `batch_no` VARCHAR(64) NOT NULL COMMENT '申请批次号',
    `card_type` VARCHAR(20) NOT NULL COMMENT '卡类型：PREPAID/BUDGET',
    `bin_id` BIGINT UNSIGNED DEFAULT NULL COMMENT 'BIN ID',
    `total_count` INT NOT NULL DEFAULT 1 COMMENT '总申请数量',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功数量',
    `fail_count` INT NOT NULL DEFAULT 0 COMMENT '失败数量',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/PARTIAL/FAILED/CANCELLED',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开卡申请单';

-- =============================================================================
-- 5. vcc_card_issue_item - 开卡申请明细
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_card_issue_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `request_id` BIGINT UNSIGNED NOT NULL COMMENT '申请单ID',
    `holder_id` BIGINT UNSIGNED NOT NULL COMMENT '持卡人ID',
    `card_product_id` VARCHAR(64) DEFAULT NULL COMMENT '卡产品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '申请数量',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED',
    `yeevcc_order_no` VARCHAR(64) DEFAULT NULL COMMENT 'YeeVCC订单号',
    `card_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '生成卡片ID',
    `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开卡申请明细';

-- =============================================================================
-- 6. vcc_adjustment_request - 调账申请
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_adjustment_request` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `request_no` VARCHAR(64) NOT NULL COMMENT '申请单号',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `adjustment_type` VARCHAR(30) NOT NULL COMMENT '调账类型：MANUAL_ADJUST/REFUND_ADJUST',
    `target_type` VARCHAR(20) NOT NULL COMMENT '调账对象类型：ACCOUNT/CARD',
    `target_id` BIGINT UNSIGNED NOT NULL COMMENT '调账对象ID',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '调账金额',
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
    `reason` VARCHAR(500) NOT NULL COMMENT '调账原因',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED/EXECUTED/CANCELLED',
    `requester_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '申请人ID',
    `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '复核人ID',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `review_time` DATETIME DEFAULT NULL COMMENT '复核时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '复核备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调账申请';

-- =============================================================================
-- 7. vcc_sub_account_permission - 子账号权限
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_sub_account_permission` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sub_account_id` BIGINT UNSIGNED NOT NULL COMMENT '子账号ID',
    `page_permissions` JSON DEFAULT NULL COMMENT '页面权限列表',
    `action_permissions` JSON DEFAULT NULL COMMENT '操作权限列表',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `assigned_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '授权人ID',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子账号权限';

-- =============================================================================
-- 8. vcc_export_task - 导出任务
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_export_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `export_type` VARCHAR(30) NOT NULL COMMENT '导出类型：TRANSACTION/RECHARGE/STATEMENT',
    `language` VARCHAR(10) NOT NULL DEFAULT 'zh_CN' COMMENT '导出语言',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    `file_format` VARCHAR(20) NOT NULL DEFAULT 'CSV' COMMENT '文件格式',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED/EXPIRED',
    `filter_params` JSON DEFAULT NULL COMMENT '导出筛选条件',
    `requester_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发起人ID',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导出任务';

-- =============================================================================
-- 9. vcc_message - 站内消息
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_message` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `receiver_type` VARCHAR(20) NOT NULL DEFAULT 'MERCHANT' COMMENT '接收对象类型：MERCHANT/USER/SUB_ACCOUNT',
    `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收对象ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `message_type` VARCHAR(30) NOT NULL COMMENT '消息类型：SYSTEM/BUSINESS/RISK/KYC',
    `is_read` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `business_type` VARCHAR(30) DEFAULT NULL COMMENT '关联业务类型',
    `business_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息';

-- =============================================================================
-- 10. vcc_limit_history - 限额调整历史
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_limit_history` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `card_id` BIGINT UNSIGNED NOT NULL COMMENT '卡片ID',
    `limit_type` VARCHAR(20) NOT NULL COMMENT '限额类型：SINGLE/DAILY/MONTHLY',
    `before_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '调整前额度',
    `after_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '调整后额度',
    `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作人ID',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '操作原因',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='限额调整历史';

-- =============================================================================
-- 11. vcc_card_debt - 欠费记录
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_card_debt` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `card_id` BIGINT UNSIGNED NOT NULL COMMENT '卡片ID',
    `debt_amount` DECIMAL(18,2) NOT NULL COMMENT '欠费金额',
    `occurred_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '产生时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'OUTSTANDING' COMMENT '状态：OUTSTANDING/SETTLED',
    `settle_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '结清金额',
    `settle_time` DATETIME DEFAULT NULL COMMENT '结清时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='欠费记录';

-- =============================================================================
-- 12. vcc_risk_rule - 风控规则
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_risk_rule` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(30) NOT NULL COMMENT '规则类型：SINGLE_LIMIT/DAILY_LIMIT/MONTHLY_LIMIT',
    `scope` VARCHAR(20) NOT NULL DEFAULT 'GLOBAL' COMMENT '作用域：GLOBAL/MERCHANT',
    `merchant_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '商户ID，GLOBAL作用域时固定为0',
    `threshold_amount` DECIMAL(18,2) NOT NULL COMMENT '阈值金额',
    `action` VARCHAR(20) NOT NULL DEFAULT 'REJECT' COMMENT '动作：REJECT/MANUAL_REVIEW',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控规则';

-- =============================================================================
-- 13. vcc_risk_event - 风控事件
-- =============================================================================
CREATE TABLE IF NOT EXISTS `vcc_risk_event` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
    `rule_id` BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    `transaction_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '交易ID',
    `trigger_detail` JSON DEFAULT NULL COMMENT '触发详情',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/RESOLVED/IGNORED',
    `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-正常，1-删除',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控事件';
