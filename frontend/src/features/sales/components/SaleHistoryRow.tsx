import Pencil from '@gravity-ui/icons/Pencil'
import TrashBin from '@gravity-ui/icons/TrashBin'
import { formatMoney } from '@/shared/utils/money'
import type { SaleResponse } from '../types/sale.types'
import { PAYMENT_METHOD_LABELS, describeItems, formatTime } from '../utils/saleFormat'

interface SaleHistoryRowProps {
  sale: SaleResponse
  onEdit: () => void
  onCancel: () => void
}

const ACTION_BUTTON =
  'grid size-11 place-items-center rounded-full bg-surface outline-none transition-[background-color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus active:scale-95'

export function SaleHistoryRow({ sale, onEdit, onCancel }: SaleHistoryRowProps) {
  const cancelled = sale.status === 'CANCELLED'
  const payment = sale.paymentMethod ? PAYMENT_METHOD_LABELS[sale.paymentMethod] : 'Sin método'

  return (
    <li className={`flex flex-wrap items-center gap-4 rounded-3xl bg-surface-secondary py-3 pr-3 pl-5 ${cancelled ? 'opacity-60' : ''}`}>
      <span className="w-14.5 font-semibold text-muted tabular-nums">{formatTime(sale.saleDate)}</span>

      <div className="flex min-w-0 flex-[1_1_13.75rem] flex-col gap-0.5">
        <span className="flex min-w-0 items-center gap-2 text-[1.0625rem] font-semibold">
          <span className="truncate">
            #{sale.id} <span className="font-medium text-muted">· {payment}</span>
          </span>
          {sale.status === 'EDITED' && (
            <span className="shrink-0 rounded-full bg-warning-soft px-2.5 py-0.5 text-sm font-semibold text-warning-soft-foreground">
              Editada
            </span>
          )}
          {cancelled && (
            <span className="shrink-0 rounded-full bg-danger-soft px-2.5 py-0.5 text-sm font-semibold text-danger-soft-foreground">
              Eliminada
            </span>
          )}
        </span>
        <span className="truncate text-sm font-medium text-muted">
          {cancelled && sale.cancellationReason ? `Motivo: ${sale.cancellationReason}` : describeItems(sale)}
        </span>
      </div>

      <span className={`text-lg font-bold whitespace-nowrap tabular-nums ${cancelled ? 'line-through' : ''}`}>
        {formatMoney(sale.total)}
      </span>

      {!cancelled && (
        <div className="flex gap-1.5">
          <button type="button" aria-label={`Editar venta #${sale.id}`} title="Editar" onClick={onEdit} className={`${ACTION_BUTTON} text-foreground/80 hover:bg-surface-tertiary`}>
            <Pencil aria-hidden="true" className="size-4.5" />
          </button>
          <button type="button" aria-label={`Eliminar venta #${sale.id}`} title="Eliminar" onClick={onCancel} className={`${ACTION_BUTTON} text-danger hover:bg-danger-soft`}>
            <TrashBin aria-hidden="true" className="size-4.5" />
          </button>
        </div>
      )}
    </li>
  )
}
