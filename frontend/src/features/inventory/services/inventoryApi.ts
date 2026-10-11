import { apiClient } from '@/shared/services/apiClient'
import type {
  InventoryMovementRequest,
  InventoryMovementResponse,
  ProductStockResponse,
} from '../types/inventory.types'

export const inventoryApi = {
  getStock: () => apiClient.get<ProductStockResponse[]>('/inventory'),
  /** Admin only. Kardex between two ISO dates (yyyy-MM-dd, both included), newest first. */
  getMovements: (from: string, to: string) =>
    apiClient.get<InventoryMovementResponse[]>(`/inventory/movements?from=${from}&to=${to}`),
  registerWithdrawal: (request: InventoryMovementRequest) =>
    apiClient.post<InventoryMovementResponse[]>('/inventory/movements', request),
}
