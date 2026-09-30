/**
 * Metadata attached to each route (`handle`) and read by the app header with `useMatches`.
 */
export interface RouteHandle {
  title: string
  subtitle?: string
  /** Parent screen for sub-pages: shows a "back" button in the header. */
  parent?: { label: string; path: string }
}

export function isRouteHandle(value: unknown): value is RouteHandle {
  return typeof value === 'object' && value !== null && 'title' in value
}
