<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowLeft, BadgeCheck, Copy, Globe, Heart, Lock, Pencil, Play, Plus, Search, Settings, Trash2, Upload,
} from 'lucide-vue-next'
import CardEditorModal from '@/components/CardEditorModal.vue'
import DeckFormModal from '@/components/DeckFormModal.vue'
import EmptyState from '@/components/EmptyState.vue'
import ImportModal from '@/components/ImportModal.vue'
import LanguagePair from '@/components/LanguagePair.vue'
import ProgressRing from '@/components/ProgressRing.vue'
import SpeakButton from '@/components/SpeakButton.vue'
import TrainingSetupModal from '@/components/TrainingSetupModal.vue'
import { ApiError, cardApi, deckApi, type Card, type CardWithProgress, type Deck } from '@/api'
import { useStatsStore } from '@/stores/stats'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'
import { celebrate } from '@/composables/useCelebrate'
import { formatRelative } from '@/utils/format'

const props = defineProps<{ id: string }>()

const { t, locale } = useI18n()
const router = useRouter()
const ui = useUiStore()
const stats = useStatsStore()
const showError = useErrorToast()

const deck = ref<Deck | null>(null)
const cards = ref<CardWithProgress[]>([])
const loading = ref(true)
const notFound = ref(false)
const filter = ref('')
const statusFilter = ref<'all' | 'new' | 'learning' | 'known'>('all')
const visible = ref(60)

const editingDeck = ref(false)
const training = ref(false)
const importing = ref(false)
const editorOpen = ref(false)
const editingCard = ref<Card | null>(null)
const busy = ref(false)

const canEdit = computed(() => deck.value?.owned ?? false)
const progress = computed(() => deck.value?.progress ?? null)
const targetCode = computed(() => deck.value?.targetLanguage.code ?? 'en')

const filtered = computed(() => {
  const q = filter.value.trim().toLowerCase()
  return cards.value.filter(
    (c) =>
      (statusFilter.value === 'all' || c.status === statusFilter.value) &&
      (!q || c.word.toLowerCase().includes(q) || c.translation.toLowerCase().includes(q)),
  )
})

async function load() {
  try {
    const [d, c] = await Promise.all([deckApi.get(props.id), deckApi.cards(props.id)])
    deck.value = d
    cards.value = c
  } catch (e) {
    if (e instanceof ApiError && (e.status === 404 || e.status === 400)) notFound.value = true
    else showError(e)
  } finally {
    loading.value = false
  }
}

async function reloadCards() {
  const [d, c] = await Promise.all([deckApi.get(props.id), deckApi.cards(props.id)])
  deck.value = d
  cards.value = c
}

function openEditor(card: Card | null = null) {
  editingCard.value = card
  editorOpen.value = true
}

async function onCardSaved(_card: Card, created: boolean) {
  if (!created) editorOpen.value = false
  ui.toast(created ? t('cards.added') : t('common.saved'), 'success')
  await reloadCards()
}

async function removeCard(card: CardWithProgress) {
  const ok = await ui.confirm({ title: t('common.delete'), text: t('cards.deleteConfirm', { word: card.word }), danger: true, confirmText: t('common.delete') })
  if (!ok) return
  try {
    await cardApi.remove(card.id)
    cards.value = cards.value.filter((c) => c.id !== card.id)
    if (deck.value) deck.value.cardsCount--
  } catch (e) {
    showError(e)
  }
}

async function onImported() {
  importing.value = false
  await reloadCards()
}

function onDeckSaved(saved: Deck) {
  const wasPublic = deck.value?.isPublic
  deck.value = saved
  editingDeck.value = false
  if (!wasPublic && saved.isPublic) {
    ui.toast(t('deck.published'), 'success')
    celebrate()
  } else {
    ui.toast(t('common.saved'), 'success')
  }
}

async function removeDeck() {
  if (!deck.value) return
  const ok = await ui.confirm({
    title: t('deck.deleteDeck'),
    text: t('deck.deleteConfirm', { title: deck.value.title }),
    danger: true,
    confirmText: t('common.delete'),
  })
  if (!ok) return
  try {
    await deckApi.remove(deck.value.id)
    ui.toast(t('common.deleted'), 'success')
    stats.refresh()
    router.replace('/decks')
  } catch (e) {
    showError(e)
  }
}

