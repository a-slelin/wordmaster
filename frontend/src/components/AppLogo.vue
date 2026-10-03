<script setup lang="ts">
defineProps<{ compact?: boolean }>()

const gid = `logo-${Math.random().toString(36).slice(2, 9)}`
</script>

<template>
  <span class="logo" :class="{ compact }">
    <svg class="mark" viewBox="0 0 64 64" aria-hidden="true">
      <defs>
        <linearGradient :id="gid" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stop-color="#7c5cff" />
          <stop offset="1" stop-color="#ff5ca8" />
        </linearGradient>
      </defs>
      <rect x="10" y="6" width="40" height="50" rx="10" fill="#ffb547" transform="rotate(-12 30 31)" />
      <rect x="14" y="8" width="40" height="50" rx="10" :fill="`url(#${gid})`" />
      <path d="M22 24l5 18 5-12 5 12 5-18" fill="none" stroke="#fff" stroke-width="5" stroke-linecap="round"
            stroke-linejoin="round" />
    </svg>
    <span v-if="!compact" class="word">Word<span class="gradient-text">Master</span></span>
  </span>
</template>

<style scoped>
.logo {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.2rem;
  color: var(--text);
  letter-spacing: -0.02em;
}

.mark {
  width: 36px;
  height: 36px;
  filter: drop-shadow(0 6px 14px rgba(124, 92, 255, 0.45));
  transition: transform 0.5s var(--ease-spring);
}

.logo:hover .mark {
  transform: rotate(-8deg) scale(1.06);
}
</style>
