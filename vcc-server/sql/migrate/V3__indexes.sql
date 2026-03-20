-- VCC 增量迁移脚本 V3 - 索引与唯一约束
-- 执行前提：已执行 V3__create_new_tables.sql 与 V3__alter_existing_tables.sql
-- 创建时间：2026-03-19

-- 说明：MySQL 8.0 对索引创建缺少统一的 IF NOT EXISTS 语法，以下通过
-- information_schema.statistics + PREPARE/EXECUTE 保证幂等执行。

-- =============================================================================
-- 新表索引与唯一约束
-- =============================================================================

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_channel' AND index_name = 'uk_channel_code'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_channel` ADD UNIQUE KEY `uk_channel_code` (`channel_code`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_channel' AND index_name = 'idx_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_channel` ADD KEY `idx_status` (`status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_bin' AND index_name = 'uk_bin'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_bin` ADD UNIQUE KEY `uk_bin` (`bin`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_bin' AND index_name = 'idx_channel_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_bin` ADD KEY `idx_channel_status` (`channel_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_bin' AND index_name = 'idx_card_type'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_bin` ADD KEY `idx_card_type` (`card_type`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_merchant_bin' AND index_name = 'uk_merchant_bin'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_merchant_bin` ADD UNIQUE KEY `uk_merchant_bin` (`merchant_id`, `bin_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_merchant_bin' AND index_name = 'idx_bin_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_merchant_bin` ADD KEY `idx_bin_id` (`bin_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_merchant_bin' AND index_name = 'idx_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_merchant_bin` ADD KEY `idx_status` (`status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_issue_request' AND index_name = 'idx_merchant_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_issue_request` ADD KEY `idx_merchant_status` (`merchant_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_issue_request' AND index_name = 'idx_batch_no'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_issue_request` ADD KEY `idx_batch_no` (`batch_no`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_issue_request' AND index_name = 'idx_submit_time'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_issue_request` ADD KEY `idx_submit_time` (`submit_time`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_issue_item' AND index_name = 'idx_request_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_issue_item` ADD KEY `idx_request_status` (`request_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_issue_item' AND index_name = 'idx_holder_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_issue_item` ADD KEY `idx_holder_id` (`holder_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_issue_item' AND index_name = 'idx_yeevcc_order_no'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_issue_item` ADD KEY `idx_yeevcc_order_no` (`yeevcc_order_no`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_adjustment_request' AND index_name = 'uk_request_no'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_adjustment_request` ADD UNIQUE KEY `uk_request_no` (`request_no`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_adjustment_request' AND index_name = 'idx_merchant_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_adjustment_request` ADD KEY `idx_merchant_status` (`merchant_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_adjustment_request' AND index_name = 'idx_target'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_adjustment_request` ADD KEY `idx_target` (`target_type`, `target_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_adjustment_request' AND index_name = 'idx_reviewer_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_adjustment_request` ADD KEY `idx_reviewer_id` (`reviewer_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_sub_account_permission' AND index_name = 'uk_sub_account_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_sub_account_permission` ADD UNIQUE KEY `uk_sub_account_id` (`sub_account_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_sub_account_permission' AND index_name = 'idx_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_sub_account_permission` ADD KEY `idx_status` (`status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_export_task' AND index_name = 'idx_merchant_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_export_task` ADD KEY `idx_merchant_status` (`merchant_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_export_task' AND index_name = 'idx_export_type'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_export_task` ADD KEY `idx_export_type` (`export_type`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_export_task' AND index_name = 'idx_submit_time'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_export_task` ADD KEY `idx_submit_time` (`submit_time`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_message' AND index_name = 'idx_receiver_read'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_message` ADD KEY `idx_receiver_read` (`receiver_id`, `is_read`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_message' AND index_name = 'idx_business'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_message` ADD KEY `idx_business` (`business_type`, `business_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_limit_history' AND index_name = 'idx_card_created_at'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_limit_history` ADD KEY `idx_card_created_at` (`card_id`, `created_at`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_limit_history' AND index_name = 'idx_merchant_created_at'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_limit_history` ADD KEY `idx_merchant_created_at` (`merchant_id`, `created_at`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_debt' AND index_name = 'idx_merchant_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_debt` ADD KEY `idx_merchant_status` (`merchant_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_debt' AND index_name = 'idx_card_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_debt` ADD KEY `idx_card_id` (`card_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_debt' AND index_name = 'idx_occurred_at'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_debt` ADD KEY `idx_occurred_at` (`occurred_at`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_risk_rule' AND index_name = 'uk_rule_code'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_risk_rule` ADD UNIQUE KEY `uk_rule_code` (`rule_code`, `scope`, `merchant_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_risk_rule' AND index_name = 'idx_rule_type_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_risk_rule` ADD KEY `idx_rule_type_status` (`rule_type`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_risk_rule' AND index_name = 'idx_scope_merchant'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_risk_rule` ADD KEY `idx_scope_merchant` (`scope`, `merchant_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_risk_event' AND index_name = 'idx_merchant_rule_status'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_risk_event` ADD KEY `idx_merchant_rule_status` (`merchant_id`, `rule_id`, `status`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_risk_event' AND index_name = 'idx_transaction_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_risk_event` ADD KEY `idx_transaction_id` (`transaction_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_risk_event' AND index_name = 'idx_created_at'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_risk_event` ADD KEY `idx_created_at` (`created_at`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- 旧表新增字段索引
-- =============================================================================

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_user_account' AND index_name = 'idx_account_type'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_user_account` ADD KEY `idx_account_type` (`account_type`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_user_account' AND index_name = 'idx_parent_account_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_user_account` ADD KEY `idx_parent_account_id` (`parent_account_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'kyc_records' AND index_name = 'idx_merchant_id'
        ),
        'SELECT 1',
        'ALTER TABLE `kyc_records` ADD KEY `idx_merchant_id` (`merchant_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'kyc_records' AND index_name = 'idx_kyc_status_merchant'
        ),
        'SELECT 1',
        'ALTER TABLE `kyc_records` ADD KEY `idx_kyc_status_merchant` (`status`, `merchant_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'kyc_records' AND index_name = 'idx_reviewer_id'
        ),
        'SELECT 1',
        'ALTER TABLE `kyc_records` ADD KEY `idx_reviewer_id` (`reviewer_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_holder' AND index_name = 'idx_holder_merchant_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_holder` ADD KEY `idx_holder_merchant_id` (`merchant_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card_holder' AND index_name = 'idx_holder_deleted_flag'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card_holder` ADD KEY `idx_holder_deleted_flag` (`deleted_flag`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card' AND index_name = 'idx_card_first6'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card` ADD KEY `idx_card_first6` (`first6`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_card' AND index_name = 'idx_card_last4'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_card` ADD KEY `idx_card_last4` (`last4`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_fee_config' AND index_name = 'idx_fee_scope_merchant'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_fee_config` ADD KEY `idx_fee_scope_merchant` (`scope`, `merchant_id`, `fee_type`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_fee_config' AND index_name = 'idx_fee_version'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_fee_config` ADD KEY `idx_fee_version` (`version_no`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_recharge' AND index_name = 'idx_recharge_merchant_scene'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_recharge` ADD KEY `idx_recharge_merchant_scene` (`merchant_id`, `recharge_scene`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_recharge' AND index_name = 'idx_recharge_chain_type'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_recharge` ADD KEY `idx_recharge_chain_type` (`chain_type`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_transaction' AND index_name = 'idx_txn_merchant_time'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_transaction` ADD KEY `idx_txn_merchant_time` (`merchant_id`, `txn_time`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_transaction' AND index_name = 'idx_txn_holder_id'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_transaction` ADD KEY `idx_txn_holder_id` (`holder_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_transaction' AND index_name = 'idx_txn_category'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_transaction` ADD KEY `idx_txn_category` (`txn_category`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_transaction' AND index_name = 'idx_txn_related'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_transaction` ADD KEY `idx_txn_related` (`related_txn_id`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'vcc_transaction' AND index_name = 'idx_txn_display_mode'
        ),
        'SELECT 1',
        'ALTER TABLE `vcc_transaction` ADD KEY `idx_txn_display_mode` (`display_mode`)'
    )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
