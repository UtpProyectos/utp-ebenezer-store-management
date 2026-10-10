import LayoutSideContentLeft from '@gravity-ui/icons/LayoutSideContentLeft'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { NAV_GROUPS, canSee } from '@/shared/constants/navigation'
import { BrandMark } from '@/shared/components/ui/BrandMark'
import { SidebarNavLink } from './SidebarNavLink'

type SidebarContentProps = {
  collapsed?: boolean
  /** Desktop only: collapse/expand toggle. */
  onToggleCollapsed?: () => void
  /** Mobile drawer: close after navigating. */
  onNavigate?: () => void
}

export function SidebarContent({ collapsed = false, onToggleCollapsed, onNavigate }: SidebarContentProps) {
  const { user } = useAuth()
  const groups = NAV_GROUPS.map((group) => group.filter((item) => canSee(item, user?.role))).filter(
    (group) => group.length > 0,
  )

  return (
    <div className="flex h-full flex-col gap-3 p-3.5">
      <div className={`flex h-16 shrink-0 items-center gap-3 ${collapsed ? 'justify-center' : 'px-2'}`}>
        <BrandMark className="size-14 bg-foreground text-lg text-background ring-4 ring-surface" />
        {!collapsed && (
          <div className="flex flex-col gap-0.5 whitespace-nowrap">
            <span className="text-lg font-bold leading-none">Eben-Ezer</span>
            <span className="text-sm text-muted">Bodega · Minimarket</span>
          </div>
        )}
      </div>

      <nav aria-label="Menú principal" className="flex flex-1 flex-col gap-3">
        {groups.map((group) => (
          <div key={group[0].path} className="flex flex-col gap-1.5 rounded-3xl bg-surface p-3.5">
            {group.map((item) => (
              <SidebarNavLink key={item.path} item={item} collapsed={collapsed} onNavigate={onNavigate} />
            ))}
          </div>
        ))}
      </nav>

      {onToggleCollapsed && (
        <div className="rounded-3xl bg-surface p-3.5">
          <button
            type="button"
            onClick={onToggleCollapsed}
            title={collapsed ? 'Expandir menú' : 'Contraer menú'}
            aria-label={collapsed ? 'Expandir menú' : 'Contraer menú'}
            aria-expanded={!collapsed}
            className={`flex h-12 w-full items-center gap-3 rounded-full text-base font-medium text-foreground/80 outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-background focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97] ${
              collapsed ? 'justify-center' : ''
            }`}
          >
            <span className="grid size-12 shrink-0 place-items-center rounded-full bg-surface-tertiary text-accent">
              <LayoutSideContentLeft className={`size-5 transition-transform duration-200 ease-out ${collapsed ? 'rotate-180' : ''}`} />
            </span>
            {!collapsed && <span>Contraer menú</span>}
          </button>
        </div>
      )}
    </div>
  )
}
