<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { watchDebounced } from '@vueuse/core'
import { BadgeCheck, Copy, Heart, Search } from 'lucide-vue-next'
import BaseModal from '@/components/BaseModal.vue'
import CatalogCard from '@/components/CatalogCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import LanguagePair from '@/components/LanguagePair.vue'
import SpeakButton from '@/components/SpeakButton.vue'
import { deckApi, type Card, type CatalogDeck, type CatalogQuery } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useDictionaryStore } from '@/stores/dictionary'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'
import { celebrate } from '@/composables/useCelebrate'
import { languageName } from '@/utils/format'

const { t, locale } = useI18n()
const router = useRouter()
const auth = useAuthStore()
const dictionary = useDictionaryStore()
const ui = useUiStore()
const showError = useErrorToast()

const decks = ref<CatalogDeck[]>([])
const loading = ref(true)
const page = ref(0)
const last = ref(true)
const search = ref('')
const languageId = ref<number | undefined>()
const tagId = ref<number | undefined>()
const official = ref(false)
const sort = ref<NonNullable<CatalogQuery['sort']>>('popular')

const selected = ref<CatalogDeck | null>(null)
const preview = ref<Card[]>([])
const busy = ref(false)

const targetLanguages = computed(() => dictionary.languages.filter((l) => l.code !== 'ru'))

