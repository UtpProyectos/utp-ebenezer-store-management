import { useState } from 'react'
import { Button, Spinner } from '@heroui/react'
import CircleCheckFill from '@gravity-ui/icons/CircleCheckFill'
import Plus from '@gravity-ui/icons/Plus'
import TriangleExclamationFill from '@gravity-ui/icons/TriangleExclamationFill'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { formatMoney } from '@/shared/utils/money'
import { CancelSaleModal } from '../components/CancelSaleModal'
import { EditSaleModal } from '../components/EditSaleModal'
import { SaleChangesPanel } from '../components/SaleChangesPanel'
import { SaleHistoryRow } from '../components/SaleHistoryRow'
import { useSalesHistory } from '../hooks/useSalesHistory'
import type { SaleResponse } from '../types/sale.types'

type Correction = { kind: 'edit' | 'cancel'; sale: SaleResponse }

// Search by sale number or by product name, as in the prototype.
function matches(sale: SaleResponse, query: string) {
  const normalized = query.trim().toLowerCase()
  if (!normalized) return true
  return (
    String(sale.id).includes(normalized) ||
    sale.details.some((detail) => detail.productName.toLowerCase().includes(normalized))
  )
}

export function SalesHistoryPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { sales, changes, initialLoading, error, reload } = useSalesHistory()
  const [query, setQuery] = useState('')
  const [correction, setCorrection] = useState<Correction | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const visible = sales.filter((sale) => matches(sale, query))
  // Cancelled sales stay visible (they are never deleted) but no longer count in the cash total.
  const total = visible.filter((sale) => sale.status !== 'CANCELLED').reduce((sum, sale) => sum + sale.total, 0)

  const finish = (message: string) => {
    setCorrection(null)
    setNotice(message)
    reload()
  }

  if (initialLoading) {
    return (
      <div className="grid place-items-center py-24">
        <Spinner size="lg" />
      </div>
    )
  }

  return (
    <div className="mx-auto grid max-w-[102.5rem] items-start gap-4 lg:grid-cols-[minmax(0,1.7fr)_minmax(18.75rem,1fr)]">
      <div className="flex min-w-0 flex-col gap-3.5">
        {changes.length > 0 && (
          <div role="status" className="flex items-center gap-3.5 rounded-full bg-warning-soft py-3.5 pr-4 pl-3.5 text-warning-soft-foreground">
            <span className="grid size-11 shrink-0 place-items-center rounded-full bg-surface">
              <TriangleExclamationFill aria-hidden="true" className="size-5" />
            </span>
            <span className="font-semibold">
              {changes.length} cambio{changes.length === 1 ? '' : 's'} en ventas de hoy
            </span>
          </div>
        )}

        {notice && (
          <div role="status" className="flex items-center gap-3 rounded-[1.25rem] bg-success-soft px-5 py-3 text-success-soft-foreground">
            <CircleCheckFill aria-hidden="true" className="size-5 shrink-0" />
            <span className="flex-1 font-medium">{notice}</span>
            <Button size="sm" variant="ghost" onPress={() => setNotice(null)}>Cerrar</Button>
          </div>
        )}

        <section className="flex flex-col gap-2 rounded-[2rem] bg-surface p-3">
          <div className="flex flex-wrap items-center gap-2.5 px-1 pt-1 pb-1.5">
            <SearchInput
              label="Buscar venta"
              placeholder="Buscar por número o producto"
              value={query}
              onChange={setQuery}
              className="min-w-0 flex-[1_1_16.25rem]"
            />
            <button
              type="button"
              onClick={() => navigate(ROUTES.sales)}
              className="flex h-13.5 items-center gap-2.5 rounded-full bg-accent py-0 pr-5 pl-2 font-bold whitespace-nowrap text-accent-foreground outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-accent-hover focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 active:scale-[0.97]"
            >
              <span className="grid size-9.5 place-items-center rounded-full bg-surface text-accent">
                <Plus aria-hidden="true" className="size-5" />
              </span>
              Registrar venta
            </button>
          </div>

          <div className="flex items-center justify-between px-3 py-1 font-semibold text-muted">
            <span>
              {visible.length} venta{visible.length === 1 ? '' : 's'} hoy
            </span>
            <span>
              Total: <b className="text-foreground tabular-nums">{formatMoney(total)}</b>
            </span>
          </div>

          {error ? (
            <div className="flex flex-col items-center gap-3 rounded-3xl bg-background p-9 text-center">
              <p className="text-muted">{error}</p>
              <Button variant="outline" onPress={reload}>Reintentar</Button>
            </div>
          ) : visible.length === 0 ? (
            <p className="rounded-3xl bg-background p-9 text-center font-medium text-muted">
              {sales.length === 0 ? 'Aún no hay ventas hoy.' : 'No hay ventas que coincidan.'}
            </p>
          ) : (
            <ul className="flex flex-col gap-2">
              {visible.map((sale) => (
                <SaleHistoryRow
                  key={sale.id}
                  sale={sale}
                  onEdit={() => setCorrection({ kind: 'edit', sale })}
                  onCancel={() => setCorrection({ kind: 'cancel', sale })}
                />
              ))}
            </ul>
          )}
        </section>
      </div>

      <SaleChangesPanel
        changes={changes}
        onShowMovements={user?.role === 'ADMIN' ? () => navigate(ROUTES.history) : undefined}
      />

      {correction?.kind === 'edit' && (
        <EditSaleModal
          sale={correction.sale}
          onClose={() => setCorrection(null)}
          onSaved={() => finish(`Venta #${correction.sale.id} corregida. El stock ya se actualizó.`)}
        />
      )}
      {correction?.kind === 'cancel' && (
        <CancelSaleModal
          sale={correction.sale}
          onClose={() => setCorrection(null)}
          onCancelled={() => finish(`Venta #${correction.sale.id} eliminada. Los productos volvieron al inventario.`)}
        />
      )}
    </div>
  )
}
