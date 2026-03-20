# VCC 虚拟卡 - 前端开发项目指南

**项目启动日期**: 2026-03-18  
**文档版本**: 1.0  
**状态**: 📋 规范定义完成，待开发  

---

## 🎯 项目目标

为 VCC 虚拟卡平台开发两个完整的前端应用：
- **商户端** (vcc-web-merchant) - 10 个页面
- **管理端** (vcc-web-admin) - 10 个页面

---

## 📚 文档导航

### 1️⃣ 新手入门 (15 分钟)
- 阅读: **FRONTEND_PLAN.md** - 项目总体规划和快速总结
- 查看: **ui-design/** - 11 张设计稿原型

### 2️⃣ 深入理解 (1-2 小时)
- 阅读: **FRONTEND_SPEC.md** - 商户端完整规范
  - 包含 10 个页面的详细设计、API、交互、字段定义
  - 包含完整的设计系统、权限矩阵、部署指南

### 3️⃣ 快速开始 (30 分钟)
- 按照 **FRONTEND_STARTUP_GUIDE.md** 初始化项目
- 包含项目初始化、目录结构、开发工作流、常见问题

### 4️⃣ 管理端参考 (可选)
- 阅读: **FRONTEND_ADMIN_SPEC.md** - 管理端初版规范
- 注: 管理端无设计稿，规范基于商户端推断
- 可根据实际需求调整

---

## 📦 交付文件清单

```
~/workspace/vcc-project/
├── FRONTEND_PLAN.md                      (6.5KB) ← 从这里开始！
├── FRONTEND_SPEC.md                      (34KB)  ← 商户端完整规范
├── FRONTEND_STARTUP_GUIDE.md             (14KB)  ← 开发指南
├── FRONTEND_ADMIN_SPEC.md                (8.3KB) ← 管理端规范
├── ui-design/                            (12.2MB)
│   ├── 常规卡主页面.png
│   ├── 常规卡详情页.png
│   ├── 共享卡主页面.png
│   ├── 共享卡详情页.png
│   ├── 用卡人主页面.png
│   ├── 用卡人详情页.png
│   ├── 交易明细主页面.png (×2 版本)
│   ├── 开卡记录.png
│   ├── 共享卡交易限额调整记录.png
│   └── 设置中心.png
└── vcc-web-merchant/                     (待创建)
    └── (项目代码)
```

---

## ⏱️ 开发时间表

| 阶段 | 开始日期 | 截止日期 | 预计工时 | 任务 |
|------|---------|---------|---------|------|
| **第 1 周** | 03-18 | 03-22 | 16h | 项目初始化 + 基础组件 |
| **第 2-3 周** | 03-23 | 04-05 | 56h | 商户端核心 + 全部页面 + API 对接 |
| **第 4 周** | 04-06 | 04-12 | 40h | 管理端初版 + 集成测试 |
| **第 5 周** | 04-13 | 04-19 | 20h | 优化、修复、部署上线 |
| **合计** | - | 04-19 | **~132h** | **全部完成** |

**人力配置**: 建议 1-2 名前端开发者

---

## 🚀 快速开始 (5 分钟)

### 前置条件
```bash
node --version  # 需要 18+
npm --version   # 需要 9+
```

### 初始化商户端项目
```bash
mkdir -p ~/workspace/vcc-project/vcc-web-merchant
cd ~/workspace/vcc-project/vcc-web-merchant

# 创建 Vue 3 项目
npm create vue@latest . -- \
  --typescript --router --pinia

# 安装依赖
npm install
npm install axios dayjs vee-validate zod element-plus

# 启动开发服务器
npm run dev

# 访问 http://localhost:5174
```

详细步骤见: **FRONTEND_STARTUP_GUIDE.md**

---

## 📋 商户端页面清单

| # | 页面 | 路由 | 设计稿 | 复杂度 | 优先级 |
|---|------|------|-------|--------|--------|
| 1 | 登录 | `/login` | ❌ | ⭐ | P0 |
| 2 | 注册 | `/register` | ❌ | ⭐⭐ | P0 |
| 3 | 仪表盘 | `/dashboard` | ❌ | ⭐⭐ | P0 |
| 4 | 用卡人管理 | `/cardholders` | ✅ | ⭐⭐ | P0 |
| 5 | 卡片列表 | `/cards` | ✅ | ⭐⭐⭐ | P0 |
| 6 | 卡片详情 | `/cards/:id` | ✅ | ⭐⭐⭐⭐ | P0 |
| 7 | 开卡申请 | `/card-apply` | ❌ | ⭐⭐⭐ | P0 |
| 8 | 开卡记录 | `/card-applications` | ✅ | ⭐⭐ | P1 |
| 9 | 交易明细 | `/transactions` | ✅ | ⭐⭐ | P1 |
| 10 | 限额历史 | `/shared-card-limits` | ✅ | ⭐⭐ | P1 |
| 11 | 个人中心 | `/profile` | ✅ | ⭐⭐ | P2 |

**✅ = 有设计稿  |  ❌ = 无设计稿，自由发挥**

---

## 💡 核心特性

### 卡片管理
- ✅ 实时查询卡片余额（点击即查）
- ✅ 三要素安全查看（二次验证 + 记录）
- ✅ 卡片批量操作（冻结、充值、注销）
- ✅ 表格导出（CSV 格式，支持中文）

### 账户权限
- ✅ 主账户 + 子账户的权限区分
- ✅ 基于角色的菜单和操作控制
- ✅ 权限矩阵完整定义

### 交易管理
- ✅ 实时交易记录展示
- ✅ 日期、类型、金额筛选
- ✅ 导出交易数据

### 共享卡功能
- ✅ 实时预算设置
- ✅ 限额调整历史记录
- ✅ 当前预算使用率展示

---

## 🛠️ 技术栈

```json
{
  "framework": "Vue 3 (Composition API)",
  "buildTool": "Vite 5",
  "uiLibrary": "Element Plus 2.4+",
  "stateManagement": "Pinia 2",
  "http": "axios 1.6+",
  "routing": "vue-router 4",
  "styling": "Tailwind CSS + Element Plus",
  "validation": "VeeValidate 4 + Zod 3",
  "charts": "ECharts 5 (管理端需要)",
  "testing": "Vitest (目标 >80% 覆盖)"
}
```

---

## 📊 质量目标

| 指标 | 目标 | 工具 |
|------|------|------|
| **测试覆盖率** | >80% | Vitest |
| **代码质量** | 100% ESLint 通过 | ESLint |
| **类型检查** | 0 any | TypeScript strict |
| **性能** | Lighthouse >85 分 | Lighthouse |
| **加载时间** | 首屏 <3s | Chrome DevTools |

---

## ✅ 开发前检查清单

在开始开发前，请确认：

- [ ] 阅读过 FRONTEND_PLAN.md（项目总览）
- [ ] 查看过 ui-design/ 中的设计稿
- [ ] 理解 FRONTEND_SPEC.md 中的页面规范
- [ ] 能够独立初始化 Vue 3 + Vite 项目
- [ ] 理解 Composition API 和 Pinia 的基本用法
- [ ] 有访问后端 API 的权限

**如果有任何疑问，请参考 FRONTEND_STARTUP_GUIDE.md 的常见问题部分。**

---

## 📞 反馈和问题

| 问题类型 | 处理方式 | 参考文档 |
|---------|---------|---------|
| 页面设计理解不清楚 | 查看 FRONTEND_SPEC.md + ui-design/ | FRONTEND_SPEC.md |
| API 接口定义问题 | 查看规范文档中的 API 部分 | FRONTEND_SPEC.md #API对接规范 |
| 项目初始化问题 | 按照启动指南执行 | FRONTEND_STARTUP_GUIDE.md |
| 开发工作流问题 | 查看启动指南的工作流部分 | FRONTEND_STARTUP_GUIDE.md #开发工作流 |
| 常见开发问题 | 查看 FAQ 部分 | FRONTEND_STARTUP_GUIDE.md #常见问题 |

---

## 📌 重要提示

1. **先完成商户端** - 商户端有完整的设计稿和规范，优先级更高
2. **复用框架** - 管理端可以复用商户端的框架和组件库
3. **API 对接** - 所有 API 定义都在规范文档中，参考即可
4. **测试覆盖** - 单元测试覆盖率目标 >80%
5. **代码规范** - 遵循 TypeScript + ESLint，无 any 类型

---

## 🎉 成功标志

当以下清单全部完成时，项目成功：

- [ ] 商户端 10 个页面全部可访问
- [ ] 管理端 10 个页面全部可访问
- [ ] 所有 API 对接完成，测试通过
- [ ] 单元测试覆盖 >80%
- [ ] ESLint 无任何错误
- [ ] 性能指标达标（Lighthouse >85）
- [ ] 部署文档完成
- [ ] 可在生产环境正常运行

---

**开始开发吧！祝你好运！** 🚀

---

**文档版本**: 1.0  
**最后更新**: 2026-03-18  
**维护人**: Architect (Agent 0)
