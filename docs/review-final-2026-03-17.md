# Final Code Review Report - 2026-03-17

## 审查摘要（通过/问题/建议各几项）

- 通过：5 项
- 问题：7 项
- 建议：2 项
- 审查方式：基于提交 `31c453b`、`5ef851a` 的 diff 与当前代码静态审查。
- 验证限制：当前环境缺少 `mvn`，且 `vcc-web/node_modules` 不存在，未执行后端编译/测试与前端构建。
- 总结论：本轮修复完成了大部分表层补丁，但后端仍存在启动/并发/越权残留，前端仍有敏感信息清理与 loading 覆盖不完整的问题，暂不建议按“已完全收口”结论上线。

## 后端修复验证

### 1. WebhookAsyncService 是否真正实现异步

- 通过：已拆成独立 Bean，`WebhookAsyncService.processWebhookAsync()` 使用 `@Async("threadPoolTaskExecutor")`，项目也启用了 `@EnableAsync` 和线程池 Bean。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookAsyncService.java:16-28`、`vcc-server/vcc-admin/src/main/java/com/vcc/VccApplication.java:13`、`vcc-server/vcc-framework/src/main/java/com/vcc/framework/config/ThreadPoolConfig.java:32-42`。
- 问题：新实现引入了直接循环依赖 `WebhookServiceImpl -> WebhookAsyncService -> WebhookServiceImpl`。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:39-40`、`vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookAsyncService.java:24-25`。在默认 Spring Boot 配置下这属于高风险启动问题；即便容器允许循环引用，也说明异步分层还没有完全解耦。

### 2. `idempotency_key` 的 DB Schema、XML、唯一索引是否完整

- 通过：DDL 已新增 `idempotency_key` 字段和唯一索引；`WebhookLog` 域对象、Mapper XML 查询/插入映射也都补齐。参考 `vcc-server/sql/vcc_init.sql:267-282`、`vcc-server/vcc-card/src/main/java/com/vcc/card/domain/WebhookLog.java:30-31`、`vcc-server/vcc-card/src/main/resources/mapper/card/WebhookLogMapper.xml:7-74`。
- 建议：`enqueueWebhook()` 仍是“先查再插”，并把所有异常统一按失败返回；并发重复回调若直接撞上唯一索引，当前会进入 `catch` 返回 `false`，而不是按“幂等成功”处理。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:62-91`。建议单独捕获 `DuplicateKeyException` 并返回成功。

### 3. 充值失败余额补偿是否幂等

- 问题：当前补偿仍然不是幂等的。`WebhookServiceImpl.handleRechargeResult()` 与 `RechargeServiceImpl.queryRechargeResult()` 都是“先读到 `PENDING`，再补余额，最后更新状态”；`updateRecharge` 也没有 `where status = 0` 之类的状态保护。并发下，Webhook 和主动查询都可能各补一次，造成重复补偿。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/webhook/WebhookServiceImpl.java:221-246`、`vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:228-253`、`vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:98-108`。

### 4. CardPersistService 事务边界是否正确

- 通过：`openCard()` 不再同类自调用事务方法，而是在轮询完成后调用独立 Bean `CardPersistService.saveCardInTransaction()`；事务边界已经缩到最终落库阶段。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardServiceImpl.java:112-124`、`vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardPersistService.java:17-27`。

### 5. 越权校验是否覆盖所有操作入口（包括 key-info）

- 通过：卡片侧 `activate/freeze/unfreeze/cancel/key-info` 都已透传 `userId` 到 service，`getCardKeyInfo()` 也复用了统一的 `checkCardOwnership()`。参考 `vcc-server/vcc-card/src/main/java/com/vcc/card/controller/CardController.java:68-100`、`vcc-server/vcc-card/src/main/java/com/vcc/card/service/impl/CardServiceImpl.java:171-303`。
- 问题：充值侧越权校验没有覆盖所有入口。`submitRecharge()` 已校验卡归属，但 `RechargeController.list()`、`getInfo()`、`queryResult()` 仍未按当前登录用户收口，商户仍可枚举或查询他人的充值记录。参考 `vcc-server/vcc-finance/src/main/java/com/vcc/finance/controller/RechargeController.java:30-43`、`vcc-server/vcc-finance/src/main/java/com/vcc/finance/controller/RechargeController.java:59-63`、`vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:43-56`。

### 6. 风控日限额 PENDING 纳入后，是否有行锁防并发

- 通过：日限额统计 SQL 已把 `PENDING` 纳入 `status IN (0, 1)`。参考 `vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:58-66`。
- 问题：并发控制仍未闭合。`submitRecharge()` 只是先做一次聚合查询，再继续扣款和下单，没有任何 `FOR UPDATE`、账户级锁或额度预占；并发请求仍能一起通过额度检查。参考 `vcc-server/vcc-finance/src/main/java/com/vcc/finance/service/impl/RechargeServiceImpl.java:88-99`、`vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:58-66`。
- 问题：新加的 `RechargeMapper.selectTodayRechargeTotal(Long userId, String startTime, String endTime)` 没有 `@Param`，但 XML 使用了 `#{userId}`、`#{startTime}`、`#{endTime}`。项目里其他多参数 Mapper 都显式用了 `@Param`，这里很可能在运行时触发参数绑定异常。参考 `vcc-server/vcc-finance/src/main/java/com/vcc/finance/mapper/RechargeMapper.java:17-20`、`vcc-server/vcc-finance/src/main/resources/mapper/finance/RechargeMapper.xml:59-65`。

