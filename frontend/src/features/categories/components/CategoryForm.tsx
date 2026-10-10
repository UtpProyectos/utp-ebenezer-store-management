import { useState, type FormEvent } from 'react'
import type { Category, CategoryInput } from '../types/category.types'

interface CategoryFormProps {
  category?: Category
  saving: boolean
  error: string | null
  onSubmit: (input: CategoryInput) => Promise<void>
  onCancel: () => void
}

const fieldClassName =
  'min-h-11 w-full rounded-full border border-separator bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-focus'

export function CategoryForm({ category, saving, error, onSubmit, onCancel }: CategoryFormProps) {
  const [name, setName] = useState(category?.name ?? '')
  const [description, setDescription] = useState(category?.description ?? '')

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    return onSubmit({ name: name.trim(), description: description.trim() || null })
  }

  return (
    <div
      className="fixed inset-0 z-50 grid place-items-center bg-black/40 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !saving) onCancel()
      }}
      onKeyDown={(event) => {
        if (event.key === 'Escape' && !saving) onCancel()
      }}
    >
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="category-form-title"
        className="w-full max-w-md rounded-3xl bg-surface p-5 shadow-xl"
      >
        <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
          <h2 id="category-form-title" className="text-lg font-bold">
            {category ? 'Editar categoría' : 'Nueva categoría'}
          </h2>

          {error && (
            <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
              {error}
            </p>
          )}

          <label className="flex flex-col gap-1.5 text-sm font-medium">
            Nombre
            <input
              className={fieldClassName}
              autoFocus
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="Ej. Congelados"
              required
              maxLength={100}
            />
          </label>

          <label className="flex flex-col gap-1.5 text-sm font-medium">
            Descripción
            <input
              className={fieldClassName}
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              placeholder="Opcional"
              maxLength={200}
            />
          </label>

          <div className="flex justify-end gap-2 border-t border-separator pt-4">
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
              disabled={saving || !name.trim()}
            >
              {saving ? 'Guardando…' : 'Guardar'}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}
