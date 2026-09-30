import { Radio, RadioGroup } from '@heroui/react'
import { useTextSize } from '@/shared/hooks/useTextSize'
import { TEXT_SIZE_OPTIONS, type TextSize } from '@/shared/utils/textSize'

export function TextSizeSelector() {
  const { textSize, setTextSize } = useTextSize()

  return (
    <section className="flex flex-col gap-4 rounded-4xl bg-surface p-5 sm:p-6">
      <div className="flex flex-col gap-1">
        <h2 className="text-lg font-bold">Tamaño de letra</h2>
        <p className="text-muted">Se guarda en esta caja. Elige el que te resulte más cómodo de leer.</p>
      </div>

      <RadioGroup
        aria-label="Tamaño de letra"
        value={textSize}
        onChange={(value) => setTextSize(value as TextSize)}
        className="grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-4"
      >
        {TEXT_SIZE_OPTIONS.map((option) => (
          <Radio key={option.id} value={option.id} className="w-full">
            {/* Radio.Content is the pressable label that holds the (hidden) radio input. */}
            <Radio.Content className="group flex h-16 w-full cursor-pointer flex-row items-center gap-3 rounded-full bg-background py-2 pr-4 pl-2 outline-none transition-[background-color,color,transform] duration-150 ease-out active:scale-[0.97] data-[focus-visible=true]:ring-2 data-[focus-visible=true]:ring-focus data-[selected=true]:bg-foreground data-[selected=true]:text-background">
              <span
                aria-hidden="true"
                className={`grid size-12 shrink-0 place-items-center rounded-full bg-surface font-bold text-foreground transition-colors duration-150 group-data-[selected=true]:bg-accent group-data-[selected=true]:text-accent-foreground ${option.previewClassName}`}
              >
                Aa
              </span>
              <span className="font-semibold">{option.label}</span>
            </Radio.Content>
          </Radio>
        ))}
      </RadioGroup>
    </section>
  )
}
