import { useState, type FormEvent } from 'react'
import ArrowUp from '@gravity-ui/icons/ArrowUp'
import { MAX_QUESTION_LENGTH } from '../hooks/useAiAssistant'

interface ChatInputProps {
  onSend: (text: string) => void
  disabled: boolean
  /** Quick questions shown above the field once the conversation started. */
  suggestions: string[]
}

export function ChatInput({ onSend, disabled, suggestions }: ChatInputProps) {
  const [text, setText] = useState('')
  const canSend = text.trim().length > 0 && !disabled

  const submit = (event: FormEvent) => {
    event.preventDefault()
    if (!canSend) return
    onSend(text)
    setText('')
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-2.5 border-t border-separator bg-background/60 p-3.5">
      {suggestions.length > 0 && (
        <div className="flex gap-1.5 overflow-x-auto pb-0.5">
          {suggestions.map((question) => (
            <button
              key={question}
              type="button"
              disabled={disabled}
              onClick={() => onSend(question)}
              className="h-10 shrink-0 rounded-full border border-border bg-background px-3 font-medium whitespace-nowrap outline-none transition-colors hover:bg-accent-soft focus-visible:ring-2 focus-visible:ring-focus disabled:opacity-60"
            >
              {question}
            </button>
          ))}
        </div>
      )}
      <div className="flex items-center gap-2 rounded-3xl border-[1.5px] border-border bg-background py-1.5 pr-1.5 pl-3.5 transition-colors focus-within:border-accent">
        <input
          aria-label="Escribe tu pregunta"
          value={text}
          maxLength={MAX_QUESTION_LENGTH}
          onChange={(event) => setText(event.target.value)}
          placeholder="Pregunta, por ejemplo: ¿qué debería comprar mañana?"
          autoFocus
          className="h-10 min-w-0 flex-1 bg-transparent text-[1.0625rem] font-medium outline-none placeholder:text-muted"
        />
        <button
          type="submit"
          aria-label="Enviar"
          disabled={!canSend}
          className="grid size-10 shrink-0 place-items-center rounded-full bg-foreground text-background outline-none transition-[background-color,transform] duration-150 focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.94] disabled:bg-default disabled:text-muted"
        >
          <ArrowUp aria-hidden="true" className="size-5" />
        </button>
      </div>
    </form>
  )
}
