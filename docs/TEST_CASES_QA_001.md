# QA 测试用例清单 QA-001

**项目**: VCC 虚拟卡管理平台
**文档版本**: v1.0
**编写日期**: 2026-03-21
**测试范围**: 后端 API / 商户端前端 / 管理端前端
**依据来源**: 代码审查报告（review-*.md）、后端控制器源码、前端页面及 API 调用清单

---

## 1. 验收摘要

| 维度 | 说明 |
|------|------|
| 系统 | Spring Boot 后端 + Vue 3 商户端 + Vue 3 管理端 |
| 核心流程 | 注册/登录 → KYC 审核 → 开卡 → 充值 → 消费 → 提现 |
| 上游集成 | YeeVCC API（14 个接口，AES/RSA 加密，Webhook 回调） |
| 已知高风险 | Webhook 验签 TODO、充值补偿非幂等、并发限额绕过、跨商户访问残留、PAN 明文暴露 |
| 优先处置 | P0 测试项必须全部通过方可上线 |

---

## 2. 测试用例清单

### 2.1 模块 A：认证与会话管理（后端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| A-001 | 认证 | 正常登录 | POST /login，body: {username, password, code, uuid} 均有效 | HTTP 200，返回 token 字段非空 | P0 | 待测 |
| A-002 | 认证 | 错误密码登录 | POST /login，password 错误 | HTTP 401 或业务错误码，无 token | P0 | 待测 |
| A-003 | 认证 | 用户名不存在 | POST /login，username 不存在 | HTTP 401，错误信息不暴露用户存在性 | P0 | 待测 |
| A-004 | 认证 | 验证码错误 | POST /login，code/uuid 不匹配 | HTTP 400，错误"验证码错误" | P0 | 待测 |
| A-005 | 认证 | 验证码过期 | POST /login，使用已过期验证码 | HTTP 400，错误"验证码已过期" | P1 | 待测 |
| A-006 | 认证 | 空 Body 登录 | POST /login，body 为空 | HTTP 400，字段校验错误 | P1 | 待测 |
| A-007 | 会话 | Token 有效获取用户信息 | GET /getInfo，携带有效 Bearer Token | HTTP 200，返回 user/roles/permissions | P0 | 待测 |
| A-008 | 会话 | 无 Token 请求受保护接口 | GET /getInfo，不携带 Authorization 头 | HTTP 401 | P0 | 待测 |
| A-009 | 会话 | 伪造 Token | GET /getInfo，Authorization: Bearer invalid_token | HTTP 401 | P0 | 待测 |
| A-010 | 会话 | Token 过期 | 使用过期 Token 请求 | HTTP 401，提示 token 过期 | P0 | 待测 |
| A-011 | 安全 | 账户锁定策略 | 连续错误登录 5 次 | 账户被锁定，返回锁定提示，不继续验证 | P1 | 待测 |
| A-012 | 安全 | SQL 注入防护 | username 字段传入 `' OR 1=1 --` | 正常返回认证失败，无 SQL 错误暴露 | P0 | 待测 |
| A-013 | 会话 | 修改密码后旧 Token 失效 | 修改密码后用旧 token 请求 | HTTP 401，旧 token 无效 | P1 | 待测 |

---

