import { describe, expect, it } from 'vitest'
import { cardsToCsv, safeFileName } from './exportCsv'
import { parseImport } from './importParser'

describe('parseImport', () => {
  it('parses dash separated lines', () => {
    const result = parseImport('apple - яблоко\npear - груша')
    expect(result.cards).toEqual([
      { word: 'apple', translation: 'яблоко', transcription: undefined, exampleSentence: undefined },
      { word: 'pear', translation: 'груша', transcription: undefined, exampleSentence: undefined },
    ])
    expect(result.skipped).toBe(0)
  })

  it('detects tab separator and optional columns', () => {
    const result = parseImport('run\tбежать\t[rʌn]\tI run every day.')
    expect(result.cards[0]).toEqual({
      word: 'run',
      translation: 'бежать',
      transcription: '[rʌn]',
      exampleSentence: 'I run every day.',
    })
  })

  it('keeps hyphenated words when another separator is used', () => {
    const result = parseImport('well-known; известный\nmother-in-law; тёща')
    expect(result.cards.map((c) => c.word)).toEqual(['well-known', 'mother-in-law'])
  })

  it('skips broken lines and comments', () => {
    const result = parseImport('# my list\napple - яблоко\njust text\n - пусто')
    expect(result.cards).toHaveLength(1)
    expect(result.skipped).toBe(2)
  })

  it('reads CSV with quotes, a header and a BOM', () => {
    const csv = '\uFEFFword,translation,transcription,example\r\nbreak the ice,"растопить лёд, начать разговор",,"He said: ""Hi!"""\r\ncat,кошка,[kæt],'
    const result = parseImport(csv)
    expect(result.cards).toEqual([
      { word: 'break the ice', translation: 'растопить лёд, начать разговор', transcription: undefined, exampleSentence: 'He said: "Hi!"' },
      { word: 'cat', translation: 'кошка', transcription: '[kæt]', exampleSentence: undefined },
    ])
    expect(result.skipped).toBe(0)
  })

  it('prefers a dash to a comma inside translations', () => {
    const result = parseImport('go - идти, ехать\nbig - большой')
    expect(result.cards.map((c) => c.translation)).toEqual(['идти, ехать', 'большой'])
  })

  it('round-trips an exported deck', () => {
    const cards = [
      { word: 'be – was/were – been', translation: 'быть', transcription: '[biː]', exampleSentence: 'I have been here; twice.' },
      { word: 'say "cheese"', translation: 'скажи «сыр», улыбнись', transcription: undefined, exampleSentence: undefined },
    ]
    expect(parseImport(cardsToCsv(cards)).cards).toEqual(cards)
  })

  it('limits the number of cards', () => {
    const text = Array.from({ length: 1005 }, (_, i) => `w${i}\tп${i}`).join('\n')
    const result = parseImport(text)
    expect(result.cards).toHaveLength(1000)
    expect(result.skipped).toBe(5)
  })
})

describe('safeFileName', () => {
  it('removes forbidden characters', () => {
    expect(safeFileName('My: deck / "best"?', 'csv')).toBe('My deck best.csv')
    expect(safeFileName('///', 'csv')).toBe('deck.csv')
  })
})
