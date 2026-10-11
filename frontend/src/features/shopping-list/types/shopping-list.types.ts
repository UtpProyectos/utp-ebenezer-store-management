// Mirrors pe.edu.utp.ebenezer.api.dto.shoppinglist.*

export type ShoppingListSource = 'MANUAL' | 'AI' | 'SYSTEM'

export type ShoppingListStatus = 'PENDING' | 'PARTIAL' | 'COMPLETED' | 'CANCELLED'

/** Quantities are expressed in the unit of the item (the product base unit). */
export interface ShoppingListDetailResponse {
  id: number
  productId: number
  productName: string
  /** Supplier of the last purchase of the product, when there is one. */
  supplierId: number | null
  supplierName: string | null
  unitOfMeasureId: number
  unitOfMeasureAbbreviation: string
  currentStock: number
  suggestedQuantity: number
  purchasedQuantity: number
  /** Quantity times the cost of the last purchase; 0 when the product was never bought. */
  estimatedCost: number
  purchased: boolean
}

export interface ShoppingListResponse {
  id: number
  userId: number
  userName: string
  createdAt: string
  source: ShoppingListSource
  status: ShoppingListStatus
  estimatedCost: number
  details: ShoppingListDetailResponse[]
}

/** Without productIds the backend adds every product at or below its minimum stock. */
export interface ShoppingListGenerateRequest {
  productIds?: number[]
}

/** Only the fields sent are changed. */
export interface ShoppingListDetailUpdateRequest {
  suggestedQuantity?: number
  purchased?: boolean
}
