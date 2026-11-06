import { useCallback, useRef, useState } from 'react'
import { ApiClientError } from '@/shared/services/apiClient'
import { aiAssistantApi } from '../services/aiAssistantApi'
import type { ChatHistoryMessage, ChatMessage } from '../types/ai-assistant.types'

/** Same limits as the backend ChatRequest. */
export const MAX_QUESTION_LENGTH = 500
const MAX_HISTORY = 10
const MAX_HISTORY_CONTENT = 1000

function errorMessage(error: unknown) {
  if (error instanceof ApiClientError) {
    if (error.status === 503) return 'El asistente no está disponible en este momento. Inténtalo en unos minutos.'
    if (error.status === 400) return `La pregunta es muy larga. Escríbela en menos de ${MAX_QUESTION_LENGTH} caracteres.`
  }
  return 'No pude responder. Revisa tu conexión e inténtalo otra vez.'
}

// The server keeps no memory: the latest turns travel with each question.
function toHistory(messages: ChatMessage[]): ChatHistoryMessage[] {
  return messages
    .flatMap((message): ChatHistoryMessage[] => {
      if (message.role === 'USER') return [{ role: 'USER', content: message.text }]
      if (message.role === 'ASSISTANT') {
        const { headline, note } = message.answer
        return [{ role: 'ASSISTANT', content: note ? `${headline} ${note}` : headline }]
      }
      return []
    })
    .map((message) => ({ ...message, content: message.content.slice(0, MAX_HISTORY_CONTENT) }))
    .slice(-MAX_HISTORY)
}

/** Chat state of the Asistente screen. Lives only in memory: leaving the screen starts a new conversation. */
export function useAiAssistant() {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [thinking, setThinking] = useState(false)
  const nextId = useRef(0)
  // Answers that arrive after "Nueva conversación" are ignored.
  const conversation = useRef(0)

  const ask = useCallback(
    async (rawText: string) => {
      const text = rawText.trim().slice(0, MAX_QUESTION_LENGTH)
      if (!text || thinking) return
      const current = conversation.current
      const history = toHistory(messages)
      setMessages((previous) => [...previous, { id: nextId.current++, role: 'USER', text }])
      setThinking(true)
      try {
        const answer = await aiAssistantApi.chat({ message: text, history })
        if (conversation.current !== current) return
        setMessages((previous) => [...previous, { id: nextId.current++, role: 'ASSISTANT', answer }])
      } catch (error) {
        if (conversation.current !== current) return
        setMessages((previous) => [...previous, { id: nextId.current++, role: 'ERROR', text: errorMessage(error) }])
      } finally {
        if (conversation.current === current) setThinking(false)
      }
    },
    [messages, thinking],
  )

  const clear = useCallback(() => {
    conversation.current += 1
    setMessages([])
    setThinking(false)
  }, [])

  return { messages, thinking, ask, clear }
}
