import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiClientError } from '@/shared/services/apiClient'
import { shoppingListApi } from '../services/shoppingListApi'
import type {
  ShoppingListDetailResponse,
  ShoppingListDetailUpdateRequest,
  ShoppingListResponse,
} from '../types/shopping-list.types'

// Backend business messages (422) shown in plain Spanish.
const BUSINESS_MESSAGES: Record<string, string> = {
  'There are no products to restock': 'No hay productos por agotarse. Todo tu stock está bien.',
  'The shopping list is cancelled': 'Esta lista ya no está activa. Recarga la pantalla.',
}

function errorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    const message = error.payload?.message ?? ''
    if (BUSINESS_MESSAGES[message]) return BUSINESS_MESSAGES[message]
    if (error.status === 404) return 'Ese producto ya no está en la lista. Recarga la pantalla.'
    return 'No se pudo guardar. Intenta de nuevo.'
  }
  return 'No hay conexión con el servidor.'
}

/** A cancelled list (all items removed) is the same as having no list. */
function activeOrNull(list: ShoppingListResponse | undefined): ShoppingListResponse | null {
  return list && list.status !== 'CANCELLED' ? list : null
}

function applyLocally(detail: ShoppingListDetailResponse, change: ShoppingListDetailUpdateRequest) {
  const quantity = change.suggestedQuantity ?? detail.suggestedQuantity
  const unitCost = detail.suggestedQuantity > 0 ? detail.estimatedCost / detail.suggestedQuantity : 0
  return {
    ...detail,
    suggestedQuantity: quantity,
    estimatedCost: unitCost * quantity,
    purchased: change.purchased ?? detail.purchased,
  }
}

/** Current shopping list. Changes are shown right away and then replaced by the backend answer. */
export function useShoppingList() {
  const [list, setList] = useState<ShoppingListResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [generating, setGenerating] = useState(false)
  // Only the answer of the latest change is applied, so fast taps on +/− do not jump back.
  const lastRequest = useRef(0)

  const load = useCallback(async () => {
    setLoadError(null)
    try {
      setList(activeOrNull(await shoppingListApi.getCurrent()))
    } catch {
      setLoadError('No se pudo cargar la lista de compra.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    shoppingListApi
      .getCurrent()
      .then((current) => {
        if (!cancelled) setList(activeOrNull(current))
      })
      .catch(() => {
        if (!cancelled) setLoadError('No se pudo cargar la lista de compra.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const run = async (request: () => Promise<ShoppingListResponse>) => {
    const id = ++lastRequest.current
    setActionError(null)
    try {
      const updated = await request()
      if (id === lastRequest.current) setList(activeOrNull(updated))
    } catch (error: unknown) {
      setActionError(errorMessage(error))
      void load()
    }
  }

  const updateDetail = (detailId: number, change: ShoppingListDetailUpdateRequest) => {
    setList((current) =>
      current && {
        ...current,
        details: current.details.map((detail) => (detail.id === detailId ? applyLocally(detail, change) : detail)),
      },
    )
    void run(() => shoppingListApi.updateDetail(detailId, change))
  }

  const removeDetail = (detailId: number) => {
    setList((current) =>
      activeOrNull(
        current
          ? {
              ...current,
              status: current.details.length === 1 ? 'CANCELLED' : current.status,
              details: current.details.filter((detail) => detail.id !== detailId),
            }
          : undefined,
      ),
    )
    void run(() => shoppingListApi.removeDetail(detailId))
  }

  const generate = async () => {
    setGenerating(true)
    await run(() => shoppingListApi.generate())
    setGenerating(false)
  }

  return {
    list,
    loading,
    loadError,
    actionError,
    generating,
    reload: load,
    generate,
    setQuantity: (detailId: number, quantity: number) => updateDetail(detailId, { suggestedQuantity: quantity }),
    setPurchased: (detailId: number, purchased: boolean) => updateDetail(detailId, { purchased }),
    removeDetail,
  }
}
