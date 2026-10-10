import { apiClient } from '@/shared/services/apiClient'
import type { Category, CategoryInput } from '../types/category.types'

export const categoryApi = {
  getAll: () => apiClient.get<Category[]>('/categories'),
  create: (category: CategoryInput) => apiClient.post<Category>('/categories', category),
  update: (id: number, category: CategoryInput) => apiClient.put<Category>(`/categories/${id}`, category),
  updateStatus: (id: number, active: boolean) =>
    apiClient.patch<Category>(`/categories/${id}/status`, { active }),
}
