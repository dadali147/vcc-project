# YeeVcc API 对接说明（2026-03-20）

来源：张剑仪提供

## 一、依赖

Maven 坐标（需本地安装 jar）：
- groupId: global.kun.shade
- artifactId: yop-java-sdk-web3
- version: 4.4.10
- jar 位置：java-web-demo/facade/lib/yop-java-sdk-4.4.10-web3.jar

## 二、环境配置

### 测试环境
- **模式：HTTP直调 demo 服务器（无需签名）**
- Demo地址：http://43.153.105.113:9991/demo
- appKey：app_12087683
- customerId：12087683
- AES密钥：K8fT3pW9xQ2mZrL6
- 私钥：见 kun-vcc-test-key.txt

### 生产环境
- 模式：YOP SDK 签名直连 VCC
- 生产地址：https://api.kun.global/yop-center
- appKey：app_10086701
- customerId：10086701
- AES密钥：6PPaVsVVfGqrCVXV
- 私钥：见 PROD_PRIV_KEY（待提供）

环境切换：EnvConfig.IS_PRODUCTION
- false = 测试模式（走demo，无需签名）
- true  = 生产模式（走YOP SDK签名）

## 三、签名机制（生产模式）

使用 YOP SDK 自动签名：
1. 解析 RSA2048 私钥（PKCS8 Base64格式）
2. 构造 YopRequest，设置 serverRoot + YopPKICredentials
3. GET请求用 addParameter，POST请求用 setContent(JSON字符串)
4. 调用 yopClient.request(request)，SDK内部完成签名

注意：YOP SDK SPI 懒加载，必须在 Spring 主线程 ClassLoader 下执行
- 解决方案：@PostConstruct 保存 springClassLoader，每次请求前切换，请求后恢复

响应格式：{ "status": 200, "message": "...", "data": {...} }
status=200 表示成功

## 四、AES解密（敏感字段 cvv/expiryDate）

算法：AES/CBC/PKCS5Padding
- key = AES_KEY（16字节 UTF-8）
- IV  = key 前16字节
- 密文为 Base64（MimeDecoder）编码

**测试模式：demo 服务端已解密，字段直接返回明文，无需再解密**
生产模式：返回 AES 加密字段，需调用 decryptSensitiveField() 解密

## 五、业务接口列表

### 用户/持卡人

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| getUserInfo() | GET | /rest/v1.0/vcc/user-info | 获取商户信息 |
| getCardholders() | GET | /rest/v1.0/vcc/card-holders | 持卡人列表 |
| createCardholder(...) | POST | /rest/v1.0/vcc/add-card-holder | 创建持卡人，返回 data.id |

createCardholder 参数：customerId, firstName, lastName, email, country, [city, address, postCode, mobilePrefix, phone, birthday]
返回：data.id = 持卡人ID（Long字符串，存 cardholder_id）

### 账户（预算）

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| createAccount(currency, remark) | POST | /rest/v1.0/vcc/user-account/create | 返回 data.accountNo |
| accountTransferIn(accountNo, amount, deductCurrency, orderId) | POST | /rest/v1.0/vcc/user-account/transfer-in | 给账户充值 |
| accountTransferOut(accountNo, amount, currency, orderId) | POST | /rest/v1.0/vcc/user-account/transfer-out | 从账户提取 |
| getAccountInfo(accountNo) | GET | /rest/v1.0/vcc/user-account/list | params: customerId, accountNo, current=1, size=1 |

### 卡片

| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| getCardBins() | GET | /rest/v1.0/vcc/card-bins | 获取可用卡BIN列表 |
| openCard(...) | POST | /rest/v1.0/vcc/card-open | 异步开卡，返回 data.taskId |
| queryOpenCardTask(taskId) | GET | /rest/v1.0/vcc/card-open-task | 轮询开卡任务 |
| getCardInfo(cardId) | GET | /rest/v1.0/vcc/card-info | |
| getCardSensitiveInfo(cardId) | GET | /rest/v1.0/vcc/card-key-info | cvv/expiryDate（生产环境AES加密） |
| getCardBalance(cardId) | POST | /rest/v1.0/vcc/card-balance | |
| cardActivate(cardId) | POST | /rest/v1.0/vcc/card-activate | |
| cardRecharge(cardId, amount, currency) | POST | /rest/v1.0/vcc/card-recharge | 从账户扣款给卡充值 |
| cardChargeOut(cardId, amount, currency) | POST | /rest/v1.0/vcc/card-charge-out | 从卡取回到账户 |
| cardFreeze(cardId) | POST | /rest/v1.0/vcc/card-freeze | |
| cardUnfreeze(cardId) | POST | /rest/v1.0/vcc/card-unfreeze | |
| cardCancel(cardId, refundCurrency) | POST | /rest/v1.0/vcc/card-cancel | 注销卡，余额退回账户 |
| cardRemark(cardId, remark) | POST | /rest/v1.0/vcc/card-remark | |
| setCardLimit(cardId, amount) | POST | /rest/v1.0/vcc/card-limit | amount=0 传 9999999999999 |
| queryTransactions(...) | GET | /rest/v1.0/vcc/transactions | |

openCard 参数：
- cardBinId, cardholderId, currency, cardType
- [amount, cardAmount, sharedAccountNo]
- **cardType: 1=共享卡（预算卡，需传sharedAccountNo）  2=常规卡（普通卡）**

## 六、回调（Webhook）

回调地址在 YeeVcc 商户后台 License 页面配置：
- 交易通知：http://域名/callback/huidiao/yeevcc/authTransaction
- 卡操作通知：http://域名/callback/huidiao/yeevcc/cardOperate
- OTP通知：http://域名/callback/huidiao/yeevcc/otp

cardOperate 事件类型（operateType）：
- card_open：开卡成功
- card_cancel_out：销卡余额转出（有amount字段）
- card_cancel：销卡完成（cardStatus=CANCELED）
- card_freeze：冻结
- card_in：账户转入
- card_out：账户转出

authTransaction 字段：
- tranId / cardId / status（approved=成功）
- transactionDirection: D=消费支出, C=入账（退款/撤销）
- originalTranId: 非空+C=撤销，空+C=退款
- billingAmount / billingCurrency: 结算金额（USD），用于扣余额
- merchantAmount / merchantName: 商户侧
- closingAmount: 交易后卡片余额（可直接覆写）
- failReason: 拒绝原因码（T1001余额不足 等）
