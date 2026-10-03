<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight, Brain, Check, Clock, Plus, Target, Zap } from 'lucide-vue-next'
import DeckCard from '@/components/DeckCard.vue'
import DeckFormModal from '@/components/DeckFormModal.vue'
import ProgressRing from '@/components/ProgressRing.vue'
import StreakFlame from '@/components/StreakFlame.vue'
import WordOfDayCard from '@/components/WordOfDayCard.vue'
import { deckApi, statsApi, type CatalogDeck, type Deck, type DeckSummary, type HardWord, type WordOfTheDay } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useStatsStore } from '@/stores/stats'
import { useTrainingStore } from '@/stores/training'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'

const { t } = useI18n()
const auth = useAuthStore()
const stats = useStatsStore()
const training = useTrainingStore()
const ui = useUiStore()
const router = useRouter()
const showError = useErrorToast()

const decks = ref<DeckSummary[]>([])
const starter = ref<CatalogDeck[]>([])
const word = ref<WordOfTheDay | null>(null)
const hard = ref<HardWord[]>([])
const loading = ref(true)
const creating = ref(false)
const adding = ref<string | null>(null)
const startingDeck = ref<string | null>(null)

const overview = computed(() => stats.overview)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 5) return t('dashboard.night')
  if (h < 12) return t('dashboard.morning')
  if (h < 18) return t('dashboard.day')
  return t('dashboard.evening')
})

const dueDecks = computed(() => decks.value.filter((d) => d.dueCount > 0).sort((a, b) => b.dueCount - a.dueCount))
const totalDue = computed(() => decks.value.reduce((sum, d) => sum + d.dueCount, 0))
const learnDecks = computed(() =>
  decks.value.filter((d) => d.dueCount === 0 && d.knownCount < d.cardsCount).slice(0, 3),
)

const subtitle = computed(() => {
  if (!decks.value.length) return t('dashboard.subtitleNew')
  if (totalDue.value > 0) return t('dashboard.subtitleDue', { n: t('common.cards', { n: totalDue.value }, totalDue.value) })
  return t('dashboard.subtitleDone')
})

const goalPercent = computed(() => {
  const o = overview.value
  return o && o.dailyGoal ? Math.min(100, Math.round((o.todayReviewed / o.dailyGoal) * 100)) : 0
})

async function load() {
  loading.value = true
  try {
    const [mine, wotd, hardWords] = await Promise.all([
      deckApi.mine(),
      statsApi.wordOfTheDay(),
      statsApi.hardWords(undefined, 5),
      stats.refresh(),
    ])
    decks.value = mine.content
    word.value = wotd ?? null
    hard.value = hardWords
    if (!mine.content.length) {
      starter.value = (await deckApi.catalog({ official: true, size: 6 })).content
    }
  } catch (e) {
    showError(e)
  } finally {
    loading.value = false
  }
}

async function train(deckId: string) {
  startingDeck.value = deckId
  try {
    await training.start(deckId, { scope: 'smart' })
  } catch (e) {
    showError(e)
  } finally {
    startingDeck.value = null
  }
}

async function addStarter(deck: CatalogDeck) {
  adding.value = deck.id
  try {
    await deckApi.copy(deck.id)
    ui.toast(t('deck.copied'), 'success', deck.icon)
    await load()
  } catch (e) {
    showError(e)
  } finally {
    adding.value = null
  }
}

function onCreated(deck: Deck) {
  creating.value = false
  ui.toast(t('deckForm.created'), 'success', deck.icon)
  router.push(`/decks/${deck.id}`)
}

onMounted(load)
</script>

