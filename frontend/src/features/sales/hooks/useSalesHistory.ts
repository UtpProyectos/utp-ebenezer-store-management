import { useCallback, useEffect, useState } from 'react'
import { saleApi } from '../services/saleApi'
import type { SaleHistoryResponse, SaleResponse } from '../types/sale.types'

interface HistoryData {
  sales: SaleResponse[]
  changes: SaleHistoryResponse[]
}

/** Sales of today and the edits / cancellations made on them. */
export function useSalesHistory() {
  const [reloadKey, setReloadKey] = useState(0)
  const [result, setResult] = useState<{ reloadKey: number; data: HistoryData; error: string | null }>({
    reloadKey: -1,
    data: { sales: [], changes: [] },
    error: null,
  })

  const reload = useCallback(() => setReloadKey((key) => key + 1), [])

  useEffect(() => {
    let cancelled = false
    Promise.all([saleApi.getToday(), saleApi.getTodayChanges()])
      .then(([sales, changes]) => {
        if (!cancelled) setResult({ reloadKey, data: { sales, changes }, error: null })
      })
      .catch(() => {
        if (!cancelled) {
          setResult((previous) => ({ ...previous, reloadKey, error: 'No se pudieron cargar las ventas.' }))
        }
      })
    return () => {
      cancelled = true
    }
  }, [reloadKey])

  // Keep showing the previous list while reloading after a change.
  const loading = result.reloadKey !== reloadKey
  return {
    ...result.data,
    initialLoading: loading && result.reloadKey === -1,
    error: loading ? null : result.error,
    reload,
  }
}
