import { useState, type FormEvent } from 'react'

interface ResetPasswordFormProps {
  userName: string
  saving: boolean
  error: string | null
  onSubmit: (password: string) => Promise<void>
  onCancel: () => void
}

export function ResetPasswordForm({ userName, saving, error, onSubmit, onCancel }: ResetPasswordFormProps) {
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const fieldClassName =
    'min-h-11 w-full rounded-full border border-separator bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-focus'

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (password === confirmation) return onSubmit(password)
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
        aria-labelledby="reset-password-title"
        className="w-full max-w-md rounded-3xl bg-surface shadow-xl"
      >
        <form onSubmit={handleSubmit}>
          <header className="border-b border-separator px-5 py-4">
            <h2 id="reset-password-title" className="text-lg font-bold">Restablecer contraseña</h2>
            <p className="mt-1 text-sm text-muted">Usuario: {userName}</p>
          </header>
          <div className="space-y-4 px-5 py-5">
            {error && (
              <p role="alert" className="rounded-xl bg-danger-soft px-4 py-3 text-sm text-danger-soft-foreground">
                {error}
              </p>
            )}
            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Nueva contraseña
              <input
                className={fieldClassName}
                type="password"
                autoComplete="new-password"
                autoFocus
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                minLength={8}
                maxLength={100}
                required
              />
            </label>
            <label className="flex flex-col gap-1.5 text-sm font-medium">
              Confirmar contraseña
              <input
                className={fieldClassName}
                type="password"
                autoComplete="new-password"
                value={confirmation}
                onChange={(event) => setConfirmation(event.target.value)}
                minLength={8}
                maxLength={100}
                required
              />
            </label>
            {confirmation && password !== confirmation && (
              <p className="text-sm text-danger" role="status">Las contraseñas no coinciden.</p>
            )}
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
              disabled={saving || password.length < 8 || password !== confirmation}
            >
              {saving ? 'Guardando…' : 'Guardar contraseña'}
            </button>
          </footer>
        </form>
      </section>
    </div>
  )
}
