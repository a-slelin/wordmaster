<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ChartColumn, Compass, House, Library, LogIn, LogOut, Moon, Shield, Sun, User } from 'lucide-vue-next'
import AppLogo from '@/components/AppLogo.vue'
import StreakFlame from '@/components/StreakFlame.vue'
import { useAuthStore } from '@/stores/auth'
import { useStatsStore } from '@/stores/stats'
import { useUiStore } from '@/stores/ui'

const auth = useAuthStore()
const stats = useStatsStore()
const ui = useUiStore()
const router = useRouter()
const { t } = useI18n()

const items = computed(() => {
  const list = [
    { to: '/home', label: t('nav.home'), icon: House, auth: true },
    { to: '/decks', label: t('nav.decks'), icon: Library, auth: true },
    { to: '/explore', label: t('nav.explore'), icon: Compass, auth: false },
    { to: '/stats', label: t('nav.stats'), icon: ChartColumn, auth: true },
    { to: '/profile', label: t('nav.profile'), icon: User, auth: true },
  ]
  return list.filter((i) => !i.auth || auth.isAuthenticated)
})

const goalPercent = computed(() => {
  const o = stats.overview
  if (!o || o.dailyGoal === 0) return 0
  return Math.min(100, Math.round((o.todayReviewed / o.dailyGoal) * 100))
})

const isDark = computed(() => ui.theme === 'dark' || (ui.theme === 'system' && document.documentElement.dataset.theme === 'dark'))

async function logout() {
  await auth.logout()
  stats.reset()
  router.push('/')
}

onMounted(() => {
  if (auth.isAuthenticated && !stats.overview) stats.refresh()
})

watch(
  () => auth.isAuthenticated,
  (value) => value && stats.refresh(),
)
</script>

<template>
  <div class="shell">
    <aside class="sidebar glass">
      <RouterLink :to="auth.isAuthenticated ? '/home' : '/'" class="brand">
        <AppLogo />
      </RouterLink>

      <nav class="nav">
        <RouterLink v-for="item in items" :key="item.to" :to="item.to" class="nav-item">
          <component :is="item.icon" :size="21" />
          <span>{{ item.label }}</span>
        </RouterLink>
        <RouterLink v-if="auth.isAdmin" to="/admin" class="nav-item">
          <Shield :size="21" />
          <span>{{ $t('nav.admin') }}</span>
        </RouterLink>
      </nav>

      <div class="spacer" />

      <div v-if="auth.isAuthenticated && stats.overview" class="mini glass">
        <div class="mini-row">
          <StreakFlame :days="stats.overview.currentStreak" :active="stats.overview.activeToday" />
          <span class="badge badge-primary">{{ $t('common.level', { n: stats.overview.level }) }}</span>
        </div>
        <div class="mini-goal">
          <div class="row small">
            <span class="muted bold">{{ $t('dashboard.dailyGoal') }}</span>
            <span class="spacer" />
            <span class="bold">{{ stats.overview.todayReviewed }}/{{ stats.overview.dailyGoal }}</span>
          </div>
          <div class="progress warm"><span :style="{ width: `${goalPercent}%` }" /></div>
        </div>
      </div>

      <div class="foot">
        <button class="btn btn-ghost btn-icon" type="button" :title="$t('nav.theme')" @click="ui.cycleTheme()">
          <Sun v-if="isDark" :size="20" />
          <Moon v-else :size="20" />
        </button>
        <button v-if="auth.isAuthenticated" class="btn btn-ghost grow" type="button" @click="logout">
          <LogOut :size="18" />
          {{ $t('nav.logout') }}
        </button>
        <RouterLink v-else to="/login" class="btn btn-primary grow">
          <LogIn :size="18" />
          {{ $t('nav.login') }}
        </RouterLink>
      </div>
    </aside>

    <header class="topbar glass">
      <RouterLink :to="auth.isAuthenticated ? '/home' : '/'"><AppLogo /></RouterLink>
      <span class="spacer" />
      <StreakFlame v-if="stats.overview" :days="stats.overview.currentStreak" :active="stats.overview.activeToday" :size="24" />
      <button class="btn btn-ghost btn-icon btn-sm" type="button" @click="ui.cycleTheme()">
        <Sun v-if="isDark" :size="18" />
        <Moon v-else :size="18" />
      </button>
      <RouterLink v-if="!auth.isAuthenticated" to="/login" class="btn btn-primary btn-sm">{{ $t('nav.login') }}</RouterLink>
    </header>

    <main class="main">
      <slot />
    </main>

    <nav v-if="auth.isAuthenticated" class="tabbar glass">
      <RouterLink v-for="item in items" :key="item.to" :to="item.to" class="tab">
        <component :is="item.icon" :size="22" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100dvh;
  display: grid;
  grid-template-columns: var(--sidebar-width) 1fr;
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 22px 16px;
  border-radius: 0 var(--radius-xl) var(--radius-xl) 0;
  border-left: none;
}

.brand {
  padding: 4px 10px 18px;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  height: 48px;
  padding: 0 14px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  font-weight: 700;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s var(--ease-out);
}

.nav-item:hover {
  background: var(--surface-hover);
  color: var(--text);
  transform: translateX(2px);
}

.nav-item.router-link-active {
  color: var(--primary);
  background: var(--primary-soft);
}

.nav-item.router-link-active svg {
  filter: drop-shadow(0 3px 8px rgba(124, 92, 255, 0.5));
}

.mini {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px;
  border-radius: var(--radius-md);
  box-shadow: none;
}

.mini-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mini-goal {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.foot {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.main {
  min-width: 0;
}

.topbar,
.tabbar {
  display: none;
}

@media (max-width: 860px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: none;
  }

  .topbar {
    position: sticky;
    top: 0;
    z-index: 50;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    padding-top: max(10px, env(safe-area-inset-top));
    border-radius: 0 0 var(--radius-lg) var(--radius-lg);
    border-top: none;
  }

  .tabbar {
    position: fixed;
    z-index: 50;
    left: 10px;
    right: 10px;
    bottom: max(10px, env(safe-area-inset-bottom));
    height: 64px;
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 0 6px;
    border-radius: var(--radius-lg);
    background: var(--surface-strong);
  }

  .tab {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    padding: 6px 0;
    border-radius: var(--radius-sm);
    color: var(--text-subtle);
    font-size: 0.66rem;
    font-weight: 800;
    transition: color 0.2s ease, transform 0.3s var(--ease-spring);
  }

  .tab.router-link-active {
    color: var(--primary);
    transform: translateY(-2px);
  }
}
</style>
