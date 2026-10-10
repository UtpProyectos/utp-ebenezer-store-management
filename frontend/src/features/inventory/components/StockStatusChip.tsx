import { Chip } from '@heroui/react'
import type { StockStatus } from '../types/inventory.types'

const STATUS_CHIPS: Record<Exclude<StockStatus, 'OK'>, { label: string; className: string }> = {
  LOW: { label: 'Quedan pocos', className: 'bg-warning-soft text-warning-soft-foreground' },
  CRITICAL: { label: 'Crítico', className: 'bg-danger-soft text-danger-soft-foreground' },
  EXPIRING_SOON: { label: 'Vence pronto', className: 'bg-info-soft text-info-soft-foreground' },
  EXPIRED: { label: 'Vencido', className: 'bg-danger text-danger-foreground' },
}

// Products in good shape ("OK") show no chip, same as the prototype.
export function StockStatusChip({ status }: { status: StockStatus }) {
  if (status === 'OK') return null
  const { label, className } = STATUS_CHIPS[status]

  return (
    <Chip className={`gap-2 whitespace-nowrap font-semibold ${className}`}>
      <span aria-hidden="true" className="size-2 rounded-full bg-current" />
      <Chip.Label>{label}</Chip.Label>
    </Chip>
  )
}
