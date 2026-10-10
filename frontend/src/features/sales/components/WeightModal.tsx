import { useState, type FormEvent } from 'react'
import { Button, Input, Modal, TextField } from '@heroui/react'
import type { ProductStockResponse } from '@/features/inventory'
import { formatMoney } from '@/shared/utils/money'
import { formatWeight, sellableStock } from '../utils/salePricing'

type WeightMode = 'weight' | 'amount'

interface WeightModalProps {
  item: ProductStockResponse
  /** Kilograms already in the cart; 0 when adding. */
  currentKilograms: number
  onSubmit: (kilograms: number) => void
  onClose: () => void
}

const WEIGHT_CHIPS: [number, string][] = [
  [250, '¼ kg'],
  [500, '½ kg'],
  [750, '¾ kg'],
  [1000, '1 kg'],
  [2000, '2 kg'],
]
const AMOUNT_CHIPS: [number, string][] = [
  [2, 'S/ 2'],
  [5, 'S/ 5'],
  [10, 'S/ 10'],
  [20, 'S/ 20'],
]

// Products sold by weight: the cashier types grams or the amount the customer wants to spend.
export function WeightModal({ item, currentKilograms, onSubmit, onClose }: WeightModalProps) {
  const [mode, setMode] = useState<WeightMode>('weight')
  const [valueText, setValueText] = useState(currentKilograms > 0 ? String(Math.round(currentKilograms * 1000)) : '')

  const value = Number(valueText) || 0
  const byWeight = mode === 'weight'
  const kilograms = byWeight ? value / 1000 : item.salePrice > 0 ? value / item.salePrice : 0
  const rounded = Math.round(kilograms * 1000) / 1000
  const available = sellableStock(item)
  const over = rounded > available
  const valid = rounded > 0 && !over

  const changeMode = (next: WeightMode) => {
    setMode(next)
    setValueText('')
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    if (valid) onSubmit(rounded)
  }

  return (
    <Modal.Backdrop isOpen onOpenChange={(open) => !open && onClose()}>
      <Modal.Container size="sm">
        <Modal.Dialog className="rounded-[1.75rem]">
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Modal.CloseTrigger aria-label="Cerrar" />
            <Modal.Header className="flex flex-col gap-0.5">
              <Modal.Heading className="text-xl font-bold">{item.productName}</Modal.Heading>
              <p className="font-medium text-muted">{formatMoney(item.salePrice)} por kilo</p>
            </Modal.Header>

            <div role="tablist" aria-label="Cómo vender" className="grid grid-cols-2 rounded-full bg-surface-tertiary p-1">
              {(['weight', 'amount'] as const).map((id) => {
                const selected = id === mode
                return (
                  <button
                    key={id}
                    type="button"
                    role="tab"
                    aria-selected={selected}
                    onClick={() => changeMode(id)}
                    className={`h-10 rounded-full font-semibold outline-none transition-colors duration-150 focus-visible:ring-2 focus-visible:ring-focus ${
                      selected ? 'bg-surface text-foreground shadow-sm' : 'text-muted'
                    }`}
                  >
                    {id === 'weight' ? 'Por peso' : 'Por monto'}
                  </button>
                )
              })}
            </div>

            <TextField
              aria-label={byWeight ? 'Peso en gramos' : 'Monto en soles'}
              value={valueText}
              onChange={(text) => setValueText(text.replace(/[^0-9.]/g, ''))}
              isInvalid={over}
              autoFocus
              className="relative"
            >
              {!byWeight && (
                <span className="pointer-events-none absolute top-1/2 left-5 -translate-y-1/2 text-[1.375rem] font-bold text-muted">
                  S/
                </span>
              )}
              <Input
                inputMode="decimal"
                placeholder="0"
                className={`h-16 rounded-full pr-16 text-[1.625rem] font-bold tabular-nums ${byWeight ? 'pl-6' : 'pl-15'}`}
              />
              {byWeight && (
                <span className="pointer-events-none absolute top-1/2 right-6 -translate-y-1/2 text-lg font-semibold text-muted">
                  g
                </span>
              )}
            </TextField>

            <div className="flex flex-wrap gap-1.5">
              {(byWeight ? WEIGHT_CHIPS : AMOUNT_CHIPS).map(([chipValue, label]) => {
                const selected = value === chipValue
                return (
                  <button
                    key={label}
                    type="button"
                    onClick={() => setValueText(String(chipValue))}
                    className={`h-11 flex-1 rounded-full px-2.5 font-semibold whitespace-nowrap outline-none transition-[background-color,transform] duration-150 focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.96] ${
                      selected ? 'bg-foreground text-background' : 'bg-surface-tertiary text-foreground'
                    }`}
                  >
                    {label}
                  </button>
                )
              })}
            </div>

            <div className="flex items-baseline justify-between rounded-[1.25rem] bg-background px-4.5 py-3.5">
              <span className="font-semibold text-foreground/80 tabular-nums">{rounded > 0 ? formatWeight(rounded) : '—'}</span>
              <span className="text-[1.625rem] font-extrabold tabular-nums">
                {formatMoney(byWeight ? rounded * item.salePrice : value)}
              </span>
            </div>

            {over && <p className="font-medium text-danger">Solo quedan {formatWeight(available)}</p>}

            <Button type="submit" size="lg" fullWidth isDisabled={!valid}>
              {currentKilograms > 0 ? 'Actualizar' : 'Agregar al carrito'}
            </Button>
          </form>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
