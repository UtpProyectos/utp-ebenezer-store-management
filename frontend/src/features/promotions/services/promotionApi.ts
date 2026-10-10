import { apiClient } from '@/shared/services/apiClient'
import type { PromotionResponse } from '../types/promotion.types'

export const promotionApi = {
  /** Promotions that apply right now, newest first. */
  getCurrent: () => apiClient.get<PromotionResponse[]>('/promotions'),
}
