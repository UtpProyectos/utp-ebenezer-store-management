type BrandMarkProps = {
  className?: string
}

// Eben-Ezer "EE" logo mark, shared by the login screen and the app header.
export function BrandMark({ className = 'size-11 text-lg' }: BrandMarkProps) {
  return (
    <span
      aria-hidden="true"
      className={`grid shrink-0 place-items-center rounded-full bg-accent font-extrabold tracking-tight text-accent-foreground ${className}`}
    >
      EE
    </span>
  )
}
