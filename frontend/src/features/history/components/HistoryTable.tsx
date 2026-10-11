import { Button, Spinner } from '@heroui/react'
import Printer from '@gravity-ui/icons/Printer'
import type { HistoryRow, HistoryTone } from '../types/history.types'
import { formatHistoryDate } from '../utils/historyRows'

interface HistoryTableProps {
  title: string
  total: string
  /** Header of the 2nd, 3rd and 4th columns (the first one is always the date). */
  headers: [string, string, string]
  rows: HistoryRow[]
  loading: boolean
  error: string | null
  onRetry: () => void
}

const TONES: Record<HistoryTone, string> = {
  default: 'text-foreground',
  success: 'text-success',
  danger: 'text-danger',
  warning: 'text-warning',
  info: 'text-info',
  muted: 'text-muted',
}

export function HistoryTable({ title, total, headers, rows, loading, error, onRetry }: HistoryTableProps) {
  return (
    <section className="flex flex-col overflow-hidden rounded-[1.75rem] bg-surface pt-3 pb-2">
      <div className="flex flex-wrap items-center gap-3.5 pt-2 pr-4.5 pb-1 pl-5.5">
        <h2 className="min-w-0 flex-1 text-xl font-bold">{title}</h2>
        <span className="text-[1.375rem] leading-none font-bold tabular-nums">{total}</span>
        <button
          type="button"
          aria-label="Imprimir"
          title="Imprimir"
          onClick={() => window.print()}
          className="grid size-11 place-items-center rounded-full bg-surface-secondary outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-tertiary focus-visible:ring-2 focus-visible:ring-focus active:scale-95 print:hidden"
        >
          <Printer aria-hidden="true" className="size-5" />
        </button>
      </div>

      {loading ? (
        <div className="grid place-items-center py-14">
          <Spinner aria-label="Cargando historial" />
        </div>
      ) : error ? (
        <div role="alert" className="flex flex-col items-center gap-3 px-5 py-12 text-center">
          <p className="font-semibold">{error}</p>
          <Button variant="outline" onPress={onRetry}>Reintentar</Button>
        </div>
      ) : rows.length === 0 ? (
        <p className="px-5 py-12 text-center text-muted">No hay registros en este período.</p>
      ) : (
        <table className="w-full border-collapse text-left">
          <thead className="hidden text-sm text-muted md:table-header-group">
            <tr>
              <th scope="col" className="w-36 pt-4 pb-2.5 pl-4.5 font-medium">Fecha</th>
              <th scope="col" className="px-3.5 pt-4 pb-2.5 font-medium">{headers[0]}</th>
              <th scope="col" className="w-1/4 px-3.5 pt-4 pb-2.5 font-medium">{headers[1]}</th>
              <th scope="col" className="w-36 pt-4 pr-4.5 pb-2.5 text-right font-medium">{headers[2]}</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.key} className="border-b border-separator last:border-b-0">
                <td className="py-3 pl-4.5 align-top text-[0.9375rem] font-medium whitespace-nowrap text-muted tabular-nums md:align-middle">
                  {formatHistoryDate(row.date)}
                </td>
                <td className="px-3.5 py-3">
                  <span className="block leading-snug font-semibold">{row.main}</span>
                  {row.sub && <span className="block truncate text-sm font-medium text-muted">{row.sub}</span>}
                  <span className={`text-sm font-medium md:hidden ${TONES[row.tagTone]}`}>{row.tag}</span>
                </td>
                <td className={`hidden px-3.5 py-3 text-[0.9375rem] font-medium md:table-cell ${TONES[row.tagTone]}`}>
                  {row.tag}
                </td>
                <td className={`py-3 pr-4.5 text-right font-semibold whitespace-nowrap tabular-nums ${TONES[row.amountTone]}`}>
                  {row.amount}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
