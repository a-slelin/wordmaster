<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Eye, EyeOff, Lock, Mail, User } from 'lucide-vue-next'
import AppLogo from '@/components/AppLogo.vue'
import LocaleSwitch from '@/components/LocaleSwitch.vue'
import { ApiError } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'

const props = defineProps<{ mode: 'login' | 'register' }>()

const { t } = useI18n()
const auth = useAuthStore()
const ui = useUiStore()
const route = useRoute()
const router = useRouter()

const form = reactive({ login: '', username: '', email: '', password: '' })
const errors = ref<Record<string, string>>({})
const loading = ref(false)
const showPassword = ref(false)
const isLogin = computed(() => props.mode === 'login')

const chips = ['hello 👋', 'bonjour', 'hola', 'привет', 'ciao', 'hallo', 'こんにちは', '안녕', 'olá', 'merhaba']

function validate() {
  const e: Record<string, string> = {}
  if (isLogin.value) {
    if (!form.login.trim()) e.login = t('auth.usernameOrEmail')
  } else {
    if (!/^[A-Za-z0-9._-]{3,50}$/.test(form.username)) e.username = t('auth.usernameRules')
    if (!/^\S+@\S+\.\S+$/.test(form.email)) e.email = t('auth.email')
  }
  if (form.password.length < (isLogin.value ? 1 : 8)) e.password = t('auth.passwordHint')
  errors.value = e
  return Object.keys(e).length === 0
}

async function submit() {
  if (!validate()) return
  loading.value = true
  try {
    if (isLogin.value) {
      await auth.login(form.login.trim(), form.password)
    } else {
      await auth.register(form.username.trim(), form.email.trim(), form.password)
      ui.toast(`${t('auth.registerTitle')} ✓`, 'success', '🎉')
    }
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/home'
    router.replace(redirect)
  } catch (e) {
    if (e instanceof ApiError) {
      if (e.status === 401) errors.value = { password: t('auth.invalidCredentials') }
      else if (e.status === 409) {
        const field = (e.body?.details?.field as string) ?? ''
        errors.value = field === 'email' ? { email: t('auth.emailTaken') } : { username: t('auth.usernameTaken') }
      } else if (e.status === 0) ui.toast(t('common.networkError'), 'error')
      else errors.value = { ...e.fieldErrors, ...(Object.keys(e.fieldErrors).length ? {} : { password: e.message }) }
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth">
    <section class="art">
      <RouterLink to="/" class="art-logo"><AppLogo /></RouterLink>
      <div class="chips" aria-hidden="true">
        <span v-for="(chip, i) in chips" :key="chip" class="chip-word"
              :style="{ '--i': i, left: `${6 + ((i * 37) % 64)}%`, top: `${4 + ((i * 53) % 84)}%` }">{{ chip }}</span>
      </div>
      <div class="art-text">
        <h2>{{ $t('auth.artTitle') }}</h2>
        <p>{{ $t('auth.artText') }}</p>
      </div>
    </section>

    <section class="side">
      <div class="side-top">
        <RouterLink to="/" class="mobile-logo"><AppLogo /></RouterLink>
        <span class="spacer" />
        <LocaleSwitch />
      </div>

      <form class="card glass" novalidate @submit.prevent="submit">
        <div class="stack head">
          <h1>{{ isLogin ? $t('auth.loginTitle') : $t('auth.registerTitle') }}</h1>
          <p class="muted">{{ isLogin ? $t('auth.loginSubtitle') : $t('auth.registerSubtitle') }}</p>
        </div>

        <template v-if="isLogin">
          <div class="field">
            <label for="login">{{ $t('auth.usernameOrEmail') }}</label>
            <div class="input-icon">
              <User :size="18" />
              <input id="login" v-model="form.login" class="input" :class="{ 'is-invalid': errors.login }"
                     autocomplete="username" autofocus />
            </div>
          </div>
        </template>
        <template v-else>
          <div class="field">
            <label for="username">{{ $t('auth.username') }}</label>
            <div class="input-icon">
              <User :size="18" />
              <input id="username" v-model="form.username" class="input" :class="{ 'is-invalid': errors.username }"
                     autocomplete="username" maxlength="50" autofocus />
            </div>
            <span v-if="errors.username" class="field-error">{{ errors.username }}</span>
          </div>
          <div class="field">
            <label for="email">{{ $t('auth.email') }}</label>
            <div class="input-icon">
              <Mail :size="18" />
              <input id="email" v-model="form.email" type="email" class="input" :class="{ 'is-invalid': errors.email }"
                     autocomplete="email" maxlength="50" />
            </div>
            <span v-if="errors.email" class="field-error">{{ errors.email }}</span>
          </div>
        </template>

        <div class="field">
          <label for="password">{{ $t('auth.password') }}</label>
          <div class="input-icon pw">
            <Lock :size="18" />
            <input id="password" v-model="form.password" :type="showPassword ? 'text' : 'password'" class="input"
                   :class="{ 'is-invalid': errors.password }" :autocomplete="isLogin ? 'current-password' : 'new-password'"
                   maxlength="72" />
            <button type="button" class="eye" :aria-label="$t('auth.password')" @click="showPassword = !showPassword">
              <EyeOff v-if="showPassword" :size="18" />
              <Eye v-else :size="18" />
            </button>
          </div>
          <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
          <span v-else-if="!isLogin" class="tiny subtle">{{ $t('auth.passwordHint') }}</span>
        </div>

        <button class="btn btn-primary btn-lg btn-block" type="submit" :disabled="loading">
          {{ isLogin ? $t('auth.loginButton') : $t('auth.registerButton') }}
        </button>

        <p class="center muted small">
          {{ isLogin ? $t('auth.noAccount') : $t('auth.hasAccount') }}
          <RouterLink :to="{ path: isLogin ? '/register' : '/login', query: route.query }" class="bold">
            {{ isLogin ? $t('nav.register') : $t('nav.login') }}
          </RouterLink>
        </p>
      </form>
    </section>
  </div>
</template>

<style scoped>
.auth {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.art {
  position: relative;
  margin: 16px;
  border-radius: var(--radius-xl);
  background: var(--grad-brand);
  color: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 32px;
  box-shadow: var(--shadow-glow);
}

.art-logo :deep(.logo) {
  color: #fff;
}

.art-logo :deep(.gradient-text) {
  background: none;
  color: #fff;
}

.chips {
  position: relative;
  flex: 1;
}

.chip-word {
  position: absolute;
  padding: 10px 18px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  font-weight: 800;
  font-size: 1.05rem;
  animation: float 6s ease-in-out infinite;
  animation-delay: calc(var(--i) * -0.7s);
  --r: calc((var(--i) - 5) * 1.5deg);
}

.art-text {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 440px;
}

.art-text p {
  opacity: 0.85;
}

.side {
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.side-top {
  display: flex;
  align-items: center;
}

.mobile-logo {
  display: none;
}

.card {
  margin: auto;
  width: min(440px, 100%);
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 34px;
  border-radius: var(--radius-xl);
  animation: stagger-in 0.6s var(--ease-out) both;
}

.head {
  gap: 6px;
  margin-bottom: 6px;
}

.pw .input {
  padding-right: 48px;
}

.eye {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: var(--text-subtle);
}

.eye:hover {
  background: var(--surface-hover);
}

@media (max-width: 900px) {
  .auth {
    grid-template-columns: 1fr;
  }

  .art {
    display: none;
  }

  .mobile-logo {
    display: block;
  }

  .card {
    padding: 26px 20px;
  }
}
</style>
