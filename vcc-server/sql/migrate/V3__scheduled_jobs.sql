-- VCC 定时任务注册 - Agent E2
-- 执行前提：sys_job 表已存在（RuoYi 框架自带）

-- 导出任务处理（每分钟执行）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, concurrent, status, misfire_policy, create_by, create_time, update_by, update_time)
VALUES ('导出任务处理', 'EXPORT', 'exportTaskJob.processPendingTasks()', '0 */1 * * * ?', '1', '0', '3', 'admin', sysdate(), '', null);

-- 过期导出清理（每天凌晨3点）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, concurrent, status, misfire_policy, create_by, create_time, update_by, update_time)
VALUES ('过期导出清理', 'EXPORT', 'exportTaskJob.cleanupExpiredTasks()', '0 0 3 * * ?', '1', '0', '3', 'admin', sysdate(), '', null);

-- 欠费检查通知（每天上午9点）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, concurrent, status, misfire_policy, create_by, create_time, update_by, update_time)
VALUES ('欠费检查通知', 'DEBT', 'debtCheckJob.checkOutstandingDebts()', '0 0 9 * * ?', '1', '0', '3', 'admin', sysdate(), '', null);
