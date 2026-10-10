import { useState, type FormEvent } from 'react'
import type { Product } from '../types/product.types'
import type { CategoryOption, ProductInput, UnitOption } from '../types/product.types'

interface ProductFormProps {
  product?: Product
  categories: CategoryOption[]
  units: UnitOption[]
  saving: boolean
  error: string | null
  onSubmit: (input: ProductInput) => Promise<void>
  onCancel: () => void
}

const fieldClassName =
  'min-h-11 w-full rounded-xl border border-separator bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-focus'

export function ProductForm({ product, categories, units, saving, error, onSubmit, onCancel }: ProductFormProps) {
  const defaultUnit = units.find((unit) => unit.abbreviation === 'UND') ?? units[0]
  const [name, setName] = useState(product?.name ?? '')
  const [categoryId, setCategoryId] = useState(String(product?.categoryId ?? categories[0]?.id ?? ''))
  const [baseUnitId, setBaseUnitId] = useState(String(product?.baseUnitId ?? defaultUnit?.id ?? ''))
  const [description, setDescription] = useState(product?.description ?? '')
  const [salePrice, setSalePrice] = useState(String(product?.salePrice ?? ''))
  const [minStock, setMinStock] = useState(String(product?.minStock ?? 0))

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await onSubmit({
      name: name.trim(),
      categoryId: Number(categoryId),
      baseUnitId: Number(baseUnitId),
      barcode: product?.barcode ?? null,
      description: description.trim() || null,
      salePrice: Number(salePrice),
      minStock: Number(minStock),
    })
  }

  return (
    <div
      className="fixed inset-0 z-50 bg-black/40"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !saving) onCancel()
      }}
      onKeyDown={(event) => {
        if (event.key === 'Escape' && !saving) onCancel()
      }}
    >
      <form
        role="dialog"
        aria-modal="true"
        aria-labelledby="product-form-title"
        className="ml-auto flex h-full w-full max-w-md flex-col border-l border-separator bg-surface shadow-xl"
        onSubmit={handleSubmit}
      >
        <header className="flex items-start justify-between border-b border-separator px-5 py-4">
          <div>
            <p className="text-xs text-muted">Catálogo</p>
            <h2 id="product-form-title" className="text-lg font-bold">
              {product ? 'Editar producto' : 'Nuevo producto'}
            </h2>
          </div>
          <button
            type="button"
            className="rounded-full p-2 text-muted hover:bg-surface-secondary hover:text-foreground focus-visible:outline-2 focus-visible:outline-focus"
            aria-label="Cerrar formulario"
            onClick={onCancel}
            disabled={saving}
          >
            <svg aria-hidden="true" viewBox="0 0 20 20" className="h-5 w-5 fill-none stroke-current" strokeWidth="1.7">
              <path strokeLinecap="round" d="m5 5 10 10M15 5 5 15" />
            </svg>
          </button>
        </header>

        <div className="flex-1 space-y-5 overflow-y-auto px-5 py-5">
          <label className="flex flex-col gap-1.5 text-sm font-medium">
            <span>Nombre <span className="text-danger">*</span></span>
            <input
              className={fieldClassName}
              autoFocus
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Ej. Galletas de vainilla 6 unid."
              required
              maxLength={150}
            />
          </label>

          <div className="grid gap-4 sm:grid-cols-2">
            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Categoría
              <select
                className={fieldClassName}
                value={categoryId}
                onChange={(event) => setCategoryId(event.target.value)}
                required
              >
                <option value="" disabled>Selecciona una categoría</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>{category.name}</option>
                ))}
              </select>
            </label>
            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Presentación
              <select
                className={fieldClassName}
                value={baseUnitId}
                onChange={(event) => setBaseUnitId(event.target.value)}
                required
              >
                <option value="" disabled>Selecciona una presentación</option>
                {units.map((unit) => (
                  <option key={unit.id} value={unit.id}>{unit.name} ({unit.abbreviation})</option>
                ))}
              </select>
            </label>
          </div>

          <div className="rounded-2xl bg-surface-secondary p-3">
            <p className="text-sm font-semibold">Control de inventario</p>
            <p className="mt-1 text-xs leading-relaxed text-muted">
              El stock se mide en la presentación elegida y se actualiza desde los movimientos de inventario.
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Stock mínimo
              <input
                className={fieldClassName}
                type="number"
                min="0"
                step="0.001"
                value={minStock}
                onChange={(event) => setMinStock(event.target.value)}
                required
              />
              <span className="text-xs font-normal text-muted">Recibirás una alerta al llegar a este nivel.</span>
            </label>
            <label className="flex flex-col gap-1.5 text-sm font-medium">
              <span>Precio de venta (S/) <span className="text-danger">*</span></span>
              <input
                className={fieldClassName}
                type="number"
                min="0"
                step="0.01"
                value={salePrice}
                onChange={(event) => setSalePrice(event.target.value)}
                placeholder="0.00"
                required
              />
            </label>
          </div>

          <label className="flex flex-col gap-1.5 text-sm font-medium">
            Descripción <span className="font-normal text-muted">(opcional)</span>
            <textarea
              className={`${fieldClassName} min-h-24 resize-y rounded-2xl py-3`}
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Agrega detalles que ayuden a identificar el producto"
              maxLength={300}
            />
          </label>

          {error && (
            <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
              {error}
            </p>
          )}
        </div>

        <footer className="flex justify-end gap-2 border-t border-separator bg-surface px-5 py-4">
          <button
            type="button"
            className="min-h-11 rounded-full border border-separator px-5 text-sm font-semibold hover:bg-surface-secondary focus-visible:outline-2 focus-visible:outline-focus disabled:opacity-60"
            onClick={onCancel}
            disabled={saving}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="min-h-11 rounded-full bg-accent px-5 text-sm font-semibold text-background hover:opacity-90 focus-visible:outline-2 focus-visible:outline-focus disabled:opacity-60"
            disabled={saving || categories.length === 0 || units.length === 0}
          >
            {saving ? 'Guardando…' : product ? 'Guardar cambios' : 'Crear producto'}
          </button>
        </footer>
      </form>
    </div>
  )
}
