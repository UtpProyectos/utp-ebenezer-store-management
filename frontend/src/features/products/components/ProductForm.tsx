import { useState, type FormEvent, type KeyboardEvent } from 'react'
import { Button, Input, Label, ListBox, Modal, Select, TextArea, TextField } from '@heroui/react'
import ChevronDown from '@gravity-ui/icons/ChevronDown'
import Plus from '@gravity-ui/icons/Plus'
import type { CategoryOption, Product, ProductInput, UnitOption } from '../types/product.types'

interface ProductFormProps {
  product?: Product
  /** Prefills the name when creating (e.g. from a search with no results). */
  initialName?: string
  categories: CategoryOption[]
  units: UnitOption[]
  saving: boolean
  error: string | null
  onSubmit: (input: ProductInput) => Promise<void>
  onCreateCategory: (name: string) => Promise<CategoryOption>
  onCancel: () => void
}

const decimalsOnly = (value: string) => value.replace(/[^0-9.]/g, '')

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'No se pudo crear la categoría.'
}

export function ProductForm({
  product,
  initialName,
  categories,
  units,
  saving,
  error,
  onSubmit,
  onCreateCategory,
  onCancel,
}: ProductFormProps) {
  const defaultUnit = units.find((unit) => unit.abbreviation === 'UND') ?? units[0]
  const [name, setName] = useState(product?.name ?? initialName ?? '')
  const [categoryId, setCategoryId] = useState(String(product?.categoryId ?? categories[0]?.id ?? ''))
  const [baseUnitId, setBaseUnitId] = useState(String(product?.baseUnitId ?? defaultUnit?.id ?? ''))
  const [barcode, setBarcode] = useState(product?.barcode ?? '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [minStock, setMinStock] = useState(String(product?.minStock ?? 0))
  const [showDetails, setShowDetails] = useState(Boolean(product?.barcode || product?.description))
  const [creatingCategory, setCreatingCategory] = useState(categories.length === 0)
  const [newCategoryName, setNewCategoryName] = useState('')
  const [categorySaving, setCategorySaving] = useState(false)
  const [categoryError, setCategoryError] = useState<string | null>(null)
  const categoryInactive = product !== undefined && !categories.some((category) => category.id === product.categoryId)
  const busy = saving || categorySaving

  async function createCategory() {
    const categoryName = newCategoryName.trim()
    if (!categoryName) return
    setCategorySaving(true)
    setCategoryError(null)
    try {
      const category = await onCreateCategory(categoryName)
      setCategoryId(String(category.id))
      setNewCategoryName('')
      setCreatingCategory(false)
    } catch (createError: unknown) {
      setCategoryError(messageFromError(createError))
    } finally {
      setCategorySaving(false)
    }
  }

  function handleCategoryKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    // Enter creates the category instead of submitting the product form.
    if (event.key === 'Enter') {
      event.preventDefault()
      void createCategory()
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (creatingCategory || !categoryId || (categoryInactive && Number(categoryId) === product?.categoryId)) {
      setCategoryError('Crea o elige una categoría antes de guardar.')
      return
    }
    await onSubmit({
      name: name.trim(),
      categoryId: Number(categoryId),
      baseUnitId: Number(baseUnitId),
      barcode: barcode.trim() || null,
      description: description.trim() || null,
      minStock: Number(minStock) || 0,
    })
  }

  return (
    <Modal.Backdrop isOpen isDismissable={!busy} isKeyboardDismissDisabled={busy} onOpenChange={(open) => !open && onCancel()}>
      <Modal.Container size="md">
        <Modal.Dialog className="rounded-3xl" aria-labelledby="product-form-title">
          <Modal.CloseTrigger aria-label="Cerrar formulario" />
          <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
            <Modal.Header>
              <Modal.Heading id="product-form-title" className="text-lg font-bold">
                {product ? 'Editar producto' : 'Nuevo producto'}
              </Modal.Heading>
            </Modal.Header>

            <Modal.Body className="flex flex-col gap-4">
              <TextField value={name} onChange={setName} isRequired maxLength={150} autoFocus>
                <Label>Nombre</Label>
                <Input placeholder="Ej. Galletas de vainilla 6 unid." />
              </TextField>

              <div className="flex flex-col gap-1.5">
                {creatingCategory ? (
                  <>
                    <TextField value={newCategoryName} onChange={setNewCategoryName} maxLength={100} isDisabled={categorySaving}>
                      <Label>Nueva categoría</Label>
                      <div className="flex gap-2">
                        <Input
                          className="min-w-0 flex-1"
                          placeholder="Ej. Bebidas"
                          onKeyDown={handleCategoryKeyDown}
                          autoFocus={categories.length > 0}
                        />
                        <Button
                          isPending={categorySaving}
                          isDisabled={!newCategoryName.trim()}
                          onPress={() => void createCategory()}
                        >
                          Crear
                        </Button>
                        {categories.length > 0 && (
                          <Button
                            variant="tertiary"
                            isDisabled={categorySaving}
                            onPress={() => {
                              setCategoryError(null)
                              setCreatingCategory(false)
                            }}
                          >
                            Cancelar
                          </Button>
                        )}
                      </div>
                    </TextField>
                    {categories.length === 0 && (
                      <p className="text-xs text-muted">Aún no hay categorías: escribe el nombre de la primera.</p>
                    )}
                  </>
                ) : (
                  <>
                  {/* Kept outside <Select>: any Button inside it is taken over as the select trigger. */}
                  <div className="flex items-center justify-between">
                    <Label id="product-category-label" isRequired>Categoría</Label>
                    <Button
                      size="sm"
                      variant="ghost"
                      isDisabled={busy}
                      onPress={() => {
                        setCategoryError(null)
                        setCreatingCategory(true)
                      }}
                    >
                      <Plus aria-hidden="true" className="size-4" />
                      Nueva categoría
                    </Button>
                  </div>
                  <Select
                    aria-labelledby="product-category-label"
                    placeholder="Selecciona una categoría"
                    value={categoryId || null}
                    onChange={(key) => setCategoryId(key === null ? '' : String(key))}
                    isRequired
                  >
                    <Select.Trigger>
                      <Select.Value />
                      <Select.Indicator />
                    </Select.Trigger>
                    <Select.Popover>
                      <ListBox>
                        {categories.map((category) => (
                          <ListBox.Item key={category.id} id={String(category.id)} textValue={category.name}>
                            {category.name}
                            <ListBox.ItemIndicator />
                          </ListBox.Item>
                        ))}
                      </ListBox>
                    </Select.Popover>
                  </Select>
                  </>
                )}

                {categoryInactive && !creatingCategory && (
                  <p className="text-xs text-danger">La categoría actual está desactivada; elige otra.</p>
                )}
                {categoryError && <p role="alert" className="text-xs text-danger">{categoryError}</p>}
              </div>

              <Button
                variant="tertiary"
                fullWidth
                aria-expanded={showDetails}
                className="justify-between"
                onPress={() => setShowDetails((open) => !open)}
              >
                <span>Más detalles <span className="font-normal text-muted">(opcional)</span></span>
                <ChevronDown
                  aria-hidden="true"
                  className={`size-4 transition-transform duration-150 ease-out ${showDetails ? 'rotate-180' : ''}`}
                />
              </Button>

              {showDetails && (
                <div className="flex flex-col gap-4">
                  <div className="grid gap-4 sm:grid-cols-2">
                    <Select
                      value={baseUnitId || null}
                      onChange={(key) => setBaseUnitId(key === null ? '' : String(key))}
                      isRequired
                    >
                      <Label>Se vende por</Label>
                      <Select.Trigger>
                        <Select.Value />
                        <Select.Indicator />
                      </Select.Trigger>
                      <Select.Popover>
                        <ListBox>
                          {units.map((unit) => (
                            <ListBox.Item key={unit.id} id={String(unit.id)} textValue={unit.name}>
                              {unit.name} ({unit.abbreviation})
                              <ListBox.ItemIndicator />
                            </ListBox.Item>
                          ))}
                        </ListBox>
                      </Select.Popover>
                    </Select>

                    <TextField value={minStock} onChange={(value) => setMinStock(decimalsOnly(value))}>
                      <Label>Avisar cuando queden</Label>
                      <Input inputMode="decimal" placeholder="0" />
                    </TextField>
                  </div>

                  <TextField value={barcode} onChange={setBarcode} maxLength={100}>
                    <Label>Código de barras</Label>
                    <Input placeholder="Ej. 7751234567890" />
                  </TextField>

                  <TextField value={description} onChange={setDescription} maxLength={300}>
                    <Label>Descripción</Label>
                    <TextArea placeholder="Detalles que ayuden a identificar el producto" rows={3} />
                  </TextField>
                </div>
              )}

              <p className="rounded-2xl bg-surface-secondary px-4 py-3 text-sm text-muted">
                El precio de venta se define en cada <span className="font-semibold text-foreground">Ingreso de mercadería</span>,
                así puedes cambiarlo cuando cambie el costo.
              </p>

              {error && (
                <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                  {error}
                </p>
              )}
            </Modal.Body>

            <Modal.Footer className="flex justify-end gap-2">
              <Button variant="tertiary" isDisabled={busy} onPress={onCancel}>
                Cancelar
              </Button>
              <Button type="submit" isPending={saving} isDisabled={busy || units.length === 0}>
                {saving ? 'Guardando…' : product ? 'Guardar cambios' : 'Crear producto'}
              </Button>
            </Modal.Footer>
          </form>
        </Modal.Dialog>
      </Modal.Container>
    </Modal.Backdrop>
  )
}
