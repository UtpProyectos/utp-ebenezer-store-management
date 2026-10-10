import { apiClient } from '@/shared/services/apiClient'
import type { CategoryOption, UnitOption } from '../types/product.types'

export const productOptionsApi = {
  getCategories: () => apiClient.get<CategoryOption[]>('/categories?active=true'),
  getUnits: () => apiClient.get<UnitOption[]>('/units'),
}
