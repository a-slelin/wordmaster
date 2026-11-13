<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { ActivityDay } from '@/api'
import { isoDate, parseDate } from '@/utils/format'

const props = withDefaults(defineProps<{ days: ActivityDay[]; goal: number; weeks?: number }>(), { weeks: 26 })
const { t, locale } = useI18n()

interface Cell {
  date: string
  count: number
  level: number
  future: boolean
}

const byDate = computed(() => new Map(props.days.map((d) => [d.date, d.cardsReviewed])))

const columns = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const dayOfWeek = (today.getDay() + 6) % 7 // Monday = 0
  const start = new Date(today)
  start.setDate(today.getDate() - dayOfWeek - (props.weeks - 1) * 7)

  const result: Cell[][] = []
  for (let w = 0; w < props.weeks; w++) {
    const column: Cell[] = []
    for (let d = 0; d < 7; d++) {
      const date = new Date(start)
      date.setDate(start.getDate() + w * 7 + d)
      const key = isoDate(date)
      const count = byDate.value.get(key) ?? 0
      const ratio = count / Math.max(1, props.goal)
      const level = count === 0 ? 0 : ratio < 0.25 ? 1 : ratio < 0.6 ? 2 : ratio < 1 ? 3 : 4
      column.push({ date: key, count, level, future: date > today })
    }
    result.push(column)
  }
  return result
})

const months = computed(() =>
  columns.value.map((column, i) => {
    const first = parseDate(column[0].date) as Date
    const prev = i > 0 ? parseDate(columns.value[i - 1][0].date) : null
    return !prev || prev.getMonth() !== first.getMonth()
      ? first.toLocaleDateString(locale.value, { month: 'short' })
      : ''
  }),
)

function title(cell: Cell) {
  const date = (parseDate(cell.date) as Date).toLocaleDateString(locale.value, { day: 'numeric', month: 'long' })
  return `${date}: ${t('stats.reviewed', { n: cell.count })}`
}
</script>

<template>
  <div class="heatmap">
    <div class="scroll">
      <div class="months">
        <span v-for="(m, i) in months" :key="i">{{ m }}</span>
      </div>
      <div class="cols">
        <div v-for="(column, i) in columns" :key="i" class="col">
          <span v-for="cell in column" :key="cell.date" class="cell" :class="[`l${cell.level}`, { future: cell.future }]"
                :title="title(cell)" />
        </div>
      </div>
    </div>
    <div class="legend tiny muted">
      <span>{{ $t('stats.less') }}</span>
      <span v-for="l in 5" :key="l" class="cell" :class="`l${l - 1}`" />
      <span>{{ $t('stats.more') }}</span>
    </div>
  </div>
</template>

<style scoped>
.heatmap {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.scroll {
  overflow-x: auto;
  padding-bottom: 4px;
}

.months,
.cols {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 16px;
  gap: 4px;
}

.months span {
  font-size: 0.68rem;
  font-weight: 700;
  color: var(--text-subtle);
  white-space: nowrap;
  overflow: visible;
}

.col {
  display: grid;
  grid-template-rows: repeat(7, 16px);
  gap: 4px;
}

.cell {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 5px;
  background: var(--surface-sunken);
  transition: transform 0.2s var(--ease-spring);
}

.cell:hover {
  transform: scale(1.3);
}

.cell.l1 { background: rgba(124, 92, 255, 0.28); }
.cell.l2 { background: rgba(124, 92, 255, 0.5); }
.cell.l3 { background: rgba(124, 92, 255, 0.78); }
.cell.l4 { background: linear-gradient(135deg, #7c5cff, #ff5ca8); box-shadow: 0 2px 8px -2px rgba(255, 92, 168, 0.7); }
.cell.future { opacity: 0.25; }

.legend {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
}

.legend .cell {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}
</style>
