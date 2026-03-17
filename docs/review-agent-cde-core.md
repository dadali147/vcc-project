# Code Review: Core/Webhook/Admin 模块 [Agent-F]
## 总体评分：3/10

## ✅ 优点
- 开卡主链路已经串起了“上游请求 -> 轮询 -> 本地落库”，卡三要素没有直接写入 `vcc_card`。
- webhook 已有验签、原始日志、交易/卡状态/充值结果分流的基础骨架。
- admin 侧补齐了卡片、持卡人、交易、充值、费率、系统配置的管理入口，并统一加了 `admin` 角色限制。

## ⚠️ 需要改进（按模块）
### Agent C / Core
- 充值链路没有扣减用户账户余额，也没有在服务端计算手续费；`fee` 直接由调用方传入，无法满足“储值卡/预算卡按费用规则扣款”的要求。参考 `aee4980 vcc-server/vcc-finance/src/main/java/com/vcc/finance/controller/RechargeController.java:46-56`、`aee4980 vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:58-110`。
- 商户侧接口普遍缺少资源归属校验：列表接口不强制 `userId = getUserId()`，详情/操作接口也只按主键查，存在跨商户读写风险。参考 `aee4980 vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardController.java:30-92`、`aee4980 vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardHolderController.java:30-65`、`aee4980 vcc-server/vcc-user/src/main/java/com/vcc/user/controller/UserAccountController.java:29-57`、`aee4980 vcc-server/vcc-finance/src/main/java/com/vcc/finance/controller/RechargeController.java:30-76`。
- `openCard` 在事务内做远程调用和最多 30 秒轮询，事务边界过大，容易长时间占用事务和连接。参考 `aee4980 vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardServiceImpl.java:62-176`。

### Agent D / Webhook
- webhook 入口先回 `200 ok`，后异步验签和执行业务；任何验签失败、DB 异常、业务异常都不会触发上游重试，事件会直接丢失。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookController.java:29-41`。
- 幂等键设计不正确：`CARD_STATUS_CHANGE` 用 `cardId` 作为 `upstreamTxnId`，同一张卡后续冻结、解冻、销卡回调都会被当成重复消息跳过。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:75-80`、`9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:292-300`。
- 充值回调按 `upstreamOrderNo` 查单的代码和 mapper 不一致，实际 SQL 根本没有按 `upstream_order_no` 过滤，会误更新最新一条充值记录。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:254-285`、`aee4980 vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:43-53`。
- `handleTransaction` 标了 `@Transactional`，但它是同类内部调用，Spring 事务代理不会生效；交易落库和卡余额更新并不原子。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:51-105`、`9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:142-200`。

### Agent E / Admin / Config
- 新增 `FeeConfig` 只有 CRUD，没有任何业务代码真正读取并应用它；运行期仍然信任前端传入 `fee`。参考 `26e899d vcc-server/vcc-card/src/main/java/com/vcc/card/controller/admin/FeeConfigController.java:23-72`、`26e899d vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/FeeConfigServiceImpl.java:19-53`、`26e899d vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:103-123`。
- 风控日限额校验是“先查所有成功充值 -> 内存过滤今天 -> 再提交”，没有并发保护，双并发请求可以同时通过检查。参考 `26e899d vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:83-100`。
- 管理统计和导出直接全量查表后在内存里聚合和拼 CSV，可用性和安全性都偏弱。参考 `26e899d vcc-server/vcc-card/src/main/java/com/vcc/card/controller/admin/AdminRechargeController.java:47-74`、`26e899d vcc-server/vcc-card/src/main/java/com/vcc/card/controller/admin/AdminTransactionController.java:42-87`。

