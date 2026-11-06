import { useEffect, useRef } from 'react'
import type { AssistantAction, ChatMessage as ChatMessageData } from '../types/ai-assistant.types'
import type { SuggestedQuestion } from '../utils/assistantQuestions'
import { AssistantAvatar } from './AssistantAvatar'
import { ChatInput } from './ChatInput'
import { ChatMessage } from './ChatMessage'

interface AssistantPanelProps {
  messages: ChatMessageData[]
  thinking: boolean
  starters: SuggestedQuestion[]
  suggestions: string[]
  onAsk: (question: string) => void
  onAction: (action: AssistantAction) => void
  pendingAction: AssistantAction | null
  actionError: string | null
}

export function AssistantPanel({
  messages,
  thinking,
  starters,
  suggestions,
  onAsk,
  onAction,
  pendingAction,
  actionError,
}: AssistantPanelProps) {
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages.length, thinking])

  const isEmpty = messages.length === 0 && !thinking

  return (
    <section className="flex min-h-[calc(100dvh-11rem)] flex-col overflow-hidden rounded-[1.75rem] bg-surface lg:min-h-[calc(100dvh-10rem)]">
      <div
        ref={scrollRef}
        aria-live="polite"
        className="flex flex-1 flex-col gap-4.5 overflow-x-hidden overflow-y-auto p-4 sm:p-5.5 lg:max-h-[calc(100dvh-18rem)]"
      >
        {isEmpty && (
          <div className="flex flex-col gap-4.5 px-1 pt-3">
            <div className="flex items-center gap-3">
              <AssistantAvatar size="lg" />
              <div className="flex flex-col gap-0.5">
                <h2 className="text-[1.375rem] font-bold">¿Qué quieres saber de tu bodega?</h2>
                <p className="font-medium text-muted">
                  Respondo con tus ventas, stock, lotes y proveedores. Nada se modifica sin tu confirmación.
                </p>
              </div>
            </div>
            <div className="grid grid-cols-[repeat(auto-fit,minmax(13.75rem,1fr))] gap-2.5">
              {starters.map(({ text, icon: Icon }) => (
                <button
                  key={text}
                  type="button"
                  onClick={() => onAsk(text)}
                  className="flex flex-col gap-2 rounded-3xl border border-transparent bg-background/75 p-3.5 text-left outline-none transition-[border-color,background-color,transform] duration-150 hover:border-border hover:bg-background focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.98]"
                >
                  <Icon aria-hidden="true" className="size-5.5" />
                  <span className="leading-snug font-semibold">{text}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((message) => (
          <ChatMessage key={message.id} message={message} onAction={onAction} pendingAction={pendingAction} />
        ))}

        {thinking && (
          <div className="flex items-center gap-3" role="status">
            <AssistantAvatar />
            <span className="flex items-center gap-1.5 font-medium text-muted">
              Revisando tus datos
              {[0, 150, 300].map((delay) => (
                <span
                  key={delay}
                  aria-hidden="true"
                  className="size-1.5 animate-pulse rounded-full bg-accent"
                  style={{ animationDelay: `${delay}ms` }}
                />
              ))}
            </span>
          </div>
        )}

        {actionError && (
          <p role="alert" className="rounded-2xl bg-danger-soft px-3.5 py-2.5 font-medium text-danger-soft-foreground">
            {actionError}
          </p>
        )}
      </div>

      <ChatInput onSend={onAsk} disabled={thinking} suggestions={isEmpty ? [] : suggestions} />
    </section>
  )
}
