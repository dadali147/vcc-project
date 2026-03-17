# Backend Bug Fix Code Review Report
## 审查摘要
- 本次按用户提供的 `9` 个 commit hash 审查；需求文案写的是 `13个`，但实际只给出了 `9` 个哈希。
- 本次结论来自 diff 和静态代码审查。当前环境缺少 `mvn`/`mvnw`，未能执行编译和自动化测试。
- 结论汇总：`2` 个提交可通过，`1` 个提交建议调整，`6` 个提交存在实质问题。
- 最高风险集中在 4 个方面：Webhook 验签仍未真正生效、充值失败后缺少余额补偿、商户资源归属校验仍不完整、事务/并发修复有多处仍未真正落地。

## 各提交审查结果
### 3389aa6: fix(upstream): 修复 YOP 签名和 Webhook 回调
- ❌ 问题
- `WebhookController.verifyWebhookSignature()` 仍是 TODO 且固定返回 `true`，`/webhook/yeevcc` 实际不会拒绝任何伪造回调；提交说明中的“修复签名/回调”没有真正落地。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookController.java:42-47`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookController.java:67-81`。
- `WebhookServiceImpl.handle3dsOtp()` 新增了 `StringUtils.isBlank(...)`，但文件头没有导入 `com.vcc.common.utils.StringUtils`，这是静态可见的编译错误。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:1-22`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:278`。
- `handle3dsOtp()` 和入口日志把 `otpCode`、手机号等敏感信息直接写到 `info` 日志，存在凭证泄露风险。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:298-299`。
- 建议修改：使用原始请求体做真实验签，缺签/错签直接拒绝；修正 `StringUtils` 导入；OTP 和手机号只做脱敏日志或完全不落 `info`。

### ffd4a68: fix(upstream): 修复 VCC-003/004 路径和签名问题
- ✅ 通过
- `addCardHolder` 路径修正和 GET query 编码统一，和 `buildUri()` 的编码逻辑保持一致，当前未发现新的事务、并发或安全回归。
- 建议修改：无阻断问题。

### 82d9510: fix(upstream/core): 修复 VCC-005/008 解密和充值问题
- ❌ 问题
- `submitRecharge()` 先扣用户余额，再把充值单置为 `PENDING`；但最终失败时，无论是主动查询失败还是 Webhook 回调失败，都没有把余额补回去，用户资金会被永久扣住。参考 `vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:123-182`、`vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:223-243`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:237-257`。
- `submitRecharge()` 只校验了卡片存在和 `upstreamCardId`，没有校验 `card.userId == userId`；商户可以用自己的余额给别人的卡充值，属于越权资源操作。参考 `vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:102-111`。
- 建议修改：把充值状态机改成“扣款保留/终态成功确认/终态失败补偿”的显式流程，并在提交前强制校验卡片归属。

### cd48496: fix(webhook): 修复 VCC-011/012 验签和幂等键问题
- ❌ 问题
- `selectByIdempotencyKey()` 已加到 `WebhookLogMapper` 并在 `enqueueWebhook()` 中调用，但 `WebhookLogMapper.xml` 没有对应 SQL，`vcc_webhook_log` 表结构和插入 SQL 也没有 `idempotency_key` 字段。这会在首个 webhook 请求到来时直接触发 MyBatis 绑定异常，幂等逻辑不可用。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/mapper/WebhookLogMapper.java:14-18`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:60-79`、`vcc-server/vcc-card/src/main/resources/mapper/card/WebhookLogMapper.xml:7-77`、`vcc-server/sql/vcc_init.sql:267-284`。
- `enqueueWebhook()` 同类内直接调用 `processWebhookAsync(Long, ...)`，`@Async` 不会生效，控制器线程仍会同步执行后续业务；“入队后立即返回 200”这个目标没有实现。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:81-82`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:93-131`。
- 提交本身引入了一个多余的 `}`，`WebhookServiceImpl` 在兼容方法后提前闭合，后续私有方法全部落到类外，属于静态可见的编译错误。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:133-139`。
- `WebhookController` 虽然改成“验签失败返回 401”，但底层 `verifyWebhookSignature()` 仍固定返回 `true`，所以 VCC-011 实际仍未完成。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookController.java:42-47`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookController.java:67-81`。
- 建议修改：补齐 `idempotency_key` 的 schema、XML 和唯一索引；把异步逻辑抽到独立 Bean；修复括号错误；真实实现验签，而不是保留放行 stub。

### df1d23f: fix(webhook): 修复 VCC-013 充值回调查询条件失效
- ✅ 通过
- `RechargeMapper.xml` 已补上 `upstream_order_no` 查询条件，和 `handleRechargeResult()` 的查询方式一致，修复方向正确。参考 `vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:46-50`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:226-235`。
- 建议修改：该修复应和“充值失败补账”一起交付，否则只能把记录正确更新为失败，不能恢复用户余额。