async function copyDeck() {
  if (!deck.value) return
  busy.value = true
  try {
    const result = await deckApi.copy(deck.value.id)
    ui.toast(t('deck.copied'), 'success', deck.value.icon)
    celebrate()
    router.push(`/decks/${result.newDeckId}`)
  } catch (e) {
    showError(e)
  } finally {
    busy.value = false
  }
}

async function toggleLike() {
  if (!deck.value) return
  try {
    const result = deck.value.likedByMe ? await deckApi.unlike(deck.value.id) : await deckApi.like(deck.value.id)
    deck.value.likedByMe = result.liked
    deck.value.likesCount = result.likesCount
  } catch (e) {
    showError(e)
  }
}

function statusClass(status: string) {
  return status === 'known' ? 'badge-success' : status === 'learning' ? 'badge-warning' : 'badge-info'
}

onMounted(load)
</script>

<template>
  <div class="page">
    <button class="btn btn-ghost btn-sm back" type="button" @click="router.back()">
      <ArrowLeft :size="18" /> {{ $t('common.back') }}
    </button>

    <div v-if="loading" class="stack">
      <div class="skeleton" style="height: 220px" />
      <div class="skeleton" style="height: 320px" />
    </div>

    <EmptyState v-else-if="notFound || !deck" icon="🔍" :title="$t('common.notFound')">
      <RouterLink to="/decks" class="btn btn-primary">{{ $t('nav.decks') }}</RouterLink>
    </EmptyState>

    <template v-else>
      <section class="hero" :data-color="deck.color ?? 'violet'">
        <div class="hero-bg" />
        <span class="hero-icon">{{ deck.icon }}</span>
        <div class="hero-info">
          <div class="row-wrap">
            <span v-if="deck.isOfficial" class="hero-badge"><BadgeCheck :size="14" /> {{ $t('common.official') }}</span>
            <span class="hero-badge">
              <Globe v-if="deck.isPublic" :size="14" /><Lock v-else :size="14" />
              {{ deck.isPublic ? $t('common.public') : $t('common.private') }}
            </span>
            <span v-if="!deck.owned" class="hero-badge">{{ $t('deck.by', { name: deck.owner.username }) }}</span>
          </div>
          <h1>{{ deck.title }}</h1>
          <p v-if="deck.description" class="hero-desc">{{ deck.description }}</p>
          <div class="row-wrap">
            <span class="hero-badge lang"><LanguagePair :source="deck.sourceLanguage" :target="deck.targetLanguage" show-names /></span>
            <span v-for="tag in deck.tags" :key="tag.id" class="hero-badge">#{{ tag.name }}</span>
          </div>
        </div>
        <div class="hero-actions">
          <button class="btn btn-lg train-btn" type="button" :disabled="!deck.cardsCount" @click="training = true">
            <Play :size="20" /> {{ $t('deck.train') }}
          </button>
          <div class="row">
            <template v-if="canEdit">
              <button class="btn hero-ghost btn-icon" type="button" :title="$t('deck.settings')" @click="editingDeck = true">
                <Settings :size="18" />
              </button>
              <button class="btn hero-ghost btn-icon" type="button" :title="$t('deck.deleteDeck')" @click="removeDeck">
                <Trash2 :size="18" />
              </button>
            </template>
            <template v-else>
              <button class="btn hero-ghost" type="button" :class="{ liked: deck.likedByMe }" @click="toggleLike">
                <Heart :size="18" /> {{ deck.likesCount }}
              </button>
              <button class="btn hero-ghost" type="button" :disabled="busy" @click="copyDeck">
                <Copy :size="18" /> {{ $t('deck.copyToMe') }}
              </button>
            </template>
          </div>
        </div>
      </section>

      <section v-if="progress" class="progress-tiles stagger">
        <div class="ptile glass">
          <ProgressRing :value="progress.percentLearned" :size="70" :stroke="8" gradient="fresh">
            <span>{{ progress.percentLearned }}%</span>
          </ProgressRing>
          <div><b class="big">{{ progress.knownCards }}</b><span class="muted small block">{{ $t('deck.known') }}</span></div>
        </div>
        <div class="ptile glass"><span class="dot learning" /><div><b class="big">{{ progress.learningCards }}</b><span class="muted small block">{{ $t('deck.learning') }}</span></div></div>
        <div class="ptile glass"><span class="dot new" /><div><b class="big">{{ progress.newCards }}</b><span class="muted small block">{{ $t('deck.new') }}</span></div></div>
        <div class="ptile glass"><span class="dot due" /><div><b class="big">{{ progress.dueCards }}</b><span class="muted small block">{{ $t('deck.due') }}</span></div></div>
      </section>

      <p v-if="!canEdit" class="readonly glass small">💡 {{ $t('deck.readonly') }}</p>

      <section class="glass card-pad stack">
        <div class="section-head">
          <h2>{{ $t('deck.cards') }} <span class="badge">{{ cards.length }}</span></h2>
          <div v-if="canEdit" class="row">
            <button class="btn btn-ghost btn-sm" type="button" @click="importing = true"><Upload :size="16" /> {{ $t('cards.import') }}</button>
            <button class="btn btn-primary btn-sm" type="button" @click="openEditor()"><Plus :size="16" /> {{ $t('cards.addCard') }}</button>
          </div>
        </div>

        <EmptyState v-if="!cards.length" icon="✍️" :title="$t('deck.noCards')" :text="canEdit ? $t('deck.noCardsText') : undefined">
          <template v-if="canEdit">
            <button class="btn btn-primary" type="button" @click="openEditor()"><Plus :size="18" /> {{ $t('cards.addCard') }}</button>
            <button class="btn" type="button" @click="importing = true"><Upload :size="18" /> {{ $t('cards.import') }}</button>
          </template>
        </EmptyState>

        <template v-else>
          <div class="filters">
            <div class="input-icon grow">
              <Search :size="18" />
              <input v-model="filter" class="input" :placeholder="$t('cards.filter')" />
            </div>
            <div class="row-wrap">
              <button v-for="s in (['all', 'new', 'learning', 'known'] as const)" :key="s" type="button" class="chip"
                      :class="{ 'is-active': statusFilter === s }" @click="statusFilter = s">
                {{ s === 'all' ? $t('common.all') : $t(`status.${s}`) }}
              </button>
            </div>
          </div>

          <ul class="cards">
            <li v-for="card in filtered.slice(0, visible)" :key="card.id" class="card-row">
              <SpeakButton :text="card.word" :lang="targetCode" size="sm" />
              <div class="word-col">
                <b class="word">{{ card.word }}</b>
                <span v-if="card.transcription" class="tiny subtle">{{ card.transcription }}</span>
              </div>
              <div class="tr-col">
                <span>{{ card.translation }}</span>
                <span v-if="card.exampleSentence" class="tiny muted example">{{ card.exampleSentence }}</span>
              </div>
              <div class="meta-col">
                <span class="badge" :class="statusClass(card.status)">{{ $t(`status.${card.status}`) }}</span>
                <span v-if="card.nextReviewAt" class="tiny subtle nowrap">{{ formatRelative(card.nextReviewAt, locale) }}</span>
              </div>
              <div v-if="canEdit" class="actions">
                <button class="btn btn-ghost btn-icon btn-sm" type="button" :title="$t('common.edit')" @click="openEditor(card)"><Pencil :size="16" /></button>
                <button class="btn btn-danger-ghost btn-icon btn-sm" type="button" :title="$t('common.delete')" @click="removeCard(card)"><Trash2 :size="16" /></button>
              </div>
            </li>
          </ul>
          <button v-if="filtered.length > visible" class="btn btn-ghost more" type="button" @click="visible += 100">
            {{ $t('explore.loadMore') }} · {{ filtered.length - visible }}
          </button>
          <p v-if="!filtered.length" class="muted center">{{ $t('common.emptySearch') }}</p>
        </template>
      </section>

      <TrainingSetupModal :open="training" :deck-id="deck.id" :progress="progress" @close="training = false" />
      <DeckFormModal :open="editingDeck" :deck="deck" @close="editingDeck = false" @saved="onDeckSaved" />
      <ImportModal :open="importing" :deck-id="deck.id" @close="importing = false" @imported="onImported" />
      <CardEditorModal :open="editorOpen" :deck-id="deck.id" :card="editingCard" @close="editorOpen = false" @saved="onCardSaved" />
    </template>
  </div>
