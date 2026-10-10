import { apiClient } from '@/shared/services/apiClient'
import type { UserResponse } from '@/features/auth/types/auth.types'
import type { RoleOption, UserInput, UserUpdateInput } from '../types/user.types'

export const userApi = {
  getAll: () => apiClient.get<UserResponse[]>('/users'),
  getRoles: () => apiClient.get<RoleOption[]>('/roles'),
  create: (user: UserInput & { password: string }) => apiClient.post<UserResponse>('/users', user),
  update: (id: number, user: UserUpdateInput) => apiClient.put<UserResponse>(`/users/${id}`, user),
  updateStatus: (id: number, active: boolean) =>
    apiClient.patch<UserResponse>(`/users/${id}/status`, { active }),
  resetPassword: (id: number, newPassword: string) =>
    apiClient.put<void>(`/users/${id}/password`, { newPassword }),
}
