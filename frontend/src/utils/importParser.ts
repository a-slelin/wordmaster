import type { CardPayload } from '@/api'

export interface ParseResult {
  cards: CardPayload[]
  skipped: number
}

export const MAX_IMPORT_CARDS = 1000

/** In order of priority: on a tie the earlier separator wins, so a comma is used only for real CSV. */
const SEPARATORS = ['\t', ' | ', '|', ';', ' = ', '=', ' — ', ' – ', ' - ', ',']

/** Separators of spreadsheet exports: a field may be wrapped in double quotes. */
const QUOTABLE = new Set(['\t', ';', ','])

const HEADER_WORDS = new Set(['word', 'term', 'front', 'слово', 'термин'])

function detectSeparator(lines: string[]): string | null {
  let best: string | null = null
  let bestScore = 0
  for (const sep of SEPARATORS) {
    const score = lines.filter((l) => l.includes(sep)).length
    if (score > bestScore) {
      best = sep
      bestScore = score
    }
  }
  return best
}

/** Splits a CSV line: "a,""b""",c -> [a, "b", c]. */
function splitQuoted(line: string, separator: string): string[] {
  const parts: string[] = []
  let current = ''
  let quoted = false

  for (let i = 0; i < line.length; i++) {
    const char = line[i]
    if (quoted) {
      if (char === '"' && line[i + 1] === '"') {
        current += '"'
        i++
      } else if (char === '"') {
        quoted = false
      } else {
        current += char
      }
    } else if (char === '"' && current.trim() === '') {
      quoted = true
      current = ''
    } else if (char === separator) {
      parts.push(current)
      current = ''
    } else {
      current += char
    }
  }

  parts.push(current)
  return parts
}

function splitLine(line: string, separator: string): string[] {
  const parts = QUOTABLE.has(separator) && line.includes('"') ? splitQuoted(line, separator) : line.split(separator)
  return parts.map((p) => p.trim())
}

/**
 * Parses a pasted list or a CSV/TSV file: "word<sep>translation[<sep>transcription[<sep>example]]" per line.
 * A header line ("word,translation,...") and lines starting with "#" are ignored.
 */
export function parseImport(text: string): ParseResult {
  const lines = text
    .replace(/^﻿/, '')
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.length > 0 && !l.startsWith('#'))

  const separator = detectSeparator(lines)
  const cards: CardPayload[] = []
  let skipped = 0

  lines.forEach((line, index) => {
    if (!separator || !line.includes(separator)) {
      skipped++
      return
    }
    const [word, translation, transcription, ...example] = splitLine(line, separator)
    if (index === 0 && HEADER_WORDS.has(word.toLowerCase())) return
    if (!word || !translation || word.length > 255 || translation.length > 255) {
      skipped++
      return
    }
    cards.push({
      word,
      translation,
      transcription: transcription && transcription.length <= 255 ? transcription : undefined,
      exampleSentence: example.filter(Boolean).join(separator === ',' ? ', ' : separator).trim().slice(0, 2000) || undefined,
    })
  })

  skipped += Math.max(0, cards.length - MAX_IMPORT_CARDS)
  return { cards: cards.slice(0, MAX_IMPORT_CARDS), skipped }
}
