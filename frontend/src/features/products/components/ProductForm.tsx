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
  const [barcode, setBarcode] = useState(product?.barcode ?? '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [salePrice, setSalePrice] = useState(String(product?.salePrice ?? ''))
  const [minStock, setMinStock] = useState(String(product?.minStock ?? 0))

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await onSubmit({
      name: name.trim(),
      categoryId: Number(categoryId),
      baseUnitId: Number(baseUnitId),
      barcode: barcode.trim() || null,
      description: description.trim() || null,
      salePrice: Number(salePrice),
      minStock: Number(minStock),
    })
  }

  return (
    <form className="flex flex-col gap-4 rounded-3xl bg-surface p-5" onSubmit={handleSubmit}>
      <div>
        <h2 className="text-lg font-bold">{product ? 'Editar producto' : 'Nuevo producto'}</h2>
        <p className="mt-1 text-sm text-muted">
          El stock se controla mediante movimientos de inventario y no se modifica desde este formulario.
        </p>
      </div>

      {error && (
        <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          {error}
        </p>
      )}

      <div className="grid gap-4 sm:grid-cols-2">
        <label className="flex flex-col gap-1.5 text-sm font-medium">
          Nombre
          <input className={fieldClassName} value={name} onChange={(event) => setName(event.target.value)} required maxLength={150} />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium">
          Categoría
          <select
            className={fieldClassName}
            value={categoryId}
            onChange={(event) => setCategoryId(event.target.value)}
            required
          >
            <option value="" disabled>
              Selecciona una categoría
            </option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium">
          Precio de venta (S/)
          <input
            className={fieldClassName}
            type="number"
            min="0"
            step="0.01"
            value={salePrice}
            onChange={(event) => setSalePrice(event.target.value)}
            required
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium">
          Unidad de control
          <select
            className={fieldClassName}
            value={baseUnitId}
            onChange={(event) => setBaseUnitId(event.target.value)}
            required
          >
            <option value="" disabled>
              Selecciona una unidad
            </option>
            {units.map((unit) => (
              <option key={unit.id} value={unit.id}>
                {unit.name} ({unit.abbreviation})
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium">
          Código de barras (opcional)
          <input
            className={fieldClassName}
            value={barcode}
            onChange={(event) => setBarcode(event.target.value)}
            maxLength={100}
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm font-medium">
          Mínimo de stock para alertas
          <input
            className={fieldClassName}
            type="number"
            min="0"
            step="0.001"
            value={minStock}
            onChange={(event) => setMinStock(event.target.value)}
            required
          />
        </label>
      </div>

      <label className="flex flex-col gap-1.5 text-sm font-medium">
        Descripción (opcional)
        <textarea
          className={`${fieldClassName} min-h-20 py-3`}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          maxLength={300}
        />
      </label>

      <div className="flex flex-wrap justify-end gap-2">
        <button
          type="button"
          className="min-h-11 rounded-full px-5 text-sm font-semibold text-muted hover:bg-surface-secondary focus-visible:outline-2 focus-visible:outline-focus"
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
      </div>
    </form>
  )
}
