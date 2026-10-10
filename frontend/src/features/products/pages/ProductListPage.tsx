import { useState, type FormEvent } from 'react'
import { Button, ListBox, Select } from '@heroui/react'
import Plus from '@gravity-ui/icons/Plus'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { ProductForm } from '../components/ProductForm'
import { ProductTable } from '../components/ProductTable'
import { productApi } from '../services/productApi'
import { useProductOptions } from '../hooks/useProductOptions'
import { useProducts } from '../hooks/useProducts'
import type { Product, ProductInput } from '../types/product.types'

type StatusFilter = 'all' | 'active' | 'inactive'

const ALL_CATEGORIES = 'all'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

export function ProductListPage() {
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
  const { products, loading, error, reload, applyUpdate } = useProducts(filters)
  const {
    categories,
    units,
    loading: supportLoading,
    error: supportError,
    createCategory,
  } = useProductOptions()

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
      applyUpdate(await productApi.updateStatus(product.id, !product.active))
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
      {!supportLoading && !supportError && units.length === 0 && (
        <p role="status" className="rounded-2xl bg-warning-soft px-4 py-3 text-sm text-warning-soft-foreground">
          No hay unidades de medida registradas. Reinicia el backend para que se creen las unidades base.
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
          onCreateCategory={createCategory}
          onCancel={() => setShowForm(false)}
        />
      )}

      <section className="flex flex-col gap-4 rounded-3xl bg-surface p-4">
        <div className="flex flex-col gap-3 xl:flex-row">
          <form className="flex min-w-0 flex-1 gap-2" onSubmit={applySearch}>
            <SearchInput label="Buscar producto" value={searchText} onChange={setSearchText} className="min-w-0 flex-1" />
            <Button type="submit" variant="secondary" className="h-13 px-5">
              Buscar
            </Button>
          </form>

          <div className="flex flex-wrap gap-2">
            <Select
              aria-label="Filtrar por categoría"
              className="w-56"
              value={categoryId || ALL_CATEGORIES}
              onChange={(key) => setCategoryId(key === null || key === ALL_CATEGORIES ? '' : String(key))}
              isDisabled={supportLoading}
            >
              <Select.Trigger className="h-13 px-4">
                <Select.Value />
                <Select.Indicator />
              </Select.Trigger>
              <Select.Popover>
                <ListBox>
                  <ListBox.Item id={ALL_CATEGORIES} textValue="Todas las categorías">
                    Todas las categorías
                    <ListBox.ItemIndicator />
                  </ListBox.Item>
                  {categories.map((category) => (
                    <ListBox.Item key={category.id} id={String(category.id)} textValue={category.name}>
                      {category.name}
                      <ListBox.ItemIndicator />
                    </ListBox.Item>
                  ))}
                </ListBox>
              </Select.Popover>
            </Select>

            <Button className="h-13 px-5" onPress={openCreateForm} isDisabled={supportLoading || units.length === 0}>
              <Plus aria-hidden="true" className="size-4" />
              Nuevo producto
            </Button>
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
