import type { HistoryPeriod } from '../types/history.types'

/** Local date as yyyy-MM-dd (toISOString would shift it to UTC). */
function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

/** From / to (both included) of a period: today, this week (from Monday) or this month. */
export function periodRange(period: HistoryPeriod, today = new Date()): { from: string; to: string } {
  const start = new Date(today)
  if (period === 'week') start.setDate(today.getDate() - ((today.getDay() + 6) % 7))
  if (period === 'month') start.setDate(1)
  return { from: toIsoDate(start), to: toIsoDate(today) }
}
