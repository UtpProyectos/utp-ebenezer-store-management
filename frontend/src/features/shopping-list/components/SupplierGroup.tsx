import ShoppingBasket from '@gravity-ui/icons/ShoppingBasket'
import { formatMoney } from '@/shared/utils/money'
import type { ShoppingListDetailResponse } from '../types/shopping-list.types'
import { ShoppingListItem } from './ShoppingListItem'

interface SupplierGroupProps {
  supplierName: string
  items: ShoppingListDetailResponse[]
  onQuantityChange: (detailId: number, quantity: number) => void
  onPurchasedChange: (detailId: number, purchased: boolean) => void
  onRemove: (detailId: number) => void
}

/** Items of one supplier (the one of the last purchase), as in the prototype. */
export function SupplierGroup({ supplierName, items, onQuantityChange, onPurchasedChange, onRemove }: SupplierGroupProps) {
  const total = items.reduce((sum, item) => sum + item.estimatedCost, 0)

  return (
    <section aria-label={supplierName} className="flex flex-col gap-2 rounded-[2rem] bg-surface p-3">
      <header className="flex items-center gap-3 px-2.5 pt-2 pb-2.5">
        <span className="grid size-11 shrink-0 place-items-center rounded-full bg-surface-secondary">
          <ShoppingBasket aria-hidden="true" className="size-5" />
        </span>
        <h2 className="min-w-0 flex-1 truncate text-lg font-bold">{supplierName}</h2>
        <span className="px-2.5 text-[1.0625rem] font-semibold tabular-nums">{formatMoney(total)}</span>
      </header>
      <ul className="flex flex-col gap-2">
        {items.map((item) => (
          <ShoppingListItem
            key={item.id}
            item={item}
            onQuantityChange={(quantity) => onQuantityChange(item.id, quantity)}
            onPurchasedChange={(purchased) => onPurchasedChange(item.id, purchased)}
            onRemove={() => onRemove(item.id)}
          />
        ))}
      </ul>
    </section>
  )
}
