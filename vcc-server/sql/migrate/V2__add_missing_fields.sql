-- VCC 增量迁移脚本 V2
-- 执行前提：已执行 vcc_init.sql (V1)

-- 1. vcc_webhook_log 增加 idempotency_key 字段（如不存在）
ALTER TABLE `vcc_webhook_log`
  ADD COLUMN IF NOT EXISTS `idempotency_key` VARCHAR(200) DEFAULT NULL COMMENT '幂等键';

-- 检查唯一索引是否存在，不存在则创建
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'vcc_webhook_log' AND index_name = 'uk_idempotency_key');
SET @sql = IF(@idx_exists = 0,
  'ALTER TABLE `vcc_webhook_log` ADD UNIQUE KEY `uk_idempotency_key` (`idempotency_key`)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. vcc_recharge 增加 fail_reason 字段（如不存在）
ALTER TABLE `vcc_recharge`
  ADD COLUMN IF NOT EXISTS `fail_reason` VARCHAR(500) DEFAULT NULL COMMENT '失败原因';

-- 3. system_configs 确保有默认费率配置
INSERT IGNORE INTO `system_configs` (`config_key`, `config_value`, `remark`) VALUES
  ('fee.recharge.budget.rate', '0.01', '预算卡充值费率'),
  ('fee.recharge.prepaid.rate', '0.01', '储值卡充值费率'),
  ('risk.recharge.enabled', 'true', '充值风控开关'),
  ('risk.single.recharge.limit', '10000', '单次充值限额(USD)'),
  ('risk.daily.recharge.limit', '50000', '日充值限额(USD)'),
  ('risk.card.open.enabled', 'true', '开卡风控开关');
