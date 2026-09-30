import { BrandMark } from '@/shared/components/ui/BrandMark'
import { LoginBrandPanel } from '../components/LoginBrandPanel'
import { LoginForm } from '../components/LoginForm'

export function LoginPage() {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <LoginBrandPanel />

      <main className="flex items-center justify-center px-5 py-10">
        <div className="flex w-full max-w-sm animate-enter flex-col gap-8 motion-reduce:animate-none">
          <div className="flex flex-col gap-2">
            <BrandMark className="mb-2 size-11 text-lg lg:hidden" />
            <h1 className="text-3xl font-bold tracking-tight">¡Bienvenido!</h1>
            <p className="text-muted">Ingresa para abrir la caja de hoy.</p>
          </div>
          <LoginForm />
        </div>
      </main>
    </div>
  )
}
