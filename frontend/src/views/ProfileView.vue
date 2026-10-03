<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { KeyRound, Monitor, Moon, Palette, Sun, Target, Trash2, UserRound, Volume2 } from 'lucide-vue-next'
import { ApiError, userApi } from '@/api'
import { setLocale, type Locale } from '@/i18n'
import { useAuthStore } from '@/stores/auth'
import { usePrefsStore } from '@/stores/prefs'
import { useStatsStore } from '@/stores/stats'
import { useUiStore, type ThemeMode } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'
import { useSpeech } from '@/composables/useSpeech'
import { formatDate } from '@/utils/format'

const { t, locale } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const prefs = usePrefsStore()
const stats = useStatsStore()
const ui = useUiStore()
const showError = useErrorToast()
const { speak, supported } = useSpeech()

const account = reactive({ username: auth.user?.username ?? '', email: auth.user?.email ?? '' })
const passwords = reactive({ oldPassword: '', newPassword: '' })
const goal = ref(stats.overview?.dailyGoal ?? 20)
const savingAccount = ref(false)
const savingPassword = ref(false)
const accountErrors = ref<Record<string, string>>({})

const goalPresets = [
  { value: 10, key: 'casual', emoji: '🐢' },
  { value: 20, key: 'regular', emoji: '🚶' },
  { value: 50, key: 'serious', emoji: '🏃' },
  { value: 100, key: 'insane', emoji: '🚀' },
]
const themes: { value: ThemeMode; icon: unknown }[] = [
  { value: 'system', icon: Monitor },
  { value: 'light', icon: Sun },
  { value: 'dark', icon: Moon },
]

const initial = computed(() => (auth.user?.username ?? '?').charAt(0).toUpperCase())

async function saveAccount() {
  savingAccount.value = true
  accountErrors.value = {}
  try {
    auth.user = await userApi.update({ username: account.username.trim(), email: account.email.trim() })
    ui.toast(t('common.saved'), 'success')
  } catch (e) {
    if (e instanceof ApiError) {
      accountErrors.value = e.fieldErrors
      if (e.status === 409) {
        const field = e.body?.details?.field as string
        accountErrors.value = { [field]: field === 'email' ? t('auth.emailTaken') : t('auth.usernameTaken') }
      }
    }
    showError(e)
  } finally {
    savingAccount.value = false
  }
}

async function changePassword() {
  savingPassword.value = true
  try {
    await userApi.changePassword({ ...passwords })
    passwords.oldPassword = ''
    passwords.newPassword = ''
    ui.toast(t('profile.passwordChanged'), 'success')
  } catch (e) {
    if (e instanceof ApiError && e.status === 422) ui.toast(t('profile.wrongPassword'), 'error')
    else showError(e)
  } finally {
    savingPassword.value = false
  }
}

async function saveGoal(value: number) {
  goal.value = value
  try {
    stats.overview = await userApi.updateSettings({ dailyGoal: value })
  } catch (e) {
    showError(e)
  }
}

async function deleteAccount() {
  const ok = await ui.confirm({ title: t('profile.deleteAccount'), text: t('profile.deleteConfirm'), danger: true, confirmText: t('common.delete') })
  if (!ok) return
  try {
    await userApi.deleteMe()
    auth.reset()
    stats.reset()
    ui.toast(t('profile.deleted'), 'info', '👋')
    router.replace('/')
  } catch (e) {
    showError(e)
  }
}

onMounted(async () => {
  const overview = stats.overview ?? (await stats.refresh())
  if (overview) goal.value = overview.dailyGoal
})
</script>