</template>

<style scoped>
.back {
  align-self: flex-start;
  margin-bottom: -12px;
}

.hero {
  position: relative;
  isolation: isolate;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 24px;
  padding: 32px;
  border-radius: var(--radius-xl);
  color: #fff;
  overflow: hidden;
  box-shadow: 0 24px 60px -20px var(--deck-a);
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: -1;
  background: radial-gradient(circle at 90% 0%, rgba(255, 255, 255, 0.3), transparent 40%),
    linear-gradient(135deg, var(--deck-a), var(--deck-b));
}

.hero-icon {
  font-size: 5rem;
  line-height: 1;
  filter: drop-shadow(0 12px 20px rgba(0, 0, 0, 0.25));
  animation: float 6s ease-in-out infinite;
}

.hero-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.hero-desc {
  opacity: 0.9;
  max-width: 640px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  font-size: 0.8rem;
  font-weight: 800;
}

.hero-badge.lang :deep(.pair),
.hero-badge.lang :deep(.arrow) {
  color: #fff;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 10px;
}

.train-btn {
  --btn-bg: #fff;
  --btn-fg: #2b1f6b;
  box-shadow: 0 12px 30px -10px rgba(0, 0, 0, 0.4);
}

.hero-ghost {
  --btn-bg: rgba(255, 255, 255, 0.18);
  --btn-fg: #fff;
  --btn-border: rgba(255, 255, 255, 0.25);
  flex: 1;
}