<template>
  <div class="page">
    <section class="hello">
      <div class="stack hello-text">
        <h1>{{ greeting }}, <span class="gradient-text">{{ auth.user?.username }}</span> 👋</h1>
        <p class="muted lead">{{ subtitle }}</p>
      </div>
      <button v-if="dueDecks.length" class="btn btn-primary btn-lg" type="button" :disabled="!!startingDeck"
              @click="train(dueDecks[0].id)">
        <Zap :size="20" /> {{ $t('dashboard.review') }} · {{ totalDue }}
      </button>
    </section>

    <section class="tiles stagger">
      <div class="tile glass">
        <ProgressRing :value="overview?.todayReviewed ?? 0" :max="overview?.dailyGoal ?? 20" :size="84" :stroke="9" gradient="warm">
          <Check v-if="goalPercent >= 100" :size="30" class="goal-done" />
          <span v-else class="ring-num">{{ goalPercent }}%</span>
        </ProgressRing>
        <div class="tile-text">
          <span class="tile-label"><Target :size="16" /> {{ $t('dashboard.dailyGoal') }}</span>
          <b class="tile-value">{{ overview?.todayReviewed ?? 0 }} / {{ overview?.dailyGoal ?? 20 }}</b>
          <span class="small muted">
            {{ goalPercent >= 100 ? $t('dashboard.goalDone')
              : $t('dashboard.goalLeft', { n: Math.max(0, (overview?.dailyGoal ?? 0) - (overview?.todayReviewed ?? 0)) }) }}
          </span>
        </div>
      </div>

      <div class="tile glass">
        <div class="flame-wrap">
          <StreakFlame :days="overview?.currentStreak ?? 0" :active="overview?.activeToday" :size="58" />
        </div>
        <div class="tile-text">
          <span class="tile-label">{{ $t('dashboard.streak') }}</span>
          <b class="tile-value">{{ $t('common.days', { n: overview?.currentStreak ?? 0 }, overview?.currentStreak ?? 0) }}</b>
          <span class="small muted">
            {{ !overview?.currentStreak ? $t('dashboard.streakStart')
              : overview?.activeToday ? $t('dashboard.streakSafe') : $t('dashboard.streakKeep') }}
          </span>
        </div>
      </div>

      <div class="tile glass">
        <div class="level-badge">
          <span>{{ overview?.level ?? 1 }}</span>
        </div>
        <div class="tile-text grow">
          <span class="tile-label">{{ $t('dashboard.level') }}</span>
          <b class="tile-value">{{ overview?.xp ?? 0 }} XP</b>
          <div class="progress"><span :style="{ width: `${overview ? (overview.levelXp / overview.levelXpGoal) * 100 : 0}%` }" /></div>
          <span class="tiny muted">{{ $t('dashboard.toNextLevel', { n: overview ? overview.levelXpGoal - overview.levelXp : 100 }) }}</span>
        </div>
      </div>
    </section>

    <template v-if="!loading && !decks.length">
      <section class="glass card-pad stack start">
        <div class="section-head">
          <div class="stack">
            <h2>🚀 {{ $t('dashboard.startTitle') }}</h2>
            <p class="muted">{{ $t('dashboard.startText') }}</p>
          </div>
          <button class="btn btn-soft" type="button" @click="creating = true"><Plus :size="18" /> {{ $t('dashboard.createOwn') }}</button>
        </div>
        <div class="starter-grid stagger">
          <div v-for="deck in starter" :key="deck.id" class="starter" :data-color="deck.color ?? 'violet'">
            <span class="starter-icon">{{ deck.icon }}</span>
            <div class="grow">
              <b class="block">{{ deck.title }}</b>
              <span class="tiny muted">{{ $t('common.cards', { n: deck.cardsCount }, deck.cardsCount) }}</span>
            </div>
            <button class="btn btn-sm" :class="deck.owned ? 'btn-ghost' : 'btn-primary'" type="button"
                    :disabled="adding === deck.id || deck.owned" @click="addStarter(deck)">
              <Plus :size="16" /> {{ $t('dashboard.addDeck') }}
            </button>
          </div>
        </div>
      </section>
    </template>

    <section class="two-col">
      <div class="glass card-pad stack">
        <div class="section-head">
          <h2><Clock :size="22" /> {{ $t('dashboard.dueTitle') }}</h2>
        </div>
        <div v-if="loading" class="stack">
          <div v-for="i in 3" :key="i" class="skeleton" style="height: 64px" />
        </div>
        <template v-else>
          <div v-if="!dueDecks.length && !learnDecks.length" class="done muted">🎉 {{ $t('dashboard.dueEmpty') }}</div>
          <div v-for="deck in dueDecks.slice(0, 4)" :key="deck.id" class="due-row" :data-color="deck.color ?? 'violet'">
            <span class="due-icon">{{ deck.icon }}</span>
            <div class="grow">
              <b class="truncate block">{{ deck.title }}</b>
              <span class="small due-count">{{ $t('decks.due', { n: deck.dueCount }) }}</span>
            </div>
            <button class="btn btn-primary btn-sm" type="button" :disabled="startingDeck === deck.id" @click="train(deck.id)">
              {{ $t('dashboard.review') }}
            </button>
          </div>
          <div v-for="deck in learnDecks" :key="deck.id" class="due-row" :data-color="deck.color ?? 'violet'">
            <span class="due-icon">{{ deck.icon }}</span>
            <div class="grow">
              <b class="truncate block">{{ deck.title }}</b>
              <span class="small muted">{{ deck.percentLearned }}% · {{ $t('common.cards', { n: deck.cardsCount }, deck.cardsCount) }}</span>
            </div>
            <button class="btn btn-soft btn-sm" type="button" :disabled="startingDeck === deck.id" @click="train(deck.id)">
              <Brain :size="16" /> {{ $t('dashboard.learn') }}
            </button>
          </div>
        </template>
      </div>

      <div class="glass card-pad">
        <WordOfDayCard v-if="word" :word="word" />
        <div v-else class="skeleton" style="height: 230px" />
      </div>
    </section>

    <section v-if="decks.length" class="stack">
      <div class="section-head">
        <h2>{{ $t('dashboard.myDecks') }}</h2>
        <RouterLink to="/decks" class="btn btn-ghost btn-sm">{{ $t('dashboard.seeAll') }} <ArrowRight :size="16" /></RouterLink>
      </div>
      <div class="grid stagger">
        <DeckCard v-for="deck in decks.slice(0, 4)" :key="deck.id" :deck="deck" />
      </div>
    </section>

    <section v-if="hard.length" class="glass card-pad stack">
      <h2>🧗 {{ $t('dashboard.hardWords') }}</h2>
      <div class="hard-list">
        <RouterLink v-for="w in hard" :key="w.cardId" :to="`/decks/${w.deckId}`" class="hard">
          <span class="grow hard-text">
            <b class="truncate block">{{ w.word }}</b>
            <span class="muted small truncate block">{{ w.translation }}</span>
          </span>
          <span class="badge badge-danger">{{ Math.round(w.errorRate * 100) }}%</span>
        </RouterLink>
      </div>
    </section>

    <DeckFormModal :open="creating" @close="creating = false" @saved="onCreated" />
  </div>
