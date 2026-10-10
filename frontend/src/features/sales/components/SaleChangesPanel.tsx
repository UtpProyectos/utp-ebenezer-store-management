import ChevronRight from '@gravity-ui/icons/ChevronRight'
import ClockArrowRotateLeft from '@gravity-ui/icons/ClockArrowRotateLeft'
import Pencil from '@gravity-ui/icons/Pencil'
import TrashBin from '@gravity-ui/icons/TrashBin'
import type { SaleHistoryResponse } from '../types/sale.types'
import { formatTime } from '../utils/saleFormat'

const MAX_VISIBLE = 6

interface SaleChangesPanelProps {
  changes: SaleHistoryResponse[]
  /** Only for users that can open the store history (admins). */
  onShowMovements?: () => void
}

function describe(change: SaleHistoryResponse) {
  if (change.action === 'CANCELLED') {
    return { title: `Venta #${change.saleId} eliminada`, icon: TrashBin, tone: 'bg-danger-soft text-danger-soft-foreground' }
  }
  return { title: `Venta #${change.saleId} editada`, icon: Pencil, tone: 'bg-warning-soft text-warning-soft-foreground' }
}

export function SaleChangesPanel({ changes, onShowMovements }: SaleChangesPanelProps) {
  return (
    <aside className="flex flex-col gap-2 rounded-[2rem] bg-surface p-3 lg:sticky lg:top-26">
      <div className="flex items-center gap-3 px-2 pt-2 pb-1.5">
        <span className="grid size-11 shrink-0 place-items-center rounded-full bg-foreground text-background">
          <ClockArrowRotateLeft aria-hidden="true" className="size-5" />
        </span>
        <span className="text-lg font-bold">Cambios</span>
      </div>

      {changes.length === 0 ? (
        <p className="rounded-3xl bg-background px-4 py-6 text-center font-medium text-muted">Sin cambios hoy</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {changes.slice(0, MAX_VISIBLE).map((change) => {
            const { title, icon: Icon, tone } = describe(change)
            return (
              <li key={change.id} className="flex gap-3 rounded-3xl bg-background px-3.5 py-3">
                <span className={`grid size-9 shrink-0 place-items-center rounded-full ${tone}`}>
                  <Icon aria-hidden="true" className="size-4" />
                </span>
                <div className="flex min-w-0 flex-col gap-0.5">
                  <span className="font-semibold">{title}</span>
                  {change.reason && <span className="text-sm leading-snug text-foreground/80">{change.reason}</span>}
                  <span className="text-xs font-medium text-muted">
                    {formatTime(change.createdAt)} · {change.userName}
                  </span>
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {onShowMovements && (
        <button
          type="button"
          onClick={onShowMovements}
          className="flex h-12 items-center justify-center gap-1.5 rounded-full bg-surface-tertiary font-semibold outline-none transition-colors duration-150 hover:bg-border focus-visible:ring-2 focus-visible:ring-focus"
        >
          Ver movimientos
          <ChevronRight aria-hidden="true" className="size-4" />
        </button>
      )}
    </aside>
  )
}
