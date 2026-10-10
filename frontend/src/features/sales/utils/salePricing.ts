import type { ProductStockResponse } from '@/features/inventory'
import type { PromotionResponse } from '@/features/promotions/types/promotion.types'
import { formatMoney } from '@/shared/utils/money'

// Preview only: the backend recalculates every amount when the sale is registered.

// Conversion factors of the base units (database/002_seed.sql). Unknown units are treated as 1.
const UNIT_FACTORS: Record<string, number> = { KG: 1, G: 0.001, L: 1, ML: 0.001, UND: 1 }

const FRACTION_LABELS: Record<number, string> = { 0.25: '¼', 0.5: '½', 0.75: '¾' }

function round(value: number, decimals: number): number {
  const factor = 10 ** decimals
  return Math.round(value * factor) / factor
}

function factor(abbreviation: string): number {
  return UNIT_FACTORS[abbreviation] ?? 1
}

/** Stock that can be sold or consumed: expired lots never leave through a sale. */
export function sellableStock(item: ProductStockResponse): number {
  return Math.max(0, round(item.currentStock - item.expiredQuantity, 3))
}

/** Products controlled by weight are added through the weight dialog. */
export function isWeighed(item: ProductStockResponse): boolean {
  return item.baseUnitType === 'WEIGHT'
}

/** Promotion quantity converted to the product base unit. */
function promotionBaseQuantity(item: ProductStockResponse, promotion: PromotionResponse): number {
  return round(
    (promotion.promotionQuantity * factor(promotion.unitOfMeasureAbbreviation)) / factor(item.baseUnitAbbreviation),
    3,
  )
}

/** Amount of a line in soles, with the promotion applied the same way as the backend. */
export function lineTotal(item: ProductStockResponse, quantity: number, promotion?: PromotionResponse): number {
  const gross = round(quantity * item.salePrice, 2)
  if (!promotion) return gross
  const groupSize = promotionBaseQuantity(item, promotion)
  if (groupSize <= 0) return gross
  let groups = Math.floor(round(quantity / groupSize, 6))
  if (!promotion.repeatable) groups = Math.min(groups, 1)
  if (groups === 0) return gross
  const promotional = round(
    groups * promotion.promotionalPrice + (quantity - groups * groupSize) * item.salePrice,
    2,
  )
  return Math.min(gross, promotional)
}

/** Weight in kilograms as the prototype shows it: ¼ kg, ½ kg, 750 g, 1.25 kg. */
export function formatWeight(kilograms: number): string {
  const rounded = round(kilograms, 3)
  if (FRACTION_LABELS[rounded]) return `${FRACTION_LABELS[rounded]} kg`
  if (rounded < 1) return `${Math.round(rounded * 1000)} g`
  return `${rounded} kg`
}

/** Short quantity for the add button of a weighed product: ¼, ½, 750g, 1.5. */
export function formatWeightShort(kilograms: number): string {
  const rounded = round(kilograms, 3)
  if (FRACTION_LABELS[rounded]) return FRACTION_LABELS[rounded]
  if (rounded < 1) return `${Math.round(rounded * 1000)}g`
  return String(round(rounded, 2))
}

/** "3 × S/ 2.00" or "½ kg × S/ 2.00". */
export function promotionLabel(item: ProductStockResponse, promotion: PromotionResponse): string {
  const size = promotionBaseQuantity(item, promotion)
  const quantity = isWeighed(item) ? formatWeight(size) : String(size)
  return `${quantity} × ${formatMoney(promotion.promotionalPrice)}`
}

/** Stock left, as shown on the product tile. */
export function formatSellable(item: ProductStockResponse): string {
  const stock = sellableStock(item)
  return isWeighed(item) ? formatWeight(stock) : `${stock} unid.`
}
