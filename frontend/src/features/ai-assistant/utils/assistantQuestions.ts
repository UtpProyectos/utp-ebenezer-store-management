import type { ComponentType, SVGProps } from 'react'
import Box from '@gravity-ui/icons/Box'
import ChartColumn from '@gravity-ui/icons/ChartColumn'
import CircleDollar from '@gravity-ui/icons/CircleDollar'
import Clock from '@gravity-ui/icons/Clock'
import Handset from '@gravity-ui/icons/Handset'
import ShoppingCart from '@gravity-ui/icons/ShoppingCart'
import TriangleExclamation from '@gravity-ui/icons/TriangleExclamation'
import type { RoleName } from '@/features/auth/types/auth.types'

export interface SuggestedQuestion {
  text: string
  icon: ComponentType<SVGProps<SVGSVGElement>>
  /** Omitted = every role. Supplier data is admin only in the backend too. */
  roles?: RoleName[]
}

// Same questions as the prototype (prototype/EEAsistente.dc.html).
const QUESTIONS: SuggestedQuestion[] = [
  { text: '¿Qué productos debo reponer?', icon: Box },
  { text: '¿Qué productos están por vencer?', icon: Clock },
  { text: '¿Cuánto vendí hoy?', icon: CircleDollar },
  { text: '¿Qué producto se vende más?', icon: ChartColumn },
  { text: '¿Cuáles fueron mis ventas de esta semana?', icon: ChartColumn },
  { text: '¿Qué productos están en stock crítico?', icon: TriangleExclamation },
  { text: '¿Qué proveedor utilizo más?', icon: Handset, roles: ['ADMIN'] },
  { text: '¿Qué debería comprar mañana?', icon: ShoppingCart },
]

export function questionsFor(role: RoleName | undefined): SuggestedQuestion[] {
  return QUESTIONS.filter((question) => !question.roles || (role !== undefined && question.roles.includes(role)))
}