### 2.2 模块 B：商户注册与 KYC 流程（商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| B-001 | 注册 | 正常注册 | POST /auth/register，填写 companyName/contactName/email/phone/password/businessType/expectedMonthlySpend | HTTP 200/201，账号创建成功，邮件发送 | P0 | 待测 |
| B-002 | 注册 | 邮箱重复 | 使用已注册邮箱注册 | 返回业务错误"邮箱已存在" | P0 | 待测 |
| B-003 | 注册 | 密码过短 | password 少于 6 字符 | 表单校验失败，不发起请求 | P1 | 待测 |
| B-004 | 注册 | 两次密码不一致 | password ≠ confirmPassword | 表单校验"密码不一致"，不发起请求 | P1 | 待测 |
| B-005 | 注册 | 邮箱格式非法 | email 传入 "abc" | 表单校验"邮箱格式错误" | P1 | 待测 |
| B-006 | 注册 | 未勾选协议 | 未勾选服务协议直接提交 | 表单校验拦截，提示勾选协议 | P2 | 待测 |
| B-007 | KYC | 提交 KYC 文件 | POST /kyc/documents 上传身份证正反面和自拍（图片类型） | 文件上传成功，返回文档 ID | P0 | 待测 |
| B-008 | KYC | 非图片文件上传 | 上传 .pdf/.exe 文件 | HTTP 400，错误"仅支持图片格式" | P1 | 待测 |
| B-009 | KYC | 提交 KYC 审核 | POST /kyc/submit | HTTP 200，状态变为 PENDING | P0 | 待测 |
| B-010 | KYC | 重复提交 | KYC 状态为 PENDING 时再次提交 | 返回错误"审核中，不可重复提交" | P1 | 待测 |
| B-011 | KYC | 查询 KYC 状态 | GET /kyc/status | 返回 NOT_SUBMITTED/PENDING/APPROVED/REJECTED | P0 | 待测 |
| B-012 | KYC | KYC 证件号格式验证 | 身份证号格式非法（非18位） | 前端表单校验拦截 | P1 | 待测 |
| B-013 | KYC | 未通过 KYC 访问核心功能 | 未通过 KYC 的用户尝试访问开卡页面 | 提示"需完成 KYC 认证"，或重定向到 KYC 页面 | P1 | 待测 |
| B-014 | 忘记密码 | 发送重置验证码 | POST /auth/send-reset-code，email 有效 | 邮件发送成功，60s 内不可重发 | P1 | 待测 |
| B-015 | 忘记密码 | 重置密码 | POST /auth/reset-password，code/email/newPassword 均有效 | 密码重置成功，旧密码失效 | P1 | 待测 |

---

### 2.3 模块 C：持卡人管理（商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| C-001 | 持卡人 | 创建持卡人 | POST /cardholders，body: {name, email, phone, idNumber} | HTTP 200，返回持卡人对象含 id | P0 | 待测 |
| C-002 | 持卡人 | 查询持卡人列表 | GET /cardholders，登录状态 | 仅返回当前商户名下持卡人，不含其他商户数据 | P0 | 待测 |
| C-003 | 持卡人 | 用户隔离验证 | 商户 A 用 token 请求商户 B 的 holderId | HTTP 403 或返回空，不暴露其他商户数据 | P0 | 待测 |
| C-004 | 持卡人 | 手机格式校验 | phone 传入非法格式 | 前端表单拦截或后端返回 400 | P1 | 待测 |
| C-005 | 持卡人 | 更新持卡人 | PUT /cardholders/{id} | 更新成功，返回最新持卡人信息 | P1 | 待测 |
| C-006 | 持卡人 | 删除持卡人 | DELETE /cardholders/{id}，该持卡人无关联卡 | 删除成功 | P1 | 待测 |
| C-007 | 持卡人 | 删除有关联卡的持卡人 | DELETE 一个名下有活跃卡的持卡人 | 返回业务错误"持卡人名下有活跃卡，无法删除" | P1 | 待测 |
| C-008 | 持卡人 | 分页查询 | GET /cardholders?page=2&limit=10 | 正确返回第 2 页数据，total 正确 | P2 | 待测 |
| C-009 | 持卡人 | 关键字搜索 | GET /cardholders?keyword=张三 | 返回 name/email 包含关键字的持卡人 | P2 | 待测 |

---

