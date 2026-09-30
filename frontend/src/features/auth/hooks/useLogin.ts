import { useState } from 'react'
import { ApiClientError } from '@/shared/services/apiClient'
import type { LoginRequest } from '../types/auth.types'
import { useAuth } from './useAuth'

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    if (error.status === 401) return 'Usuario o contraseña incorrectos.'
    if (error.status === 400) return 'Completa tu usuario y contraseña.'
    return 'No se pudo iniciar sesión. Intenta de nuevo.'
  }
  return 'No hay conexión con el servidor.'
}

export function useLogin() {
  const { login } = useAuth()
  const [isPending, setIsPending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  // Incremented on every failure so the error alert replays its shake animation.
  const [errorKey, setErrorKey] = useState(0)

  const submit = async (request: LoginRequest, remember: boolean) => {
    setIsPending(true)
    setError(null)
    try {
      await login({ username: request.username.trim(), password: request.password }, remember)
      // Navigation happens in GuestRoute once the session is authenticated.
    } catch (caught) {
      setError(toErrorMessage(caught))
      setErrorKey((key) => key + 1)
      setIsPending(false)
    }
  }

  return { submit, isPending, error, errorKey }
}
