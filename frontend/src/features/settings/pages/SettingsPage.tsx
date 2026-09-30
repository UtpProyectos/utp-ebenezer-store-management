import { TextSizeSelector } from '../components/TextSizeSelector'

// TODO(team): add the business settings form (BusinessSettings API) below the text size section.
export function SettingsPage() {
  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-4">
      <TextSizeSelector />
    </div>
  )
}
