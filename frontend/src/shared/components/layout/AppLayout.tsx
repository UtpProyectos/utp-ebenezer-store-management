import { Outlet } from 'react-router'

export function AppLayout() {
  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-separator">
        <div className="mx-auto flex h-16 max-w-7xl items-center px-4 sm:px-6">
          <span className="text-lg font-semibold">Eben-Ezer</span>
        </div>
      </header>
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8 sm:px-6">
        <Outlet />
      </main>
    </div>
  )
}
