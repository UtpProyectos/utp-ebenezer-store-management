import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { ProductForm } from '../components/ProductForm'
import { ProductTable } from '../components/ProductTable'
import { productApi } from '../services/productApi'
import { productOptionsApi } from '../services/productOptionsApi'
import { useProducts } from '../hooks/useProducts'
import type { CategoryOption, Product, ProductInput, UnitOption } from '../types/product.types'

type StatusFilter = 'all' | 'active' | 'inactive'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

export function ProductListPage() {
  const [supportData, setSupportData] = useState<{
    categories: CategoryOption[]
    units: UnitOption[]
    error: string | null
  } | null>(null)
  const [searchText, setSearchText] = useState('')
  const [appliedSearch, setAppliedSearch] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [status, setStatus] = useState<StatusFilter>('all')
  const [formProduct, setFormProduct] = useState<Product | undefined>()
  const [showForm, setShowForm] = useState(false)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const filters = {
    search: appliedSearch,
    categoryId: categoryId ? Number(categoryId) : undefined,
    active: status === 'all' ? undefined : status === 'active',
  }
  const { products, loading, error, reload } = useProducts(filters)
  const categories = supportData?.categories ?? []
  const units = supportData?.units ?? []
  const supportLoading = supportData === null
  const supportError = supportData?.error ?? null

  useEffect(() => {
    let cancelled = false

    Promise.all([productOptionsApi.getCategories(), productOptionsApi.getUnits()])
      .then(([categoryOptions, unitOptions]) => {
        if (cancelled) return
        setSupportData({ categories: categoryOptions, units: unitOptions, error: null })
      })
      .catch((loadError: unknown) => {
        if (!cancelled) {
          setSupportData({ categories: [], units: [], error: messageFromError(loadError) })
        }
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
    setFormProduct(undefined)
    setFormError(null)
    setActionError(null)
    setShowForm(true)
  }

  function openEditForm(product: Product) {
    setFormProduct(product)
    setFormError(null)
    setActionError(null)
    setShowForm(true)
  }

  async function saveProduct(input: ProductInput) {
    setSaving(true)
    setFormError(null)
    try {
      if (formProduct) {
        await productApi.update(formProduct.id, input)
      } else {
        await productApi.create(input)
      }
      setShowForm(false)
      setFormProduct(undefined)
      reload()
    } catch (saveError: unknown) {
      setFormError(messageFromError(saveError))
    } finally {
      setSaving(false)
    }
  }

  async function toggleStatus(product: Product) {
    setActionError(null)
    try {
      await productApi.updateStatus(product.id, !product.active)
      reload()
    } catch (statusError: unknown) {
      setActionError(messageFromError(statusError))
    }
  }

  return (
    <div className="mx-auto flex w-full max-w-350 flex-col gap-4">
      {supportError && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          No se pudieron cargar las categorías y unidades: {supportError}
        </p>
      )}
      {!supportLoading && !supportError && (categories.length === 0 || units.length === 0) && (
        <p role="status" className="rounded-2xl bg-warning-soft px-4 py-3 text-sm text-warning-soft-foreground">
          Para crear productos se necesita una categoría activa y una unidad de medida. Puedes administrar categorías en{' '}
          <Link className="font-semibold underline" to={ROUTES.categories}>Administración → Categorías</Link>. Para cargar
          la lista inicial de 40 productos junto con sus categorías y la unidad UND, sigue el paso «Cargar el catálogo de
          demostración» de docs\INICIO-LOCAL.txt después de verificar que la base configurada sea local.
        </p>
      )}
      {actionError && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          {actionError}
        </p>
      )}

      {showForm && (
        <ProductForm
          key={formProduct?.id ?? 'new'}
          product={formProduct}
          categories={categories}
          units={units}
          saving={saving}
          error={formError}
          onSubmit={saveProduct}
          onCancel={() => setShowForm(false)}
        />
      )}

      <section className="flex flex-col gap-4 rounded-3xl bg-surface p-4">
        <div className="flex flex-col gap-3 xl:flex-row">
          <form className="flex min-w-0 flex-1 gap-2" onSubmit={applySearch}>
            <label className="sr-only" htmlFor="product-search">Buscar producto</label>
            <input
              id="product-search"
              className="min-h-11 min-w-0 flex-1 rounded-full border border-separator bg-background px-4 text-sm outline-none focus-visible:ring-2 focus-visible:ring-focus"
              type="search"
              placeholder="Buscar producto"
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
            />
            <button
              className="min-h-11 rounded-full bg-surface-secondary px-4 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
              type="submit"
            >
              Buscar
            </button>
          </form>

          <div className="flex flex-wrap gap-2">
            <label className="sr-only" htmlFor="product-category">Filtrar por categoría</label>
            <select
              id="product-category"
              className="min-h-11 rounded-full border border-separator bg-background px-4 text-sm outline-none focus-visible:ring-2 focus-visible:ring-focus"
              value={categoryId}
              onChange={(event) => setCategoryId(event.target.value)}
              disabled={supportLoading}
            >
              <option value="">Todas las categorías</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>{category.name}</option>
              ))}
            </select>

            <button
              type="button"
              className="min-h-11 rounded-full bg-accent px-5 text-sm font-semibold text-background hover:opacity-90 focus-visible:outline-2 focus-visible:outline-focus disabled:opacity-60"
              onClick={openCreateForm}
              disabled={supportLoading || categories.length === 0 || units.length === 0}
            >
              + Nuevo producto
            </button>
          </div>
        </div>

        <div className="flex flex-wrap gap-1 rounded-full bg-surface-secondary p-1" role="group" aria-label="Filtrar productos por estado">
          {([
            ['all', 'Todos'],
            ['active', 'Activos'],
            ['inactive', 'Inactivos'],
          ] as const).map(([value, label]) => (
            <button
              key={value}
              type="button"
              aria-pressed={status === value}
              className={`min-h-9 rounded-full px-4 text-sm transition-colors duration-150 ease-out focus-visible:outline-2 focus-visible:outline-focus ${
                status === value ? 'bg-surface font-semibold text-foreground' : 'text-muted hover:text-foreground'
              }`}
              onClick={() => setStatus(value)}
            >
              {label}
            </button>
          ))}
        </div>
      </section>

      {error && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          No se pudieron cargar los productos: {error}
        </p>
      )}
      {loading ? (
        <div className="rounded-3xl bg-surface px-5 py-12 text-center text-sm text-muted" role="status">
          Cargando productos…
        </div>
      ) : error ? null : (
        <ProductTable products={products} onEdit={openEditForm} onToggleStatus={toggleStatus} />
      )}
    </div>
  )
}