.hero-ghost.liked svg {
  fill: #fff;
}

.progress-tiles {
  display: grid;
  grid-template-columns: 1.3fr 1fr 1fr 1fr;
  gap: 14px;
}

.ptile {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
}

.big {
  font-family: var(--font-display);
  font-size: 1.6rem;
}

.block {
  display: block;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex: none;
}

.dot.learning { background: var(--warning); box-shadow: 0 0 0 5px var(--warning-soft); }
.dot.new { background: var(--info); box-shadow: 0 0 0 5px var(--info-soft); }
.dot.due { background: var(--danger); box-shadow: 0 0 0 5px var(--danger-soft); }

.readonly {
  padding: 14px 18px;
  border-radius: var(--radius-md);
}

.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.cards {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.card-row {
  display: grid;
  grid-template-columns: auto minmax(120px, 1fr) minmax(120px, 1.3fr) auto auto;
  align-items: center;
  gap: 14px;
  padding: 12px 8px;
  border-bottom: 1px solid var(--border);
  transition: background 0.2s ease;
  border-radius: 12px;
}

.card-row:hover {
  background: var(--surface-hover);
}

.card-row:last-child {
  border-bottom: none;
}

.word-col,
.tr-col,
.meta-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.meta-col {
  align-items: flex-end;
}

.word {
  font-size: 1.02rem;
}

.example {
  font-style: italic;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions {
  display: flex;
  gap: 2px;
}

.more {
  align-self: center;
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .hero-icon {
    font-size: 3.6rem;
  }

  .progress-tiles {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .card-row {
    grid-template-columns: auto 1fr auto;
    grid-template-areas:
      'speak word meta'
      'speak tr actions';
    row-gap: 4px;
  }

  .card-row > :first-child { grid-area: speak; }
  .word-col { grid-area: word; }
  .tr-col { grid-area: tr; }
  .meta-col { grid-area: meta; }
  .actions { grid-area: actions; justify-content: flex-end; }
  .example { display: none; }
}
</style>
