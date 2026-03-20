-- VCC 增量迁移脚本 V3 - 旧表改造
-- 执行前提：已执行 sql/vcc_init.sql 与 sql/migrate/V2__add_missing_fields.sql
-- 创建时间：2026-03-19

-- 说明：本次按任务范围仅补充新字段，不回改 V1/V2 已存在的历史状态列类型。

-- =============================================================================
-- 1. vcc_user_account 改造
-- =============================================================================
ALTER TABLE `vcc_user_account`
    ADD COLUMN IF NOT EXISTS `account_type` VARCHAR(20) NOT NULL DEFAULT 'MAIN' COMMENT '账户类型：MAIN/SUB' AFTER `user_id`;

ALTER TABLE `vcc_user_account`
    ADD COLUMN IF NOT EXISTS `parent_account_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父账户ID' AFTER `account_type`;

ALTER TABLE `vcc_user_account`
    ADD COLUMN IF NOT EXISTS `deposit_balance` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '保证金余额' AFTER `frozen_amount`;

ALTER TABLE `vcc_user_account`
    ADD COLUMN IF NOT EXISTS `deposit_preset` JSON DEFAULT NULL COMMENT '保证金预设配置' AFTER `deposit_balance`;

-- =============================================================================
-- 2. kyc_records 改造
-- =============================================================================
ALTER TABLE `kyc_records`
    ADD COLUMN IF NOT EXISTS `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID' AFTER `user_id`;

ALTER TABLE `kyc_records`
    ADD COLUMN IF NOT EXISTS `questionnaire` JSON DEFAULT NULL COMMENT '问卷信息' AFTER `verify_data`;

ALTER TABLE `kyc_records`
    ADD COLUMN IF NOT EXISTS `attachments` JSON DEFAULT NULL COMMENT '附件列表' AFTER `questionnaire`;

ALTER TABLE `kyc_records`
    ADD COLUMN IF NOT EXISTS `submit_count_today` INT NOT NULL DEFAULT 0 COMMENT '当日提交次数' AFTER `attachments`;

ALTER TABLE `kyc_records`
    ADD COLUMN IF NOT EXISTS `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID' AFTER `submit_count_today`;

ALTER TABLE `kyc_records`
    ADD COLUMN IF NOT EXISTS `review_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `reviewer_id`;

-- =============================================================================
-- 3. vcc_card_holder 改造
-- =============================================================================
ALTER TABLE `vcc_card_holder`
    ADD COLUMN IF NOT EXISTS `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID' AFTER `user_id`;

ALTER TABLE `vcc_card_holder`
    ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER `upstream_holder_id`;

ALTER TABLE `vcc_card_holder`
    ADD COLUMN IF NOT EXISTS `deleted_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0-正常，1-删除' AFTER `remark`;

-- =============================================================================
-- 4. vcc_card 改造
-- =============================================================================
ALTER TABLE `vcc_card`
    ADD COLUMN IF NOT EXISTS `first6` VARCHAR(6) DEFAULT NULL COMMENT '卡号前6位' AFTER `card_no_mask`;

ALTER TABLE `vcc_card`
    ADD COLUMN IF NOT EXISTS `last4` VARCHAR(4) DEFAULT NULL COMMENT '卡号后4位' AFTER `first6`;

ALTER TABLE `vcc_card`
    ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER `expiry_date`;

-- =============================================================================
-- 5. vcc_fee_config 改造
-- =============================================================================
ALTER TABLE `vcc_fee_config`
    ADD COLUMN IF NOT EXISTS `scope` VARCHAR(20) NOT NULL DEFAULT 'PLATFORM' COMMENT '作用域：PLATFORM/MERCHANT' AFTER `user_id`;

ALTER TABLE `vcc_fee_config`
    ADD COLUMN IF NOT EXISTS `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID' AFTER `scope`;

ALTER TABLE `vcc_fee_config`
    ADD COLUMN IF NOT EXISTS `condition_type` VARCHAR(30) DEFAULT NULL COMMENT '条件类型' AFTER `fee_type`;

ALTER TABLE `vcc_fee_config`
    ADD COLUMN IF NOT EXISTS `threshold_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '阈值金额' AFTER `condition_type`;

ALTER TABLE `vcc_fee_config`
    ADD COLUMN IF NOT EXISTS `charge_source` VARCHAR(20) DEFAULT NULL COMMENT '扣费来源：ACCOUNT_BALANCE/CARD_BALANCE/DEPOSIT_BALANCE' AFTER `max_fee`;

ALTER TABLE `vcc_fee_config`
    ADD COLUMN IF NOT EXISTS `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号' AFTER `charge_source`;

-- =============================================================================
-- 6. vcc_recharge 改造
-- =============================================================================
ALTER TABLE `vcc_recharge`
    ADD COLUMN IF NOT EXISTS `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID' AFTER `user_id`;

ALTER TABLE `vcc_recharge`
    ADD COLUMN IF NOT EXISTS `recharge_scene` VARCHAR(30) NOT NULL DEFAULT 'TOP_UP' COMMENT '充值场景：TOP_UP/CARD_LOAD' AFTER `merchant_id`;

ALTER TABLE `vcc_recharge`
    ADD COLUMN IF NOT EXISTS `chain_type` VARCHAR(20) DEFAULT NULL COMMENT '链类型：TRC20/ERC20/BEP20' AFTER `recharge_type`;

-- =============================================================================
-- 7. vcc_transaction 改造
-- =============================================================================
ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID' AFTER `user_id`;

ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `holder_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '持卡人ID' AFTER `merchant_id`;

ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `txn_category` VARCHAR(30) DEFAULT NULL COMMENT '交易分类：PURCHASE/REFUND/REVERSE/FEE/ADJUSTMENT' AFTER `txn_type`;

ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `related_txn_id` VARCHAR(64) DEFAULT NULL COMMENT '关联交易ID' AFTER `txn_category`;

ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `fail_reason_code` VARCHAR(30) DEFAULT NULL COMMENT '失败原因编码' AFTER `fail_reason`;

ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `fail_reason_text` VARCHAR(200) DEFAULT NULL COMMENT '失败原因描述' AFTER `fail_reason_code`;

ALTER TABLE `vcc_transaction`
    ADD COLUMN IF NOT EXISTS `display_mode` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '展示模式：NORMAL/HIDDEN' AFTER `fail_reason_text`;
