import ArrowUpRight from '@gravity-ui/icons/ArrowUpRight'
import { useDashboard } from '@/features/dashboard'
import { formatMoney } from '@/shared/utils/money'

interface AssistantSidebarProps {
  questions: string[]
  onAsk: (question: string) => void
  onClear: () => void
  disabled: boolean
}

/** "Tu bodega hoy" with the same numbers as Home, more questions and "Nueva conversación". */
export function AssistantSidebar({ questions, onAsk, onClear, disabled }: AssistantSidebarProps) {
  const { salesTotal, restockCount, expiringCount, initialLoading } = useDashboard()
  const stats = [
    { label: 'Ventas', value: formatMoney(salesTotal), tone: 'text-foreground' },
    { label: 'Por reponer', value: String(restockCount), tone: 'text-danger' },
    { label: 'Por vencer / vencidos', value: String(expiringCount), tone: 'text-accent' },
  ]

  return (
    <aside className="sticky top-24 flex flex-col gap-3.5">
      <section className="flex flex-col gap-3.5 rounded-[1.625rem] bg-surface p-4.5">
        <span className="text-sm font-semibold tracking-widest text-muted uppercase">Tu bodega hoy</span>
        {stats.map(({ label, value, tone }) => (
          <div key={label} className="flex items-baseline justify-between gap-2">
            <span className="font-medium text-muted">{label}</span>
            <span className={`text-2xl font-bold tabular-nums ${tone}`}>{initialLoading ? '…' : value}</span>
          </div>
        ))}
      </section>
      <section className="flex flex-col gap-1 rounded-[1.625rem] bg-surface p-4">
        <span className="pb-1.5 font-bold">Más preguntas</span>
        {questions.map((question) => (
          <button
            key={question}
            type="button"
            disabled={disabled}
            onClick={() => onAsk(question)}
            className="flex items-center justify-between gap-2 border-t border-separator py-2.5 text-left font-medium outline-none hover:text-accent focus-visible:ring-2 focus-visible:ring-focus disabled:opacity-60"
          >
            {question}
            <ArrowUpRight aria-hidden="true" className="size-5 shrink-0 text-muted" />
          </button>
        ))}
      </section>
      <button
        type="button"
        onClick={onClear}
        className="h-10 rounded-full border border-border bg-background font-semibold text-muted outline-none hover:bg-surface-tertiary focus-visible:ring-2 focus-visible:ring-focus"
      >
        Nueva conversación
      </button>
    </aside>
  )
}
