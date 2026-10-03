<script setup lang="ts">
import { useUiStore } from '@/stores/ui'
import BaseModal from './BaseModal.vue'

const ui = useUiStore()
</script>

<template>
  <BaseModal :open="ui.confirmRequest !== null" :title="ui.confirmRequest?.title" @close="ui.resolveConfirm(false)">
    <p v-if="ui.confirmRequest?.text" class="muted">{{ ui.confirmRequest.text }}</p>
    <template #footer>
      <button class="btn btn-ghost" type="button" @click="ui.resolveConfirm(false)">{{ $t('common.cancel') }}</button>
      <button class="btn" :class="ui.confirmRequest?.danger ? 'btn-danger' : 'btn-primary'" type="button"
              @click="ui.resolveConfirm(true)">
        {{ ui.confirmRequest?.confirmText ?? $t('common.confirm') }}
      </button>
    </template>
  </BaseModal>
</template>
