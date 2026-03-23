# YeeVCC (Yeewallex)

## 概述
YeeVCC 是 VCC 项目的上游卡服务提供方。

## 接入信息

| 项目 | 值 |
|------|---|
| 测试环境 | https://apiqa.kun.global/yop-center |
| 生产环境 | https://api.kun.global/yop-center |
| 认证方式 | RSA2048 + AES16 |
| API 文档版本 | v1.4.1 (20251224) |

## 本目录文件
- `YeeVCC-API客户接入指南.md` — 上游接口接入指南（从飞书搬入）
- 后续可继续补充：签名规范、错误码说明、回调文档等

## 与内部基线的关系
- 本目录为**上游外部参考**
- 内部正式接口契约见：`../../02-技术基线/01-内部系统接口契约.md`
- 内部 Webhook 规范见：`../../02-技术基线/05-Webhook与异步任务规范承接方案.md`
