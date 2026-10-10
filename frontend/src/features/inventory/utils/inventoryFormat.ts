import type { ProductStockResponse } from '../types/inventory.types'

const UNIT_LABELS: Record<string, string> = { UND: 'unid.', KG: 'kg', G: 'g', L: 'L', ML: 'ml' }

// Display only (same as the prototype): closer dates are shown as "En N días".
const RELATIVE_DAYS_LIMIT = 10

const quantityFormatter = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 3 })
const dateFormatter = new Intl.DateTimeFormat('es-PE', { day: '2-digit', month: 'short', year: 'numeric' })

export function unitLabel(abbreviation: string): string {
  return UNIT_LABELS[abbreviation] ?? abbreviation.toLowerCase()
}

export function formatQuantity(quantity: number): string {
  return quantityFormatter.format(quantity)
}

export function formatStock(item: Pick<ProductStockResponse, 'currentStock' | 'baseUnitAbbreviation'>): string {
  return `${formatQuantity(item.currentStock)} ${unitLabel(item.baseUnitAbbreviation)}`
}

/** Parses an ISO date (yyyy-MM-dd) as a local date. */
function parseIsoDate(isoDate: string): Date {
  const [year, month, day] = isoDate.split('-').map(Number)
  return new Date(year, month - 1, day)
}

export function daysUntil(isoDate: string): number {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return Math.round((parseIsoDate(isoDate).getTime() - today.getTime()) / 86_400_000)
}

export function formatDate(isoDate: string): string {
  return dateFormatter.format(parseIsoDate(isoDate)).replace('.', '')
}

export type ExpirationTone = 'expired' | 'soon' | 'later' | 'none'

export function describeExpiration(isoDate: string | null): { label: string; tone: ExpirationTone } {
  if (!isoDate) return { label: 'No vence', tone: 'none' }
  const days = daysUntil(isoDate)
  if (days < 0) return { label: 'Ya venció', tone: 'expired' }
  if (days === 0) return { label: 'Vence hoy', tone: 'soon' }
  if (days <= RELATIVE_DAYS_LIMIT) return { label: `En ${days} ${days === 1 ? 'día' : 'días'}`, tone: 'soon' }
  return { label: formatDate(isoDate), tone: 'later' }
}

export function matchesSearch(item: ProductStockResponse, query: string): boolean {
  const normalized = query.trim().toLowerCase()
  if (!normalized) return true
  return item.productName.toLowerCase().includes(normalized) || (item.barcode ?? '').includes(normalized)
}
