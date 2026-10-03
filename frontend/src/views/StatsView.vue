<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Award, Brain, CalendarDays, Crosshair, Flame, Layers, MessageCircle, TrendingUp } from 'lucide-vue-next'
import AchievementBadge from '@/components/AchievementBadge.vue'
import ActivityHeatmap from '@/components/ActivityHeatmap.vue'
import ForecastChart from '@/components/ForecastChart.vue'
import { statsApi, type Achievement, type ActivityDay, type ForecastDay, type HardWord, type UserStats } from '@/api'
import { useStatsStore } from '@/stores/stats'
import { useErrorToast } from '@/composables/useErrorToast'

const statsStore = useStatsStore()
const showError = useErrorToast()

const overview = ref<UserStats | null>(null)
const activity = ref<ActivityDay[]>([])
const forecast = ref<ForecastDay[]>([])
const achievements = ref<Achievement[]>([])
const hard = ref<HardWord[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [o, a, f, ach, h] = await Promise.all([
      statsStore.refresh(),
      statsApi.activity(182),
      statsApi.forecast(14),
      statsApi.achievements(),
      statsApi.hardWords(undefined, 12),
    ])
    overview.value = o
    activity.value = a
    forecast.value = f
    achievements.value = [...ach].sort((x, y) => Number(y.unlocked) - Number(x.unlocked))
    hard.value = h
  } catch (e) {
    showError(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page">
    <section class="stack" style="gap: 6px">
      <h1>📈 {{ $t('stats.title') }}</h1>
      <p class="muted">{{ $t('stats.subtitle') }}</p>
    </section>

    <div v-if="loading" class="grid">
      <div v-for="i in 6" :key="i" class="skeleton" style="height: 110px" />
    </div>

    <template v-else-if="overview">
      <section class="tiles stagger">
        <div class="tile glass" data-color="green">
          <span class="t-icon"><Brain :size="22" /></span>
          <b>{{ overview.knownWords }}</b><span>{{ $t('stats.knownWords') }}</span>
        </div>
        <div class="tile glass" data-color="amber">
          <span class="t-icon"><TrendingUp :size="22" /></span>
          <b>{{ overview.learningWords }}</b><span>{{ $t('stats.learningWords') }}</span>
        </div>
        <div class="tile glass" data-color="sky">
          <span class="t-icon"><Crosshair :size="22" /></span>
          <b>{{ overview.accuracyPercent }}%</b><span>{{ $t('stats.accuracy') }}</span>
        </div>
        <div class="tile glass" data-color="violet">
          <span class="t-icon"><Layers :size="22" /></span>
          <b>{{ overview.totalSessions }}</b><span>{{ $t('stats.sessions') }}</span>
        </div>
        <div class="tile glass" data-color="pink">
          <span class="t-icon"><MessageCircle :size="22" /></span>
          <b>{{ overview.totalAnswers }}</b><span>{{ $t('stats.answers') }}</span>
        </div>
        <div class="tile glass" data-color="orange">
          <span class="t-icon"><Flame :size="22" /></span>
          <b>{{ overview.longestStreak }}</b><span>{{ $t('stats.longestStreak') }}</span>
        </div>
      </section>

      <section class="level glass card-pad">
        <div class="lvl">{{ overview.level }}</div>
        <div class="grow stack" style="gap: 8px">
          <div class="row">
            <b class="lvl-title">{{ $t('common.level', { n: overview.level }) }}</b>
            <span class="spacer" />
            <span class="bold muted">{{ overview.levelXp }} / {{ overview.levelXpGoal }} XP</span>
          </div>
          <div class="progress big-progress"><span :style="{ width: `${(overview.levelXp / overview.levelXpGoal) * 100}%` }" /></div>
          <span class="tiny muted">{{ $t('dashboard.toNextLevel', { n: overview.levelXpGoal - overview.levelXp }) }} · {{ overview.xp }} XP</span>
        </div>
      </section>

      <section class="two">
        <div class="glass card-pad stack">
          <h2><CalendarDays :size="22" /> {{ $t('stats.activity') }}</h2>
          <p class="small muted">{{ $t('stats.activityHint') }}</p>
          <ActivityHeatmap :days="activity" :goal="overview.dailyGoal" />
        </div>
        <div class="glass card-pad stack">
          <h2><TrendingUp :size="22" /> {{ $t('stats.forecast') }}</h2>
          <p class="small muted">{{ $t('stats.forecastHint') }}</p>
          <ForecastChart :days="forecast" />
        </div>
      </section>

      <section class="glass card-pad stack">
        <div class="section-head">
          <h2><Award :size="22" /> {{ $t('stats.achievements') }}</h2>
          <span class="badge badge-warning">{{ $t('stats.achievementsCount', { n: overview.achievementsUnlocked, total: overview.achievementsTotal }) }}</span>
        </div>
        <div class="achievements stagger">
          <AchievementBadge v-for="a in achievements" :key="a.code" :achievement="a" />
        </div>
      </section>

      <section v-if="hard.length" class="glass card-pad stack">
        <h2>🧗 {{ $t('stats.hardWords') }}</h2>
        <table class="table">
          <tbody>
            <tr v-for="w in hard" :key="w.cardId">
              <td><b>{{ w.word }}</b></td>
              <td class="muted">{{ w.translation }}</td>
              <td class="hide-sm"><RouterLink :to="`/decks/${w.deckId}`" class="small">{{ w.deckTitle }}</RouterLink></td>
              <td class="err">
                <div class="progress err-bar"><span :style="{ width: `${w.errorRate * 100}%` }" /></div>
                <span class="tiny bold">{{ Math.round(w.errorRate * 100) }}%</span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<style scoped>
.tiles {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}

.tile {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 18px;
}

.tile b {
  font-family: var(--font-display);
  font-size: 1.7rem;
  margin-top: 8px;
}

.tile > span:last-child {
  color: var(--text-muted);
  font-size: 0.82rem;
  font-weight: 700;
}

.t-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
}

.level {
  display: flex;
  align-items: center;
  gap: 22px;
}

.lvl {
  display: grid;
  place-items: center;
  flex: none;
  width: 84px;
  height: 84px;
  border-radius: 26px;
  background: var(--grad-brand);
  color: #fff;
  font-family: var(--font-display);
  font-size: 2.2rem;
  font-weight: 700;
  box-shadow: var(--shadow-glow);
}

.lvl-title {
  font-family: var(--font-display);
  font-size: 1.2rem;
}

.big-progress {
  height: 14px;
}

.two {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 18px;
}

.two h2,
section h2 {
  display: flex;
  align-items: center;
  gap: 10px;
}

.achievements {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.err {
  width: 160px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.err-bar {
  flex: 1;
  height: 6px;
}

.err-bar > span {
  background: linear-gradient(90deg, #ffb547, #f03e5e);
}

@media (max-width: 1100px) {
  .tiles {
    grid-template-columns: repeat(3, 1fr);
  }

  .two {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .tiles {
    grid-template-columns: 1fr 1fr;
  }

  .hide-sm {
    display: none;
  }

  .err {
    width: 100px;
  }
}
</style>
