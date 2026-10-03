import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'system' | 'light' | 'dark'
export type ToastKind = 'success' | 'error' | 'info' | 'xp'

export interface Toast {
  id: number
  kind: ToastKind
  text: string
  icon?: string
}

export interface ConfirmRequest {
  title: string
  text?: string
  confirmText?: string
  danger?: boolean
  resolve: (value: boolean) => void
}

const THEME_KEY = 'wm-theme'
const media = window.matchMedia('(prefers-color-scheme: dark)')

export const useUiStore = defineStore('ui', () => {
  const theme = ref<ThemeMode>((localStorage.getItem(THEME_KEY) as ThemeMode) || 'system')
  const toasts = ref<Toast[]>([])
  const confirmRequest = ref<ConfirmRequest | null>(null)
  let nextId = 1

  function applyTheme() {
    const dark = theme.value === 'dark' || (theme.value === 'system' && media.matches)
    document.documentElement.dataset.theme = dark ? 'dark' : 'light'
  }

  watch(theme, (value) => {
    localStorage.setItem(THEME_KEY, value)
    applyTheme()
  })
  media.addEventListener('change', applyTheme)
  applyTheme()

  function cycleTheme() {
    const isDark = document.documentElement.dataset.theme === 'dark'
    theme.value = isDark ? 'light' : 'dark'
  }

  function toast(text: string, kind: ToastKind = 'info', icon?: string, timeout = 3200) {
    const id = nextId++
    toasts.value.push({ id, kind, text, icon })
    setTimeout(() => dismiss(id), timeout)
  }

  function dismiss(id: number) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  function confirm(options: Omit<ConfirmRequest, 'resolve'>): Promise<boolean> {
    return new Promise((resolve) => {
      confirmRequest.value = { ...options, resolve }
    })
  }

  function resolveConfirm(value: boolean) {
    confirmRequest.value?.resolve(value)
    confirmRequest.value = null
  }

  return { theme, toasts, confirmRequest, cycleTheme, toast, dismiss, confirm, resolveConfirm }
})
