import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { formatMoney } from '@/shared/utils/money'

interface DaySummaryProps {
  salesTotal: number
  salesCount: number
  restockCount: number
  expiringCount: number
}

export function DaySummary({ salesTotal, salesCount, restockCount, expiringCount }: DaySummaryProps) {
  const navigate = useNavigate()

  const stats = [
    { label: 'Ventas de hoy', value: formatMoney(salesTotal), path: ROUTES.salesHistory, main: true },
    { label: 'Cantidad de ventas', value: String(salesCount), path: ROUTES.salesHistory },
    { label: 'Productos por reponer', value: String(restockCount), path: `${ROUTES.inventory}?filter=restock` },
    { label: 'Productos por vencer', value: String(expiringCount), path: `${ROUTES.inventory}?filter=expiring` },
  ]

  return (
    <section className="flex flex-col gap-1.5 rounded-[2rem] bg-surface p-3">
      <h2 className="px-2.5 pt-2 pb-1.5 text-xl font-bold">Resumen del día</h2>
      <div className="grid grid-cols-[repeat(auto-fit,minmax(12.5rem,1fr))] gap-2.5">
        {stats.map(({ label, value, path, main }) => (
          <button
            key={label}
            type="button"
            onClick={() => navigate(path)}
            className={`flex flex-col gap-3.5 rounded-3xl px-6 py-5.5 text-left outline-none transition-transform duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 active:scale-[0.98] ${
              main ? 'bg-foreground text-background' : 'bg-background text-foreground'
            }`}
          >
            <span className={`font-semibold ${main ? 'text-background/85' : 'text-muted'}`}>{label}</span>
            <span className="text-4xl leading-none font-bold tracking-tight tabular-nums">{value}</span>
          </button>
        ))}
      </div>
    </section>
  )
}
