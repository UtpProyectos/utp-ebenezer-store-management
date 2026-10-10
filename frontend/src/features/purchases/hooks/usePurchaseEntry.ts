import { useState } from 'react'
import { formatQuantity, unitLabel, type ProductStockResponse } from '@/features/inventory'
import type { Supplier } from '@/features/suppliers/types/supplier.types'
import { ApiClientError } from '@/shared/services/apiClient'
import { purchaseApi } from '../services/purchaseApi'
import type { PurchaseRequest } from '../types/purchase.types'

/** UI-only grouping: the backend always receives the quantity in the product base unit. */
export type QuantityGroup = 'unit' | 'dozen' | 'box'

const DOZEN = 12
// Suggested price from the prototype: unit cost + 30 %, rounded up to S/ 0.10.
const SUGGESTED_MARKUP = 1.3
const LOW_MARGIN_PERCENT = 15

export interface SavedEntry {
  productName: string
  quantityLabel: string
  stockLabel: string
}

function toNumber(text: string): number {
  const value = Number(text)
  return Number.isFinite(value) ? value : 0
}

function round(value: number, decimals: number): number {
  const factor = 10 ** decimals
  return Math.round(value * factor) / factor
}

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    const fieldErrors = error.payload?.fieldErrors
    if (fieldErrors && Object.keys(fieldErrors).length > 0) return 'Revisa los datos del ingreso.'
    return error.payload?.message ?? 'No se pudo guardar el ingreso. Intenta de nuevo.'
  }
  return 'No hay conexión con el servidor.'
}

function defaultSupplierId(product: ProductStockResponse, suppliers: Supplier[]): string {
  const supplier =
    suppliers.find((option) => option.products.some((linked) => linked.id === product.productId)) ??
    suppliers.find((option) => option.name === product.lastSupplierName)
  return supplier ? String(supplier.id) : ''
}

