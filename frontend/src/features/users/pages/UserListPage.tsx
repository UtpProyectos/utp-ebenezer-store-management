import { useEffect, useMemo, useState } from 'react'
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

      <section className="flex flex-col gap-3 rounded-3xl bg-surface p-4 sm:flex-row">
        <label className="sr-only" htmlFor="user-search">Buscar usuario</label>
        <input
          id="user-search"
          className="min-h-11 min-w-0 flex-1 rounded-full border border-separator bg-background px-4 text-sm outline-none focus-visible:ring-2 focus-visible:ring-focus"
          type="search"
          placeholder="Buscar por nombre, usuario o correo"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
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
        <div className="overflow-x-auto rounded-3xl border border-separator bg-surface">
          <table className="w-full min-w-[780px] border-collapse text-left">
            <thead className="text-xs text-muted">
              <tr className="border-b border-separator">
                <th scope="col" className="px-4 py-3 font-medium sm:px-5">Usuario</th>
                <th scope="col" className="px-4 py-3 font-medium">Rol</th>
                <th scope="col" className="px-4 py-3 font-medium">Último acceso</th>
                <th scope="col" className="px-4 py-3 text-center font-medium">Estado</th>
                <th scope="col" className="px-4 py-3 text-right font-medium">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {visibleUsers.map((item) => {
                const isCurrentUser = item.id === currentUser?.id
                return (
                  <tr key={item.id} className="border-b border-separator last:border-0">
                    <th scope="row" className="px-4 py-4 font-semibold sm:px-5">
                      <span className="block">{item.name}{isCurrentUser ? ' (tú)' : ''}</span>
                      <span className="mt-0.5 block text-xs font-normal text-muted">@{item.username}</span>
                      {item.email && <span className="mt-0.5 block text-xs font-normal text-muted">{item.email}</span>}
                    </th>
                    <td className="px-4 py-4 text-sm">{ROLE_LABELS[item.role]}</td>
                    <td className="px-4 py-4 text-sm text-muted">
                      {item.lastLoginAt ? dateFormatter.format(new Date(item.lastLoginAt)) : 'Nunca'}
                    </td>
                    <td className="px-4 py-4 text-center">
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
                      <span className={`ml-2 text-xs ${item.active ? 'text-success' : 'text-muted'}`}>
                        {item.active ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-right">
                      <div className="flex justify-end gap-2">
                        <button
                          type="button"
                          className="min-h-10 rounded-full bg-surface-secondary px-3 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                          onClick={() => openEditForm(item)}
                        >
                          Editar
                        </button>
                        <button
                          type="button"
                          className="min-h-10 rounded-full bg-surface-secondary px-3 text-sm font-semibold hover:bg-default focus-visible:outline-2 focus-visible:outline-focus"
                          onClick={() => {
                            setActionError(null)
                            setPasswordUser(item)
                          }}
                        >
                          Contraseña
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
