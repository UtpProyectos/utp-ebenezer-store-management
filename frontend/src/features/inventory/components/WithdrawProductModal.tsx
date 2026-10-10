import { useState } from 'react'
import { Button, Input, Label, Modal, TextField } from '@heroui/react'
import ArrowUturnCcwLeft from '@gravity-ui/icons/ArrowUturnCcwLeft'
import ClockArrowRotateLeft from '@gravity-ui/icons/ClockArrowRotateLeft'
import TrashBin from '@gravity-ui/icons/TrashBin'
import { formatMoney } from '@/shared/utils/money'
import { useWithdrawProduct } from '../hooks/useWithdrawProduct'
import type { ProductStockResponse, WithdrawalType } from '../types/inventory.types'
import { formatDate, formatQuantity, unitLabel } from '../utils/inventoryFormat'

interface WithdrawProductModalProps {
  item: ProductStockResponse
  onClose: () => void
  onWithdrawn: (message: string) => void
}

export function WithdrawProductModal({ item, onClose, onWithdrawn }: WithdrawProductModalProps) {
  const { submit, isPending, error } = useWithdrawProduct()
  const [movementType, setMovementType] = useState<WithdrawalType>('WASTE')
  const [quantityText, setQuantityText] = useState(String(item.expiredQuantity || item.currentStock))

  const quantity = Number(quantityText) || 0
  const valid = quantity > 0 && quantity <= item.currentStock
  const unit = unitLabel(item.baseUnitAbbreviation)
  const loss = item.lastUnitCost !== null ? formatMoney(quantity * item.lastUnitCost) : null

  const options: { id: WithdrawalType; label: string; hint: string; icon: typeof TrashBin }[] = [
    {
      id: 'WASTE',
      label: 'Desechar',
      hint: loss ? `Se registra como merma · pérdida aprox. ${loss}` : 'Se registra como merma',
      icon: TrashBin,
    },
    {
      id: 'RETURN',
      label: 'Devolver al proveedor',
      hint: `${item.lastSupplierName ?? 'Proveedor'} · pide cambio o nota de crédito`,
      icon: ArrowUturnCcwLeft,
    },
  ]

  const handleConfirm = async () => {
    if (!valid || isPending) return
    const done = await submit(item, movementType, quantity)
    if (done) {
      const action = movementType === 'WASTE' ? 'desechó' : 'devolvió'
      onWithdrawn(`Se ${action} ${formatQuantity(quantity)} ${unit} de ${item.productName}.`)
    }
  }

  return (
    <Modal.Backdrop isOpen isDismissable={!isPending} onOpenChange={(open) => !open && onClose()}>
      <Modal.Container size="sm">
        <Modal.Dialog className="flex flex-col gap-4 rounded-3xl">
          <Modal.CloseTrigger aria-label="Cerrar" />
          <Modal.Header className="flex flex-col gap-1">
            <Modal.Heading className="text-xl font-bold">Retirar {item.productName}</Modal.Heading>
            <p className="text-muted">
              {item.nextExpirationDate ? `Venció el ${formatDate(item.nextExpirationDate)}. ` : ''}
              Sácalo del estante para que no se venda.
            </p>
          </Modal.Header>

          <Modal.Body className="flex flex-col gap-4">
            <div role="radiogroup" aria-label="Qué hacer con el producto" className="flex flex-col gap-2">
              {options.map(({ id, label, hint, icon: Icon }) => {
                const selected = id === movementType
                return (
                  <button
                    key={id}
                    type="button"
                    role="radio"
                    aria-checked={selected}
                    onClick={() => setMovementType(id)}
                    className={`flex items-center gap-3.5 rounded-[1.25rem] border-2 px-4 py-3.5 text-left outline-none transition-[background-color,border-color] duration-150 focus-visible:ring-2 focus-visible:ring-focus ${
                      selected ? 'border-accent bg-accent-soft' : 'border-separator bg-surface hover:bg-surface-secondary'
                    }`}
                  >
                    <span
                      className={`grid size-10 shrink-0 place-items-center rounded-full bg-surface ${
                        selected ? 'text-accent' : 'text-muted'
                      }`}
                    >
                      <Icon aria-hidden="true" className="size-5" />
                    </span>
                    <span className="flex min-w-0 flex-col gap-0.5">
                      <span className="font-semibold">{label}</span>
                      <span className="text-sm text-muted">{hint}</span>
                    </span>
                  </button>
                )
              })}
            </div>

            <TextField
              value={quantityText}
              onChange={(value) => setQuantityText(value.replace(/[^0-9.]/g, ''))}
              isInvalid={!valid}
              className="flex flex-row items-center justify-between gap-3"
            >
              <Label className="font-semibold">Cantidad a retirar ({unit})</Label>
              <Input inputMode="decimal" className="h-12 w-28 rounded-full text-center text-lg font-bold tabular-nums" />
            </TextField>

            <p className="flex items-center gap-2 text-sm text-muted">
              <ClockArrowRotateLeft aria-hidden="true" className="size-4" />
              Se descuenta del stock y queda en movimientos.
            </p>

            {error && (
              <p role="alert" className="rounded-[1.25rem] bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                {error}
              </p>
            )}
          </Modal.Body>

          <Modal.Footer>
            <Button variant="danger" size="lg" fullWidth isDisabled={!valid} isPending={isPending} onPress={handleConfirm}>
              {isPending ? 'Retirando…' : `Retirar ${quantity > 0 ? formatQuantity(quantity) : ''} ${unit}`}
            </Button>
          </Modal.Footer>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
