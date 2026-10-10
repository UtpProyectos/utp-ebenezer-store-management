import { useState } from 'react'
import { Button } from '@heroui/react'
import Check from '@gravity-ui/icons/Check'
import Plus from '@gravity-ui/icons/Plus'
import { formatStock, matchesSearch, type ProductStockResponse } from '@/features/inventory'
import { formatMoney } from '@/shared/utils/money'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { EntryStep } from './EntryStep'

const MAX_RESULTS = 6

interface ProductPickerStepProps {
  items: ProductStockResponse[]
  product: ProductStockResponse | null
  invalid: boolean
  onSelect: (productId: number) => void
  onClear: () => void
  /** Only provided when the current user may create products. */
  onCreateProduct?: (name: string) => void
}

// Without a search, products that are running out (lowest stock / minimum) come first.
function stockRatio(item: ProductStockResponse) {
  return item.minStock > 0 ? item.currentStock / item.minStock : Number.POSITIVE_INFINITY
}

export function ProductPickerStep({ items, product, invalid, onSelect, onClear, onCreateProduct }: ProductPickerStepProps) {
  const [query, setQuery] = useState('')
  const searching = query.trim().length > 0
  const results = (searching
    ? items.filter((item) => matchesSearch(item, query))
    : [...items].sort((a, b) => stockRatio(a) - stockRatio(b))
  ).slice(0, MAX_RESULTS)

  const select = (productId: number) => {
    setQuery('')
    onSelect(productId)
  }

  return (
    <EntryStep number={1} title="¿Qué producto llegó?" done={product !== null}>
      {product ? (
        <div className="flex items-center gap-3.5 rounded-full bg-accent-soft p-3">
          <span className="grid size-12 shrink-0 place-items-center rounded-full bg-accent text-accent-foreground">
            <Check aria-hidden="true" className="size-6" />
          </span>
          <span className="flex min-w-0 flex-1 flex-col gap-0.5">
            <span className="text-lg font-bold leading-tight">{product.productName}</span>
            <span className="text-sm text-foreground/80">
              Ahora tienes {formatStock(product)} · la última vez costó{' '}
              {product.lastUnitCost !== null ? formatMoney(product.lastUnitCost) : 'sin dato'}
            </span>
          </span>
          <Button variant="tertiary" className="bg-surface" onPress={onClear}>Cambiar</Button>
        </div>
      ) : (
        <>
          <SearchInput
            label="Buscar producto"
            placeholder="Escribe el nombre o el código"
            value={query}
            onChange={setQuery}
            isInvalid={invalid}
          />

          <div className="grid grid-cols-[repeat(auto-fill,minmax(15rem,1fr))] gap-2">
            {results.map((item) => (
              <button
                key={item.productId}
                type="button"
                onClick={() => select(item.productId)}
                className="flex items-center gap-3 rounded-full bg-background py-2.5 pr-4 pl-2.5 text-left outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-tertiary focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97]"
              >
                <span className="grid size-10 shrink-0 place-items-center rounded-full bg-surface text-accent">
                  <Plus aria-hidden="true" className="size-5" />
                </span>
                <span className="flex min-w-0 flex-col">
                  <span className="truncate font-semibold leading-tight">{item.productName}</span>
                  <span className="text-sm text-muted">Tienes {formatStock(item)}</span>
                </span>
              </button>
            ))}
          </div>

          {searching && results.length === 0 && (
            <p className="text-muted">No encontramos “{query.trim()}”.</p>
          )}
          {!searching && items.length === 0 && (
            <p className="text-muted">
              Aún no hay productos activos.{!onCreateProduct && ' Pídele al administrador que los registre.'}
            </p>
          )}
          {onCreateProduct && results.length === 0 && (
            <div>
              <Button variant="outline" onPress={() => onCreateProduct(query.trim())}>
                <Plus aria-hidden="true" className="size-4" />
                {searching ? `Crear “${query.trim()}”` : 'Crear producto'}
              </Button>
            </div>
          )}
          {invalid && <p className="text-sm text-danger">Elige el producto que llegó.</p>}
        </>
      )}
    </EntryStep>
  )
}
