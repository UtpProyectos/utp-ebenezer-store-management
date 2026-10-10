import { useState, type FormEvent } from 'react'
import type { UserResponse } from '@/features/auth/types/auth.types'
import type { RoleOption, UserInput, UserUpdateInput } from '../types/user.types'

interface UserFormProps {
  user?: UserResponse
  roles: RoleOption[]
  isCurrentUser: boolean
  saving: boolean
  error: string | null
  onSubmit: (input: UserInput | UserUpdateInput) => Promise<void>
  onCancel: () => void
}

const fieldClassName =
  'min-h-11 w-full rounded-full border border-separator bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-focus'

const ROLE_LABELS: Record<RoleOption['name'], string> = {
  ADMIN: 'Administrador',
  CASHIER: 'Cajero',
}

export function UserForm({ user, roles, isCurrentUser, saving, error, onSubmit, onCancel }: UserFormProps) {
  const [name, setName] = useState(user?.name ?? '')
  const [username, setUsername] = useState(user?.username ?? '')
  const [email, setEmail] = useState(user?.email ?? '')
  const [role, setRole] = useState(user?.role ?? roles[0]?.name ?? 'CASHIER')
  const [password, setPassword] = useState('')

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (user) {
      const input: UserUpdateInput = { name: name.trim(), email: email.trim() || null, role }
      return onSubmit(input)
    }
    const input: UserInput = {
      name: name.trim(),
      username: username.trim(),
      email: email.trim() || null,
      role,
      password,
    }
    return onSubmit(input)
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
        aria-labelledby="user-form-title"
        className="w-full max-w-lg rounded-3xl bg-surface shadow-xl"
      >
        <form onSubmit={handleSubmit}>
          <header className="border-b border-separator px-5 py-4">
            <h2 id="user-form-title" className="text-lg font-bold">
              {user ? 'Editar usuario' : 'Nuevo usuario'}
            </h2>
          </header>

          <div className="space-y-4 px-5 py-5">
            {error && (
              <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                {error}
              </p>
            )}

            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Nombre completo
              <input
                className={fieldClassName}
                autoFocus
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
                maxLength={120}
              />
            </label>

            {!user && (
              <label className="flex flex-col gap-1.5 text-sm font-medium">
                Nombre de usuario
                <input
                  className={fieldClassName}
                  autoComplete="username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                  required
                  minLength={3}
                  maxLength={80}
                />
              </label>
            )}

            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Correo electrónico <span className="font-normal text-muted">(opcional)</span>
              <input
                className={fieldClassName}
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                maxLength={150}
              />
            </label>

            {!user && (
              <label className="flex flex-col gap-1.5 text-sm font-medium">
                Contraseña inicial
                <input
                  className={fieldClassName}
                  type="password"
                  autoComplete="new-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                  minLength={8}
                  maxLength={100}
                />
                <span className="text-xs font-normal text-muted">Debe tener al menos 8 caracteres.</span>
              </label>
            )}

            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Rol
              <select
                className={fieldClassName}
                value={role}
                onChange={(event) => {
                  const selectedRole = roles.find((option) => option.name === event.target.value)
                  if (selectedRole) setRole(selectedRole.name)
                }}
                disabled={isCurrentUser}
                required
              >
                {roles.map((option) => (
                  <option key={option.id} value={option.name}>{ROLE_LABELS[option.name]}</option>
                ))}
              </select>
              {isCurrentUser && (
                <span className="text-xs font-normal text-muted">No puedes cambiar tu propio rol.</span>
              )}
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
              disabled={saving || roles.length === 0 || !name.trim() || (!user && (!username.trim() || password.length < 8))}
            >
              {saving ? 'Guardando…' : user ? 'Guardar cambios' : 'Crear usuario'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}
