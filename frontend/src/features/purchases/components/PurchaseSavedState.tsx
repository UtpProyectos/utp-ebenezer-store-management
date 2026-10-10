import { Button } from '@heroui/react'
import CircleCheckFill from '@gravity-ui/icons/CircleCheckFill'
import type { SavedEntry } from '../hooks/usePurchaseEntry'

interface PurchaseSavedStateProps {
  saved: SavedEntry
  onBackToInventory: () => void
  onRegisterAnother: () => void
}

export function PurchaseSavedState({ saved, onBackToInventory, onRegisterAnother }: PurchaseSavedStateProps) {
  return (
    <section className="flex flex-col items-center gap-3.5 rounded-3xl bg-surface px-6 py-14 text-center">
      <span className="grid size-22 place-items-center rounded-full bg-success-soft text-success-soft-foreground">
        <CircleCheckFill aria-hidden="true" className="size-12" />
      </span>
      <h2 className="text-3xl font-bold">Ingreso guardado</h2>
      <p className="max-w-lg text-lg text-pretty text-foreground/80">
        {saved.productName}: llegaron {saved.quantityLabel}. Ahora tienes {saved.stockLabel} en la tienda.
      </p>
      <div className="mt-2 flex flex-wrap justify-center gap-3">
        <Button size="lg" variant="secondary" onPress={onBackToInventory}>Volver a inventario</Button>
        <Button size="lg" variant="primary" onPress={onRegisterAnother}>Registrar otro producto</Button>
      </div>
    </section>
  )
}
