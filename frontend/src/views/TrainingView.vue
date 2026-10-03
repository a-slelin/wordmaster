<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Check, Flame, Sparkles, Turtle, Volume2, X } from 'lucide-vue-next'
import SessionSummary from '@/components/SessionSummary.vue'
import SpeakButton from '@/components/SpeakButton.vue'
import { trainingApi, type AnswerResult, type Grade, type NextCard, type SessionFinish } from '@/api'
import { usePrefsStore } from '@/stores/prefs'
import { useStatsStore } from '@/stores/stats'
import { useTrainingStore } from '@/stores/training'
import { useUiStore } from '@/stores/ui'
import { useErrorToast } from '@/composables/useErrorToast'
import { useSpeech } from '@/composables/useSpeech'

const props = defineProps<{ sessionId: string }>()

const { t, tm } = useI18n()
const router = useRouter()
const prefs = usePrefsStore()
const stats = useStatsStore()
const training = useTrainingStore()
const ui = useUiStore()
const showError = useErrorToast()
const { speak } = useSpeech()

const deckId = ref('')
const deckTitle = ref('')
const card = ref<NextCard | null>(null)
const result = ref<AnswerResult | null>(null)
const summary = ref<SessionFinish | null>(null)
const loading = ref(true)
const sending = ref(false)
const flipped = ref(false)
const typed = ref('')
const chosen = ref<string | null>(null)
const combo = ref(0)
const bestCombo = ref(0)
const xp = ref(0)
const floaters = ref<{ id: number; text: string }[]>([])
const leaving = ref<'left' | 'right' | null>(null)
const cardKey = ref(0)
const inputRef = ref<HTMLInputElement | null>(null)
let floaterId = 0

const mode = computed(() => card.value?.mode ?? 'flashcards')
const answered = computed(() => card.value ? card.value.position - 1 + (result.value ? 1 : 0) : 0)
const total = computed(() => card.value?.cardsTotal ?? 0)
const progressPercent = computed(() => (total.value ? Math.min(100, (answered.value / total.value) * 100) : 0))
const feedbackKind = computed(() => {
  if (!result.value) return null
  if (!result.value.correct) return 'wrong'
  return result.value.grade === 'hard' && mode.value !== 'flashcards' ? 'almost' : 'correct'
})

// ===== Session lifecycle =====
async function init() {
  const started = training.take(props.sessionId)
  try {
    if (started) {
      deckId.value = started.deckId
      deckTitle.value = started.deckTitle
      showCard(started.firstCard ?? null)
      if (!started.firstCard) await finish()
    } else {
      const next = await trainingApi.next(props.sessionId)
      if (next) showCard(next)
      else await finish()
    }
  } catch (e) {
    showError(e)
    router.replace('/home')
  } finally {
    loading.value = false
  }
}

function showCard(next: NextCard | null) {
  card.value = next
  result.value = null
  flipped.value = false
  typed.value = ''
  chosen.value = null
  leaving.value = null
  cardKey.value++
  if (!next) return
  nextTick(() => {
    inputRef.value?.focus()
    autoSpeak(next)
  })
}

function autoSpeak(next: NextCard) {
  if (next.mode === 'listening') {
    setTimeout(() => speak(next.prompt, next.promptLanguage), 250)
    return
  }
  if (prefs.autoplay && next.direction === 'forward') {
    setTimeout(() => speak(next.prompt, next.promptLanguage), 300)
  }
}

async function finish() {
  try {
    summary.value = await trainingApi.finish(props.sessionId)
    deckId.value ||= summary.value.deckId
    stats.refresh()
  } catch (e) {
    showError(e)
  }
}

async function exit() {
  if (summary.value) return router.push(deckId.value ? `/decks/${deckId.value}` : '/home')
  if (answered.value === 0) {
    await finish()
    return router.push(deckId.value ? `/decks/${deckId.value}` : '/home')
  }
  const ok = await ui.confirm({ title: t('training.exit'), text: t('training.exitConfirm'), confirmText: t('training.exit') })
  if (ok) await finish()
}

// ===== Answering =====
async function submit(payload: { grade?: Grade; answer?: string }) {
  if (!card.value || sending.value || result.value) return
  sending.value = true
  try {
    const res = await trainingApi.answer(props.sessionId, { cardId: card.value.cardId, ...payload })
    result.value = res
    onResult(res)
  } catch (e) {
    showError(e)
  } finally {
    sending.value = false
  }
}

