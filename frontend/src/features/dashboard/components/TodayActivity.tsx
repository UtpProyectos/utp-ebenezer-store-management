import { useState } from 'react'
import ChevronDown from '@gravity-ui/icons/ChevronDown'
import { describeItems, formatTime, PAYMENT_METHOD_LABELS, type SaleResponse } from '@/features/sales'
import { formatMoney } from '@/shared/utils/money'

interface TodayActivityProps {
  sales: SaleResponse[]
}

// Collapsed by default, as in the prototype. Only sales are listed: they are today's registered activity.
export function TodayActivity({ sales }: TodayActivityProps) {
  const [open, setOpen] = useState(false)

  return (
    <section className="flex flex-col gap-2.5">
      <button
        type="button"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
        className="flex h-13 items-center gap-2.5 self-start rounded-full bg-surface py-0 pr-2 pl-5 font-semibold outline-none transition-colors duration-150 hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus"
      >
        {open ? 'Ocultar actividad de hoy' : 'Ver actividad de hoy'}
        <span className="grid size-9 place-items-center rounded-full bg-surface-tertiary">
          <ChevronDown aria-hidden="true" className={`size-5 transition-transform duration-200 ease-out ${open ? 'rotate-180' : ''}`} />
        </span>
      </button>

      {open && (
        <div className="flex flex-col gap-2 rounded-[2rem] bg-surface p-3">
          {sales.length === 0 ? (
            <p className="rounded-3xl bg-background px-4 py-6 text-center font-medium text-muted">Aún no hay ventas hoy.</p>
          ) : (
            <ul className="flex flex-col gap-2">
              {sales.map((sale) => {
                const cancelled = sale.status === 'CANCELLED'
                const payment = sale.paymentMethod ? PAYMENT_METHOD_LABELS[sale.paymentMethod] : 'Sin método'
                return (
                  <li
                    key={sale.id}
                    className="grid grid-cols-[4rem_minmax(0,1fr)_auto] items-center gap-3.5 rounded-3xl bg-background px-4.5 py-3"
                  >
                    <span className="font-semibold text-muted tabular-nums">{formatTime(sale.saleDate)}</span>
                    <div className="flex min-w-0 flex-col gap-0.5">
                      <span className="truncate leading-snug font-medium">
                        Venta #{sale.id}
                        {cancelled ? ' eliminada' : sale.status === 'EDITED' ? ' (editada)' : ''} · {describeItems(sale)}
                      </span>
                      <span className="text-sm font-medium text-muted">
                        {sale.userName} · {payment}
                      </span>
                    </div>
                    <span className={`font-semibold tabular-nums ${cancelled ? 'text-muted line-through' : ''}`}>
                      {formatMoney(sale.total)}
                    </span>
                  </li>
                )
              })}
            </ul>
          )}
        </div>
      )}
    </section>
  )
}
