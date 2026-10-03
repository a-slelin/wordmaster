<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowRight } from 'lucide-vue-next'
import type { Language } from '@/api'
import { languageName } from '@/utils/format'

const props = defineProps<{ source: Language; target: Language; showNames?: boolean }>()
const { locale } = useI18n()
const targetName = computed(() => languageName(props.target.code, locale.value, props.target.name))
</script>

<template>
  <span class="pair" :title="`${source.name} → ${target.name}`">
    <span class="flag">{{ source.flag ?? source.code }}</span>
    <ArrowRight :size="13" class="arrow" />
    <span class="flag">{{ target.flag ?? target.code }}</span>
    <span v-if="showNames" class="name">{{ targetName }}</span>
  </span>
</template>

<style scoped>
.pair {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-muted);
}

.flag {
  font-size: 1.05rem;
  line-height: 1;
}

.arrow {
  color: var(--text-subtle);
}

.name {
  margin-left: 4px;
}
</style>
