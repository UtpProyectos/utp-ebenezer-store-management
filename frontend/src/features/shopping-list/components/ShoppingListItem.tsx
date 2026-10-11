import Check from '@gravity-ui/icons/Check'
import Xmark from '@gravity-ui/icons/Xmark'
import { formatQuantity, unitLabel } from '@/features/inventory'
import { formatMoney } from '@/shared/utils/money'
import type { ShoppingListDetailResponse } from '../types/shopping-list.types'
import { QuantityStepper } from './QuantityStepper'

interface ShoppingListItemProps {
  item: ShoppingListDetailResponse
  onQuantityChange: (quantity: number) => void
  onPurchasedChange: (purchased: boolean) => void
  onRemove: () => void
}

export function ShoppingListItem({ item, onQuantityChange, onPurchasedChange, onRemove }: ShoppingListItemProps) {
  const bought = item.purchased

  return (
    <li
      className={`grid grid-cols-[2.25rem_minmax(0,1fr)_auto] items-center gap-x-4 gap-y-3 rounded-3xl px-3.5 py-3 transition-[opacity,background-color] duration-200 ease-out md:grid-cols-[2.25rem_minmax(0,2fr)_8.25rem_6rem_2.5rem] ${
        bought ? 'bg-surface-secondary/60 opacity-55' : 'bg-surface-secondary'
      }`}
    >
      <button
        type="button"
        role="checkbox"
        aria-checked={bought}
        aria-label={`Marcar ${item.productName} como comprado`}
        onClick={() => onPurchasedChange(!bought)}
        className={`grid size-9 place-items-center rounded-full border-2 text-accent-foreground outline-none transition-[background-color,border-color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus active:scale-90 ${
          bought ? 'border-accent bg-accent' : 'border-border bg-surface'
        }`}
      >
        <Check aria-hidden="true" className={`size-5 transition-opacity duration-150 ${bought ? 'opacity-100' : 'opacity-0'}`} />
      </button>

      <div className="flex min-w-0 flex-col gap-0.5">
        <span className={`text-[1.0625rem] font-semibold ${bought ? 'line-through' : ''}`}>{item.productName}</span>
        <span className={`text-sm font-medium ${item.currentStock <= 0 ? 'text-danger' : 'text-muted'}`}>
          Quedan {formatQuantity(Math.max(0, item.currentStock))} {unitLabel(item.unitOfMeasureAbbreviation)}
        </span>
      </div>

      <span className="text-right text-[1.0625rem] font-bold tabular-nums md:col-start-4 md:row-start-1">
        {item.estimatedCost > 0 ? formatMoney(item.estimatedCost) : <span className="text-sm font-medium text-muted">Sin costo</span>}
      </span>

      <div className="col-span-3 flex items-center justify-between gap-3 md:col-span-1 md:col-start-3 md:row-start-1">
        <QuantityStepper value={item.suggestedQuantity} label={item.productName} onChange={onQuantityChange} />
        <button
          type="button"
          title="Quitar de la lista"
          aria-label={`Quitar ${item.productName} de la lista`}
          onClick={onRemove}
          className="grid size-10 place-items-center rounded-full bg-surface text-muted outline-none transition-colors duration-150 hover:bg-danger-soft hover:text-danger focus-visible:ring-2 focus-visible:ring-focus md:hidden"
        >
          <Xmark aria-hidden="true" className="size-4.5" />
        </button>
      </div>

      <button
        type="button"
        title="Quitar de la lista"
        aria-label={`Quitar ${item.productName} de la lista`}
        onClick={onRemove}
        className="hidden size-10 place-items-center rounded-full bg-surface text-muted outline-none transition-colors duration-150 hover:bg-danger-soft hover:text-danger focus-visible:ring-2 focus-visible:ring-focus md:col-start-5 md:row-start-1 md:grid"
      >
        <Xmark aria-hidden="true" className="size-4.5" />
      </button>
    </li>
  )
}
