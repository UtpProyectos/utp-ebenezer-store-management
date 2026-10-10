import { useMemo, useState } from 'react'
import type { ProductStockResponse } from '@/features/inventory'
import type { PromotionResponse } from '@/features/promotions/types/promotion.types'
import { isWeighed, lineTotal, sellableStock } from '../utils/salePricing'

export type CheckoutMode = 'sale' | 'consumption'

/** Quantity is expressed in the product base unit (kg for weighed products). */
interface CartEntry {
  productId: number
  quantity: number
}

export interface CartLine {
  item: ProductStockResponse
  promotion?: PromotionResponse
  quantity: number
  total: number
}

export function useCart(products: ProductStockResponse[], promotionByProduct: Map<number, PromotionResponse>) {
  const [entries, setEntries] = useState<CartEntry[]>([])
  const [mode, setMode] = useState<CheckoutMode>('sale')

  const lines = useMemo<CartLine[]>(() => {
    const byId = new Map(products.map((item) => [item.productId, item]))
    return entries.flatMap((entry) => {
      const item = byId.get(entry.productId)
      if (!item) return []
      const promotion = promotionByProduct.get(item.productId)
      return [{ item, promotion, quantity: entry.quantity, total: lineTotal(item, entry.quantity, promotion) }]
    })
  }, [entries, products, promotionByProduct])

  const quantityOf = (productId: number) => entries.find((entry) => entry.productId === productId)?.quantity ?? 0

  /** Sets the quantity, capped at the sellable stock. Zero or less removes the line. */
  const setQuantity = (item: ProductStockResponse, quantity: number) => {
    const capped = Math.min(quantity, sellableStock(item))
    setEntries((current) => {
      if (capped <= 0) return current.filter((entry) => entry.productId !== item.productId)
      if (current.some((entry) => entry.productId === item.productId)) {
        return current.map((entry) => (entry.productId === item.productId ? { ...entry, quantity: capped } : entry))
      }
      return [...current, { productId: item.productId, quantity: capped }]
    })
  }

  /** Adds one unit. Returns false when there is no more stock. */
  const addOne = (item: ProductStockResponse) => {
    const next = quantityOf(item.productId) + 1
    if (next > sellableStock(item)) return false
    setQuantity(item, next)
    return true
  }

  const subtotal = lines.reduce((sum, line) => sum + line.total, 0)

  return {
    lines,
    mode,
    setMode,
    quantityOf,
    setQuantity,
    addOne,
    remove: (productId: number) => setEntries((current) => current.filter((entry) => entry.productId !== productId)),
    clear: () => setEntries([]),
    subtotal: Math.round(subtotal * 100) / 100,
    /** Weighed products count as one item each, like the prototype. */
    itemCount: lines.reduce((sum, line) => sum + (isWeighed(line.item) ? 1 : line.quantity), 0),
  }
}
