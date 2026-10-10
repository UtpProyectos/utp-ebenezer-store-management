import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router'
import { Button, Spinner } from '@heroui/react'
import { ROUTES } from '@/app/router/routes'
import { useAuth } from '@/features/auth'
import { useInventory } from '@/features/inventory'
import { CreateProductModal } from '@/features/products'
import { MoreDetailsSection } from '../components/MoreDetailsSection'
import { ProductPickerStep } from '../components/ProductPickerStep'
import { PurchaseSavedState } from '../components/PurchaseSavedState'
import { PurchaseSummaryCard } from '../components/PurchaseSummaryCard'
import { QuantityStep } from '../components/QuantityStep'
import { SalePriceStep } from '../components/SalePriceStep'
import { usePurchaseEntry } from '../hooks/usePurchaseEntry'
import { useSupplierOptions } from '../hooks/useSupplierOptions'

export function PurchaseEntryPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [newProductName, setNewProductName] = useState<string | null>(null)
  const [searchParams] = useSearchParams()
  const initialProductId = Number(searchParams.get('productId')) || null

  const { items, initialLoading, error, reload } = useInventory()
  const { suppliers, createSupplier } = useSupplierOptions()
  const entry = usePurchaseEntry(items, suppliers, initialProductId)

  const backToInventory = () => navigate(ROUTES.inventory)

  if (initialLoading) {
    return (
      <div className="grid place-items-center rounded-3xl bg-surface py-16">
        <Spinner aria-label="Cargando productos" />
      </div>
    )
  }

  if (error) {
    return (
      <div role="alert" className="flex flex-col items-center gap-3 rounded-3xl bg-surface px-5 py-14 text-center">
        <p className="font-semibold">No se pudieron cargar los productos</p>
        <p className="text-sm text-muted">{error}</p>
        <Button variant="outline" onPress={reload}>Reintentar</Button>
      </div>
    )
  }

  if (entry.saved) {
    return (
      <div className="mx-auto w-full max-w-410">
        <PurchaseSavedState
          saved={entry.saved}
          onBackToInventory={backToInventory}
          onRegisterAnother={() => {
            entry.reset()
            reload()
          }}
        />
      </div>
    )
  }

  return (
    <div className="mx-auto grid w-full max-w-410 items-start gap-5 lg:grid-cols-[minmax(0,1.7fr)_minmax(20rem,1fr)]">
      <div className="flex flex-col gap-3.5">
        <ProductPickerStep
          items={items}
          product={entry.product}
          invalid={entry.errors.product}
          onSelect={entry.selectProduct}
          onClear={entry.clearProduct}
          onCreateProduct={user?.role === 'ADMIN' ? setNewProductName : undefined}
        />
        <QuantityStep entry={entry} />
        <SalePriceStep entry={entry} />
        <MoreDetailsSection
          entry={entry}
          suppliers={suppliers}
          onCreateSupplier={
            user?.role === 'ADMIN'
              ? (name) => createSupplier(name, entry.product?.productId ?? null)
              : undefined
          }
        />
      </div>

      <PurchaseSummaryCard entry={entry} onSave={() => void entry.submit()} onBack={backToInventory} />

      {newProductName !== null && (
        <CreateProductModal
          initialName={newProductName}
          onClose={() => setNewProductName(null)}
          onCreated={(product) => {
            setNewProductName(null)
            reload()
            entry.selectProduct(product.id)
          }}
        />
      )}
    </div>
  )
}
