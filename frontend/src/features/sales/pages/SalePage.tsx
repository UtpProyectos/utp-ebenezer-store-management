import { useState } from 'react'
import { Button, Spinner } from '@heroui/react'
import type { ProductStockResponse } from '@/features/inventory'
import { formatMoney } from '@/shared/utils/money'
import { CartPanel } from '../components/CartPanel'
import { CheckoutDoneModal } from '../components/CheckoutDoneModal'
import { PaymentModal } from '../components/PaymentModal'
import { ProductGrid } from '../components/ProductGrid'
import { WeightModal } from '../components/WeightModal'
import { useCart, type CheckoutMode } from '../hooks/useCart'
import { useCheckout } from '../hooks/useCheckout'
import { useSaleCatalog } from '../hooks/useSaleCatalog'
import { formatSellable, isWeighed } from '../utils/salePricing'

export function SalePage() {
  const catalog = useSaleCatalog()
  const cart = useCart(catalog.products, catalog.promotionByProduct)
  const checkout = useCheckout(catalog.reload)
  const [paying, setPaying] = useState(false)
  const [weighing, setWeighing] = useState<ProductStockResponse | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  const consumption = cart.mode === 'consumption'

  const add = (item: ProductStockResponse) => {
    setNotice(null)
    if (isWeighed(item)) {
      setWeighing(item)
      return
    }
    if (!cart.addOne(item)) setNotice(`Solo quedan ${formatSellable(item)} de ${item.productName}.`)
  }

  const changeMode = (mode: CheckoutMode) => {
    cart.setMode(mode)
    checkout.clearError()
  }

  const charge = () => {
    if (cart.lines.length === 0) return
    setNotice(null)
    checkout.clearError()
    if (consumption) {
      void checkout.registerConsumption(cart.lines, cart.itemCount)
    } else {
      setPaying(true)
    }
  }

  const startOver = () => {
    checkout.reset()
    cart.clear()
    cart.setMode('sale')
  }

  if (catalog.initialLoading) {
    return (
      <div className="grid place-items-center py-24">
        <Spinner size="lg" />
      </div>
    )
  }

  if (catalog.error && catalog.products.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-24 text-center">
        <p className="text-muted">{catalog.error}</p>
        <Button variant="outline" onPress={catalog.reload}>Reintentar</Button>
      </div>
    )
  }

  return (
    <div className="mx-auto grid max-w-[102.5rem] items-start gap-5 lg:grid-cols-[minmax(0,1fr)_26.25rem]">
      <ProductGrid
        products={catalog.products}
        categories={catalog.categories}
        promotionByProduct={catalog.promotionByProduct}
        quantityOf={cart.quantityOf}
        onAdd={add}
      />

      <CartPanel
        mode={cart.mode}
        onModeChange={changeMode}
        lines={cart.lines}
        itemCount={cart.itemCount}
        subtotal={cart.subtotal}
        notice={notice}
        error={paying ? null : checkout.error}
        isPending={checkout.isPending}
        onCharge={charge}
        onCancel={() => {
          cart.clear()
          setNotice(null)
          checkout.clearError()
        }}
        onSetQuantity={(item, quantity) => {
          setNotice(null)
          cart.setQuantity(item, quantity)
        }}
        onIncrement={add}
        onEditWeight={setWeighing}
        onRemove={cart.remove}
      />

      {/* Narrow screens: the cart is below the products, so the total stays reachable. */}
      {cart.lines.length > 0 && !paying && !checkout.result && (
        <div className="fixed inset-x-3 bottom-3 z-30 flex items-center gap-3 rounded-[1.625rem] bg-accent py-2.5 pr-2.5 pl-4 text-accent-foreground shadow-[var(--overlay-shadow)] lg:hidden">
          <div className="flex flex-1 flex-col">
            <span className="text-sm font-medium text-accent-foreground/80">
              {cart.itemCount} producto{cart.itemCount === 1 ? '' : 's'}
            </span>
            <span className="text-xl font-extrabold tabular-nums">{formatMoney(consumption ? 0 : cart.subtotal)}</span>
          </div>
          <button
            type="button"
            onClick={charge}
            disabled={checkout.isPending}
            className="h-12.5 rounded-full bg-foreground px-4.5 text-[1.0625rem] font-bold text-background outline-none focus-visible:ring-2 focus-visible:ring-background active:scale-[0.97]"
          >
            {consumption ? 'Registrar consumo' : `Cobrar ${formatMoney(cart.subtotal)}`}
          </button>
        </div>
      )}

      {weighing && (
        <WeightModal
          item={weighing}
          currentKilograms={cart.quantityOf(weighing.productId)}
          onClose={() => setWeighing(null)}
          onSubmit={(kilograms) => {
            cart.setQuantity(weighing, kilograms)
            setWeighing(null)
          }}
        />
      )}

      {paying && (
        <PaymentModal
          total={cart.subtotal}
          isPending={checkout.isPending}
          error={checkout.error}
          onClose={() => {
            setPaying(false)
            checkout.clearError()
          }}
          onConfirm={async (paymentMethod, received) => {
            if (await checkout.chargeSale(cart.lines, paymentMethod, received)) setPaying(false)
          }}
        />
      )}

      {checkout.result && <CheckoutDoneModal result={checkout.result} onNewSale={startOver} />}
    </div>
  )
}
