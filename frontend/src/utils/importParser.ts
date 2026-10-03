import type { CardPayload } from '@/api'

export interface ParseResult {
  cards: CardPayload[]
  skipped: number
}

const SEPARATORS = ['\t', ' | ', '|', ';', ' = ', '=', ' — ', ' – ', ' - ']

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

/**
 * Parses a pasted list: "word<sep>translation[<sep>transcription[<sep>example]]" per line.
 */
export function parseImport(text: string): ParseResult {
  const lines = text
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l.length > 0 && !l.startsWith('#'))

  const separator = detectSeparator(lines)
  const cards: CardPayload[] = []
  let skipped = 0

  for (const line of lines) {
    if (!separator || !line.includes(separator)) {
      skipped++
      continue
    }
    const [word, translation, transcription, ...example] = line.split(separator).map((p) => p.trim())
    if (!word || !translation || word.length > 255 || translation.length > 255) {
      skipped++
      continue
    }
    cards.push({
      word,
      translation,
      transcription: transcription || undefined,
      exampleSentence: example.join(separator).trim() || undefined,
    })
  }

  return { cards: cards.slice(0, 1000), skipped }
}
