import { Spinner } from '@heroui/react'
import Check from '@gravity-ui/icons/Check'
import TriangleExclamation from '@gravity-ui/icons/TriangleExclamation'
import { formatQuantity } from '@/features/inventory'
import { formatMoney } from '@/shared/utils/money'
import type { PurchaseEntry } from '../hooks/usePurchaseEntry'

interface PurchaseSummaryCardProps {
  entry: PurchaseEntry
  onSave: () => void
  onBack: () => void
}

// Native buttons: the prototype pill (black + orange check circle) does not match HeroUI's default button shape.
export function PurchaseSummaryCard({ entry, onSave, onBack }: PurchaseSummaryCardProps) {
  const { product } = entry
  const stockAfter = product ? `${formatQuantity(product.currentStock + entry.baseQuantity)} ${entry.unit}` : '—'

  return (
    <aside className="flex flex-col gap-3 rounded-3xl bg-surface p-3 lg:sticky lg:top-26">
      <div className="flex flex-col gap-3 rounded-[1.25rem] bg-background px-5 py-4.5 text-[1.0625rem]">
        <span className="text-lg font-bold">Resumen</span>
        <div className="flex justify-between gap-3">
          <span className="text-muted">Producto</span>
          <span className="text-right font-semibold">{product?.productName ?? '—'}</span>
        </div>
        <div className="flex justify-between gap-3">
          <span className="text-muted">Llegaron</span>
          <span className="text-right font-semibold">{entry.groupLabel}</span>
        </div>
        <div className="flex justify-between gap-3">
          <span className="text-muted">Costo total</span>
          <span className="font-semibold tabular-nums">{formatMoney(entry.totalCostValue)}</span>
        </div>
      </div>

      <div className="flex items-center justify-between gap-3 rounded-[1.25rem] bg-accent px-5 py-4.5 text-accent-foreground">
        <span className="font-semibold">Tendrás en total</span>
        <span className="text-[1.75rem] leading-none font-bold tabular-nums">{stockAfter}</span>
      </div>

      {entry.error && (
        <div role="alert" className="flex gap-2.5 rounded-[1.25rem] bg-danger-soft px-4 py-3 text-danger-soft-foreground">
          <TriangleExclamation aria-hidden="true" className="mt-0.5 size-5 shrink-0" />
          {entry.error}
        </div>
      )}

      <button
        type="button"
        onClick={onSave}
        disabled={entry.isPending}
        className="flex h-15 items-center justify-between gap-2.5 rounded-full bg-foreground pr-1.5 pl-6 text-lg font-bold text-background outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-foreground/85 focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 active:scale-[0.97] disabled:cursor-wait"
      >
        {entry.isPending ? 'Guardando…' : 'Guardar ingreso'}
        <span className="grid size-12 place-items-center rounded-full bg-accent text-accent-foreground">
          {entry.isPending ? (
            <Spinner size="sm" color="current" aria-label="Guardando" />
          ) : (
            <Check aria-hidden="true" className="size-6" />
          )}
        </span>
      </button>
      <button
        type="button"
        onClick={onBack}
        className="h-13 rounded-full bg-surface-tertiary font-semibold text-foreground outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-border focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97]"
      >
        Volver
      </button>
    </aside>
  )
}
