import { useState } from 'react'
import { consumptionApi } from '@/features/consumption/services/consumptionApi'
import { ApiClientError } from '@/shared/services/apiClient'
import { saleApi } from '../services/saleApi'
import type { PaymentMethod, SaleResponse } from '../types/sale.types'
import type { CartLine } from './useCart'

export type CheckoutResult =
  | { kind: 'sale'; sale: SaleResponse; change: number }
  | { kind: 'consumption'; itemCount: number; cost: number }

const NOT_ENOUGH_STOCK = 'Not enough stock for '

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    const message = error.payload?.message ?? ''
    if (message.startsWith(NOT_ENOUGH_STOCK)) {
      return `Ya no hay stock suficiente de ${message.slice(NOT_ENOUGH_STOCK.length)}. Revisa la cantidad.`
    }
    if (error.status === 404) return 'Uno de los productos ya no está disponible. Recarga la pantalla.'
    return 'No se pudo registrar. Intenta de nuevo.'
  }
  return 'No hay conexión con el servidor.'
}

// Quantities go in the product base unit, which is what the cart keeps.
function toDetails(lines: CartLine[]) {
  return lines.map(({ item, quantity }) => ({ productId: item.productId, unitOfMeasureId: item.baseUnitId, quantity }))
}

export function useCheckout(onRegistered: () => void) {
  const [isPending, setIsPending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<CheckoutResult | null>(null)

  const run = async (action: () => Promise<CheckoutResult>) => {
    setIsPending(true)
    setError(null)
    try {
      setResult(await action())
      onRegistered()
      return true
    } catch (caught) {
      setError(toErrorMessage(caught))
      return false
    } finally {
      setIsPending(false)
    }
  }

  const chargeSale = (lines: CartLine[], paymentMethod: PaymentMethod, received: number) =>
    run(async () => {
      const sale = await saleApi.create({ paymentMethod, details: toDetails(lines) })
      return { kind: 'sale', sale, change: received > sale.total ? Math.round((received - sale.total) * 100) / 100 : 0 }
    })

  const registerConsumption = (lines: CartLine[], itemCount: number) =>
    run(async () => {
      await consumptionApi.create({ details: toDetails(lines) })
      const cost = lines.reduce((sum, { item, quantity }) => sum + quantity * (item.lastUnitCost ?? 0), 0)
      return { kind: 'consumption', itemCount, cost }
    })

  return {
    chargeSale,
    registerConsumption,
    isPending,
    error,
    result,
    clearError: () => setError(null),
    reset: () => {
      setResult(null)
      setError(null)
    },
  }
}
