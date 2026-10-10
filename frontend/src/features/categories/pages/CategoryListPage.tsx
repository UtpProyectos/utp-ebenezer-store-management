import { useEffect, useState } from 'react'
import { CategoryCard } from '../components/CategoryCard'
import { CategoryForm } from '../components/CategoryForm'
import { categoryApi } from '../services/categoryApi'
import type { Category, CategoryInput } from '../types/category.types'

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

export function CategoryListPage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [editingCategory, setEditingCategory] = useState<Category | undefined>()
  const [formOpen, setFormOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [busyCategoryId, setBusyCategoryId] = useState<number | null>(null)

  async function reload() {
    setLoading(true)
    setLoadError(null)
    try {
      setCategories(await categoryApi.getAll())
    } catch (error: unknown) {
      setLoadError(messageFromError(error))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    categoryApi.getAll()
      .then((result) => {
        if (!cancelled) setCategories(result)
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
  }, [])

  function openCreateForm() {
    setActionError(null)
    setEditingCategory(undefined)
    setFormOpen(true)
  }

  function openEditForm(category: Category) {
    setActionError(null)
    setEditingCategory(category)
    setFormOpen(true)
  }

  async function saveCategory(input: CategoryInput) {
    setSaving(true)
    setActionError(null)
    try {
      if (editingCategory) {
        await categoryApi.update(editingCategory.id, input)
      } else {
        await categoryApi.create(input)
      }
      setFormOpen(false)
      setEditingCategory(undefined)
      await reload()
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setSaving(false)
    }
  }

  async function toggleStatus(category: Category) {
    setBusyCategoryId(category.id)
    setActionError(null)
    try {
      const updated = await categoryApi.updateStatus(category.id, !category.active)
      setCategories((previous) => previous.map((item) => (item.id === updated.id ? updated : item)))
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setBusyCategoryId(null)
    }
  }

  const activeCount = categories.filter((category) => category.active).length
  const productCount = categories.reduce((total, category) => total + category.productCount, 0)

  return (
    <div className="mx-auto flex w-full max-w-350 flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-muted">
          {activeCount} {activeCount === 1 ? 'categoría activa' : 'categorías activas'} · {productCount}{' '}
          {productCount === 1 ? 'producto' : 'productos'}
        </p>
        <button
          type="button"
          className="min-h-11 rounded-full bg-accent px-5 text-sm font-semibold text-background hover:opacity-90 focus-visible:outline-2 focus-visible:outline-focus"
          onClick={openCreateForm}
        >
          + Nueva categoría
        </button>
      </div>

      {actionError && !formOpen && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          {actionError}
        </p>
      )}

      {formOpen && (
        <CategoryForm
          key={editingCategory?.id ?? 'new'}
          category={editingCategory}
          saving={saving}
          error={actionError}
          onSubmit={saveCategory}
          onCancel={() => setFormOpen(false)}
        />
      )}

      {loadError ? (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          No se pudieron cargar las categorías: {loadError}
        </p>
      ) : loading ? (
        <p role="status" className="rounded-3xl bg-surface px-5 py-12 text-center text-sm text-muted">
          Cargando categorías…
        </p>
      ) : categories.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-separator bg-surface px-5 py-12 text-center">
          <p className="font-semibold">Todavía no hay categorías</p>
          <p className="mt-1 text-sm text-muted">Crea una categoría para poder organizar tus productos.</p>
        </div>
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
          {categories.map((category) => (
            <CategoryCard
              key={category.id}
              category={category}
              busy={busyCategoryId === category.id}
              onEdit={openEditForm}
              onToggleStatus={toggleStatus}
            />
          ))}
        </div>
      )}
    </div>
  )
}
