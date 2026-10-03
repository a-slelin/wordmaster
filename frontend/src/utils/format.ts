/** Backend sends LocalDateTime as "dd.MM.yyyy HH:mm:ss" and LocalDate as ISO "yyyy-MM-dd". */
export function parseDate(value?: string | null): Date | null {
  if (!value) return null
  const match = /^(\d{2})\.(\d{2})\.(\d{4})(?: (\d{2}):(\d{2}):(\d{2}))?$/.exec(value)
  if (match) {
    const [, d, m, y, hh = '0', mm = '0', ss = '0'] = match
    return new Date(+y, +m - 1, +d, +hh, +mm, +ss)
  }
  const iso = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  if (iso) return new Date(+iso[1], +iso[2] - 1, +iso[3])
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

export function formatDate(value: string | null | undefined, locale: string, options?: Intl.DateTimeFormatOptions) {
  const date = parseDate(value)
  if (!date) return ''
  return date.toLocaleDateString(locale, options ?? { day: 'numeric', month: 'long', year: 'numeric' })
}

/** "через 3 дня" / "in 3 days" relative to now. */
export function formatRelative(value: string | null | undefined, locale: string) {
  const date = parseDate(value)
  if (!date) return ''
  const diff = date.getTime() - Date.now()
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' })
  const minutes = Math.round(diff / 60000)
  if (Math.abs(minutes) < 60) return rtf.format(minutes, 'minute')
  const hours = Math.round(minutes / 60)
  if (Math.abs(hours) < 24) return rtf.format(hours, 'hour')
  const days = Math.round(hours / 24)
  if (Math.abs(days) < 30) return rtf.format(days, 'day')
  const months = Math.round(days / 30)
  return rtf.format(months, 'month')
}

export function formatDuration(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}:${String(s).padStart(2, '0')}`
}

export function languageName(code: string, locale: string, fallback?: string) {
  try {
    const names = new Intl.DisplayNames([locale], { type: 'language' })
    const name = names.of(code)
    if (name) return name.charAt(0).toUpperCase() + name.slice(1)
  } catch {
    // ignore
  }
  return fallback ?? code
}

export function isoDate(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
