import { apiClient } from '@/shared/services/apiClient'
import type { SaleRequest, SaleResponse } from '../types/sale.types'

export const saleApi = {
  create: (request: SaleRequest) => apiClient.post<SaleResponse>('/sales', request),
}
