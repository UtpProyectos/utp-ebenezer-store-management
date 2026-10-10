import { useEffect, useMemo, useState } from 'react'
import { useInventory } from '@/features/inventory'
import { promotionApi } from '@/features/promotions/services/promotionApi'
import type { PromotionResponse } from '@/features/promotions/types/promotion.types'

/** Products of the sales screen (stock, price, category) plus the promotion that applies to each one. */
export function useSaleCatalog() {
  const inventory = useInventory()
  const [promotions, setPromotions] = useState<PromotionResponse[]>([])

  useEffect(() => {
    let cancelled = false
    promotionApi
      .getCurrent()
      .then((list) => {
        if (!cancelled) setPromotions(list)
      })
      // Without promotions the screen still sells at the regular price.
      .catch(() => {})
    return () => {
      cancelled = true
    }
  }, [])

  // The backend sends the newest first: that is the one that applies.
  const promotionByProduct = useMemo(() => {
    const byProduct = new Map<number, PromotionResponse>()
    for (const promotion of promotions) {
      if (!byProduct.has(promotion.productId)) byProduct.set(promotion.productId, promotion)
    }
    return byProduct
  }, [promotions])

  const categories = useMemo(
    () => [...new Set(inventory.items.map((item) => item.categoryName))].sort((a, b) => a.localeCompare(b, 'es')),
    [inventory.items],
  )

  return {
    products: inventory.items,
    promotionByProduct,
    categories,
    initialLoading: inventory.initialLoading,
    error: inventory.error,
    reload: inventory.reload,
  }
}