function onResult(res: AnswerResult) {
  xp.value += res.xpEarned
  addFloater(`+${res.xpEarned} XP`)
  if (res.correct) {
    combo.value++
    bestCombo.value = Math.max(bestCombo.value, combo.value)
    if (combo.value > 0 && combo.value % 5 === 0) {
      const praise = tm('training.praise') as string[]
      ui.toast(`${praise[Math.floor(Math.random() * praise.length)]} ${t('training.combo', { n: combo.value })}`, 'xp', '🔥', 1800)
    }
  } else {
    combo.value = 0
  }

  if (mode.value !== 'flashcards' && card.value) {
    const word = card.value.direction === 'forward' || mode.value === 'listening' ? card.value.prompt : res.correctAnswer
    const lang = card.value.direction === 'forward' || mode.value === 'listening' ? card.value.promptLanguage : card.value.answerLanguage
    if (prefs.autoplay || mode.value === 'listening') setTimeout(() => speak(word, lang), 200)
  }

  if (mode.value === 'flashcards') {
    proceed()
  } else if (mode.value === 'choice' && res.correct) {
    setTimeout(() => result.value === res && proceed(), 900)
  }
}

function proceed() {
  const res = result.value
  if (!res) return
  if (mode.value === 'flashcards') {
    leaving.value = res.correct ? 'right' : 'left'
    setTimeout(() => (res.nextCard ? showCard(res.nextCard) : finish()), 260)
  } else if (res.nextCard) {
    showCard(res.nextCard)
  } else {
    finish()
  }
}

function addFloater(text: string) {
  const id = ++floaterId
  floaters.value.push({ id, text })
  setTimeout(() => (floaters.value = floaters.value.filter((f) => f.id !== id)), 1100)
}

function flip() {
  if (mode.value !== 'flashcards' || result.value) return
  flipped.value = !flipped.value
  if (flipped.value && card.value && card.value.direction === 'reverse' && prefs.autoplay && card.value.answer) {
    speak(card.value.answer, card.value.answerLanguage)
  }
}

function grade(g: Grade) {
  if (!flipped.value) {
    flipped.value = true
    return
  }
  submit({ grade: g })
}

function check() {
  submit({ answer: typed.value })
}

function choose(option: string) {
  chosen.value = option
  submit({ answer: option })
}

// ===== Swipe (flashcards) =====
const drag = ref({ active: false, startX: 0, dx: 0 })

function onPointerDown(event: PointerEvent) {
  if (mode.value !== 'flashcards' || result.value) return
  if ((event.target as HTMLElement).closest('button')) return
  drag.value = { active: true, startX: event.clientX, dx: 0 }
}

function onPointerMove(event: PointerEvent) {
  if (!drag.value.active) return
  drag.value.dx = event.clientX - drag.value.startX
}

function onPointerUp() {
  if (!drag.value.active) return
  const dx = drag.value.dx
  drag.value = { active: false, startX: 0, dx: 0 }
  if (Math.abs(dx) < 8) return flip()
  if (!flipped.value) return flip()
  if (dx > 110) grade('good')
  else if (dx < -110) grade('again')
}

const cardStyle = computed(() => {
  if (leaving.value) return {}
  const dx = drag.value.dx
  return dx ? { transform: `translateX(${dx}px) rotate(${dx / 18}deg)`, transition: 'none' } : {}
})

// ===== Keyboard =====
function onKey(event: KeyboardEvent) {
  if (summary.value || loading.value || ui.confirmRequest) return
  const target = event.target as HTMLElement
  const typing = target.tagName === 'INPUT'

  if (event.key === 'Escape') return exit()

  if (mode.value === 'flashcards') {
    if (event.key === ' ' || (event.key === 'Enter' && !flipped.value)) {
      event.preventDefault()
      flip()
    } else if (flipped.value && ['1', '2', '3', '4'].includes(event.key)) {
      grade((['again', 'hard', 'good', 'easy'] as Grade[])[+event.key - 1])
    }
    return
  }

  if (result.value && event.key === 'Enter') {
    event.preventDefault()
    proceed()
    return
  }

  if (mode.value === 'choice' && !result.value && !typing && ['1', '2', '3', '4'].includes(event.key)) {
    const option = card.value?.options?.[+event.key - 1]
    if (option) choose(option)
  }
}

