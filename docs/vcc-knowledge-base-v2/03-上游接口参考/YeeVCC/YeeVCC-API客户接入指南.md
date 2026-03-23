# YeeVCC API 客户接入指南

> **来源**: https://y8qet3cwh0.feishu.cn/wiki/XWVAwfXiDiafMFka3uxcz1GBnBc
> **版本**: v1.4.1
> **抽取时间**: 2026-03-22
> **抽取状态**: ⚠️ 内容为 PDF 附件，无法通过 API 提取文本

## 抽取说明

该飞书文档正文仅为一个 PDF 附件：`YeeVCC API客户接入指南 v1.4.1 20251224.pdf`

飞书 doc API 不支持直接下载/解析 PDF 文件内容，因此无法自动抽取全文。

### 手动获取步骤

1. 打开飞书文档：https://y8qet3cwh0.feishu.cn/wiki/XWVAwfXiDiafMFka3uxcz1GBnBc
2. 下载 PDF 附件
3. 使用 PDF 解析工具提取文本
4. 手动写入本文件

### 替代参考

相关上游接口信息可参考以下已抽取的内部文档：
- **02.1-内部系统接口契约 v1** — 包含上游 YeeVCC 接口路径、认证机制、错误码映射等汇总信息
- **YeeVCC 官方文档**: https://open.kun.global/docs/apis/yeevcc/

## 附录：从内部接口契约中提取的 YeeVCC 相关信息

### 基础信息

| 项目 | 内容 |
|------|------|
| 测试环境 | https://apiqa.kun.global/yop-center |
| 生产环境 | https://api.kun.global/yop-center |
| 认证方式 | RSA2048 签名 + AES 加密 |
| 官方文档 | https://open.kun.global/docs/apis/yeevcc/ |

### 上游核心接口清单

| 分类 | 接口名称 | 上游路径 | 方法 | 说明 |
|------|---------|---------|------|------|
| 卡基础 | 获取卡 Bin 列表 | /rest/v1.0/vcc/card-bins | GET | 获取可开卡产品范围 |
| 持卡人 | 添加持卡人 | /rest/v1.0/vcc/add-card-holder | POST | 建立上游持卡人 |
| 开卡 | 申请开卡 | /rest/v1.0/vcc/card-open | POST | 发起开卡申请 |
| 开卡 | 查询开卡结果 | /rest/v1.0/vcc/card-open-task | GET | 查询异步开卡结果 |
| 卡信息 | 获取卡三要素 | /rest/v1.0/vcc/card-key-info | GET | 获取卡号/CVV/有效期 |
| 卡状态 | 激活卡片 | /rest/v1.0/vcc/card-activate | POST | 卡片激活 |
| 资金 | 卡充值 | /rest/v1.0/vcc/card-recharge | POST | 发起充值 |
| 资金 | 充值结果查询 | /rest/v1.0/vcc/card-recharge-query | GET | 查询异步充值结果 |
| 资金 | 卡转出 | /rest/v1.0/vcc/card-withdraw | POST | 卡资金转出 |
| 资金 | 查询余额 | /rest/v1.0/vcc/card-balance | GET | 查询卡余额 |

### 上游错误码映射

| YeeVCC 错误码 | 内部错误码 | 说明 |
|---------------|-----------|------|
| 0 | 10000 | 成功 |
| 10001 | 10101 | 参数错误 |
| 10002 | 10203 | 签名错误 |
| 10003 | 20101 | 持卡人不存在 |
| 10004 | 31101 | 余额不足 |
| 10005 | 30101 | 卡片不存在 |
| 10006 | 30104 | 卡片已冻结 |
| 10007 | 30105 | 卡片已销卡 |
| 10008 | 32101 | 交易失败 |
| 10009 | 90102 | 上游接口超时 |
| 10010 | 90101 | 上游接口错误 |
