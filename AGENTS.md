# AGENTS.md

## Propósito

Este archivo es la **fuente principal de reglas** de arquitectura, organización y convenciones para cualquier agente de código (Claude Code, Codex u otro) y para cualquier persona que trabaje en **Eben-Ezer Store Management**.

Es intencionalmente agnóstico al agente: no se usan archivos específicos como `CLAUDE.md`. Las reglas detalladas por área viven en la carpeta [`AGENTS/`](AGENTS/):

| Archivo | Cuándo leerlo |
|---|---|
| [`AGENTS/backend.md`](AGENTS/backend.md) | Cualquier cambio dentro de `backend/` |
| [`AGENTS/frontend.md`](AGENTS/frontend.md) | Cualquier cambio dentro de `frontend/` |
| [`AGENTS/database.md`](AGENTS/database.md) | Cualquier cambio de la base de datos |

Antes de modificar un área, **leer este archivo y el archivo específico del área**.

Si en el futuro se necesitan reglas específicas nuevas (  `AGENTS/testing.md`), agregarlas dentro de `AGENTS/` y enlazarlas en la tabla anterior.

---

# 1. Idioma

La documentación puede estar en español, pero **todo lo relacionado con código debe estar en inglés**:

- paquetes, carpetas y archivos
- clases, componentes, interfaces y types
- métodos, funciones, hooks y variables
- campos, enums y DTOs
- tablas y columnas
- endpoints y rutas
- comentarios técnicos dentro del código (preferentemente)

Correcto: `Product`, `ProductService`, `PurchaseDetail`, `InventoryMovement`, `InternalConsumption`, `PaymentMethod`, `SaleController`, `ProductForm`.

Incorrecto: `Producto`, `ServicioProducto`, `DetalleCompra`, `MovimientoInventario`, `ConsumoInterno`, `MetodoPago`, `ControladorVenta`.

---

# 2. Contexto del proyecto

Repositorio: `utp-ebenezer-store-management`

Proyecto universitario (UTP) para la gestión comercial e inventario de la bodega **Eben-Ezer**.

## Stack

| Área | Tecnologías |
|---|---|
| Backend | Java 25, Spring Boot 4.1.1, Maven (wrapper), PostgreSQL, Spring Data JPA / Hibernate, Spring Security, JWT (JJWT 0.12.x), Spring Validation, Lombok, DevTools |
| Frontend | React 19, TypeScript, Vite 8, HeroUI v3, Tailwind CSS v4, React Router |

## Módulos previstos

Authentication · Users · Roles · Categories · Products · Suppliers · Purchases · Inventory · Lots / expiration dates · Sales · Internal consumption · Dashboard · AI Assistant

El asistente de IA responderá consultas como:

- Which products are low in stock?
- Which products are about to expire?
- How much did I sell today?
- What products should I restock?

Proveedores de IA: OpenAI (principal) y OpenRouter (fallback). **Solo desde el backend.**

---

# 3. Estructura del monorepo

```text
utp-ebenezer-store-management/
├── backend/          # Spring Boot API (Maven)
├── frontend/         # React + Vite SPA
├── AGENTS/           # Reglas específicas por área
│   ├── backend.md
│   └── frontend.md
├── AGENTS.md         # Este archivo (reglas globales)
└── README.md         # Guía del proyecto para personas
```

`backend/` y `frontend/` viven en la raíz. **No mover** a `apps/` u otra ubicación sin que se solicite explícitamente. Una carpeta `docs/` puede agregarse cuando exista documentación funcional (modelo de datos, diagramas, etc.).

---

# 4. Reglas globales

## 4.1 Antes de modificar

1. Inspeccionar primero la estructura real existente.
2. Respetar paquetes, rutas y convenciones actuales.
3. No duplicar clases, componentes, services, types ni helpers.
4. No eliminar configuraciones que funcionan.
5. No mover archivos masivamente sin necesidad.
6. No implementar funcionalidades fuera del alcance pedido.
7. No inventar entidades, campos ni reglas de negocio que no hayan sido definidos.
8. No agregar dependencias si el stack actual ya resuelve la necesidad; si una es necesaria, justificarla.
9. Mantener el proyecto compilando después de cada cambio.

## 4.2 Secretos

- Nunca commitear secretos ni credenciales reales.
- Backend: variables de entorno o `backend/.env` (ignorado por git). Plantilla: `backend/.env.example`.
- Frontend: `frontend/.env.local` (ignorado). Plantilla: `frontend/.env.example`.
- Las variables `VITE_*` se exponen en el navegador: **nunca** colocar secretos allí.
- `OPENAI_API_KEY`, `OPENROUTER_API_KEY`, `JWT_SECRET` y credenciales de BD solo existen en el backend.

## 4.3 Contrato backend ↔ frontend

- Los DTOs del backend son la fuente del contrato HTTP.
- El frontend modela esos contratos con types de TypeScript, sin asumir campos que el backend no exponga.
- Todas las rutas de la API usan el prefijo `/api`.
- Los errores siguen un formato uniforme (`ApiError` en backend ↔ `ApiError` en `frontend/src/shared/types/api.types.ts`).
- Mantener nombres conceptualmente consistentes:

```text
Backend:  Product, ProductController, ProductService, ProductResponse
Frontend: features/products/, ProductListPage, ProductForm, productApi, ProductResponse
```

## 4.4 Seguridad

- El backend es responsable de la autorización real. El frontend solo oculta/redirige por UX.
- JWT se envía como `Authorization: Bearer <token>`.
- Nunca hardcodear tokens ni credenciales.
- No exponer stack traces ni mensajes internos al cliente.

---

# 5. Flujo para implementar una feature

1. Identificar el módulo correspondiente.
2. Backend: DTOs → entity (solo si el modelo lo requiere) → repository → service (interface + impl) → controller → manejo de errores → pruebas donde aporten valor. Detalle en [`AGENTS/backend.md`](AGENTS/backend.md).
3. Frontend: types → service de la feature (`xxxApi.ts`) → hook → components → page → ruta. Detalle en [`AGENTS/frontend.md`](AGENTS/frontend.md).
4. No modificar módulos no relacionados.
5. Verificar que backend y frontend compilen.

---

# 6. Principios generales

Priorizar:

- claridad y mantenibilidad
- separación de responsabilidades
- nombres descriptivos
- arquitectura simple y progresiva
- bajo acoplamiento
- organización por feature a medida que el proyecto crece

Evitar:

- sobrearquitectura y abstracciones innecesarias
- duplicación
- clases o componentes gigantes
- services o stores globales gigantes
- lógica de negocio en controllers o componentes visuales
- acceso a datos fuera de repositories
- carpetas o clases ficticias solo para "llenar" la estructura
- nombres en español dentro del código
