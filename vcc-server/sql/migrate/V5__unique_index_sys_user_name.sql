-- VCC-REG-001: 防止并发注册竞态条件
-- 为 sys_user.user_name 添加唯一索引，确保数据库层面的唯一性约束
ALTER TABLE sys_user ADD UNIQUE INDEX uk_user_name (user_name);
