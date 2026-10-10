// Mirrors pe.edu.utp.ebenezer.api.dto.sale.*

export type PaymentMethod = 'CASH' | 'YAPE_PLIN' | 'CARD' | 'OTHER'

export type SaleStatus = 'CONFIRMED' | 'EDITED' | 'CANCELLED'

export interface SaleDetailRequest {
  productId: number
  unitOfMeasureId: number
  quantity: number
  discount?: number | null
}

/** Prices and totals are calculated by the backend; the client never sends them. */
export interface SaleRequest {
  paymentMethod: PaymentMethod
  discount?: number | null
  details: SaleDetailRequest[]
}

export interface SaleDetailResponse {
  id: number
  productId: number
  productName: string
  unitOfMeasureId: number
  unitOfMeasureAbbreviation: string
  quantity: number
  baseQuantity: number
  unitPrice: number
  /** Promotion saving plus any line discount. */
  discount: number
  subtotal: number
}

export interface SaleResponse {
  id: number
  userId: number
  userName: string
  saleDate: string
  subtotal: number
  discount: number
  total: number
  paymentMethod: PaymentMethod | null
  status: SaleStatus
  cancellationReason: string | null
  createdAt: string
  updatedAt: string | null
  details: SaleDetailResponse[]
}

export type SaleHistoryAction = 'CREATED' | 'EDITED' | 'CANCELLED' | 'REACTIVATED'

/** New quantity of an existing line, in the unit of that line. Zero removes the product. */
export interface SaleDetailUpdateRequest {
  saleDetailId: number
  quantity: number
}

/** Lines not listed keep their quantity (an empty list only changes the payment method). Prices are recalculated by the backend. */
export interface SaleUpdateRequest {
  paymentMethod: PaymentMethod
  reason: string
  details: SaleDetailUpdateRequest[]
}

export interface SaleCancelRequest {
  reason: string
}

export interface SaleHistoryResponse {
  id: number
  saleId: number
  userId: number
  userName: string
  action: SaleHistoryAction
  /** JSON of the sale before the change. */
  previousData: string | null
  /** JSON of the sale after the change. */
  newData: string | null
  reason: string | null
  createdAt: string
}
