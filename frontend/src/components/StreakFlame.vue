<script setup lang="ts">
withDefaults(defineProps<{ days: number; active?: boolean; size?: number }>(), { active: true, size: 28 })

// Unique per instance: a gradient referenced from a hidden SVG (display: none) is not rendered.
const gid = `flame-${Math.random().toString(36).slice(2, 9)}`
</script>

<template>
  <span class="flame" :class="{ off: !active || days === 0 }" :style="{ '--s': `${size}px` }">
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <defs>
        <linearGradient :id="gid" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stop-color="#ffd166" />
          <stop offset="0.55" stop-color="#ff7a3d" />
          <stop offset="1" stop-color="#ff3d6e" />
        </linearGradient>
      </defs>
      <path
        :fill="`url(#${gid})`"
        d="M12 2c.5 3.2-1.4 5-3 6.8C7.4 10.6 6 12.4 6 15a6 6 0 0 0 12 0c0-2.4-1.1-4-2.2-5.5-.3 1.4-1 2.4-2.1 2.9.6-3.4-.4-7.6-1.7-10.4Z"
      />
      <path fill="#fff4c2" opacity=".85" d="M12 21a3 3 0 0 1-3-3c0-1.6 1.2-2.6 2-3.6.3 1 1 1.7 1.8 2 .2-.9.1-1.8-.2-2.6 1.4.9 2.4 2.3 2.4 4.2a3 3 0 0 1-3 3Z" />
    </svg>
    <b>{{ days }}</b>
  </span>
</template>

<style scoped>
.flame {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-display);
  font-weight: 700;
  color: #ff7a3d;
}

svg {
  width: var(--s);
  height: var(--s);
  transform-origin: 50% 90%;
  animation: flicker 1.6s ease-in-out infinite;
  filter: drop-shadow(0 4px 10px rgba(255, 122, 61, 0.45));
}

.off {
  color: var(--text-subtle);
}

.off svg {
  filter: grayscale(1) opacity(0.45);
  animation: none;
}

@keyframes flicker {
  0%, 100% { transform: scale(1) rotate(-2deg); }
  50% { transform: scale(1.08, 1.12) rotate(2deg); }
}
</style>
