import { defineStore } from 'pinia'
import { ref } from 'vue'
import { statsApi, userApi, type UserStats } from '@/api'

/** Gamification summary shown in the shell (streak, XP, daily goal). */
export const useStatsStore = defineStore('stats', () => {
  const overview = ref<UserStats | null>(null)
  let zoneSyncFailed = false

  async function refresh() {
    try {
      overview.value = await statsApi.overview()
      await syncTimeZone()
    } catch {
      // the shell works without stats
    }
    return overview.value
  }

  /**
   * Days (streak, daily goal, forecast) are counted on the server in the time zone of the user -
   * tell it the zone of this device when it differs (first visit, travelling).
   */
  async function syncTimeZone() {
    const zone = Intl.DateTimeFormat().resolvedOptions().timeZone
    if (!zone || !overview.value || overview.value.timeZone === zone || zoneSyncFailed) return
    try {
      overview.value = await userApi.updateSettings({ timeZone: zone })
    } catch {
      // A zone unknown to the server: keep its default and do not ask again in this session.
      zoneSyncFailed = true
    }
  }

  function reset() {
    overview.value = null
  }

  return { overview, refresh, reset }
})
