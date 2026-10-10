// Mirrors pe.edu.utp.ebenezer.api.dto.consumption.*

export interface InternalConsumptionDetailRequest {
  productId: number
  unitOfMeasureId: number
  quantity: number
}

export interface InternalConsumptionRequest {
  /** ISO date-time; the backend uses now when null. */
  consumptionDate?: string | null
  reason?: string | null
  notes?: string | null
  details: InternalConsumptionDetailRequest[]
}

export interface InternalConsumptionDetailResponse {
  id: number
  productId: number
  productName: string
  unitOfMeasureId: number
  unitOfMeasureAbbreviation: string
  quantity: number
  baseQuantity: number
}

export interface InternalConsumptionResponse {
  id: number
  userId: number
  userName: string
  consumptionDate: string
  reason: string | null
  notes: string | null
  details: InternalConsumptionDetailResponse[]
}