## 🐛 发现的 Bug
1. `submitRecharge` 调上游时把本地 `cardId` 当成上游卡号传出，充值会打到错误标识。参考 `26e899d vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:118-123`。
2. `submitRecharge` 从未调用 `userAccountService.deductBalance(...)`，储值卡充值不会扣用户余额。参考 `aee4980 vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:58-110`。
3. `usdt-arrival` 允许请求方指定任意 `userId`，且 `txHash` 无幂等校验，重复提交会重复加钱。参考 `aee4980 vcc-server/vcc-finance/src/main/java/com/vcc/finance/controller/RechargeController.java:66-76`、`26e899d vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:190-214`、`vcc-server/sql/vcc_init.sql:197-221`。
4. webhook 充值回调查询条件失效，会把状态写到错误的充值记录。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:254-285`、`aee4980 vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:43-53`。
5. 卡状态 webhook 的去重键错误，同一张卡第二次状态变化开始就不会再处理。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:75-80`、`9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:292-300`。
6. `SystemConfigServiceImpl.updateSystemConfig()` 试图支持 `******` 占位符，但没有把该值置空，最终会把真实密文覆盖成 `******`。参考 `26e899d vcc-server/vcc-system/src/main/java/com/vcc/system/service/impl/SystemConfigServiceImpl.java:125-138`、`26e899d vcc-server/vcc-system/src/main/resources/mapper/system/SystemConfigMapper.xml:62-73`。
7. `submitRecharge` 在初次调用成功时直接把本地状态置为 `SUCCESS`，导致 `queryRechargeResult` 和 webhook 里的“待处理 -> 终态”分支基本失效。参考 `26e899d vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:125-153`、`9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:265-287`。

## 🔒 安全隐患
- 商户接口未做数据归属控制，任意有权限的商户都能遍历和操作其他商户的卡、持卡人、账户、充值记录，`key-info` 还能取到完整卡三要素。参考 `aee4980 vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardController.java:30-92`、`aee4980 vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardHolderController.java:30-65`、`aee4980 vcc-server/vcc-user/src/main/java/com/vcc/user/controller/UserAccountController.java:29-57`、`aee4980 vcc-server/vcc-finance/src/main/java/com/vcc/finance/controller/RechargeController.java:30-76`。
- `vcc_card_holder.id_card` 明文落库，没有加密或脱敏处理。参考 `aee4980 vcc-server/vcc-card/src/main/java/com/vcc/card/domain/CardHolder.java:28-40`、`aee4980 vcc-server/vcc-card/src/main/resources/mapper/card/CardHolderMapper.xml:57-79`。
- webhook 原始 payload 在入口日志和 `vcc_webhook_log` 中完整保存，缺少字段级脱敏策略。参考 `9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookController.java:33-39`、`9138303 vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:58-66`。
- 敏感配置加密方案不安全：使用硬编码默认密钥和 `AES/ECB`；另外 `/set` 新建配置默认 `isEncrypted=0`，敏感新键会明文落库。参考 `26e899d vcc-server/vcc-system/src/main/java/com/vcc/system/service/impl/SystemConfigServiceImpl.java:25-27`、`26e899d vcc-server/vcc-system/src/main/java/com/vcc/system/service/impl/SystemConfigServiceImpl.java:68-79`、`26e899d vcc-server/vcc-system/src/main/java/com/vcc/system/service/impl/SystemConfigServiceImpl.java:163-205`。
- CSV 导出未防公式注入，交易商户名等外部数据如果以 `= + - @` 开头，管理员在 Excel 中打开时存在执行风险。参考 `26e899d vcc-server/vcc-card/src/main/java/com/vcc/card/controller/admin/AdminTransactionController.java:51-87`。

## 📝 建议
1. 先修充值主链路：服务端根据卡类型和费率配置计算费用，先原子扣减账户余额，再调上游；同时用 `upstreamCardId` 发起充值。
2. 商户端所有列表、详情、操作接口统一强制 `userId = getUserId()`，`key-info` 增加二次校验和审计。
3. webhook 改成“同步验签 + 成功入队或落库后再返回 2xx”；幂等键至少要区分 `webhookType + 事件ID`，并用唯一索引或乐观更新兜底。
4. `handleTransaction`、`handleRechargeResult` 拆到独立 service bean，确保事务代理生效；充值回调查询补齐 `upstream_order_no` 条件。
5. `SystemConfig` 改成 KMS 或环境注入密钥加随机 IV 的对称加密方案；`set()` 需要显式声明是否加密，`update()` 遇到 `******` 必须保留原值。
6. `usdt-arrival` 应改为内部专用入口或受签名 webhook 保护，并对 `txHash` 加唯一约束和幂等检查。
7. 当前环境缺少 `mvn`，本次未完成编译和测试验证，建议在 CI 上补 `mvn -pl vcc-card,vcc-finance,vcc-user,vcc-system -am test`。
