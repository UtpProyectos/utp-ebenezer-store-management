import { useState } from 'react'
import { parseDate, today, getLocalTimeZone } from '@internationalized/date'
import { Button, Calendar, DateField, DatePicker, Input, Label, ListBox, Select, Switch, TextArea, TextField } from '@heroui/react'
import ChevronDown from '@gravity-ui/icons/ChevronDown'
import Plus from '@gravity-ui/icons/Plus'
import type { Supplier } from '@/features/suppliers/types/supplier.types'
import type { PurchaseEntry } from '../hooks/usePurchaseEntry'

const NO_SUPPLIER = 'none'

interface MoreDetailsSectionProps {
  entry: PurchaseEntry
  suppliers: Supplier[]
  /** Only provided when the current user may create suppliers. */
  onCreateSupplier?: (name: string) => Promise<Supplier>
}

export function MoreDetailsSection({ entry, suppliers, onCreateSupplier }: MoreDetailsSectionProps) {
  const [open, setOpen] = useState(false)
  const { details } = entry
  const [creatingSupplier, setCreatingSupplier] = useState(false)
  const [newSupplierName, setNewSupplierName] = useState('')
  const [supplierSaving, setSupplierSaving] = useState(false)
  const [supplierError, setSupplierError] = useState<string | null>(null)

  async function createSupplier() {
    const name = newSupplierName.trim()
    if (!name || !onCreateSupplier) return
    setSupplierSaving(true)
    setSupplierError(null)
    try {
      const supplier = await onCreateSupplier(name)
      details.setSupplierId(String(supplier.id))
      setNewSupplierName('')
      setCreatingSupplier(false)
    } catch (createError: unknown) {
      setSupplierError(createError instanceof Error ? createError.message : 'No se pudo crear el proveedor.')
    } finally {
      setSupplierSaving(false)
    }
  }

  return (
    <section className="overflow-hidden rounded-3xl bg-surface">
      <button
        type="button"
        aria-expanded={open}
        aria-controls="purchase-more-details"
        onClick={() => setOpen((value) => !value)}
        className="flex w-full items-center justify-between gap-3 px-5 py-5 text-left outline-none hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-inset sm:px-6"
      >
        <span className="flex flex-col gap-0.5">
          <span className="text-lg font-bold">Más detalles (opcional)</span>
          <span className="text-muted">Proveedor, lote, fecha de vencimiento y notas</span>
        </span>
        <span className="grid size-11 shrink-0 place-items-center rounded-full bg-surface-tertiary">
          <ChevronDown
            aria-hidden="true"
            className={`size-5 transition-transform duration-200 ease-out ${open ? 'rotate-180' : ''}`}
          />
        </span>
      </button>

      {open && (
        <div id="purchase-more-details" className="grid gap-4 px-5 pt-1 pb-6 sm:grid-cols-2 sm:px-6">
          <div className="flex flex-col gap-2">
            {/* Kept outside <Select>: any Button inside it is taken over as the select trigger. */}
            <div className="flex min-h-8 items-center justify-between gap-2">
              <Label id="purchase-supplier-label" className="font-semibold">
                {creatingSupplier ? 'Nuevo proveedor' : 'Proveedor'}
              </Label>
              {onCreateSupplier && !creatingSupplier && (
                <Button
                  size="sm"
                  variant="ghost"
                  onPress={() => {
                    setSupplierError(null)
                    setCreatingSupplier(true)
                  }}
                >
                  <Plus aria-hidden="true" className="size-4" />
                  Nuevo proveedor
                </Button>
              )}
            </div>
            {creatingSupplier ? (
              <div className="flex gap-2">
                <TextField
                  aria-labelledby="purchase-supplier-label"
                  className="min-w-0 flex-1"
                  value={newSupplierName}
                  onChange={setNewSupplierName}
                  maxLength={150}
                  isDisabled={supplierSaving}
                  autoFocus
                >
                  <Input
                    placeholder="Ej. Distribuidora Lima"
                    className="h-13 rounded-full px-5"
                    onKeyDown={(event) => {
                      if (event.key === 'Enter') {
                        event.preventDefault()
                        void createSupplier()
                      }
                    }}
                  />
                </TextField>
                <Button
                  className="h-13"
                  isPending={supplierSaving}
                  isDisabled={!newSupplierName.trim()}
                  onPress={() => void createSupplier()}
                >
                  Crear
                </Button>
                <Button
                  className="h-13"
                  variant="tertiary"
                  isDisabled={supplierSaving}
                  onPress={() => {
                    setSupplierError(null)
                    setCreatingSupplier(false)
                  }}
                >
                  Cancelar
                </Button>
              </div>
            ) : (
            <Select
              aria-labelledby="purchase-supplier-label"
              value={details.supplierId || NO_SUPPLIER}
              onChange={(key) => details.setSupplierId(key === null || key === NO_SUPPLIER ? '' : String(key))}
            >
              <Select.Trigger className="h-13 px-4">
                <Select.Value />
                <Select.Indicator />
              </Select.Trigger>
              <Select.Popover>
                <ListBox>
                  <ListBox.Item id={NO_SUPPLIER} textValue="Sin proveedor">
                    Sin proveedor
                    <ListBox.ItemIndicator />
                  </ListBox.Item>
                  {suppliers.map((supplier) => (
                    <ListBox.Item key={supplier.id} id={String(supplier.id)} textValue={supplier.name}>
                      {supplier.name}
                      <ListBox.ItemIndicator />
                    </ListBox.Item>
                  ))}
                </ListBox>
              </Select.Popover>
            </Select>
            )}
            {supplierError && <p role="alert" className="text-sm text-danger">{supplierError}</p>}
          </div>

          <TextField value={details.lotCode} onChange={details.setLotCode} maxLength={100}>
            <Label className="flex min-h-8 items-center font-semibold">Lote</Label>
            <Input placeholder="Ej. L-0929-A" className="h-13 rounded-full px-5 font-mono" />
          </TextField>

          <div className="flex flex-col gap-3 sm:col-span-2">
            <DatePicker
              value={details.expirationDate ? parseDate(details.expirationDate) : null}
              onChange={(date) => details.setExpirationDate(date ? date.toString() : '')}
              minValue={today(getLocalTimeZone())}
            >
              <Label className="font-semibold">Fecha de vencimiento</Label>
              <DateField.Group fullWidth className="h-13 rounded-full px-5">
                <DateField.Input>{(segment) => <DateField.Segment segment={segment} />}</DateField.Input>
                <DateField.Suffix>
                  <DatePicker.Trigger>
                    <DatePicker.TriggerIndicator />
                  </DatePicker.Trigger>
                </DateField.Suffix>
              </DateField.Group>
              <DatePicker.Popover>
                <Calendar aria-label="Fecha de vencimiento">
                  <Calendar.Header>
                    <Calendar.YearPickerTrigger>
                      <Calendar.YearPickerTriggerHeading />
                      <Calendar.YearPickerTriggerIndicator />
                    </Calendar.YearPickerTrigger>
                    <Calendar.NavButton slot="previous" />
                    <Calendar.NavButton slot="next" />
                  </Calendar.Header>
                  <Calendar.Grid>
                    <Calendar.GridHeader>{(day) => <Calendar.HeaderCell>{day}</Calendar.HeaderCell>}</Calendar.GridHeader>
                    <Calendar.GridBody>{(date) => <Calendar.Cell date={date} />}</Calendar.GridBody>
                  </Calendar.Grid>
                  <Calendar.YearPickerGrid>
                    <Calendar.YearPickerGridBody>
                      {({ year }) => <Calendar.YearPickerCell year={year} />}
                    </Calendar.YearPickerGridBody>
                  </Calendar.YearPickerGrid>
                </Calendar>
              </DatePicker.Popover>
            </DatePicker>

            <Switch isSelected={details.noExpiration} onChange={details.setNoExpiration}>
              <Switch.Content className="w-fit rounded-2xl bg-background px-4 py-3">
                <Switch.Control>
                  <Switch.Thumb />
                </Switch.Control>
                <span className="flex flex-col">
                  <Label className="font-semibold">Este producto no vence</Label>
                  <span className="text-sm font-normal text-muted">Ej. bolsas, detergente, utensilios</span>
                </span>
              </Switch.Content>
            </Switch>
          </div>

          <TextField className="sm:col-span-2" value={details.notes} onChange={details.setNotes} maxLength={500}>
            <Label className="font-semibold">Notas</Label>
            <TextArea rows={2} placeholder="Ej. llegó una caja golpeada" className="rounded-[1.25rem] px-5 py-3.5" />
          </TextField>
        </div>
      )}
    </section>
  )
}
