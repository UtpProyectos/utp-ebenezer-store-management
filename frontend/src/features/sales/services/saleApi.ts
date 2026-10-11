import { apiClient } from '@/shared/services/apiClient'
import type {
  SaleCancelRequest,
  SaleHistoryResponse,
  SaleRequest,
  SaleResponse,
  SaleUpdateRequest,
} from '../types/sale.types'

export const saleApi = {
  create: (request: SaleRequest) => apiClient.post<SaleResponse>('/sales', request),
  /** Sales of today (server date), every status, newest first. */
  getToday: () => apiClient.get<SaleResponse[]>('/sales'),
  /** Sales between two ISO dates (yyyy-MM-dd, both included), every status, newest first. */
  getByPeriod: (from: string, to: string) => apiClient.get<SaleResponse[]>(`/sales?from=${from}&to=${to}`),
  /** Edits and cancellations made today, newest first. */
  getTodayChanges: () => apiClient.get<SaleHistoryResponse[]>('/sales/changes'),
  update: (id: number, request: SaleUpdateRequest) => apiClient.put<SaleResponse>(`/sales/${id}`, request),
  cancel: (id: number, request: SaleCancelRequest) => apiClient.patch<SaleResponse>(`/sales/${id}/cancel`, request),
}
