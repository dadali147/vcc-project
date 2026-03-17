# Code Review: vcc-upstream 模块 [Agent-F]

## 总体评分：4/10

## ✅ 优点

- `YeeVccClient` 已实现 14 个 YeeVCC 接口入口，接口数量满足本次审查范围；对应请求/响应 DTO 也已拆分，基础封装面是完整的。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:87` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:193`。
- AES 工具实现与审查要求基本一致：算法是 `AES/CBC/PKCS5Padding`，校验 16 字节 key，IV 取 key 前 16 字节。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Aes16CryptoUtils.java:17`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Aes16CryptoUtils.java:42`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Aes16CryptoUtils.java:67`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Aes16CryptoUtils.java:73`。
- 超时、重试、异常包装和配置外置已具备基本框架，后续补契约细节时有落点。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:332` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:379`，以及 `vcc-server/vcc-admin/src/main/resources/application.yml:149` 到 `vcc-server/vcc-admin/src/main/resources/application.yml:174`。

## ⚠️ 需要改进

- 缺少任何自动化测试。`vcc-server/vcc-upstream/` 下没有 `src/test`，仓库中也没有 `YeeVccClient` 的业务调用方或 Mock upstream 验证；当前改动几乎只能靠联调兜底。
- 接口路径使用硬编码加 fallback 候选值的方式，说明协议尚未真正固化。例如 `PATH_CARD_HOLDER_CANDIDATES` 和 `PATH_CARD_KEY_INFO_CANDIDATES` 同时保留两套路径，属于“猜测兼容”而不是“已验证契约”。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:56` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:66`。
- 请求参数几乎没有本地校验。除 `request != null` 和 `customerId` 自动回填外，所有 DTO 都允许必填字段为空直接打到上游，错误只能在运行时以远端报错暴露。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:196` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:215`，以及 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccRequests.java`。
- `maxRetries` 的命名和实际行为不一致。现在 `Math.max(config.getMaxRetries(), 1)` 直接把配置值当成总尝试次数，默认 `2` 实际只会有 2 次总请求，而不是“初始请求 + 2 次重试”。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/config/YeeVccConfig.java:32` 和 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:334`。

## 🐛 发现的 Bug

- `addCardHolder` 的路径大概率写错。代码使用 `/rest/v1.0/vcc/add-card-holder`，但公开 YeeVCC 文档页面对应的是 `POST /rest/v1.0/vcc/user-account/create`。这不是命名差异，而是资源路径完全不同。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:55`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:87`，以及官方文档 `<https://open.kun.global/docs/apis/yeevcc/post__rest__v1.0__vcc__user-account__create>`。
- GET 请求的签名串和真实发送的 URI 可能不一致。`buildUri()` 会对 query 参数做 URL encode，而 `toCanonicalQuery()` 签名时只是直接拼接 `key=value`，没有做同样的编码；当参数含邮箱、空格、中文、`+`、`&` 等字符时，签名会失配。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:246` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:249`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:309` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:319`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:617` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:632`。
- 响应解密失败会被静默吞掉。`tryDecrypt()` 捕获异常后直接返回原始密文，上层既收不到异常，也不知道卡号/CVV/有效期没有成功解密，错误数据会继续向下游传播。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:473` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:486`。
- 响应验签逻辑和请求签名逻辑不对称。请求侧自己构造 canonical string，响应侧却直接对 `responseBody` 验签；如果平台验签协议不是“原文 body 直接签名”，这里会长期处于不可用状态。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:271` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:306`，以及 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:401` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:405`。

## 🔒 安全隐患

- 请求签名虽然使用了 `SHA256withRSA`，但 canonical 规则并未基于已确认的官方协议实现。代码注释已经明确承认“文档未公开完整 canonical 规则时”先收敛在本地实现，这意味着协议层正确性并没有被证实。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:290` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:306`。
- Header 名和签名编码方式与官方 YOP SDK 暴露的约定不一致。当前默认值是 `x-yop-date`、`x-yop-signature`，且签名使用标准 Base64；官方 `yop-go-sdk` 暴露的是 `Date`、`x-yop-sign`，`RsaSignBase64` 也明确说明是 base64url。若 YeeVCC 复用 YOP 标准协议，这里会直接导致请求不兼容或验签失败。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/config/YeeVccConfig.java:224` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/config/YeeVccConfig.java:236`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Rsa2048SignatureUtils.java:33` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Rsa2048SignatureUtils.java:50`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Rsa2048SignatureUtils.java:63` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/util/Rsa2048SignatureUtils.java:76`，以及官方 SDK 文档 `<https://pkg.go.dev/github.com/yop-platform/yop-go-sdk/v3/yop/http#pkg-constants>`、`<https://pkg.go.dev/github.com/yop-platform/yop-go-sdk/v3/yop/security#RsaSignBase64>`。
- 敏感信息暴露面过大。`YeeVccApiResponse` 永远保留 `rawBody`，`mapCardKeyInfo()` 又把密文和解密后的 PAN/CVV/有效期同时保存到 DTO；只要业务层序列化或打印响应对象，就有较高概率把三要素和上游原始报文一起打到日志或下游。参考 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccApiResponse.java:18` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccApiResponse.java:22`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccApiResponse.java:74` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccApiResponse.java:82`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:457` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/YeeVccClient.java:470`、`vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccModels.java:402` 到 `vcc-server/vcc-upstream/src/main/java/com/vcc/upstream/dto/YeeVccModels.java:480`。
- 响应验签默认关闭。`verifyResponseSignature` 和 `failOnMissingSignature` 在默认配置里都是 `false`，上游链路默认等于“不做报文真实性校验”。参考 `vcc-server/vcc-admin/src/main/resources/application.yml:161` 到 `vcc-server/vcc-admin/src/main/resources/application.yml:162`。

## 📝 建议

- 先以官方 YeeVCC/YOP 契约为准，统一修正 14 个接口的 endpoint path、header name、canonical 规则和 Base64URL 编码，不要继续依赖 fallback path 掩盖契约不确定性。
- 为 14 个接口补契约测试，最低覆盖：请求路径/方法、query/body 序列化、签名 header、上游 4xx/5xx、超时重试、AES 解密成功/失败、签名缺失/错误。
- `CardKeyInfoData` 不要同时持有密文和明文三要素；`YeeVccApiResponse.rawBody` 也不应该默认暴露给业务层，至少应做脱敏或只在受控调试场景下可用。
- `tryDecrypt()` 解密失败时应显式抛错或返回带状态的结果对象，不能把密文当成功结果继续下发。
- 给所有 request DTO 增加本地必填校验和格式校验，避免明显非法请求直接打到上游。
- 本次只做了静态代码审查，未执行编译或测试验证；当前环境缺少 `mvn`，建议在 CI 中增加 `vcc-upstream` 的独立构建与测试任务。

---