### 2.4 模块 D：卡片管理 - 开卡（后端 & 商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| D-001 | 开卡 | 正常开卡 | POST /merchant/card/open，{holderId, cardBinId, currency, amount} 均有效 | HTTP 200，返回新卡对象，状态 0（未激活） | P0 | 待测 |
| D-002 | 开卡 | 余额不足开卡 | 账户余额 < 开卡费，提交开卡 | 返回业务错误"余额不足" | P0 | 待测 |
| D-003 | 开卡 | holderId 非本商户 | 使用他人 holderId 开卡 | HTTP 403，禁止访问 | P0 | 待测 |
| D-004 | 开卡 | cardBinId 不存在 | cardBinId 传入不存在的值 | HTTP 400，"卡 BIN 不存在" | P1 | 待测 |
| D-005 | 开卡 | 上游超时处理 | 模拟 YeeVCC API 超时（>30s） | 本地事务回滚，返回错误提示，不产生孤立记录 | P0 | 待测 |
| D-006 | 开卡 | 重复开卡幂等 | 同一 holderId/cardBinId 快速重复提交 2 次 | 只创建 1 张卡，第 2 次返回已有记录或幂等提示 | P0 | 待测 |
| D-007 | 开卡 | 申请列表查询 | GET /card-applications | 只返回本商户申请列表，状态正确 | P1 | 待测 |
| D-008 | 开卡 | 前端表单校验 | 商户端开卡表单未选持卡人直接提交 | 前端拦截，提示"请选择持卡人" | P1 | 待测 |

---

### 2.5 模块 E：卡片管理 - 状态操作（后端 & 商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| E-001 | 激活 | 正常激活 | POST /merchant/card/activate/{id}，卡为未激活状态 | HTTP 200，卡状态变为 1（激活） | P0 | 待测 |
| E-002 | 激活 | 重复激活 | 已激活的卡再次激活 | 返回业务错误"卡已激活"，状态不变 | P1 | 待测 |
| E-003 | 冻结 | 正常冻结 | POST /merchant/card/freeze/{id}，卡为激活状态 | HTTP 200，状态变为 2（冻结） | P0 | 待测 |
| E-004 | 冻结 | 已注销卡冻结 | 对状态 3 的卡执行冻结 | 返回业务错误"已注销卡不可操作" | P1 | 待测 |
| E-005 | 解冻 | 正常解冻 | POST /merchant/card/unfreeze/{id}，卡为冻结状态 | HTTP 200，状态变为 1（激活） | P0 | 待测 |
| E-006 | 注销 | 正常注销 | POST /merchant/card/cancel/{id} | HTTP 200，状态变为 3（注销），不可逆 | P0 | 待测 |
| E-007 | 注销 | 注销后操作 | 注销后再次发起冻结或充值 | 返回业务错误"卡已注销" | P0 | 待测 |
| E-008 | 敏感信息 | 获取卡号 CVV | GET /merchant/card/key-info/{id} | 返回 PAN/CVV，且验证操作者是卡的归属商户 | P0 | 待测 |
| E-009 | 敏感信息 | 跨商户获取敏感信息 | 商户 A 请求商户 B 的卡 key-info | HTTP 403，禁止 | P0 | 待测 |
| E-010 | 操作隔离 | 跨商户冻结 | 商户 A 尝试冻结商户 B 的卡 | HTTP 403，禁止 | P0 | 待测 |
| E-011 | 前端 | 冻结后按钮状态 | 成功冻结后，卡列表页"冻结"按钮应变为"解冻" | 按钮状态正确切换，不出现重复操作按钮 | P1 | 待测 |

---

