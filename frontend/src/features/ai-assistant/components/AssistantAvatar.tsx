import Sparkles from '@gravity-ui/icons/Sparkles'

/** Assistant mark: lavender (token ai) only on this screen, like the prototype. */
export function AssistantAvatar({ size = 'sm' }: { size?: 'sm' | 'lg' }) {
  const large = size === 'lg'
  return (
    <span
      aria-hidden="true"
      className={`grid shrink-0 place-items-center ${
        large
          ? 'size-11 rounded-[1.125rem] bg-linear-to-br from-accent to-ai text-accent-foreground'
          : 'size-8 rounded-[0.875rem] bg-surface-tertiary text-foreground'
      }`}
    >
      <Sparkles className={large ? 'size-6' : 'size-5'} />
    </span>
  )
}
