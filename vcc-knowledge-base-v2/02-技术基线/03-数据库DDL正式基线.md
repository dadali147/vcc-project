# VCC 数据库 DDL 设计 v1

版本: 1.0
创建时间: 2026-03-19
基础来源: 《VCC 统一状态字典与数据模型草案 v1》
用途: 作为后端数据库重构的第一版 DDL 设计基线，供 Opus 4.6 子 agent 开发前统一表结构与索引策略。

---

## 一、设计原则

- 保留 RuoYi 框架层，不重造基础系统表
- 业务表统一使用 bigint 主键 + code 枚举字段
- 状态字段统一存 code，不存中文文案
- 高变结构优先 JSON 字段承载（如 questionnaire、conditions、attachments）
- 核心流水类表必须保留 created_at / updated_at
- 涉及对账、资金、状态回溯的记录不做物理删除

---

## 二、DDL 总览

### 2.1 需新增的核心业务表

| 表名 | 用途 | 优先级 |
|------|------|--------|
| vcc_channel | 渠道定义 | P0 |
| vcc_card_bin | BIN 定义 | P0 |
| vcc_merchant_bin | 商户-BIN 关系 | P0 |
| vcc_card_issue_request | 开卡申请单 | P0 |
| vcc_card_issue_item | 开卡单卡结果 | P0 |
| vcc_sub_account_permission | 子账号页面权限 | P0 |
| vcc_export_task | 导出任务 | P1 |
| vcc_message | 消息中心记录 | P1 |
| vcc_limit_history | 限额调整记录 | P1 |
| vcc_card_debt | 卡欠费记录 | P1 |
| vcc_risk_rule | 风控规则 | P1 |
| vcc_risk_event | 风控事件 | P1 |
| vcc_adjustment_request | 手工调账审批单 | P0 |

### 2.2 需改造的现有业务表

| 现有表 | 改造方向 |
|--------|----------|
| vcc_user_account | 拆出主/子账号与钱包能力，增加保证金语义 |
| kyc_records | 补充问卷、附件、每日次数锁定等字段 |
| vcc_card_holder | 增加 remark、deleted_flag、merchant_id 语义明确化 |
| vcc_card | 增加 first6/last4、remark、预算卡语义、负数支持 |
| vcc_fee_config | 重构为支持 scope / conditionType / chargeSource / version |
| vcc_recharge | 区分主账户充值与充值到卡 |
| vcc_transaction | 重构为平台展示交易模型 + 关联关系 |

---

## 三、新增表设计

### 3.1 vcc_channel

```sql
CREATE TABLE `vcc_channel` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_code` VARCHAR(50) NOT NULL COMMENT '渠道编码',
  `channel_name` VARCHAR(100) NOT NULL COMMENT '渠道名称',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `api_config` JSON DEFAULT NULL COMMENT '接口配置JSON',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='渠道定义表';
