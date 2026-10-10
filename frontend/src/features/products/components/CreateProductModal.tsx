import { useState } from 'react'
import { useProductOptions } from '../hooks/useProductOptions'
import { productApi } from '../services/productApi'
import type { Product, ProductInput } from '../types/product.types'
import { ProductForm } from './ProductForm'

interface CreateProductModalProps {
  initialName?: string
  onClose: () => void
  onCreated: (product: Product) => void
}

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

/** Self-contained "new product" popup, usable from other features (inventory, purchases). */
export function CreateProductModal({ initialName, onClose, onCreated }: CreateProductModalProps) {
  const { categories, units, loading, error: optionsError, createCategory } = useProductOptions()
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function save(input: ProductInput) {
    setSaving(true)
    setError(null)
    try {
      onCreated(await productApi.create(input))
    } catch (saveError: unknown) {
      setError(messageFromError(saveError))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return null

  return (
    <ProductForm
      initialName={initialName}
      categories={categories}
      units={units}
      saving={saving}
      error={error ?? (optionsError && `No se pudieron cargar las categorías y unidades: ${optionsError}`)}
      onSubmit={save}
      onCreateCategory={createCategory}
      onCancel={onClose}
    />
  )
}