async function load(reset = true) {
  if (reset) {
    page.value = 0
    loading.value = true
  }
  try {
    const result = await deckApi.catalog({
      search: search.value.trim() || undefined,
      languageId: languageId.value,
      tagId: tagId.value,
      official: official.value || undefined,
      sort: sort.value,
      page: page.value,
      size: 24,
    })
    decks.value = reset ? result.content : [...decks.value, ...result.content]
    last.value = result.page.last
  } catch (e) {
    showError(e)
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  load(false)
}

watchDebounced(search, () => load(), { debounce: 350 })
watch([languageId, tagId, official, sort], () => load())

async function open(deck: CatalogDeck) {
  selected.value = deck
  preview.value = []
  try {
    preview.value = await deckApi.preview(deck.id, 8)
  } catch {
    preview.value = []
  }
}

async function copy() {
  if (!selected.value) return
  if (!auth.isAuthenticated) return router.push({ name: 'register' })
  busy.value = true
  try {
    const result = await deckApi.copy(selected.value.id)
    ui.toast(t('deck.copied'), 'success', selected.value.icon)
    celebrate()
    router.push(`/decks/${result.newDeckId}`)
  } catch (e) {
    showError(e)
  } finally {
    busy.value = false
  }
}

async function like() {
  const deck = selected.value
  if (!deck) return
  if (!auth.isAuthenticated) return router.push({ name: 'login' })
  try {
    const result = deck.likedByMe ? await deckApi.unlike(deck.id) : await deckApi.like(deck.id)
    deck.likedByMe = result.liked
    deck.likesCount = result.likesCount
  } catch (e) {
    showError(e)
  }
}

onMounted(async () => {
  dictionary.load().catch(() => undefined)
  await load()
})
</script>

<template>
  <div class="page">
    <section class="head">
      <h1>🧭 {{ $t('explore.title') }}</h1>
      <p class="muted">{{ $t('explore.subtitle') }}</p>
      <div class="input-icon search">
        <Search :size="20" />
        <input v-model="search" class="input" :placeholder="$t('explore.searchPlaceholder')" />
      </div>
    </section>

    <section class="filters">
      <div class="row-wrap">
        <button type="button" class="chip" :class="{ 'is-active': languageId === undefined }" @click="languageId = undefined">
          🌍 {{ $t('explore.allLanguages') }}
        </button>
        <button v-for="l in targetLanguages" :key="l.id" type="button" class="chip" :class="{ 'is-active': languageId === l.id }"
                @click="languageId = l.id">
          {{ l.flag }} {{ languageName(l.code, locale, l.name) }}
        </button>
      </div>
      <div class="row-wrap">
        <button v-for="tag in dictionary.tags" :key="tag.id" type="button" class="chip small-chip"
                :class="{ 'is-active': tagId === tag.id }" @click="tagId = tagId === tag.id ? undefined : tag.id">
          #{{ tag.name }}
        </button>
      </div>
      <div class="row-wrap">
        <select v-model="sort" class="select sort">
          <option v-for="s in (['popular', 'likes', 'new', 'title'] as const)" :key="s" :value="s">{{ $t(`explore.sorts.${s}`) }}</option>
        </select>
        <button type="button" class="chip" :class="{ 'is-active': official }" @click="official = !official">
          <BadgeCheck :size="16" /> {{ $t('explore.officialOnly') }}
        </button>
      </div>
    </section>

    <div v-if="loading" class="grid">
      <div v-for="i in 8" :key="i" class="skeleton" style="height: 250px" />
    </div>
    <EmptyState v-else-if="!decks.length" icon="🔭" :title="$t('common.emptySearch')" :text="$t('explore.empty')" />
    <template v-else>
      <div class="grid stagger">
        <CatalogCard v-for="deck in decks" :key="deck.id" :deck="deck" @open="open" />
      </div>
      <button v-if="!last" class="btn load-more" type="button" @click="loadMore">{{ $t('explore.loadMore') }}</button>
    </template>

    <BaseModal :open="selected !== null" wide @close="selected = null">
      <template #header>
        <div v-if="selected" class="row modal-head">
          <span class="modal-icon" :data-color="selected.color ?? 'violet'">{{ selected.icon }}</span>
          <div class="stack" style="gap: 4px">
            <h2>{{ selected.title }}</h2>
            <div class="row-wrap small">
              <LanguagePair :source="selected.sourceLanguage" :target="selected.targetLanguage" show-names />
              <span class="subtle">· @{{ selected.ownerUsername }}</span>
              <span v-if="selected.isOfficial" class="badge badge-primary"><BadgeCheck :size="12" /> {{ $t('common.official') }}</span>
            </div>
          </div>
        </div>
      </template>
      <div v-if="selected" class="stack">
        <p v-if="selected.description" class="muted">{{ selected.description }}</p>
        <div class="row-wrap">
          <span class="badge">{{ $t('common.cards', { n: selected.cardsCount }, selected.cardsCount) }}</span>
          <span class="badge"><Heart :size="12" /> {{ selected.likesCount }}</span>
          <span class="badge"><Copy :size="12" /> {{ selected.copiesCount }} {{ $t('explore.copies') }}</span>
          <span v-for="tag in selected.tags" :key="tag.id" class="badge badge-primary">#{{ tag.name }}</span>
        </div>
        <h3 class="sample-title">{{ $t('explore.sampleCards') }}</h3>
        <div class="samples">
          <div v-for="card in preview" :key="card.id" class="sample">
            <SpeakButton :text="card.word" :lang="selected.targetLanguage.code" size="sm" />
            <div class="grow">
              <b class="block">{{ card.word }}</b>
              <span class="small muted">{{ card.translation }}</span>
            </div>
          </div>
          <template v-if="!preview.length">
            <div v-for="i in 4" :key="i" class="skeleton" style="height: 56px" />
          </template>
        </div>
      </div>
      <template #footer>
        <template v-if="selected">
          <button class="btn btn-ghost" type="button" :class="{ liked: selected.likedByMe }" :disabled="selected.owned" @click="like">
            <Heart :size="18" /> {{ selected.likesCount }}
          </button>
          <span class="spacer" />
          <RouterLink v-if="selected.owned" :to="`/decks/${selected.id}`" class="btn btn-primary">{{ $t('explore.owned') }}</RouterLink>
          <button v-else class="btn btn-primary" type="button" :disabled="busy" @click="copy">
            <Copy :size="18" /> {{ auth.isAuthenticated ? $t('deck.copyToMe') : $t('explore.loginToCopy') }}
          </button>
        </template>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  padding: 12px 0 4px;
}

.search {
  width: min(560px, 100%);
  margin-top: 8px;
}

.search .input {
  height: 56px;
  border-radius: var(--radius-full);
  font-size: 1.05rem;
  box-shadow: var(--shadow-md);
}

.filters {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.small-chip {
  height: 30px;
  font-size: 0.8rem;
}

.sort {
  width: auto;
  height: 38px;
  border-radius: var(--radius-full);
}

.load-more {
  align-self: center;
}

.modal-head {
  gap: 14px;
  align-items: center;
}

.modal-icon {
  display: grid;
  place-items: center;
  width: 60px;
  height: 60px;
  flex: none;
  border-radius: 18px;
  font-size: 2rem;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
}

.sample-title {
  margin-top: 6px;
}

.samples {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.sample {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
}

.block {
  display: block;
}

.liked {
  color: var(--danger);
}

.liked svg {
  fill: currentColor;
}

@media (max-width: 600px) {
  .samples {
    grid-template-columns: 1fr;
  }
}
</style>
