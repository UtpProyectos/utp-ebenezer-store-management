// Mirrors pe.edu.utp.ebenezer.api.dto.ai.*

export type ChatRole = 'USER' | 'ASSISTANT'

export interface ChatHistoryMessage {
  role: ChatRole
  content: string
}

export interface ChatRequest {
  /** Max 500 characters. */
  message: string
  /** Latest turns, oldest first (max 10). The server does not store the conversation. */
  history: ChatHistoryMessage[]
}

export type AssistantBlockType = 'STATS' | 'LIST' | 'BARS'

export type AssistantTone = 'NEUTRAL' | 'SUCCESS' | 'WARNING' | 'DANGER' | 'INFO' | 'ACCENT'

export interface AssistantBlockItem {
  label: string
  value: string
  /** Status text of LIST rows. */
  badge: string | null
  tone: AssistantTone
  /** Bar length between 0 and 1 (BARS only). */
  ratio: number | null
}

export interface AssistantBlock {
  type: AssistantBlockType
  title: string
  items: AssistantBlockItem[]
}

export type AssistantActionType = 'NAVIGATE' | 'GENERATE_SHOPPING_LIST'

export type AssistantActionTarget =
  | 'DASHBOARD'
  | 'SALES'
  | 'SALES_HISTORY'
  | 'INVENTORY'
  | 'SHOPPING_LIST'
  | 'CONSUMPTION'
  | 'PURCHASES'
  | 'PRODUCTS'
  | 'SUPPLIERS'

export interface AssistantAction {
  type: AssistantActionType
  label: string
  target: AssistantActionTarget
  productIds: number[]
  primary: boolean
}

export interface ChatResponse {
  headline: string
  note: string | null
  outOfScope: boolean
  blocks: AssistantBlock[]
  actions: AssistantAction[]
  /** openai / openrouter */
  provider: string
}

/** One bubble of the chat (client state only). */
export type ChatMessage =
  | { id: number; role: 'USER'; text: string }
  | { id: number; role: 'ASSISTANT'; answer: ChatResponse }
  | { id: number; role: 'ERROR'; text: string }
