import type { RoleName, UserResponse } from '@/features/auth/types/auth.types'

export interface UserInput {
  name: string
  username: string
  email: string | null
  role: RoleName
  password?: string
}

export interface UserUpdateInput {
  name: string
  email: string | null
  role: RoleName
}

export interface RoleOption {
  id: number
  name: RoleName
  description: string | null
}

export type ManagedUser = UserResponse
