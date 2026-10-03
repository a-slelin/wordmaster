<script setup lang="ts">
import { ref } from 'vue'
import { Sparkles } from 'lucide-vue-next'
import type { WordOfTheDay } from '@/api'
import SpeakButton from './SpeakButton.vue'

defineProps<{ word: WordOfTheDay }>()
const flipped = ref(false)
</script>

<template>
  <div class="wotd">
    <div class="head row">
      <Sparkles :size="18" class="spark" />
      <h3>{{ $t('dashboard.wordOfDay') }}</h3>
      <span class="spacer" />
      <span class="tiny subtle">{{ $t('dashboard.tapToFlip') }}</span>
    </div>
    <button class="flip" :class="{ flipped }" type="button" @click="flipped = !flipped">
      <div class="face front">
        <div class="row center-row">
          <span class="word">{{ word.word }}</span>
          <SpeakButton :text="word.word" :lang="word.languageCode" size="sm" />
        </div>
        <span v-if="word.transcription" class="ipa">{{ word.transcription }}</span>
        <span class="tiny deck">{{ word.deckTitle }}</span>
      </div>
      <div class="face back">
        <span class="translation">{{ word.translation }}</span>
        <span v-if="word.exampleSentence" class="example">“{{ word.exampleSentence }}”</span>
      </div>
    </button>
  </div>
</template>

<style scoped>
.wotd {
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: 100%;
}

.spark {
  color: var(--warning);
}

.flip {
  position: relative;
  flex: 1;
  min-height: 190px;
  border: none;
  padding: 0;
  background: none;
  perspective: 1000px;
  transform-style: preserve-3d;
  transition: transform 0.7s var(--ease-spring);
}

.flip.flipped {
  transform: rotateY(180deg);
}

.face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  border-radius: var(--radius-lg);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  text-align: center;
}

.front {
  background: var(--grad-brand);
  color: #fff;
  box-shadow: var(--shadow-glow);
}

.front :deep(.speak) {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.back {
  transform: rotateY(180deg);
  background: var(--surface-strong);
  border: 1px solid var(--border);
}

.center-row {
  justify-content: center;
}

.word {
  font-family: var(--font-display);
  font-size: 1.9rem;
  font-weight: 700;
}

.ipa {
  opacity: 0.85;
  font-weight: 600;
}

.deck {
  opacity: 0.7;
  font-weight: 700;
}

.translation {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 600;
}

.example {
  color: var(--text-muted);
  font-style: italic;
}
</style>
