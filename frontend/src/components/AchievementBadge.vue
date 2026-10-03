<script setup lang="ts">
import { computed } from 'vue'
import type { Achievement } from '@/api'

const props = defineProps<{ achievement: Achievement; compact?: boolean }>()
const percent = computed(() => Math.round((props.achievement.progress / props.achievement.goal) * 100))
</script>

<template>
  <div class="ach" :class="{ locked: !achievement.unlocked, compact }">
    <span class="medal">{{ achievement.icon }}</span>
    <div class="info">
      <b>{{ $t(`achievements.${achievement.code}.title`) }}</b>
      <span class="tiny muted">{{ $t(`achievements.${achievement.code}.text`) }}</span>
      <div v-if="!achievement.unlocked && !compact" class="row prog">
        <div class="progress grow"><span :style="{ width: `${percent}%` }" /></div>
        <span class="tiny subtle bold">{{ achievement.progress }}/{{ achievement.goal }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ach {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: var(--radius-md);
  background: var(--surface-sunken);
  transition: transform 0.3s var(--ease-spring);
}

.ach:hover {
  transform: translateY(-3px);
}

.medal {
  display: grid;
  place-items: center;
  flex: none;
  width: 56px;
  height: 56px;
  border-radius: 18px;
  font-size: 1.8rem;
  background: var(--grad-warm);
  box-shadow: 0 10px 24px -10px rgba(255, 122, 89, 0.7);
}

.locked .medal {
  background: var(--surface-strong);
  filter: grayscale(1);
  opacity: 0.55;
  box-shadow: none;
}

.info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.prog {
  gap: 8px;
  margin-top: 6px;
}

.prog .progress {
  height: 6px;
}
</style>
