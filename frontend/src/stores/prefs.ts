import { defineStore } from 'pinia'
import { useLocalStorage } from '@vueuse/core'

/** Per-device learning preferences. */
export const usePrefsStore = defineStore('prefs', () => {
  const autoplay = useLocalStorage('wm-autoplay', true)
  const speechRate = useLocalStorage('wm-speech-rate', 0.95)
  const trainingMode = useLocalStorage<'flashcards' | 'typing' | 'choice' | 'listening'>('wm-mode', 'flashcards')
  const trainingDirection = useLocalStorage<'forward' | 'reverse'>('wm-direction', 'forward')
  const trainingScope = useLocalStorage<'smart' | 'all' | 'hard'>('wm-scope', 'smart')
  const trainingLimit = useLocalStorage('wm-limit', 20)

  return { autoplay, speechRate, trainingMode, trainingDirection, trainingScope, trainingLimit }
})
