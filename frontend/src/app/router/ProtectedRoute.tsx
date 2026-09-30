import { Spinner } from '@heroui/react'
import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '@/features/auth/hooks/useAuth'
import type { RoleName } from '@/features/auth/types/auth.types'
import { ROUTES } from './routes'

type ProtectedRouteProps = {
  /** When set, only these roles can enter. The backend still enforces authorization. */
  roles?: RoleName[]
}

export function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { status, user } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return (
      <div className="grid min-h-screen place-items-center">
        <Spinner size="lg" />
      </div>
    )
  }

  if (status === 'anonymous' || !user) {
    return <Navigate to={ROUTES.login} replace state={{ from: location.pathname }} />
  }

  if (roles && !roles.includes(user.role)) {
    return <Navigate to={ROUTES.home} replace />
  }

  return <Outlet />
}
