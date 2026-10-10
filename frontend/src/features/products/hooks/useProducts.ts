import { useCallback, useEffect, useState } from 'react'
import { productApi, type ProductFilters } from '../services/productApi'
import type { Product } from '../types/product.types'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'No se pudieron cargar los productos.'
}

export function useProducts(filters: ProductFilters) {
  const { search, categoryId, active } = filters
  const [reloadKey, setReloadKey] = useState(0)
  const requestKey = JSON.stringify([search ?? '', categoryId ?? '', active ?? 'all', reloadKey])
  const [result, setResult] = useState<{
    requestKey: string
    products: Product[]
    error: string | null
  }>({ requestKey: '', products: [], error: null })

  const reload = useCallback(() => setReloadKey((key) => key + 1), [])

  useEffect(() => {
    let cancelled = false

    productApi
      .getAll({ search, categoryId, active })
      .then((result) => {
        if (!cancelled) setResult({ requestKey, products: result, error: null })
      })
      .catch((loadError: unknown) => {
        if (!cancelled) setResult({ requestKey, products: [], error: messageFromError(loadError) })
      })

    return () => {
      cancelled = true
    }
  }, [active, categoryId, search, requestKey])

  const loading = result.requestKey !== requestKey
  return {
    products: loading ? [] : result.products,
    loading,
    error: loading ? null : result.error,
    reload,
  }
}
