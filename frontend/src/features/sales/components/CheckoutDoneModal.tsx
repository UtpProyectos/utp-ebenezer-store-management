import { Button, Modal } from '@heroui/react'
import CircleCheckFill from '@gravity-ui/icons/CircleCheckFill'
import { formatMoney } from '@/shared/utils/money'
import type { CheckoutResult } from '../hooks/useCheckout'

interface CheckoutDoneModalProps {
  result: CheckoutResult
  onNewSale: () => void
}

function describe(result: CheckoutResult): { title: string; message: string } {
  if (result.kind === 'sale') {
    const change = result.change > 0 ? ` · vuelto ${formatMoney(result.change)}` : ''
    return {
      title: '¡Venta cobrada!',
      message: `Venta #${result.sale.id} por ${formatMoney(result.sale.total)}${change}. El stock ya se actualizó.`,
    }
  }
  return {
    title: 'Consumo registrado',
    message: `Se descontaron ${result.itemCount} unidades del stock. No se registró ingreso en caja (costo ${formatMoney(result.cost)}).`,
  }
}

export function CheckoutDoneModal({ result, onNewSale }: CheckoutDoneModalProps) {
  const { title, message } = describe(result)

  return (
    <Modal.Backdrop isOpen onOpenChange={(open) => !open && onNewSale()}>
      <Modal.Container size="sm">
        <Modal.Dialog className="flex flex-col items-center gap-3 rounded-[1.75rem] px-6 pt-9 pb-6 text-center">
          <span className="grid size-17 place-items-center rounded-full bg-success-soft text-success-soft-foreground">
            <CircleCheckFill aria-hidden="true" className="size-10" />
          </span>
          <Modal.Heading className="text-[1.375rem] font-bold">{title}</Modal.Heading>
          <p className="leading-normal font-medium text-muted">{message}</p>
          <Button size="lg" fullWidth className="mt-2" onPress={onNewSale} autoFocus>
            {result.kind === 'sale' ? 'Nueva venta' : 'Listo'}
          </Button>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
