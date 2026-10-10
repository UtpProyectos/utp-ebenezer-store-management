import { apiClient } from '@/shared/services/apiClient'
import type { PurchaseRequest, PurchaseResponse } from '../types/purchase.types'

export const purchaseApi = {
  create: (request: PurchaseRequest) => apiClient.post<PurchaseResponse>('/purchases', request),
}
