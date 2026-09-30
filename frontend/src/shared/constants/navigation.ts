import type { ComponentType, SVGProps } from 'react'
import Box from '@gravity-ui/icons/Box'
import ClockArrowRotateLeft from '@gravity-ui/icons/ClockArrowRotateLeft'
import Gear from '@gravity-ui/icons/Gear'
import House from '@gravity-ui/icons/House'
import ListCheck from '@gravity-ui/icons/ListCheck'
import Persons from '@gravity-ui/icons/Persons'
import Receipt from '@gravity-ui/icons/Receipt'
import ShoppingCart from '@gravity-ui/icons/ShoppingCart'
import Sliders from '@gravity-ui/icons/Sliders'
import Sparkles from '@gravity-ui/icons/Sparkles'
import Tag from '@gravity-ui/icons/Tag'
import Handset from '@gravity-ui/icons/Handset'
import { ROUTES } from '@/app/router/routes'
import type { RoleName } from '@/features/auth/types/auth.types'

type Icon = ComponentType<SVGProps<SVGSVGElement>>

export interface NavItem {
  label: string
  path: string
  icon: Icon
  /** Roles that can see the item. Omitted = every authenticated user. */
  roles?: RoleName[]
  description?: string
}

const ADMIN_ONLY: RoleName[] = ['ADMIN']

/** Sidebar groups, same order as the prototype. The backend still enforces authorization. */
export const NAV_GROUPS: NavItem[][] = [
  [
    { label: 'Inicio', path: ROUTES.home, icon: House },
    { label: 'Ventas', path: ROUTES.sales, icon: ShoppingCart },
    { label: 'Inventario', path: ROUTES.inventory, icon: Box },
    { label: 'Compras', path: ROUTES.shoppingList, icon: ListCheck },
    { label: 'Historial', path: ROUTES.salesHistory, icon: Receipt },
    { label: 'Asistente', path: ROUTES.assistant, icon: Sparkles },
  ],
  [{ label: 'Administración', path: ROUTES.admin, icon: Gear, roles: ADMIN_ONLY }],
]

/** Tiles of the Administration hub (admin only). */
export const ADMIN_ITEMS: NavItem[] = [
  { label: 'Productos', path: ROUTES.products, icon: Box, description: 'Precios, códigos y datos completos' },
  { label: 'Categorías', path: ROUTES.categories, icon: Tag, description: 'Grupos para ordenar lo que vendes' },
  { label: 'Proveedores', path: ROUTES.suppliers, icon: Handset, description: 'A quién le compras y sus teléfonos' },
  { label: 'Usuarios', path: ROUTES.users, icon: Persons, description: 'Quién entra al sistema y con qué rol' },
  { label: 'Configuración', path: ROUTES.settings, icon: Sliders, description: 'Nombre de la tienda, moneda y avisos' },
  { label: 'Historiales', path: ROUTES.history, icon: ClockArrowRotateLeft, description: 'Ventas, ingresos, movimientos y consumo' },
]

export function canSee(item: NavItem, role: RoleName | undefined): boolean {
  return !item.roles || (role !== undefined && item.roles.includes(role))
}
