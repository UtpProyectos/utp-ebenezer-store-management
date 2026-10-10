import { Button } from '@heroui/react'
import Box from '@gravity-ui/icons/Box'
import Plus from '@gravity-ui/icons/Plus'
import type { ProductStockResponse } from '../types/inventory.types'
import { describeExpiration, formatQuantity, unitLabel, type ExpirationTone } from '../utils/inventoryFormat'
import { StockStatusChip } from './StockStatusChip'

interface InventoryTableProps {
  items: ProductStockResponse[]
  onRestock: (item: ProductStockResponse) => void
  onWithdraw: (item: ProductStockResponse) => void
}

const EXPIRATION_TONES: Record<ExpirationTone, string> = {
  expired: 'text-danger',
  soon: 'text-foreground',
  later: 'text-muted',
  none: 'text-muted',
}

function Quantity({ item, large = false }: { item: ProductStockResponse; large?: boolean }) {
  return (
    <span
      className={`flex items-baseline gap-1 font-bold tabular-nums ${large ? 'text-2xl' : 'text-lg'} ${
        item.status === 'CRITICAL' ? 'text-danger' : 'text-foreground'
      }`}
    >
      {formatQuantity(item.currentStock)}
      <span className="text-sm font-medium text-muted">{unitLabel(item.baseUnitAbbreviation)}</span>
    </span>
  )
}

export function InventoryTable({ items, onRestock, onWithdraw }: InventoryTableProps) {
  return (
    <div className="overflow-hidden rounded-3xl bg-surface">
      {/* Desktop: table */}
      <table className="hidden w-full border-collapse text-left md:table">
        <thead className="text-sm text-muted">
          <tr>
            <th scope="col" className="px-5 pt-4 pb-2 font-medium">Producto</th>
            <th scope="col" className="w-32 px-3 pt-4 pb-2 font-medium">Cantidad</th>
            <th scope="col" className="w-44 px-3 pt-4 pb-2 font-medium">Estado</th>
            <th scope="col" className="w-36 px-3 pt-4 pb-2 font-medium">Vence</th>
            <th scope="col" className="w-28 px-5 pt-4 pb-2"><span className="sr-only">Acción</span></th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const expiration = describeExpiration(item.nextExpirationDate)
            return (
              <tr key={item.productId} className="border-t border-separator">
                <th scope="row" className="px-5 py-3 font-semibold">
                  <span className="flex items-center gap-3">
                    <span className="grid size-10 shrink-0 place-items-center rounded-full bg-surface-tertiary text-accent">
                      <Box aria-hidden="true" className="size-5" />
                    </span>
                    {item.productName}
                  </span>
                </th>
                <td className="px-3 py-3"><Quantity item={item} /></td>
                <td className="px-3 py-3"><StockStatusChip status={item.status} /></td>
                <td className={`px-3 py-3 ${EXPIRATION_TONES[expiration.tone]}`}>{expiration.label}</td>
                <td className="px-5 py-3 text-right">
                  {item.status === 'EXPIRED' ? (
                    <Button variant="danger-soft" onPress={() => onWithdraw(item)}>Retirar</Button>
                  ) : (
                    <Button
                      isIconOnly
                      variant="secondary"
                      aria-label={`Registrar ingreso de ${item.productName}`}
                      onPress={() => onRestock(item)}
                    >
                      <Plus className="size-5" />
                    </Button>
                  )}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>

      {/* Mobile: cards */}
      <ul className="md:hidden">
        {items.map((item) => (
          <li key={item.productId} className="flex flex-col gap-3 border-b border-separator p-4 last:border-0">
            <div className="flex items-start justify-between gap-3">
              <div className="flex min-w-0 flex-col items-start gap-1.5">
                <span className="text-lg font-semibold leading-snug">{item.productName}</span>
                <StockStatusChip status={item.status} />
              </div>
              <Quantity item={item} large />
            </div>
            {item.status === 'EXPIRED' ? (
              <Button variant="danger-soft" fullWidth onPress={() => onWithdraw(item)}>Retirar del estante</Button>
            ) : (
              <Button variant="outline" fullWidth onPress={() => onRestock(item)}>Registrar ingreso</Button>
            )}
          </li>
        ))}
      </ul>
    </div>
  )
}
