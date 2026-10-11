import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { formatMoney } from '@/shared/utils/money'
import { HistoryFilters } from '../components/HistoryFilters'
import { HistoryTable } from '../components/HistoryTable'
import { useHistory } from '../hooks/useHistory'
import type { HistoryPeriod, HistoryRow, HistoryTab } from '../types/history.types'

const TABLES: Record<HistoryTab, { title: string; headers: [string, string, string]; total: (rows: HistoryRow[]) => string }> = {
  sales: {
    title: 'Historial de ventas',
    headers: ['Venta', 'Pago', 'Total'],
    total: (rows) => formatMoney(rows.reduce((sum, row) => sum + row.value, 0)),
  },
  purchases: {
    title: 'Historial de ingresos',
    headers: ['Producto', 'Proveedor', 'Pagaste'],
    total: (rows) => formatMoney(rows.reduce((sum, row) => sum + row.value, 0)),
  },
  movements: {
    title: 'Movimientos de inventario',
    headers: ['Producto', 'Tipo', 'Cantidad'],
    total: (rows) => `${rows.length} ${rows.length === 1 ? 'movimiento' : 'movimientos'}`,
  },
  consumption: {
    title: 'Consumo interno',
    headers: ['Producto', 'Motivo', 'Cantidad'],
    total: (rows) => `${rows.length} ${rows.length === 1 ? 'producto' : 'productos'}`,
  },
}

function toTab(value: string | null): HistoryTab {
  return value !== null && Object.hasOwn(TABLES, value) ? (value as HistoryTab) : 'sales'
}

/** Admin only. ?tab= opens a tab directly (e.g. movements from the sales history). */
export function HistoryPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const tab = toTab(searchParams.get('tab'))
  const [period, setPeriod] = useState<HistoryPeriod>('week')
  const { rows, loading, error, reload } = useHistory(tab, period)
  const table = TABLES[tab]

  return (
    <div className="mx-auto flex w-full max-w-410 flex-col gap-4">
      <HistoryFilters
        tab={tab}
        period={period}
        onTabChange={(next) => setSearchParams({ tab: next }, { replace: true })}
        onPeriodChange={setPeriod}
      />
      <HistoryTable
        title={table.title}
        total={loading ? '' : table.total(rows)}
        headers={table.headers}
        rows={rows}
        loading={loading}
        error={error}
        onRetry={reload}
      />
    </div>
  )
}
