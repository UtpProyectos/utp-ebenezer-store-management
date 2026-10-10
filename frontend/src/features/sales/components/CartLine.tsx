import Minus from '@gravity-ui/icons/Minus'
import Plus from '@gravity-ui/icons/Plus'
import ScalesBalanced from '@gravity-ui/icons/ScalesBalanced'
import TrashBin from '@gravity-ui/icons/TrashBin'
import { formatMoney } from '@/shared/utils/money'
import type { CartLine as CartLineData } from '../hooks/useCart'
import { formatWeight, isWeighed, promotionLabel } from '../utils/salePricing'

interface CartLineProps {
  line: CartLineData
  onIncrement: () => void
  onDecrement: () => void
  onSetQuantity: (quantity: number) => void
  onEditWeight: () => void
  onRemove: () => void
}

const STEPPER_BUTTON =
  'grid h-full w-8 place-items-center bg-surface-secondary outline-none transition-colors duration-150 hover:bg-surface-tertiary focus-visible:bg-surface-tertiary'

export function CartLine({ line, onIncrement, onDecrement, onSetQuantity, onEditWeight, onRemove }: CartLineProps) {
  const { item, promotion, quantity, total } = line
  const weighed = isWeighed(item)
  const unitPrice = weighed
    ? `${formatMoney(item.salePrice)} /kg`
    : `${formatMoney(item.salePrice)} c/u${promotion ? ` · ${promotionLabel(item, promotion)}` : ''}`

  return (
    <li className="grid grid-cols-[minmax(0,1fr)_auto] gap-x-2.5 gap-y-1.5 rounded-[0.875rem] px-2 py-2.5">
      <div className="flex min-w-0 flex-col gap-0.5">
        <span className="leading-snug font-semibold">{item.productName}</span>
        <span className="text-sm font-medium text-muted tabular-nums">{unitPrice}</span>
      </div>
      <span className="text-right text-[1.0625rem] font-bold tabular-nums">{formatMoney(total)}</span>

      <div>
        {weighed ? (
          <button
            type="button"
            onClick={onEditWeight}
            className="flex h-8.5 items-center gap-1.5 rounded-full bg-surface-tertiary px-3 font-bold tabular-nums outline-none transition-colors duration-150 hover:bg-border focus-visible:ring-2 focus-visible:ring-focus"
          >
            <ScalesBalanced aria-hidden="true" className="size-4 text-muted" />
            {formatWeight(quantity)}
          </button>
        ) : (
          <div className="flex h-8 w-27 items-center overflow-hidden rounded-xl border border-field-border">
            <button type="button" aria-label="Menos" onClick={onDecrement} className={STEPPER_BUTTON}>
              <Minus aria-hidden="true" className="size-4" />
            </button>
            <input
              aria-label={`Cantidad de ${item.productName}`}
              value={quantity}
              inputMode="numeric"
              onChange={(event) => onSetQuantity(Number.parseInt(event.target.value, 10) || 0)}
              className="h-full w-11 min-w-0 flex-1 bg-surface text-center font-bold tabular-nums outline-none"
            />
            <button type="button" aria-label="Más" onClick={onIncrement} className={STEPPER_BUTTON}>
              <Plus aria-hidden="true" className="size-4" />
            </button>
          </div>
        )}
      </div>

      <button
        type="button"
        onClick={onRemove}
        className="flex h-10 items-center gap-1 justify-self-end rounded-full px-2 text-sm font-semibold text-muted outline-none transition-colors duration-150 hover:bg-danger-soft hover:text-danger focus-visible:ring-2 focus-visible:ring-focus"
      >
        <TrashBin aria-hidden="true" className="size-4.5" />
        Quitar
      </button>
    </li>
  )
}
