# Eben-Ezer Store Management

Sistema web para la gestión comercial e inventario de la **Bodega Eben-Ezer** (proyecto universitario UTP).

> **Reglas del proyecto:** [`AGENTS.md`](AGENTS.md) es la fuente principal de reglas de arquitectura y convenciones, tanto para personas como para agentes de código (Claude Code, Codex, etc.). Las reglas detalladas están en [`AGENTS/backend.md`](AGENTS/backend.md) y [`AGENTS/frontend.md`](AGENTS/frontend.md).

---

## Stack

### Backend

- Java 25
- Spring Boot 4.1.1 (Maven Wrapper)
- PostgreSQL
- Spring Data JPA / Hibernate
- Spring Security + JWT (JJWT 0.12.x)
- Spring Validation
- Lombok

### Frontend

- React 19 + TypeScript
- Vite 8
- HeroUI v3 (componentes)
- Tailwind CSS v4 (estilos)
- React Router

---

## Estructura del monorepo

```text
utp-ebenezer-store-management/
├── backend/          # API REST Spring Boot
├── frontend/         # SPA React + Vite
├── AGENTS/           # Reglas específicas por área
│   ├── backend.md
│   └── frontend.md
├── AGENTS.md         # Reglas globales del proyecto
└── README.md
```

---

## Puesta en marcha

### Requisitos

- JDK 25
- Node.js 24+ y npm
- Acceso a una base de datos PostgreSQL

### Backend

```bash
cd backend
cp .env.example .env      # completar DB_URL, DB_USERNAME, DB_PASSWORD
./mvnw spring-boot:run    # Windows: .\mvnw.cmd spring-boot:run
```

- API: `http://localhost:8080/api`
- Verificación: `GET http://localhost:8080/api/health` → `{"status":"UP"}`

El archivo `backend/.env` se carga automáticamente y **nunca** se commitea.

Para cargar el catálogo, los proveedores, sus productos principales y las cuatro cuentas de la familia en una base de prueba, agrega `DEMO_CATALOG_ENABLED=true` a `backend/.env` y reinicia el backend. La carga agrega solo productos y usuarios faltantes; no reemplaza registros existentes. Las cuentas demo nuevas usan como contraseña inicial el mismo formato `nombre.apellido` del usuario.

### Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

- App: `http://localhost:5173`

### Comandos útiles

| Área | Comando | Descripción |
|---|---|---|
| Backend | `./mvnw compile` | Compilar |
| Backend | `./mvnw test` | Pruebas (requiere BD) |
| Backend | `./mvnw clean package` | Generar jar |
| Frontend | `npm run build` | Type-check + build |
| Frontend | `npm run lint` | Linter (oxlint) |
| Frontend | `npm run preview` | Previsualizar build |

---

## Variables de entorno

### Backend (`backend/.env`)

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de PostgreSQL |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de la BD |
| `SERVER_PORT` | Puerto de la API (default `8080`) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (default `http://localhost:5173`) |
| `JWT_SECRET` / `JWT_EXPIRATION_MS` | Configuración JWT (pendiente de uso) |
| `OPENAI_API_KEY` / `OPENROUTER_API_KEY` | Proveedores de IA (pendiente de uso) |

### Frontend (`frontend/.env.local`)

| Variable | Descripción |
|---|---|
| `VITE_API_BASE_URL` | URL base de la API (`http://localhost:8080/api`) |

> Las variables `VITE_*` se exponen en el navegador. Nunca colocar secretos en el frontend.

---

## Backend

Arquitectura por capas con agrupación progresiva por feature:

```text
Controller  →  Service  →  Repository  →  PostgreSQL
```

Los controllers **nunca** acceden directamente a repositories, y **nunca** exponen entidades JPA (siempre DTOs).

Paquete raíz: `pe.edu.utp.ebenezer`

```text
src/main/java/pe/edu/utp/ebenezer/
├── api/
│   ├── controller/     # REST controllers por feature (/api/...)
│   └── dto/            # Request/Response DTOs por feature
├── config/             # SecurityConfig, CorsConfig
├── domain/
│   ├── entity/         # Entidades JPA
│   ├── repository/     # Spring Data JPA
│   └── enums/          # Enums del dominio
├── service/            # Lógica de negocio por feature (Service + ServiceImpl)
├── security/           # JWT y autenticación
├── exception/          # GlobalExceptionHandler y ApiError
├── ai/
│   ├── provider/       # AiProvider (OpenAI / OpenRouter)
│   ├── model/
│   └── prompt/
└── EbenezerBackendApplication.java
```