watch(result, (value) => {
  if (value && mode.value !== 'flashcards') nextTick(() => (document.activeElement as HTMLElement | null)?.blur())
})

onMounted(() => {
  window.addEventListener('keydown', onKey)
  init()
})

onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

async function again() {
  if (!deckId.value) return
  try {
    summary.value = null
    loading.value = true
    combo.value = 0
    xp.value = 0
    await training.start(deckId.value)
  } catch (e) {
    showError(e)
    loading.value = false
  }
}

watch(
  () => props.sessionId,
  () => {
    loading.value = true
    init()
  },
)
</script>

<template>
  <div class="training">
    <header class="top">
      <button class="btn btn-ghost btn-icon" type="button" :aria-label="$t('training.exit')" @click="exit">
        <X :size="22" />
      </button>
      <div class="bar">
        <div class="progress"><span :style="{ width: `${summary ? 100 : progressPercent}%` }" /></div>
        <span class="tiny bold muted counter">{{ $t('training.of', { n: Math.min(answered + (summary ? 0 : 1), total), total }) }}</span>
      </div>
      <Transition name="pop">
        <span v-if="combo >= 3 && !summary" :key="combo" class="combo"><Flame :size="16" /> ×{{ combo }}</span>
      </Transition>
      <span class="xp"><Sparkles :size="16" /> {{ xp }}
        <TransitionGroup name="float">
          <span v-for="f in floaters" :key="f.id" class="floater">{{ f.text }}</span>
        </TransitionGroup>
      </span>
    </header>

    <main class="stage">
      <div v-if="loading" class="skeleton big-skeleton" />

      <SessionSummary v-else-if="summary" :result="summary" @again="again"
                      @deck="router.push(`/decks/${deckId}`)" @home="router.push('/home')" />

      <template v-else-if="card">
        <p v-if="deckTitle" class="deck-title tiny bold subtle">{{ deckTitle }}</p>

        <!-- FLASHCARDS -->
        <template v-if="mode === 'flashcards'">
          <div class="flash-wrap">
            <div :key="cardKey" class="flash" :class="[{ flipped }, leaving ? `leave-${leaving}` : '']" :style="cardStyle"
                 @pointerdown="onPointerDown" @pointermove="onPointerMove" @pointerup="onPointerUp" @pointercancel="onPointerUp">
              <div class="face front">
                <span class="status badge" :class="`st-${card.status}`">{{ $t(`status.${card.status}`) }}</span>
                <img v-if="card.imageUrl" :src="card.imageUrl" alt="" class="pic" draggable="false" />
                <span class="prompt">{{ card.prompt }}</span>
                <span v-if="card.transcription" class="ipa">{{ card.transcription }}</span>
                <SpeakButton v-if="card.direction === 'forward'" :text="card.prompt" :lang="card.promptLanguage" />
                <span class="hint tiny">{{ $t('training.flipHint') }}</span>
              </div>
              <div class="face back">
                <span class="answer">{{ card.answer }}</span>
                <SpeakButton v-if="card.direction === 'reverse' && card.answer" :text="card.answer" :lang="card.answerLanguage" />
                <span v-if="card.exampleSentence" class="example">“{{ card.exampleSentence }}”</span>
              </div>
              <span class="swipe-tag good" :style="{ opacity: Math.max(0, drag.dx / 120) }">✓</span>
              <span class="swipe-tag again" :style="{ opacity: Math.max(0, -drag.dx / 120) }">✗</span>
            </div>
          </div>

          <div class="actions">
            <button v-if="!flipped" class="btn btn-primary btn-lg wide" type="button" @click="flip">
              {{ $t('training.showAnswer') }} <span class="kbd">Space</span>
            </button>
            <div v-else class="grades">
              <button v-for="(g, i) in (['again', 'hard', 'good', 'easy'] as Grade[])" :key="g" type="button"
                      class="grade" :class="g" :disabled="sending" @click="grade(g)">
                <span class="grade-emoji">{{ ['😵', '🤔', '🙂', '😎'][i] }}</span>
                <b>{{ $t(`training.grades.${g}`) }}</b>
                <span class="kbd">{{ i + 1 }}</span>
              </button>
            </div>
            <p v-if="flipped" class="tiny subtle center swipe-hint">{{ $t('training.swipeHint') }}</p>
          </div>
        </template>

        <!-- TYPING / LISTENING / CHOICE -->
        <template v-else>
          <div :key="cardKey" class="prompt-card glass" :class="feedbackKind ?? ''">
            <template v-if="mode === 'listening'">
              <button class="listen" type="button" @click="speak(card.prompt, card.promptLanguage)">
                <Volume2 :size="44" />
              </button>
              <button class="btn btn-ghost btn-sm" type="button" @click="speak(card.prompt, card.promptLanguage, true)">
                <Turtle :size="16" /> {{ $t('training.slow') }}
              </button>
              <Transition name="pop">
                <div v-if="result" class="reveal">
                  <span class="prompt">{{ card.prompt }}</span>
                  <span v-if="card.transcription" class="ipa">{{ card.transcription }}</span>
                </div>
              </Transition>
            </template>
            <template v-else>
              <span class="status badge" :class="`st-${card.status}`">{{ $t(`status.${card.status}`) }}</span>
              <img v-if="card.imageUrl" :src="card.imageUrl" alt="" class="pic" />
              <div class="row prompt-row">
                <span class="prompt">{{ card.prompt }}</span>
                <SpeakButton v-if="card.direction === 'forward'" :text="card.prompt" :lang="card.promptLanguage" size="sm" />
              </div>
              <span v-if="card.transcription" class="ipa">{{ card.transcription }}</span>
            </template>
          </div>

          <!-- choice -->
          <div v-if="mode === 'choice'" class="options">
            <button v-for="(option, i) in card.options" :key="option" type="button" class="option"
                    :class="{
                      correct: result && option === result.correctAnswer,
                      wrong: result && chosen === option && !result.correct,
                      dim: result && option !== result.correctAnswer && chosen !== option,
                    }"
                    :disabled="!!result || sending" @click="choose(option)">
              <span class="kbd">{{ i + 1 }}</span>
              <span class="grow">{{ option }}</span>
              <Check v-if="result && option === result.correctAnswer" :size="20" />
              <X v-else-if="result && chosen === option" :size="20" />
            </button>
          </div>

          <!-- typing / listening -->
          <form v-else class="type-form" @submit.prevent="result ? proceed() : check()">
            <input ref="inputRef" v-model="typed" class="input answer-input" :class="feedbackKind ?? ''"
                   :placeholder="mode === 'listening' ? $t('training.typeWord') : $t('training.typeAnswer')"
                   :readonly="!!result" autocomplete="off" autocapitalize="off" spellcheck="false" maxlength="255" />
            <div v-if="!result" class="row type-actions">
              <button class="btn btn-ghost" type="button" :disabled="sending" @click="submit({ answer: '' })">{{ $t('training.dontKnow') }}</button>
              <button class="btn btn-primary grow" type="submit" :disabled="sending || !typed.trim()">{{ $t('training.check') }}</button>
            </div>
          </form>

          <Transition name="pop">
            <div v-if="result" class="feedback" :class="feedbackKind ?? ''">
              <div class="fb-head">
                <span class="fb-icon">{{ feedbackKind === 'correct' ? '🎉' : feedbackKind === 'almost' ? '👌' : '💡' }}</span>
                <b>{{ feedbackKind === 'correct' ? $t('training.correct') : feedbackKind === 'almost' ? $t('training.almost') : $t('training.wrong') }}</b>
              </div>
              <div v-if="feedbackKind !== 'correct'" class="fb-answer">
                <span class="tiny muted">{{ $t('training.correctAnswer') }}</span>
                <b class="fb-text">{{ result.correctAnswer }}</b>
              </div>
              <span v-if="card.exampleSentence" class="example small">“{{ card.exampleSentence }}”</span>
              <button class="btn btn-lg wide" :class="feedbackKind === 'wrong' ? 'btn-danger' : 'btn-success'" type="button" @click="proceed">
                {{ $t('training.continue') }} <span class="kbd light">Enter</span>
              </button>
            </div>
          </Transition>
        </template>
      </template>
    </main>
  </div>
