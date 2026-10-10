import { useCallback, useEffect, useState } from 'react'
import { productOptionsApi } from '../services/productOptionsApi'
import type { CategoryOption, UnitOption } from '../types/product.types'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

/** Active categories and units needed to create or edit a product. */
export function useProductOptions() {
  const [data, setData] = useState<{
    categories: CategoryOption[]
    units: UnitOption[]
    error: string | null
  } | null>(null)

  useEffect(() => {
    let cancelled = false

    Promise.all([productOptionsApi.getCategories(), productOptionsApi.getUnits()])
      .then(([categories, units]) => {
        if (!cancelled) setData({ categories, units, error: null })
      })
      .catch((loadError: unknown) => {
        if (!cancelled) setData({ categories: [], units: [], error: messageFromError(loadError) })
      })

    return () => {
      cancelled = true
    }
  }, [])

  const createCategory = useCallback(async (name: string) => {
    const category = await productOptionsApi.createCategory(name)
    setData((previous) => previous && {
      ...previous,
      categories: [...previous.categories, category].sort((a, b) => a.name.localeCompare(b.name)),
    })
    return category
  }, [])

  return {
    categories: data?.categories ?? [],
    units: data?.units ?? [],
    loading: data === null,
    error: data?.error ?? null,
    createCategory,
  }
}
