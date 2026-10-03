<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowLeftRight, Headphones, Keyboard, Layers, ListChecks, Play } from 'lucide-vue-next'
import type { DeckProgress, TrainingMode, TrainingScope } from '@/api'
import { usePrefsStore } from '@/stores/prefs'
import { useTrainingStore } from '@/stores/training'
import { useErrorToast } from '@/composables/useErrorToast'
import BaseModal from './BaseModal.vue'

const props = defineProps<{ open: boolean; deckId: string; progress?: DeckProgress | null }>()
const emit = defineEmits<{ close: [] }>()

const { t } = useI18n()
const prefs = usePrefsStore()
const training = useTrainingStore()
const showError = useErrorToast()
const starting = ref(false)

const modes: { value: TrainingMode; icon: unknown; color: string }[] = [
  { value: 'flashcards', icon: Layers, color: 'violet' },
  { value: 'typing', icon: Keyboard, color: 'sky' },
  { value: 'choice', icon: ListChecks, color: 'teal' },
  { value: 'listening', icon: Headphones, color: 'amber' },
]
const scopes: TrainingScope[] = ['smart', 'all', 'hard']
const counts = [10, 20, 30, 50]

const hint = computed(() => {
  const p = props.progress
  if (!p) return ''
  return `${t('deck.due')}: ${p.dueCards} · ${t('deck.new')}: ${p.newCards}`
})

async function start() {
  starting.value = true
  try {
    await training.start(props.deckId)
    emit('close')
  } catch (e) {
    showError(e)
  } finally {
    starting.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="$t('training.setupTitle')" wide @close="emit('close')">
    <div class="setup">
      <div class="field">
        <span class="label">{{ $t('training.mode') }}</span>
        <div class="modes">
          <button v-for="m in modes" :key="m.value" type="button" class="mode" :data-color="m.color"
                  :class="{ active: prefs.trainingMode === m.value }" @click="prefs.trainingMode = m.value">
            <span class="mode-icon"><component :is="m.icon" :size="24" /></span>
            <b>{{ $t(`training.modes.${m.value}`) }}</b>
            <span class="tiny muted">{{ $t(`training.modeHints.${m.value}`) }}</span>
          </button>
        </div>
      </div>

      <div class="field" :class="{ disabled: prefs.trainingMode === 'listening' }">
        <span class="label">{{ $t('training.direction') }}</span>
        <div class="segmented">
          <button type="button" :class="{ active: prefs.trainingDirection === 'forward' }"
                  @click="prefs.trainingDirection = 'forward'">{{ $t('training.forward') }}</button>
          <ArrowLeftRight :size="16" class="subtle" />
          <button type="button" :class="{ active: prefs.trainingDirection === 'reverse' }"
                  @click="prefs.trainingDirection = 'reverse'">{{ $t('training.reverse') }}</button>
        </div>
      </div>

      <div class="field">
        <span class="label">{{ $t('training.scope') }} <span v-if="hint" class="subtle">· {{ hint }}</span></span>
        <div class="scopes">
          <button v-for="s in scopes" :key="s" type="button" class="scope" :class="{ active: prefs.trainingScope === s }"
                  @click="prefs.trainingScope = s">
            <b>{{ $t(`training.scopes.${s}`) }}</b>
            <span class="tiny muted">{{ $t(`training.scopeHints.${s}`) }}</span>
          </button>
        </div>
      </div>

      <div class="field">
        <span class="label">{{ $t('training.count') }}</span>
        <div class="row-wrap">
          <button v-for="c in counts" :key="c" type="button" class="chip" :class="{ 'is-active': prefs.trainingLimit === c }"
                  @click="prefs.trainingLimit = c">{{ c }}</button>
        </div>
      </div>
    </div>

    <template #footer>
      <button class="btn btn-primary btn-lg btn-block" type="button" :disabled="starting" @click="start">
        <Play :size="20" /> {{ $t('training.start') }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.setup {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.modes {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.mode {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 18px 10px;
  border-radius: var(--radius-md);
  border: 2px solid var(--border);
  background: var(--surface-sunken);
  text-align: center;
  transition: all 0.3s var(--ease-spring);
}

.mode:hover {
  transform: translateY(-3px);
}

.mode.active {
  border-color: var(--deck-a);
  background: color-mix(in srgb, var(--deck-a) 12%, transparent);
  box-shadow: 0 10px 30px -12px var(--deck-a);
}

.mode-icon {
  display: grid;
  place-items: center;
  width: 50px;
  height: 50px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
}

.segmented {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px;
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
  width: fit-content;
  max-width: 100%;
}

.segmented button {
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--text-muted);
  font-weight: 800;
  transition: all 0.25s ease;
}

.segmented button.active {
  background: var(--surface-strong);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.scopes {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.scope {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px;
  text-align: left;
  border-radius: var(--radius-md);
  border: 2px solid var(--border);
  background: var(--surface-sunken);
  transition: all 0.25s ease;
}

.scope.active {
  border-color: var(--primary);
  background: var(--primary-soft);
}

@media (max-width: 640px) {
  .modes {
    grid-template-columns: 1fr 1fr;
  }

  .scopes {
    grid-template-columns: 1fr;
  }
}
</style>
