import { useState } from 'react'
import { Drawer } from '@heroui/react'
import { Outlet } from 'react-router'
import { AppHeader } from './AppHeader'
import { SidebarContent } from './SidebarContent'

const COLLAPSED_KEY = 'ebenezer.sidebar-collapsed'

function readCollapsed(): boolean {
  try {
    return localStorage.getItem(COLLAPSED_KEY) === 'true'
  } catch {
    return false
  }
}

export function AppLayout() {
  const [collapsed, setCollapsed] = useState(readCollapsed)
  const [menuOpen, setMenuOpen] = useState(false)

  const toggleCollapsed = () => {
    setCollapsed((value) => {
      try {
        localStorage.setItem(COLLAPSED_KEY, String(!value))
      } catch {
        // Preference only; ignore storage errors.
      }
      return !value
    })
  }

  return (
    <div className="flex min-h-screen bg-default">
      {/* Desktop sidebar */}
      <aside
        className={`sticky top-0 hidden h-screen shrink-0 overflow-x-hidden overflow-y-auto transition-[width] duration-200 ease-out lg:block ${
          collapsed ? 'w-26' : 'w-75'
        }`}
      >
        <SidebarContent collapsed={collapsed} onToggleCollapsed={toggleCollapsed} />
      </aside>

      {/* Mobile sidebar */}
      <Drawer isOpen={menuOpen} onOpenChange={setMenuOpen}>
        <Drawer.Backdrop isOpen={menuOpen} onOpenChange={setMenuOpen}>
          <Drawer.Content placement="left" className="w-75 max-w-[85vw] bg-default p-0">
            <Drawer.Dialog aria-label="Menú" className="h-full overflow-y-auto bg-default p-0">
              <SidebarContent onNavigate={() => setMenuOpen(false)} />
            </Drawer.Dialog>
          </Drawer.Content>
        </Drawer.Backdrop>
      </Drawer>

      <div className="flex min-w-0 flex-1 flex-col">
        <AppHeader onOpenMenu={() => setMenuOpen(true)} />
        <main className="flex-1 px-4 pt-6 pb-24 sm:px-7">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
