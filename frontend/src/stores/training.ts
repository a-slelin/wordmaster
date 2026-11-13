import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { trainingApi, type SessionStart, type TrainingDirection, type TrainingMode, type TrainingScope, type Uuid } from '@/api'
import { usePrefsStore } from './prefs'

export interface StartOptions {
  mode?: TrainingMode
  direction?: TrainingDirection
  scope?: TrainingScope
  limit?: number
}

/** Keeps the just started session so the training screen does not need an extra request. */
export const useTrainingStore = defineStore('training', () => {
  const current = ref<SessionStart | null>(null)
  const router = useRouter()

  async function start(deckId: Uuid, options: StartOptions = {}) {
    const prefs = usePrefsStore()
    const session = await trainingApi.start({
      deckId,
      mode: options.mode ?? prefs.trainingMode,
      direction: options.direction ?? prefs.trainingDirection,
      scope: options.scope ?? prefs.trainingScope,
      limit: options.limit ?? prefs.trainingLimit,
    })
    current.value = session
    await router.push({ name: 'train', params: { sessionId: session.sessionId } })
    return session
  }

  function take(sessionId: Uuid): SessionStart | null {
    const session = current.value?.sessionId === sessionId ? current.value : null
    current.value = null
    return session
  }

  return { current, start, take }
})
