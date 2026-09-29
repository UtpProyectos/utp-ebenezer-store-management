import { createBrowserRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { AppLayout } from '@/shared/components/layout/AppLayout'
import { ROUTES } from './routes'

const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [{ path: ROUTES.home, element: <DashboardPage /> }],
  },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
