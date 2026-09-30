import { apiClient } from '@/shared/services/apiClient'
import type { ChangePasswordRequest, LoginRequest, LoginResponse, UserResponse } from '../types/auth.types'

export const authApi = {
  login: (request: LoginRequest) => apiClient.post<LoginResponse>('/auth/login', request, { skipAuth: true }),
  me: () => apiClient.get<UserResponse>('/auth/me'),
  changePassword: (request: ChangePasswordRequest) => apiClient.put<void>('/auth/password', request),
}
