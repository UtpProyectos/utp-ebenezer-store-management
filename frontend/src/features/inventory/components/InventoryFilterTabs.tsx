import type { ProductStockResponse } from '../types/inventory.types'
import { matchesFilter, type InventoryFilter } from '../utils/inventoryFilter'

const FILTERS: { id: InventoryFilter; label: string }[] = [
  { id: 'all', label: 'Todos' },
  { id: 'restock', label: 'Por agotarse' },
  { id: 'expiring', label: 'Vencen pronto' },
]

interface InventoryFilterTabsProps {
  items: ProductStockResponse[]
  value: InventoryFilter
  onChange: (filter: InventoryFilter) => void
}

export function InventoryFilterTabs({ items, value, onChange }: InventoryFilterTabsProps) {
  return (
    <div role="tablist" aria-label="Filtrar inventario" className="flex flex-wrap gap-2">
      {FILTERS.map(({ id, label }) => {
        const selected = id === value
        const count = items.filter((item) => matchesFilter(item, id)).length
        return (
          <button
            key={id}
            type="button"
            role="tab"
            aria-selected={selected}
            onClick={() => onChange(id)}
            className={`flex h-12 items-center gap-2.5 rounded-full border px-4 font-semibold outline-none transition-[background-color,border-color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97] ${
              selected ? 'border-ai bg-ai text-white' : 'border-border bg-surface text-foreground hover:bg-surface-secondary'
            }`}
          >
            {label}
            <span
              className={`grid h-7 min-w-7 place-items-center rounded-full px-2 text-sm font-bold ${
                selected ? 'bg-surface text-ai' : 'bg-surface-tertiary text-foreground/80'
              }`}
            >
              {count}
            </span>
          </button>
        )
      })}
    </div>
  )
}
