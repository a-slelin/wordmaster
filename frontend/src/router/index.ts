import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    /** true - only for authenticated users, 'guest' - only for anonymous ones. */
    auth?: boolean | 'guest'
    admin?: boolean
    /** Layout: application shell with navigation or a bare full screen page. */
    layout?: 'app' | 'bare'
  }
}

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'landing', component: () => import('@/views/LandingView.vue'), meta: { auth: 'guest', layout: 'bare' } },
  { path: '/login', name: 'login', component: () => import('@/views/AuthView.vue'), props: { mode: 'login' }, meta: { auth: 'guest', layout: 'bare' } },
  { path: '/register', name: 'register', component: () => import('@/views/AuthView.vue'), props: { mode: 'register' }, meta: { auth: 'guest', layout: 'bare' } },
  { path: '/home', name: 'home', component: () => import('@/views/DashboardView.vue'), meta: { auth: true, layout: 'app' } },
  { path: '/decks', name: 'decks', component: () => import('@/views/DecksView.vue'), meta: { auth: true, layout: 'app' } },
  { path: '/decks/:id', name: 'deck', component: () => import('@/views/DeckView.vue'), props: true, meta: { auth: true, layout: 'app' } },
  { path: '/explore', name: 'explore', component: () => import('@/views/ExploreView.vue'), meta: { layout: 'app' } },
  { path: '/train/:sessionId', name: 'train', component: () => import('@/views/TrainingView.vue'), props: true, meta: { auth: true, layout: 'bare' } },
  { path: '/stats', name: 'stats', component: () => import('@/views/StatsView.vue'), meta: { auth: true, layout: 'app' } },
  { path: '/profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { auth: true, layout: 'app' } },
  { path: '/admin', name: 'admin', component: () => import('@/views/AdminView.vue'), meta: { auth: true, admin: true, layout: 'app' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue'), meta: { layout: 'bare' } },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(_to, _from, saved) {
    return saved ?? { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.init()

  if (to.meta.auth === true && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.auth === 'guest' && auth.isAuthenticated) {
    return { name: 'home' }
  }
  if (to.meta.admin && !auth.isAdmin) {
    return { name: 'home' }
  }
  return true
})