export function usePurchaseEntry(items: ProductStockResponse[], suppliers: Supplier[], initialProductId: number | null) {
  const [productId, setProductId] = useState<number | null>(initialProductId)
  const [quantityText, setQuantityText] = useState('')
  const [group, setGroup] = useState<QuantityGroup>('unit')
  const [unitsPerBoxText, setUnitsPerBoxText] = useState('')
  const [totalCostText, setTotalCostText] = useState('')
  // null = keep the product default (current sale price, current expiration habit, linked supplier).
  const [salePriceText, setSalePriceText] = useState<string | null>(null)
  const [noExpirationOverride, setNoExpirationOverride] = useState<boolean | null>(null)
  const [supplierIdOverride, setSupplierIdOverride] = useState<string | null>(null)
  const [lotCode, setLotCode] = useState('')
  const [expirationDate, setExpirationDate] = useState('')
  const [notes, setNotes] = useState('')
  const [tried, setTried] = useState(false)
  const [isPending, setIsPending] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [saved, setSaved] = useState<SavedEntry | null>(null)

  const product = items.find((item) => item.productId === productId) ?? null
  const countsUnits = product?.baseUnitType === 'UNIT'
  const unit = product ? unitLabel(product.baseUnitAbbreviation) : ''

  const quantity = toNumber(quantityText)
  const unitsPerBox = toNumber(unitsPerBoxText)
  const groupSize = !countsUnits || group === 'unit' ? 1 : group === 'dozen' ? DOZEN : unitsPerBox
  const baseQuantity = round(quantity * groupSize, 3)
  const totalCost = toNumber(totalCostText)
  const unitCost = baseQuantity > 0 && totalCost > 0 ? totalCost / baseQuantity : 0

  const salePrice = salePriceText ?? (product && product.salePrice > 0 ? product.salePrice.toFixed(2) : '')
  const price = toNumber(salePrice)
  const suggestedPrice = unitCost > 0 ? Math.ceil(unitCost * SUGGESTED_MARKUP * 10) / 10 : 0
  const gain = unitCost > 0 && price > 0 ? price - unitCost : null
  const marginPercent = gain !== null ? (gain / price) * 100 : 0

  // Only assume "no vence" when the product already has stock and none of it expires; new products start open.
  const noExpiration =
    noExpirationOverride ?? (product ? product.currentStock > 0 && product.nextExpirationDate === null : false)
  const supplierId = supplierIdOverride ?? (product ? defaultSupplierId(product, suppliers) : '')

  const errors = {
    product: !product,
    quantity: baseQuantity <= 0,
    unitsPerBox: countsUnits && group === 'box' && unitsPerBox <= 0,
    totalCost: totalCost <= 0,
    salePrice: price <= 0,
  }
  const validationMessage = errors.product
    ? 'Falta elegir el producto.'
    : errors.unitsPerBox
      ? 'Falta escribir cuántas unidades trae cada caja.'
      : errors.quantity
        ? 'Falta escribir la cantidad que llegó.'
        : errors.totalCost
          ? 'Falta escribir el costo total.'
          : errors.salePrice
            ? 'Falta el precio de venta.'
            : null

  const groupLabel = (() => {
    if (!product || quantity <= 0) return '—'
    const baseLabel = `${formatQuantity(baseQuantity)} ${unit}`
    if (!countsUnits || group === 'unit') return baseLabel
    if (group === 'dozen') return `${formatQuantity(quantity)} ${quantity === 1 ? 'docena' : 'docenas'} · ${baseLabel}`
    return `${formatQuantity(quantity)} ${quantity === 1 ? 'caja' : 'cajas'} × ${formatQuantity(unitsPerBox)} · ${baseLabel}`
  })()

  const selectProduct = (id: number | null) => {
    setProductId(id)
    setGroup('unit')
    setQuantityText('')
    setUnitsPerBoxText('')
    setTotalCostText('')
    setSalePriceText(null)
    setNoExpirationOverride(null)
    setSupplierIdOverride(null)
    setTried(false)
    setSubmitError(null)
  }

  const reset = () => {
    selectProduct(null)
    setLotCode('')
    setExpirationDate('')
    setNotes('')
    setSaved(null)
  }

  const submit = async () => {
    setTried(true)
    if (validationMessage || !product || isPending) return false

    const request: PurchaseRequest = {
      supplierId: supplierId ? Number(supplierId) : null,
      notes: notes.trim() || null,
      details: [
        {
          productId: product.productId,
          unitOfMeasureId: product.baseUnitId,
          quantity: baseQuantity,
          subtotal: round(totalCost, 2),
          salePrice: price > 0 && round(price, 2) !== product.salePrice ? round(price, 2) : null,
          lotCode: lotCode.trim() || null,
          expirationDate: noExpiration || !expirationDate ? null : expirationDate,
        },
      ],
    }

    setIsPending(true)
    setSubmitError(null)
    try {
      await purchaseApi.create(request)
      setSaved({
        productName: product.productName,
        quantityLabel: `${formatQuantity(baseQuantity)} ${unit}`,
        stockLabel: `${formatQuantity(round(product.currentStock + baseQuantity, 3))} ${unit}`,
      })
      return true
    } catch (caught) {
      setSubmitError(toErrorMessage(caught))
      return false
    } finally {
      setIsPending(false)
    }
  }

  return {
    product,
    countsUnits,
    unit,
    selectProduct,
    clearProduct: () => setProductId(null),
    quantity: { text: quantityText, set: setQuantityText, group, setGroup, unitsPerBoxText, setUnitsPerBoxText },
    totalCost: { text: totalCostText, set: setTotalCostText },
    unitCost,
    baseQuantity,
    groupLabel,
    totalCostValue: totalCost,
    salePrice: {
      text: salePrice,
      set: setSalePriceText,
      suggested: suggestedPrice,
      canSuggest: suggestedPrice > 0 && Math.abs(suggestedPrice - price) > 0.001,
      gain,
      gainTotal: gain !== null ? gain * baseQuantity : null,
      marginPercent,
      lowMargin: marginPercent < LOW_MARGIN_PERCENT,
    },
    details: {
      supplierId,
      setSupplierId: setSupplierIdOverride,
      lotCode,
      setLotCode,
      expirationDate,
      // Picking a date means the product does expire; marking "no vence" clears the date.
      setExpirationDate: (date: string) => {
        setExpirationDate(date)
        if (date) setNoExpirationOverride(false)
      },
      noExpiration,
      setNoExpiration: (value: boolean) => {
        setNoExpirationOverride(value)
        if (value) setExpirationDate('')
      },
      notes,
      setNotes,
    },
    errors: tried ? errors : { product: false, quantity: false, unitsPerBox: false, totalCost: false, salePrice: false },
    error: submitError ?? (tried ? validationMessage : null),
    isPending,
    saved,
    submit,
    reset,
  }
}

export type PurchaseEntry = ReturnType<typeof usePurchaseEntry>
