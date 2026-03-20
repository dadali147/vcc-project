-- ==========================================
-- V4: 风控规则表新增字段 (P1 修复)
-- 新增: threshold_count, priority, card_type
-- ==========================================

ALTER TABLE vcc_risk_rule
    ADD COLUMN threshold_count INT DEFAULT NULL COMMENT '阈值次数（用于 daily_count 规则）' AFTER threshold_amount,
    ADD COLUMN priority INT DEFAULT 999 COMMENT '优先级（数值越小越优先）' AFTER threshold_count,
    ADD COLUMN card_type VARCHAR(20) DEFAULT NULL COMMENT '适用卡类型：PREPAID / BUDGET，null 表示全部' AFTER priority;

-- 为 rule_code 添加唯一索引（配合业务校验）
ALTER TABLE vcc_risk_rule
    ADD UNIQUE INDEX uk_rule_code (rule_code) COMMENT 'ruleCode 唯一约束';

-- 为优先级排序添加索引
ALTER TABLE vcc_risk_rule
    ADD INDEX idx_priority_status (priority, status) COMMENT '优先级+状态联合索引';
