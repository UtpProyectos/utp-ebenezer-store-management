import { useEffect, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { SupplierForm } from '../components/SupplierForm'
import { SupplierList } from '../components/SupplierList'
import { productApi } from '@/features/products/services/productApi'
import type { Product } from '@/features/products/types/product.types'
import { supplierApi } from '../services/supplierApi'
import type { Supplier, SupplierInput } from '../types/supplier.types'

type StatusFilter = 'all' | 'active' | 'inactive'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

export function SupplierListPage() {
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  // ?q= comes from the header search.
  const [searchParams] = useSearchParams()
  const urlQuery = searchParams.get('q') ?? ''
  const [syncedUrlQuery, setSyncedUrlQuery] = useState(urlQuery)
  const [searchText, setSearchText] = useState(urlQuery)
  const [appliedSearch, setAppliedSearch] = useState(urlQuery)
  if (urlQuery !== syncedUrlQuery) {
    setSyncedUrlQuery(urlQuery)
    setSearchText(urlQuery)
    setAppliedSearch(urlQuery)
  }
  const [status, setStatus] = useState<StatusFilter>('all')
  const [editingSupplier, setEditingSupplier] = useState<Supplier | undefined>()
  const [formOpen, setFormOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [busySupplierId, setBusySupplierId] = useState<number | null>(null)

  async function reload() {
    setLoading(true)
    setLoadError(null)
    try {
      const active = status === 'all' ? undefined : status === 'active'
      setSuppliers(await supplierApi.getAll({ search: appliedSearch, active }))
    } catch (error: unknown) {
      setLoadError(messageFromError(error))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false
    const active = status === 'all' ? undefined : status === 'active'

    supplierApi.getAll({ search: appliedSearch, active })
      .then((supplierList) => {
        if (!cancelled) setSuppliers(supplierList)
      })
      .catch((error: unknown) => {
        if (!cancelled) setLoadError(messageFromError(error))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [appliedSearch, status])

  // The product options do not depend on the supplier filters: load them once.
  useEffect(() => {
    let cancelled = false

    productApi.getAll()
      .then((productList) => {
        if (!cancelled) setProducts(productList)
      })
      .catch((error: unknown) => {
        if (!cancelled) setLoadError(messageFromError(error))
      })

    return () => {
      cancelled = true
    }
  }, [])

  function applySearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAppliedSearch(searchText.trim())
  }

  function openCreateForm() {
    setActionError(null)
    setEditingSupplier(undefined)
    setFormOpen(true)
  }

  function openEditForm(supplier: Supplier) {
    setActionError(null)
    setEditingSupplier(supplier)
    setFormOpen(true)
  }

  async function saveSupplier(input: SupplierInput) {
    setSaving(true)
    setActionError(null)
    try {
      if (editingSupplier) {
        await supplierApi.update(editingSupplier.id, input)
      } else {
        await supplierApi.create(input)
      }
      setFormOpen(false)
      setEditingSupplier(undefined)
      await reload()
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setSaving(false)
    }
  }

  async function toggleStatus(supplier: Supplier) {
    setBusySupplierId(supplier.id)
    setActionError(null)
    try {
      const updated = await supplierApi.updateStatus(supplier.id, !supplier.active)
      // Same result as reloading: the supplier leaves the list if it no longer matches the status filter.
      setSuppliers((previous) => status !== 'all' && updated.active !== (status === 'active')
        ? previous.filter((item) => item.id !== updated.id)
        : previous.map((item) => (item.id === updated.id ? updated : item)))
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setBusySupplierId(null)
    }
  }

  const activeCount = suppliers.filter((supplier) => supplier.active).length

  return (
    <div className="mx-auto flex w-full max-w-350 flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <form className="flex w-full max-w-md gap-2" onSubmit={applySearch}>
          <SearchInput label="Buscar proveedor" value={searchText} onChange={setSearchText} className="min-w-0 flex-1" />
          <button
            type="submit"
            className="h-13 rounded-full bg-surface-secondary px-5 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
          >
            Buscar
          </button>
        </form>
        <button
          type="button"
          className="min-h-11 rounded-full bg-accent px-5 text-sm font-semibold text-background hover:opacity-90 focus-visible:outline-2 focus-visible:outline-focus"
          onClick={openCreateForm}
        >
          + Nuevo proveedor
        </button>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-muted">
          {suppliers.length} {suppliers.length === 1 ? 'proveedor' : 'proveedores'} · {activeCount} activos
        </p>
        <div className="flex gap-1 rounded-full bg-surface-secondary p-1" role="group" aria-label="Filtrar proveedores por estado">
          {([
            ['all', 'Todos'],
            ['active', 'Activos'],
            ['inactive', 'Inactivos'],
          ] as const).map(([value, label]) => (
            <button
              key={value}
              type="button"
              aria-pressed={status === value}
              className={`min-h-9 rounded-full px-4 text-sm focus-visible:outline-2 focus-visible:outline-focus ${
                status === value ? 'bg-surface font-semibold text-foreground' : 'text-muted hover:text-foreground'
              }`}
              onClick={() => setStatus(value)}
            >
              {label}
            </button>
          ))}
        </div>
      </div>

      {actionError && !formOpen && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          {actionError}
        </p>
      )}
      {formOpen && (
        <SupplierForm
          key={editingSupplier?.id ?? 'new'}
          supplier={editingSupplier}
          products={products}
          saving={saving}
          error={actionError}
          onSubmit={saveSupplier}
          onCancel={() => setFormOpen(false)}
        />
      )}

      {loadError ? (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          No se pudieron cargar los proveedores: {loadError}
        </p>
      ) : loading ? (
        <p role="status" className="rounded-3xl bg-surface px-5 py-12 text-center text-sm text-muted">
          Cargando proveedores…
        </p>
      ) : (
        <SupplierList
          suppliers={suppliers}
          busySupplierId={busySupplierId}
          onEdit={openEditForm}
          onToggleStatus={toggleStatus}
        />
      )}
    </div>
  )
}
