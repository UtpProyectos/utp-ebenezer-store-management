// Mirrors pe.edu.utp.ebenezer.api.dto.inventory.*

export type UnitType = 'WEIGHT' | 'VOLUME' | 'UNIT'

/** Derived by the backend from stock and lot expiration. */
export type StockStatus = 'OK' | 'LOW' | 'CRITICAL' | 'EXPIRING_SOON' | 'EXPIRED'

/** Quantities are expressed in the product base unit. */
export interface ProductStockResponse {
  productId: number
  productName: string
  barcode: string | null
  categoryName: string
  baseUnitId: number
  baseUnitAbbreviation: string
  baseUnitType: UnitType
  currentStock: number
  minStock: number
  salePrice: number
  /** Cost per base unit of the last purchase. */
  lastUnitCost: number | null
  lastSupplierName: string | null
  /** ISO date (yyyy-MM-dd) of the nearest lot with stock. */
  nextExpirationDate: string | null
  expiredQuantity: number
  status: StockStatus
}

export type WithdrawalType = 'WASTE' | 'RETURN'

export interface InventoryMovementRequest {
  productId: number
  lotId?: number | null
  movementType: WithdrawalType
  /** Positive quantity in the product base unit; the backend applies the sign. */
  quantity: number
  reason?: string | null
  notes?: string | null
}

export interface InventoryMovementResponse {
  id: number
  productId: number
  productName: string
  lotId: number | null
  userId: number
  userName: string
  movementType: string
  baseQuantity: number
  purchaseDetailId: number | null
  saleDetailId: number | null
  internalConsumptionDetailId: number | null
  movementDate: string
  reason: string | null
  notes: string | null
}
