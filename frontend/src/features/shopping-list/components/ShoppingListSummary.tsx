import ArrowRight from '@gravity-ui/icons/ArrowRight'
import Printer from '@gravity-ui/icons/Printer'
import { formatMoney } from '@/shared/utils/money'

interface ShoppingListSummaryProps {
  /** Cost of the items not bought yet. */
  pendingCost: number
  boughtCount: number
  totalCount: number
  onRegisterEntry: () => void
}

export function ShoppingListSummary({ pendingCost, boughtCount, totalCount, onRegisterEntry }: ShoppingListSummaryProps) {
  const percent = totalCount ? Math.round((boughtCount / totalCount) * 100) : 0

  return (
    <section className="flex flex-wrap items-center gap-6 rounded-[2rem] bg-surface py-4 pr-4 pl-7 print:hidden">
      <div className="flex flex-col gap-1">
        <span className="text-[0.9375rem] font-medium text-muted">Costo estimado</span>
        <span className="text-[2rem] leading-none font-bold tracking-tight tabular-nums">{formatMoney(pendingCost)}</span>
      </div>

      <div className="flex max-w-70 flex-[1_1_12.5rem] flex-col gap-2">
        <span className="text-[0.9375rem] font-medium text-muted">
          {boughtCount} de {totalCount} comprados
        </span>
        <div
          role="progressbar"
          aria-label="Productos comprados"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={percent}
          className="h-2 overflow-hidden rounded-full bg-surface-secondary"
        >
          <div className="h-full rounded-full bg-accent transition-[width] duration-300 ease-out" style={{ width: `${percent}%` }} />
        </div>
      </div>

      <div className="ml-auto flex flex-wrap gap-2">
        <button
          type="button"
          aria-label="Imprimir lista"
          title="Imprimir lista"
          onClick={() => window.print()}
          className="grid size-13 place-items-center rounded-full bg-surface-secondary outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-tertiary focus-visible:ring-2 focus-visible:ring-focus active:scale-95"
        >
          <Printer aria-hidden="true" className="size-5.5" />
        </button>
        <button
          type="button"
          onClick={onRegisterEntry}
          className="flex h-13 items-center gap-2.5 rounded-full bg-foreground pr-1.5 pl-5 text-base font-semibold text-background outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-foreground/85 focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97]"
        >
          Registrar ingreso
          <span className="grid size-10 place-items-center rounded-full bg-accent text-accent-foreground">
            <ArrowRight aria-hidden="true" className="size-5" />
          </span>
        </button>
      </div>
    </section>
  )
}