```

### 3.2 vcc_card_bin

```sql
CREATE TABLE `vcc_card_bin` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_id` BIGINT UNSIGNED NOT NULL COMMENT '渠道ID',
  `bin_code` VARCHAR(32) NOT NULL COMMENT 'BIN编码/前缀',
  `bin_name` VARCHAR(100) NOT NULL COMMENT 'BIN名称',
  `card_type` VARCHAR(20) NOT NULL COMMENT 'PREPAID/BUDGET',
  `currency` VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT '币种',
  `total_quota` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '总额度',
  `used_quota` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '已用额度',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_bin_code` (`channel_id`, `bin_code`),
  KEY `idx_card_type` (`card_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡BIN定义表';
```

### 3.3 vcc_merchant_bin

```sql
CREATE TABLE `vcc_merchant_bin` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户主账号ID',
  `bin_id` BIGINT UNSIGNED NOT NULL COMMENT 'BIN ID',
  `status` VARCHAR(20) NOT NULL DEFAULT 'DISABLED' COMMENT '状态：ENABLED/DISABLED',
  `fee_configured` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已配置商户费率',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_bin` (`merchant_id`, `bin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_bin_id` (`bin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户-BIN关系表';
```

### 3.4 vcc_card_issue_request

```sql
CREATE TABLE `vcc_card_issue_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(64) NOT NULL COMMENT '申请单号',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `bin_id` BIGINT UNSIGNED NOT NULL COMMENT 'BIN ID',
  `holder_id` BIGINT UNSIGNED NOT NULL COMMENT '持卡人ID',
  `card_count` INT NOT NULL COMMENT '开卡数',
  `recharge_amount` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '充值金额',
  `status` VARCHAR(30) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/SUCCESS/FAIL/PARTIAL_SUCCESS',
  `submit_by` BIGINT UNSIGNED NOT NULL COMMENT '提交账号ID',
  `submit_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开卡申请单表';
```

### 3.5 vcc_card_issue_item

```sql
CREATE TABLE `vcc_card_issue_item` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `issue_request_id` BIGINT UNSIGNED NOT NULL COMMENT '申请单ID',
  `card_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '成功后关联卡ID',
  `holder_id` BIGINT UNSIGNED NOT NULL COMMENT '持卡人ID',
  `status` VARCHAR(20) NOT NULL COMMENT 'SUCCESS/FAIL/PROCESSING',
  `fail_reason_code` VARCHAR(50) DEFAULT NULL COMMENT '失败原因码',
  `fail_reason_text` VARCHAR(255) DEFAULT NULL COMMENT '标准化失败原因文案',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_issue_request_id` (`issue_request_id`),
  KEY `idx_card_id` (`card_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开卡申请单-单卡结果表';
```

### 3.6 vcc_sub_account_permission

```sql
CREATE TABLE `vcc_sub_account_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sub_account_id` BIGINT UNSIGNED NOT NULL COMMENT '子账号ID',
  `page_code` VARCHAR(50) NOT NULL COMMENT '页面编码',
  `enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_page` (`sub_account_id`, `page_code`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子账号页面权限表';
```

### 3.7 vcc_export_task

```sql
CREATE TABLE `vcc_export_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `export_type` VARCHAR(30) NOT NULL COMMENT 'TXN/RECHARGE/CARD/STATEMENT/LIMIT_HISTORY',
  `params` JSON DEFAULT NULL COMMENT '导出参数',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
  `file_type` VARCHAR(20) NOT NULL DEFAULT 'EXCEL' COMMENT '文件类型',
  `status` VARCHAR(20) NOT NULL DEFAULT 'GENERATING' COMMENT 'GENERATING/FINISHED/EXPIRED/FAIL',
  `expire_at` TIMESTAMP NULL DEFAULT NULL COMMENT '过期时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_export_type` (`export_type`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导出任务表';
```

### 3.8 vcc_message

```sql
CREATE TABLE `vcc_message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `receiver_account_id` BIGINT UNSIGNED NOT NULL COMMENT '接收账号ID',
  `category` VARCHAR(20) NOT NULL COMMENT 'SYSTEM/RISK/ACCOUNT',
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `status` VARCHAR(20) NOT NULL DEFAULT 'UNREAD' COMMENT 'UNREAD/READ/HANDLED',
  `jump_target` VARCHAR(255) DEFAULT NULL COMMENT '跳转目标',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_receiver_account_id` (`receiver_account_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息中心表';
```

### 3.9 vcc_limit_history

```sql
CREATE TABLE `vcc_limit_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `card_id` BIGINT UNSIGNED NOT NULL COMMENT '卡ID',
  `holder_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '持卡人ID',
  `before_limit` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '调整前额度',
  `after_limit` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '调整后额度',
  `adjust_amount` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '调整金额',
  `operator_id` BIGINT UNSIGNED NOT NULL COMMENT '操作账号ID',
  `adjust_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调整时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_card_id` (`card_id`),
  KEY `idx_adjust_time` (`adjust_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='共享卡限额历史表';
```

### 3.10 vcc_card_debt

```sql
CREATE TABLE `vcc_card_debt` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `card_id` BIGINT UNSIGNED NOT NULL COMMENT '卡ID',
  `debt_amount` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '欠费金额',
  `fee_details` JSON DEFAULT NULL COMMENT '欠费明细JSON',
  `status` VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/SETTLED',
  `settled_at` TIMESTAMP NULL DEFAULT NULL COMMENT '结清时间',
  `settled_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '结清操作人',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_card_id` (`card_id`),
  KEY `idx_status` (`status`),
  KEY `idx_settled_at` (`settled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡欠费记录表';
```

### 3.11 vcc_risk_rule

```sql
CREATE TABLE `vcc_risk_rule` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `conditions` JSON NOT NULL COMMENT '规则条件JSON',
  `action` VARCHAR(30) NOT NULL COMMENT 'WARN/REVIEW/FREEZE_CARD/LOCK_USER/REJECT_TXN',
  `priority` INT NOT NULL DEFAULT 100 COMMENT '优先级',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_action` (`action`),
  KEY `idx_priority` (`priority`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控规则表';
```

### 3.12 vcc_risk_event

```sql
CREATE TABLE `vcc_risk_event` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `card_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '卡ID',
  `txn_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '交易ID',
  `action` VARCHAR(30) NOT NULL COMMENT '触发动作',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/REVIEWING/RESOLVED/REJECTED',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '原因',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_card_id` (`card_id`),
  KEY `idx_txn_id` (`txn_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风控事件表';
```

### 3.13 vcc_adjustment_request

```sql
CREATE TABLE `vcc_adjustment_request` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_no` VARCHAR(64) NOT NULL COMMENT '审批单号',
  `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT 'ACCOUNT/CARD',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `adjustment_type` VARCHAR(20) NOT NULL COMMENT 'INCREASE/DECREASE',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `reason` VARCHAR(255) NOT NULL COMMENT '原因',
  `apply_by` BIGINT UNSIGNED NOT NULL COMMENT '申请人',
  `review_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '复核人',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `review_time` TIMESTAMP NULL DEFAULT NULL COMMENT '复核时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_no` (`request_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_target_type_target_id` (`target_type`, `target_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='手工调账审批表';
```

---

## 四、现有表改造设计

### 4.1 vcc_user_account → 重构建议

**现状问题：**
- 只有 balance / frozen_amount / total_recharge / total_withdraw
- 无主账号/子账号语义
- 无保证金子账户能力

**建议改造后字段：**

```sql
ALTER TABLE `vcc_user_account`
  ADD COLUMN `account_type` VARCHAR(20) NOT NULL DEFAULT 'MAIN' COMMENT 'MAIN/SUB' AFTER `user_id`,
  ADD COLUMN `parent_account_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父主账号ID' AFTER `account_type`,
  ADD COLUMN `deposit_balance` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '保证金余额' AFTER `balance`,
  ADD COLUMN `deposit_preset` DECIMAL(18,2) NOT NULL DEFAULT '0.00' COMMENT '保证金预设值' AFTER `deposit_balance`;
```

**说明：**
- 若后续决定"账号资料"和"钱包余额"拆表，可进一步拆出 vcc_wallet 表
- 当前 v1 先采用较保守改造方案

### 4.2 kyc_records → 改造建议

```sql
ALTER TABLE `kyc_records`
  ADD COLUMN `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户主体ID' AFTER `user_id`,
  ADD COLUMN `questionnaire` JSON DEFAULT NULL COMMENT '问卷JSON' AFTER `verify_data`,
  ADD COLUMN `attachments` JSON DEFAULT NULL COMMENT '附件JSON' AFTER `questionnaire`,
  ADD COLUMN `submit_count_today` INT NOT NULL DEFAULT 0 COMMENT '当日提交次数' AFTER `attachments`,
  ADD COLUMN `reviewer_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID' AFTER `fail_reason`,
  ADD COLUMN `review_time` TIMESTAMP NULL DEFAULT NULL COMMENT '审核时间' AFTER `reviewer_id`;
```

### 4.3 vcc_card_holder → 改造建议

```sql
ALTER TABLE `vcc_card_holder`
  ADD COLUMN `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户主账号ID' AFTER `user_id`,
  ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注' AFTER `status`,
  ADD COLUMN `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记' AFTER `remark`;
```

### 4.4 vcc_card → 改造建议

```sql
ALTER TABLE `vcc_card`
  ADD COLUMN `first6` VARCHAR(10) DEFAULT NULL COMMENT '卡号前六位' AFTER `card_no_mask`,
  ADD COLUMN `last4` VARCHAR(10) DEFAULT NULL COMMENT '卡号后四位' AFTER `first6`,
  ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注' AFTER `budget_amount`;
```

**说明：**
- balance 字段继续保留 DECIMAL(18,2)，天然支持负数
- PREPAID 卡允许负数，不需改类型，只需改业务校验

### 4.5 vcc_fee_config → 重构建议

**当前问题：**
- 结构仅适合基础费率，不支持：平台默认 vs 商户费率、阈值条件、扣费来源、版本管理

**建议字段：**

```sql
ALTER TABLE `vcc_fee_config`
  ADD COLUMN `scope` VARCHAR(30) NOT NULL DEFAULT 'PLATFORM_DEFAULT' COMMENT 'PLATFORM_DEFAULT/MERCHANT' AFTER `id`,
  ADD COLUMN `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID' AFTER `user_id`,
  ADD COLUMN `condition_type` VARCHAR(20) NOT NULL DEFAULT 'TRIGGER' COMMENT 'TRIGGER/GTE/LT' AFTER `fee_type`,
  ADD COLUMN `threshold_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '阈值金额' AFTER `condition_type`,
  ADD COLUMN `charge_source` VARCHAR(30) NOT NULL DEFAULT 'MAIN_ACCOUNT' COMMENT 'CARD_BALANCE/MAIN_ACCOUNT' AFTER `threshold_amount`,
  ADD COLUMN `version_no` INT DEFAULT NULL COMMENT '版本号（平台费率用）' AFTER `charge_source`;
```

### 4.6 vcc_recharge → 改造建议

```sql
ALTER TABLE `vcc_recharge`
  ADD COLUMN `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID' AFTER `order_no`,
  ADD COLUMN `recharge_scene` VARCHAR(30) NOT NULL DEFAULT 'ACCOUNT_USDT' COMMENT 'ACCOUNT_USDT/CARD_LOAD' AFTER `recharge_type`,
  ADD COLUMN `chain_type` VARCHAR(20) DEFAULT NULL COMMENT 'TRC20/ERC20/BEP20' AFTER `recharge_scene`;
```

### 4.7 vcc_transaction → 重构建议

**当前问题：**
- 仅支持简单 AUTH/CAPTURE/REFUND/REVERSE
- 不支持平台展示交易类型、关联手续费、展示模式、失败原因标准化

**建议改造字段：**

```sql
ALTER TABLE `vcc_transaction`
  ADD COLUMN `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商户ID' AFTER `txn_id`,
  ADD COLUMN `holder_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '持卡人ID' AFTER `card_id`,
  ADD COLUMN `txn_category` VARCHAR(20) NOT NULL DEFAULT 'CONSUMPTION' COMMENT '交易大类' AFTER `txn_type`,
  ADD COLUMN `related_txn_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联原交易ID' AFTER `status`,
  ADD COLUMN `fail_reason_code` VARCHAR(50) DEFAULT NULL COMMENT '失败原因码' AFTER `fail_reason`,
  ADD COLUMN `fail_reason_text` VARCHAR(255) DEFAULT NULL COMMENT '标准化失败原因文案' AFTER `fail_reason_code`,
  ADD COLUMN `display_mode` VARCHAR(20) NOT NULL DEFAULT 'INDEPENDENT' COMMENT 'INDEPENDENT/MERGED/CARD_ONLY' AFTER `fail_reason_text`;
```

---

## 五、索引建议

### 5.1 高频查询索引

| 表 | 索引建议 | 用途 |
|----|----------|------|
| vcc_transaction | (merchant_id, txn_time) | 商户交易时间检索 |
| vcc_transaction | (card_id, txn_time) | 卡详情交易页 |
| vcc_recharge | (merchant_id, created_at) | 充值记录页 |
| vcc_card | (merchant_id, status) | 卡片列表 |
| vcc_card_holder | (merchant_id, status) | 持卡人列表 |
| vcc_message | (receiver_account_id, status, created_at) | 消息中心 |
| vcc_export_task | (merchant_id, status) | 下载中心 |
| vcc_sub_account_permission | (sub_account_id, page_code) | 页面鉴权 |

### 5.2 唯一约束建议

| 表 | 唯一约束 |
|----|----------|
| vcc_channel | channel_code |
| vcc_card_bin | channel_id + bin_code |
| vcc_merchant_bin | merchant_id + bin_id |
| vcc_card_issue_request | apply_no |
| vcc_adjustment_request | request_no |
| vcc_sub_account_permission | sub_account_id + page_code |

---

## 六、分阶段落地建议

### P0（先做）

- vcc_channel
- vcc_card_bin
- vcc_merchant_bin
- vcc_card_issue_request
- vcc_card_issue_item
- vcc_adjustment_request
- 改造 vcc_user_account / vcc_fee_config / vcc_transaction / vcc_recharge / vcc_card / vcc_card_holder / kyc_records

### P1（第二批）

- vcc_sub_account_permission
- vcc_export_task
- vcc_message
- vcc_limit_history
- vcc_card_debt
- vcc_risk_rule
- vcc_risk_event

---

## 七、下一步建议

1. 基于本 DDL 设计，先由 Agent A2 产出正式 SQL 脚本
2. 再由各模块 Agent 并行开发对应 Mapper / Entity / Service / Controller
3. QA 阶段重点检查：
   - 状态 code 是否与统一字典一致
   - 导出/消息/交易展示是否符合商户端规则
   - 账户余额、保证金、欠费是否存在串账风险

---

## 八、异步任务模型承接（承接 02.6《异步任务状态机与补偿规范》）

### 8.1 承接说明

为避免 02.6 仅停留在规范层、未在 03 数据库设计层形成正式落库，本节补充异步任务统一表结构，作为后续开卡、充值、交易修复、Webhook 补处理、对账修正等异步场景的数据库承接基线。

本节新增以下四张表：
- vcc_async_task：异步任务主表
- vcc_async_task_log：异步任务状态流转与执行日志表
- vcc_async_task_retry：异步任务重试计划与执行记录表
- vcc_async_task_compensation：异步任务补偿记录表

**状态承接原则：**
- 状态机主状态以 INIT / PENDING / PROCESSING / SUCCESS / FAIL / CANCELLED / DEAD 为核心集合
- 重试与补偿不直接覆盖主状态，而通过独立记录表承接"为什么重试、何时补偿、补偿是否成功"
- 所有异步任务必须具备幂等键、业务关联键、可追溯日志、失败原因、下一次调度时间
- 对资金、卡状态、开户、充值、交易修正等不可丢失场景，不允许只在应用日志中留痕，必须落库

### 8.2 vcc_async_task

**用途：** 统一承接系统异步任务主记录，描述"这个任务是什么、当前执行到哪一步、是否还能继续重试、关联哪笔业务"。该表是 02.6 状态机的主落点。

```sql
CREATE TABLE `vcc_async_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` VARCHAR(64) NOT NULL COMMENT '异步任务号',
  `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型：CARD_OPEN/RECHARGE_SUBMIT/RECHARGE_NOTIFY/WEBHOOK_PROCESS/TRANSACTION_REPAIR/STATEMENT_SYNC 等',
  `biz_type` VARCHAR(50) NOT NULL COMMENT '业务类型：CARD/RECHARGE/TRANSACTION/WEBHOOK/ACCOUNT',
  `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务主键ID',
  `biz_no` VARCHAR(64) DEFAULT NULL COMMENT '业务单号',
  `merchant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商户ID',
  `card_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联卡ID',
  `holder_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联持卡人ID',
  `source_system` VARCHAR(50) NOT NULL DEFAULT 'VCC' COMMENT '来源系统',
  `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
  `request_data` JSON DEFAULT NULL COMMENT '任务请求快照',
  `context_data` JSON DEFAULT NULL COMMENT '上下文快照',
  `status` VARCHAR(20) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/PENDING/PROCESSING/SUCCESS/FAIL/CANCELLED/DEAD',
  `retry_status` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/WAITING/RETRYING/EXHAUSTED',
  `compensation_status` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/PENDING/PROCESSING/SUCCESS/FAIL',
  `max_retry_count` INT NOT NULL DEFAULT 0 COMMENT '最大重试次数',
  `current_retry_count` INT NOT NULL DEFAULT 0 COMMENT '当前已重试次数',
  `next_retry_at` TIMESTAMP NULL DEFAULT NULL COMMENT '下次重试时间',
  `last_execute_at` TIMESTAMP NULL DEFAULT NULL COMMENT '最近执行时间',
  `finished_at` TIMESTAMP NULL DEFAULT NULL COMMENT '任务完成时间',
  `error_code` VARCHAR(64) DEFAULT NULL COMMENT '最近失败码',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '最近失败信息',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  UNIQUE KEY `uk_idempotency_key` (`idempotency_key`),
  KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`),
  KEY `idx_biz_no` (`biz_no`),
  KEY `idx_task_type_status` (`task_type`, `status`),
  KEY `idx_retry_status_next_retry_at` (`retry_status`, `next_retry_at`),
  KEY `idx_compensation_status` (`compensation_status`),
  KEY `idx_merchant_id_created_at` (`merchant_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务主表';
```

**关键字段说明：**
- task_no：异步任务唯一编号，用于全链路追踪。
- task_type：区分开卡、充值、Webhook、交易修复等执行器路由。
- biz_type / biz_id / biz_no：把任务与业务单据绑定，避免异步任务游离。
- idempotency_key：承接 02.6 的幂等要求，避免重复投递、重复补偿。
- status：承接 02.6 主状态机。
- retry_status / compensation_status：将"主状态""重试状态""补偿状态"拆开，避免一个字段承载过多语义。
- current_retry_count / max_retry_count / next_retry_at：承接 02.6 的重试调度规则。
- error_code / error_message：保留最近一次失败上下文，便于快速定位。

**与 02.6 状态机/补偿规则的关系：**
- INIT/PENDING/PROCESSING/SUCCESS/FAIL/CANCELLED/DEAD 对应 02.6 的任务生命周期主状态。
- 达到可重试失败时，主表状态可仍为 FAIL 或回到 PENDING，但重试编排必须由 retry_status 与重试子表共同表达。
- 达到不可自动恢复或需要逆向动作的失败时，由 compensation_status=PENDING 进入补偿流程。
- DEAD 用于明确"任务已穷尽，退出自动恢复队列"，避免无限重试。

### 8.3 vcc_async_task_log

**用途：** 记录异步任务每一次状态流转、调度、执行结果、人工干预与关键上下文，是 02.6 中"可审计、可回放、可追溯"的直接落库承接。

```sql
CREATE TABLE `vcc_async_task_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '异步任务ID',
  `task_no` VARCHAR(64) NOT NULL COMMENT '异步任务号',
  `action_type` VARCHAR(30) NOT NULL COMMENT '动作类型：CREATE/DISPATCH/EXECUTE/STATE_CHANGE/RETRY/COMPENSATE/CANCEL/MANUAL_FIX',
  `from_status` VARCHAR(20) DEFAULT NULL COMMENT '变更前状态',
  `to_status` VARCHAR(20) DEFAULT NULL COMMENT '变更后状态',
  `success_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功',
  `trigger_source` VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT '触发来源：SYSTEM/SCHEDULER/MANUAL/WEBHOOK',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '人工处理人ID',
  `retry_no` INT DEFAULT NULL COMMENT '对应第几次重试',
  `compensation_no` INT DEFAULT NULL COMMENT '对应第几次补偿',
  `message` VARCHAR(500) DEFAULT NULL COMMENT '摘要信息',
  `request_snapshot` JSON DEFAULT NULL COMMENT '请求快照',
  `response_snapshot` JSON DEFAULT NULL COMMENT '响应快照',
  `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误码',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id_created_at` (`task_id`, `created_at`),
  KEY `idx_task_no_created_at` (`task_no`, `created_at`),
  KEY `idx_action_type` (`action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务日志表';
```

**关键字段说明：**
- action_type：区分创建、派发、执行、重试、补偿、取消、人工修复等动作。
- from_status / to_status：把状态迁移轨迹显式化。
- trigger_source：区分系统自动触发、调度器触发、人工干预、Webhook 驱动。
- retry_no / compensation_no：把日志与第几次重试/补偿绑定。
- request_snapshot / response_snapshot：保存关键执行快照，避免只看业务表看不出当时入参与出参。

**与 02.6 状态机/补偿规则的关系：**
- 02.6 要求所有状态迁移必须可审计，本表就是审计落点。
- 02.6 要求补偿动作不能"静默执行"，补偿发起、成功、失败都必须写日志。
- 人工介入修改状态时，不允许只改主表，必须同步写 MANUAL_FIX 日志。

### 8.4 vcc_async_task_retry

**用途：** 承接重试计划、重试原因、退避策略与每次重试结果。主表只表达"是否还要重试"，该表表达"具体怎么重试过"。

```sql
CREATE TABLE `vcc_async_task_retry` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '异步任务ID',
  `task_no` VARCHAR(64) NOT NULL COMMENT '异步任务号',
  `retry_no` INT NOT NULL COMMENT '第几次重试',
  `retry_reason` VARCHAR(255) DEFAULT NULL COMMENT '重试原因',
  `retry_strategy` VARCHAR(30) NOT NULL DEFAULT 'EXPONENTIAL_BACKOFF' COMMENT 'FIXED/LINEAR/EXPONENTIAL_BACKOFF/MANUAL',
  `planned_at` TIMESTAMP NOT NULL COMMENT '计划重试时间',
  `started_at` TIMESTAMP NULL DEFAULT NULL COMMENT '实际开始时间',
  `finished_at` TIMESTAMP NULL DEFAULT NULL COMMENT '实际结束时间',
  `status` VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/PROCESSING/SUCCESS/FAIL/CANCELLED',
  `error_code` VARCHAR(64) DEFAULT NULL COMMENT '失败码',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '失败信息',
  `request_snapshot` JSON DEFAULT NULL COMMENT '重试请求快照',
  `response_snapshot` JSON DEFAULT NULL COMMENT '重试响应快照',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_retry_no` (`task_id`, `retry_no`),
  KEY `idx_status_planned_at` (`status`, `planned_at`),
  KEY `idx_task_id_created_at` (`task_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务重试表';
```

**关键字段说明：**
- retry_no：明确第几次重试，避免与日志语义混淆。
- retry_strategy：承接固定间隔、线性退避、指数退避、人工触发等策略。
- planned_at / started_at / finished_at：把"计划执行"和"实际执行"分开，方便排查调度延迟。
- status：独立表达单次重试本身的执行结果。

**与 02.6 状态机/补偿规则的关系：**
- 02.6 要求可配置重试上限和退避策略，本表是具体承接点。
- 当某次重试成功时，主任务可以恢复到 SUCCESS；当所有重试穷尽时，主任务 retry_status=EXHAUSTED，并按规则进入补偿评估或 DEAD。
- 若为人工触发重试，retry_strategy=MANUAL，同时日志表应记录 MANUAL_FIX 或 RETRY 动作。

### 8.5 vcc_async_task_compensation

**用途：** 承接异步任务失败后的补偿动作，例如回滚账户金额、回退卡状态、撤销中间流水、恢复业务单状态等。该表解决 02.6 中"失败后怎么收口"的落库问题。

```sql
CREATE TABLE `vcc_async_task_compensation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT UNSIGNED NOT NULL COMMENT '异步任务ID',
  `task_no` VARCHAR(64) NOT NULL COMMENT '异步任务号',
  `compensation_no` INT NOT NULL COMMENT '第几次补偿',
  `compensation_type` VARCHAR(50) NOT NULL COMMENT '补偿类型：ACCOUNT_ROLLBACK/CARD_STATUS_RESTORE/RECHARGE_REVERSE/TXN_REPAIR/WEBHOOK_REPLAY 等',
  `trigger_reason` VARCHAR(255) DEFAULT NULL COMMENT '触发补偿原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/SUCCESS/FAIL/CANCELLED',
  `execute_mode` VARCHAR(20) NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO/MANUAL',
  `request_snapshot` JSON DEFAULT NULL COMMENT '补偿请求快照',
  `result_snapshot` JSON DEFAULT NULL COMMENT '补偿结果快照',
  `error_code` VARCHAR(64) DEFAULT NULL COMMENT '失败码',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '失败信息',
  `executed_at` TIMESTAMP NULL DEFAULT NULL COMMENT '执行时间',
  `finished_at` TIMESTAMP NULL DEFAULT NULL COMMENT '完成时间',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '人工补偿操作人ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_compensation_no` (`task_id`, `compensation_no`),
  KEY `idx_status_created_at` (`status`, `created_at`),
  KEY `idx_compensation_type` (`compensation_type`),
  KEY `idx_task_id_created_at` (`task_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务补偿表';
```

**关键字段说明：**
- compensation_no：明确第几次补偿尝试。
- compensation_type：区分资金回滚、卡状态恢复、充值冲正、交易修复、Webhook 重放等补偿动作。
- execute_mode：区分系统自动补偿与人工补偿。
- request_snapshot / result_snapshot：保留补偿前后证据，防止"补偿做了但不可验证"。
- operator_id：人工补偿时保留责任人。

**与 02.6 状态机/补偿规则的关系：**
- 02.6 的核心要求之一是：补偿是独立流程，不得与普通重试混为一谈；本表即承接该规则。
- 对资金类、账户类、卡状态类场景，补偿结果必须落库，不可仅依赖业务表最终态推断。
- 自动补偿失败后，任务不应直接消失，应保留失败补偿记录并进入人工处理队列。

### 8.6 关系与落地约束

**表关系建议：**
- vcc_async_task 1 --- n vcc_async_task_log
- vcc_async_task 1 --- n vcc_async_task_retry
- vcc_async_task 1 --- n vcc_async_task_compensation

**落地约束：**
- 任一异步任务创建时，必须先写 vcc_async_task，再进入调度队列。
- 任一状态迁移、重试、补偿、人工修正，必须至少写一条 vcc_async_task_log。
- 任一可自动恢复失败，必须落一条 vcc_async_task_retry 记录后再进入下次调度。
- 任一进入补偿判定的失败，必须落一条 vcc_async_task_compensation 记录，不允许口头补偿或只在代码注释中声明补偿。
- 资金、开卡、充值、交易修复类异步任务默认纳入该模型；纯临时性、可丢失的低价值任务如缓存刷新，不纳入本模型。

### 8.7 分阶段落地建议补充

建议将以下四张表加入数据库落地优先级：

- **P0.5：** vcc_async_task、vcc_async_task_log
- **P1：** vcc_async_task_retry、vcc_async_task_compensation

**原因：**
- 主表与日志表是 02.6 承接闭环的最低要求，没有这两张表就无法证明异步任务真正落库。
- 重试表与补偿表建议紧随其后落地，否则状态机只能"记状态"，不能"记恢复过程"。

### 8.8 对旧《VCC 数据库设计文档》的迁移建议

当前旧文档《VCC 数据库设计文档》（docx token: RheldFtlSoWz8RxxyrNcuIdXnoB）仍停留在早期草稿阶段，未承接 02.6 的异步任务模型。

正式承接节点建议以下规则：
- 以本文件《VCC 数据库 DDL 设计 v1》作为当前正式数据库承接节点；
- 旧文档后续若继续保留，应至少补一段"异步任务模型已迁移至《VCC 数据库 DDL 设计 v1》第八章"；
- 后续正式 SQL 脚本、实体映射、任务调度实现，均以本节四张表为基线，不再以旧草稿文档为准。
