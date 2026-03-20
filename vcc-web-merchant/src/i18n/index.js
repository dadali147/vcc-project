import { createI18n } from 'vue-i18n'
import zh from './messages/zh'
import zh_TW from './messages/zh_TW'
import en from './messages/en'
import vi from './messages/vi'

const messages = {
  zh,
  zh_TW,
  en,
  vi
}

const getInitialLocale = () => {
  const saved = localStorage.getItem('language')
  // 如果有合法的缓存则使用缓存
  if (saved && messages[saved]) {
    return saved
  }
  // 首次进入或无合法缓存时，强制设置为简体中文并写入缓存，确保稳定
  localStorage.setItem('language', 'zh')
  return 'zh'
}

const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: 'zh', // 将 fallback 从 en 改为 zh，确保极端情况下也是中文
  messages
})

export default i18n
