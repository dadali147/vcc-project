# VCC 多语言支持 - 快速开始指南

## 🚀 快速启动

### 1. 安装依赖（如未安装）

```bash
cd ~/workspace/vcc-project/vcc-web
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 `http://localhost:5174`（或控制台显示的实际端口）

### 3. 测试语言切换

1. 打开应用
2. 找到导航栏右上角的语言按钮（显示"中文"、"繁體"、"EN"、"VN"）
3. 点击选择不同语言
4. 页面文本立即切换 ✅

---

## 📋 翻译的模块一览

### 基础操作 (common)
- 搜索、重置、新增、编辑、删除
- 确定、取消、保存、提交
- 返回、导出、导入
- 加载、成功、失败、警告

### 导航菜单 (nav)
- 仪表板、菜单、系统管理
- 用户、角色、权限、部门
- 日志、监控、缓存等

### 页面头部 (navbar)
- 语言切换、主题模式、大小调整
- 用户菜单、退出登录
- 帮助链接

### 登录页面 (login)
- 用户名、密码输入
- 记住我、忘记密码
- 错误提示

### 仪表板 (dashboard)
- 欢迎文本、统计卡片
- 数据图表标签

### 用户管理 (user)
- 用户字段、操作按钮
- 添加、编辑、删除确认

### 表格操作 (table)
- 操作列、分页
- 全选、批量删除

### 提示信息 (message)
- 成功、失败提示
- 网络错误、超时信息

---

## 💻 在代码中使用翻译

### 方法 1：模板中使用 (最常见)

```vue
<template>
  <div>
    <h1>{{ $t('dashboard.welcome') }}</h1>
    <button>{{ $t('common.save') }}</button>
    <el-tooltip :content="$t('navbar.language')">
      <icon />
    </el-tooltip>
  </div>
</template>
```

### 方法 2：脚本中使用

```javascript
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

// 获取翻译
const message = t('message.delete_success')

// 获取当前语言
console.log(locale.value)  // 'zh-CN' / 'zh-TW' / 'en-US' / 'vi-VN'
```

### 方法 3：事件处理中切换

```javascript
import { setLocale } from '@/locales'

function handleLanguageChange(lang) {
  setLocale(lang)  // 自动切换语言并保存到 localStorage
}
```

---

## 📁 文件位置速查

```
src/locales/                              # 翻译文件目录
├── index.js                              # i18n 配置
├── zh-CN.json                            # 简体中文 (165+ 条)
├── zh-TW.json                            # 繁体中文 (165+ 条)
├── en-US.json                            # 英文 (165+ 条)
└── vi-VN.json                            # 越南语 (165+ 条)

src/components/LanguageSelect/
└── index.vue                             # 语言切换器组件

src/layout/components/
└── Navbar.vue                            # 已集成语言切换器
```

---

## 🌐 支持的语言

| 代码 | 名称 | 显示简称 | 完成度 |
|-----|------|---------|--------|
| zh-CN | 简体中文 | 中文 | ✅ 100% |
| zh-TW | 繁体中文 | 繁體 | ✅ 100% |
| en-US | English | EN | ✅ 100% |
| vi-VN | Tiếng Việt | VN | ✅ 100% |

---

## 🔧 添加新翻译的步骤

### 例：添加新模块"支付"的翻译

**第 1 步**：在四个翻译文件中添加

```json
// src/locales/zh-CN.json
{
  "payment": {
    "title": "支付管理",
    "amount": "金额",
    "status": "状态",
    "success": "支付成功"
  }
}

// src/locales/zh-TW.json
{
  "payment": {
    "title": "支付管理",
    "amount": "金額",
    "status": "狀態",
    "success": "支付成功"
  }
}

// src/locales/en-US.json
{
  "payment": {
    "title": "Payment Management",
    "amount": "Amount",
    "status": "Status",
    "success": "Payment successful"
  }
}

// src/locales/vi-VN.json
{
  "payment": {
    "title": "Quản lý thanh toán",
    "amount": "Số tiền",
    "status": "Trạng thái",
    "success": "Thanh toán thành công"
  }
}
```

**第 2 步**：在组件中使用

```vue
<template>
  <div>
    <h2>{{ $t('payment.title') }}</h2>
    <p>{{ $t('payment.amount') }}: {{ amount }}</p>
    <p>{{ $t('payment.status') }}: {{ $t('payment.success') }}</p>
  </div>
</template>
```

---

## 🐛 常见问题

### Q: 如何判断当前使用的语言？

```javascript
import { useI18n } from 'vue-i18n'
const { locale } = useI18n()
console.log(locale.value)  // 'zh-CN' 或其他
```

### Q: 刷新后语言会改变吗？

不会。语言选择自动保存到 `localStorage`，刷新后仍使用上次选择。

### Q: 如何重置为默认语言？

```javascript
import { setLocale } from '@/locales'
setLocale('zh-CN')  // 回到简体中文
```

### Q: 如何为新语言添加翻译？

1. 创建 `src/locales/ja-JP.json`（以日语为例）
2. 在 `src/locales/index.js` 导入并添加到 messages
3. 在 `LanguageSelect` 组件添加到菜单

### Q: 翻译不显示或显示 key 是什么原因？

- 检查 JSON 文件是否有语法错误
- 确保 key 拼写完全相同（区分大小写）
- 检查 i18n 是否正确初始化（在 main.js 中）

### Q: 能否在 URL 中显示语言前缀？

可以，需要修改路由配置。这是高级用法，详见文档。

---

## 📊 翻译统计

- **总条目数**：165+
- **支持语言**：4 种
- **总翻译量**：660+ 字符串
- **覆盖范围**：所有主要功能模块

---

## ✨ 功能亮点

✅ **即时切换** - 无需刷新，点击即切换  
✅ **自动保存** - localStorage 持久化语言选择  
✅ **完整翻译** - 165+ 条目覆盖所有主要功能  
✅ **四种语言** - 简体、繁体、英文、越南语  
✅ **易于扩展** - 模块化结构，易于添加新翻译  
✅ **零依赖冲突** - 与现有框架完美兼容  

---

## 📚 更多资源

- **实现文档**：`MULTILINGUAL_IMPLEMENTATION.md`
- **测试报告**：`TESTING_REPORT.md`
- **Vue i18n 官文**：https://vue-i18n.intlify.dev/
- **项目文件**：`src/locales/`

---

## 🎯 下一步

1. ✅ 测试四种语言是否正常切换
2. ✅ 验证语言选择是否自动保存
3. ⏳ 为其他页面添加翻译（可选）
4. ⏳ 集成翻译管理工具（可选）

---

**文档版本**：1.0  
**最后更新**：2026-03-17  
**维护人员**：VCC Project Agent E
