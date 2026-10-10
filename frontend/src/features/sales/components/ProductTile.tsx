import Plus from '@gravity-ui/icons/Plus'
import ShoppingBag from '@gravity-ui/icons/ShoppingBag'
import type { ProductStockResponse } from '@/features/inventory'
import type { PromotionResponse } from '@/features/promotions/types/promotion.types'
import { formatMoney } from '@/shared/utils/money'
import { formatSellable, formatWeightShort, isWeighed, promotionLabel, sellableStock } from '../utils/salePricing'

interface ProductTileProps {
  item: ProductStockResponse
  promotion?: PromotionResponse
  /** Quantity already in the cart, in the base unit. */
  inCart: number
  onAdd: (item: ProductStockResponse) => void
}

// Same thresholds as the prototype: warn when half of the minimum stock or less is left.
function stockNotice(item: ProductStockResponse): string | null {
  const sellable = sellableStock(item)
  if (sellable <= 0) return item.expiredQuantity > 0 ? 'Vencido' : 'Sin stock'
  if (sellable <= item.minStock / 2) return `Solo ${formatSellable(item)}`
  return null
}

export function ProductTile({ item, promotion, inCart, onAdd }: ProductTileProps) {
  const weighed = isWeighed(item)
  const unavailable = sellableStock(item) <= 0
  const notice = stockNotice(item)

  return (
    <button
      type="button"
      disabled={unavailable}
      onClick={() => onAdd(item)}
      className={`flex min-h-32 flex-col gap-2.5 rounded-3xl bg-background py-3 pr-3 pl-3.5 text-left outline-none transition-[background-color,box-shadow,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus enabled:hover:bg-surface-tertiary enabled:active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50 ${
        inCart > 0 ? 'ring-2 ring-accent ring-inset' : ''
      }`}
    >
      <span className="flex w-full items-center justify-between">
        <span className="grid size-10 shrink-0 place-items-center rounded-full bg-surface text-accent">
          <ShoppingBag aria-hidden="true" className="size-5" />
        </span>
        <span
          className={`grid size-10 shrink-0 place-items-center rounded-full font-bold transition-colors duration-150 ${
            inCart > 0 ? 'bg-accent text-accent-foreground' : 'bg-surface text-accent'
          }`}
        >
          {inCart > 0 ? (
            <span className={weighed ? 'text-xs' : 'text-base'}>{weighed ? formatWeightShort(inCart) : inCart}</span>
          ) : (
            <Plus aria-label="Agregar" className="size-5" />
          )}
        </span>
      </span>

      <span className="line-clamp-2 text-base leading-snug font-semibold text-pretty">{item.productName}</span>

      <span className="mt-auto flex w-full flex-wrap items-baseline justify-between gap-2">
        <span className="flex items-baseline gap-0.5 text-[1.0625rem] font-bold whitespace-nowrap tabular-nums">
          {formatMoney(item.salePrice)}
          {weighed && <span className="text-sm font-medium text-muted">/kg</span>}
        </span>
        {promotion && (
          <span className="rounded-full bg-accent-soft px-2 py-0.5 text-sm font-semibold whitespace-nowrap text-accent-soft-foreground">
            {promotionLabel(item, promotion)}
          </span>
        )}
        {notice && <span className="text-sm font-medium whitespace-nowrap text-danger">{notice}</span>}
      </span>
    </button>
  )
}
