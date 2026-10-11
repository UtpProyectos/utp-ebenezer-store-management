import { useCallback, useEffect, useState } from 'react'
import { consumptionApi } from '@/features/consumption/services/consumptionApi'
import { inventoryApi } from '@/features/inventory/services/inventoryApi'
import { purchaseApi } from '@/features/purchases/services/purchaseApi'
import { saleApi } from '@/features/sales'
import type { HistoryPeriod, HistoryRow, HistoryTab } from '../types/history.types'
import { periodRange } from '../utils/historyPeriod'
import { consumptionRows, movementRows, purchaseRows, saleRows } from '../utils/historyRows'

const LOADERS: Record<HistoryTab, (from: string, to: string) => Promise<HistoryRow[]>> = {
  sales: (from, to) => saleApi.getByPeriod(from, to).then(saleRows),
  purchases: (from, to) => purchaseApi.getByPeriod(from, to).then(purchaseRows),
  movements: (from, to) => inventoryApi.getMovements(from, to).then(movementRows),
  consumption: (from, to) => consumptionApi.getByPeriod(from, to).then(consumptionRows),
}

/** Rows of one history tab for a period. Only the visible tab is requested. */
export function useHistory(tab: HistoryTab, period: HistoryPeriod) {
  const [reloadKey, setReloadKey] = useState(0)
  const requestKey = `${tab}|${period}|${reloadKey}`
  const [result, setResult] = useState<{ key: string; rows: HistoryRow[]; error: string | null }>({
    key: '',
    rows: [],
    error: null,
  })

  const reload = useCallback(() => setReloadKey((key) => key + 1), [])

  useEffect(() => {
    let cancelled = false
    const { from, to } = periodRange(period)
    LOADERS[tab](from, to)
      .then((rows) => {
        if (!cancelled) setResult({ key: requestKey, rows, error: null })
      })
      .catch(() => {
        if (!cancelled) setResult({ key: requestKey, rows: [], error: 'No se pudo cargar el historial.' })
      })
    return () => {
      cancelled = true
    }
  }, [tab, period, requestKey])

  const loading = result.key !== requestKey
  return { rows: loading ? [] : result.rows, loading, error: loading ? null : result.error, reload }
}
