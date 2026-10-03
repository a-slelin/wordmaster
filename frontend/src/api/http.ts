import type { ApiErrorBody, AuthResponse } from './types'

const BASE = '/api'
const ACCESS_KEY = 'wm-access'
const REFRESH_KEY = 'wm-refresh'

export class ApiError extends Error {
  readonly status: number
  readonly body?: ApiErrorBody

  constructor(status: number, message: string, body?: ApiErrorBody) {
    super(message)
    this.status = status
    this.body = body
  }

  /** Field validation errors as { field: message }. */
  get fieldErrors(): Record<string, string> {
    const errors = (this.body?.details?.errors ?? []) as Array<{ field?: string; message?: string }>
    const result: Record<string, string> = {}
    for (const e of errors) {
      if (e.field && e.message && !result[e.field]) result[e.field] = e.message
    }
    return result
  }
}

export const tokens = {
  get access(): string | null {
    return localStorage.getItem(ACCESS_KEY)
  },
  get refresh(): string | null {
    return localStorage.getItem(REFRESH_KEY)
  },
  save(auth: Pick<AuthResponse, 'accessToken' | 'refreshToken'>) {
    localStorage.setItem(ACCESS_KEY, auth.accessToken)
    localStorage.setItem(REFRESH_KEY, auth.refreshToken)
  },
  clear() {
    localStorage.removeItem(ACCESS_KEY)
    localStorage.removeItem(REFRESH_KEY)
  },
}

type Listener = () => void
const unauthorizedListeners: Listener[] = []

/** Called when the session can not be restored (refresh token expired). */
export function onUnauthorized(listener: Listener) {
  unauthorizedListeners.push(listener)
}

let refreshing: Promise<boolean> | null = null

async function refreshTokens(): Promise<boolean> {
  const refreshToken = tokens.refresh
  if (!refreshToken) return false

  // Single-flight: parallel requests wait for the same refresh.
  refreshing ??= fetch(`${BASE}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
    .then(async (response) => {
      if (!response.ok) return false
      tokens.save((await response.json()) as AuthResponse)
      return true
    })
    .catch(() => false)
    .finally(() => {
      setTimeout(() => (refreshing = null), 0)
    })

  return refreshing
}

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: Record<string, string | number | boolean | undefined | null>
  auth?: boolean
}

function buildUrl(path: string, query?: RequestOptions['query']): string {
  const url = new URL(BASE + path, window.location.origin)
  if (query) {
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== '') url.searchParams.set(key, String(value))
    }
  }
  return url.pathname + url.search
}

export async function request<T>(path: string, options: RequestOptions = {}, retried = false): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'
  const access = tokens.access
  if (access && options.auth !== false) headers.Authorization = `Bearer ${access}`

  let response: Response
  try {
    response = await fetch(buildUrl(path, options.query), {
      method: options.method ?? 'GET',
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'network')
  }

  if (response.status === 401 && options.auth !== false && !retried && tokens.refresh) {
    if (await refreshTokens()) return request<T>(path, options, true)
    tokens.clear()
    unauthorizedListeners.forEach((listener) => listener())
  }

  if (response.status === 204) return undefined as T

  const text = await response.text()
  const data = text ? JSON.parse(text) : undefined

  if (!response.ok) {
    const body = data as ApiErrorBody | undefined
    throw new ApiError(response.status, body?.message ?? response.statusText, body)
  }

  return data as T
}

export const http = {
  get: <T>(path: string, query?: RequestOptions['query']) => request<T>(path, { query }),
  post: <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body }),
  put: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body }),
  patch: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PATCH', body }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}
