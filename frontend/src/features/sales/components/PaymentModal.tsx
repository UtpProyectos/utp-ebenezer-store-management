import { useState } from 'react'
import { Button, Input, Label, Modal, TextField } from '@heroui/react'
import CreditCard from '@gravity-ui/icons/CreditCard'
import QrCode from '@gravity-ui/icons/QrCode'
import Wallet from '@gravity-ui/icons/Wallet'
import { formatMoney } from '@/shared/utils/money'
import type { PaymentMethod } from '../types/sale.types'

interface PaymentModalProps {
  total: number
  isPending: boolean
  error: string | null
  onConfirm: (paymentMethod: PaymentMethod, received: number) => void
  onClose: () => void
}

const METHODS: { id: PaymentMethod; label: string; icon: typeof Wallet }[] = [
  { id: 'CASH', label: 'Efectivo', icon: Wallet },
  { id: 'YAPE_PLIN', label: 'Yape / Plin', icon: QrCode },
  { id: 'CARD', label: 'Tarjeta', icon: CreditCard },
]

const QUICK_AMOUNTS = [10, 20, 50, 100]

// Received money and change only help the cashier: they are not stored.
export function PaymentModal({ total, isPending, error, onConfirm, onClose }: PaymentModalProps) {
  const [method, setMethod] = useState<PaymentMethod>('CASH')
  const [receivedText, setReceivedText] = useState('')

  const cash = method === 'CASH'
  const received = Number(receivedText) || 0
  const short = cash && received > 0 && received < total
  const quickCash = QUICK_AMOUNTS.filter((amount) => amount >= total).slice(0, 3)

  return (
    <Modal.Backdrop isOpen isDismissable={!isPending} onOpenChange={(open) => !open && onClose()}>
      <Modal.Container size="sm">
        <Modal.Dialog className="flex flex-col gap-4 rounded-[1.75rem]">
          <Modal.CloseTrigger aria-label="Cerrar" />
          <Modal.Header>
            <Modal.Heading className="text-xl font-bold">Cobrar venta</Modal.Heading>
          </Modal.Header>

          <Modal.Body className="flex flex-col gap-4">
            <div className="flex items-baseline justify-between rounded-[1.125rem] bg-accent-soft px-4 py-3.5">
              <span className="font-semibold">Total a cobrar</span>
              <span className="text-[1.875rem] font-extrabold tabular-nums">{formatMoney(total)}</span>
            </div>

            <div role="radiogroup" aria-label="Método de pago" className="grid grid-cols-3 gap-2">
              {METHODS.map(({ id, label, icon: Icon }) => {
                const selected = id === method
                return (
                  <button
                    key={id}
                    type="button"
                    role="radio"
                    aria-checked={selected}
                    onClick={() => setMethod(id)}
                    className={`flex flex-col items-center gap-1.5 rounded-[1.125rem] border-[1.5px] px-1.5 py-3 font-semibold outline-none transition-[background-color,border-color,transform] duration-150 focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.96] ${
                      selected ? 'border-accent bg-accent-soft' : 'border-border bg-surface hover:bg-surface-secondary'
                    }`}
                  >
                    <Icon aria-hidden="true" className={`size-5.5 ${selected ? 'text-foreground' : 'text-muted'}`} />
                    {label}
                  </button>
                )
              })}
            </div>

            {cash && (
              <>
                <div className="grid grid-cols-2 gap-3">
                  <TextField value={receivedText} onChange={(value) => setReceivedText(value.replace(/[^0-9.]/g, ''))}>
                    <Label className="font-semibold">Recibido (S/)</Label>
                    <Input inputMode="decimal" placeholder="0.00" className="h-13 rounded-full text-lg font-bold tabular-nums" />
                  </TextField>
                  <div className="flex flex-col gap-1.5">
                    <span className="font-semibold text-muted">Vuelto</span>
                    <span
                      className={`flex h-13 items-center rounded-[0.875rem] bg-background px-3 text-lg font-bold tabular-nums ${
                        short ? 'text-danger' : ''
                      }`}
                    >
                      {received > 0 ? formatMoney(Math.max(0, received - total)) : '—'}
                    </span>
                  </div>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {quickCash.map((amount) => (
                    <Button key={amount} variant="outline" size="sm" onPress={() => setReceivedText(String(amount))}>
                      S/ {amount}
                    </Button>
                  ))}
                  <Button variant="outline" size="sm" onPress={() => setReceivedText(total.toFixed(2))}>
                    Monto exacto
                  </Button>
                </div>
              </>
            )}

            {error && (
              <p role="alert" className="rounded-[1.25rem] bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                {error}
              </p>
            )}
          </Modal.Body>

          <Modal.Footer>
            <Button
              size="lg"
              fullWidth
              isDisabled={short}
              isPending={isPending}
              onPress={() => onConfirm(method, cash ? received : total)}
            >
              {isPending ? 'Procesando…' : 'Confirmar cobro'}
            </Button>
          </Modal.Footer>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
