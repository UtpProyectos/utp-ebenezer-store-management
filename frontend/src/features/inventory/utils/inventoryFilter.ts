import type { ProductStockResponse } from '../types/inventory.types'

export type InventoryFilter = 'all' | 'restock' | 'expiring'

export function matchesFilter(item: ProductStockResponse, filter: InventoryFilter): boolean {
  if (filter === 'restock') return item.status === 'LOW' || item.status === 'CRITICAL'
  if (filter === 'expiring') return item.status === 'EXPIRING_SOON' || item.status === 'EXPIRED'
  return true
}