</template>

<style scoped>
.training {
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
}

.top {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  padding-top: max(14px, env(safe-area-inset-top));
  max-width: 980px;
  width: 100%;
  margin: 0 auto;
}

.bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar .progress {
  flex: 1;
  height: 14px;
}

.bar .progress > span {
  box-shadow: 0 0 18px rgba(124, 92, 255, 0.6);
}

.counter {
  white-space: nowrap;
}

.combo {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  background: var(--grad-fire);
  color: #fff;
  font-weight: 800;
  font-size: 0.85rem;
  box-shadow: 0 6px 18px -6px rgba(255, 90, 60, 0.7);
}

.xp {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 800;
  color: var(--warning);
}

.floater {
  position: absolute;
  right: 0;
  top: 100%;
  white-space: nowrap;
  font-weight: 900;
  color: var(--warning);
  animation: rise-fade 1.1s ease-out forwards;
  pointer-events: none;
}

.stage {
  flex: 1;
  width: 100%;
  max-width: 720px;
  margin: 0 auto;
  padding: 12px 20px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22px;
}

.deck-title {
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-top: 6px;
}

.big-skeleton {
  width: 100%;
  height: 360px;
  margin-top: 40px;
  border-radius: var(--radius-xl);
}

/* ===== Flashcard ===== */
.flash-wrap {
  width: 100%;
  perspective: 1600px;
}

