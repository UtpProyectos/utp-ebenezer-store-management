import ArrowRightFromSquare from '@gravity-ui/icons/ArrowRightFromSquare'
import CircleInfo from '@gravity-ui/icons/CircleInfo'
import ShoppingBasket from '@gravity-ui/icons/ShoppingBasket'
import ShoppingCart from '@gravity-ui/icons/ShoppingCart'
import Wallet from '@gravity-ui/icons/Wallet'
import type { ProductStockResponse } from '@/features/inventory'
import { formatMoney } from '@/shared/utils/money'
import type { CartLine as CartLineData, CheckoutMode } from '../hooks/useCart'
import { CartLine } from './CartLine'

interface CartPanelProps {
  mode: CheckoutMode
  onModeChange: (mode: CheckoutMode) => void
  lines: CartLineData[]
  itemCount: number
  subtotal: number
  notice: string | null
  error: string | null
  isPending: boolean
  onCharge: () => void
  onCancel: () => void
  onSetQuantity: (item: ProductStockResponse, quantity: number) => void
  onIncrement: (item: ProductStockResponse) => void
  onEditWeight: (item: ProductStockResponse) => void
  onRemove: (productId: number) => void
}

const MODES: { id: CheckoutMode; label: string; icon: typeof ShoppingCart }[] = [
  { id: 'sale', label: 'Venta', icon: ShoppingCart },
  { id: 'consumption', label: 'Consumo', icon: ArrowRightFromSquare },
]

export function CartPanel({
  mode,
  onModeChange,
  lines,
  itemCount,
  subtotal,
  notice,
  error,
  isPending,
  onCharge,
  onCancel,
  onSetQuantity,
  onIncrement,
  onEditWeight,
  onRemove,
}: CartPanelProps) {
  const consumption = mode === 'consumption'
  const empty = lines.length === 0
  const total = consumption ? 0 : subtotal

  return (
    <aside className="flex flex-col overflow-hidden rounded-[2rem] bg-surface lg:sticky lg:top-23">
      <div className="flex flex-col gap-2.5 border-b border-separator p-3.5">
        <div role="tablist" aria-label="Tipo de registro" className="relative grid grid-cols-2 rounded-[1.125rem] bg-surface-tertiary p-1">
          <span
            aria-hidden="true"
            className={`absolute top-1 bottom-1 left-1 w-[calc(50%-0.25rem)] rounded-xl bg-surface shadow-sm transition-transform duration-200 ease-out ${
              consumption ? 'translate-x-full' : ''
            }`}
          />
          {MODES.map(({ id, label, icon: Icon }) => {
            const selected = id === mode
            const color = selected ? (id === 'consumption' ? 'text-warning-soft-foreground' : 'text-foreground') : 'text-muted'
            return (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={selected}
                onClick={() => onModeChange(id)}
                className={`relative flex h-9 items-center justify-center gap-1.5 rounded-xl font-semibold outline-none focus-visible:ring-2 focus-visible:ring-focus ${color}`}
              >
                <Icon aria-hidden="true" className="size-5" />
                {label}
              </button>
            )
          })}
        </div>
        {consumption && (
          <p className="flex gap-2.5 rounded-[0.875rem] bg-warning-soft px-3 py-2.5 font-medium text-warning-soft-foreground">
            <CircleInfo aria-hidden="true" className="mt-0.5 size-5 shrink-0" />
            Descuenta stock sin generar ingreso.
          </p>
        )}
      </div>

      <div className="flex items-center justify-between px-4 pt-3 pb-1">
        <span className="text-[1.0625rem] font-bold">{consumption ? 'Productos a consumir' : 'Carrito'}</span>
        <span className="font-medium text-muted">
          {itemCount} producto{itemCount === 1 ? '' : 's'}
        </span>
      </div>

      <div className="overflow-y-auto px-2 py-1 lg:max-h-[calc(100vh-29.5rem)]">
        {empty ? (
          <div className="flex flex-col items-center gap-2 px-4 py-9 text-center">
            <span className="grid size-12 place-items-center rounded-3xl bg-surface-tertiary text-muted">
              <ShoppingBasket aria-hidden="true" className="size-6" />
            </span>
            <span className="font-medium text-muted">Carrito vacío</span>
          </div>
        ) : (
          <ul className="flex flex-col">
            {lines.map((line) => (
              <CartLine
                key={line.item.productId}
                line={line}
                onIncrement={() => onIncrement(line.item)}
                onDecrement={() => onSetQuantity(line.item, line.quantity - 1)}
                onSetQuantity={(quantity) => onSetQuantity(line.item, quantity)}
                onEditWeight={() => onEditWeight(line.item)}
                onRemove={() => onRemove(line.item.productId)}
              />
            ))}
          </ul>
        )}
      </div>

      <div className="flex flex-col gap-2.5 border-t border-separator bg-surface-secondary px-4 pt-3.5 pb-4">
        {notice && (
          <p role="status" className="rounded-[0.875rem] bg-danger-soft px-3 py-2 text-sm font-medium text-danger-soft-foreground">
            {notice}
          </p>
        )}
        {error && (
          <p role="alert" className="rounded-[0.875rem] bg-danger-soft px-3 py-2 text-sm font-medium text-danger-soft-foreground">
            {error}
          </p>
        )}
        {consumption && (
          <div className="flex justify-between font-medium text-muted">
            <span>Valor de venta</span>
            <span className="tabular-nums">{formatMoney(subtotal)}</span>
          </div>
        )}
        <div className="flex items-baseline justify-between">
          <span className="text-lg font-bold">{consumption ? 'Total en caja' : 'Total'}</span>
          <span
            className={`text-[2rem] leading-none font-extrabold tracking-tight tabular-nums ${
              consumption ? 'text-warning-soft-foreground' : ''
            }`}
          >
            {formatMoney(total)}
          </span>
        </div>
        <button
          type="button"
          disabled={empty || isPending}
          onClick={onCharge}
          className={`flex h-13.5 items-center justify-center gap-2 rounded-full text-lg font-bold outline-none transition-[background-color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 enabled:active:scale-[0.98] disabled:cursor-not-allowed ${
            empty
              ? 'bg-border text-muted'
              : consumption
                ? 'bg-accent text-accent-foreground hover:bg-accent-hover'
                : 'bg-foreground text-background hover:bg-foreground/85'
          }`}
        >
          {consumption ? (
            <ArrowRightFromSquare aria-hidden="true" className="size-5" />
          ) : (
            <Wallet aria-hidden="true" className="size-5" />
          )}
          {consumption ? (isPending ? 'Registrando…' : 'Registrar consumo') : `Cobrar ${formatMoney(total)}`}
        </button>
        {!empty && (
          <button
            type="button"
            onClick={onCancel}
            className="h-10 rounded-full font-semibold text-danger outline-none transition-colors duration-150 hover:bg-danger-soft focus-visible:ring-2 focus-visible:ring-focus"
          >
            {consumption ? 'Cancelar consumo' : 'Cancelar venta'}
          </button>
        )}
      </div>
    </aside>
  )
}
