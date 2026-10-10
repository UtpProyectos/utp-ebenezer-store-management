import { Button, Switch } from '@heroui/react'
import Pencil from '@gravity-ui/icons/Pencil'
import type { Product } from '../types/product.types'

interface ProductTableProps {
  products: Product[]
  onEdit: (product: Product) => void
  onToggleStatus: (product: Product) => void
}

const priceFormatter = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const stockFormatter = new Intl.NumberFormat('es-PE', { maximumFractionDigits: 3 })

function unitLabel(abbreviation: string) {
  return abbreviation === 'UND' ? 'unid.' : abbreviation.toLowerCase()
}

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
      <table className="w-full min-w-180 border-collapse text-left">
        <thead className="text-sm text-muted">
          <tr>
            <th scope="col" className="px-5 pt-4 pb-2 font-medium">Producto</th>
            <th scope="col" className="w-32 px-3 pt-4 pb-2 text-right font-medium">Precio</th>
            <th scope="col" className="w-32 px-3 pt-4 pb-2 text-right font-medium">Cantidad</th>
            <th scope="col" className="w-48 px-3 pt-4 pb-2 font-medium">¿Se vende?</th>
            <th scope="col" className="w-20 px-5 pt-4 pb-2"><span className="sr-only">Acción</span></th>
          </tr>
        </thead>
        <tbody>
          {products.map((product) => {
            const lowStock = product.minStock > 0 && product.currentStock <= product.minStock
            return (
              <tr key={product.id} className="border-t border-separator">
                <th scope="row" className="px-5 py-3.5 text-left">
                  <span className="block text-[1.0625rem] font-semibold">{product.name}</span>
                  <span className="block text-sm font-normal text-muted">{product.categoryName}</span>
                </th>
                <td className="px-3 py-3.5 text-right text-[1.0625rem] tabular-nums">
                  {product.salePrice > 0 ? (
                    <span className="font-bold">{priceFormatter.format(product.salePrice)}</span>
                  ) : (
                    <span className="text-sm text-muted">Sin precio</span>
                  )}
                </td>
                <td className="px-3 py-3.5 text-right tabular-nums">
                  <span className={`text-[1.0625rem] font-bold ${lowStock ? 'text-danger' : ''}`}>
                    {stockFormatter.format(product.currentStock)}
                  </span>{' '}
                  <span className="text-sm text-muted">{unitLabel(product.baseUnitAbbreviation)}</span>
                </td>
                <td className="px-3 py-3.5">
                  <Switch
                    isSelected={product.active}
                    onChange={() => onToggleStatus(product)}
                    aria-label={`${product.active ? 'Desactivar' : 'Activar'} ${product.name}`}
                  >
                    <Switch.Content>
                      <Switch.Control>
                        <Switch.Thumb />
                      </Switch.Control>
                      <span className={`font-semibold ${product.active ? 'text-success' : 'text-muted'}`}>
                        {product.active ? 'Sí, activo' : 'No, oculto'}
                      </span>
                    </Switch.Content>
                  </Switch>
                </td>
                <td className="px-5 py-3.5 text-right">
                  <Button
                    isIconOnly
                    variant="secondary"
                    aria-label={`Editar ${product.name}`}
                    onPress={() => onEdit(product)}
                  >
                    <Pencil className="size-4" />
                  </Button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
