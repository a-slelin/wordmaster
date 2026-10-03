import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi, tokens, userApi, type AuthResponse, type User } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const ready = ref(false)

  const isAuthenticated = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'admin')

  function apply(auth: AuthResponse) {
    tokens.save(auth)
    user.value = auth.user
  }

  /** Restores the session from saved tokens on application start. */
  async function init() {
    if (ready.value) return
    try {
      if (tokens.access || tokens.refresh) {
        user.value = await userApi.me()
      }
    } catch {
      tokens.clear()
      user.value = null
    } finally {
      ready.value = true
    }
  }

  async function login(usernameOrEmail: string, password: string) {
    apply(await authApi.login({ usernameOrEmail, password }))
  }

  async function register(username: string, email: string, password: string) {
    apply(await authApi.register({ username, email, password }))
  }

  async function logout() {
    const refresh = tokens.refresh
    tokens.clear()
    user.value = null
    if (refresh) {
      authApi.logout(refresh).catch(() => undefined)
    }
  }

  function reset() {
    tokens.clear()
    user.value = null
  }

  return { user, ready, isAuthenticated, isAdmin, init, login, register, logout, reset }
})
