# YeeVcc 测试环境接口文档（官方版）

> 来源：张剑仪，2026-03-20

测试环境基础地址：`http://43.153.105.113:9991/demo`  
customerId：`12087683`  
（测试模式无需签名，demo服务端直接转发给VCC）

响应格式统一：`{ "status": 200, "message": "...", "data": {...} }`  
status=200 表示成功

---

## 一、用户 / 持卡人

### 1. 获取用户信息
`GET /demo/user/userInfo`  
参数：无  
作用：获取商户账户基本信息

### 2. 获取持卡人列表
`GET /demo/user/cardholders`  
参数：无

### 3. 创建持卡人
`POST /demo/user/addCardholder`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerId | Long | ✅ | 商户ID（12087683）|
| firstName | String | ✅ | 名 |
| lastName | String | ✅ | 姓 |
| email | String | ✅ | 邮箱 |
| country | String | ✅ | 国家代码（如 "US"）|
| city | String | | 城市 |
| address | String | | 地址 |
| postCode | String | | 邮编 |
| mobilePrefix | String | | 电话国家前缀（如 "+1"）|
| phone | String | | 电话号码 |
| birthday | String | | 出生日期（yyyy-MM-dd）|

返回：`data.id` = 持卡人ID

---

## 二、账户（预算）

### 4. 创建账户
`POST /demo/account/create`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerId | Long | ✅ | 商户ID |
| currency | String | ✅ | 币种（如 "USD"）|
| remark | String | | 备注（预算名称）|

返回：`data.accountNo` = 账户号

### 5. 账户转入
`POST /demo/account/transferIn`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountNo | String | ✅ | 账户号 |
| transferInAmount | BigDecimal | ✅ | 转入金额 |
| deductCurrency | String | ✅ | 扣款币种（USD 或 USDT）|
| orderId | String | | 幂等唯一订单号 |

### 6. 账户转出
`POST /demo/account/transferOut`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountNo | String | ✅ | 账户号 |
| transferOutAmount | BigDecimal | ✅ | 转出金额 |
| depositCurrency | String | ✅ | 币种（测试用此字段）|
| deductCurrency | String | ✅ | 币种（生产用此字段，测试两个都传）|
| orderId | String | | 幂等唯一订单号 |

### 7. 查询账户信息
`GET /demo/account/list`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerId | Long | ✅ | 商户ID |
| accountNo | String | ✅ | 账户号 |
| current | int | ✅ | 页码（传 1）|
| size | int | ✅ | 每页条数（传 1）|

返回：`data.records[0].balance` = 账户实时余额

---

## 三、卡片

### 8. 获取卡BIN列表
`GET /demo/card/cardBins`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerId | Long | ✅ | 商户ID |

### 9. 开卡
`POST /demo/card/open`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardBinId | String | ✅ | 卡BIN ID（从 getCardBins 获取）|
| cardholderId | Long | ✅ | 持卡人ID |
| currency | String | ✅ | 扣费钱包币种（USD 或 USDT）|
| cardType | int | ✅ | 1=共享卡（预算卡）2=常规卡（普通卡）|
| amount | BigDecimal | | 开卡首次转入金额（共享卡传null）|
| cardAmount | BigDecimal | | 卡初始额度 |
| sharedAccountNo | String | | 共享账户号（cardType=1时必填）|

返回：`data.taskId` = 开卡任务ID（异步）

> ⚠️ 开卡是异步的，需轮询 queryOpenCardTask 获取结果

### 10. 查询开卡任务状态
`GET /demo/card/queryTask`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| taskId | Long | ✅ | 开卡任务ID |

返回：`data.cardId` = 卡片ID（K-xxx格式）

### 11. 查询卡片基本信息
`GET /demo/card/cardInfo`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |

> ⚠️ cardNumber 为脱敏格式，不可用作入库卡号

### 12. 查询卡片三要素（敏感信息）
`GET /demo/card/sensitiveInfo`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |

返回：
- `cardNumber` — 明文完整卡号（16位）
- `cvv` — 测试模式已解密明文，生产模式为AES加密
- `expiryDate` — 测试模式已解密明文，生产模式为AES加密

### 13. 查询卡片余额
`POST /demo/card/balance`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |

### 14. 激活卡片
`POST /demo/card/activate`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |

### 15. 卡片充值（账户→卡）
`POST /demo/card/recharge`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |
| amount | BigDecimal | ✅ | 充值金额 |
| currency | String | ✅ | 币种（如 "USD"）|
| deductCurrency | String | ✅ | 扣款币种（同 currency）|

### 16. 卡片转出（卡→账户）
`POST /demo/card/chargeOut`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |
| amount | BigDecimal | ✅ | 转出金额 |
| currency | String | ✅ | 币种（如 "USD"）|

### 17. 冻结卡片
`POST /demo/card/freeze`

### 18. 解冻卡片
`POST /demo/card/unfreeze`

### 19. 注销卡片
`POST /demo/card/cancel`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |
| refundCurrency | String | | 余额退回币种（如 "USD"）|

> 触发 card_cancel_out + card_cancel 两个回调

### 20. 修改卡片备注
`POST /demo/card/modifyRemark`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |
| remark | String | ✅ | 备注内容 |

### 21. 设置卡片限额
`POST /demo/card/limitConfig`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | ✅ | 卡片ID |
| limitConfig.cardLifeCycleLimit | BigDecimal | ✅ | 9999999999999=不限额 |

### 22. 查询交易列表
`GET /demo/card/authList`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| cardId | String | | 卡片ID（不传查全部）|
| startDate | String | | 开始日期（yyyy-MM-dd）|
| endDate | String | | 结束日期 |
| current | long | ✅ | 页码（从1开始）|
| size | long | ✅ | 每页条数 |

---

## 四、开卡完整流程（共享卡/预算卡）

```
Step 1: createAccount("USD", "预算名称")
        → 得到 accountNo，存 zjy_budget.budget_id

Step 2: accountTransferIn(accountNo, amount, "USD", orderId)
        → 给账户充值

Step 3: createCardholder(firstName, lastName, email, "US", ...)
        → 得到 cardholderId（data.id），存 zjy_cardholder.cardholder_id

Step 4: openCard(cardBinId, cardholderId, "USD", null, null, 1, accountNo)
        → cardType=1（共享卡），传 sharedAccountNo
        → 得到 taskId

Step 5: queryOpenCardTask(taskId) 轮询
        → 成功后得到 cardId（K-xxx格式）

Step 6: getCardSensitiveInfo(cardId)
        → 得到 cardNumber（明文）、cvv、expiryDate
        → 加密后存 zjy_budget_card
```

---

## 五、回调地址（Webhook）

| 类型 | 地址 |
|------|------|
| 交易通知 | `http://43.153.105.113:8888/callback/huidiao/yeevcc/authTransaction` |
| 卡操作通知 | `http://43.153.105.113:8888/callback/huidiao/yeevcc/cardOperate` |
| OTP通知 | `http://43.153.105.113:8888/callback/huidiao/yeevcc/otp` |

主要回调事件（operateType）：

| 事件 | 说明 |
|------|------|
| card_open | 开卡成功 |
| card_cancel_out | 销卡余额转出（含 amount 字段）|
| card_cancel | 销卡完成（cardStatus=CANCELED）|
| card_freeze | 冻结 |
| card_in | 账户转入 |
| card_out | 账户转出 |
