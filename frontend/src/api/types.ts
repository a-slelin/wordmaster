// Types of the WordMaster REST API (mirror backend DTOs).

export type Uuid = string

export interface Page {
  number: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface Sheet<T> {
  content: T[]
  page: Page
}

export interface User {
  id: Uuid
  username: string
  email: string
  role: 'user' | 'admin'
  createdAt: string
}

export interface UserPublic {
  id: Uuid
  username: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface Language {
  id: number
  code: string
  name: string
  flag?: string
}

export interface Tag {
  id: number
  name: string
}

export type DeckColor =
  | 'violet' | 'blue' | 'sky' | 'teal' | 'green' | 'amber' | 'orange' | 'pink' | 'red' | 'slate'

export interface DeckProgress {
  deckId: Uuid
  totalCards: number
  newCards: number
  learningCards: number
  knownCards: number
  dueCards: number
  percentLearned: number
}

export interface Deck {
  id: Uuid
  title: string
  description?: string
  icon?: string
  color?: DeckColor
  owner: UserPublic
  sourceLanguage: Language
  targetLanguage: Language
  isPublic: boolean
  isOfficial: boolean
  owned: boolean
  likedByMe: boolean
  likesCount: number
  copiesCount: number
  cardsCount: number
  tags: Tag[]
  sourceDeckId?: Uuid
  progress?: DeckProgress
  createdAt: string
  updatedAt: string
}

export interface DeckSummary {
  id: Uuid
  title: string
  description?: string
  icon?: string
  color?: DeckColor
  sourceLanguage: Language
  targetLanguage: Language
  isPublic: boolean
  isOfficial: boolean
  cardsCount: number
  dueCount: number
  knownCount: number
  percentLearned: number
  tags: Tag[]
  sourceDeckId?: Uuid
  updatedAt: string
}

export interface CatalogDeck {
  id: Uuid
  title: string
  description?: string
  icon?: string
  color?: DeckColor
  ownerUsername: string
  sourceLanguage: Language
  targetLanguage: Language
  isOfficial: boolean
  likedByMe: boolean
  owned: boolean
  cardsCount: number
  likesCount: number
  copiesCount: number
  tags: Tag[]
  createdAt: string
}

export interface DeckPayload {
  title?: string
  description?: string | null
  icon?: string
  color?: DeckColor
  sourceLanguageId?: number
  targetLanguageId?: number
  isPublic?: boolean
  tagIds?: number[]
}

export interface DeckCopyResult {
  newDeckId: Uuid
  sourceDeckId: Uuid
  cardsCopiedCount: number
}

export interface DeckLikeResult {
  deckId: Uuid
  liked: boolean
  likesCount: number
}

export type CardStatus = 'new' | 'learning' | 'known'

export interface Card {
  id: Uuid
  deckId: Uuid
  word: string
  translation: string
  transcription?: string
  exampleSentence?: string
  imageUrl?: string
  audioUrl?: string
  position: number
  createdAt: string
  updatedAt: string
}

export interface CardWithProgress extends Card {
  status: CardStatus
  easeFactor: number
  intervalDays: number
  repetitions: number
  correctCount: number
  incorrectCount: number
  lastReviewedAt?: string
  nextReviewAt?: string
}

export interface CardPayload {
  word?: string
  translation?: string
  transcription?: string | null
  exampleSentence?: string | null
  imageUrl?: string | null
  audioUrl?: string | null
  position?: number
}

export type TrainingMode = 'flashcards' | 'typing' | 'choice' | 'listening'
export type TrainingDirection = 'forward' | 'reverse'
export type TrainingScope = 'smart' | 'all' | 'hard'
export type Grade = 'again' | 'hard' | 'good' | 'easy'

export interface NextCard {
  sessionId: Uuid
  cardId: Uuid
  mode: TrainingMode
  direction: TrainingDirection
  prompt: string
  promptLanguage: string
  answerLanguage: string
  transcription?: string
  exampleSentence?: string
  imageUrl?: string
  audioUrl?: string
  answer?: string
  options?: string[]
  status: CardStatus
  position: number
  cardsRemaining: number
  cardsTotal: number
}

export interface SessionStart {
  sessionId: Uuid
  deckId: Uuid
  deckTitle: string
  mode: TrainingMode
  direction: TrainingDirection
  totalCards: number
  startedAt: string
  firstCard?: NextCard
}

export interface CardProgress {
  cardId: Uuid
  status: CardStatus
  easeFactor: number
  intervalDays: number
  repetitions: number
  correctCount: number
  incorrectCount: number
  lastReviewedAt?: string
  nextReviewAt?: string
}

export interface AnswerResult {
  cardId: Uuid
  correct: boolean
  grade: Grade
  correctAnswer: string
  userAnswer?: string
  updatedProgress: CardProgress
  xpEarned: number
  finished: boolean
  nextCard?: NextCard
}

export interface Achievement {
  code: string
  icon: string
  progress: number
  goal: number
  unlocked: boolean
  unlockedAt?: string
}

export interface SessionFinish {
  sessionId: Uuid
  deckId: Uuid
  cardsTotal: number
  cardsCorrect: number
  answersTotal: number
  accuracyPercent: number
  durationSeconds: number
  xpEarned: number
  totalXp: number
  level: number
  currentStreak: number
  streakExtended: boolean
  dailyGoal: number
  todayReviewed: number
  newAchievements: Achievement[]
}

export interface UserStats {
  totalDecks: number
  totalCards: number
  knownWords: number
  learningWords: number
  totalSessions: number
  totalAnswers: number
  accuracyPercent: number
  dueToday: number
  xp: number
  level: number
  levelXp: number
  levelXpGoal: number
  currentStreak: number
  longestStreak: number
  activeToday: boolean
  dailyGoal: number
  todayReviewed: number
  achievementsUnlocked: number
  achievementsTotal: number
}

export interface ActivityDay {
  date: string
  cardsReviewed: number
  xpEarned: number
}

export interface ForecastDay {
  date: string
  dueCount: number
}

export interface HardWord {
  cardId: Uuid
  word: string
  translation: string
  deckId: Uuid
  deckTitle: string
  correctCount: number
  incorrectCount: number
  errorRate: number
}

export interface WordOfTheDay {
  cardId: Uuid
  word: string
  translation: string
  transcription?: string
  exampleSentence?: string
  languageCode: string
  deckId: Uuid
  deckTitle: string
}

export interface ApiErrorBody {
  path?: string
  status?: string
  message?: string
  debugMessage?: string
  exception?: string
  details?: Record<string, unknown>
}
