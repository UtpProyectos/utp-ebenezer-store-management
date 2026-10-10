import type { Product } from '../types/product.types'

interface ProductTableProps {
  products: Product[]
  onEdit: (product: Product) => void
  onToggleStatus: (product: Product) => void
}

const priceFormatter = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const stockFormatter = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 3 })

export function ProductTable({ products, onEdit, onToggleStatus }: ProductTableProps) {
  if (products.length === 0) {
    return (
      <div className="rounded-3xl bg-surface px-5 py-12 text-center">
        <p className="font-semibold">No hay productos para mostrar</p>
        <p className="mt-1 text-sm text-muted">Prueba cambiando la búsqueda o los filtros.</p>
      </div>
    )
  }

  return (
    <div className="overflow-x-auto rounded-3xl bg-surface">
      <table className="w-full min-w-[760px] border-collapse text-left">
        <thead className="text-xs text-muted">
          <tr className="border-b border-separator">
            <th scope="col" className="px-4 py-4 font-medium sm:px-5">Producto</th>
            <th scope="col" className="px-4 py-4 font-medium">Categoría</th>
            <th scope="col" className="px-4 py-4 text-right font-medium">Precio</th>
            <th scope="col" className="px-4 py-4 text-right font-medium">Stock</th>
            <th scope="col" className="px-4 py-4 text-center font-medium">Estado</th>
            <th scope="col" className="px-4 py-4 text-right font-medium">Acción</th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => {
            const lowStock = product.minStock > 0 && product.currentStock <= product.minStock
            return (
              <tr key={product.id} className="border-b border-separator last:border-0">
                <th scope="row" className="px-4 py-4 font-semibold sm:px-5">
                  <span className="block">{product.name}</span>
                </th>
                <td className="px-4 py-4 text-sm text-muted">{product.categoryName}</td>
                <td className="px-4 py-4 text-right text-sm font-semibold">{priceFormatter.format(product.salePrice)}</td>
                <td className={`px-4 py-4 text-right text-sm ${lowStock ? 'font-semibold text-warning' : ''}`}>
                  {stockFormatter.format(product.currentStock)} {product.baseUnitAbbreviation}
                </td>
                <td className="px-4 py-4 text-center">
                  <button
                    type="button"
                    role="switch"
                    aria-checked={product.active}
                    aria-label={`${product.active ? 'Desactivar' : 'Activar'} ${product.name}`}
                    className={`relative inline-flex h-7 w-12 items-center rounded-full transition-colors duration-150 ease-out focus-visible:outline-2 focus-visible:outline-focus active:scale-[0.98] ${
                      product.active ? 'bg-accent' : 'bg-default'
                    }`}
                    onClick={() => onToggleStatus(product)}
                  >
                    <span
                      aria-hidden="true"
                      className={`absolute top-1 size-5 rounded-full bg-background transition-[left] duration-150 ease-out ${
                        product.active ? 'left-6' : 'left-1'
                      }`}
                    />
                  </button>
                  <span className={`ml-2 text-xs ${product.active ? 'text-success' : 'text-muted'}`}>
                    {product.active ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td className="px-4 py-4 text-right">
                  <button
                    type="button"
                    className="min-h-10 rounded-full bg-surface-secondary px-4 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                    onClick={() => onEdit(product)}
                    aria-label={`Editar ${product.name}`}
                  >
                    Editar
                  </button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
