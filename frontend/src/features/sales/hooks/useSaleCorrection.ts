import { useState } from 'react'
import { saleApi } from '../services/saleApi'
import type { SaleCancelRequest, SaleUpdateRequest } from '../types/sale.types'
import { saleErrorMessage } from '../utils/saleErrors'

/** Edits or cancels a registered sale. Both actions require a reason and stay in the sale history. */
export function useSaleCorrection() {
  const [isPending, setIsPending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const run = async (action: () => Promise<unknown>) => {
    setIsPending(true)
    setError(null)
    try {
      await action()
      return true
    } catch (caught) {
      setError(saleErrorMessage(caught))
      return false
    } finally {
      setIsPending(false)
    }
  }

  return {
    update: (id: number, request: SaleUpdateRequest) => run(() => saleApi.update(id, request)),
    cancel: (id: number, request: SaleCancelRequest) => run(() => saleApi.cancel(id, request)),
    isPending,
    error,
    clearError: () => setError(null),
  }
}
