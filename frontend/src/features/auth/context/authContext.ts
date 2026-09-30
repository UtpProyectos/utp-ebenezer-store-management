import { createContext } from 'react'
import type { AuthStatus, LoginRequest, UserResponse } from '../types/auth.types'

export interface AuthContextValue {
  status: AuthStatus
  user: UserResponse | null
  login: (request: LoginRequest, remember: boolean) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
