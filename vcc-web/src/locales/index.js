import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.json'
import zhTW from './zh-TW.json'
import enUS from './en-US.json'
import viVN from './vi-VN.json'

// 从localStorage中获取保存的语言选项
const savedLocale = localStorage.getItem('vcc-language') || 'zh-CN'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'zh-TW': zhTW,
    'en-US': enUS,
    'vi-VN': viVN
  }
})

// 设置语言函数
export function setLocale(locale) {
  if (i18n.global.locale) {
    i18n.global.locale.value = locale
  }
  localStorage.setItem('vcc-language', locale)
  // 更新Element Plus的语言
  document.documentElement.lang = locale
}

export default i18n
