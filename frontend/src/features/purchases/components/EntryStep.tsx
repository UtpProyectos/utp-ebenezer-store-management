import type { ReactNode } from 'react'

interface EntryStepProps {
  number: number
  title: string
  done: boolean
  children: ReactNode
}

// Numbered card of the 3-step entry; the number turns orange once the step is complete.
export function EntryStep({ number, title, done, children }: EntryStepProps) {
  return (
    <section className="flex flex-col gap-4 rounded-3xl bg-surface p-5 sm:p-6">
      <div className="flex items-center gap-3">
        <span
          className={`grid size-10 shrink-0 place-items-center rounded-full font-bold transition-colors duration-200 ${
            done ? 'bg-accent text-accent-foreground' : 'bg-surface-tertiary text-foreground'
          }`}
        >
          {number}
        </span>
        <h2 className="text-xl font-bold">{title}</h2>
      </div>
      {children}
    </section>
  )
}
