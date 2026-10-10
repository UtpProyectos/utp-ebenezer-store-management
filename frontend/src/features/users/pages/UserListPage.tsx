import { useEffect, useMemo, useState } from 'react'
import { SearchInput } from '@/shared/components/ui/SearchInput'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { ResetPasswordForm } from '../components/ResetPasswordForm'
import { UserForm } from '../components/UserForm'
import { userApi } from '../services/userApi'
import type { RoleOption, UserInput, UserUpdateInput } from '../types/user.types'
import type { UserResponse } from '@/features/auth/types/auth.types'

type StatusFilter = 'all' | 'active' | 'inactive'

const ROLE_LABELS: Record<UserResponse['role'], string> = {
  ADMIN: 'Administrador',
  CASHIER: 'Cajero',
}

const dateFormatter = new Intl.DateTimeFormat('es-PE', {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function messageFromError(error: unknown) {
  return error instanceof Error ? error.message : 'Ocurrió un error. Inténtalo nuevamente.'
}

export function UserListPage() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState<UserResponse[]>([])
  const [roles, setRoles] = useState<RoleOption[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<StatusFilter>('all')
  const [editingUser, setEditingUser] = useState<UserResponse | undefined>()
  const [passwordUser, setPasswordUser] = useState<UserResponse | undefined>()
  const [formOpen, setFormOpen] = useState(false)
  const [saving, setSaving] = useState(false)
  const [busyUserId, setBusyUserId] = useState<number | null>(null)

  async function reload() {
    setLoading(true)
    setLoadError(null)
    try {
      setUsers(await userApi.getAll())
    } catch (error: unknown) {
      setLoadError(messageFromError(error))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    Promise.all([userApi.getAll(), userApi.getRoles()])
      .then(([userList, roleList]) => {
        if (cancelled) return
        setUsers(userList)
        setRoles(roleList)
      })
      .catch((error: unknown) => {
        if (!cancelled) setLoadError(messageFromError(error))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const visibleUsers = useMemo(() => {
    const normalizedSearch = search.trim().toLocaleLowerCase('es')
    return users.filter((item) => {
      const matchesSearch = !normalizedSearch
        || item.name.toLocaleLowerCase('es').includes(normalizedSearch)
        || item.username.toLocaleLowerCase('es').includes(normalizedSearch)
        || item.email?.toLocaleLowerCase('es').includes(normalizedSearch)
      const matchesStatus = status === 'all' || item.active === (status === 'active')
      return matchesSearch && matchesStatus
    })
  }, [search, status, users])

  function openCreateForm() {
    setActionError(null)
    setEditingUser(undefined)
    setFormOpen(true)
  }

  function openEditForm(user: UserResponse) {
    setActionError(null)
    setEditingUser(user)
    setFormOpen(true)
  }

  async function saveUser(input: UserInput | UserUpdateInput) {
    setSaving(true)
    setActionError(null)
    try {
      if ('username' in input) {
        if (editingUser || !input.password) {
          throw new Error('No se pudo validar la información del usuario')
        }
        await userApi.create({ ...input, password: input.password })
      } else {
        if (!editingUser) {
          throw new Error('No se pudo validar la información del usuario')
        }
        await userApi.update(editingUser.id, input)
      }
      setFormOpen(false)
      setEditingUser(undefined)
      await reload()
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setSaving(false)
    }
  }

  async function toggleStatus(user: UserResponse) {
    setBusyUserId(user.id)
    setActionError(null)
    try {
      await userApi.updateStatus(user.id, !user.active)
      await reload()
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setBusyUserId(null)
    }
  }

  async function resetPassword(password: string) {
    if (!passwordUser) return
    setSaving(true)
    setActionError(null)
    try {
      await userApi.resetPassword(passwordUser.id, password)
      setPasswordUser(undefined)
    } catch (error: unknown) {
      setActionError(messageFromError(error))
    } finally {
      setSaving(false)
    }
  }

  const activeCount = users.filter((item) => item.active).length
  const adminCount = users.filter((item) => item.role === 'ADMIN').length
  const cashierCount = users.filter((item) => item.role === 'CASHIER').length

  return (
    <div className="mx-auto flex w-full max-w-350 flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-muted">
          {users.length} {users.length === 1 ? 'usuario' : 'usuarios'} · {activeCount} activos
        </p>
        <button
          type="button"
          className="min-h-11 rounded-full bg-accent px-5 text-sm font-semibold text-background hover:opacity-90 focus-visible:outline-2 focus-visible:outline-focus"
          onClick={openCreateForm}
        >
          + Nuevo usuario
        </button>
      </div>

      <section className="grid gap-3 sm:grid-cols-2">
        <article className="rounded-3xl border border-accent/30 bg-accent/10 p-5">
          <p className="text-sm font-semibold text-muted">Administradores</p>
          <p className="mt-2 text-3xl font-bold">{adminCount}</p>
          <p className="mt-1 text-sm text-muted">Acceso a la configuración y gestión del negocio</p>
        </article>
        <article className="rounded-3xl border border-separator bg-surface-secondary p-5">
          <p className="text-sm font-semibold text-muted">Cajeros</p>
          <p className="mt-2 text-3xl font-bold">{cashierCount}</p>
          <p className="mt-1 text-sm text-muted">Acceso a las tareas diarias de venta</p>
        </article>
      </section>

      <section className="flex flex-col gap-3 rounded-3xl bg-surface p-4 sm:flex-row">
        <SearchInput
          label="Buscar usuario"
          placeholder="Buscar por nombre, usuario o correo"
          value={search}
          onChange={setSearch}
          className="min-w-0 flex-1"
        />
        <div className="flex gap-1 rounded-full bg-surface-secondary p-1" role="group" aria-label="Filtrar usuarios por estado">
          {([
            ['all', 'Todos'],
            ['active', 'Activos'],
            ['inactive', 'Inactivos'],
          ] as const).map(([value, label]) => (
            <button
              key={value}
              type="button"
              aria-pressed={status === value}
              className={`min-h-9 rounded-full px-4 text-sm focus-visible:outline-2 focus-visible:outline-focus ${
                status === value ? 'bg-surface font-semibold text-foreground' : 'text-muted hover:text-foreground'
              }`}
              onClick={() => setStatus(value)}
            >
              {label}
            </button>
          ))}
        </div>
      </section>

      {actionError && !formOpen && !passwordUser && (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          {actionError}
        </p>
      )}
      {formOpen && (
        <UserForm
          key={editingUser?.id ?? 'new'}
          user={editingUser}
          roles={roles}
          isCurrentUser={editingUser?.id === currentUser?.id}
          saving={saving}
          error={actionError}
          onSubmit={saveUser}
          onCancel={() => setFormOpen(false)}
        />
      )}
      {passwordUser && (
        <ResetPasswordForm
          userName={passwordUser.username}
          saving={saving}
          error={actionError}
          onSubmit={resetPassword}
          onCancel={() => setPasswordUser(undefined)}
        />
      )}

      {loadError ? (
        <p role="alert" className="rounded-2xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
          No se pudieron cargar los usuarios: {loadError}
        </p>
      ) : loading ? (
        <p role="status" className="rounded-3xl bg-surface px-5 py-12 text-center text-sm text-muted">
          Cargando usuarios…
        </p>
      ) : visibleUsers.length === 0 ? (
        <div className="rounded-3xl border border-dashed border-separator bg-surface px-5 py-12 text-center">
          <p className="font-semibold">No hay usuarios para mostrar</p>
          <p className="mt-1 text-sm text-muted">Prueba otra búsqueda o crea un usuario nuevo.</p>
        </div>
      ) : (
        <div className="grid gap-3">
          {visibleUsers.map((item) => {
            const isCurrentUser = item.id === currentUser?.id
            const initials = item.name
              .split(/\s+/)
              .filter(Boolean)
              .slice(0, 2)
              .map((part) => part[0]?.toLocaleUpperCase('es') ?? '')
              .join('')
            return (
              <article key={item.id} className="rounded-3xl border border-separator bg-surface p-4 sm:p-5">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="flex min-w-0 items-center gap-3">
                    <span className={`grid size-12 shrink-0 place-items-center rounded-2xl text-sm font-bold ${
                      item.role === 'ADMIN' ? 'bg-accent/10 text-accent' : 'bg-surface-secondary text-foreground'
                    }`}>
                      {initials}
                    </span>
                    <div className="min-w-0">
                      <h2 className="truncate font-bold">{item.name}{isCurrentUser ? ' · Tú' : ''}</h2>
                      <p className="truncate text-sm text-muted">@{item.username}</p>
                      {item.email && <p className="truncate text-xs text-muted">{item.email}</p>}
                    </div>
                  </div>
                  <span className={`rounded-full px-3 py-1 text-xs font-semibold ${
                    item.role === 'ADMIN' ? 'bg-accent text-background' : 'bg-surface-secondary text-foreground'
                  }`}>
                    {ROLE_LABELS[item.role]}
                  </span>
                </div>

                <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-separator pt-4">
                  <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm">
                    <span className="text-muted">
                      Último acceso: {item.lastLoginAt ? dateFormatter.format(new Date(item.lastLoginAt)) : 'Nunca'}
                    </span>
                    <span className={`font-medium ${item.active ? 'text-success' : 'text-muted'}`}>
                      {item.active ? 'Activo' : 'Inactivo'}
                    </span>
                    <button
                      type="button"
                      role="switch"
                      aria-checked={item.active}
                      aria-label={`${item.active ? 'Desactivar' : 'Activar'} ${item.name}`}
                      className={`relative inline-flex h-7 w-12 items-center rounded-full focus-visible:outline-2 focus-visible:outline-focus disabled:cursor-not-allowed disabled:opacity-50 ${
                        item.active ? 'bg-accent' : 'bg-default'
                      }`}
                      onClick={() => void toggleStatus(item)}
                      disabled={busyUserId === item.id || isCurrentUser}
                      title={isCurrentUser ? 'No puedes desactivar tu propia cuenta' : undefined}
                    >
                      <span
                        aria-hidden="true"
                        className={`absolute top-1 size-5 rounded-full bg-background transition-[left] duration-150 ease-out ${
                          item.active ? 'left-6' : 'left-1'
                        }`}
                      />
                    </button>
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      className="min-h-10 rounded-full bg-surface-secondary px-4 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                      onClick={() => openEditForm(item)}
                    >
                      Editar
                    </button>
                    <button
                      type="button"
                      className="min-h-10 rounded-full bg-surface-secondary px-4 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                      onClick={() => {
                        setActionError(null)
                        setPasswordUser(item)
                      }}
                    >
                      Contraseña
                    </button>
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      )}
    </div>
  )
}
