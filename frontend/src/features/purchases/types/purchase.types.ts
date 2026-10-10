// Mirrors pe.edu.utp.ebenezer.api.dto.purchase.*

export type PurchaseStatus = 'REGISTERED' | 'CANCELLED'

export interface PurchaseDetailRequest {
  productId: number
  unitOfMeasureId: number
  quantity: number
  /** Total cost of the line; the backend derives the unit cost. */
  subtotal: number
  /** Updates the product sale price when present. */
  salePrice?: number | null
  lotCode?: string | null
  /** ISO date (yyyy-MM-dd); null when the product does not expire. */
  expirationDate?: string | null
}

export interface PurchaseRequest {
  supplierId?: number | null
  purchaseDate?: string | null
  notes?: string | null
  details: PurchaseDetailRequest[]
}

export interface PurchaseDetailResponse {
  id: number
  productId: number
  productName: string
  unitOfMeasureId: number
  unitOfMeasureAbbreviation: string
  quantity: number
  baseQuantity: number
  unitCost: number
  subtotal: number
}

export interface PurchaseResponse {
  id: number
  supplierId: number | null
  supplierName: string | null
  userId: number
  userName: string
  purchaseDate: string
  subtotal: number
  total: number
  status: PurchaseStatus
  notes: string | null
  details: PurchaseDetailResponse[]
}
