<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { Clock, Layers, RotateCcw, Sparkles, Target } from 'lucide-vue-next'
import type { SessionFinish } from '@/api'
import { celebrate } from '@/composables/useCelebrate'
import { formatDuration } from '@/utils/format'
import AchievementBadge from './AchievementBadge.vue'
import ProgressRing from './ProgressRing.vue'
import StreakFlame from './StreakFlame.vue'

const props = defineProps<{ result: SessionFinish; busy?: boolean }>()
defineEmits<{ again: []; deck: []; home: [] }>()

const title = computed(() => {
  const a = props.result.accuracyPercent
  if (a >= 100 && props.result.cardsTotal > 0) return 'finish.perfect'
  if (a >= 80) return 'finish.great'
  if (a >= 50) return 'finish.good'
  return 'finish.keepGoing'
})

const emoji = computed(() => {
  const a = props.result.accuracyPercent
  return a >= 100 ? '🏆' : a >= 80 ? '🎉' : a >= 50 ? '💪' : '🌱'
})

const goalReached = computed(() => props.result.todayReviewed >= props.result.dailyGoal)
const goalPercent = computed(() => Math.min(100, (props.result.todayReviewed / Math.max(1, props.result.dailyGoal)) * 100))

onMounted(() => {
  if (props.result.accuracyPercent >= 80 || props.result.newAchievements.length) {
    setTimeout(() => celebrate(props.result.accuracyPercent >= 100 || props.result.newAchievements.length > 0), 250)
  }
})
</script>

<template>
  <div class="summary">
    <div class="trophy">{{ emoji }}</div>
    <h1>{{ $t('finish.title') }}</h1>
    <p class="lead muted">{{ $t(title) }}</p>

    <div class="stats stagger">
      <div class="stat glass">
        <ProgressRing :value="result.accuracyPercent" :size="76" :stroke="8" gradient="fresh">
          <span>{{ Math.round(result.accuracyPercent) }}%</span>
        </ProgressRing>
        <span class="label"><Target :size="14" /> {{ $t('finish.accuracy') }}</span>
      </div>
      <div class="stat glass">
        <b class="value">{{ result.cardsCorrect }}/{{ result.cardsTotal }}</b>
        <span class="label"><Layers :size="14" /> {{ $t('finish.cards') }}</span>
      </div>
      <div class="stat glass">
        <b class="value">{{ formatDuration(result.durationSeconds) }}</b>
        <span class="label"><Clock :size="14" /> {{ $t('finish.time') }}</span>
      </div>
      <div class="stat glass xp">
        <b class="value">+{{ result.xpEarned }}</b>
        <span class="label"><Sparkles :size="14" /> XP · {{ $t('common.level', { n: result.level }) }}</span>
      </div>
    </div>

    <div class="bonus glass">
      <div class="row">
        <StreakFlame :days="result.currentStreak" :active="true" :size="34" />
        <b v-if="result.streakExtended">{{ $t('finish.streakExtended', { n: $t('common.days', { n: result.currentStreak }, result.currentStreak) }) }}</b>
        <b v-else>{{ $t('common.days', { n: result.currentStreak }, result.currentStreak) }}</b>
      </div>
      <div class="goal">
        <div class="row small">
          <b>{{ goalReached ? `✅ ${$t('finish.goalReached')}` : $t('finish.goalProgress', { done: result.todayReviewed, goal: result.dailyGoal }) }}</b>
        </div>
        <div class="progress warm"><span :style="{ width: `${goalPercent}%` }" /></div>
      </div>
    </div>

    <div v-if="result.newAchievements.length" class="achievements">
      <h3>🏅 {{ $t('finish.newAchievements') }}</h3>
      <div class="ach-list stagger">
        <AchievementBadge v-for="a in result.newAchievements" :key="a.code" :achievement="a" compact />
      </div>
    </div>

    <div class="actions">
      <button class="btn btn-primary btn-lg" type="button" :disabled="busy" @click="$emit('again')">
        <RotateCcw :size="20" /> {{ $t('finish.again') }}
      </button>
      <button class="btn btn-lg" type="button" @click="$emit('deck')">{{ $t('finish.toDeck') }}</button>
      <button class="btn btn-ghost btn-lg" type="button" @click="$emit('home')">{{ $t('finish.home') }}</button>
    </div>
  </div>
</template>

<style scoped>
.summary {
  width: min(720px, 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 18px;
  text-align: center;
  padding: 24px 0 48px;
}

.trophy {
  font-size: 5rem;
  line-height: 1;
  animation: trophy-in 0.9s var(--ease-spring) both, float 4s ease-in-out 1s infinite;
  filter: drop-shadow(0 16px 30px rgba(255, 181, 71, 0.45));
}

@keyframes trophy-in {
  from { transform: scale(0) rotate(-30deg); }
  to { transform: scale(1) rotate(0); }
}

.lead {
  font-size: 1.1rem;
}

.stats {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 8px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 18px 10px;
  min-height: 140px;
}

.value {
  font-family: var(--font-display);
  font-size: 1.8rem;
}

.xp .value {
  background: var(--grad-warm);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.78rem;
  font-weight: 800;
  color: var(--text-subtle);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.bonus {
  width: 100%;
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 24px;
  padding: 18px 22px;
  text-align: left;
}

.goal {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.achievements {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ach-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 10px;
  text-align: left;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 10px;
}

@media (max-width: 640px) {
  .stats {
    grid-template-columns: 1fr 1fr;
  }

  .bonus {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .actions .btn {
    width: 100%;
  }
}
</style>
