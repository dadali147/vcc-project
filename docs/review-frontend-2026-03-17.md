# Frontend Code Review Report

## 审查摘要

审查范围基于 `main..feature/admin-panel` 的新增前端代码，重点覆盖：

- `vcc-web/src/views/merchant/*`
- `vcc-web/src/views/admin/*`
- `vcc-web/src/api/admin/*`
- 与商户端直接对应的新增 API：`vcc-web/src/api/card.js`、`vcc-web/src/api/cardHolder.js`、`vcc-web/src/api/recharge.js`、`vcc-web/src/api/transaction.js`、`vcc-web/src/api/user.js`
- 路由与权限上下文：`vcc-web/src/router/index.js`、`vcc-web/src/permission.js`、`vcc-web/src/store/modules/permission.js`

结论：

- 存在 3 个高优先级问题：前端路由权限缺失、敏感数据保护不足、密码修改接口把密码放进 URL 查询串。
- 存在多处中优先级问题：列表页/首页的加载态异常时不会收敛，导出未携带日期筛选，若干审核/资料表单校验不完整，`merchant/recharge` 误引入未声明依赖。
- API 调用总体统一走了 `@/utils/request`，未发现绕过 axios 封装的直接请求。
- 本轮在审查范围内未发现 `v-html` / `innerHTML` 使用，XSS 风险点相对可控。
- 本地未安装 `vcc-web/node_modules`，因此未执行构建验证；以下结论基于代码静态审查。

## 各页面审查结果

### 商户端

#### `vcc-web/src/views/merchant/home/index.vue`

- `153-164`：`loadData()` 并发发起 3 个请求，但 `loading` 只在 `getRecentTransactions()` 的成功分支里关闭。任一请求异常时没有 `catch/finally` 收敛，最近交易表格可能长期处于 loading。
- `144`：首页统计、最近交易、公告都从 `@/api/transaction` 引用，模块职责偏混杂，后续继续扩展时可维护性一般。

#### `vcc-web/src/views/merchant/wallet/index.vue`

- `112-118`：列表加载只在 `then` 中 `loading = false`，接口报错后分页区域可以继续操作，但表格会一直转圈。
- `106-109`：钱包概览无独立异常兜底，失败时界面直接静默为空。

#### `vcc-web/src/views/merchant/card/index.vue`

- `33`、`106-113`、`237-243`：卡号完整明文展示；CVV/有效期点击即可拉取并常驻在页面状态中，缺少二次认证、角色校验、自动隐藏/超时清理或审计提示。该页前端侧对敏感卡要素保护不足。
- `168-174`：卡片列表 loading 无异常收敛。
- `159-164`：开卡表单只有必填校验，没有金额边界/业务规则校验（例如额度上限、非法字符规则）。

#### `vcc-web/src/views/merchant/cardHolder/index.vue`

- `34`：持卡人证件号在列表页直接明文展示，缺少脱敏。
- `131-135`：表单只校验必填，没有手机号格式、邮箱格式、证件号格式校验，身份资料录入校验不完整。
- `140-146`：列表 loading 无异常收敛。

#### `vcc-web/src/views/merchant/recharge/index.vue`

- `93`：引入了 `vue-clipboard3`，但项目 `vcc-web/package.json` 中没有该依赖，且代码实际未使用该 import；在干净环境安装/构建时这是潜在阻断项。
- `118-125`：复制逻辑实际使用 `navigator.clipboard`，与上面的外部依赖引入重复。
- `127-133`：充值记录 loading 无异常收敛。

#### `vcc-web/src/views/merchant/transaction/index.vue`

- `115-121`：交易列表 loading 无异常收敛。
- `142-144`：导出直接传 `{ ...queryParams.value }`，没有把 `dateRange` 通过 `addDateRange()` 带上。用户按时间筛选后，导出结果会与当前列表不一致。
- `93`：导入了 `exportTransaction`，但页面实际调用的是 `proxy.download`，存在未使用导入和重复 API 封装。

