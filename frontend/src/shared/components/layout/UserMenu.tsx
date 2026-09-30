import type { Key } from 'react'
import { Dropdown, Label } from '@heroui/react'
import ArrowRightFromSquare from '@gravity-ui/icons/ArrowRightFromSquare'
import ChevronDown from '@gravity-ui/icons/ChevronDown'
import Sliders from '@gravity-ui/icons/Sliders'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { useAuth } from '@/features/auth/hooks/useAuth'
import type { RoleName } from '@/features/auth/types/auth.types'

const ROLE_LABELS: Record<RoleName, string> = {
  ADMIN: 'Administrador',
  CASHIER: 'Cajero',
}

function initials(name: string): string {
  const parts = name.trim().split(/\s+/)
  return ((parts[0]?.[0] ?? '') + (parts.length > 1 ? parts[parts.length - 1][0] : '')).toUpperCase()
}

export function UserMenu() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) return null

  const handleAction = (key: Key) => {
    if (key === 'settings') navigate(ROUTES.settings)
    if (key === 'logout') logout()
  }

  return (
    <Dropdown>
      <Dropdown.Trigger
        aria-label="Menú de usuario"
        className="flex h-12 shrink-0 items-center gap-2.5 rounded-full bg-surface p-1.5 outline-none transition-[background-color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97] md:pr-3"
      >
        <span className="grid size-9 place-items-center rounded-full bg-accent text-sm font-bold text-accent-foreground">
          {initials(user.name)}
        </span>
        <span className="hidden flex-col items-start md:flex">
          <span className="text-sm font-semibold leading-tight">{user.name}</span>
          <span className="text-xs text-muted">{ROLE_LABELS[user.role]}</span>
        </span>
        <ChevronDown className="hidden size-4 text-muted md:block" />
      </Dropdown.Trigger>
      <Dropdown.Popover placement="bottom end" className="min-w-56">
        <div className="flex flex-col gap-0.5 border-b border-separator px-3 pt-2.5 pb-3">
          <span className="font-semibold">{user.name}</span>
          <span className="text-sm text-muted">
            {user.username} · {ROLE_LABELS[user.role]}
          </span>
        </div>
        <Dropdown.Menu aria-label="Opciones de usuario" onAction={handleAction}>
          {user.role === 'ADMIN' ? (
            <Dropdown.Item id="settings" textValue="Configuración">
              <Sliders className="size-4 text-muted" />
              <Label>Configuración</Label>
            </Dropdown.Item>
          ) : null}
          <Dropdown.Item id="logout" textValue="Cerrar sesión" variant="danger">
            <ArrowRightFromSquare className="size-4" />
            <Label>Cerrar sesión</Label>
          </Dropdown.Item>
        </Dropdown.Menu>
      </Dropdown.Popover>
    </Dropdown>
  )
}
