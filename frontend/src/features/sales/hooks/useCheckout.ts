import { useState } from 'react'
import { consumptionApi } from '@/features/consumption/services/consumptionApi'
import { saleApi } from '../services/saleApi'
import type { PaymentMethod, SaleResponse } from '../types/sale.types'
import { saleErrorMessage } from '../utils/saleErrors'
import type { CartLine } from './useCart'

export type CheckoutResult =
  | { kind: 'sale'; sale: SaleResponse; change: number }
  | { kind: 'consumption'; itemCount: number; cost: number }

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
      setError(saleErrorMessage(caught))
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
