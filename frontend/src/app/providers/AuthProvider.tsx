import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { AuthContext, type AuthContextValue } from '@/features/auth/context/authContext'
import { authApi } from '@/features/auth/services/authApi'
import { sessionStore } from '@/features/auth/services/sessionStore'
import type { AuthStatus, LoginRequest, UserResponse } from '@/features/auth/types/auth.types'
import { configureApiAuth } from '@/shared/services/apiClient'

const SESSION_EXPIRED_EVENT = 'ebenezer:session-expired'

// Configured at module load so the very first request already carries the token.
configureApiAuth({
  getToken: sessionStore.getToken,
  onUnauthorized: () => {
    sessionStore.clear()
    window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT))
  },
})

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [status, setStatus] = useState<AuthStatus>(() => (sessionStore.load() ? 'loading' : 'anonymous'))

  // Restore the stored session by asking the backend who the token belongs to.
  useEffect(() => {
    if (status !== 'loading') return
    let cancelled = false

    authApi
      .me()
      .then((currentUser) => {
        if (cancelled) return
        setUser(currentUser)
        setStatus('authenticated')
      })
      .catch(() => {
        if (cancelled) return
        sessionStore.clear()
        setStatus('anonymous')
      })

    return () => {
      cancelled = true
    }
  }, [status])

  // Any 401 on an authenticated request (expired token, deactivated user) ends the session.
  useEffect(() => {
    const handleExpired = () => {
      setUser(null)
      setStatus('anonymous')
    }
    window.addEventListener(SESSION_EXPIRED_EVENT, handleExpired)
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleExpired)
  }, [])

  const login = useCallback(async (request: LoginRequest, remember: boolean) => {
    const response = await authApi.login(request)
    sessionStore.save({ token: response.token, expiresAt: Date.now() + response.expiresIn * 1000 }, remember)
    setUser(response.user)
    setStatus('authenticated')
  }, [])

  const logout = useCallback(() => {
    sessionStore.clear()
    setUser(null)
    setStatus('anonymous')
  }, [])

  const value = useMemo<AuthContextValue>(() => ({ status, user, login, logout }), [status, user, login, logout])

  return <AuthContext value={value}>{children}</AuthContext>
}
