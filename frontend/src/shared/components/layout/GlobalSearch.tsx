import { useEffect, useRef, useState, type KeyboardEvent } from 'react'
import Handset from '@gravity-ui/icons/Handset'
import Magnifier from '@gravity-ui/icons/Magnifier'
import ShoppingBag from '@gravity-ui/icons/ShoppingBag'
import { useNavigate } from 'react-router'
import { ROUTES } from '@/app/router/routes'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { useGlobalSearch, type GlobalSearchResult } from '@/shared/hooks/useGlobalSearch'

// Header search of the prototype: products go to Inventory, suppliers to Suppliers (admin only).
export function GlobalSearch() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { load, search } = useGlobalSearch(user?.role === 'ADMIN')
  const inputRef = useRef<HTMLInputElement>(null)
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(0)

  const results = search(query)

  // Ctrl + K (⌘ + K on Mac) focuses the search from anywhere.
  useEffect(() => {
    const handleShortcut = (event: globalThis.KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        inputRef.current?.focus()
      }
    }
    window.addEventListener('keydown', handleShortcut)
    return () => window.removeEventListener('keydown', handleShortcut)
  }, [])

  const go = (result: GlobalSearchResult) => {
    const target = result.kind === 'product' ? ROUTES.inventory : ROUTES.suppliers
    navigate(`${target}?q=${encodeURIComponent(result.name)}`)
    setQuery('')
    setOpen(false)
    inputRef.current?.blur()
  }

  const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'ArrowDown' && results.length > 0) {
      event.preventDefault()
      setActive((index) => (index + 1) % results.length)
    } else if (event.key === 'ArrowUp' && results.length > 0) {
      event.preventDefault()
      setActive((index) => (index - 1 + results.length) % results.length)
    } else if (event.key === 'Enter' && results.length > 0) {
      event.preventDefault()
      go(results[Math.min(active, results.length - 1)])
    } else if (event.key === 'Escape') {
      setOpen(false)
      inputRef.current?.blur()
    }
  }

  const showResults = open && results.length > 0

  return (
    <div className="relative hidden w-75 shrink-0 xl:block">
      <Magnifier aria-hidden="true" className="pointer-events-none absolute top-1/2 left-3.5 size-5 -translate-y-1/2 text-muted" />
      <input
        ref={inputRef}
        type="search"
        role="combobox"
        aria-label="Buscar producto o proveedor"
        aria-expanded={showResults}
        aria-controls="global-search-results"
        aria-autocomplete="list"
        value={query}
        placeholder={user?.role === 'ADMIN' ? 'Buscar producto, proveedor…' : 'Buscar producto…'}
        onChange={(event) => {
          setQuery(event.target.value)
          setActive(0)
          setOpen(true)
        }}
        onFocus={() => {
          load()
          setOpen(true)
        }}
        // Delay so a click on a result lands before the list closes.
        onBlur={() => window.setTimeout(() => setOpen(false), 150)}
        onKeyDown={handleKeyDown}
        className="h-12 w-full rounded-full border border-surface bg-surface pr-16 pl-10 font-medium outline-none transition-[border-color,box-shadow] duration-150 placeholder:text-field-placeholder focus:border-accent focus:ring-3 focus:ring-accent/20 [&::-webkit-search-cancel-button]:hidden"
      />
      <kbd className="pointer-events-none absolute top-1/2 right-2.5 -translate-y-1/2 rounded-[0.3125rem] border border-border bg-surface-secondary px-1.5 py-0.5 font-sans text-xs font-semibold text-muted">
        Ctrl K
      </kbd>

      {showResults && (
        <ul
          id="global-search-results"
          role="listbox"
          aria-label="Resultados"
          className="absolute top-13 right-0 left-0 z-40 rounded-[1.125rem] bg-overlay p-1.5 shadow-[var(--overlay-shadow)]"
        >
          {results.map((result, index) => {
            const Icon = result.kind === 'product' ? ShoppingBag : Handset
            return (
              <li key={result.key} role="option" aria-selected={index === active}>
                <button
                  type="button"
                  tabIndex={-1}
                  onMouseDown={(event) => event.preventDefault()}
                  onClick={() => go(result)}
                  onMouseEnter={() => setActive(index)}
                  className={`flex w-full items-center gap-2.5 rounded-xl px-2.5 py-2 text-left font-medium ${
                    index === active ? 'bg-surface-tertiary' : ''
                  }`}
                >
                  <Icon aria-hidden="true" className="size-5 shrink-0 text-muted" />
                  <span className="min-w-0 flex-1 truncate">{result.name}</span>
                  <span className="shrink-0 text-sm text-muted">{result.meta}</span>
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
