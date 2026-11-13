import { createI18n } from 'vue-i18n'
import ru from './ru'
import en from './en'
import { russianPlural } from './plural'

export type Locale = 'ru' | 'en'

const LOCALE_KEY = 'wm-locale'

function initialLocale(): Locale {
  const saved = localStorage.getItem(LOCALE_KEY)
  if (saved === 'ru' || saved === 'en') return saved
  return navigator.language?.toLowerCase().startsWith('ru') ? 'ru' : 'en'
}

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale(),
  fallbackLocale: 'ru',
  messages: { ru, en },
  pluralRules: { ru: russianPlural },
})

export function setLocale(locale: Locale) {
  i18n.global.locale.value = locale
  localStorage.setItem(LOCALE_KEY, locale)
  document.documentElement.lang = locale
}

document.documentElement.lang = i18n.global.locale.value
