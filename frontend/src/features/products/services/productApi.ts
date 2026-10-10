import { apiClient } from '@/shared/services/apiClient'
import type { Product, ProductInput } from '../types/product.types'

export interface ProductFilters {
  search?: string
  categoryId?: number
  active?: boolean
}

function buildQuery(filters: ProductFilters) {
  const query = new URLSearchParams()
  if (filters.search) query.set('search', filters.search)
  if (filters.categoryId !== undefined) query.set('categoryId', String(filters.categoryId))
  if (filters.active !== undefined) query.set('active', String(filters.active))
  const queryString = query.toString()
  return queryString ? `?${queryString}` : ''
}

export const productApi = {
  getAll: (filters: ProductFilters = {}) => apiClient.get<Product[]>(`/products${buildQuery(filters)}`),
  getById: (id: number) => apiClient.get<Product>(`/products/${id}`),
  create: (product: ProductInput) => apiClient.post<Product>('/products', product),
  update: (id: number, product: ProductInput) => apiClient.put<Product>(`/products/${id}`, product),
  updateStatus: (id: number, active: boolean) =>
    apiClient.patch<Product>(`/products/${id}/status`, { active }),
}
