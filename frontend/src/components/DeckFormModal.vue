<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { Deck, DeckColor, DeckPayload } from '@/api'
import { ApiError, deckApi } from '@/api'
import { useDictionaryStore } from '@/stores/dictionary'
import { useErrorToast } from '@/composables/useErrorToast'
import { languageName } from '@/utils/format'
import BaseModal from './BaseModal.vue'

const props = defineProps<{ open: boolean; deck?: Deck | null }>()
const emit = defineEmits<{ close: []; saved: [deck: Deck] }>()

const { t, locale } = useI18n()
const dictionary = useDictionaryStore()
const showError = useErrorToast()

const ICONS = ['📚', '🔥', '💬', '🧩', '🔁', '✈️', '💻', '🌍', '🍎', '🎬', '🎵', '⚽', '🧠', '💼', '🏠', '❤️', '🌞', '🥨', '🐉', '🚀', '🎨', '🩺', '⚖️', '🌿']
const COLORS: DeckColor[] = ['violet', 'blue', 'sky', 'teal', 'green', 'amber', 'orange', 'pink', 'red', 'slate']

const form = reactive({
  title: '',
  description: '',
  icon: '📚',
  color: 'violet' as DeckColor,
  sourceLanguageId: 0,
  targetLanguageId: 0,
  isPublic: false,
  tagIds: [] as number[],
})
const saving = ref(false)
const errors = ref<Record<string, string>>({})

const languages = computed(() =>
  dictionary.languages.map((l) => ({ ...l, label: `${l.flag ?? ''} ${languageName(l.code, locale.value, l.name)}` })),
)

watch(
  () => props.open,
  async (open) => {
    if (!open) return
    await dictionary.load()
    errors.value = {}
    const d = props.deck
    const ru = dictionary.languageByCode('ru')?.id ?? dictionary.languages[0]?.id ?? 0
    const en = dictionary.languageByCode('en')?.id ?? dictionary.languages[1]?.id ?? 0
    Object.assign(form, {
      title: d?.title ?? '',
      description: d?.description ?? '',
      icon: d?.icon ?? ICONS[Math.floor(Math.random() * 8)],
      color: d?.color ?? COLORS[Math.floor(Math.random() * COLORS.length)],
      sourceLanguageId: d?.sourceLanguage.id ?? ru,
      targetLanguageId: d?.targetLanguage.id ?? en,
      isPublic: d?.isPublic ?? false,
      tagIds: d?.tags.map((tag) => tag.id) ?? [],
    })
  },
  { immediate: true },
)

function toggleTag(id: number) {
  form.tagIds = form.tagIds.includes(id) ? form.tagIds.filter((x) => x !== id) : [...form.tagIds, id]
}

