const moneyFormatter = new Intl.NumberFormat('es-PE', {
  style: 'currency',
  currency: 'PEN',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/** Formats an amount in Peruvian soles: `S/ 12.50`. */
export function formatMoney(amount: number): string {
  return moneyFormatter.format(Number.isFinite(amount) ? amount : 0)
}
