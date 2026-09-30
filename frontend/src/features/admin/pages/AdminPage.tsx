import ArrowRight from '@gravity-ui/icons/ArrowRight'
import { Link } from 'react-router'
import { ADMIN_ITEMS } from '@/shared/constants/navigation'

// Administration hub (admin only): one tile per management screen, as in the prototype.
export function AdminPage() {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
      {ADMIN_ITEMS.map(({ label, path, icon: Icon, description }) => (
        <Link
          key={path}
          to={path}
          className="group flex flex-col gap-6 rounded-3xl bg-surface p-5 outline-none transition-[background-color,transform] duration-150 ease-out hover:bg-surface-secondary focus-visible:ring-2 focus-visible:ring-focus active:scale-[0.98]"
        >
          <span className="grid size-14 place-items-center rounded-full bg-surface-tertiary text-accent">
            <Icon className="size-6" />
          </span>
          <div className="flex items-end justify-between gap-3">
            <div className="flex flex-col gap-1">
              <span className="text-lg font-bold">{label}</span>
              <span className="text-muted">{description}</span>
            </div>
            <span className="grid size-11 shrink-0 place-items-center rounded-full bg-foreground text-background">
              <ArrowRight className="size-5" />
            </span>
          </div>
        </Link>
      ))}
    </div>
  )
}