</template>

<style scoped>
.hello {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.hello-text {
  gap: 6px;
}

.lead {
  font-size: 1.08rem;
}

.tiles {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.tile {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px;
}

.tile-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.tile-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-subtle);
}

.tile-value {
  font-family: var(--font-display);
  font-size: 1.35rem;
}

.ring-num {
  font-size: 1rem;
}

.goal-done {
  color: var(--success);
}

.flame-wrap {
  display: grid;
  place-items: center;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 140, 60, 0.18), transparent 70%);
}

.flame-wrap :deep(b) {
  display: none;
}

.level-badge {
  flex: none;
  display: grid;
  place-items: center;
  width: 78px;
  height: 78px;
  border-radius: 24px;
  background: var(--grad-brand);
  color: #fff;
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 700;
  transform: rotate(-6deg);
  box-shadow: var(--shadow-glow);
}

.level-badge span {
  transform: rotate(6deg);
}

.two-col {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 18px;
}

.two-col > * {
  min-width: 0;
}

.due-row,
.starter {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
  transition: transform 0.25s var(--ease-out);
}

.due-row:hover,
.starter:hover {
  transform: translateX(3px);
}

.due-icon,
.starter-icon {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  flex: none;
  border-radius: 14px;
  font-size: 1.5rem;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
}

.due-count {
  color: var(--warning);
  font-weight: 800;
}

.block {
  display: block;
}

.done {
  padding: 18px;
  text-align: center;
  font-weight: 700;
}

.starter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

.hard-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 10px;
}

.hard {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
  color: var(--text);
}

@media (max-width: 1100px) {
  .tiles {
    grid-template-columns: 1fr 1fr;
  }

  .tiles .tile:last-child {
    grid-column: span 2;
  }
}

@media (max-width: 860px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .tiles {
    grid-template-columns: 1fr;
  }

  .tiles .tile:last-child {
    grid-column: auto;
  }

  .hello .btn {
    width: 100%;
  }
}
</style>