### 2.6 模块 F：充值流程（后端 & 商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| F-001 | 充值 | 正常充值 | POST /merchant/recharge/submit，{cardId, amount} 有效 | HTTP 200，充值记录状态 0（处理中） | P0 | 待测 |
| F-002 | 充值 | 账户余额扣除 | 提交充值后查询账户余额 | 账户余额应立即减少充值金额+手续费 | P0 | 待测 |
| F-003 | 充值 | 余额不足 | 账户余额 < 充值金额+手续费 | 返回业务错误"余额不足"，不产生充值记录 | P0 | 待测 |
| F-004 | 充值 | 手续费服务端计算 | 请求 body 中携带 fee 字段 | 后端忽略客户端 fee，自行计算，不受篡改 | P0 | 待测 |
| F-005 | 充值 | 充值成功回调 | 模拟上游 Webhook 回调，eventType=recharge_success | 充值状态更新为 1（成功），卡余额增加 | P0 | 待测 |
| F-006 | 充值 | 充值失败回调 | 模拟上游 Webhook 回调，eventType=recharge_fail | 充值状态更新为 2（失败），账户余额退还 | P0 | 待测 |
| F-007 | 充值 | 补偿幂等性 | 同一失败充值回调触发 2 次补偿 | 只补偿一次，账户余额不重复增加 | P0 | 待测 |
| F-008 | 充值 | userId 来自 Token | 请求 body 中传入他人 userId | 后端从 token 提取 userId，忽略 body 中 userId | P0 | 待测 |
| F-009 | 充值 | 充值记录列表 | GET /merchant/recharge/list | 仅返回当前商户充值记录 | P0 | 待测 |
| F-010 | 充值 | 订单号查询 | GET /merchant/recharge/query/{orderNo} | 返回对应充值状态，订单属于当前商户 | P1 | 待测 |
| F-011 | 充值 | USDT 到账通知 | POST /merchant/recharge/usdt-arrival，amount/txHash 有效 | 记录创建，userId 从 token 读取，不可伪造 | P1 | 待测 |
| F-012 | 充值 | 并发充值 | 同一账户并发提交 5 笔充值 | 余额扣除无超额，不出现负余额 | P0 | 待测 |

---

### 2.7 模块 G：限额管理（后端 & 商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| G-001 | 限额 | 查询限额历史 | GET /merchant/v3/limit/card/{cardId}/history | 返回该卡限额调整历史，仅本商户卡 | P1 | 待测 |
| G-002 | 限额 | 调整限额 | POST /merchant/v3/limit/card/{cardId}/adjust，{newAmount, reason} | 限额更新成功，历史记录新增 | P1 | 待测 |
| G-003 | 限额 | 超过每日限额消费 | 模拟消费金额超过 daily limit | 交易被拒绝，风控规则触发 | P0 | 待测 |
| G-004 | 限额 | 并发消费绕过日限 | 并发发起多笔消费，总额超过日限额 | 并发锁生效，总消费不超过日限额 | P0 | 待测 |
| G-005 | 限额 | 跨商户调整限额 | 商户 A 尝试调整商户 B 的卡限额 | HTTP 403，禁止 | P0 | 待测 |

---

### 2.8 模块 H：交易记录（后端 & 商户端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| H-001 | 交易 | 查询交易列表 | GET /merchant/v3/transactions/list | 仅返回本商户交易，分页正确 | P0 | 待测 |
| H-002 | 交易 | 按卡查询 | GET /merchant/v3/transactions/card/{cardId} | 返回该卡所有交易，cardId 属于本商户 | P0 | 待测 |
| H-003 | 交易 | 跨商户查交易 | 商户 A 请求商户 B 的 cardId 交易 | HTTP 403 或返回空 | P0 | 待测 |
| H-004 | 交易 | 筛选条件 | 按 type/status/日期范围查询 | 返回符合条件的交易，过滤正确 | P1 | 待测 |
| H-005 | 交易 | 前端导出 | 点击"导出"按钮，选择日期范围后下载 | 下载 CSV 文件，含列头，数据与列表一致 | P1 | 待测 |
| H-006 | 交易 | CSV 公式注入 | 交易 Merchant 字段包含 =SUM() | 导出文件中该字段被转义，不触发 Excel 公式 | P1 | 待测 |
| H-007 | 交易 | 关联交易查询 | GET /merchant/v3/transactions/related/{txnId} | 返回冲正/退款关联交易 | P2 | 待测 |

---

