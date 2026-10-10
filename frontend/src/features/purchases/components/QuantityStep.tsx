import { Input, Label, ListBox, Select, TextField } from '@heroui/react'
import Calculator from '@gravity-ui/icons/Calculator'
import { formatMoney } from '@/shared/utils/money'
import type { PurchaseEntry, QuantityGroup } from '../hooks/usePurchaseEntry'
import { EntryStep } from './EntryStep'

const GROUP_OPTIONS: { id: QuantityGroup; label: string }[] = [
  { id: 'unit', label: 'unid.' },
  { id: 'dozen', label: 'docena (12)' },
  { id: 'box', label: 'caja' },
]

const inputClassName = 'h-14 rounded-full px-5 text-xl font-semibold tabular-nums'

const decimalsOnly = (value: string) => value.replace(/[^0-9.]/g, '')

export function QuantityStep({ entry }: { entry: PurchaseEntry }) {
  const { quantity, totalCost, errors, countsUnits, unit } = entry

  return (
    <EntryStep number={2} title="¿Cuánto llegó?" done={entry.baseQuantity > 0 && entry.totalCostValue > 0}>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-2">
          <TextField value={quantity.text} onChange={(value) => quantity.set(decimalsOnly(value))} isInvalid={errors.quantity}>
            <Label className="font-semibold">Cantidad</Label>
            <div className="flex gap-2">
              <Input
                inputMode={countsUnits ? 'numeric' : 'decimal'}
                placeholder="0"
                className={`min-w-0 flex-1 ${inputClassName}`}
              />
              {countsUnits ? (
                <Select
                  aria-label="Cómo llegó"
                  className="w-auto shrink-0"
                  value={quantity.group}
                  onChange={(key) => key !== null && quantity.setGroup(key as QuantityGroup)}
                >
                  <Select.Trigger className="h-14 min-w-36 px-4 text-base">
                    <Select.Value />
                    <Select.Indicator />
                  </Select.Trigger>
                  <Select.Popover>
                    <ListBox>
                      {GROUP_OPTIONS.map((option) => (
                        <ListBox.Item key={option.id} id={option.id} textValue={option.label}>
                          {option.label}
                          <ListBox.ItemIndicator />
                        </ListBox.Item>
                      ))}
                    </ListBox>
                  </Select.Popover>
                </Select>
              ) : (
                <span className="grid h-14 place-items-center rounded-full bg-surface-tertiary px-5 font-semibold">
                  {unit || 'unid.'}
                </span>
              )}
            </div>
          </TextField>
          {countsUnits && quantity.group === 'box' && (
            <TextField
              value={quantity.unitsPerBoxText}
              onChange={(value) => quantity.setUnitsPerBoxText(value.replace(/[^0-9]/g, ''))}
              isInvalid={errors.unitsPerBox}
            >
              <Label className="text-sm font-semibold">Unidades por caja</Label>
              <Input inputMode="numeric" placeholder="Ej. 12" className="h-12 rounded-full px-5 tabular-nums" />
            </TextField>
          )}
        </div>

        <TextField value={totalCost.text} onChange={(value) => totalCost.set(decimalsOnly(value))} isInvalid={errors.totalCost}>
          <Label className="font-semibold">Costo total (S/)</Label>
          <Input inputMode="decimal" placeholder="0.00" className={inputClassName} />
        </TextField>
      </div>

      <div className="flex items-center justify-between gap-3 rounded-full bg-background px-5 py-3.5">
        <span className="flex items-center gap-2.5">
          <Calculator aria-hidden="true" className="size-5 text-muted" />
          Costo por {unit === 'unid.' || !unit ? 'unidad' : unit}
        </span>
        <b className="text-lg tabular-nums">{entry.unitCost > 0 ? formatMoney(entry.unitCost) : '—'}</b>
      </div>
    </EntryStep>
  )
}
