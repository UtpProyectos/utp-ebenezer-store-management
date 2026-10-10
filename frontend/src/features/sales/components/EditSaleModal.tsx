import { useState } from 'react'
import { Button, Input, Label, Modal, TextField } from '@heroui/react'
import ClockArrowRotateLeft from '@gravity-ui/icons/ClockArrowRotateLeft'
import Minus from '@gravity-ui/icons/Minus'
import Plus from '@gravity-ui/icons/Plus'
import { formatMoney } from '@/shared/utils/money'
import { useSaleCorrection } from '../hooks/useSaleCorrection'
import type { PaymentMethod, SaleDetailResponse, SaleResponse } from '../types/sale.types'
import { PAYMENT_METHOD_LABELS, describeItems, formatTime, soldLines } from '../utils/saleFormat'

interface EditSaleModalProps {
  sale: SaleResponse
  onClose: () => void
  onSaved: () => void
}

const EDITABLE_METHODS: PaymentMethod[] = ['CASH', 'YAPE_PLIN', 'CARD']

const STEPPER_BUTTON =
  'grid h-full w-9 place-items-center bg-surface-secondary outline-none transition-colors duration-150 hover:bg-surface-tertiary focus-visible:bg-surface-tertiary'

// Units sold one by one use a stepper; weights and volumes take decimals.
function isCounted(detail: SaleDetailResponse) {
  return detail.unitOfMeasureAbbreviation === 'UND'
}

