<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import zhTw from 'element-plus/dist/locale/zh-tw.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import { useI18n } from 'vue-i18n'

const { locale: i18nLocale } = useI18n()

// 动态匹配 Element Plus 的语言包，默认强制走简体中文
const locale = computed(() => {
  switch (i18nLocale.value) {
    case 'zh':
      return zhCn
    case 'zh_TW':
      return zhTw
    case 'en':
      return en
    default:
      return zhCn
  }
})
</script>

<style>
/* Global styles can stay here */
body {
  margin: 0;
  font-family: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background-color: #F9FAFB;
  color: #111111;
}

#app {
  min-height: 100vh;
}

* {
  box-sizing: border-box;
}
</style>
