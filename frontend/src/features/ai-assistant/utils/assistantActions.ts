import { ROUTES } from '@/app/router/routes'
import type { AssistantActionTarget } from '../types/ai-assistant.types'

/** Screen of each action target sent by the backend (closed list, see AssistantActionTarget). */
export const ACTION_ROUTES: Record<AssistantActionTarget, string> = {
  DASHBOARD: ROUTES.home,
  SALES: ROUTES.sales,
  SALES_HISTORY: ROUTES.salesHistory,
  INVENTORY: ROUTES.inventory,
  SHOPPING_LIST: ROUTES.shoppingList,
  CONSUMPTION: ROUTES.internalConsumption,
  PURCHASES: ROUTES.purchases,
  PRODUCTS: ROUTES.products,
  SUPPLIERS: ROUTES.suppliers,
}
