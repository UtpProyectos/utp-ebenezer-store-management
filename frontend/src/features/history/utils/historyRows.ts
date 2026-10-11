import type { InternalConsumptionResponse } from '@/features/consumption/types/consumption.types'
import { formatQuantity, unitLabel } from '@/features/inventory'
import type { InventoryMovementResponse, InventoryMovementType } from '@/features/inventory/types/inventory.types'
import type { PurchaseResponse } from '@/features/purchases/types/purchase.types'
import { describeItems, PAYMENT_METHOD_LABELS, type SaleResponse } from '@/features/sales'
import { formatMoney } from '@/shared/utils/money'
import type { HistoryRow, HistoryTone } from '../types/history.types'

const MOVEMENT_LABELS: Record<InventoryMovementType, { label: string; tone: HistoryTone }> = {
  PURCHASE: { label: 'Entrada', tone: 'success' },
  SALE: { label: 'Venta', tone: 'warning' },
  INTERNAL_CONSUMPTION: { label: 'Consumo interno', tone: 'info' },
  WASTE: { label: 'Merma', tone: 'danger' },
  ADJUSTMENT_IN: { label: 'Ajuste de entrada', tone: 'success' },
  ADJUSTMENT_OUT: { label: 'Ajuste de salida', tone: 'danger' },
  RETURN: { label: 'Devolución', tone: 'danger' },
  REVERSAL: { label: 'Reversa', tone: 'muted' },
}

const timeFormatter = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false })
const dayFormatter = new Intl.DateTimeFormat('es-PE', { day: 'numeric', month: 'short' })

/** "Hoy 11:42" or "28 sep 11:42", as in the prototype. */
export function formatHistoryDate(isoDateTime: string): string {
  const date = new Date(isoDateTime)
  const isToday = date.toDateString() === new Date().toDateString()
  return `${isToday ? 'Hoy' : dayFormatter.format(date).replace('.', '')} ${timeFormatter.format(date)}`
}

function quantityWithUnit(quantity: number, abbreviation: string): string {
  return `${formatQuantity(quantity)} ${unitLabel(abbreviation)}`
}

export function saleRows(sales: SaleResponse[]): HistoryRow[] {
  return sales.map((sale) => {
    const cancelled = sale.status === 'CANCELLED'
    return {
      key: `sale-${sale.id}`,
      date: sale.saleDate,
      main: `#${sale.id}`,
      sub: cancelled && sale.cancellationReason ? `Motivo: ${sale.cancellationReason}` : describeItems(sale),
      tag: cancelled ? 'Eliminada' : sale.paymentMethod ? PAYMENT_METHOD_LABELS[sale.paymentMethod] : 'Sin método',
      tagTone: cancelled ? 'danger' : 'muted',
      amount: formatMoney(sale.total),
      amountTone: cancelled ? 'muted' : 'default',
      value: cancelled ? 0 : sale.total,
    }
  })
}

/** One row per product received. */
export function purchaseRows(purchases: PurchaseResponse[]): HistoryRow[] {
  return purchases.flatMap((purchase) => {
    const cancelled = purchase.status === 'CANCELLED'
    return purchase.details.map((detail) => ({
      key: `purchase-${detail.id}`,
      date: purchase.purchaseDate,
      main: detail.productName,
      sub: `${quantityWithUnit(detail.quantity, detail.unitOfMeasureAbbreviation)} · ${purchase.userName}`,
      tag: cancelled ? 'Anulada' : (purchase.supplierName ?? 'Sin proveedor'),
      tagTone: cancelled ? 'danger' : 'muted',
      amount: formatMoney(detail.subtotal),
      amountTone: cancelled ? 'muted' : 'default',
      value: cancelled ? 0 : detail.subtotal,
    }))
  })
}

export function movementRows(movements: InventoryMovementResponse[]): HistoryRow[] {
  return movements.map((movement) => {
    const { label, tone } = MOVEMENT_LABELS[movement.movementType]
    const sign = movement.baseQuantity > 0 ? '+' : '−'
    return {
      key: `movement-${movement.id}`,
      date: movement.movementDate,
      main: movement.productName,
      sub: [movement.reason, movement.userName].filter(Boolean).join(' · '),
      tag: label,
      tagTone: tone,
      amount: `${sign}${quantityWithUnit(Math.abs(movement.baseQuantity), movement.baseUnitAbbreviation)}`,
      amountTone: movement.baseQuantity > 0 ? 'success' : 'danger',
      value: 0,
    }
  })
}

/** One row per product used. The cost is not stored for consumptions, so the quantity is shown. */
export function consumptionRows(consumptions: InternalConsumptionResponse[]): HistoryRow[] {
  return consumptions.flatMap((consumption) =>
    consumption.details.map((detail) => ({
      key: `consumption-${detail.id}`,
      date: consumption.consumptionDate,
      main: detail.productName,
      sub: consumption.userName,
      tag: consumption.reason ?? 'Sin motivo',
      tagTone: 'muted' as const,
      amount: `−${quantityWithUnit(detail.quantity, detail.unitOfMeasureAbbreviation)}`,
      amountTone: 'danger' as const,
      value: 0,
    })),
  )
}
