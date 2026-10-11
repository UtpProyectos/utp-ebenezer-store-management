import { apiClient } from '@/shared/services/apiClient'
import type { InternalConsumptionRequest, InternalConsumptionResponse } from '../types/consumption.types'

export const consumptionApi = {
  create: (request: InternalConsumptionRequest) =>
    apiClient.post<InternalConsumptionResponse>('/internal-consumptions', request),
  /** Admin only. Consumptions between two ISO dates (yyyy-MM-dd, both included), newest first. */
  getByPeriod: (from: string, to: string) =>
    apiClient.get<InternalConsumptionResponse[]>(`/internal-consumptions?from=${from}&to=${to}`),
}
