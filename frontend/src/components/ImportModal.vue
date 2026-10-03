<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Upload } from 'lucide-vue-next'
import { deckApi } from '@/api'
import { parseImport } from '@/utils/importParser'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'
import BaseModal from './BaseModal.vue'

const props = defineProps<{ open: boolean; deckId: string }>()
const emit = defineEmits<{ close: []; imported: [count: number] }>()

const { t } = useI18n()
const ui = useUiStore()
const showError = useErrorToast()
const text = ref('')
const saving = ref(false)

const parsed = computed(() => parseImport(text.value))

watch(
  () => props.open,
  (open) => open && (text.value = ''),
)

async function onFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (file) text.value = await file.text()
}

async function submit() {
  if (!parsed.value.cards.length) return
  saving.value = true
  try {
    const created = await deckApi.addCards(props.deckId, parsed.value.cards)
    ui.toast(t('import.done', { n: created.length }), 'success')
    emit('imported', created.length)
  } catch (e) {
    showError(e)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="$t('import.title')" wide @close="emit('close')">
    <div class="stack">
      <p class="muted small">{{ $t('import.hint') }}</p>
      <textarea v-model="text" class="textarea mono" rows="8" :placeholder="$t('import.placeholder')" />
      <label class="btn btn-ghost btn-sm file">
        <Upload :size="16" /> .txt / .csv / .tsv
        <input type="file" accept=".txt,.csv,.tsv,text/plain" @change="onFile" />
      </label>

      <template v-if="text.trim()">
        <div class="row small">
          <span class="badge badge-success">{{ $t('import.found', { n: parsed.cards.length }) }}</span>
          <span v-if="parsed.skipped" class="badge badge-warning">{{ $t('import.skipped', { n: parsed.skipped }) }}</span>
        </div>
        <div v-if="parsed.cards.length" class="preview">
          <table class="table">
            <thead>
              <tr>
                <th>{{ $t('cards.word') }}</th>
                <th>{{ $t('cards.translation') }}</th>
                <th>{{ $t('cards.transcription') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(card, i) in parsed.cards.slice(0, 50)" :key="i">
                <td class="bold">{{ card.word }}</td>
                <td>{{ card.translation }}</td>
                <td class="muted">{{ card.transcription }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>
    <template #footer>
      <button class="btn btn-ghost" type="button" @click="emit('close')">{{ $t('common.cancel') }}</button>
      <button class="btn btn-primary" type="button" :disabled="!parsed.cards.length || saving" @click="submit">
        {{ $t('import.button', { n: parsed.cards.length }) }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, monospace;
  font-size: 0.9rem;
}

.file {
  position: relative;
  align-self: flex-start;
  overflow: hidden;
}

.file input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.preview {
  max-height: 260px;
  overflow: auto;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
}
</style>
