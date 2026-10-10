import { useCallback, useEffect, useState } from 'react'
import { inventoryApi } from '../services/inventoryApi'
import type { ProductStockResponse } from '../types/inventory.types'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'No se pudo cargar el inventario.'
}

export function useInventory() {
  const [reloadKey, setReloadKey] = useState(0)
  const [result, setResult] = useState<{
    reloadKey: number
    items: ProductStockResponse[]
    error: string | null
  }>({ reloadKey: -1, items: [], error: null })

  const reload = useCallback(() => setReloadKey((key) => key + 1), [])

  useEffect(() => {
    let cancelled = false

    inventoryApi
      .getStock()
      .then((items) => {
        if (!cancelled) setResult({ reloadKey, items, error: null })
      })
      .catch((loadError: unknown) => {
        if (!cancelled) setResult((previous) => ({ ...previous, reloadKey, error: messageFromError(loadError) }))
      })

    return () => {
      cancelled = true
    }
  }, [reloadKey])

  // Keep showing the previous list while reloading after a change.
  const loading = result.reloadKey !== reloadKey
  return {
    items: result.items,
    loading,
    initialLoading: loading && result.reloadKey === -1,
    error: loading ? null : result.error,
    reload,
  }
}
