<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ApiError, cardApi, deckApi, type Card, type CardPayload } from '@/api'
import { useErrorToast } from '@/composables/useErrorToast'
import BaseModal from './BaseModal.vue'

const props = defineProps<{ open: boolean; deckId: string; card?: Card | null }>()
const emit = defineEmits<{ close: []; saved: [card: Card, created: boolean] }>()

const showError = useErrorToast()
const form = reactive({ word: '', translation: '', transcription: '', exampleSentence: '', imageUrl: '' })
const errors = ref<Record<string, string>>({})
const saving = ref(false)
const keepOpen = ref(true)

watch(
  () => props.open,
  (open) => {
    if (!open) return
    errors.value = {}
    Object.assign(form, {
      word: props.card?.word ?? '',
      translation: props.card?.translation ?? '',
      transcription: props.card?.transcription ?? '',
      exampleSentence: props.card?.exampleSentence ?? '',
      imageUrl: props.card?.imageUrl ?? '',
    })
  },
  { immediate: true },
)

async function submit() {
  if (!form.word.trim() || !form.translation.trim()) {
    errors.value = { word: form.word.trim() ? '' : '!', translation: form.translation.trim() ? '' : '!' }
    return
  }
  saving.value = true
  const payload: CardPayload = {
    word: form.word.trim(),
    translation: form.translation.trim(),
    transcription: form.transcription.trim() || null,
    exampleSentence: form.exampleSentence.trim() || null,
    imageUrl: form.imageUrl.trim() || null,
  }
  try {
    if (props.card) {
      emit('saved', await cardApi.update(props.card.id, payload), false)
    } else {
      emit('saved', await deckApi.addCard(props.deckId, payload), true)
      if (keepOpen.value) {
        Object.assign(form, { word: '', translation: '', transcription: '', exampleSentence: '', imageUrl: '' })
        document.getElementById('card-word')?.focus()
      }
    }
  } catch (e) {
    if (e instanceof ApiError) errors.value = e.fieldErrors
    showError(e)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <BaseModal :open="open" :title="card ? $t('cards.editTitle') : $t('cards.addTitle')" @close="emit('close')">
    <form id="card-form" class="stack form" @submit.prevent="submit">
      <div class="two">
        <div class="field">
          <label for="card-word">{{ $t('cards.word') }}</label>
          <input id="card-word" v-model="form.word" class="input" :class="{ 'is-invalid': errors.word }"
                 :placeholder="$t('cards.wordPlaceholder')" maxlength="255" autofocus />
        </div>
        <div class="field">
          <label for="card-translation">{{ $t('cards.translation') }}</label>
          <input id="card-translation" v-model="form.translation" class="input" :class="{ 'is-invalid': errors.translation }"
                 :placeholder="$t('cards.translationPlaceholder')" maxlength="255" />
        </div>
      </div>
      <div class="field">
        <label for="card-ipa">{{ $t('cards.transcription') }}</label>
        <input id="card-ipa" v-model="form.transcription" class="input" :placeholder="$t('cards.transcriptionPlaceholder')"
               maxlength="255" />
      </div>
      <div class="field">
        <label for="card-example">{{ $t('cards.example') }}</label>
        <textarea id="card-example" v-model="form.exampleSentence" class="textarea" rows="2" maxlength="2000"
                  :placeholder="$t('cards.examplePlaceholder')" />
      </div>
      <div class="field">
        <label for="card-image">{{ $t('cards.imageUrl') }}</label>
        <input id="card-image" v-model="form.imageUrl" class="input" :class="{ 'is-invalid': errors.imageUrl }"
               placeholder="https://…" maxlength="1024" />
        <span v-if="errors.imageUrl" class="field-error">{{ errors.imageUrl }}</span>
      </div>
    </form>
    <template #footer>
      <label v-if="!card" class="row small muted keep">
        <input v-model="keepOpen" type="checkbox" /> {{ $t('cards.addAnother') }}
      </label>
      <span class="spacer" />
      <button class="btn btn-ghost" type="button" @click="emit('close')">{{ $t('common.cancel') }}</button>
      <button class="btn btn-primary" type="submit" form="card-form" :disabled="saving">
        {{ card ? $t('common.save') : $t('common.add') }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.form {
  gap: 16px;
}

.two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.keep {
  gap: 6px;
  cursor: pointer;
}

@media (max-width: 600px) {
  .two {
    grid-template-columns: 1fr;
  }
}
</style>
