<script setup lang="ts">
import { CircleAlert, CircleCheck, Info, Sparkles } from 'lucide-vue-next'
import { useUiStore } from '@/stores/ui'

const ui = useUiStore()
const icons = { success: CircleCheck, error: CircleAlert, info: Info, xp: Sparkles }
</script>

<template>
  <div class="toasts" aria-live="polite">
    <TransitionGroup name="toast">
      <button v-for="t in ui.toasts" :key="t.id" class="toast glass" :class="t.kind" type="button" @click="ui.dismiss(t.id)">
        <span class="icon">
          <template v-if="t.icon">{{ t.icon }}</template>
          <component :is="icons[t.kind]" v-else :size="20" />
        </span>
        <span class="text">{{ t.text }}</span>
      </button>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toasts {
  position: fixed;
  z-index: 200;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  pointer-events: none;
  width: min(440px, calc(100% - 32px));
}

.toast {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: var(--surface-strong);
  box-shadow: var(--shadow-lg);
  text-align: left;
  font-weight: 700;
  border: 1px solid var(--border);
}

.icon {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 10px;
  font-size: 1.1rem;
}

.success .icon { background: var(--success-soft); color: var(--success); }
.error .icon { background: var(--danger-soft); color: var(--danger); }
.info .icon { background: var(--info-soft); color: var(--info); }
.xp .icon { background: var(--warning-soft); color: var(--warning); }

.toast-enter-active { transition: all 0.45s var(--ease-spring); }
.toast-leave-active { transition: all 0.25s ease; }
.toast-enter-from { opacity: 0; transform: translateY(-20px) scale(0.9); }
.toast-leave-to { opacity: 0; transform: scale(0.95); }
</style>