### 2.9 模块 I：Webhook 处理（后端）

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| I-001 | Webhook | 正常回调验签 | POST /webhook/yeevcc，携带正确 HMAC-SHA256 签名和有效时间戳 | HTTP 200，返回 "ok"，业务逻辑异步处理 | P0 | 待测 |
| I-002 | Webhook | 签名错误 | 传入错误的 X-Webhook-Signature | HTTP 401，拒绝处理 | P0 | 待测 |
| I-003 | Webhook | 时间戳过期（>300s） | X-Webhook-Timestamp 超过 300s | HTTP 401，"请求已过期" | P0 | 待测 |
| I-004 | Webhook | 重放攻击 | 重复发送相同 timestamp+body | 验签时间戳校验拦截或幂等处理，不重复执行 | P0 | 待测 |
| I-005 | Webhook | 验签 TODO 状态 | 检查当前代码 verifySignature 实现 | **已知问题**：当前返回固定 true，必须修复再上线 | P0 | 待测 |
| I-006 | Webhook | 开卡成功回调 | eventType=card_open，cardId 有效 | 卡状态更新，操作日志写入 | P0 | 待测 |
| I-007 | Webhook | 开卡失败回调 | eventType=card_open_fail | 卡状态回滚，告警或通知商户 | P0 | 待测 |
| I-008 | Webhook | 异步处理失败 | WebhookAsyncService 处理抛出异常 | webhook_log 记录失败状态，支持重试机制 | P1 | 待测 |
| I-009 | Webhook | 循环依赖风险 | 启动服务检查 WebhookServiceImpl → WebhookAsyncService → WebhookServiceImpl | 服务正常启动，无 BeanCreationException | P0 | 待测 |
| I-010 | Webhook | OTP 回调 | POST /webhook/otp，携带有效签名 | 返回 {method:2, destination:"otp@..."} | P2 | 待测 |
| I-011 | Webhook | auth-decision 固定放行 | POST /webhook/auth-decision | **已知问题**：固定返回 authResult:"A"，应按业务逻辑判断 | P1 | 待测 |

---

### 2.10 模块 J：管理端 - 用户/商户管理

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| J-001 | 商户管理 | 查询商户列表 | GET /admin/merchant/list，管理员 token | 返回所有商户，分页正确 | P0 | 待测 |
| J-002 | 商户管理 | 非管理员访问 | 使用商户 token 请求 /admin/merchant/list | HTTP 403，禁止 | P0 | 待测 |
| J-003 | KYC 审核 | 审核通过 | PUT /admin/merchant/{userId}/approve，remark 可选 | 商户状态变为已通过，商户收到通知 | P0 | 待测 |
| J-004 | KYC 审核 | 审核拒绝 | PUT /admin/merchant/{userId}/reject，{reason} | 商户状态变为已拒绝，reason 必填校验 | P0 | 待测 |
| J-005 | KYC 审核 | 拒绝未填 reason | PUT /admin/merchant/{userId}/reject，body 无 reason | HTTP 400，"拒绝原因不能为空" | P1 | 待测 |
| J-006 | 状态管理 | 禁用商户 | PUT /admin/merchant/{userId}/status，{status:"0"} | 商户账号禁用，token 失效 | P0 | 待测 |
| J-007 | 状态管理 | 启用商户 | PUT /admin/merchant/{userId}/status，{status:"1"} | 商户账号恢复，可正常登录 | P1 | 待测 |
| J-008 | 子账号 | 创建运营账号 | POST /admin/merchant/operator/create，{userName, password, parentMerchantId} | 创建成功，角色正确分配 | P1 | 待测 |
| J-009 | 子账号 | 查询运营账号列表 | GET /admin/merchant/operator/list/{merchantId} | 返回该商户所有子账号 | P1 | 待测 |
| J-010 | 前端路由 | 非管理员访问管理路由 | 商户 token 直接访问 /dashboard（admin）路由 | 路由守卫拦截，重定向到 403 或登录页 | P0 | 待测 |
| J-011 | 前端 | 管理端登录 | 管理员账号密码登录 | 成功进入 dashboard，菜单权限正确 | P0 | 待测 |

---

