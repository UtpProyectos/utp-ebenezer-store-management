import { useState, type FormEvent } from 'react'
import type { Supplier, SupplierInput, SupplierType } from '../types/supplier.types'

interface SupplierFormProps {
  supplier?: Supplier
  saving: boolean
  error: string | null
  onSubmit: (input: SupplierInput) => Promise<void>
  onCancel: () => void
}

const fieldClassName =
  'min-h-11 w-full rounded-full border border-separator bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-focus'

const SUPPLIER_TYPES: Array<[SupplierType, string]> = [
  ['WHOLESALER', 'Mayorista'],
  ['DISTRIBUTOR', 'Distribuidor'],
  ['SELF_SERVICE', 'Autoservicio'],
  ['MARKET', 'Mercado'],
  ['LOCAL', 'Local'],
  ['OTHER', 'Otro'],
]

export function SupplierForm({ supplier, saving, error, onSubmit, onCancel }: SupplierFormProps) {
  const [name, setName] = useState(supplier?.name ?? '')
  const [contactName, setContactName] = useState(supplier?.contactName ?? '')
  const [phone, setPhone] = useState(supplier?.phone ?? '')
  const [type, setType] = useState<SupplierType | ''>(supplier?.type ?? '')
  const [address, setAddress] = useState(supplier?.address ?? '')
  const [notes, setNotes] = useState(supplier?.notes ?? '')

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    return onSubmit({
      name: name.trim(),
      contactName: contactName.trim() || null,
      phone: phone.trim() || null,
      type: type || null,
      address: address.trim() || null,
      notes: notes.trim() || null,
      documentNumber: supplier?.documentNumber ?? null,
    })
  }

  return (
    <div
      className="fixed inset-0 z-50 grid place-items-center bg-black/40 p-4"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !saving) onCancel()
      }}
      onKeyDown={(event) => {
        if (event.key === 'Escape' && !saving) onCancel()
      }}
    >
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="supplier-form-title"
        className="w-full max-w-lg rounded-3xl bg-surface shadow-xl"
      >
        <form onSubmit={handleSubmit}>
          <header className="border-b border-separator px-5 py-4">
            <h2 id="supplier-form-title" className="text-lg font-bold">
              {supplier ? 'Editar proveedor' : 'Nuevo proveedor'}
            </h2>
          </header>

          <div className="space-y-4 px-5 py-5">
            {error && (
              <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                {error}
              </p>
            )}

            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Nombre
              <input
                className={fieldClassName}
                autoFocus
                value={name}
                onChange={(event) => setName(event.target.value)}
                placeholder="Ej. Distribuidora Los Andes"
                required
                maxLength={150}
              />
            </label>

            <div className="grid gap-4 sm:grid-cols-2">
              <label className="flex flex-col gap-1.5 text-sm font-medium">
                Contacto
                <input
                  className={fieldClassName}
                  value={contactName}
                  onChange={(event) => setContactName(event.target.value)}
                  placeholder="Nombre"
                  maxLength={150}
                />
              </label>
              <label className="flex flex-col gap-1.5 text-sm font-medium">
                Teléfono
                <input
                  className={fieldClassName}
                  type="tel"
                  value={phone}
                  onChange={(event) => setPhone(event.target.value)}
                  placeholder="999 123 456"
                  maxLength={30}
                />
              </label>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <label className="flex flex-col gap-1.5 text-sm font-medium">
                Tipo
                <select
                  className={fieldClassName}
                  value={type}
                  onChange={(event) => {
                    const selectedType = SUPPLIER_TYPES.find(([value]) => value === event.target.value)?.[0] ?? ''
                    setType(selectedType)
                  }}
                >
                  <option value="">Selecciona un tipo</option>
                  {SUPPLIER_TYPES.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                </select>
              </label>
              <label className="flex flex-col gap-1.5 text-sm font-medium">
                Dirección <span className="font-normal text-muted">(opcional)</span>
                <input
                  className={fieldClassName}
                  value={address}
                  onChange={(event) => setAddress(event.target.value)}
                  maxLength={250}
                />
              </label>
            </div>

            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Observaciones
              <textarea
                className="min-h-24 w-full resize-y rounded-2xl border border-separator bg-background px-3 py-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-focus"
                value={notes}
                onChange={(event) => setNotes(event.target.value)}
                placeholder="Días de visita, pedidos mínimos..."
                maxLength={500}
              />
            </label>
          </div>

          <footer className="flex justify-end gap-2 border-t border-separator px-5 py-4">
            <button
              type="button"
              className="min-h-11 rounded-full border border-separator px-5 text-sm font-semibold hover:bg-surface-secondary focus-visible:outline-2 focus-visible:outline-focus disabled:opacity-60"
              onClick={onCancel}
              disabled={saving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="min-h-11 rounded-full bg-accent px-5 text-sm font-semibold text-background hover:opacity-90 focus-visible:outline-2 focus-visible:outline-focus disabled:opacity-60"
              disabled={saving || !name.trim()}
            >
              {saving ? 'Guardando…' : supplier ? 'Guardar proveedor' : 'Crear proveedor'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}
