import { Button, Input, Label, TextField } from '@heroui/react'
import { formatMoney } from '@/shared/utils/money'
import type { PurchaseEntry } from '../hooks/usePurchaseEntry'
import { EntryStep } from './EntryStep'

export function SalePriceStep({ entry }: { entry: PurchaseEntry }) {
  const { salePrice, unit } = entry
  const losing = salePrice.gain !== null && salePrice.gain <= 0
  const per = unit === 'unid.' || !unit ? 'unidad' : unit

  return (
    <EntryStep number={3} title="¿A qué precio se venderá?" done={Number(salePrice.text) > 0}>
      <div className="flex flex-wrap items-end gap-3">
        <TextField
          className="min-w-56 flex-1"
          value={salePrice.text}
          onChange={(value) => salePrice.set(value.replace(/[^0-9.]/g, ''))}
          isInvalid={entry.errors.salePrice}
        >
          <Label className="font-semibold">Precio de venta por {per} (S/)</Label>
          <Input inputMode="decimal" placeholder="0.00" className="h-14 rounded-full px-5 text-xl font-semibold tabular-nums" />
        </TextField>
        {salePrice.canSuggest && (
          <Button
            size="lg"
            className="h-14 bg-ai/15 text-foreground"
            onPress={() => salePrice.set(salePrice.suggested.toFixed(2))}
          >
            Usar sugerido: {formatMoney(salePrice.suggested)}
          </Button>
        )}
      </div>

      {salePrice.gain !== null && (
        <div className="grid gap-2.5 sm:grid-cols-2">
          <div
            className={`flex flex-col gap-1.5 rounded-[1.25rem] px-5 py-4 ${
              losing ? 'bg-danger-soft text-danger-soft-foreground' : 'bg-success-soft text-success-soft-foreground'
            }`}
          >
            <span className="text-sm font-semibold">Ganancia aproximada</span>
            <span className="text-2xl leading-none font-bold tabular-nums">{formatMoney(salePrice.gain)}</span>
            <span className="text-sm">por {per} · {formatMoney(salePrice.gainTotal ?? 0)} en total</span>
          </div>
          <div className="flex flex-col gap-1.5 rounded-[1.25rem] bg-background px-5 py-4">
            <span className="text-sm font-semibold text-foreground/80">Margen</span>
            <span className="text-2xl leading-none font-bold tabular-nums">{Math.round(salePrice.marginPercent)}%</span>
            <span className="text-sm text-muted">
              {losing ? 'Con ese precio pierdes dinero' : salePrice.lowMargin ? 'Ganancia baja' : 'Ganancia normal'}
            </span>
          </div>
        </div>
      )}
    </EntryStep>
  )
}
