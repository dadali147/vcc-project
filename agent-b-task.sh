#!/bin/bash
# Agent B (Upstream) 任务启动脚本
# 用于修复 VCC-001~008 上游对接问题

cd ~/workspace/vcc-project

# 设置模型为 Opus 4.6
export CLAUDE_MODEL=claude-opus-4-6

# 启动 Claude Code 并传入任务
claude --permission-mode bypassPermissions --print "
## Agent B (Upstream) 任务 - VCC 项目修复

### 背景
你是 Agent B，负责 VCC 项目的 YeeVCC 上游对接模块。

### 任务清单

#### 任务 1: 修复 VCC-001 - YOP 签名错误
文件: vcc-server/vcc-card/src/main/java/com/vcc/card/client/YeevccApiClientUtil.java

需要检查并修复:
- [ ] 确认 CertTypeEnum.RSA2048 使用正确
- [ ] 确认 YopPKICredentials 构造方式正确
- [ ] 添加调试日志：打印签名原文

#### 任务 2: 修复 VCC-002 - Webhook 回调处理
文件: vcc-server/vcc-card/src/main/java/com/vcc/card/controller/YeevccCallbackController.java

任务:
- [ ] 添加 verifyWebhookSignature() 私有方法（暂时返回 true）
- [ ] 在三个回调接口调用验签方法: /otp, /authTransaction, /cardOperate
- [ ] 3DS 验证码处理 - 收到 /otp 回调后保存验证码，支持商户端展示

#### 任务 3: 查看并修复 VCC-003~008
- [ ] 查看飞书 09-问题追踪 Bitable
- [ ] 修复属于上游模块的问题

### 提交规范
fix(upstream): 修复 YOP 签名和 Webhook 回调 [Agent-B]

- 确认签名实现符合 YOP SDK 规范
- 添加 Webhook 验签空白方法
- 3DS 验证码回调处理

Refs: https://feishu.cn/docx/RWBAdncATop71IxOKomckVIRn3g

### 完成标准
- [ ] 代码编译通过
- [ ] 提交到 git
- [ ] 更新飞书 09-问题追踪 Bitable
- [ ] 报告完成情况
"
