import { useState } from 'react'
import { Button, Input, Label, Modal, TextField } from '@heroui/react'
import ClockArrowRotateLeft from '@gravity-ui/icons/ClockArrowRotateLeft'
import TriangleExclamation from '@gravity-ui/icons/TriangleExclamation'
import { formatMoney } from '@/shared/utils/money'
import { useSaleCorrection } from '../hooks/useSaleCorrection'
import type { SaleResponse } from '../types/sale.types'
import { describeItems, formatTime } from '../utils/saleFormat'

interface CancelSaleModalProps {
  sale: SaleResponse
  onClose: () => void
  onCancelled: () => void
}

// "Eliminar" in the prototype: the sale is cancelled (never deleted) and its stock comes back.
export function CancelSaleModal({ sale, onClose, onCancelled }: CancelSaleModalProps) {
  const { cancel, isPending, error } = useSaleCorrection()
  const [reason, setReason] = useState('')
  const [tried, setTried] = useState(false)
  const missingReason = reason.trim().length === 0

  const handleConfirm = async () => {
    setTried(true)
    if (missingReason || isPending) return
    if (await cancel(sale.id, { reason: reason.trim() })) onCancelled()
  }

  return (
    <Modal.Backdrop isOpen isDismissable={!isPending} onOpenChange={(open) => !open && onClose()}>
      <Modal.Container size="md">
        <Modal.Dialog className="flex flex-col gap-4 rounded-[2rem]">
          <Modal.CloseTrigger aria-label="Cerrar" />
          <Modal.Header className="flex flex-col gap-1">
            <Modal.Heading className="text-[1.375rem] font-bold">¿Eliminar la venta #{sale.id}?</Modal.Heading>
            <p className="text-muted">
              {formatTime(sale.saleDate)} · {describeItems(sale)} · {formatMoney(sale.total)}
            </p>
          </Modal.Header>

          <Modal.Body className="flex flex-col gap-4">
            <p className="flex gap-2.5 rounded-[1.25rem] bg-danger-soft px-4 py-3.5 font-medium text-danger-soft-foreground">
              <TriangleExclamation aria-hidden="true" className="mt-0.5 size-5 shrink-0" />
              La venta se quitará del total de caja y los productos vuelven al inventario.
            </p>

            <TextField value={reason} onChange={setReason} isInvalid={tried && missingReason} autoFocus>
              <Label className="font-semibold">Motivo del cambio</Label>
              <Input placeholder="Ej. el cliente devolvió los productos" className="h-13 rounded-full" />
            </TextField>
            {tried && missingReason && <p className="text-sm text-danger">Escribe por qué eliminas esta venta.</p>}

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
            <Button variant="danger" size="lg" className="flex-[2_1_11.25rem]" isPending={isPending} onPress={handleConfirm}>
              {isPending ? 'Eliminando…' : 'Eliminar venta'}
            </Button>
          </Modal.Footer>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
