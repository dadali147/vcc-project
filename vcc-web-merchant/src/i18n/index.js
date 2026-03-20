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

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('language') || 'en',
  fallbackLocale: 'en',
  messages
})

export default i18n
