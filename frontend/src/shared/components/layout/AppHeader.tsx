import { Button } from '@heroui/react'
import ArrowLeft from '@gravity-ui/icons/ArrowLeft'
import Bars from '@gravity-ui/icons/Bars'
import { useMatches, useNavigate } from 'react-router'
import { isRouteHandle } from '@/app/router/routeHandle'
import { GlobalSearch } from './GlobalSearch'
import { UserMenu } from './UserMenu'

type AppHeaderProps = {
  onOpenMenu: () => void
}

export function AppHeader({ onOpenMenu }: AppHeaderProps) {
  const navigate = useNavigate()
  // The deepest route with metadata defines the title, subtitle and back button.
  const handle = useMatches()
    .map((match) => match.handle)
    .filter(isRouteHandle)
    .at(-1)

  return (
    <header className="sticky top-0 z-30 flex h-20 shrink-0 items-center gap-3 bg-background/90 px-4 backdrop-blur-md sm:px-7">
      <Button isIconOnly variant="secondary" aria-label="Abrir menú" onPress={onOpenMenu} className="lg:hidden">
        <Bars className="size-5" />
      </Button>

      {handle?.parent && (
        <button
          type="button"
          onClick={() => navigate(handle.parent!.path)}
          className="flex h-12 shrink-0 items-center gap-2 rounded-full bg-surface p-1.5 font-semibold sm:pr-4 outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.97]"
        >
          <span className="grid size-9 place-items-center rounded-full bg-surface-tertiary">
            <ArrowLeft className="size-4" />
          </span>
          <span className="hidden sm:inline">{handle.parent.label}</span>
        </button>
      )}

      <div className="flex min-w-0 flex-1 flex-col gap-0.5">
        <h1 className="truncate text-xl font-bold tracking-tight sm:text-2xl">{handle?.title}</h1>
        {handle?.subtitle && <p className="hidden truncate text-muted sm:block">{handle.subtitle}</p>}
      </div>

      <GlobalSearch />
      <UserMenu />
    </header>
  )
}
