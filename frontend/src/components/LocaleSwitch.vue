<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { setLocale, type Locale } from '@/i18n'

const { locale } = useI18n()

function toggle() {
  setLocale((locale.value === 'ru' ? 'en' : 'ru') as Locale)
}
</script>

<template>
  <button class="btn btn-ghost btn-sm locale" type="button" @click="toggle">
    {{ locale === 'ru' ? '🇷🇺 RU' : '🇬🇧 EN' }}
  </button>
</template>

<style scoped>
.locale {
  font-weight: 800;
}
</style>
