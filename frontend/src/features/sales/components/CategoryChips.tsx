export const ALL_CATEGORIES = 'Todos'

interface CategoryChipsProps {
  categories: string[]
  value: string
  onChange: (category: string) => void
}

export function CategoryChips({ categories, value, onChange }: CategoryChipsProps) {
  return (
    <div role="tablist" aria-label="Filtrar por categoría" className="-mb-0.5 flex gap-1.5 overflow-x-auto pb-0.5">
      {[ALL_CATEGORIES, ...categories].map((category) => {
        const selected = category === value
        return (
          <button
            key={category}
            type="button"
            role="tab"
            aria-selected={selected}
            onClick={() => onChange(category)}
            className={`h-10.5 shrink-0 rounded-full px-4 font-semibold whitespace-nowrap outline-none transition-[background-color,color,transform] duration-150 ease-out focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.96] ${
              selected ? 'bg-ai text-white' : 'bg-background text-foreground/80 hover:bg-surface-tertiary'
            }`}
          >
            {category}
          </button>
        )
      })}
    </div>
  )
}
