// Text size preference ("Tamaño de letra"), saved per device like in the prototype.
// It scales the root font size: Tailwind and HeroUI are rem-based, so the whole UI scales with it.

export type TextSize = 'small' | 'normal' | 'large' | 'extra-large'

export interface TextSizeOption {
  id: TextSize
  label: string
  /** Root font-size scale (same factors as the prototype zoom). */
  scale: number
  /** Size of the "Aa" preview in the selector. */
  previewClassName: string
}

export const TEXT_SIZE_OPTIONS: TextSizeOption[] = [
  { id: 'small', label: 'Pequeño', scale: 0.76, previewClassName: 'text-xs' },
  { id: 'normal', label: 'Normal', scale: 0.86, previewClassName: 'text-base' },
  { id: 'large', label: 'Grande', scale: 1, previewClassName: 'text-xl' },
  { id: 'extra-large', label: 'Muy grande', scale: 1.12, previewClassName: 'text-2xl' },
]

export const DEFAULT_TEXT_SIZE: TextSize = 'normal'

const STORAGE_KEY = 'ebenezer.text-size'

function isTextSize(value: unknown): value is TextSize {
  return TEXT_SIZE_OPTIONS.some((option) => option.id === value)
}

export function readTextSize(): TextSize {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    return isTextSize(stored) ? stored : DEFAULT_TEXT_SIZE
  } catch {
    return DEFAULT_TEXT_SIZE
  }
}

export function applyTextSize(size: TextSize) {
  const option = TEXT_SIZE_OPTIONS.find((item) => item.id === size) ?? TEXT_SIZE_OPTIONS[1]
  document.documentElement.style.fontSize = `${option.scale * 100}%`
}

export function saveTextSize(size: TextSize) {
  applyTextSize(size)
  try {
    localStorage.setItem(STORAGE_KEY, size)
  } catch {
    // Preference only; it still applies for this session.
  }
}
