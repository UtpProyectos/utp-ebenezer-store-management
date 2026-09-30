import { useState } from 'react'
import { readTextSize, saveTextSize, type TextSize } from '@/shared/utils/textSize'

export function useTextSize() {
  const [textSize, setTextSizeState] = useState<TextSize>(readTextSize)

  const setTextSize = (size: TextSize) => {
    saveTextSize(size)
    setTextSizeState(size)
  }

  return { textSize, setTextSize }
}
