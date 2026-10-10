import { useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { Button, Spinner } from '@heroui/react'
import CircleCheckFill from '@gravity-ui/icons/CircleCheckFill'
import ListCheck from '@gravity-ui/icons/ListCheck'
import Magnifier from '@gravity-ui/icons/Magnifier'
import { ROUTES } from '@/app/router/routes'
import { useAuth } from '@/features/auth'
import { CreateProductModal } from '@/features/products'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { InventoryFilterTabs } from '../components/InventoryFilterTabs'
import { InventoryTable } from '../components/InventoryTable'
import { TruckIcon } from '../components/TruckIcon'
import { WithdrawProductModal } from '../components/WithdrawProductModal'
import { useInventory } from '../hooks/useInventory'
import type { ProductStockResponse } from '../types/inventory.types'
import { matchesFilter, type InventoryFilter } from '../utils/inventoryFilter'
import { matchesSearch } from '../utils/inventoryFormat'

// Pill buttons from the prototype (52px, bold), not HeroUI's default button shape.
const HEADER_BUTTON =
  'inline-flex h-13 items-center gap-2.5 rounded-full px-5.5 text-[1.0625rem] font-bold outline-none transition-[background-color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 active:scale-[0.97]'

function toFilter(value: string | null): InventoryFilter {
  return value === 'restock' || value === 'expiring' ? value : 'all'
}

export function InventoryPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const canCreateProduct = user?.role === 'ADMIN'
  const { items, initialLoading, error, reload } = useInventory()
  // ?q= comes from the header search; ?filter= from the Home summary.
  const [searchParams] = useSearchParams()
  const urlQuery = searchParams.get('q') ?? ''
  const urlFilter = toFilter(searchParams.get('filter'))
  const [syncedUrl, setSyncedUrl] = useState({ query: urlQuery, filter: urlFilter })
  const [query, setQuery] = useState(urlQuery)
  const [filter, setFilter] = useState<InventoryFilter>(urlFilter)
  if (urlQuery !== syncedUrl.query || urlFilter !== syncedUrl.filter) {
    setSyncedUrl({ query: urlQuery, filter: urlFilter })
    setQuery(urlQuery)
    setFilter(urlFilter)
  }
  const [withdrawing, setWithdrawing] = useState<ProductStockResponse | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [creatingProduct, setCreatingProduct] = useState(false)

  const visibleItems = useMemo(
    () => items.filter((item) => matchesFilter(item, filter) && matchesSearch(item, query)),
    [items, filter, query],
  )

  const goToEntry = (item?: ProductStockResponse) =>
    navigate(item ? `${ROUTES.purchases}?productId=${item.productId}` : ROUTES.purchases)

  const showAll = () => {
    setFilter('all')
    setQuery('')
  }

  return (
    <div className="mx-auto flex w-full max-w-410 flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3.5">
        <InventoryFilterTabs items={items} value={filter} onChange={setFilter} />
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={() => goToEntry()}
            className={`${HEADER_BUTTON} bg-foreground text-background hover:bg-foreground/85`}
          >
            <TruckIcon aria-hidden="true" className="size-5.5" />
            Ingreso
          </button>
          <button
            type="button"
            onClick={() => navigate(ROUTES.shoppingList)}
            className={`${HEADER_BUTTON} bg-accent text-accent-foreground hover:bg-accent-hover`}
          >
            <ListCheck aria-hidden="true" className="size-5.5" />
            Lista de compra
          </button>
        </div>
      </div>

      <SearchInput label="Buscar un producto" value={query} onChange={setQuery} />

      {notice && (
        <div role="status" className="flex items-center gap-3 rounded-[1.25rem] bg-success-soft px-5 py-3 text-success-soft-foreground">
          <CircleCheckFill aria-hidden="true" className="size-5 shrink-0" />
          <span className="flex-1 font-medium">{notice}</span>
          <Button size="sm" variant="ghost" onPress={() => setNotice(null)}>Cerrar</Button>
        </div>
      )}

      {initialLoading ? (
        <div className="grid place-items-center rounded-3xl bg-surface py-16">
          <Spinner aria-label="Cargando inventario" />
        </div>
      ) : error ? (
        <div role="alert" className="flex flex-col items-center gap-3 rounded-3xl bg-surface px-5 py-14 text-center">
          <p className="font-semibold">No se pudo cargar el inventario</p>
          <p className="text-sm text-muted">{error}</p>
          <Button variant="outline" onPress={reload}>Reintentar</Button>
        </div>
      ) : visibleItems.length === 0 ? (
        <div className="flex flex-col items-center gap-3 rounded-3xl bg-surface px-5 py-14 text-center">
          <span className="grid size-15 place-items-center rounded-[1.25rem] bg-surface-tertiary text-muted">
            <Magnifier aria-hidden="true" className="size-7" />
          </span>
          <p className="text-lg font-bold">
            {items.length === 0 ? 'Aún no hay productos activos' : 'No encontramos ese producto'}
          </p>
          <div className="flex flex-wrap justify-center gap-2">
            {canCreateProduct && (
              <Button onPress={() => setCreatingProduct(true)}>
                {query.trim() ? `Crear “${query.trim()}”` : 'Crear producto'}
              </Button>
            )}
            {items.length > 0 && <Button variant="outline" onPress={showAll}>Mostrar todos</Button>}
          </div>
          {!canCreateProduct && items.length === 0 && (
            <p className="text-sm text-muted">Pídele al administrador que registre los productos.</p>
          )}
        </div>
      ) : (
        <InventoryTable items={visibleItems} onRestock={goToEntry} onWithdraw={setWithdrawing} />
      )}

      {creatingProduct && (
        <CreateProductModal
          initialName={query.trim()}
          onClose={() => setCreatingProduct(false)}
          onCreated={(product) => {
            setCreatingProduct(false)
            setQuery('')
            setNotice(`Se creó ${product.name}. Ya puedes registrar su ingreso.`)
            reload()
          }}
        />
      )}

      {withdrawing && (
        <WithdrawProductModal
          item={withdrawing}
          onClose={() => setWithdrawing(null)}
          onWithdrawn={(message) => {
            setWithdrawing(null)
            setNotice(message)
            reload()
          }}
        />
      )}
    </div>
  )
}
