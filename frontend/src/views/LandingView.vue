<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ArrowRight, Brain, ChartColumn, Flame, Headphones, Layers, Library, Moon, Sun } from 'lucide-vue-next'
import AppLogo from '@/components/AppLogo.vue'
import CatalogCard from '@/components/CatalogCard.vue'
import LocaleSwitch from '@/components/LocaleSwitch.vue'
import { deckApi, type CatalogDeck } from '@/api'
import { useUiStore } from '@/stores/ui'

const { t } = useI18n()
const ui = useUiStore()
const router = useRouter()
const decks = ref<CatalogDeck[]>([])
const isDark = ref(document.documentElement.dataset.theme === 'dark')

const showcase = [
  { word: 'serendipity', translation: 'счастливая случайность', ipa: '[ˌser.ənˈdɪp.ə.ti]', color: 'violet' },
  { word: 'wanderlust', translation: 'страсть к путешествиям', ipa: '[ˈwɒn.də.lʌst]', color: 'pink' },
  { word: 'Fernweh', translation: 'тоска по дальним краям', ipa: '[ˈfɛʁnˌveː]', color: 'amber' },
  { word: 'sobremesa', translation: 'беседа после обеда', ipa: '[so.βɾeˈme.sa]', color: 'teal' },
]
const active = ref(0)
const flipped = ref(false)
let timer: number | undefined

const features = computed(() => [
  { icon: Brain, title: t('landing.features.srsTitle'), text: t('landing.features.srsText'), color: 'violet' },
  { icon: Layers, title: t('landing.features.modesTitle'), text: t('landing.features.modesText'), color: 'pink' },
  { icon: Library, title: t('landing.features.decksTitle'), text: t('landing.features.decksText'), color: 'sky' },
  { icon: Flame, title: t('landing.features.gameTitle'), text: t('landing.features.gameText'), color: 'orange' },
  { icon: ChartColumn, title: t('landing.features.statsTitle'), text: t('landing.features.statsText'), color: 'teal' },
  { icon: Headphones, title: t('landing.features.voiceTitle'), text: t('landing.features.voiceText'), color: 'amber' },
])

function toggleTheme() {
  ui.cycleTheme()
  isDark.value = document.documentElement.dataset.theme === 'dark'
}

onMounted(async () => {
  timer = window.setInterval(() => {
    if (!flipped.value) {
      flipped.value = true
    } else {
      flipped.value = false
      setTimeout(() => (active.value = (active.value + 1) % showcase.length), 350)
    }
  }, 2200)
  try {
    decks.value = (await deckApi.catalog({ official: true, size: 8 })).content
  } catch {
    decks.value = []
  }
})

onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="landing">
    <header class="nav">
      <AppLogo />
      <span class="spacer" />
      <RouterLink to="/explore" class="btn btn-ghost btn-sm hide-mobile">{{ $t('landing.explore') }}</RouterLink>
      <LocaleSwitch />
      <button class="btn btn-ghost btn-icon btn-sm" type="button" @click="toggleTheme">
        <Sun v-if="isDark" :size="18" />
        <Moon v-else :size="18" />
      </button>
      <RouterLink to="/login" class="btn btn-soft btn-sm">{{ $t('nav.login') }}</RouterLink>
    </header>

    <section class="hero">
      <div class="hero-text">
        <span class="pill glass">✨ {{ $t('landing.badge') }}</span>
        <h1>
          {{ $t('landing.title1') }}<br />
          <span class="gradient-text">{{ $t('landing.title2') }}</span>
        </h1>
        <p class="lead muted">{{ $t('landing.subtitle') }}</p>
        <div class="row-wrap">
          <RouterLink to="/register" class="btn btn-primary btn-lg">
            {{ $t('landing.start') }} <ArrowRight :size="20" />
          </RouterLink>
          <RouterLink to="/explore" class="btn btn-lg">{{ $t('landing.explore') }}</RouterLink>
        </div>
        <div class="numbers">
          <div><b>640+</b><span>{{ $t('landing.statWords') }}</span></div>
          <div><b>4</b><span>{{ $t('landing.statModes') }}</span></div>
          <div><b>11</b><span>{{ $t('landing.statLanguages') }}</span></div>
          <div><b>20</b><span>{{ $t('landing.statAchievements') }}</span></div>
        </div>
      </div>

      <div class="hero-art" aria-hidden="true">
        <div class="float-chip c1 glass">🔥 7</div>
        <div class="float-chip c2 glass">+10 XP</div>
        <div class="float-chip c3 glass">🎯 100%</div>
        <div class="card-stack">
          <div class="back b2" />
          <div class="back b1" />
          <div class="flip" :class="{ flipped }" :data-color="showcase[active].color">
            <div class="face front">
              <span class="tiny bold label">EN</span>
              <span class="word">{{ showcase[active].word }}</span>
              <span class="ipa">{{ showcase[active].ipa }}</span>
            </div>
            <div class="face backface">
              <span class="tiny bold label">RU</span>
              <span class="word small-word">{{ showcase[active].translation }}</span>
              <div class="grades">
                <span>😵</span><span>🤔</span><span>🙂</span><span>😎</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <h2 class="center">{{ $t('landing.featuresTitle') }}</h2>
      <div class="features stagger">
        <article v-for="f in features" :key="f.title" class="feature glass" :data-color="f.color">
          <span class="f-icon"><component :is="f.icon" :size="24" /></span>
          <h3>{{ f.title }}</h3>
          <p class="muted">{{ f.text }}</p>
        </article>
      </div>
    </section>

    <section v-if="decks.length" class="section">
      <div class="center stack">
        <h2>{{ $t('landing.decksTitle') }}</h2>
        <p class="muted">{{ $t('landing.decksSubtitle') }}</p>
      </div>
      <div class="grid stagger">
        <CatalogCard v-for="deck in decks" :key="deck.id" :deck="deck" @open="router.push('/register')" />
      </div>
    </section>

    <section class="cta">
      <h2>{{ $t('landing.ctaTitle') }}</h2>
      <RouterLink to="/register" class="btn btn-lg cta-btn">
        {{ $t('landing.ctaButton') }} <ArrowRight :size="20" />
      </RouterLink>
    </section>

    <footer class="footer muted small">
      <AppLogo compact />
      <span>{{ $t('landing.footer') }}</span>
      <span class="spacer" />
      <a href="/swagger-ui.html" target="_blank" rel="noopener">API</a>
      <a href="https://github.com/a-slelin/wordmaster" target="_blank" rel="noopener">GitHub</a>
    </footer>
  </div>
