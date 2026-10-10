import { useCallback, useEffect, useState } from 'react'
import { supplierApi } from '@/features/suppliers/services/supplierApi'
import type { Supplier } from '@/features/suppliers/types/supplier.types'

/** Active suppliers for the optional "Proveedor" field. Failing to load them never blocks the entry. */
export function useSupplierOptions() {
  const [suppliers, setSuppliers] = useState<Supplier[]>([])

  useEffect(() => {
    let cancelled = false
    supplierApi
      .getAll({ active: true })
      .then((result) => {
        if (!cancelled) setSuppliers(result)
      })
      .catch(() => {
        if (!cancelled) setSuppliers([])
      })
    return () => {
      cancelled = true
    }
  }, [])

  // Quick creation from the entry: only the name, linked to the product being received.
  const createSupplier = useCallback(async (name: string, productId: number | null) => {
    const supplier = await supplierApi.create({
      name,
      documentNumber: null,
      phone: null,
      type: null,
      contactName: null,
      address: null,
      notes: null,
      productIds: productId ? [productId] : [],
    })
    setSuppliers((previous) => [...previous, supplier].sort((a, b) => a.name.localeCompare(b.name)))
    return supplier
  }, [])

  return { suppliers, createSupplier }
}
