# VCC 商户端多语言支持 (i18n) 实现文档

## 概述

本项目已成功实现前端商户端应用的多语言国际化 (i18n) 支持，支持以下四种语言：
- 简体中文 (zh-CN)
- 繁体中文 (zh-TW)
- 英文 (en-US)
- 越南语 (vi-VN)

默认语言为简体中文，用户选择的语言会被持久化到 localStorage。

## 目录结构

```
src/
├── locales/
│   ├── index.js          # i18n 配置和初始化文件
│   ├── zh-CN.json        # 简体中文翻译文件
│   ├── zh-TW.json        # 繁体中文翻译文件
│   ├── en-US.json        # 英文翻译文件
│   └── vi-VN.json        # 越南语翻译文件
├── components/
│   └── LanguageSelect/
│       └── index.vue     # 语言切换器组件
├── layout/
│   └── components/
│       └── Navbar.vue    # 已集成语言切换按钮
├── main.js              # 应用入口（已配置 i18n）
└── ...
```

## 核心实现

### 1. 翻译文件结构

所有翻译文件（.json）都采用统一的键值结构，包含以下主要模块：

```json
{
  "common": {
    "search": "搜索",
    "reset": "重置",
    ...
  },
  "nav": {
    "dashboard": "仪表板",
    ...
  },
  "navbar": {
    "language": "语言",
    ...
  },
  "login": {
    "title": "登录",
    ...
  },
  "dashboard": {
    "title": "仪表板",
    ...
  },
  "user": {
    "title": "用户管理",
    ...
  },
  "form": {
    "name": "名称",
    ...
  },
  "table": {
    "operation": "操作",
    ...
  },
  "message": {
    "delete_confirm": "确定删除吗？",
    ...
  }
}
```

### 2. i18n 配置 (`src/locales/index.js`)

```javascript
import { createI18n } from 'vue-i18n'

// 从localStorage中获取保存的语言选项
const savedLocale = localStorage.getItem('vcc-language') || 'zh-CN'

const i18n = createI18n({
  legacy: false,                    // 使用 Composition API
  locale: savedLocale,             // 默认语言
  fallbackLocale: 'zh-CN',         // 回退语言
  messages: {
    'zh-CN': zhCN,
    'zh-TW': zhTW,
    'en-US': enUS,
    'vi-VN': viVN
  }
})

// 设置语言并持久化
export function setLocale(locale) {
  if (i18n.global.locale) {
    i18n.global.locale.value = locale
  }
  localStorage.setItem('vcc-language', locale)
}
```

### 3. 主应用入口配置 (`src/main.js`)

```javascript
import i18n from '@/locales'

// ...

app.use(i18n)
```

### 4. 语言切换器组件 (`src/components/LanguageSelect/index.vue`)

- 位置：导航栏右侧菜单
- 显示当前语言简称（中文、繁體、EN、VN）
- 点击后显示下拉菜单，包含四种语言选项
- 选中的语言会显示对勾标记
- 选择语言后立即切换

### 5. 导航栏集成

在 `src/layout/components/Navbar.vue` 中：
- 导入 `LanguageSelect` 组件
- 添加语言切换器到导航栏
- 所有导航栏文本均使用 i18n（如 `$t('navbar.logout')`）

## 使用方法

### 在模板中使用翻译

```vue
<template>
  <div>
    <!-- 直接使用 $t() 函数 -->
    <button>{{ $t('common.search') }}</button>
    
    <!-- 在属性中使用 -->
    <el-tooltip :content="$t('navbar.language')"></el-tooltip>
  </div>
</template>
```

### 在脚本中使用翻译

```javascript
import { useI18n } from 'vue-i18n'

export default {
  setup() {
    const { t, locale } = useI18n()
    
    // 使用 t() 获取翻译
    const title = t('dashboard.title')
    
    // 获取当前语言
    console.log(locale.value)  // 'zh-CN'
    
    return { title }
  }
}
```

### 动态切换语言

```javascript
import { setLocale } from '@/locales'

// 切换到英文
setLocale('en-US')

// 语言会自动持久化到 localStorage
```

## 已翻译内容

### 翻译覆盖范围

