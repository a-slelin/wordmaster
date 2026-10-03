<script setup lang="ts">
import { BadgeCheck, Copy, Heart } from 'lucide-vue-next'
import type { CatalogDeck } from '@/api'
import LanguagePair from './LanguagePair.vue'

defineProps<{ deck: CatalogDeck }>()
defineEmits<{ open: [deck: CatalogDeck] }>()
</script>

<template>
  <button type="button" class="deck glass" :data-color="deck.color ?? 'violet'" @click="$emit('open', deck)">
    <div class="cover">
      <span class="icon">{{ deck.icon ?? '📚' }}</span>
      <span v-if="deck.isOfficial" class="official"><BadgeCheck :size="14" /> {{ $t('common.official') }}</span>
    </div>
    <div class="body">
      <h3 class="title">{{ deck.title }}</h3>
      <p v-if="deck.description" class="desc muted small">{{ deck.description }}</p>
      <div class="tags">
        <span v-for="tag in deck.tags.slice(0, 3)" :key="tag.id" class="badge">#{{ tag.name }}</span>
      </div>
      <div class="foot">
        <LanguagePair :source="deck.sourceLanguage" :target="deck.targetLanguage" />
        <span class="spacer" />
        <span class="stat" :class="{ liked: deck.likedByMe }"><Heart :size="15" /> {{ deck.likesCount }}</span>
        <span class="stat"><Copy :size="15" /> {{ deck.copiesCount }}</span>
      </div>
      <div class="author tiny subtle">
        {{ $t('common.cards', { n: deck.cardsCount }, deck.cardsCount) }} · @{{ deck.ownerUsername }}
      </div>
    </div>
  </button>
</template>

<style scoped>
.deck {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0;
  text-align: left;
  color: var(--text);
  transition: transform 0.35s var(--ease-spring), box-shadow 0.3s ease;
}

.deck:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.cover {
  position: relative;
  height: 92px;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
}

.cover::after {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 10%, rgba(255, 255, 255, 0.35), transparent 50%);
}

.icon {
  position: absolute;
  left: 18px;
  bottom: -12px;
  font-size: 3rem;
  line-height: 1;
  filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.25));
  transition: transform 0.45s var(--ease-spring);
}

.deck:hover .icon {
  transform: rotate(-10deg) scale(1.08);
}

.official {
  position: absolute;
  z-index: 1;
  top: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 9px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.92);
  color: #4b3bb5;
  font-size: 0.72rem;
  font-weight: 800;
}

.body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 22px 18px 16px;
  flex: 1;
}

.title {
  font-size: 1rem;
}

.desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.foot {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: auto;
  padding-top: 6px;
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  font-weight: 800;
  color: var(--text-muted);
}

.stat.liked {
  color: var(--danger);
}

.stat.liked svg {
  fill: currentColor;
}
</style>
