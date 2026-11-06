import ArrowRight from '@gravity-ui/icons/ArrowRight'
import TriangleExclamation from '@gravity-ui/icons/TriangleExclamation'
import { Spinner } from '@heroui/react'
import type { AssistantAction, ChatMessage as ChatMessageData } from '../types/ai-assistant.types'
import { AssistantAvatar } from './AssistantAvatar'
import { AssistantBlockView } from './AssistantBlockView'

interface ChatMessageProps {
  message: ChatMessageData
  onAction: (action: AssistantAction) => void
  pendingAction: AssistantAction | null
}

export function ChatMessage({ message, onAction, pendingAction }: ChatMessageProps) {
  if (message.role === 'USER') {
    return (
      <div className="max-w-[80%] self-end rounded-[1.5rem_0.875rem_0.25rem_0.875rem] bg-accent px-3.5 py-2.5 font-medium text-accent-foreground">
        {message.text}
      </div>
    )
  }

  if (message.role === 'ERROR') {
    return (
      <div role="alert" className="flex items-start gap-3">
        <AssistantAvatar />
        <p className="flex items-center gap-2 rounded-2xl bg-danger-soft px-3.5 py-2.5 font-medium text-danger-soft-foreground">
          <TriangleExclamation aria-hidden="true" className="size-5 shrink-0" />
          {message.text}
        </p>
      </div>
    )
  }

  const { answer } = message
  return (
    <div className="flex items-start gap-3">
      <AssistantAvatar />
      <div className="flex min-w-0 flex-1 flex-col gap-3">
        <p className="text-lg leading-snug font-semibold text-pretty">{answer.headline}</p>
        {answer.blocks.map((block, index) => (
          <AssistantBlockView key={`${block.title}-${index}`} block={block} />
        ))}
        {answer.note && <p className="leading-relaxed text-muted">{answer.note}</p>}
        {answer.actions.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {answer.actions.map((action) => {
              const isPending = pendingAction === action
              return (
                <button
                  key={`${action.type}-${action.target}`}
                  type="button"
                  disabled={pendingAction !== null}
                  onClick={() => onAction(action)}
                  className={`inline-flex h-11.5 items-center gap-1.5 rounded-full px-3.5 font-semibold outline-none transition-[filter,transform] duration-150 ease-out hover:brightness-95 focus-visible:ring-2 focus-visible:ring-focus focus-visible:ring-offset-2 active:scale-[0.97] disabled:opacity-60 ${
                    action.primary ? 'bg-foreground text-background' : 'border border-border bg-background text-foreground'
                  }`}
                >
                  {action.label}
                  {isPending ? <Spinner size="sm" color="current" /> : <ArrowRight aria-hidden="true" className="size-4.5" />}
                </button>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
