import type { Supplier, SupplierType } from '../types/supplier.types'

interface SupplierListProps {
  suppliers: Supplier[]
  busySupplierId: number | null
  onEdit: (supplier: Supplier) => void
  onToggleStatus: (supplier: Supplier) => void
}

const SUPPLIER_TYPE_LABELS: Record<SupplierType, string> = {
  WHOLESALER: 'Mayorista',
  DISTRIBUTOR: 'Distribuidor',
  SELF_SERVICE: 'Autoservicio',
  MARKET: 'Mercado',
  LOCAL: 'Local',
  OTHER: 'Otro',
}

export function SupplierList({ suppliers, busySupplierId, onEdit, onToggleStatus }: SupplierListProps) {
  if (suppliers.length === 0) {
    return (
      <div className="rounded-3xl border border-dashed border-separator bg-surface px-5 py-12 text-center">
        <p className="font-semibold">No hay proveedores para mostrar</p>
        <p className="mt-1 text-sm text-muted">Prueba otra búsqueda o registra un proveedor.</p>
      </div>
    )
  }

  return (
    <div className="overflow-hidden rounded-3xl border border-separator bg-surface">
      <div className="grid grid-cols-[minmax(0,1fr)_8rem_5rem] gap-3 border-b border-separator px-4 py-3 text-xs font-medium text-muted sm:grid-cols-[minmax(0,1fr)_10rem_7rem] sm:px-5">
        <span>Proveedor</span>
        <span>Tipo</span>
        <span>Estado</span>
      </div>
      {suppliers.map((supplier) => (
        <details key={supplier.id} className="group border-b border-separator last:border-b-0">
          <summary
            className={`grid cursor-pointer list-none grid-cols-[minmax(0,1fr)_8rem_5rem] items-center gap-3 px-4 py-3 outline-none marker:hidden hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus sm:grid-cols-[minmax(0,1fr)_10rem_7rem] sm:px-5 ${
              supplier.active ? '' : 'text-muted'
            }`}
          >
            <span className="min-w-0">
              <span className="block truncate font-semibold text-foreground">{supplier.name}</span>
              {supplier.phone && <span className="mt-0.5 block text-xs text-muted">{supplier.phone}</span>}
            </span>
            <span className="text-sm text-muted">
              {supplier.type ? SUPPLIER_TYPE_LABELS[supplier.type] : '—'}
            </span>
            <span className="flex items-center justify-between gap-2 text-xs">
              <span className={supplier.active ? 'text-success' : 'text-muted'}>
                {supplier.active ? 'Activo' : 'Inactivo'}
              </span>
              <svg
                aria-hidden="true"
                viewBox="0 0 20 20"
                className="size-4 shrink-0 fill-none stroke-muted transition-transform duration-150 ease-out group-open:rotate-180"
                strokeWidth="1.7"
              >
                <path strokeLinecap="round" strokeLinejoin="round" d="m5 7.5 5 5 5-5" />
              </svg>
            </span>
          </summary>

          <div className="grid gap-5 bg-surface-secondary/50 px-4 py-4 sm:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] sm:px-5">
            <dl className="space-y-3 text-sm">
              <div>
                <dt className="text-muted">Contacto</dt>
                <dd>{supplier.contactName || 'Sin contacto registrado'}</dd>
              </div>
              <div>
                <dt className="text-muted">Dirección</dt>
                <dd>{supplier.address || 'Sin dirección registrada'}</dd>
              </div>
              <div>
                <dt className="text-muted">Observaciones</dt>
                <dd>{supplier.notes || 'Sin observaciones'}</dd>
              </div>
            </dl>

            <p className="self-start rounded-2xl bg-surface px-4 py-3 text-sm text-muted">
              El historial de compras y los productos asociados estarán disponibles cuando se implemente el ingreso de
              mercadería.
            </p>

            <div className="flex flex-wrap items-start gap-2 sm:justify-end">
              {supplier.phone ? (
                <a
                  href={`tel:${supplier.phone.replace(/[^\d+]/g, '')}`}
                  className="grid size-10 place-items-center rounded-full bg-surface text-foreground hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                  aria-label={`Llamar a ${supplier.name}`}
                  title={`Llamar a ${supplier.name}`}
                >
                  <svg aria-hidden="true" viewBox="0 0 20 20" className="size-4 fill-none stroke-current" strokeWidth="1.7">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5.2 2.8h2.4l1.2 3.6-1.5 1.5a12 12 0 0 0 4.8 4.8l1.5-1.5 3.6 1.2v2.4a1.4 1.4 0 0 1-1.5 1.4A13.7 13.7 0 0 1 3.8 4.3a1.4 1.4 0 0 1 1.4-1.5Z" />
                  </svg>
                </a>
              ) : (
                <button
                  type="button"
                  className="grid size-10 place-items-center rounded-full bg-surface text-muted"
                  aria-label="No hay teléfono registrado"
                  disabled
                >
                  <svg aria-hidden="true" viewBox="0 0 20 20" className="size-4 fill-none stroke-current" strokeWidth="1.7">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5.2 2.8h2.4l1.2 3.6-1.5 1.5a12 12 0 0 0 4.8 4.8l1.5-1.5 3.6 1.2v2.4a1.4 1.4 0 0 1-1.5 1.4A13.7 13.7 0 0 1 3.8 4.3a1.4 1.4 0 0 1 1.4-1.5Z" />
                  </svg>
                </button>
              )}
              <button
                type="button"
                className="min-h-10 rounded-full bg-default px-4 text-sm font-semibold text-muted"
                disabled
                title="El ingreso de mercadería aún no está habilitado"
              >
                Registrar compra
              </button>
              <button
                type="button"
                className="min-h-10 rounded-full bg-surface px-4 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                onClick={() => onEdit(supplier)}
              >
                Editar
              </button>
              <button
                type="button"
                role="switch"
                aria-checked={supplier.active}
                aria-label={`${supplier.active ? 'Desactivar' : 'Activar'} ${supplier.name}`}
                className={`relative inline-flex h-7 w-12 shrink-0 items-center rounded-full focus-visible:outline-2 focus-visible:outline-focus ${
                  supplier.active ? 'bg-accent' : 'bg-default'
                }`}
                onClick={() => onToggleStatus(supplier)}
                disabled={busySupplierId === supplier.id}
                title={supplier.active ? 'Desactivar proveedor' : 'Activar proveedor'}
              >
                <span
                  aria-hidden="true"
                  className={`absolute top-1 size-5 rounded-full bg-background transition-[left] duration-150 ease-out ${
                    supplier.active ? 'left-6' : 'left-1'
                  }`}
                />
              </button>
            </div>
          </div>
        </details>
      ))}
    </div>
  )
}
