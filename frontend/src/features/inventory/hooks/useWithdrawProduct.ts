import { useState } from 'react'
import { ApiClientError } from '@/shared/services/apiClient'
import { inventoryApi } from '../services/inventoryApi'
import type { ProductStockResponse, WithdrawalType } from '../types/inventory.types'

// Withdrawals from this screen are always for expired products.
const EXPIRED_REASON = 'EXPIRED'

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    if (error.status === 422) return 'No hay suficiente stock para retirar esa cantidad.'
    return error.payload?.message ?? 'No se pudo retirar el producto. Intenta de nuevo.'
  }
  return 'No hay conexión con el servidor.'
}

export function useWithdrawProduct() {
  const [isPending, setIsPending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async (item: ProductStockResponse, movementType: WithdrawalType, quantity: number) => {
    setIsPending(true)
    setError(null)
    try {
      await inventoryApi.registerWithdrawal({
        productId: item.productId,
        movementType,
        quantity,
        reason: EXPIRED_REASON,
      })
      return true
    } catch (caught) {
      setError(toErrorMessage(caught))
      return false
    } finally {
      setIsPending(false)
    }
  }

  return { submit, isPending, error, clearError: () => setError(null) }
}
