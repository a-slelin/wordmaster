<script setup lang="ts">
import { onBeforeUnmount, onMounted, watch } from 'vue'
import { X } from 'lucide-vue-next'

const props = defineProps<{ open: boolean; title?: string; wide?: boolean }>()
const emit = defineEmits<{ close: [] }>()

function onKey(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.open) emit('close')
}

watch(
  () => props.open,
  (open) => {
    document.body.style.overflow = open ? 'hidden' : ''
  },
)

onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="overlay" @mousedown.self="emit('close')">
        <div class="modal glass" :class="{ wide }" role="dialog" aria-modal="true">
          <header v-if="title || $slots.header" class="head">
            <slot name="header">
              <h2>{{ title }}</h2>
            </slot>
            <button class="btn btn-ghost btn-icon btn-sm" type="button" :aria-label="$t('common.close')" @click="emit('close')">
              <X :size="20" />
            </button>
          </header>
          <div class="body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="foot">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(10, 6, 30, 0.45);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.modal {
  width: min(560px, 100%);
  max-height: min(88dvh, 900px);
  display: flex;
  flex-direction: column;
  background: var(--surface-strong);
  box-shadow: var(--shadow-lg);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.modal.wide {
  width: min(820px, 100%);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 22px 24px 8px;
}

.body {
  padding: 14px 24px 24px;
  overflow-y: auto;
}

.foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 24px 22px;
  border-top: 1px solid var(--border);
}

.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}

.modal-enter-active .modal {
  transition: transform 0.45s var(--ease-spring), opacity 0.25s ease;
}

.modal-leave-active .modal {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal {
  transform: translateY(24px) scale(0.96);
}

.modal-leave-to .modal {
  transform: scale(0.97);
}

@media (max-width: 600px) {
  .overlay {
    align-items: end;
    padding: 0;
  }

  .modal,
  .modal.wide {
    width: 100%;
    border-radius: var(--radius-xl) var(--radius-xl) 0 0;
    max-height: 92dvh;
  }

  .modal-enter-from .modal {
    transform: translateY(100%);
  }
}
</style>
