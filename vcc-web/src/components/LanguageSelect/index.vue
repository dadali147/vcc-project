<template>
  <el-dropdown @command="handleCommand" class="language-selector right-menu-item hover-effect" trigger="hover">
    <div class="language-wrapper">
      <svg-icon icon-class="language" />
      <span class="language-text">{{ currentLanguage }}</span>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-CN" :class="{ active: currentLocale === 'zh-CN' }">
          <span>简体中文</span>
          <i v-if="currentLocale === 'zh-CN'" class="el-icon-check"></i>
        </el-dropdown-item>
        <el-dropdown-item command="zh-TW" :class="{ active: currentLocale === 'zh-TW' }">
          <span>繁體中文</span>
          <i v-if="currentLocale === 'zh-TW'" class="el-icon-check"></i>
        </el-dropdown-item>
        <el-dropdown-item command="en-US" :class="{ active: currentLocale === 'en-US' }">
          <span>English</span>
          <i v-if="currentLocale === 'en-US'" class="el-icon-check"></i>
        </el-dropdown-item>
        <el-dropdown-item command="vi-VN" :class="{ active: currentLocale === 'vi-VN' }">
          <span>Tiếng Việt</span>
          <i v-if="currentLocale === 'vi-VN'" class="el-icon-check"></i>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { setLocale } from '@/locales'

const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

const languageNames = {
  'zh-CN': '中文',
  'zh-TW': '繁體',
  'en-US': 'EN',
  'vi-VN': 'VN'
}

const currentLanguage = computed(() => languageNames[locale.value] || 'Language')

function handleCommand(command) {
  setLocale(command)
}
</script>

<style scoped lang="scss">
.language-selector {
  display: inline-block;
  padding: 0 8px;
  height: 100%;
  cursor: pointer;
  transition: background 0.3s;
  -webkit-tap-highlight-color: transparent;
  display: flex;
  align-items: center;

  &:hover {
    background: rgba(0, 0, 0, 0.025);
  }

  .language-wrapper {
    display: flex;
    align-items: center;
    gap: 4px;

    .language-text {
      font-size: 12px;
      font-weight: 500;
    }
  }

  :deep(.el-dropdown-menu__item) {
    display: flex;
    justify-content: space-between;
    align-items: center;

    &.active {
      color: var(--el-color-primary);
      font-weight: 500;
    }

    i {
      margin-left: 8px;
      color: var(--el-color-primary);
    }
  }
}
</style>
