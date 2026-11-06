import { useState } from 'react'
import { useNavigate } from 'react-router'
import { shoppingListApi } from '@/features/shopping-list'
import type { AssistantAction } from '../types/ai-assistant.types'
import { ACTION_ROUTES } from '../utils/assistantActions'

/**
 * Runs the buttons under an answer. The assistant never changes data by itself: the shopping list is
 * only generated when the user presses the button.
 */
export function useAssistantAction() {
  const navigate = useNavigate()
  const [pending, setPending] = useState<AssistantAction | null>(null)
  const [error, setError] = useState<string | null>(null)

  const run = async (action: AssistantAction) => {
    setError(null)
    if (action.type === 'NAVIGATE') {
      navigate(ACTION_ROUTES[action.target])
      return
    }
    setPending(action)
    try {
      await shoppingListApi.generate({ productIds: action.productIds })
      navigate(ACTION_ROUTES[action.target])
    } catch {
      setError('No se pudo preparar la lista de compra. Inténtalo desde la pantalla Compras.')
    } finally {
      setPending(null)
    }
  }

  return { run, pending, error }
}
