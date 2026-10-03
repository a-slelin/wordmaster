<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ForecastDay } from '@/api'
import { parseDate } from '@/utils/format'

const props = defineProps<{ days: ForecastDay[] }>()
const { t, locale } = useI18n()

const max = computed(() => Math.max(1, ...props.days.map((d) => d.dueCount)))

function label(day: ForecastDay, index: number) {
  if (index === 0) return t('stats.today')
  if (index === 1) return t('stats.tomorrow')
  return parseDate(day.date)?.toLocaleDateString(locale.value, { weekday: 'short' }) ?? ''
}
</script>

<template>
  <div class="chart">
    <div v-for="(day, i) in days" :key="day.date" class="bar-col" :title="`${day.date}: ${day.dueCount}`">
      <span class="value tiny bold">{{ day.dueCount || '' }}</span>
      <div class="bar-track">
        <div class="bar" :class="{ today: i === 0 }" :style="{ height: `${(day.dueCount / max) * 100}%` }" />
      </div>
      <span class="label tiny">{{ label(day, i) }}</span>
    </div>
  </div>
</template>

<style scoped>
.chart {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(28px, 1fr);
  gap: 8px;
  height: 200px;
  overflow-x: auto;
}

.bar-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.bar-track {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  border-radius: 10px;
  background: var(--surface-sunken);
  overflow: hidden;
}

.bar {
  width: 100%;
  min-height: 3px;
  border-radius: 10px;
  background: linear-gradient(180deg, #3ba7ff, #7c5cff);
  transition: height 0.8s var(--ease-out);
}

.bar.today {
  background: var(--grad-warm);
}

.value {
  min-height: 1em;
  color: var(--text-muted);
}

.label {
  color: var(--text-subtle);
  font-weight: 700;
  white-space: nowrap;
}
</style>
