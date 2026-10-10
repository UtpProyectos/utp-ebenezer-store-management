import { useState, type KeyboardEvent } from 'react'
import ChevronDown from '@gravity-ui/icons/ChevronDown'
import { matchesSearch, type ProductStockResponse } from '@/features/inventory'
import type { PromotionResponse } from '@/features/promotions/types/promotion.types'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { sellableStock } from '../utils/salePricing'
import { ALL_CATEGORIES, CategoryChips } from './CategoryChips'
import { ProductTile } from './ProductTile'

const FIRST_PAGE = 12
const NEXT_PAGE = 24

interface ProductGridProps {
  products: ProductStockResponse[]
  categories: string[]
  promotionByProduct: Map<number, PromotionResponse>
  quantityOf: (productId: number) => number
  onAdd: (item: ProductStockResponse) => void
}

export function ProductGrid({ products, categories, promotionByProduct, quantityOf, onAdd }: ProductGridProps) {
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState(ALL_CATEGORIES)
  const [limit, setLimit] = useState(FIRST_PAGE)

  const visible = products.filter(
    (item) => (category === ALL_CATEGORIES || item.categoryName === category) && matchesSearch(item, query),
  )

  const changeQuery = (value: string) => {
    setQuery(value)
    setLimit(FIRST_PAGE)
  }

  const changeCategory = (value: string) => {
    setCategory(value)
    setLimit(FIRST_PAGE)
  }

  // Enter adds the first product that can be sold, then clears the search for the next one.
  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key !== 'Enter') return
    const first = visible.find((item) => sellableStock(item) > 0)
    if (!first) return
    event.preventDefault()
    onAdd(first)
    changeQuery('')
  }

  return (
    <section className="flex min-w-0 flex-col gap-4 rounded-[2rem] bg-surface p-4">
      <div onKeyDown={handleKeyDown}>
        <SearchInput label="Buscar producto" placeholder="Buscar producto" value={query} onChange={changeQuery} autoFocus />
      </div>

      <CategoryChips categories={categories} value={category} onChange={changeCategory} />

      {visible.length > 0 ? (
        <div className="grid grid-cols-[repeat(auto-fill,minmax(max(11.25rem,calc((100%-1.875rem)/4)),1fr))] gap-2.5">
          {visible.slice(0, limit).map((item) => (
            <ProductTile
              key={item.productId}
              item={item}
              promotion={promotionByProduct.get(item.productId)}
              inCart={quantityOf(item.productId)}
              onAdd={onAdd}
            />
          ))}
        </div>
      ) : (
        <p className="rounded-3xl bg-background p-9 text-center font-medium text-muted">
          {query.trim() ? `No hay productos que coincidan con “${query.trim()}”.` : 'No hay productos en esta categoría.'}
        </p>
      )}

      {visible.length > limit && (
        <button
          type="button"
          onClick={() => setLimit(limit + NEXT_PAGE)}
          className="flex h-12 items-center gap-2 self-center rounded-full bg-background py-0 pr-2 pl-5 font-semibold outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-tertiary focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97]"
        >
          Ver {visible.length - limit} productos más
          <span className="grid size-8.5 place-items-center rounded-full bg-surface">
            <ChevronDown aria-hidden="true" className="size-4" />
          </span>
        </button>
      )}
    </section>
  )
}
