import { BrandMark } from '@/shared/components/ui/BrandMark'

// Left side of the login screen (large screens only): brand and a short message.
export function LoginBrandPanel() {
  return (
    <aside className="hidden flex-col justify-between bg-foreground p-12 text-background lg:flex">
      <div className="flex items-center gap-3">
        <BrandMark />
        <div className="flex flex-col">
          <span className="text-lg font-bold leading-tight">Eben-Ezer</span>
          <span className="text-sm opacity-70">Bodega · Minimarket</span>
        </div>
      </div>

      <p className="max-w-md text-balance text-4xl font-bold leading-tight tracking-tight">
        Buen control de inventario y ventas para tu negocio
      </p>

      <span className="text-sm opacity-60">Eben-Ezer Store Management · v1.0</span>
    </aside>
  )
}