#### `vcc-web/src/views/merchant/user/index.vue`

- `vcc-web/src/api/user.js:21-26`：密码修改接口通过 `params` 发送 `oldPassword/newPassword`，会进入 URL 查询串、浏览器历史、代理日志或网关日志，安全性明显弱于 body 传输。对比 `vcc-web/src/api/system/user.js:91-100`，同类接口已有正确实现。
- `167-170`：KYC 表单只校验必填，没有证件号格式规则。
- `97-111`、`232-238`：2FA 绑定前端只校验验证码非空，没有保证 `tfaSecret` 已成功生成，也没有验证码格式校验。
- `148-165`：资料/密码校验规则弱于系统侧个人中心实现，缺少手机号规则和新密码非法字符规则，存在一致性问题。

### 管理端

#### `vcc-web/src/views/admin/stats/index.vue`

- `114-136`：统计、BIN、渠道、趋势 4 个请求全部没有异常兜底，任何一个失败都会让对应模块静默留空。
- `139-179`：图表区域没有 loading / empty 状态，数据量为空时用户缺少反馈。

#### `vcc-web/src/views/admin/cardBin/index.vue`

- `185-191`：列表 loading 无异常收敛。
- `174-180`：新增/编辑 BIN 的规则只覆盖部分字段，`monthlyFee`、`rechargeRate`、`channel`、`binNo` 格式长度等没有明确约束，表单校验不完整。

#### `vcc-web/src/views/admin/card/index.vue`

- `37`、`78`：卡号在管理端列表和详情页完整明文展示；若管理端页面路由继续保持无角色限制，敏感信息暴露范围会进一步扩大。
- `124-130`：列表 loading 无异常收敛。
- `99`：导入了 `exportCard`，但页面实际走 `proxy.download`，API 封装重复且未使用。

#### `vcc-web/src/views/admin/recharge/index.vue`

- `76-89`、`196-201`：审核表单没有 `rules`，`submitAudit()` 也未触发表单校验。通过时可以提交空到账金额，拒绝时也可以不填原因，审核数据质量无法保证。
- `167-173`：列表 loading 无异常收敛。
- `224-226`：导出未带 `dateRange`，与列表筛选结果不一致。

#### `vcc-web/src/views/admin/transaction/index.vue`

- `166-172`：列表 loading 无异常收敛。
- `193-195`：导出未带 `dateRange`，与页面筛选不一致。

#### `vcc-web/src/views/admin/user/index.vue`

- `68-72`：KYC 审核弹窗直接展示完整证件号码，缺少脱敏。
- `74-88`、`178-183`：KYC 审核表单没有校验规则，拒绝时可不填写审核备注；费率配置弹窗也没有表单校验。
- `149-155`：列表 loading 无异常收敛。

#### `vcc-web/src/views/admin/wallet/index.vue`

- `148-154`、`196-202`：钱包列表和动账明细两个 loading 都没有异常收敛。
- `132-137`：余额调整只校验必填，没有更细粒度规则（金额上限、备注长度等）。

### 路由与 API 层补充

- `vcc-web/src/router/index.js:87-190`：商户端和管理端路由被放在 `constantRoutes` 中，且 `meta` 没有 `roles` / `permissions`。
- `vcc-web/src/permission.js:20-54`：导航守卫只校验是否登录，不校验角色。
- `vcc-web/src/store/modules/permission.js:35-52`：后端 `getRouters()` 仅用于生成额外路由；`constantRoutes` 会直接进入 sidebar/default routes。
- 以上组合意味着：前端侧任意已登录用户都能看到并直接访问 `/admin/*` 路由，管理端没有前端角色隔离。
- `vcc-web/src/api/admin/*.js` 与商户侧新增 API 均统一复用 `@/utils/request`，这一点本身是规范的；当前主要问题在于调用方未正确处理异常/导出参数，以及个别接口传参方式不安全。

