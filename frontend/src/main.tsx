import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@/styles/globals.css'
import App from '@/App'
import { applyTextSize, readTextSize } from '@/shared/utils/textSize'

// Apply the saved text size before the first render to avoid a visible resize.
applyTextSize(readTextSize())

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
