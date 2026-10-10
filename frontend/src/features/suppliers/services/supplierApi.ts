import { apiClient } from '@/shared/services/apiClient'
import type { Supplier, SupplierInput } from '../types/supplier.types'

export interface SupplierFilters {
  search?: string
  active?: boolean
}

function buildQuery(filters: SupplierFilters) {
  const query = new URLSearchParams()
  if (filters.search) query.set('search', filters.search)
  if (filters.active !== undefined) query.set('active', String(filters.active))
  const queryString = query.toString()
  return queryString ? `?${queryString}` : ''
}

export const supplierApi = {
  getAll: (filters: SupplierFilters = {}) => apiClient.get<Supplier[]>(`/suppliers${buildQuery(filters)}`),
  create: (supplier: SupplierInput) => apiClient.post<Supplier>('/suppliers', supplier),
  update: (id: number, supplier: SupplierInput) =>
    apiClient.put<Supplier>(`/suppliers/${id}`, supplier),
  updateStatus: (id: number, active: boolean) =>
    apiClient.patch<Supplier>(`/suppliers/${id}/status`, { active }),
}