1. **通用文本** (common)：搜索、重置、新增、编辑、删除等基础操作
2. **导航菜单** (nav)：仪表板、用户管理、角色管理等菜单项
3. **顶部导航栏** (navbar)：语言选择、主题切换、大小调整、用户菜单等
4. **登录页面** (login)：用户名、密码、登录等
5. **仪表板** (dashboard)：欢迎文本、统计项等
6. **用户管理** (user)：用户相关字段和操作
7. **表单** (form)：通用表单字段
8. **表格** (table)：表格操作列、分页等
9. **提示信息** (message)：成功、失败、错误提示等

### 翻译数量

- **简体中文 (zh-CN)**：165+ 个翻译条目
- **繁体中文 (zh-TW)**：165+ 个翻译条目
- **英文 (en-US)**：165+ 个翻译条目
- **越南语 (vi-VN)**：165+ 个翻译条目

## 测试语言切换功能

### 方式1：通过导航栏

1. 启动开发服务器：`npm run dev`
2. 打开应用 `http://localhost:5174`
3. 在导航栏右侧找到语言切换按钮（显示当前语言简称）
4. 点击下拉菜单，选择不同语言
5. 页面文本立即切换
6. 刷新页面，语言选择会被保留（localStorage）

### 方式2：控制台调试

```javascript
// 在浏览器控制台执行
import { setLocale } from '/src/locales/index.js'

setLocale('en-US')      // 切换到英文
setLocale('zh-TW')      // 切换到繁体中文
setLocale('vi-VN')      // 切换到越南语
setLocale('zh-CN')      // 切换回简体中文
```

## 完成检查清单

- [x] 创建 `src/locales/` 目录结构
- [x] 为四种语言创建翻译文件 (zh-CN.json, zh-TW.json, en-US.json, vi-VN.json)
- [x] 配置 Vue i18n 插件并集成到应用
- [x] 创建语言切换器组件
- [x] 在主导航栏添加语言切换按钮
- [x] 翻译主要页面（导航、登录、仪表板、菜单等）的文本
- [x] 实现语言选择持久化 (localStorage)
- [x] 设置默认语言为简体中文 (zh-CN)
- [x] 开发服务器测试成功

## 扩展指南

### 添加新的翻译

1. 在所有语言文件中添加对应的键值对：

```json
{
  "newModule": {
    "newKey": "翻译内容"
  }
}
```

2. 在组件中使用：

```vue
{{ $t('newModule.newKey') }}
```

### 添加新语言

1. 创建新的语言文件，如 `src/locales/ja-JP.json`
2. 复制现有翻译结构并翻译
3. 在 `src/locales/index.js` 中导入并添加：

```javascript
import jaJP from './ja-JP.json'

const i18n = createI18n({
  // ...
  messages: {
    'ja-JP': jaJP,
    // ...
  }
})
```

4. 更新 `LanguageSelect` 组件的语言列表

## 性能优化

- 翻译文件使用 JSON 格式，易于缓存
- 语言选择持久化在 localStorage，避免重复查询
- Lazy Loading：可配置按需加载不同语言包

## 浏览器兼容性

- 支持所有现代浏览器（Chrome, Firefox, Safari, Edge）
- localStorage 支持所有现代浏览器
- Vue 3 + Vue-i18n 9 官方支持

## 相关文件修改摘要

1. **src/main.js**：添加 i18n 导入和使用
2. **src/locales/index.js**：新建 i18n 配置文件
3. **src/locales/zh-CN.json**：简体中文翻译
4. **src/locales/zh-TW.json**：繁体中文翻译
5. **src/locales/en-US.json**：英文翻译
6. **src/locales/vi-VN.json**：越南语翻译
7. **src/components/LanguageSelect/index.vue**：语言切换器组件
8. **src/layout/components/Navbar.vue**：集成语言切换器，更新文本为 i18n

## 后续建议

1. **添加更多页面的翻译**：继续为其他页面添加翻译键值
2. **日期/数字格式化**：使用 vue-i18n 的数字和日期格式化功能
3. **双语网址**：可考虑在 URL 中包含语言前缀（如 `/en/dashboard`）
4. **翻译管理工具**：使用翻译管理平台（如 Crowdin）协作管理翻译
5. **国际化 SEO**：为多语言版本添加适当的 hreflang 标签

---

**实现完成时间**：2026-03-17 22:43 UTC+8
**版本**：Vue 3.5.26 + Vue-i18n 9.14.5
**开发状态**：✅ 完成，已测试
