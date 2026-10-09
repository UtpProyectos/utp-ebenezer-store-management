import ArrowRight from '@gravity-ui/icons/ArrowRight'
import CircleInfo from '@gravity-ui/icons/CircleInfo'
import { Link } from 'react-router'
import { ADMIN_ITEMS } from '@/shared/constants/navigation'

const ITEM_BADGES: Record<string, string> = {
  '/admin/products': 'Catálogo',
  '/admin/categories': 'Organización',
  '/admin/suppliers': 'Contactos',
  '/admin/users': 'Accesos',
  '/admin/history': 'Reportes',
  '/admin/settings': 'General',
}

export function AdminPage() {
  return (
    <div className="mx-auto flex w-full max-w-350 flex-col gap-4">
      <div className="flex items-center gap-3 rounded-3xl bg-surface px-5 py-4">
        <span className="grid size-10 shrink-0 place-items-center rounded-full bg-surface-tertiary text-info">
          <CircleInfo aria-hidden="true" className="size-5" />
        </span>
        <p className="text-sm text-muted sm:text-base">
          Aquí se cambian los datos de la tienda. Para vender, registrar mercadería o revisar el stock no necesitas
          entrar aquí.
        </p>
      </div>

      <nav aria-label="Opciones de administración" className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5">
        {ADMIN_ITEMS.map(({ label, path, icon: Icon, description }) => (
          <Link
            key={path}
            to={path}
            className="group flex min-h-36 flex-col justify-between gap-5 rounded-3xl bg-surface p-4 outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.98]"
          >
            <div className="flex items-center justify-between gap-3">
              <span className="grid size-11 place-items-center rounded-full bg-surface-tertiary text-accent">
                <Icon aria-hidden="true" className="size-5" />
              </span>
              <span className="rounded-full bg-surface-secondary px-3 py-1 text-xs font-medium text-muted">
                {ITEM_BADGES[path]}
              </span>
            </div>

            <div className="flex items-end justify-between gap-3">
              <div className="flex min-w-0 flex-col gap-1">
                <span className="text-lg font-bold">{label}</span>
                <span className="text-sm leading-snug text-muted">{description}</span>
              </div>
              <span
                aria-hidden="true"
                className="grid size-10 shrink-0 place-items-center rounded-full bg-foreground text-background transition-transform duration-150 ease-out group-hover:translate-x-0.5"
              >
                <ArrowRight className="size-5" />
              </span>
            </div>
          </Link>
        ))}
      </nav>
    </div>
  )
}
