# VCC 项目 Git 工作流规范

> 版本：v1.0  
> 更新时间：2026-03-16

---

## 一、分支策略（Git Flow 简化版）

```
main (生产分支)
  ↑
develop (开发分支) ← 所有开发从这里切出
  ↑
feature/* (功能分支)
hotfix/* (热修分支)
```

### 分支说明

| 分支 | 用途 | 命名规范 |
|------|------|----------|
| `main` | 生产环境代码，永远可部署 | 固定 |
| `develop` | 开发环境代码，集成测试 | 固定 |
| `feature/{模块}-{功能}` | 新功能开发 | `feature/card-holder-crud` |
| `hotfix/{问题描述}` | 生产环境紧急修复 | `hotfix/fix-auth-bug` |

---

## 二、开发流程

### 2.1 开始新功能

```bash
# 1. 从 develop 更新代码
git checkout develop
git pull origin develop

# 2. 创建功能分支
git checkout -b feature/card-holder-crud

# 3. 开发...（提交规范见下文）

# 4. 推送到远程
git push origin feature/card-holder-crud

# 5. 发起 Pull Request 到 develop
```

### 2.2 提交规范（Conventional Commits）

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型（type）：**

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | 修复 Bug |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响功能） |
| `refactor` | 重构 |
| `test` | 测试相关 |
| `chore` | 构建/工具相关 |

**示例：**

```bash
feat(card): 添加持卡人创建接口

- 实现 POST /api/v1/card-holders
- 添加参数校验
- 集成 YeeVCC 上游接口

Closes #123
```

---

## 三、Code Review 流程

### 3.1 Review 规则

| 规则 | 说明 |
|------|------|
| **必须 Review** | 所有代码合并前必须经过 Review |
| **Review 人** | 至少 1 人 Approve 才能合并 |
| **Review 时效** | 24 小时内必须响应 |
| **冲突解决** | 提交者负责解决冲突 |

### 3.2 Review 检查清单

**代码质量：**
- [ ] 代码符合项目规范
- [ ] 命名清晰、注释完整
- [ ] 无明显的性能问题
- [ ] 异常处理完善

**功能完整：**
- [ ] 功能实现符合需求
- [ ] 边界条件处理
- [ ] 单元测试覆盖

**安全性：**
- [ ] 无敏感信息硬编码
- [ ] 输入参数校验
- [ ] SQL 注入防护

### 3.3 Review 评论规范

| 标签 | 含义 | 处理方式 |
|------|------|----------|
| `blocking` | 阻塞问题，必须修复 | 修复后才能合并 |
| `suggestion` | 建议，可选修复 | 讨论后决定是否修复 |
| `question` | 疑问 | 回复解释 |
| `nit` | 小瑕疵 | 可选修复 |

**示例：**
```
blocking: 这里缺少参数校验，可能导致空指针异常

suggestion: 建议提取为常量，避免魔法数字

question: 这个查询是否考虑了并发情况？
```

---

## 四、合并规范

### 4.1 合并前检查

- [ ] CI 检查通过（如果有）
- [ ] Code Review 通过（至少 1 个 Approve）
- [ ] 无冲突
- [ ] 提交信息规范

### 4.2 合并方式

**使用 `Squash and Merge`：**

```
feat(card): 添加持卡人管理功能

- 实现持卡人 CRUD 接口
- 集成 YeeVCC 上游
- 添加单元测试

Closes #123
```

**禁止：**
- ❌ 直接 push 到 main/develop
- ❌ 使用 `Merge Commit`（保持历史整洁）
- ❌ 合并未 Review 的代码

---

## 五、发布流程

### 5.1 开发环境发布

```
feature/* → develop 分支自动部署到测试环境
```

### 5.2 生产环境发布

```
develop → main 分支手动触发部署
```

**发布检查清单：**
- [ ] 版本号更新
- [ ] CHANGELOG 更新
- [ ] 数据库迁移脚本执行
- [ ] 配置检查

---

## 六、Agent 协作规范

### 6.1 分支分配

| Agent | 分支命名 | 说明 |
|-------|----------|------|
| Agent A (Foundation) | `feature/foundation-*` | 基础框架、数据库 |
| Agent B (Upstream) | `feature/upstream-*` | YeeVCC 对接 |
| Agent C (Core) | `feature/core-*` | 持卡人、开卡、充值 |
| Agent D (Webhook) | `feature/webhook-*` | 回调处理 |
| Agent E (Admin) | `feature/admin-*` | 管理后台 |

### 6.2 代码提交格式

```
feat({模块}): {功能描述} [Agent-{代号}]

- 详细变更点
- 详细变更点

Refs: {相关文档链接}
```

**示例：**
```
feat(card-holder): 添加持卡人创建接口 [Agent-C]

- 实现 POST /api/v1/card-holders
- 添加参数校验
- 集成 YeeVCC 上游接口

Refs: https://feishu.cn/wiki/LE4Mw89w6iaAOJkEo59cZwZWnHd
```

---

## 七、相关文档

- [接口契约文档](https://feishu.cn/wiki/LE4Mw89w6iaAOJkEo59cZwZWnHd)
- [数据库设计文档](https://feishu.cn/docx/RheldFtlSoWz8RxxyrNcuIdXnoB)
- [会议纪要](https://feishu.cn/wiki/RS1swUs3qiks0ckO5CIcoLx8ngK)
