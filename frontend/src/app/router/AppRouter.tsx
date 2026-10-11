import { createBrowserRouter, Navigate, type RouteObject } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { AdminPage } from '@/features/admin/pages/AdminPage'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { CategoryListPage } from '@/features/categories/pages/CategoryListPage'
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage'
import { HistoryPage } from '@/features/history'
import { InventoryPage } from '@/features/inventory'
import { ProductListPage } from '@/features/products/pages/ProductListPage'
import { PurchaseEntryPage } from '@/features/purchases'
import { SalePage, SalesHistoryPage } from '@/features/sales'
import { ShoppingListPage } from '@/features/shopping-list'
import { SupplierListPage } from '@/features/suppliers/pages/SupplierListPage'
import { SettingsPage } from '@/features/settings/pages/SettingsPage'
import { UserListPage } from '@/features/users/pages/UserListPage'
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
const CONFIGURATION = { label: 'Configuración', path: ROUTES.admin }

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
          page(ROUTES.sales, { title: 'Ventas', subtitle: 'Toca los productos y luego presiona Cobrar' }, <SalePage />),
          page(ROUTES.internalConsumption, {
            title: 'Consumo interno',
            subtitle: 'Productos que se usan en la tienda y no se venden',
            parent: SALES,
          }),
          page(ROUTES.inventory, { title: 'Inventario', subtitle: 'Cuánto tienes de cada producto' }, <InventoryPage />),
          page(ROUTES.purchases, {
            title: 'Ingreso de mercadería',
            subtitle: 'Anota lo que llegó a la tienda en 3 pasos',
            parent: INVENTORY,
          }, <PurchaseEntryPage />),
          page(ROUTES.shoppingList, { title: 'Lista de compra', subtitle: 'Productos por agotarse que debes comprar' }, <ShoppingListPage />),
          page(ROUTES.salesHistory, { title: 'Historial de ventas', subtitle: 'Corrige o anula ventas. Todo queda anotado.' }, <SalesHistoryPage />),
          page(ROUTES.assistant, { title: 'Asistente', subtitle: 'Pregunta lo que quieras sobre tu tienda' }),
          // Configuración: every role. Cashiers only create catalog records (the backend enforces it).
          page(ROUTES.admin, { title: 'Configuración', subtitle: 'Catálogo, usuarios, reportes y preferencias' }, <AdminPage />),
          page(ROUTES.products, { title: 'Productos', subtitle: 'Precios y datos de lo que vendes', parent: CONFIGURATION }, <ProductListPage />),
          page(ROUTES.categories, { title: 'Categorías', subtitle: 'Grupos para ordenar tus productos', parent: CONFIGURATION }, <CategoryListPage />),
          page(ROUTES.suppliers, { title: 'Proveedores', subtitle: 'A quién le compras', parent: CONFIGURATION }, <SupplierListPage />),
          page(ROUTES.settings, { title: 'Preferencias', subtitle: 'Tamaño de letra y datos de la tienda', parent: CONFIGURATION }, <SettingsPage />),
          {
            // Admin only. The backend enforces the same rule.
            element: <ProtectedRoute roles={['ADMIN']} />,
            children: [
              page(ROUTES.users, { title: 'Usuarios', subtitle: 'Quién puede entrar al sistema', parent: CONFIGURATION }, <UserListPage />),
              page(ROUTES.history, { title: 'Historiales', subtitle: 'Lo que pasó en la tienda, día por día', parent: CONFIGURATION }, <HistoryPage />),
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
