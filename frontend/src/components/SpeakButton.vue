<script setup lang="ts">
import { ref } from 'vue'
import { Volume2 } from 'lucide-vue-next'
import { useSpeech } from '@/composables/useSpeech'

const props = withDefaults(defineProps<{ text: string; lang?: string; size?: 'sm' | 'md' | 'lg' }>(), {
  lang: 'en',
  size: 'md',
})

const { speak, supported } = useSpeech()
const playing = ref(false)

function play(event: Event) {
  event.stopPropagation()
  speak(props.text, props.lang)
  playing.value = true
  setTimeout(() => (playing.value = false), 700)
}
</script>

<template>
  <button
    v-if="supported"
    class="speak"
    :class="[size, { playing }]"
    type="button"
    :title="$t('common.listen')"
    :aria-label="$t('common.listen')"
    @click="play"
  >
    <Volume2 :size="size === 'lg' ? 26 : size === 'sm' ? 16 : 20" />
  </button>
</template>

<style scoped>
.speak {
  display: inline-grid;
  place-items: center;
  flex: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: var(--primary-soft);
  color: var(--primary);
  transition: transform 0.3s var(--ease-spring), background 0.2s ease;
}

.speak.sm { width: 32px; height: 32px; }
.speak.lg { width: 60px; height: 60px; }

.speak:hover { transform: scale(1.08); }
.speak:active { transform: scale(0.94); }

.speak.playing {
  animation: pulse-glow 0.7s ease-out;
}
</style>