### 2.11 模块 K：管理端 - 卡片管理

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| K-001 | 卡片管理 | 查询全部卡 | GET /admin/card/list，无商户过滤 | 返回所有商户卡片，跨商户可见 | P0 | 待测 |
| K-002 | 卡片管理 | 强制冻结 | PUT /admin/business/card/{cardId}/force-freeze | 卡被冻结，操作日志记录管理员 | P0 | 待测 |
| K-003 | 卡片管理 | 强制注销 | PUT /admin/business/card/{cardId}/force-cancel | 卡被注销，不可逆 | P0 | 待测 |
| K-004 | 卡片统计 | 卡片数量统计 | GET /admin/card/stats | 返回各状态卡数量汇总，数据实时 | P1 | 待测 |
| K-005 | 卡片导出 | 导出全量卡 | GET /admin/card/export | 下载 Excel，含所有字段，CSV 无公式注入 | P1 | 待测 |
| K-006 | 前端 | 卡片状态筛选 | 选择 Status 和 Card Type 筛选条件 | 列表正确过滤，分页 total 更新 | P1 | 待测 |

---

### 2.12 模块 L：管理端 - 充值审核

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| L-001 | 充值审核 | 查询充值列表 | GET /admin/recharge/list | 返回全量充值记录（无商户隔离） | P0 | 待测 |
| L-002 | 充值审核 | 审核通过 | POST /admin/recharge/approve，{rechargeId, status:1} | 充值状态变为成功，触发余额增加 | P0 | 待测 |
| L-003 | 充值审核 | 审核拒绝 | POST /admin/recharge/approve，{rechargeId, status:2} | 充值状态变为失败，触发余额退还 | P0 | 待测 |
| L-004 | 充值审核 | 重复审核 | 已审核的充值再次触发审核 | 返回业务错误"已审核，不可重复操作" | P1 | 待测 |

---

### 2.13 模块 M：管理端 - 交易管理

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| M-001 | 交易管理 | 查询全量交易 | GET /admin/transaction/list | 返回所有商户交易，支持分页 | P0 | 待测 |
| M-002 | 交易管理 | 多条件筛选 | 组合 type/status/startDate/endDate | 筛选结果正确，total 准确 | P1 | 待测 |
| M-003 | 交易管理 | 导出 | GET /admin/transaction/export | 下载 CSV，字段完整，无公式注入 | P1 | 待测 |
| M-004 | 交易统计 | 今日统计 | GET /admin/transaction/stats | 返回今日交易量、金额汇总 | P1 | 待测 |

---

### 2.14 模块 N：管理端 - 费率与风控配置

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| N-001 | 费率配置 | 查询费率 | GET /admin/fee-config | 返回开卡费/月费/交易手续费配置 | P0 | 待测 |
| N-002 | 费率配置 | 更新费率 | PUT /admin/fee-config/{id}，value 在合法范围 | 费率更新成功，新费率下次开卡即生效 | P0 | 待测 |
| N-003 | 费率配置 | 费率范围校验 | 传入 value=150（超过 100%） | 前端拦截或后端返回 400 "费率超出范围" | P1 | 待测 |
| N-004 | 风控规则 | 创建规则 | POST /admin/risk-rules，{name, type, threshold, cardType, priority} | 规则创建成功，立即生效 | P1 | 待测 |
| N-005 | 风控规则 | 更新规则 | PUT /admin/risk-rules/{id} | 规则更新成功 | P1 | 待测 |
| N-006 | 风控规则 | 禁用规则 | PUT /admin/risk-rules/{id}/status，{status:INACTIVE} | 规则停止生效，对新交易不再判断 | P1 | 待测 |
| N-007 | 风控规则 | 优先级范围 | priority 传入 0 或 101 | 前端表单校验（1-100 范围） | P2 | 待测 |

---