### 7d94115: fix(core): 修复 VCC-009 商户接口资源归属校验
- ❌ 问题
- 本次只修了 `list` 和 `getInfo`，但 `CardController` 的 `activate/freeze/unfreeze/cancel/key-info` 仍然只按 `id` 操作；其中 `key-info` 会直接返回完整卡号、CVV、有效期，属于高风险越权读取。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardController.java:68-100`、`vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardServiceImpl.java:204-330`。
- `CardHolderController` 的 `edit/remove` 也没有归属校验，仍可按主键修改或删除别人的持卡人。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardHolderController.java:62-73`、`vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardHolderServiceImpl.java:71-84`。
- 建议修改：把 `userId` 透传到 service 层，对所有读写接口统一做资源归属校验，不要只在 controller 的部分读接口补丁式处理。

### 37e48f7: fix(core): 修复 VCC-010 openCard 事务边界过大
- ❌ 问题
- `saveCardInTransaction()` 虽然标了 `@Transactional`，但它是 `CardServiceImpl.openCard()` 对同类方法的直接调用，Spring 事务代理不会介入，事务边界并没有真正缩小成功。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardServiceImpl.java:119-127`。
- 建议修改：把落库逻辑提到独立 Bean，或改用 `TransactionTemplate` 显式包裹，只在真正需要的写库阶段开启事务。

### 097522f: fix(webhook): 修复 VCC-014 handleTransaction 事务不生效
- ⚠️ 建议
- 把 `handleTransaction()` 拆到 `WebhookTransactionService` 独立 Bean 后，事务代理问题本身已经修正，方向正确。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:106-110`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookTransactionService.java:33-91`。
- 但并发幂等仍是“先查再插”，重复回调并发进入时，第二次请求仍可能撞上 `uk_txn_id` 唯一索引并被外层记成失败日志，行为不够稳定。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookTransactionService.java:41-74`、`vcc-server/sql/vcc_init.sql:250`。
- 建议修改：捕获 `DuplicateKeyException` 作为幂等成功处理，或者直接改成数据库原子 upsert。

### 165d4e0: fix(finance): 修复 VCC-016 风控日限额并发问题
- ❌ 问题
- `selectTodayRechargeTotal()` 只统计 `status = 1` 的成功充值；而 `submitRecharge()` 在 VCC-008 后把新请求先记为 `PENDING`。这意味着并发或连续提交的待处理充值都不会进入风控统计，日限额仍然可以在回调到达前被绕过。参考 `vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:58-66`、`vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:88-99`、`vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:141-156`。
- 当前实现只是把内存聚合改成数据库聚合，没有对用户账户或风控计数做任何锁定，严格来说并没有解决并发竞争。
- 建议修改：把 `PENDING` 也纳入额度占用，或者在用户账户/风控表上做原子预占和行锁控制，再由回调释放或确认。

## 总体评价
- 本轮修复里，`ffd4a68` 和 `df1d23f` 两个提交质量相对稳定；`097522f` 的事务修复方向正确，但幂等并发兜底还不够。
- 其余提交中有多处“看起来修了，但代理/Schema/补偿链路没有真正闭合”的问题，尤其是 `Webhook` 和 `Recharge` 两条主链路，当前不适合直接视为可上线状态。
- 如果按风险优先级排序，建议先修 `Webhook 验签/幂等/编译问题`、`充值失败补账`、`商户越权访问`、`openCard 事务自调用失效`，再处理风控并发和日志脱敏。

## 需要修复的问题清单
- 修复 `WebhookController` 的真实验签逻辑，使用原始请求体，拒绝缺签/错签请求。
- 补齐 `WebhookLog` 的 `idempotency_key` 数据库字段、Mapper XML、唯一索引和插入/查询 SQL。
- 修正 `WebhookServiceImpl` 的编译错误和 `@Async` 自调用问题。
- 为充值失败终态补做余额补偿，并把补偿流程做成幂等事务。
- 在 `submitRecharge()`、`key-info`、卡片状态变更、持卡人编辑/删除等入口统一补资源归属校验。
- 将 `openCard` 的最终写库事务提到独立 Bean 或显式事务模板中。
- 重做日限额并发控制，至少把 `PENDING` 订单纳入额度占用。
