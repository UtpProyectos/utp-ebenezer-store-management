export type HistoryTab = 'sales' | 'purchases' | 'movements' | 'consumption'

export type HistoryPeriod = 'today' | 'week' | 'month'

export type HistoryTone = 'default' | 'success' | 'danger' | 'warning' | 'info' | 'muted'

/** One line of a history table, already formatted for display. */
export interface HistoryRow {
  key: string
  /** ISO date-time of the record. */
  date: string
  main: string
  sub?: string
  tag: string
  tagTone: HistoryTone
  amount: string
  amountTone: HistoryTone
  /** Money that counts in the total of the tab (sales, purchases); 0 for cancelled records. */
  value: number
}