</template>

<style scoped>
.landing {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 0;
}

.hero {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  align-items: center;
  gap: 48px;
  min-height: min(80dvh, 720px);
  padding: 24px 0 48px;
}

.hero-text {
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: stagger-in 0.8s var(--ease-out) both;
}

.pill {
  align-self: flex-start;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  font-weight: 700;
  font-size: 0.88rem;
  box-shadow: none;
}

h1 {
  font-size: clamp(2.4rem, 5.5vw, 4.2rem);
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.lead {
  font-size: 1.15rem;
  max-width: 540px;
}

.numbers {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.numbers div {
  display: flex;
  flex-direction: column;
}

.numbers b {
  font-family: var(--font-display);
  font-size: 1.6rem;
}

.numbers span {
  color: var(--text-muted);
  font-size: 0.85rem;
  font-weight: 700;
}

.hero-art {
  position: relative;
  height: 440px;
  display: grid;
  place-items: center;
  perspective: 1400px;
}

.card-stack {
  position: relative;
  width: min(360px, 80vw);
  height: 250px;
}

.back {
  position: absolute;
  inset: 0;
  border-radius: var(--radius-xl);
  background: var(--surface-strong);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-md);
}

.b1 { transform: rotate(-6deg) translate(-14px, 10px); }
.b2 { transform: rotate(7deg) translate(18px, 14px); opacity: 0.7; }

.flip {
  position: absolute;
  inset: 0;
  transform-style: preserve-3d;
  transition: transform 0.7s var(--ease-spring);
}

.flip.flipped {
  transform: rotateY(180deg);
}

.face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-radius: var(--radius-xl);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  box-shadow: var(--shadow-lg);
  padding: 24px;
  text-align: center;
}

.front {
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
  color: #fff;
}

.backface {
  transform: rotateY(180deg);
  background: var(--surface-strong);
  border: 1px solid var(--border);
}

.label {
  position: absolute;
  top: 18px;
  left: 20px;
  opacity: 0.7;
  letter-spacing: 0.1em;
}

.word {
  font-family: var(--font-display);
  font-size: 2.2rem;
  font-weight: 700;
}

.small-word {
  font-size: 1.5rem;
}

.ipa {
  opacity: 0.85;
  font-weight: 600;
}

.grades {
  display: flex;
  gap: 10px;
  font-size: 1.6rem;
}

.float-chip {
  position: absolute;
  z-index: 2;
  padding: 10px 16px;
  border-radius: var(--radius-full);
  font-weight: 800;
  animation: float 5s ease-in-out infinite;
}

.c1 { top: 40px; left: 6%; --r: -6deg; }
.c2 { top: 70px; right: 4%; color: var(--warning); --r: 5deg; animation-delay: -1.5s; }
.c3 { bottom: 40px; left: 18%; color: var(--success); --r: 3deg; animation-delay: -3s; }

.section {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 56px 0;
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.feature {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 26px;
  transition: transform 0.35s var(--ease-spring);
}

.feature:hover {
  transform: translateY(-6px);
}

.f-icon {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--deck-a), var(--deck-b));
  box-shadow: 0 10px 24px -8px var(--deck-a);
}

.cta {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  margin: 40px 0;
  padding: 56px 28px;
  border-radius: var(--radius-xl);
  background: var(--grad-brand);
  color: #fff;
  text-align: center;
  box-shadow: var(--shadow-glow);
  position: relative;
  overflow: hidden;
}

.cta h2 {
  max-width: 720px;
  font-size: clamp(1.4rem, 3vw, 2.1rem);
}

.cta-btn {
  --btn-bg: #fff;
  --btn-fg: #5b3df5;
}

.footer {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 28px 0 40px;
  flex-wrap: wrap;
}

.footer a {
  color: var(--text-muted);
  font-weight: 700;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .hero-art {
    height: 340px;
  }

  .features {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .landing {
    padding: 0 16px;
  }

  .features {
    grid-template-columns: 1fr;
  }

  .hide-mobile {
    display: none;
  }
}
</style>
