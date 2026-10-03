import { defineStore } from 'pinia'
import { ref } from 'vue'
import { statsApi, type UserStats } from '@/api'

/** Gamification summary shown in the shell (streak, XP, daily goal). */
export const useStatsStore = defineStore('stats', () => {
  const overview = ref<UserStats | null>(null)

  async function refresh() {
    try {
      overview.value = await statsApi.overview()
    } catch {
      // the shell works without stats
    }
    return overview.value
  }

  function reset() {
    overview.value = null
  }

  return { overview, refresh, reset }
})
