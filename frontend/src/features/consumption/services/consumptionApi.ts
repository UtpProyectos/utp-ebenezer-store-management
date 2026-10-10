import { apiClient } from '@/shared/services/apiClient'
import type { InternalConsumptionRequest, InternalConsumptionResponse } from '../types/consumption.types'

export const consumptionApi = {
  create: (request: InternalConsumptionRequest) =>
    apiClient.post<InternalConsumptionResponse>('/internal-consumptions', request),
}
