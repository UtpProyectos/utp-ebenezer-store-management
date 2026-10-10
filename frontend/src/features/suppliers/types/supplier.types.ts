export type SupplierType =
  | 'WHOLESALER'
  | 'DISTRIBUTOR'
  | 'SELF_SERVICE'
  | 'MARKET'
  | 'LOCAL'
  | 'OTHER'

export interface Supplier {
  id: number
  name: string
  documentNumber: string | null
  phone: string | null
  type: SupplierType | null
  contactName: string | null
  address: string | null
  notes: string | null
  active: boolean
  products: SupplierProduct[]
}

export interface SupplierProduct {
  id: number
  name: string
}

export interface SupplierInput {
  name: string
  documentNumber: string | null
  phone: string | null
  type: SupplierType | null
  contactName: string | null
  address: string | null
  notes: string | null
  productIds: number[]
}
