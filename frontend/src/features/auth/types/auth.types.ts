// Mirrors backend DTOs: pe.edu.utp.ebenezer.api.dto.auth / api.dto.user

export type RoleName = 'ADMIN' | 'CASHIER'

export interface UserResponse {
  id: number
  name: string
  username: string
  email: string | null
  role: RoleName
  active: boolean
  lastLoginAt: string | null
  createdAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  /** Seconds until the token expires. */
  expiresIn: number
  user: UserResponse
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface StoredSession {
  token: string
  /** Epoch milliseconds. */
  expiresAt: number
}

export type AuthStatus = 'loading' | 'authenticated' | 'anonymous'
