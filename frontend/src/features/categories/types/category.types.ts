export interface Category {
  id: number
  name: string
  description: string | null
  active: boolean
  productCount: number
}

export interface CategoryInput {
  name: string
  description: string | null
}
