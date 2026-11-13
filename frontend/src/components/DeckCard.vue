<script setup lang="ts">
import { Clock, Globe } from 'lucide-vue-next'
import type { DeckSummary } from '@/api'
import LanguagePair from './LanguagePair.vue'
import ProgressRing from './ProgressRing.vue'

defineProps<{ deck: DeckSummary }>()
</script>

<template>
  <RouterLink :to="`/decks/${deck.id}`" class="deck glass" :data-color="deck.color ?? 'violet'">
    <div class="cover">
      <span class="icon">{{ deck.icon ?? '📚' }}</span>
      <span v-if="deck.dueCount > 0" class="due">
        <Clock :size="13" /> {{ $t('decks.due', { n: deck.dueCount }) }}
      </span>
      <span v-else-if="deck.isPublic" class="due soft"><Globe :size="13" /> {{ $t('common.public') }}</span>
    </div>
    <div class="body">
      <h3 class="title">{{ deck.title }}</h3>
      <div class="meta">
        <LanguagePair :source="deck.sourceLanguage" :target="deck.targetLanguage" />
        <span class="dot">·</span>
        <span class="muted small bold">{{ $t('common.cards', { n: deck.cardsCount }, deck.cardsCount) }}</span>
      </div>
      <div class="foot">
        <ProgressRing :value="deck.percentLearned" :size="46" :stroke="5" gradient="fresh">
          <span class="pct">{{ deck.percentLearned }}%</span>
        </ProgressRing>
        <div class="learned">
          <b>{{ deck.knownCount }}</b>
          <span class="muted small">{{ $t('decks.learned') }}</span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.deck {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  color: var(--text);
  transition: transform 0.35s var(--ease-spring), box-shadow 0.3s ease;
}

.deck:hover {
  transform: translateY(-6px) rotate(-0.4deg);
  box-shadow: var(--shadow-lg);
}

.cover {
  position: relative;
  height: 108px;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
  overflow: hidden;
}

.cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 85% 20%, rgba(255, 255, 255, 0.35), transparent 45%),
    radial-gradient(circle at 10% 110%, rgba(255, 255, 255, 0.2), transparent 40%);
}

.icon {
  position: absolute;
  left: 18px;
  bottom: -14px;
  font-size: 3.6rem;
  line-height: 1;
  filter: drop-shadow(0 8px 14px rgba(0, 0, 0, 0.25));
  transition: transform 0.45s var(--ease-spring);
}

.deck:hover .icon {
  transform: translateY(-6px) rotate(-8deg) scale(1.06);
}

.due {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.92);
  color: #c2410c;
  font-size: 0.75rem;
  font-weight: 800;
}

.due.soft {
  color: #4b3bb5;
}

.body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 26px 18px 18px;
}

.title {
  font-size: 1.05rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5em;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  color: var(--text-subtle);
}

.foot {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.pct {
  font-size: 0.72rem;
}

.learned {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
</style>