export function EditSaleModal({ sale, onClose, onSaved }: EditSaleModalProps) {
  const { update, isPending, error } = useSaleCorrection()
  const lines = soldLines(sale)
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(sale.paymentMethod ?? 'CASH')
  const [quantities, setQuantities] = useState<Record<number, string>>(() =>
    Object.fromEntries(lines.map((detail) => [detail.id, String(detail.quantity)])),
  )
  const [reason, setReason] = useState('')
  const [tried, setTried] = useState(false)

  const quantityOf = (detail: SaleDetailResponse) => Number(quantities[detail.id]) || 0
  const setQuantity = (detail: SaleDetailResponse, value: string) =>
    setQuantities((current) => ({ ...current, [detail.id]: value }))

  const allRemoved = lines.every((detail) => quantityOf(detail) === 0)
  const missingReason = reason.trim().length === 0
  const validationError = allRemoved
    ? 'Si quitas todos los productos, mejor elimina la venta.'
    : missingReason
      ? 'Escribe el motivo del cambio.'
      : null

  const handleSave = async () => {
    setTried(true)
    if (validationError || isPending) return
    // Only the lines that changed; an empty list just changes the payment method.
    const details = lines
      .filter((detail) => quantityOf(detail) !== detail.quantity)
      .map((detail) => ({ saleDetailId: detail.id, quantity: quantityOf(detail) }))
    if (await update(sale.id, { paymentMethod, reason: reason.trim(), details })) onSaved()
  }

  return (
    <Modal.Backdrop isOpen isDismissable={!isPending} onOpenChange={(open) => !open && onClose()}>
      <Modal.Container size="md">
        <Modal.Dialog className="flex flex-col gap-4 rounded-[2rem]">
          <Modal.CloseTrigger aria-label="Cerrar" />
          <Modal.Header className="flex flex-col gap-1">
            <Modal.Heading className="text-[1.375rem] font-bold">Editar venta #{sale.id}</Modal.Heading>
            <p className="text-muted">
              {formatTime(sale.saleDate)} · {describeItems(sale)}
            </p>
          </Modal.Header>

          <Modal.Body className="flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <span className="font-semibold">Método de pago</span>
              <div role="radiogroup" aria-label="Método de pago" className="flex flex-wrap gap-1.5">
                {EDITABLE_METHODS.map((method) => {
                  const selected = method === paymentMethod
                  return (
                    <button
                      key={method}
                      type="button"
                      role="radio"
                      aria-checked={selected}
                      onClick={() => setPaymentMethod(method)}
                      className={`h-11.5 rounded-full px-4 font-semibold outline-none transition-colors duration-150 focus-visible:ring-2 focus-visible:ring-focus ${
                        selected ? 'bg-foreground text-background' : 'bg-surface-tertiary text-foreground'
                      }`}
                    >
                      {PAYMENT_METHOD_LABELS[method]}
                    </button>
                  )
                })}
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <span className="font-semibold">Productos</span>
              <ul className="flex flex-col gap-1.5">
                {lines.map((detail) => {
                  const quantity = quantityOf(detail)
                  return (
                    <li key={detail.id} className="flex items-center gap-3 rounded-[1.25rem] bg-background py-2 pr-2 pl-4">
                      <span className="flex min-w-0 flex-1 flex-col">
                        <span className={`truncate font-semibold ${quantity === 0 ? 'text-muted line-through' : ''}`}>
                          {detail.productName}
                        </span>
                        <span className="text-sm text-muted tabular-nums">
                          {formatMoney(detail.unitPrice)} / {detail.unitOfMeasureAbbreviation.toLowerCase()}
                        </span>
                      </span>
                      {isCounted(detail) ? (
                        <div className="flex h-9 w-31 shrink-0 items-center overflow-hidden rounded-xl border border-field-border">
                          <button type="button" aria-label="Menos" className={STEPPER_BUTTON} onClick={() => setQuantity(detail, String(Math.max(0, quantity - 1)))}>
                            <Minus aria-hidden="true" className="size-4" />
                          </button>
                          <input
                            aria-label={`Cantidad de ${detail.productName}`}
                            value={quantities[detail.id]}
                            inputMode="numeric"
                            onChange={(event) => setQuantity(detail, event.target.value.replace(/[^0-9]/g, ''))}
                            className="h-full min-w-0 flex-1 bg-surface text-center font-bold tabular-nums outline-none"
                          />
                          <button type="button" aria-label="Más" className={STEPPER_BUTTON} onClick={() => setQuantity(detail, String(quantity + 1))}>
                            <Plus aria-hidden="true" className="size-4" />
                          </button>
                        </div>
                      ) : (
                        <TextField
                          aria-label={`Cantidad de ${detail.productName} en ${detail.unitOfMeasureAbbreviation}`}
                          value={quantities[detail.id]}
                          onChange={(value) => setQuantity(detail, value.replace(/[^0-9.]/g, ''))}
                          className="w-31 shrink-0"
                        >
                          <Input inputMode="decimal" className="h-9 rounded-full text-center font-bold tabular-nums" />
                        </TextField>
                      )}
                    </li>
                  )
                })}
              </ul>
              <p className="text-sm text-muted">Pon 0 para quitar un producto. El total se recalcula al guardar.</p>
            </div>

            <TextField value={reason} onChange={setReason} isInvalid={tried && missingReason}>
              <Label className="font-semibold">Motivo del cambio</Label>
              <Input placeholder="Ej. me equivoqué de cantidad" className="h-13 rounded-full" />
            </TextField>

            {tried && validationError && <p className="text-sm text-danger">{validationError}</p>}

            <p className="flex items-center gap-2 text-sm text-muted">
              <ClockArrowRotateLeft aria-hidden="true" className="size-4" />
              Queda registrado en movimientos con tu nombre y la hora.
            </p>

            {error && (
              <p role="alert" className="rounded-[1.25rem] bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                {error}
              </p>
            )}
          </Modal.Body>

          <Modal.Footer className="flex flex-wrap gap-2.5">
            <Button variant="tertiary" size="lg" className="flex-[1_1_7.5rem]" isDisabled={isPending} onPress={onClose}>
              Cancelar
            </Button>
            <Button size="lg" className="flex-[2_1_11.25rem]" isPending={isPending} onPress={handleSave}>
              {isPending ? 'Guardando…' : 'Guardar cambios'}
            </Button>
          </Modal.Footer>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
