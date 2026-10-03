<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppShell from '@/layouts/AppShell.vue'
import ToastHost from '@/components/ToastHost.vue'
import ConfirmHost from '@/components/ConfirmHost.vue'

const route = useRoute()
const layout = computed(() => route.meta.layout ?? 'app')
</script>

<template>
  <div class="aurora" aria-hidden="true"><span /></div>
  <AppShell v-if="layout === 'app'">
    <RouterView v-slot="{ Component, route: r }">
      <Transition name="page" mode="out-in">
        <component :is="Component" :key="r.path" />
      </Transition>
    </RouterView>
  </AppShell>
  <RouterView v-else v-slot="{ Component, route: r }">
    <Transition name="page" mode="out-in">
      <component :is="Component" :key="r.path" />
    </Transition>
  </RouterView>
  <ToastHost />
  <ConfirmHost />
</template>