Configuración inicial incluida:

- **Seguridad:** API stateless, CORS para el frontend, `/api/health` público y el resto protegido. BCrypt como `PasswordEncoder`. JWT pendiente de implementar.
- **Errores:** formato uniforme `ApiError` (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`) sin stack traces.
- **Paquetes:** todos los paquetes previstos existen con `package-info.java` (sin clases ficticias).

Detalle completo: [`AGENTS/backend.md`](AGENTS/backend.md).

---

## Frontend

Arquitectura **feature-first**: cada funcionalidad mantiene juntos sus componentes, páginas, services, hooks y types. `shared/` solo contiene código realmente reutilizado por varias features.

```text
src/
├── app/
│   ├── router/         # AppRouter, routes
│   ├── providers/      # Solo si existe una necesidad real
│   └── store/          # Solo estado realmente global
├── features/
│   ├── auth/  dashboard/  users/  products/  categories/  suppliers/
│   └── purchases/  inventory/  sales/  consumption/  ai-assistant/
├── shared/
│   ├── components/
│   │   ├── ui/
│   │   └── layout/     # AppLayout
│   ├── hooks/  services/ (apiClient)  types/  constants/  utils/  lib/
├── assets/
├── styles/             # globals.css (Tailwind + HeroUI)
├── App.tsx
└── main.tsx
```

Las carpetas se crean progresivamente según se implementa cada módulo.

Ejemplo de una feature:

```text
features/products/
├── components/   ProductForm.tsx, ProductTable.tsx
├── pages/        ProductListPage.tsx
├── services/     productApi.ts
├── hooks/        useProducts.ts
├── types/        product.types.ts
└── index.ts
```

Flujo de dependencias:

```text
Page → Hook / Component → Feature service → apiClient → Spring Boot API
```

### UI y estilos

```text
HeroUI        → componentes base (Button, Input, Select, Modal, Card, Table, ...)
Tailwind CSS  → layout, spacing, responsive y ajustes visuales
```

- Antes de crear un componente visual, verificar si HeroUI ya lo ofrece.
- No crear wrappers que solo replican HeroUI (`CustomButton`, `CustomInput`).
- HeroUI v3 no necesita provider.
- Alias de imports: `@/` → `src/`.

Detalle completo: [`AGENTS/frontend.md`](AGENTS/frontend.md).

---

## Módulos principales

- Authentication
- Users and roles
- Products and categories
- Suppliers
- Purchases
- Inventory and lots
- Expiration alerts
- Sales
- Internal consumption
- Dashboard
- AI Assistant

---

## AI Assistant

El frontend **nunca** se conecta directamente con OpenAI u OpenRouter.

```text
React  →  Spring Boot  →  AiProvider
                            ├── OpenAI      (principal)
                            └── OpenRouter  (fallback)
```

Las API keys permanecen exclusivamente en el backend.

---

## Convenciones

- **Todo el código en inglés** (paquetes, clases, componentes, variables, tablas, endpoints). La documentación puede estar en español.
- Backend: `ProductService` / `ProductServiceImpl` (sin prefijo `I`), DTOs `ProductRequest` / `ProductResponse`, constructor injection.
- Frontend: componentes en PascalCase (`ProductForm.tsx`), hooks `useXxx.ts`, services `xxxApi.ts`, types `xxx.types.ts`.
- Nunca commitear secretos (`.env`, `.env.local`).

```text
✔ Product, ProductService, InventoryMovement, PurchaseDetail, InternalConsumption
✘ Producto, ServicioProducto, MovimientoInventario, DetalleCompra, ConsumoInterno
```

---

## Checklist para una nueva feature

1. Leer [`AGENTS.md`](AGENTS.md) y el archivo del área.
2. **Backend:** DTOs → entity (si el modelo lo requiere) → repository → service → controller → errores → pruebas.
3. **Frontend:** types → `xxxApi.ts` → hook → components → page → ruta.
4. No modificar módulos no relacionados.
5. Verificar: `./mvnw test` y `npm run build && npm run lint`.
