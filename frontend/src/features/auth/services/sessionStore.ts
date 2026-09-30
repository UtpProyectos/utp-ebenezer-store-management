import type { StoredSession } from '../types/auth.types'

const STORAGE_KEY = 'ebenezer.session'

// In-memory copy so the token is available synchronously (and even if browser storage is blocked).
let current: StoredSession | null = null

function readFrom(storage: Storage): StoredSession | null {
  try {
    const raw = storage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as StoredSession) : null
  } catch {
    return null
  }
}

function isValid(session: StoredSession | null): session is StoredSession {
  return session !== null && session.expiresAt > Date.now()
}

export const sessionStore = {
  /** Current valid session, restored from storage on first access. */
  load(): StoredSession | null {
    if (!current) {
      // "Remember me" sessions live in localStorage; the rest only while the tab is open.
      current = readFrom(localStorage) ?? readFrom(sessionStorage)
    }
    if (!isValid(current)) {
      sessionStore.clear()
      return null
    }
    return current
  },

  getToken(): string | null {
    return sessionStore.load()?.token ?? null
  },

  save(session: StoredSession, remember: boolean) {
    sessionStore.clear()
    current = session
    try {
      ;(remember ? localStorage : sessionStorage).setItem(STORAGE_KEY, JSON.stringify(session))
    } catch {
      // Storage unavailable (private mode): the in-memory session still works until reload.
    }
  },

  clear() {
    current = null
    try {
      localStorage.removeItem(STORAGE_KEY)
      sessionStorage.removeItem(STORAGE_KEY)
    } catch {
      // Ignore storage errors.
    }
  },
}