.flash {
  position: relative;
  width: 100%;
  height: min(52dvh, 420px);
  min-height: 300px;
  transform-style: preserve-3d;
  transition: transform 0.6s var(--ease-spring);
  cursor: pointer;
  touch-action: pan-y;
  user-select: none;
  animation: card-in 0.5s var(--ease-spring);
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(30px) scale(0.92); }
  to { opacity: 1; transform: none; }
}

.flash.flipped {
  transform: rotateY(180deg);
}

.flash.leave-right {
  transform: translateX(120%) rotate(18deg) rotateY(180deg);
  opacity: 0;
  transition: transform 0.3s ease-in, opacity 0.3s ease-in;
}

.flash.leave-left {
  transform: translateX(-120%) rotate(-18deg) rotateY(180deg);
  opacity: 0;
  transition: transform 0.3s ease-in, opacity 0.3s ease-in;
}

.face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 32px 24px;
  border-radius: var(--radius-xl);
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
  text-align: center;
  box-shadow: var(--shadow-lg);
}

.front {
  background: var(--surface-strong);
  border: 1px solid var(--border);
}

.back {
  transform: rotateY(180deg);
  background: var(--grad-brand);
  color: #fff;
}

.back :deep(.speak) {
  background: rgba(255, 255, 255, 0.22);
  color: #fff;
}

.status {
  position: absolute;
  top: 18px;
  left: 18px;
}

.st-new { background: var(--info-soft); color: var(--info); }
.st-learning { background: var(--warning-soft); color: var(--warning); }
.st-known { background: var(--success-soft); color: var(--success); }

.pic {
  max-height: 120px;
  border-radius: var(--radius-md);
  object-fit: cover;
}

.prompt {
  font-family: var(--font-display);
  font-size: clamp(1.8rem, 5vw, 2.8rem);
  font-weight: 700;
  line-height: 1.15;
  word-break: break-word;
}

.answer {
  font-family: var(--font-display);
  font-size: clamp(1.6rem, 4.5vw, 2.4rem);
  font-weight: 600;
}

.ipa {
  color: var(--text-muted);
  font-size: 1.1rem;
  font-weight: 600;
}

.example {
  font-style: italic;
  opacity: 0.85;
  max-width: 520px;
}

.hint {
  position: absolute;
  bottom: 16px;
  color: var(--text-subtle);
  font-weight: 700;
}

.swipe-tag {
  position: absolute;
  top: 22px;
  font-size: 2rem;
  font-weight: 900;
  padding: 2px 16px;
  border-radius: 12px;
  border: 4px solid;
  transform: rotateY(180deg);
  pointer-events: none;
  backface-visibility: hidden;
}

.swipe-tag.good { right: 22px; color: var(--success); border-color: var(--success); }
.swipe-tag.again { left: 22px; color: var(--danger); border-color: var(--danger); }

.actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.wide {
  width: min(420px, 100%);
}

.grades {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  animation: stagger-in 0.35s var(--ease-out);
}

.grade {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 6px;
  border-radius: var(--radius-md);
  border: 2px solid transparent;
  background: var(--surface-strong);
  box-shadow: var(--shadow-sm);
  transition: transform 0.25s var(--ease-spring), box-shadow 0.2s ease;
}