## 需要修复的问题清单

1. 高优先级：为 `/admin/*` 路由补充角色/权限校验，不要把管理端菜单与页面放在 `constantRoutes` 里裸暴露。涉及 `vcc-web/src/router/index.js:87-190`、`vcc-web/src/permission.js:20-54`、`vcc-web/src/store/modules/permission.js:35-52`。
2. 高优先级：收紧敏感数据展示，至少对 merchant 卡片页的 PAN/CVV/有效期增加二次验证、脱敏和自动清理；管理端 KYC 证件号也应做脱敏。涉及 `vcc-web/src/views/merchant/card/index.vue:33`、`vcc-web/src/views/merchant/card/index.vue:106-113`、`vcc-web/src/views/merchant/card/index.vue:237-243`、`vcc-web/src/views/admin/user/index.vue:68-72`。
3. 高优先级：把商户端密码修改接口从 URL 查询参数改为 request body，和系统侧实现保持一致。涉及 `vcc-web/src/api/user.js:21-26`，参考 `vcc-web/src/api/system/user.js:91-100`。
4. 中优先级：所有列表页/首页/明细弹窗统一改成 `try/catch/finally` 或 `promise.finally()` 收敛 loading，避免接口异常后界面一直转圈。典型位置：`vcc-web/src/views/merchant/home/index.vue:153-164`、`vcc-web/src/views/merchant/wallet/index.vue:112-118`、`vcc-web/src/views/merchant/card/index.vue:168-174`、`vcc-web/src/views/merchant/cardHolder/index.vue:140-146`、`vcc-web/src/views/merchant/recharge/index.vue:127-133`、`vcc-web/src/views/merchant/transaction/index.vue:115-121`、`vcc-web/src/views/admin/card/index.vue:124-130`、`vcc-web/src/views/admin/cardBin/index.vue:185-191`、`vcc-web/src/views/admin/recharge/index.vue:167-173`、`vcc-web/src/views/admin/transaction/index.vue:166-172`、`vcc-web/src/views/admin/user/index.vue:149-155`、`vcc-web/src/views/admin/wallet/index.vue:148-154`、`vcc-web/src/views/admin/wallet/index.vue:196-202`。
5. 中优先级：修复导出接口参数，与列表查询保持一致，尤其是 `dateRange`。涉及 `vcc-web/src/views/merchant/transaction/index.vue:142-144`、`vcc-web/src/views/admin/recharge/index.vue:224-226`、`vcc-web/src/views/admin/transaction/index.vue:193-195`。
6. 中优先级：补齐审核/资料类表单校验，至少覆盖到账金额、拒绝原因、手机号/邮箱/证件号格式、2FA 验证码格式等。涉及 `vcc-web/src/views/admin/recharge/index.vue:76-89`、`vcc-web/src/views/admin/user/index.vue:74-88`、`vcc-web/src/views/merchant/cardHolder/index.vue:131-135`、`vcc-web/src/views/merchant/user/index.vue:148-170`、`vcc-web/src/views/merchant/user/index.vue:232-238`。
7. 中优先级：删除 `merchant/recharge` 中未使用且未声明的 `vue-clipboard3` 引入，避免后续构建/安装问题。涉及 `vcc-web/src/views/merchant/recharge/index.vue:93`、`vcc-web/package.json:18-47`。
8. 低优先级：清理未使用的导出 API 封装与重复调用方式，避免后续维护出现“双入口”。涉及 `vcc-web/src/views/merchant/transaction/index.vue:93`、`vcc-web/src/views/admin/card/index.vue:99`、`vcc-web/src/api/transaction.js:21-28`、`vcc-web/src/api/admin/card.js:45-52`、`vcc-web/src/api/admin/recharge.js:31-38`、`vcc-web/src/api/admin/transaction.js:29-36`。

