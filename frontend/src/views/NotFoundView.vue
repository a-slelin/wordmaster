<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="nf">
    <div class="code gradient-text">404</div>
    <h1>{{ $t('notFound.title') }}</h1>
    <p class="muted">{{ $t('notFound.text') }}</p>
    <RouterLink :to="auth.isAuthenticated ? '/home' : '/'" class="btn btn-primary btn-lg">{{ $t('notFound.home') }}</RouterLink>
  </div>
</template>

<style scoped>
.nf {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  text-align: center;
  padding: 24px;
}

.code {
  font-family: var(--font-display);
  font-size: clamp(6rem, 22vw, 12rem);
  font-weight: 700;
  line-height: 1;
  animation: float 5s ease-in-out infinite;
}
</style>
