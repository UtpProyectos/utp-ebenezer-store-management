import type { Category } from '../types/category.types'

interface CategoryCardProps {
  category: Category
  busy: boolean
  onEdit: (category: Category) => void
  onToggleStatus: (category: Category) => void
}

export function CategoryCard({ category, busy, onEdit, onToggleStatus }: CategoryCardProps) {
  const productLabel = category.productCount === 1 ? 'producto' : 'productos'

  return (
    <article
      className={`flex min-h-33 flex-col justify-between rounded-3xl border border-separator bg-surface p-4 ${
        category.active ? '' : 'text-muted'
      }`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h2 className={`font-bold ${category.active ? 'text-foreground' : 'text-muted'}`}>{category.name}</h2>
          <p className="mt-1 text-sm text-muted">{category.description || 'Sin descripción'}</p>
        </div>
        <button
          type="button"
          className="shrink-0 rounded-full p-2 text-muted hover:bg-surface-secondary hover:text-foreground focus-visible:outline-2 focus-visible:outline-focus"
          aria-label={`Editar categoría ${category.name}`}
          onClick={() => onEdit(category)}
        >
          <svg aria-hidden="true" viewBox="0 0 20 20" className="h-4 w-4 fill-none stroke-current" strokeWidth="1.7">
            <path strokeLinecap="round" strokeLinejoin="round" d="m13.8 3.2 3 3M3 17l3.8-.8L17.2 5.8a2.1 2.1 0 0 0-3-3L3.8 13.2 3 17Z" />
          </svg>
        </button>
      </div>

      <div className="mt-3 flex items-center justify-between gap-2 border-t border-separator pt-3 text-sm">
        <span>{category.productCount} {productLabel}</span>
        <button
          type="button"
          role="switch"
          aria-checked={category.active}
          aria-label={`${category.active ? 'Desactivar' : 'Activar'} categoría ${category.name}`}
          className={`inline-flex min-h-8 items-center gap-2 rounded-full px-2 focus-visible:outline-2 focus-visible:outline-focus disabled:opacity-60 ${
            category.active ? 'text-success' : 'text-muted'
          }`}
          disabled={busy}
          onClick={() => onToggleStatus(category)}
        >
          <span>{busy ? 'Guardando…' : category.active ? 'Activa' : 'Inactiva'}</span>
          <span
            aria-hidden="true"
            className={`relative h-6 w-10 shrink-0 rounded-full transition-colors duration-150 ease-out ${
              category.active ? 'bg-accent' : 'bg-default'
            }`}
          >
            <span
              className={`absolute top-1 h-4 w-4 rounded-full bg-background transition-[left] duration-150 ease-out ${
                category.active ? 'left-5' : 'left-1'
              }`}
            />
          </span>
        </button>
      </div>
    </article>
  )
}