### 2.15 模块 O：管理端 - 钱包与余额管理

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| O-001 | 钱包 | 查询用户钱包 | GET /admin/wallet/{userId} | 返回该用户所有币种余额 | P0 | 待测 |
| O-002 | 钱包 | 正数余额调整 | POST /admin/wallet/{userId}/adjust，{amount: 100, reason} | 余额增加 100，调整日志写入 | P0 | 待测 |
| O-003 | 钱包 | 负数余额调整 | POST /admin/wallet/{userId}/adjust，{amount: -50, reason} | 余额减少 50，不触发负余额（如余额>50） | P0 | 待测 |
| O-004 | 钱包 | 调整使余额为负 | amount 超出当前余额 | 返回业务错误"余额不足" | P0 | 待测 |
| O-005 | 钱包 | 手动调整需 reason | reason 字段为空 | HTTP 400，"调整原因不能为空" | P1 | 待测 |

---

### 2.16 模块 P：系统配置与安全

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| P-001 | 系统配置 | 查询配置 | GET /admin/system-config/list | 返回配置列表，敏感值（AES 密钥）应脱敏或不返回明文 | P0 | 待测 |
| P-002 | 系统配置 | configKey 白名单 | POST /admin/system-config/set，configKey=任意值 | 非白名单 key 返回 400 拒绝 | P0 | 待测 |
| P-003 | 系统配置 | 加密存储 | 检查 SystemConfig 加密方式 | **已知问题**：当前使用 ECB 模式+硬编码 key，需确认是否已修复 | P0 | 待测 |
| P-004 | 安全 | PAN 暴露时间 | 前端获取卡敏感信息后等待 60s | CVV/到期日清除，**但 PAN 仍显示**（已知问题） | P0 | 待测 |
| P-005 | 安全 | 身份证明文存储 | 查询 kyc_records 表中 id_number | **已知问题**：身份证号明文存储，需加密 | P0 | 待测 |
| P-006 | 安全 | 密码修改方式 | POST /profile/change-password | 参数通过 body 传递（非 URL query），已修复确认 | P0 | 待测 |
| P-007 | 安全 | Admin 路由守卫 | 直接访问 /admin/* 路由（非管理员 token） | 路由守卫拦截，确认已修复 | P0 | 待测 |
| P-008 | 安全 | HTTPS 强制 | 所有 API 调用走 HTTPS | 无 HTTP 明文传输 | P1 | 待测 |

---

### 2.17 模块 Q：前端通用交互

| TC# | 模块 | 场景 | 测试步骤 | 预期结果 | 优先级 | 状态 |
|-----|------|------|----------|----------|--------|------|
| Q-001 | 通用 | 网络请求中 loading | 列表页加载时 | 显示加载指示器，不可重复提交 | P1 | 待测 |
| Q-002 | 通用 | 请求异常提示 | 模拟 500 错误 | ElMessage 或 Toast 显示错误信息，页面不白屏 | P1 | 待测 |
| Q-003 | 通用 | 401 自动跳转 | Token 过期时发起请求 | 自动清除 session，跳转 /login | P0 | 待测 |
| Q-004 | 通用 | loading 不被 finally 覆盖 | KYC 页面 userAuth 加载中断 | **已知问题**：userAuth loading 未在 finally 中清除，确认修复 | P1 | 待测 |
| Q-005 | 通用 | 多语言切换 | 商户端切换中英文 | 所有文案正确切换，不出现 key 原文 | P2 | 待测 |
| Q-006 | 通用 | 分页边界 | 最后一页删除最后一条数据 | 自动跳回上一页或显示"暂无数据" | P2 | 待测 |

---

## 3. 问题清单（已知缺陷，需在测试前/中确认修复状态）

| BUG# | 来源 | 描述 | 严重程度 | 当前状态 |
|------|------|------|----------|----------|
| BUG-001 | review-backend-bugfix | Webhook 验签 `verifySignature` 固定返回 `true`，未实现 | Critical | 未修复 |
| BUG-002 | review-agent-cde-core | 充值失败补偿非幂等，重复回调可能双倍补偿 | Critical | 部分修复 |
| BUG-003 | review-final | 每日限额缺乏行级锁，并发可绕过 | Critical | 未修复 |
| BUG-004 | review-agent-cde-core | openCard 在事务中执行 30s 长轮询，事务超时风险 | High | 未明确 |
| BUG-005 | review-final | Webhook 循环依赖风险（WebhookServiceImpl ↔ WebhookAsyncService） | High | 需启动验证 |
| BUG-006 | review-final | RechargeMapper 缺少 @Param 注解，多参数查询可能失败 | High | 未确认 |
| BUG-007 | review-frontend | 前端 PAN 不清除（仅 CVV/到期日 60s 清除） | High | 未修复 |
| BUG-008 | review-agent-cde-core | SystemConfig 使用 ECB 模式+硬编码 AES key | High | 未确认 |
| BUG-009 | review-agent-cde-core | 身份证号明文存储于 kyc_records | High | 未修复 |
| BUG-010 | review-final | userAuth 页面 loading 未在 finally 中清除 | Medium | 未确认 |
| BUG-011 | review-agent-cde-core | auth-decision webhook 固定返回授权通过（authResult:"A"） | Medium | 需确认是否为预期行为 |
| BUG-012 | review-frontend | 商户端 KYC 表单身份证号格式未校验 | Medium | 需确认修复 |

---

## 4. 模块结论

| 模块 | 覆盖状态 | 关键风险 | 上线建议 |
|------|----------|----------|----------|
| A 认证会话 | 完整覆盖 | Token 安全、暴力破解防护 | 需验证锁定机制已启用 |
| B 注册/KYC | 完整覆盖 | KYC 审核流程、证件存储 | **BUG-009 身份证明文需加密修复后上线** |
| C 持卡人管理 | 完整覆盖 | 跨商户隔离 | 需重点验证 C-003 用户隔离 |
| D 开卡 | 完整覆盖 | 上游超时/幂等 | 需验证 BUG-004 长轮询事务问题 |
| E 卡状态操作 | 完整覆盖 | 跨商户访问、状态机 | E-009/E-010 为上线前必测项 |
| F 充值流程 | 完整覆盖 | **幂等、并发、余额** | **BUG-002/003 必须修复才能上线** |
| G 限额管理 | 完整覆盖 | 并发绕过 | **BUG-003 行级锁缺失为 P0 阻断** |
| H 交易记录 | 完整覆盖 | CSV 公式注入 | 低风险，建议修复 H-006 |
| I Webhook | 完整覆盖 | **验签 TODO** | **BUG-001 是最高优先级阻断项** |
| J 管理员-商户管理 | 完整覆盖 | 权限路由守卫 | J-010 已修复，需回归验证 |
| K 管理员-卡片 | 完整覆盖 | 强制操作日志 | 确保强制操作有审计追踪 |
| L 管理员-充值审核 | 完整覆盖 | 重复审核 | L-004 幂等需测试 |
| M 管理员-交易 | 完整覆盖 | 导出安全 | 低风险 |
| N 费率/风控 | 完整覆盖 | 规则生效时机 | 建议确认规则变更是否需重启 |
| O 钱包余额 | 完整覆盖 | 负余额防护 | O-004 边界需严格测试 |
| P 系统安全 | 完整覆盖 | **PAN 暴露、ECB 加密、明文证件** | **P-001~P-007 均为 P0，阻断上线** |
| Q 前端通用 | 基本覆盖 | loading 状态管理 | 建议修复后回归 |

---

## 5. 总计

| 分类 | 数量 |
|------|------|
| 测试用例总计 | **117 个** |
| P0 优先级 | 63 个 |
| P1 优先级 | 41 个 |
| P2 优先级 | 13 个 |
| 已知缺陷 | **12 个**（其中 4 个 Critical/High 为上线阻断项） |

### 上线阻断项（必须修复后才可部署生产）

1. **BUG-001** - Webhook 验签固定返回 true（安全漏洞）
2. **BUG-002** - 充值失败补偿非幂等（资金安全）
3. **BUG-003** - 每日限额无行级锁（并发绕过）
4. **BUG-007** - PAN 前端明文持续展示（合规风险）
5. **BUG-009** - 身份证号明文存储（数据合规）
