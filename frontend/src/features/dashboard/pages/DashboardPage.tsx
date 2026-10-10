import { Button, Spinner } from '@heroui/react'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { AttentionList } from '../components/AttentionList'
import { DaySummary } from '../components/DaySummary'
import { QuickActions } from '../components/QuickActions'
import { TodayActivity } from '../components/TodayActivity'
import { useDashboard } from '../hooks/useDashboard'

function greeting(name: string | undefined): string {
  const hour = new Date().getHours()
  const salutation = hour < 12 ? 'Buenos días' : hour < 19 ? 'Buenas tardes' : 'Buenas noches'
  const firstName = name?.trim().split(/\s+/)[0]
  return firstName ? `${salutation}, ${firstName}` : salutation
}

// Home: only what is already built (sales, inventory, purchases). The AI assistant block is added with its module.
export function DashboardPage() {
  const { user } = useAuth()
  const dashboard = useDashboard()

  return (
    <div className="mx-auto flex max-w-[102.5rem] flex-col gap-8">
      <h2 className="text-4xl leading-tight font-bold tracking-tight">{greeting(user?.name)}</h2>

      <QuickActions />

      {dashboard.initialLoading ? (
        <div className="grid place-items-center py-16">
          <Spinner size="lg" />
        </div>
      ) : dashboard.error ? (
        <div className="flex flex-col items-center gap-3 rounded-[2rem] bg-surface py-12 text-center">
          <p className="text-muted">{dashboard.error}</p>
          <Button variant="outline" onPress={dashboard.reload}>Reintentar</Button>
        </div>
      ) : (
        <>
          <DaySummary
            salesTotal={dashboard.salesTotal}
            salesCount={dashboard.salesCount}
            restockCount={dashboard.restockCount}
            expiringCount={dashboard.expiringCount}
          />
          <AttentionList items={dashboard.attention} />
          <TodayActivity sales={dashboard.todaySales} />
        </>
      )}
    </div>
  )
}
