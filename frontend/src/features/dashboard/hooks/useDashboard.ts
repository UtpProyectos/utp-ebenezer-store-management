import { useCallback, useEffect, useMemo, useState } from 'react'
import { matchesFilter, useInventory, type ProductStockResponse, type StockStatus } from '@/features/inventory'
import { saleApi, type SaleResponse } from '@/features/sales'

const MAX_ATTENTION = 3

// Same priority as the prototype: expired, critical, expiring soon, running low.
const ATTENTION_ORDER: Record<Exclude<StockStatus, 'OK'>, number> = {
  EXPIRED: 0,
  CRITICAL: 1,
  EXPIRING_SOON: 2,
  LOW: 3,
}

function attentionRank(item: ProductStockResponse) {
  return item.status === 'OK' ? Number.POSITIVE_INFINITY : ATTENTION_ORDER[item.status]
}

/** Today's numbers for Home: sales of the day (backend date) and the stock status of every product. */
export function useDashboard() {
  const inventory = useInventory()
  const [salesReloadKey, setSalesReloadKey] = useState(0)
  const [salesResult, setSalesResult] = useState<{ reloadKey: number; sales: SaleResponse[]; error: string | null }>({
    reloadKey: -1,
    sales: [],
    error: null,
  })

  useEffect(() => {
    let cancelled = false
    saleApi
      .getToday()
      .then((sales) => {
        if (!cancelled) setSalesResult({ reloadKey: salesReloadKey, sales, error: null })
      })
      .catch(() => {
        if (!cancelled) {
          setSalesResult((previous) => ({ ...previous, reloadKey: salesReloadKey, error: 'No se pudieron cargar las ventas de hoy.' }))
        }
      })
    return () => {
      cancelled = true
    }
  }, [salesReloadKey])

  const { reload: reloadInventory } = inventory
  const reload = useCallback(() => {
    reloadInventory()
    setSalesReloadKey((key) => key + 1)
  }, [reloadInventory])

  // Cancelled sales are kept in the history but do not count as sold.
  const sales = useMemo(() => salesResult.sales.filter((sale) => sale.status !== 'CANCELLED'), [salesResult.sales])

  const attention = useMemo(
    () =>
      inventory.items
        .filter((item) => item.status !== 'OK')
        .sort((a, b) => attentionRank(a) - attentionRank(b))
        .slice(0, MAX_ATTENTION),
    [inventory.items],
  )

  const salesLoading = salesResult.reloadKey !== salesReloadKey
  return {
    /** Every sale of today, including cancelled ones (for the activity list). */
    todaySales: salesResult.sales,
    salesTotal: sales.reduce((sum, sale) => sum + sale.total, 0),
    salesCount: sales.length,
    restockCount: inventory.items.filter((item) => matchesFilter(item, 'restock')).length,
    expiringCount: inventory.items.filter((item) => matchesFilter(item, 'expiring')).length,
    attention,
    initialLoading: inventory.initialLoading || (salesLoading && salesResult.reloadKey === -1),
    error: inventory.error ?? (salesLoading ? null : salesResult.error),
    reload,
  }
}
