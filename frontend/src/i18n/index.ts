import { createI18n } from 'vue-i18n'
import ru from './ru'
import en from './en'

export type Locale = 'ru' | 'en'

const LOCALE_KEY = 'wm-locale'

function initialLocale(): Locale {
  const saved = localStorage.getItem(LOCALE_KEY)
  if (saved === 'ru' || saved === 'en') return saved
  return navigator.language?.toLowerCase().startsWith('ru') ? 'ru' : 'en'
}

/** Russian plural forms: "no | one | few | many". */
function russianPlural(choice: number, choicesLength: number): number {
  if (choice === 0) return 0
  if (choicesLength < 4) return choice === 1 ? 1 : 2
  const mod10 = choice % 10
  const mod100 = choice % 100
  if (mod10 === 1 && mod100 !== 11) return 1
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return 2
  return 3
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
