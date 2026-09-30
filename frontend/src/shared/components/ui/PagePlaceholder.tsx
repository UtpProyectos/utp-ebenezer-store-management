import CircleInfo from '@gravity-ui/icons/CircleInfo'

// Temporary content for modules not implemented yet. Replace the route element with the real page.
export function PagePlaceholder() {
  return (
    <div className="flex items-center gap-4 rounded-3xl bg-surface p-5">
      <span className="grid size-11 shrink-0 place-items-center rounded-full bg-surface-tertiary">
        <CircleInfo className="size-5 text-muted" />
      </span>
      <p className="text-muted">Este módulo aún está en construcción.</p>
    </div>
  )
}
