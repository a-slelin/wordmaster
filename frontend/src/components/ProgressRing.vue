<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{ value: number; max?: number; size?: number; stroke?: number; gradient?: 'brand' | 'warm' | 'fresh' }>(),
  { max: 100, size: 64, stroke: 7, gradient: 'brand' },
)

const id = `ring-${Math.random().toString(36).slice(2, 9)}`
const radius = computed(() => (props.size - props.stroke) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)
const ratio = computed(() => (props.max <= 0 ? 0 : Math.min(1, Math.max(0, props.value / props.max))))
const offset = computed(() => circumference.value * (1 - ratio.value))
const stops = computed(() =>
  props.gradient === 'warm' ? ['#ffb547', '#ff5c7a'] : props.gradient === 'fresh' ? ['#22d3a0', '#3ba7ff'] : ['#7c5cff', '#ff5ca8'],
)
</script>

<template>
  <div class="ring" :style="{ width: `${size}px`, height: `${size}px` }">
    <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
      <defs>
        <linearGradient :id="id" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" :stop-color="stops[0]" />
          <stop offset="1" :stop-color="stops[1]" />
        </linearGradient>
      </defs>
      <circle class="track" :cx="size / 2" :cy="size / 2" :r="radius" :stroke-width="stroke" fill="none" />
      <circle
        class="bar"
        :cx="size / 2"
        :cy="size / 2"
        :r="radius"
        :stroke-width="stroke"
        fill="none"
        :stroke="`url(#${id})`"
        stroke-linecap="round"
        :stroke-dasharray="circumference"
        :stroke-dashoffset="offset"
      />
    </svg>
    <div class="content">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.ring {
  position: relative;
  flex: none;
}

svg {
  transform: rotate(-90deg);
}

.track {
  stroke: var(--surface-sunken);
}

.bar {
  transition: stroke-dashoffset 1s var(--ease-out);
}

.content {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  text-align: center;
  font-weight: 800;
}
</style>
