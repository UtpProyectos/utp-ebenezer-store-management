import { Button, Spinner } from '@heroui/react'
import CircleCheckFill from '@gravity-ui/icons/CircleCheckFill'
import ListCheck from '@gravity-ui/icons/ListCheck'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { ShoppingListSummary } from '../components/ShoppingListSummary'
import { SupplierGroup } from '../components/SupplierGroup'
import { useShoppingList } from '../hooks/useShoppingList'
import type { ShoppingListDetailResponse } from '../types/shopping-list.types'

const NO_SUPPLIER = 'Sin proveedor'

/** Items grouped by suggested supplier; items without one go last. */
function groupBySupplier(details: ShoppingListDetailResponse[]) {
  const groups = new Map<string, ShoppingListDetailResponse[]>()
  for (const detail of details) {
    const name = detail.supplierName ?? NO_SUPPLIER
    groups.set(name, [...(groups.get(name) ?? []), detail])
  }
  return [...groups.entries()].sort(([a], [b]) => Number(a === NO_SUPPLIER) - Number(b === NO_SUPPLIER))
}

export function ShoppingListPage() {
  const navigate = useNavigate()
  const shoppingList = useShoppingList()
  const { list, loading, loadError, actionError, generating } = shoppingList

  if (loading) {
    return (
      <div className="grid place-items-center rounded-3xl bg-surface py-16">
        <Spinner aria-label="Cargando lista de compra" />
      </div>
    )
  }

  if (loadError) {
    return (
      <div role="alert" className="mx-auto flex max-w-410 flex-col items-center gap-3 rounded-3xl bg-surface px-5 py-14 text-center">
        <p className="font-semibold">{loadError}</p>
        <Button variant="outline" onPress={() => void shoppingList.reload()}>Reintentar</Button>
      </div>
    )
  }

  const details = list?.details ?? []
  const bought = details.filter((detail) => detail.purchased)
  const pendingCost = details.filter((detail) => !detail.purchased).reduce((sum, detail) => sum + detail.estimatedCost, 0)
  // Same as the prototype: the entry starts with the first bought product (or the first one of the list).
  const entryProduct = bought[0] ?? details[0]
  const registerEntry = () =>
    navigate(entryProduct ? `${ROUTES.purchases}?productId=${entryProduct.productId}` : ROUTES.purchases)

  return (
    <div className="mx-auto flex w-full max-w-410 flex-col gap-4">
      {list && (
        <ShoppingListSummary
          pendingCost={pendingCost}
          boughtCount={bought.length}
          totalCount={details.length}
          onRegisterEntry={registerEntry}
        />
      )}

      {actionError && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          {actionError}
        </p>
      )}

      {list?.status === 'COMPLETED' && (
        <div role="status" className="flex flex-wrap items-center gap-3 rounded-[1.25rem] bg-success-soft px-5 py-3 text-success-soft-foreground print:hidden">
          <CircleCheckFill aria-hidden="true" className="size-5 shrink-0" />
          <span className="flex-1 font-medium">Ya compraste todo lo de esta lista.</span>
          <Button size="sm" variant="secondary" isPending={generating} onPress={() => void shoppingList.generate()}>
            Armar una nueva lista
          </Button>
        </div>
      )}

      {!list ? (
        <div className="flex flex-col items-center gap-3 rounded-[2rem] bg-surface px-5 py-16 text-center">
          <span className="grid size-16 place-items-center rounded-full bg-surface-secondary text-accent">
            <ListCheck aria-hidden="true" className="size-7" />
          </span>
          <p className="text-xl font-bold">Tu lista está vacía</p>
          <p className="max-w-85 text-muted">
            Arma la lista con los productos que están por agotarse o revisa tu inventario.
          </p>
          <div className="mt-1 flex flex-wrap justify-center gap-2">
            <Button isPending={generating} onPress={() => void shoppingList.generate()}>
              Armar lista con lo que falta
            </Button>
            <Button variant="outline" onPress={() => navigate(ROUTES.inventory)}>Ir a inventario</Button>
          </div>
        </div>
      ) : (
        groupBySupplier(details).map(([supplierName, items]) => (
          <SupplierGroup
            key={supplierName}
            supplierName={supplierName}
            items={items}
            onQuantityChange={shoppingList.setQuantity}
            onPurchasedChange={shoppingList.setPurchased}
            onRemove={shoppingList.removeDetail}
          />
        ))
      )}
    </div>
  )
}
