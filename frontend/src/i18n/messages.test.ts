import { describe, expect, it } from 'vitest'
import { createI18n } from 'vue-i18n'
import en from './en'
import ru from './ru'
import { russianPlural } from './plural'

type Messages = Record<string, unknown>

/** All "a.b.c" keys of string messages. */
function keys(messages: Messages, prefix = ''): string[] {
  return Object.entries(messages).flatMap(([key, value]) => {
    const path = prefix ? `${prefix}.${key}` : key
    return value && typeof value === 'object' && !Array.isArray(value) ? keys(value as Messages, path) : [path]
  })
}

function get(messages: Messages, path: string): unknown {
  return path.split('.').reduce<unknown>((node, key) => (node as Messages | undefined)?.[key], messages)
}

describe('i18n messages', () => {
  // The message schema is too deep for the type checker here, and the test needs none of it.
  const create = createI18n as unknown as (options: object) => { global: { t: (...args: unknown[]) => unknown } }
  const i18n = create({
    legacy: false,
    locale: 'ru',
    messages: { ru, en },
    pluralRules: { ru: russianPlural },
    missingWarn: false,
    fallbackWarn: false,
  })
  const t = i18n.global.t

  it('has the same keys in both languages', () => {
    expect(keys(en as Messages).sort()).toEqual(keys(ru as Messages).sort())
  })

  // A message with an unescaped "|", "{" or "@" breaks the whole screen it is used on.
  it.each(['ru', 'en'])('every %s message is rendered to a non-empty string', (locale) => {
    const messages = (locale === 'ru' ? ru : en) as Messages
    const broken: string[] = []
    for (const key of keys(messages)) {
      if (typeof get(messages, key) !== 'string') continue
      for (const n of [0, 1, 2, 5, 21]) {
        try {
          const text = t(key, { n, total: n, title: 'x', word: 'x', name: 'x', count: n, pipe: '|' }, { locale })
          if (typeof text !== 'string' || !text.trim()) broken.push(`${key} (n = ${n})`)
        } catch {
          broken.push(`${key} (n = ${n})`)
        }
      }
    }
    expect(broken).toEqual([])
  })

  it('declines Russian nouns', () => {
    const cards = (n: number) => t('common.cards', n)
    expect([1, 2, 5, 11, 21, 22, 25, 111].map(cards)).toEqual([
      '1 карточка', '2 карточки', '5 карточек', '11 карточек', '21 карточка', '22 карточки', '25 карточек', '111 карточек',
    ])
  })
})
