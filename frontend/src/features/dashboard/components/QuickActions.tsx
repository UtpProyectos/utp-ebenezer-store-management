import type { ComponentType, SVGProps } from 'react'
import ArrowRight from '@gravity-ui/icons/ArrowRight'
import Box from '@gravity-ui/icons/Box'
import ShoppingCart from '@gravity-ui/icons/ShoppingCart'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { TruckIcon } from '@/features/inventory'

interface QuickAction {
  label: string
  path: string
  icon: ComponentType<SVGProps<SVGSVGElement>>
  main?: boolean
}

const ACTIONS: QuickAction[] = [
  { label: 'Nueva venta', path: ROUTES.sales, icon: ShoppingCart, main: true },
  { label: 'Registrar mercadería', path: ROUTES.purchases, icon: TruckIcon },
  { label: 'Revisar inventario', path: ROUTES.inventory, icon: Box },
]

export function QuickActions() {
  const navigate = useNavigate()

  return (
    <div className="grid grid-cols-[repeat(auto-fit,minmax(16.25rem,1fr))] gap-3.5">
      {ACTIONS.map(({ label, path, icon: Icon, main }) => (
        <button
          key={path}
          type="button"
          onClick={() => navigate(path)}
          className={`flex items-center gap-4 rounded-full p-3.5 text-left outline-none transition-[box-shadow,transform] duration-200 ease-out hover:shadow-[0_14px_30px_-18px_rgba(17,17,17,0.35)] focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 active:scale-[0.98] ${
            main ? 'bg-accent text-accent-foreground' : 'bg-surface text-foreground'
          }`}
        >
          <span className={`grid size-15 shrink-0 place-items-center rounded-full text-accent ${main ? 'bg-surface' : 'bg-surface-tertiary'}`}>
            <Icon aria-hidden="true" className="size-7" />
          </span>
          <span className="min-w-0 flex-1 text-xl font-bold">{label}</span>
          <span
            className={`grid size-12 shrink-0 place-items-center rounded-full ${
              main ? 'bg-foreground text-background' : 'bg-surface-tertiary text-foreground'
            }`}
          >
            <ArrowRight aria-hidden="true" className="size-5" />
          </span>
        </button>
      ))}
    </div>
  )
}
