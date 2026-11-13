import { usePrefsStore } from '@/stores/prefs'

const LOCALES: Record<string, string> = {
  en: 'en-US',
  ru: 'ru-RU',
  es: 'es-ES',
  de: 'de-DE',
  fr: 'fr-FR',
  it: 'it-IT',
  pt: 'pt-PT',
  tr: 'tr-TR',
  zh: 'zh-CN',
  ja: 'ja-JP',
  ko: 'ko-KR',
  pl: 'pl-PL',
}

export const speechSupported = typeof window !== 'undefined' && 'speechSynthesis' in window

let voices: SpeechSynthesisVoice[] = []
if (speechSupported) {
  const load = () => (voices = window.speechSynthesis.getVoices())
  load()
  window.speechSynthesis.addEventListener?.('voiceschanged', load)
}

function pickVoice(lang: string): SpeechSynthesisVoice | undefined {
  const exact = voices.filter((v) => v.lang.replace('_', '-').toLowerCase() === lang.toLowerCase())
  const sameLanguage = exact.length ? exact : voices.filter((v) => v.lang.toLowerCase().startsWith(lang.slice(0, 2)))
  // Prefer natural/online voices when the browser provides them.
  return sameLanguage.find((v) => /natural|google|premium|enhanced/i.test(v.name)) ?? sameLanguage[0]
}

/** Text-to-speech through the Web Speech API (free, works offline with system voices). */
export function useSpeech() {
  const prefs = usePrefsStore()

  function speak(text: string, languageCode = 'en', slow = false) {
    if (!speechSupported || !text) return
    const synth = window.speechSynthesis
    synth.cancel()
    const utterance = new SpeechSynthesisUtterance(text.replace(/[–—]/g, ','))
    const lang = LOCALES[languageCode] ?? languageCode
    utterance.lang = lang
    const voice = pickVoice(lang)
    if (voice) utterance.voice = voice
    utterance.rate = Math.max(0.5, Math.min(1.5, prefs.speechRate * (slow ? 0.6 : 1)))
    synth.speak(utterance)
  }

  function stop() {
    if (speechSupported) window.speechSynthesis.cancel()
  }

  return { speak, stop, supported: speechSupported }
}
