import { apiClient } from '@/shared/services/apiClient'
import type {
  InventoryMovementRequest,
  InventoryMovementResponse,
  ProductStockResponse,
} from '../types/inventory.types'

export const inventoryApi = {
  getStock: () => apiClient.get<ProductStockResponse[]>('/inventory'),
  registerWithdrawal: (request: InventoryMovementRequest) =>
    apiClient.post<InventoryMovementResponse[]>('/inventory/movements', request),
}
