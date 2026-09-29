# Frontend rules

Reglas específicas para `frontend/`. Complementan [`../AGENTS.md`](../AGENTS.md), que siempre tiene prioridad en reglas globales.

---

## 1. Stack y comandos

- React 19 · TypeScript · Vite 8
- HeroUI v3 (`@heroui/react` + `@heroui/styles`) — librería principal de componentes
- Tailwind CSS v4 (`@tailwindcss/vite`) — estilos utilitarios
- React Router (`react-router`) — routing
- oxlint — linter

Comandos (desde `frontend/`):

```bash
npm install          # instalar dependencias
npm run dev          # servidor de desarrollo en http://localhost:5173
npm run build        # type-check (tsc) + build de producción
npm run lint         # oxlint
npm run preview      # previsualizar el build
```

No cambiar Vite por otro bundler. No agregar otra UI library (MUI, Ant Design, Bootstrap, Chakra, ...) ni otra librería de estilos (styled-components, ...). No agregar Redux/Zustand sin necesidad real y aprobación.

---

## 2. Estructura (feature-first)

```text
src/
├── app/
│   ├── router/          # AppRouter.tsx, routes.ts, (ProtectedRoute.tsx)
│   ├── providers/       # Solo cuando exista un provider real (ej. AuthProvider)
│   └── store/           # Solo estado realmente global
├── features/
│   ├── auth/  dashboard/  users/  products/  categories/  suppliers/
│   ├── purchases/  inventory/  sales/  consumption/  ai-assistant/
├── shared/
│   ├── components/
│   │   ├── ui/          # Componentes genéricos reutilizables
│   │   └── layout/      # AppLayout, Sidebar, Header...
│   ├── hooks/  services/  types/  constants/  utils/  lib/
├── assets/              # images, icons, logos
├── styles/              # globals.css
├── App.tsx
└── main.tsx
```

Estado actual: `app/router/`, `features/dashboard/pages/`, `shared/components/layout/`, `shared/services/`, `shared/types/`, `styles/`.

**No crear carpetas vacías.** Crear la estructura progresivamente según se implemente cada módulo.

---

## 3. Organización por feature

Cada feature mantiene juntos sus componentes, páginas, services, hooks y types:

```text
features/products/
├── components/
│   ├── ProductForm.tsx
│   └── ProductTable.tsx
├── pages/
│   └── ProductListPage.tsx
├── services/
│   └── productApi.ts
├── hooks/
│   └── useProducts.ts
├── types/
│   └── product.types.ts
└── index.ts
```

Mismo patrón para `inventory/` (`InventoryPage`, `InventoryTable`, `StockAlert`, `inventoryApi`, `useInventory`), `auth/` (`LoginPage`, `LoginForm`, `authApi`, `useAuth`), etc.

AI Assistant:

```text
features/ai-assistant/
├── components/   AssistantPanel.tsx, ChatMessage.tsx, ChatInput.tsx
├── services/     aiAssistantApi.ts
├── hooks/        useAiAssistant.ts
├── types/        ai-assistant.types.ts
└── index.ts
```

El frontend **nunca** llama a OpenAI/OpenRouter ni contiene sus API keys: solo habla con el backend.

---

## 4. Qué va en `shared/`

Solo código reutilizado por **varias** features o con responsabilidad claramente global. No mover algo a `shared` "porque quizá se reutilice".

- `shared/components/ui/` — wrappers basados en HeroUI **solo** cuando hay una razón real (ej. `ConfirmModal`, `DataTable`, `EmptyState`, `PageHeader`, `LoadingState`).
- `shared/components/layout/` — `AppLayout` (existe), `Sidebar`, `Header`, `ContentContainer`.
- `shared/services/apiClient.ts` — cliente HTTP central (existe).
- `shared/types/api.types.ts` — types globales de la API (existe).
- `shared/hooks/` — hooks genéricos (ej. `useDebounce`).

`ProductForm` pertenece a `features/products/components/`, **no** a `shared/`.

---

## 5. HeroUI v3 + Tailwind CSS v4

### Setup actual

- `vite.config.ts` usa el plugin `@tailwindcss/vite`.
- `src/styles/globals.css`:

  ```css
  @import "tailwindcss";
  @import "@heroui/styles";
  ```

- HeroUI v3 **no requiere provider** (`HeroUIProvider` ya no existe). No crear uno.
- No hay `tailwind.config.js`: Tailwind v4 se configura desde CSS (`@theme`) si hace falta.

### Uso de HeroUI

Antes de crear un componente visual, revisar si HeroUI lo ofrece: Button, Input, TextField, Select, Modal, Drawer, Card, Table, Tabs, Dropdown, Tooltip, Chip, Pagination, Spinner, DatePicker...

HeroUI v3 usa **compound components** y props de React Aria:

```tsx
import { Button, Card } from '@heroui/react'

<Card>
  <Card.Header>
    <Card.Title>Title</Card.Title>
    <Card.Description>Description</Card.Description>
  </Card.Header>
  <Card.Content>...</Card.Content>
  <Card.Footer>
    <Button variant="primary" onPress={handleSave}>Save</Button>
  </Card.Footer>
</Card>
```

- Eventos: preferir `onPress` (React Aria) sobre `onClick` en componentes HeroUI.
- Variantes de Button: `primary`, `secondary`, `tertiary`, `outline`, `ghost`, `danger`, `danger-soft`.
- Documentación: https://heroui.com (v3) · https://heroui.com/llms.txt

**No** crear wrappers como `CustomButton`, `CustomInput`, `CustomCard` que solo replican HeroUI: usar HeroUI directamente en la feature.

### Personalización

Mediante props de HeroUI, `className`, Tailwind y variables de tema oficiales (`@heroui/styles`). No modificar la librería internamente ni hacer personalizaciones profundas.

