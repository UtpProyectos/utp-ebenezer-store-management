import ArrowDown from '@gravity-ui/icons/ArrowDown'
import Ban from '@gravity-ui/icons/Ban'
import ChevronRight from '@gravity-ui/icons/ChevronRight'
import Clock from '@gravity-ui/icons/Clock'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { daysUntil, formatStock, type ProductStockResponse } from '@/features/inventory'

interface AttentionListProps {
  items: ProductStockResponse[]
}

function describe(item: ProductStockResponse) {
  if (item.status === 'EXPIRED') {
    return { message: 'Vencido', icon: Ban, tone: 'bg-danger-soft text-danger', urgent: true }
  }
  if (item.status === 'EXPIRING_SOON' && item.nextExpirationDate) {
    const days = daysUntil(item.nextExpirationDate)
    const message = days <= 0 ? 'Vence hoy' : `Vence en ${days} ${days === 1 ? 'día' : 'días'}`
    return { message, icon: Clock, tone: 'bg-info-soft text-info-soft-foreground', urgent: false }
  }
  const critical = item.status === 'CRITICAL'
  return {
    message: `Quedan ${formatStock(item)}`,
    icon: ArrowDown,
    tone: critical ? 'bg-danger-soft text-danger' : 'bg-warning-soft text-warning-soft-foreground',
    urgent: critical,
  }
}

export function AttentionList({ items }: AttentionListProps) {
  const navigate = useNavigate()

  return (
    <section className="flex flex-col gap-2 rounded-[2rem] bg-surface p-3">
      <div className="flex flex-wrap items-center justify-between gap-3 px-2.5 pt-2 pb-1.5">
        <h2 className="text-xl font-bold">Lo que necesita atención</h2>
        <button
          type="button"
          onClick={() => navigate(ROUTES.inventory)}
          className="flex h-10 items-center gap-1 rounded-full bg-surface-tertiary px-3.5 font-semibold outline-none transition-colors duration-150 hover:bg-border focus-visible:ring-2 focus-visible:ring-focus"
        >
          Ver todo
          <ChevronRight aria-hidden="true" className="size-4" />
        </button>
      </div>

      {items.length === 0 ? (
        <p className="rounded-3xl bg-background px-4 py-7 text-center font-medium text-muted">
          Todo en orden: no hay productos por reponer ni por vencer.
        </p>
      ) : (
        <ul className="flex flex-col gap-2">
          {items.map((item) => {
            const { message, icon: Icon, tone, urgent } = describe(item)
            return (
              <li key={item.productId} className="flex flex-wrap items-center gap-3.5 rounded-3xl bg-background py-3 pr-3 pl-3.5">
                <span className={`grid size-11 shrink-0 place-items-center rounded-full ${tone}`}>
                  <Icon aria-hidden="true" className="size-5" />
                </span>
                <div className="flex min-w-0 flex-[1_1_12.5rem] flex-col gap-0.5">
                  <span className="text-[1.0625rem] leading-snug font-semibold">{item.productName}</span>
                  <span className={`font-medium ${urgent ? 'text-danger' : 'text-foreground/80'}`}>{message}</span>
                </div>
                <button
                  type="button"
                  aria-label={`Ver ${item.productName} en inventario`}
                  onClick={() => navigate(`${ROUTES.inventory}?q=${encodeURIComponent(item.productName)}`)}
                  className="grid size-11 shrink-0 place-items-center rounded-full bg-surface outline-none transition-[background-color,transform] duration-150 hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus active:scale-95"
                >
                  <ChevronRight aria-hidden="true" className="size-5" />
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
