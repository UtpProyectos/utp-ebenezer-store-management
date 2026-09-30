import { Spinner } from '@heroui/react'
import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { ROUTES } from './routes'

type GuestLocationState = { from?: string } | null

// Pages only for visitors without a session (login). Once authenticated, return to where the user was going.
export function GuestRoute() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return (
      <div className="grid min-h-screen place-items-center">
        <Spinner size="lg" />
      </div>
    )
  }

  if (status === 'authenticated') {
    const from = (location.state as GuestLocationState)?.from
    return <Navigate to={from && from !== ROUTES.login ? from : ROUTES.home} replace />
  }

  return <Outlet />
}
