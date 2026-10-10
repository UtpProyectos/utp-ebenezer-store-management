import { ApiClientError } from '@/shared/services/apiClient'

const NOT_ENOUGH_STOCK = 'Not enough stock for '

// Backend business messages (422) shown in plain Spanish.
const BUSINESS_MESSAGES: Record<string, string> = {
  'The sale has no changes': 'No cambiaste nada de la venta.',
  'A sale needs at least one product; cancel it instead': 'Si quitas todos los productos, mejor elimina la venta.',
  'The sale is cancelled': 'Esta venta ya fue eliminada.',
}

/** Message for a failed sale, consumption, edit or cancellation. */
export function saleErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    const message = error.payload?.message ?? ''
    if (message.startsWith(NOT_ENOUGH_STOCK)) {
      return `Ya no hay stock suficiente de ${message.slice(NOT_ENOUGH_STOCK.length)}. Revisa la cantidad.`
    }
    if (BUSINESS_MESSAGES[message]) return BUSINESS_MESSAGES[message]
    if (error.status === 404) return 'Ya no está disponible. Recarga la pantalla.'
    return 'No se pudo guardar. Intenta de nuevo.'
  }
  return 'No hay conexión con el servidor.'
}
