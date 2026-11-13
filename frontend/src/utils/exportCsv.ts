export interface ExportableCard {
  word: string
  translation: string
  transcription?: string | null
  exampleSentence?: string | null
}

function cell(value: string | null | undefined): string {
  const text = (value ?? '').replace(/\r?\n/g, ' ').trim()
  return /[",;\t]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text
}

/** CSV that opens in Excel / Google Sheets and can be imported back into WordMaster. */
export function cardsToCsv(cards: ExportableCard[]): string {
  const rows = [['word', 'translation', 'transcription', 'example']]
  for (const card of cards) {
    rows.push([card.word, card.translation, card.transcription ?? '', card.exampleSentence ?? ''])
  }
  return rows.map((row) => row.map(cell).join(',')).join('\r\n') + '\r\n'
}

/** A file name without characters forbidden by file systems. */
export function safeFileName(title: string, extension: string): string {
  const base = title.replace(/[\\/:*?"<>|\u0000-\u001F]+/g, ' ').replace(/\s+/g, ' ').trim().slice(0, 80)
  return `${base || 'deck'}.${extension}`
}

export function downloadText(fileName: string, text: string, mime = 'text/csv') {
  // The BOM makes Excel read the file as UTF-8.
  const blob = new Blob(['﻿', text], { type: `${mime};charset=utf-8` })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
