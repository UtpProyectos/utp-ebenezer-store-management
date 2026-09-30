import { createBrowserRouter, Navigate, type RouteObject } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { AdminPage } from '@/features/admin/pages/AdminPage'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { SettingsPage } from '@/features/settings/pages/SettingsPage'
import { AppLayout } from '@/shared/components/layout/AppLayout'
import { PagePlaceholder } from '@/shared/components/ui/PagePlaceholder'
import { GuestRoute } from './GuestRoute'
import { ProtectedRoute } from './ProtectedRoute'
import type { RouteHandle } from './routeHandle'
import { ROUTES } from './routes'

// TODO(team): replace <PagePlaceholder /> with each feature page as it is implemented.
function page(path: string, handle: RouteHandle, element = <PagePlaceholder />): RouteObject {
  return { path, handle, element }
}

const SALES = { label: 'Ventas', path: ROUTES.sales }
const INVENTORY = { label: 'Inventario', path: ROUTES.inventory }
const ADMIN = { label: 'Administración', path: ROUTES.admin }

const router = createBrowserRouter([
  {
    element: <GuestRoute />,
    children: [{ path: ROUTES.login, element: <LoginPage /> }],
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          page(ROUTES.home, { title: 'Inicio', subtitle: 'Resumen de hoy' }, <DashboardPage />),
          page(ROUTES.sales, { title: 'Ventas', subtitle: 'Toca los productos y luego presiona Cobrar' }),
          page(ROUTES.internalConsumption, {
            title: 'Consumo interno',
            subtitle: 'Productos que se usan en la tienda y no se venden',
            parent: SALES,
          }),
          page(ROUTES.inventory, { title: 'Inventario', subtitle: 'Cuánto tienes de cada producto' }),
          page(ROUTES.purchases, {
            title: 'Ingreso de mercadería',
            subtitle: 'Anota lo que llegó a la tienda',
            parent: INVENTORY,
          }),
          page(ROUTES.shoppingList, { title: 'Lista de compra', subtitle: 'Productos por agotarse que debes comprar' }),
          page(ROUTES.salesHistory, { title: 'Historial de ventas', subtitle: 'Corrige o anula ventas. Todo queda anotado.' }),
          page(ROUTES.assistant, { title: 'Asistente', subtitle: 'Pregunta lo que quieras sobre tu tienda' }),
          {
            // Administration: admin only. The backend enforces the same rule.
            element: <ProtectedRoute roles={['ADMIN']} />,
            children: [
              page(ROUTES.admin, { title: 'Administración', subtitle: 'Datos de la tienda, usuarios y reportes' }, <AdminPage />),
              page(ROUTES.products, { title: 'Productos', subtitle: 'Precios y datos de lo que vendes', parent: ADMIN }),
              page(ROUTES.categories, { title: 'Categorías', subtitle: 'Grupos para ordenar tus productos', parent: ADMIN }),
              page(ROUTES.suppliers, { title: 'Proveedores', subtitle: 'A quién le compras', parent: ADMIN }),
              page(ROUTES.users, { title: 'Usuarios', subtitle: 'Quién puede entrar al sistema', parent: ADMIN }),
              page(ROUTES.settings, { title: 'Configuración', subtitle: 'Datos de la tienda y avisos', parent: ADMIN }, <SettingsPage />),
              page(ROUTES.history, { title: 'Historiales', subtitle: 'Lo que pasó en la tienda, día por día', parent: ADMIN }),
            ],
          },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to={ROUTES.home} replace /> },
])

export function AppRouter() {
  return <RouterProvider router={router} />
}
