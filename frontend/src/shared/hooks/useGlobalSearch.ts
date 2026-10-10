import { useCallback, useRef, useState } from 'react'
import { formatStock, matchesSearch, type ProductStockResponse } from '@/features/inventory'
import { inventoryApi } from '@/features/inventory/services/inventoryApi'
import { supplierApi } from '@/features/suppliers/services/supplierApi'
import type { Supplier } from '@/features/suppliers/types/supplier.types'

const MAX_PRODUCTS = 5
const MAX_SUPPLIERS = 2

export interface GlobalSearchResult {
  key: string
  kind: 'product' | 'supplier'
  name: string
  meta: string
}

/**
 * Data of the header search. It is loaded the first time the field gets focus (not on every page) and
 * refreshed on later focuses, so stock stays current. Suppliers are only searched for admins.
 */
export function useGlobalSearch(includeSuppliers: boolean) {
  const [products, setProducts] = useState<ProductStockResponse[]>([])
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const loading = useRef(false)

  const load = useCallback(() => {
    if (loading.current) return
    loading.current = true
    Promise.all([
      inventoryApi.getStock(),
      includeSuppliers ? supplierApi.getAll({ active: true }) : Promise.resolve<Supplier[]>([]),
    ])
      .then(([stock, supplierList]) => {
        setProducts(stock)
        setSuppliers(supplierList)
      })
      // The search is a shortcut: if it cannot load, it simply shows no results.
      .catch(() => {})
      .finally(() => {
        loading.current = false
      })
  }, [includeSuppliers])

  const search = (query: string): GlobalSearchResult[] => {
    const normalized = query.trim().toLowerCase()
    if (!normalized) return []
    return [
      ...products
        .filter((item) => matchesSearch(item, normalized))
        .slice(0, MAX_PRODUCTS)
        .map((item) => ({
          key: `product-${item.productId}`,
          kind: 'product' as const,
          name: item.productName,
          meta: `${formatStock(item)} en stock`,
        })),
      ...suppliers
        .filter((supplier) => supplier.name.toLowerCase().includes(normalized))
        .slice(0, MAX_SUPPLIERS)
        .map((supplier) => ({
          key: `supplier-${supplier.id}`,
          kind: 'supplier' as const,
          name: supplier.name,
          meta: 'Proveedor',
        })),
    ]
  }

  return { load, search }
}