.grade:hover:not(:disabled) {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.grade-emoji {
  font-size: 1.6rem;
}

.grade.again { border-color: var(--danger-soft); color: var(--danger); }
.grade.hard { border-color: var(--warning-soft); color: var(--warning); }
.grade.good { border-color: var(--success-soft); color: var(--success); }
.grade.easy { border-color: var(--info-soft); color: var(--info); }

.swipe-hint {
  display: none;
}

/* ===== Prompt card (typing / choice / listening) ===== */
.prompt-card {
  position: relative;
  width: 100%;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 36px 24px;
  text-align: center;
  border-radius: var(--radius-xl);
  animation: card-in 0.45s var(--ease-spring);
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.prompt-card.correct { border-color: var(--success); box-shadow: 0 0 0 4px var(--success-soft), var(--shadow-md); }
.prompt-card.almost { border-color: var(--warning); box-shadow: 0 0 0 4px var(--warning-soft), var(--shadow-md); }
.prompt-card.wrong { border-color: var(--danger); animation: shake 0.5s; }

.prompt-row {
  justify-content: center;
}

.listen {
  display: grid;
  place-items: center;
  width: 120px;
  height: 120px;
  border: none;
  border-radius: 50%;
  background: var(--grad-brand);
  color: #fff;
  box-shadow: var(--shadow-glow);
  animation: pulse-glow 2s ease-out infinite;
  transition: transform 0.3s var(--ease-spring);
}

.listen:hover {
  transform: scale(1.06);
}

.reveal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.options {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.option {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 64px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  border: 2px solid var(--border);
  background: var(--surface-strong);
  text-align: left;
  font-weight: 700;
  font-size: 1.02rem;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s var(--ease-spring), border-color 0.2s ease, background 0.2s ease, opacity 0.2s ease;
  animation: stagger-in 0.4s var(--ease-out) both;
}

.option:nth-child(2) { animation-delay: 0.05s; }
.option:nth-child(3) { animation-delay: 0.1s; }
.option:nth-child(4) { animation-delay: 0.15s; }

.option:hover:not(:disabled) {
  transform: translateY(-3px);
  border-color: var(--primary);
}

.option:disabled {
  cursor: default;
}

.option.correct {
  border-color: var(--success);
  background: var(--success-soft);
  color: var(--success);
  transform: scale(1.02);
}

.option.wrong {
  border-color: var(--danger);
  background: var(--danger-soft);
  color: var(--danger);
  animation: shake 0.45s;
}

.option.dim {
  opacity: 0.45;
}

.type-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.answer-input {
  height: 64px;
  font-size: 1.3rem;
  font-weight: 700;
  text-align: center;
  border-radius: var(--radius-md);
}

.answer-input.correct { border-color: var(--success); background: var(--success-soft); }
.answer-input.almost { border-color: var(--warning); background: var(--warning-soft); }
.answer-input.wrong { border-color: var(--danger); background: var(--danger-soft); text-decoration: line-through; }

.type-actions .btn {
  height: 52px;
}

.feedback {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  border-radius: var(--radius-lg);
  text-align: center;
}

.feedback.correct { background: var(--success-soft); }
.feedback.almost { background: var(--warning-soft); }
.feedback.wrong { background: var(--danger-soft); }

.fb-head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.2rem;
}

.fb-icon {
  font-size: 1.6rem;
}

.fb-answer {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.fb-text {
  font-family: var(--font-display);
  font-size: 1.4rem;
}

.kbd.light {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
  color: #fff;
}

.float-enter-active,
.float-leave-active {
  transition: none;
}

@media (hover: none) {
  .hint,
  .kbd {
    display: none;
  }
}

@media (max-width: 640px) {
  .top {
    padding: 10px 12px;
    gap: 8px;
  }

  .counter {
    display: none;
  }

  .stage {
    padding: 8px 14px 28px;
  }

  .grades {
    grid-template-columns: repeat(4, 1fr);
    gap: 6px;
  }

  .grade {
    padding: 10px 2px;
    font-size: 0.8rem;
  }

  .grade .kbd {
    display: none;
  }

  .swipe-hint {
    display: block;
  }

  .options {
    grid-template-columns: 1fr;
  }
}
</style>
