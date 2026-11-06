import type { AssistantBlock, AssistantTone } from '../types/ai-assistant.types'

// Theme tokens only (AGENTS/frontend.md §5), same soft colors as the stock status chips.
const BADGE_TONE: Record<AssistantTone, string> = {
  NEUTRAL: 'bg-surface-tertiary text-foreground',
  SUCCESS: 'bg-success-soft text-success-soft-foreground',
  WARNING: 'bg-warning-soft text-warning-soft-foreground',
  DANGER: 'bg-danger-soft text-danger-soft-foreground',
  INFO: 'bg-info-soft text-info-soft-foreground',
  ACCENT: 'bg-accent-soft text-accent-soft-foreground',
}

const BAR_TONE: Record<AssistantTone, string> = {
  NEUTRAL: 'bg-foreground',
  SUCCESS: 'bg-success',
  WARNING: 'bg-warning',
  DANGER: 'bg-danger',
  INFO: 'bg-info',
  ACCENT: 'bg-accent',
}

/** Data under an answer: stat tiles, rows with a badge, or a ranking with bars. */
export function AssistantBlockView({ block }: { block: AssistantBlock }) {
  if (block.items.length === 0) return null

  if (block.type === 'STATS') {
    return (
      <section aria-label={block.title} className="flex flex-wrap gap-px overflow-hidden rounded-[1.125rem] border border-separator bg-separator">
        {block.items.map((item) => (
          <div key={item.label} className="flex min-w-30 flex-[1_1_7.5rem] flex-col gap-1 bg-background px-3.5 py-3">
            <span className="text-sm font-medium text-muted">{item.label}</span>
            <span className={`text-xl font-bold tabular-nums ${item.tone === 'ACCENT' ? 'text-accent' : ''}`}>{item.value}</span>
          </div>
        ))}
      </section>
    )
  }

  if (block.type === 'BARS') {
    return (
      <section aria-label={block.title} className="flex flex-col gap-2 rounded-[1.125rem] border border-separator p-3.5">
        <span className="text-sm font-semibold text-muted">{block.title}</span>
        {block.items.map((item) => (
          <div key={item.label} className="grid grid-cols-[minmax(5.5rem,10rem)_minmax(0,1fr)] items-center gap-x-2.5 gap-y-0.5 sm:grid-cols-[minmax(5.5rem,10rem)_minmax(0,1fr)_auto]">
            <span className="truncate font-medium">{item.label}</span>
            <div className="h-2.5 overflow-hidden rounded-full bg-surface-tertiary">
              <div
                className={`h-full rounded-full ${BAR_TONE[item.tone]}`}
                style={{ width: `${Math.round((item.ratio ?? 0) * 100)}%` }}
              />
            </div>
            <span className="col-span-2 text-right text-sm font-semibold tabular-nums sm:col-span-1">{item.value}</span>
          </div>
        ))}
      </section>
    )
  }

  return (
    <section aria-label={block.title} className="overflow-hidden rounded-[1.125rem] border border-separator">
      <p className="border-b border-separator px-3.5 py-2.5 text-sm font-semibold text-muted">{block.title}</p>
      <ul>
        {block.items.map((item, index) => (
          <li
            key={`${item.label}-${index}`}
            className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-x-3 gap-y-1 border-b border-separator px-3.5 py-2.5 last:border-b-0 sm:grid-cols-[minmax(0,1fr)_auto_auto]"
          >
            <span className="truncate font-semibold">{item.label}</span>
            <span className="text-sm text-muted tabular-nums max-sm:order-3 max-sm:col-span-2">{item.value}</span>
            {item.badge && (
              <span className={`justify-self-end rounded-full px-2.5 py-0.5 text-sm font-semibold whitespace-nowrap ${BADGE_TONE[item.tone]}`}>
                {item.badge}
              </span>
            )}
          </li>
        ))}
      </ul>
    </section>
  )
}
