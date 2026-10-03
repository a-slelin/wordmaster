import { describe, expect, it } from 'vitest'
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
})
