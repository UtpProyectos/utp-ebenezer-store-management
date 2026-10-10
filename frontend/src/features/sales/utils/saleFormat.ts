import type { PaymentMethod, SaleResponse } from '../types/sale.types'

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  CASH: 'Efectivo',
  YAPE_PLIN: 'Yape / Plin',
  CARD: 'Tarjeta',
  OTHER: 'Otro',
}

const timeFormatter = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false })
const quantityFormatter = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 3 })

/** "14:32" from an ISO date-time. */
export function formatTime(isoDateTime: string): string {
  return timeFormatter.format(new Date(isoDateTime))
}

/** Lines still in the sale (an edit can leave a line at zero). */
export function soldLines(sale: SaleResponse) {
  return sale.details.filter((detail) => detail.quantity > 0)
}

/** "Galletas ×3, Arroz a granel 0.5 kg". */
export function describeItems(sale: SaleResponse): string {
  return soldLines(sale)
    .map((detail) =>
      detail.unitOfMeasureAbbreviation === 'UND'
        ? `${detail.productName} ×${quantityFormatter.format(detail.quantity)}`
        : `${detail.productName} ${quantityFormatter.format(detail.quantity)} ${detail.unitOfMeasureAbbreviation.toLowerCase()}`,
    )
    .join(', ')
}
