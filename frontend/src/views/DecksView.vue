<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Compass, Plus, Search } from 'lucide-vue-next'
import DeckCard from '@/components/DeckCard.vue'
import DeckFormModal from '@/components/DeckFormModal.vue'
import EmptyState from '@/components/EmptyState.vue'
import { deckApi, type Deck, type DeckSummary } from '@/api'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'

const { t } = useI18n()
const router = useRouter()
const ui = useUiStore()
const showError = useErrorToast()

const decks = ref<DeckSummary[]>([])
const loading = ref(true)
const search = ref('')
const creating = ref(false)

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return decks.value
  return decks.value.filter((d) => d.title.toLowerCase().includes(q) || d.description?.toLowerCase().includes(q))
})

const totals = computed(() => ({
  cards: decks.value.reduce((s, d) => s + d.cardsCount, 0),
  known: decks.value.reduce((s, d) => s + d.knownCount, 0),
  due: decks.value.reduce((s, d) => s + d.dueCount, 0),
}))

onMounted(async () => {
  try {
    decks.value = (await deckApi.mine()).content
  } catch (e) {
    showError(e)
  } finally {
    loading.value = false
  }
})

function onCreated(deck: Deck) {
  creating.value = false
  ui.toast(t('deckForm.created'), 'success', deck.icon)
  router.push(`/decks/${deck.id}`)
}
</script>

<template>
  <div class="page">
    <section class="section-head">
      <div class="stack" style="gap: 6px">
        <h1>{{ $t('decks.title') }}</h1>
        <p class="muted">
          {{ $t('decks.subtitle') }}
          <template v-if="decks.length">
            · {{ $t('common.cards', { n: totals.cards }, totals.cards) }} · {{ totals.known }} {{ $t('decks.learned') }}
          </template>
        </p>
      </div>
      <button class="btn btn-primary" type="button" @click="creating = true">
        <Plus :size="20" /> {{ $t('decks.create') }}
      </button>
    </section>

    <div v-if="decks.length > 3" class="input-icon search">
      <Search :size="18" />
      <input v-model="search" class="input" :placeholder="$t('decks.searchPlaceholder')" />
    </div>

    <div v-if="loading" class="grid">
      <div v-for="i in 6" :key="i" class="skeleton" style="height: 250px" />
    </div>

    <EmptyState v-else-if="!decks.length" icon="📚" :title="$t('decks.emptyTitle')" :text="$t('decks.emptyText')">
      <button class="btn btn-primary" type="button" @click="creating = true"><Plus :size="18" /> {{ $t('decks.create') }}</button>
      <RouterLink to="/explore" class="btn"><Compass :size="18" /> {{ $t('decks.toCatalog') }}</RouterLink>
    </EmptyState>

    <template v-else>
      <p v-if="!filtered.length" class="muted center">{{ $t('common.emptySearch') }}</p>
      <TransitionGroup tag="div" name="list" class="grid">
        <DeckCard v-for="deck in filtered" :key="deck.id" :deck="deck" />
        <button key="new" class="new-deck" type="button" @click="creating = true">
          <span class="plus"><Plus :size="28" /></span>
          <b>{{ $t('decks.create') }}</b>
        </button>
      </TransitionGroup>
    </template>

    <DeckFormModal :open="creating" @close="creating = false" @saved="onCreated" />
  </div>
</template>

<style scoped>
.search {
  max-width: 420px;
}

.new-deck {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 250px;
  border-radius: var(--radius-lg);
  border: 2px dashed var(--border-strong);
  background: transparent;
  color: var(--text-muted);
  transition: all 0.3s var(--ease-out);
}

.new-deck:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-soft);
}

.plus {
  display: grid;
  place-items: center;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--primary);
  transition: transform 0.4s var(--ease-spring);
}

.new-deck:hover .plus {
  transform: rotate(90deg) scale(1.1);
}
</style>
