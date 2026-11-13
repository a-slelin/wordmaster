import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@fontsource-variable/manrope'
import '@fontsource/unbounded/500.css'
import '@fontsource/unbounded/600.css'
import '@fontsource/unbounded/700.css'
import './styles/tokens.css'
import './styles/base.css'
import './styles/components.css'
import App from './App.vue'
import { router } from './router'
import { i18n } from './i18n'
import { onUnauthorized } from './api'
import { useAuthStore } from './stores/auth'
import { useUiStore } from './stores/ui'

const app = createApp(App)
app.use(createPinia())
app.use(i18n)
app.use(router)

onUnauthorized(() => {
  useAuthStore().reset()
  useUiStore().toast(i18n.global.t('auth.sessionExpired'), 'info')
  const current = router.currentRoute.value
  if (current.meta.auth === true) {
    router.push({ name: 'login', query: { redirect: current.fullPath } })
  }
})

app.mount('#app')
