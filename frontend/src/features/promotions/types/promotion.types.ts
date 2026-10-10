// Mirrors pe.edu.utp.ebenezer.api.dto.promotion.*

export interface PromotionResponse {
  id: number
  productId: number
  productName: string
  unitOfMeasureId: number
  unitOfMeasureAbbreviation: string
  name: string | null
  /** Expressed in the promotion unit (unitOfMeasureAbbreviation). */
  promotionQuantity: number
  promotionalPrice: number
  repeatable: boolean
  startDate: string | null
  endDate: string | null
  active: boolean
}
