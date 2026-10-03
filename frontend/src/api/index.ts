import { http, request } from './http'
import type {
  Achievement, ActivityDay, AnswerResult, AuthResponse, Card, CardPayload, CardWithProgress, CatalogDeck, Deck,
  DeckCopyResult, DeckLikeResult, DeckPayload, DeckProgress, DeckSummary, ForecastDay, Grade, HardWord, Language,
  NextCard, SessionFinish, SessionStart, Sheet, Tag, TrainingDirection, TrainingMode, TrainingScope, User,
  UserStats, Uuid, WordOfTheDay,
} from './types'

export const authApi = {
  register: (body: { username: string; email: string; password: string }) =>
    request<AuthResponse>('/auth/register', { method: 'POST', body, auth: false }),
  login: (body: { usernameOrEmail: string; password: string }) =>
    request<AuthResponse>('/auth/login', { method: 'POST', body, auth: false }),
  logout: (refreshToken: string) =>
    request<void>('/auth/logout', { method: 'POST', body: { refreshToken }, auth: false }),
}

export const userApi = {
  me: () => http.get<User>('/users/me'),
  update: (body: { username?: string; email?: string }) => http.patch<User>('/users/me', body),
  changePassword: (body: { oldPassword: string; newPassword: string }) => http.put<void>('/users/me/password', body),
  updateSettings: (body: { dailyGoal: number }) => http.put<UserStats>('/users/me/settings', body),
  deleteMe: () => http.delete<void>('/users/me'),
}

export const dictionaryApi = {
  languages: () => http.get<Language[]>('/languages'),
  tags: () => http.get<Tag[]>('/tags'),
}

export interface CatalogQuery {
  search?: string
  languageId?: number
  tagId?: number
  official?: boolean
  sort?: 'popular' | 'likes' | 'new' | 'title'
  page?: number
  size?: number
}

export const deckApi = {
  mine: (search?: string) => http.get<Sheet<DeckSummary>>('/decks', { search, size: 100 }),
  catalog: (query: CatalogQuery) => http.get<Sheet<CatalogDeck>>('/decks/public', { ...query }),
  preview: (id: Uuid, limit = 12) => http.get<Card[]>(`/decks/public/${id}/cards`, { limit }),
  get: (id: Uuid) => http.get<Deck>(`/decks/${id}`),
  create: (body: DeckPayload) => http.post<Deck>('/decks', body),
  update: (id: Uuid, body: DeckPayload) => http.patch<Deck>(`/decks/${id}`, body),
  remove: (id: Uuid) => http.delete<void>(`/decks/${id}`),
  copy: (id: Uuid) => http.post<DeckCopyResult>(`/decks/${id}/copy`),
  like: (id: Uuid) => http.post<DeckLikeResult>(`/decks/${id}/like`),
  unlike: (id: Uuid) => http.delete<DeckLikeResult>(`/decks/${id}/like`),
  progress: (id: Uuid) => http.get<DeckProgress>(`/decks/${id}/progress`),
  cards: (id: Uuid) => http.get<CardWithProgress[]>(`/decks/${id}/cards`),
  addCard: (id: Uuid, body: CardPayload) => http.post<Card>(`/decks/${id}/cards`, body),
  addCards: (id: Uuid, cards: CardPayload[]) => http.post<Card[]>(`/decks/${id}/cards/bulk`, { cards }),
}

export const cardApi = {
  update: (id: Uuid, body: CardPayload) => http.patch<Card>(`/cards/${id}`, body),
  remove: (id: Uuid) => http.delete<void>(`/cards/${id}`),
}

export const trainingApi = {
  start: (body: { deckId: Uuid; mode: TrainingMode; direction: TrainingDirection; scope: TrainingScope; limit: number }) =>
    http.post<SessionStart>('/training/sessions', body),
  next: (sessionId: Uuid) => http.get<NextCard | undefined>(`/training/sessions/${sessionId}/next`),
  answer: (sessionId: Uuid, body: { cardId: Uuid; grade?: Grade; answer?: string }) =>
    http.post<AnswerResult>(`/training/sessions/${sessionId}/answers`, body),
  finish: (sessionId: Uuid) => http.post<SessionFinish>(`/training/sessions/${sessionId}/finish`),
}

export const statsApi = {
  overview: () => http.get<UserStats>('/stats/overview'),
  activity: (days = 182) => http.get<ActivityDay[]>('/stats/activity', { days }),
  forecast: (days = 14) => http.get<ForecastDay[]>('/stats/forecast', { days }),
  hardWords: (deckId?: Uuid, limit = 10) => http.get<HardWord[]>('/stats/hard-words', { deckId, limit }),
  wordOfTheDay: () => http.get<WordOfTheDay | undefined>('/stats/word-of-the-day'),
  achievements: () => http.get<Achievement[]>('/stats/achievements'),
}

export const adminApi = {
  users: (page = 0, size = 20) => http.get<Sheet<User>>('/admin/users', { page, size }),
  setRole: (id: Uuid, role: 'user' | 'admin') => http.patch<User>(`/admin/users/${id}/role`, { role }),
  deleteUser: (id: Uuid) => http.delete<void>(`/admin/users/${id}`),
  createLanguage: (body: { code: string; name: string; flag?: string }) => http.post<Language>('/admin/languages', body),
  deleteLanguage: (id: number) => http.delete<void>(`/admin/languages/${id}`),
  createTag: (name: string) => http.post<Tag>('/admin/tags', { name }),
  deleteTag: (id: number) => http.delete<void>(`/admin/tags/${id}`),
}

export { ApiError, tokens, onUnauthorized } from './http'
export type * from './types'