<template>
  <div class="page narrow">
    <section class="profile-head glass card-pad">
      <div class="avatar">{{ initial }}</div>
      <div class="stack" style="gap: 4px">
        <h1>{{ auth.user?.username }}</h1>
        <span class="muted">{{ auth.user?.email }}</span>
        <span class="tiny subtle">{{ $t('profile.memberSince', { date: formatDate(auth.user?.createdAt, locale) }) }}</span>
      </div>
      <span class="spacer" />
      <div v-if="stats.overview" class="row-wrap head-badges">
        <span class="badge badge-primary">{{ $t('common.level', { n: stats.overview.level }) }}</span>
        <span class="badge badge-warning">🔥 {{ stats.overview.currentStreak }}</span>
        <span class="badge badge-success">🧠 {{ stats.overview.knownWords }}</span>
      </div>
    </section>

    <section class="glass card-pad stack">
      <h2><Target :size="22" /> {{ $t('profile.dailyGoal') }}</h2>
      <p class="muted small">{{ $t('profile.dailyGoalHint') }}</p>
      <div class="presets">
        <button v-for="p in goalPresets" :key="p.value" type="button" class="preset" :class="{ active: goal === p.value }"
                @click="saveGoal(p.value)">
          <span class="preset-emoji">{{ p.emoji }}</span>
          <b>{{ $t(`profile.goals.${p.key}`) }}</b>
          <span class="tiny muted">{{ $t('common.cards', { n: p.value }, p.value) }}</span>
        </button>
      </div>
      <div class="row">
        <input v-model.number="goal" type="range" min="5" max="200" step="5" class="grow" @change="saveGoal(goal)" />
        <b class="goal-value">{{ goal }}</b>
      </div>
    </section>

    <section class="glass card-pad stack">
      <h2><Palette :size="22" /> {{ $t('profile.appearance') }}</h2>
      <div class="row-wrap">
        <button v-for="th in themes" :key="th.value" type="button" class="chip" :class="{ 'is-active': ui.theme === th.value }"
                @click="ui.theme = th.value">
          <component :is="th.icon" :size="16" /> {{ $t(`theme.${th.value}`) }}
        </button>
      </div>
      <span class="label">{{ $t('profile.interfaceLanguage') }}</span>
      <div class="row-wrap">
        <button v-for="l in (['ru', 'en'] as Locale[])" :key="l" type="button" class="chip" :class="{ 'is-active': locale === l }"
                @click="setLocale(l)">
          {{ l === 'ru' ? '🇷🇺 Русский' : '🇬🇧 English' }}
        </button>
      </div>
    </section>

    <section class="glass card-pad stack">
      <h2><Volume2 :size="22" /> {{ $t('profile.speech') }}</h2>
      <p v-if="!supported" class="muted">{{ $t('profile.speechUnsupported') }}</p>
      <template v-else>
        <label class="switch-row">
          <span class="grow bold">{{ $t('profile.autoplay') }}</span>
          <input v-model="prefs.autoplay" type="checkbox" class="switch" />
        </label>
        <span class="label">{{ $t('profile.speechRate') }}: {{ prefs.speechRate.toFixed(2) }}×</span>
        <div class="row">
          <input v-model.number="prefs.speechRate" type="range" min="0.5" max="1.4" step="0.05" class="grow" />
          <button class="btn btn-soft btn-sm" type="button" @click="speak('The quick brown fox jumps over the lazy dog', 'en')">
            {{ $t('profile.testVoice') }}
          </button>
        </div>
      </template>
    </section>

    <section class="glass card-pad stack">
      <h2><UserRound :size="22" /> {{ $t('profile.account') }}</h2>
      <form class="two" @submit.prevent="saveAccount">
        <div class="field">
          <label for="p-username">{{ $t('auth.username') }}</label>
          <input id="p-username" v-model="account.username" class="input" :class="{ 'is-invalid': accountErrors.username }" maxlength="50" />
          <span v-if="accountErrors.username" class="field-error">{{ accountErrors.username }}</span>
        </div>
        <div class="field">
          <label for="p-email">{{ $t('auth.email') }}</label>
          <input id="p-email" v-model="account.email" type="email" class="input" :class="{ 'is-invalid': accountErrors.email }" maxlength="50" />
          <span v-if="accountErrors.email" class="field-error">{{ accountErrors.email }}</span>
        </div>
        <button class="btn btn-primary save" type="submit" :disabled="savingAccount">{{ $t('common.save') }}</button>
      </form>
    </section>

    <section class="glass card-pad stack">
      <h2><KeyRound :size="22" /> {{ $t('profile.password') }}</h2>
      <form class="two" @submit.prevent="changePassword">
        <div class="field">
          <label for="p-old">{{ $t('profile.oldPassword') }}</label>
          <input id="p-old" v-model="passwords.oldPassword" type="password" class="input" autocomplete="current-password" />
        </div>
        <div class="field">
          <label for="p-new">{{ $t('profile.newPassword') }}</label>
          <input id="p-new" v-model="passwords.newPassword" type="password" class="input" autocomplete="new-password" minlength="8" maxlength="72" />
        </div>
        <button class="btn btn-soft save" type="submit"
                :disabled="savingPassword || !passwords.oldPassword || passwords.newPassword.length < 8">
          {{ $t('profile.changePassword') }}
        </button>
      </form>
    </section>

    <section class="glass card-pad stack danger">
      <h2>{{ $t('profile.danger') }}</h2>
      <button class="btn btn-danger-ghost save" type="button" @click="deleteAccount">
        <Trash2 :size="18" /> {{ $t('profile.deleteAccount') }}
      </button>
    </section>
  </div>
</template>

<style scoped>
.narrow {
  max-width: 860px;
}

section h2 {
  display: flex;
  align-items: center;
  gap: 10px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.avatar {
  display: grid;
  place-items: center;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: var(--grad-brand);
  color: #fff;
  font-family: var(--font-display);
  font-size: 2.2rem;
  font-weight: 700;
  box-shadow: var(--shadow-glow);
}

.presets {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.preset {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 8px;
  border-radius: var(--radius-md);
  border: 2px solid var(--border);
  background: var(--surface-sunken);
  transition: all 0.25s var(--ease-spring);
}

.preset:hover {
  transform: translateY(-3px);
}

.preset.active {
  border-color: var(--primary);
  background: var(--primary-soft);
}

.preset-emoji {
  font-size: 1.8rem;
}

.goal-value {
  font-family: var(--font-display);
  font-size: 1.3rem;
  min-width: 48px;
  text-align: right;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
  cursor: pointer;
}

.two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.save {
  justify-self: start;
}

.danger h2 {
  color: var(--danger);
}

@media (max-width: 640px) {
  .presets {
    grid-template-columns: 1fr 1fr;
  }

  .two {
    grid-template-columns: 1fr;
  }
}
</style>
