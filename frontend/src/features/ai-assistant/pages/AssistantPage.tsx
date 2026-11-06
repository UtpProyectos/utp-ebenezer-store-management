import { useMemo } from 'react'
import { useAuth } from '@/features/auth'
import { AssistantPanel } from '../components/AssistantPanel'
import { AssistantSidebar } from '../components/AssistantSidebar'
import { useAiAssistant } from '../hooks/useAiAssistant'
import { useAssistantAction } from '../hooks/useAssistantAction'
import { questionsFor } from '../utils/assistantQuestions'

const STARTER_COUNT = 6
const SUGGESTION_COUNT = 5

/** Asistente: questions about the store answered by the backend AI (prototype/EEAsistente.dc.html). */
export function AssistantPage() {
  const { user } = useAuth()
  const { messages, thinking, ask, clear } = useAiAssistant()
  const action = useAssistantAction()

  const questions = useMemo(() => questionsFor(user?.role), [user?.role])
  const asked = useMemo(
    () => new Set(messages.flatMap((message) => (message.role === 'USER' ? [message.text] : []))),
    [messages],
  )
  const suggestions = questions
    .map((question) => question.text)
    .filter((text) => !asked.has(text))
    .slice(0, SUGGESTION_COUNT)
  const more = [...questions.slice(STARTER_COUNT), ...questions.slice(0, 2)].map((question) => question.text)

  return (
    <div className="mx-auto grid w-full max-w-410 items-start gap-5 lg:grid-cols-[minmax(0,1fr)_18.75rem]">
      <AssistantPanel
        messages={messages}
        thinking={thinking}
        starters={questions.slice(0, STARTER_COUNT)}
        suggestions={suggestions}
        onAsk={(question) => void ask(question)}
        onAction={(selected) => void action.run(selected)}
        pendingAction={action.pending}
        actionError={action.error}
      />
      <div className="hidden lg:block">
        <AssistantSidebar questions={more} onAsk={(question) => void ask(question)} onClear={clear} disabled={thinking} />
      </div>
      {messages.length > 0 && (
        <button
          type="button"
          onClick={clear}
          className="h-11 rounded-full border border-border bg-background font-semibold text-muted outline-none focus-visible:ring-2 focus-visible:ring-focus lg:hidden"
        >
          Nueva conversación
        </button>
      )}
    </div>
  )
}