async function submit() {
  if (form.title.trim().length < 3) {
    errors.value = { title: t('deckForm.titlePlaceholder') }
    return
  }
  saving.value = true
  try {
    const payload: DeckPayload = { ...form, title: form.title.trim(), description: form.description.trim() || null }
    const saved = props.deck ? await deckApi.update(props.deck.id, payload) : await deckApi.create(payload)
    emit('saved', saved)
  } catch (e) {
    if (e instanceof ApiError) errors.value = e.fieldErrors
    showError(e)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="deck ? $t('deckForm.editTitle') : $t('deckForm.createTitle')" wide @close="emit('close')">
    <form id="deck-form" class="form" @submit.prevent="submit">
      <div class="preview" :data-color="form.color">
        <span class="preview-icon">{{ form.icon }}</span>
        <span class="preview-title">{{ form.title || $t('deckForm.titlePlaceholder') }}</span>
      </div>

      <div class="field">
        <label for="deck-title">{{ $t('deckForm.title') }}</label>
        <input id="deck-title" v-model="form.title" class="input" :class="{ 'is-invalid': errors.title }"
               :placeholder="$t('deckForm.titlePlaceholder')" maxlength="255" autofocus />
        <span v-if="errors.title" class="field-error">{{ errors.title }}</span>
      </div>

      <div class="field">
        <label for="deck-desc">{{ $t('deckForm.description') }}</label>
        <textarea id="deck-desc" v-model="form.description" class="textarea" rows="2" maxlength="2000"
                  :placeholder="$t('deckForm.descriptionPlaceholder')" />
      </div>

      <div class="two">
        <div class="field">
          <label for="deck-source">{{ $t('deckForm.sourceLanguage') }}</label>
          <select id="deck-source" v-model.number="form.sourceLanguageId" class="select">
            <option v-for="l in languages" :key="l.id" :value="l.id">{{ l.label }}</option>
          </select>
        </div>
        <div class="field">
          <label for="deck-target">{{ $t('deckForm.targetLanguage') }}</label>
          <select id="deck-target" v-model.number="form.targetLanguageId" class="select">
            <option v-for="l in languages" :key="l.id" :value="l.id">{{ l.label }}</option>
          </select>
        </div>
      </div>

      <div class="field">
        <span class="label">{{ $t('deckForm.icon') }}</span>
        <div class="icons">
          <button v-for="icon in ICONS" :key="icon" type="button" class="icon-btn" :class="{ active: form.icon === icon }"
                  @click="form.icon = icon">{{ icon }}</button>
        </div>
      </div>

      <div class="field">
        <span class="label">{{ $t('deckForm.color') }}</span>
        <div class="colors">
          <button v-for="c in COLORS" :key="c" type="button" class="color" :data-color="c"
                  :class="{ active: form.color === c }" :aria-label="c" @click="form.color = c" />
        </div>
      </div>

      <div v-if="dictionary.tags.length" class="field">
        <span class="label">{{ $t('deckForm.tags') }}</span>
        <div class="row-wrap">
          <button v-for="tag in dictionary.tags" :key="tag.id" type="button" class="chip"
                  :class="{ 'is-active': form.tagIds.includes(tag.id) }" @click="toggleTag(tag.id)">
            #{{ tag.name }}
          </button>
        </div>
      </div>

      <label class="switch-row">
        <span class="grow">
          <span class="bold">{{ $t('deckForm.isPublic') }}</span>
          <span class="muted small block">{{ $t('deckForm.isPublicHint') }}</span>
        </span>
        <input v-model="form.isPublic" type="checkbox" class="switch" />
      </label>
    </form>

    <template #footer>
      <button class="btn btn-ghost" type="button" @click="emit('close')">{{ $t('common.cancel') }}</button>
      <button class="btn btn-primary" type="submit" form="deck-form" :disabled="saving">
        {{ deck ? $t('common.save') : $t('common.create') }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.preview {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  height: 84px;
  padding: 0 20px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
  color: #fff;
  overflow: hidden;
  transition: background 0.4s ease;
}

.preview-icon {
  font-size: 2.6rem;
  filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.25));
}

.preview-title {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 1.05rem;
}

.two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.icons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.icon-btn {
  width: 44px;
  height: 44px;
  font-size: 1.4rem;
  border-radius: 12px;
  border: 2px solid transparent;
  background: var(--surface-sunken);
  transition: transform 0.25s var(--ease-spring), border-color 0.2s ease;
}

.icon-btn:hover {
  transform: scale(1.12);
}

.icon-btn.active {
  border-color: var(--primary);
  background: var(--primary-soft);
}

.colors {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.color {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 3px solid var(--surface-strong);
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
  box-shadow: 0 0 0 2px transparent;
  transition: transform 0.25s var(--ease-spring), box-shadow 0.2s ease;
}

.color:hover {
  transform: scale(1.12);
}

.color.active {
  box-shadow: 0 0 0 2px var(--primary);
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

.block {
  display: block;
}

@media (max-width: 600px) {
  .two {
    grid-template-columns: 1fr;
  }
}
</style>
