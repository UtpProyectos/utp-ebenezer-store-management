export interface Product {
  id: number
  categoryId: number
  categoryName: string
  baseUnitId: number
  baseUnitAbbreviation: string
  name: string
  description: string | null
  barcode: string | null
  salePrice: number
  minStock: number
  active: boolean
  createdAt: string
  currentStock: number
}

export interface ProductInput {
  categoryId: number
  baseUnitId: number
  name: string
  description: string | null
  barcode: string | null
  /** Omitted: the sale price is set on each purchase entry. */
  salePrice?: number
  minStock: number
}

export interface CategoryOption {
  id: number
  name: string
  description: string | null
  active: boolean
}

export interface UnitOption {
  id: number
  name: string
  abbreviation: string
  type: 'WEIGHT' | 'VOLUME' | 'UNIT'
  conversionFactor: number
}
