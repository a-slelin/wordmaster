import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { ApiError, authApi, tokens, userApi, type AuthResponse, type User } from '@/api'

const USER_KEY = 'wm-user'

function cachedUser(): User | null {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) ?? 'null') as User | null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const ready = ref(false)

  const isAuthenticated = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'admin')

  function setUser(value: User | null) {
    user.value = value
    if (value) localStorage.setItem(USER_KEY, JSON.stringify(value))
    else localStorage.removeItem(USER_KEY)
  }

  function apply(auth: AuthResponse) {
    tokens.save(auth)
    setUser(auth.user)
  }

  /** Restores the session from saved tokens on application start. */
  async function init() {
    if (ready.value) return
    try {
      if (tokens.access || tokens.refresh) {
        setUser(await userApi.me())
      }
    } catch (e) {
      if (e instanceof ApiError && e.status === 0 && tokens.refresh) {
        // The server is unreachable right now: keep the session and show the last known profile.
        user.value = cachedUser()
      } else {
        tokens.clear()
        setUser(null)
      }
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
    setUser(null)
    if (refresh) {
      authApi.logout(refresh).catch(() => undefined)
    }
  }

  function reset() {
    tokens.clear()
    setUser(null)
  }

  return { user, setUser, ready, isAuthenticated, isAdmin, init, login, register, logout, reset }
})
