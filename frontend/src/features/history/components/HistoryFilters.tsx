import type { ComponentType, SVGProps } from 'react'
import ArrowUpArrowDown from '@gravity-ui/icons/ArrowUpArrowDown'
import Cup from '@gravity-ui/icons/Cup'
import Receipt from '@gravity-ui/icons/Receipt'
import { TruckIcon } from '@/features/inventory'
import type { HistoryPeriod, HistoryTab } from '../types/history.types'

type Icon = ComponentType<SVGProps<SVGSVGElement>>

const HISTORY_TABS: { id: HistoryTab; label: string; icon: Icon }[] = [
  { id: 'sales', label: 'Ventas', icon: Receipt },
  { id: 'purchases', label: 'Ingresos', icon: TruckIcon },
  { id: 'movements', label: 'Movimientos', icon: ArrowUpArrowDown },
  { id: 'consumption', label: 'Consumo interno', icon: Cup },
]

const PERIODS: { id: HistoryPeriod; label: string }[] = [
  { id: 'today', label: 'Hoy' },
  { id: 'week', label: 'Esta semana' },
  { id: 'month', label: 'Este mes' },
]

interface HistoryFiltersProps {
  tab: HistoryTab
  period: HistoryPeriod
  onTabChange: (tab: HistoryTab) => void
  onPeriodChange: (period: HistoryPeriod) => void
}

/** Tabs (what to see) and period pills (when), as in the prototype. */
export function HistoryFilters({ tab, period, onTabChange, onPeriodChange }: HistoryFiltersProps) {
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 print:hidden">
      <div role="tablist" aria-label="Tipo de historial" className="flex flex-wrap gap-2">
        {HISTORY_TABS.map(({ id, label, icon: Icon }) => {
          const selected = id === tab
          return (
            <button
              key={id}
              type="button"
              role="tab"
              aria-selected={selected}
              onClick={() => onTabChange(id)}
              className={`flex h-12 items-center gap-2 rounded-full px-4.5 font-semibold outline-none transition-[background-color,color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97] ${
                selected ? 'bg-accent text-accent-foreground' : 'bg-surface text-foreground hover:bg-surface-secondary'
              }`}
            >
              <Icon aria-hidden="true" className="size-5" />
              {label}
            </button>
          )
        })}
      </div>

      <div role="group" aria-label="Período" className="flex gap-1 rounded-full bg-surface p-1">
        {PERIODS.map(({ id, label }) => (
          <button
            key={id}
            type="button"
            aria-pressed={id === period}
            onClick={() => onPeriodChange(id)}
            className={`h-10 rounded-full px-4 text-[0.9375rem] font-semibold outline-none transition-colors duration-150 focus-visible:ring-2 focus-visible:ring-focus ${
              id === period ? 'bg-foreground text-background' : 'text-foreground/80 hover:text-foreground'
            }`}
          >
            {label}
          </button>
        ))}
      </div>
    </div>
  )
}
