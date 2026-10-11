import { apiClient } from '@/shared/services/apiClient'
import type {
  ShoppingListDetailUpdateRequest,
  ShoppingListGenerateRequest,
  ShoppingListResponse,
} from '../types/shopping-list.types'

export const shoppingListApi = {
  /** Latest list; undefined (204) when there is none. */
  getCurrent: () => apiClient.get<ShoppingListResponse | undefined>('/shopping-lists/current'),
  generate: (request: ShoppingListGenerateRequest = {}) =>
    apiClient.post<ShoppingListResponse>('/shopping-lists/generate', request),
  updateDetail: (detailId: number, request: ShoppingListDetailUpdateRequest) =>
    apiClient.patch<ShoppingListResponse>(`/shopping-lists/details/${detailId}`, request),
  removeDetail: (detailId: number) => apiClient.delete<ShoppingListResponse>(`/shopping-lists/details/${detailId}`),
}
