import { NavLink } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import type { NavItem } from '@/shared/constants/navigation'

type SidebarNavLinkProps = {
  item: NavItem
  collapsed: boolean
  onNavigate?: () => void
}

// Pill item with an icon circle: orange when active (it also stays active on its sub-pages).
export function SidebarNavLink({ item, collapsed, onNavigate }: SidebarNavLinkProps) {
  const { icon: Icon, label, path } = item

  return (
    <NavLink
      to={path}
      end={path === ROUTES.home}
      onClick={onNavigate}
      title={collapsed ? label : undefined}
      aria-label={collapsed ? label : undefined}
      className={({ isActive }) =>
        [
          'flex h-12 items-center gap-3 rounded-full text-base whitespace-nowrap outline-none',
          'transition-[background-color,color,transform] duration-150 ease-out active:scale-[0.97]',
          'focus-visible:ring-2 focus-visible:ring-focus',
          collapsed ? 'justify-center' : 'pr-4',
          isActive
            ? `font-bold text-foreground ${collapsed ? '' : 'bg-accent-soft'}`
            : 'font-medium text-foreground/80 hover:bg-background hover:text-foreground',
        ].join(' ')
      }
    >
      {({ isActive }) => (
        <>
          <span
            className={`grid size-12 shrink-0 place-items-center rounded-full transition-colors duration-150 ${
              isActive ? 'bg-accent text-accent-foreground' : 'bg-surface-tertiary text-accent'
            }`}
          >
            <Icon className="size-5" />
          </span>
          {!collapsed && <span className="truncate">{label}</span>}
        </>
      )}
    </NavLink>
  )
}
