import { useState } from 'react'
import Minus from '@gravity-ui/icons/Minus'
import Plus from '@gravity-ui/icons/Plus'
import { formatQuantity } from '@/features/inventory'

interface QuantityStepperProps {
  value: number
  label: string
  onChange: (value: number) => void
}

const STEP_BUTTON =
  'grid size-9 place-items-center rounded-full bg-surface-secondary outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-tertiary focus-visible:ring-2 focus-visible:ring-focus active:scale-90 disabled:opacity-40'

/** − / value / + pill of the prototype. Typed values are applied on blur or Enter; the minimum is 1. */
export function QuantityStepper({ value, label, onChange }: QuantityStepperProps) {
  const [draft, setDraft] = useState<string | null>(null)

  const commit = () => {
    if (draft === null) return
    const parsed = Number(draft.replace(',', '.'))
    setDraft(null)
    if (Number.isFinite(parsed) && parsed > 0 && parsed !== value) onChange(parsed)
  }

  return (
    <div className="flex h-11 w-33 items-center justify-between rounded-full bg-surface px-1">
      <button
        type="button"
        aria-label={`Menos ${label}`}
        className={STEP_BUTTON}
        disabled={value <= 1}
        onClick={() => onChange(Math.max(1, value - 1))}
      >
        <Minus aria-hidden="true" className="size-4" />
      </button>
      <input
        value={draft ?? formatQuantity(value)}
        inputMode="decimal"
        aria-label={`Cantidad a comprar de ${label}`}
        onChange={(event) => setDraft(event.target.value)}
        onBlur={commit}
        onKeyDown={(event) => {
          if (event.key === 'Enter') event.currentTarget.blur()
        }}
        className="h-full w-11 bg-transparent text-center text-[1.0625rem] font-bold tabular-nums outline-none"
      />
      <button type="button" aria-label={`Más ${label}`} className={STEP_BUTTON} onClick={() => onChange(value + 1)}>
        <Plus aria-hidden="true" className="size-4" />
      </button>
    </div>
  )
}