## 前端修复验证

### 1. `asyncRoutes` 角色校验是否真正生效

- 通过：`/admin` 已从 `constantRoutes` 移到 `dynamicRoutes`，并补了 `roles: ['admin']`；`permission.js` 也对 `/admin/*` 做了二次拦截，非 admin 用户直接跳 `401`。从前端路由层面看，非 admin 用户无法直接进入 `/admin/*`。参考 `vcc-web/src/router/index.js:141-213`、`vcc-web/src/store/modules/permission.js:45-48`、`vcc-web/src/permission.js:52-58`。
- 建议：`filterDynamicRoutes()` 仍然是“优先按 `permissions` 判断，再按 `roles` 判断”，而 `/admin` 同时带了 `permissions` 和 `roles`。当前因为 `beforeEach` 又补了一层角色拦截，所以访问面是收住了，但菜单生成逻辑和访问控制逻辑并不完全一致。参考 `vcc-web/src/store/modules/permission.js:99-113`。

### 2. CVV/PAN 60 秒自动清除逻辑是否可靠

- 问题：当前只对 `cvv/expMonth/expYear` 设置了 60 秒清理，PAN 没有一起清除；详情弹窗里的 `detail.cardNo` 仍然直接展示，计时器也不会在关闭弹窗时立即清空该字段。参考 `vcc-web/src/views/merchant/card/index.vue:108-118`、`vcc-web/src/views/merchant/card/index.vue:245-258`。按需求“CVV/PAN 60 秒自动清除”来看，这个修复还不完整。

### 3. 密码接口是否真的改成 body 传输

- 通过：`updateUserPwd()` 已从 `params` 改为 `data`；后端 `SysProfileController.updatePwd()` 也接受 `@RequestBody Map<String, String>`，前后端在这一点上是对齐的。参考 `vcc-web/src/api/user.js:20-26`、`vcc-server/vcc-admin/src/main/java/com/vcc/web/controller/system/SysProfileController.java:92-96`。

### 4. loading finally 是否所有页面都覆盖到

- 问题：本轮修复覆盖了大部分目标页面，但并没有“所有页面都覆盖到”。`admin/userAuth` 仍然只在成功分支里关闭 loading，接口异常时会一直转圈。参考 `vcc-web/src/views/admin/userAuth/index.vue:135-141`。

## 遗留问题（如有）

- 本次最终审查没有执行后端编译、单测、集成测试，也没有执行前端构建；结论仍以静态审查为主。
- 后端若要真正达到可上线状态，至少还需要补齐：Webhook Bean 解耦、充值终态状态迁移保护、充值查询接口用户收口、日限额并发控制。
- 前端若要真正达到“敏感信息自动清理已完成”的结论，需把 PAN 纳入同一套清理逻辑，并在弹窗关闭/组件卸载时主动擦除状态。

## 上线风险评估（高）

- 评级：高
- 原因：后端仍有循环依赖启动风险、充值失败重复补偿风险、充值记录越权读取风险、日限额并发绕过风险；前端仍有 PAN 未按要求自动清理、loading 覆盖未完全收口的问题。
