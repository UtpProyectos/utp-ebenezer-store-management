import { apiClient } from '@/shared/services/apiClient'
import type { ChatRequest, ChatResponse } from '../types/ai-assistant.types'

// The browser only talks to our backend; OpenAI / OpenRouter keys never reach the frontend.
export const aiAssistantApi = {
  chat: (request: ChatRequest) => apiClient.post<ChatResponse>('/ai/chat', request),
}