Usar los tokens de tema de HeroUI como utilidades Tailwind en lugar de colores fijos: `background`, `foreground`, `surface`, `muted`, `accent`, `default`, `danger`, `border`, `separator`, `field`... (ej. `bg-background`, `text-muted`, `bg-surface`, `border-separator`, `text-danger`). Así se respeta el modo claro/oscuro.

### Reglas Tailwind

Usar Tailwind para layout, spacing, responsive, grid/flex, sizing, alignment y ajustes visuales alrededor de HeroUI.

```tsx
<div className="grid gap-4 md:grid-cols-2">
```

Evitar: estilos inline extensos, clases gigantes o duplicadas, CSS por componente sin necesidad, valores arbitrarios cuando existe una utility estándar, reconstruir con Tailwind algo que HeroUI ya resuelve. Si una combinación se repite mucho, extraer un componente.

---

## 6. Pages

Viven en su feature: `features/products/pages/ProductListPage.tsx`. No usar una carpeta global `pages/`.

Una page compone components, usa hooks de la feature y coordina acciones de UI. No contiene clientes HTTP crudos ni lógica compleja de acceso a la API.

---

## 7. API services

Cliente central: `shared/services/apiClient.ts`

- Base URL desde `VITE_API_BASE_URL`.
- Header `Content-Type: application/json` y `Authorization: Bearer <token>` cuando se pasa `token`.
- Lanza `ApiClientError` (`status`, `payload: ApiError`) cuando la respuesta no es OK.
- Métodos: `apiClient.get/post/put/patch/delete<T>(path, ...)`.

Cada feature encapsula sus endpoints:

```ts
// features/products/services/productApi.ts
import { apiClient } from '@/shared/services/apiClient'
import type { ProductResponse } from '../types/product.types'

export const productApi = {
  getAll: () => apiClient.get<ProductResponse[]>('/products'),
}
```

Evitar un `services/api.ts` gigante con todos los endpoints, y evitar `fetch` directo en componentes.

Cuando se implemente auth, la inyección del token puede centralizarse en `apiClient` (sin duplicarla en cada feature).

---

## 8. Types

- Específicos de la feature: `features/products/types/product.types.ts`.
- Globales: `shared/types/api.types.ts` (`ApiError` refleja el `ApiError` del backend).
- Modelar los DTOs del backend; no inventar campos.
- No usar una carpeta global `models/`.
- `.ts` para archivos sin JSX; `.tsx` solo para archivos que renderizan JSX.
- Importar types con `import type` (el proyecto usa `verbatimModuleSyntax`).

---

## 9. Hooks

- De feature: `features/products/hooks/useProducts.ts`.
- Genéricos: `shared/hooks/`.
- Extraer lógica compleja de los componentes visuales hacia hooks o services.

---

## 10. Estado global

Solo para datos realmente compartidos: usuario autenticado, token/sesión, UI global. El estado de una feature permanece en la feature. Usar primero React (context + hooks); no agregar librerías de estado sin necesidad.

---

## 11. Router (`app/router/`)

- `routes.ts` — constantes de rutas (`ROUTES`).
- `AppRouter.tsx` — `createBrowserRouter` + `RouterProvider` (de `react-router/dom`). Las páginas se montan dentro de `AppLayout` (`<Outlet />`).
- `ProtectedRoute.tsx` — (futuro) usa el estado de auth y los roles entregados por el backend.

No duplicar reglas de seguridad críticas solo en el frontend.

---

## 12. Providers (`app/providers/`)

Solo cuando exista una necesidad real (ej. `AuthProvider`). HeroUI v3 no necesita provider.

---

## 13. Variables de entorno

- `frontend/.env.example` → copiar a `frontend/.env.local`.
- `VITE_API_BASE_URL=http://localhost:8080/api`
- Tipadas en `src/vite-env.d.ts`. Al agregar una variable, tiparla allí y agregarla a `.env.example`.
- Nunca secretos en `VITE_*`.

---

## 14. Convenciones

| Tipo | Convención | Ejemplo |
|---|---|---|
| Componentes / pages | PascalCase `.tsx` | `ProductForm.tsx`, `LoginPage.tsx` |
| Hooks | camelCase con `use` | `useProducts.ts` |
| Services / API | camelCase | `productApi.ts` |
| Types | `<feature>.types.ts` | `product.types.ts` |
| Constantes | UPPER_SNAKE_CASE | `ROUTES` |

Exports nombrados para componentes (`export function ProductForm`). Evitar nombres ambiguos (`Helper.ts`, `Utils2.ts`, `Component1.tsx`).

---

## 15. Imports

Alias `@/` → `src/` (configurado en `vite.config.ts` y `tsconfig.app.json`).

```ts
import { apiClient } from '@/shared/services/apiClient'
```

Evitar rutas relativas largas (`../../../../shared/...`). Imports relativos cortos dentro de la misma feature están bien (`../types/product.types`).

---

## 16. Regla de dependencia

```text
Page → Feature Component / Hook → Feature Service → apiClient → Spring Boot API
```

Ejemplo: `ProductListPage → useProducts → productApi → apiClient → /api/products`.

Un componente visual no debe conocer detalles de infraestructura.

---

## 17. Reglas finales

1. Inspeccionar la estructura real antes de crear o mover archivos.
2. Decidir si algo pertenece a una feature o a `shared`.
3. No duplicar componentes, services, types ni helpers.
4. Todo el código en inglés.
5. No agregar librerías sin justificar.
6. Respetar los contratos del backend.
7. Mantener secretos fuera del frontend.
8. Evitar componentes, services y stores gigantes.
9. `npm run build` y `npm run lint` deben pasar.
