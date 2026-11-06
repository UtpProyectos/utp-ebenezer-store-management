# Backend rules

Reglas específicas para `backend/`. Complementan [`../AGENTS.md`](../AGENTS.md), que siempre tiene prioridad en reglas globales.

---

## 1. Stack y comandos

- Java 25 · Spring Boot 4.1.1 · Maven Wrapper
- PostgreSQL · Spring Data JPA / Hibernate
- Spring Security · JJWT 0.12.x · Spring Validation
- Lombok · DevTools

Comandos (desde `backend/`):

```bash
./mvnw spring-boot:run        # levantar la API en http://localhost:8080
./mvnw compile                # compilar
./mvnw test                   # pruebas (requiere BD accesible)
./mvnw clean package          # generar el jar
```

En Windows PowerShell usar `.\mvnw.cmd`.

Endpoint de verificación: `GET /api/health` → `{"status":"UP"}` (público).

---

## 2. Paquete raíz

```text
pe.edu.utp.ebenezer
```

La clase principal permanece en el paquete raíz y no debe duplicarse:

```text
pe.edu.utp.ebenezer.EbenezerBackendApplication
```

---

## 3. Arquitectura por capas

```text
Controller  →  Service  →  Repository  →  PostgreSQL
```

Nunca `Controller → Repository`. Los controllers no consultan repositories directamente.

---

## 4. Estructura

```text
src/main/java/pe/edu/utp/ebenezer/
├── api/
│   ├── controller/
│   │   ├── health/          # HealthController (/api/health)
│   │   ├── auth/  user/     # Implementados (AuthController, UserController, RoleController)
│   │   ├── category/  unit/  product/  promotion/  supplier/  purchase/
│   │   ├── inventory/  sale/  consumption/  shoppinglist/  settings/  ai/
│   └── dto/
│       ├── auth/  user/  category/  unit/  product/  promotion/  supplier/
│       ├── purchase/  inventory/  sale/  consumption/  shoppinglist/  settings/  ai/
├── config/                  # SecurityConfig, CorsConfig, DataInitializer, *Properties
├── domain/
│   ├── entity/              # Entidades JPA (plano)
│   ├── repository/<feature>/ # Spring Data JPA agrupado por feature
│   └── enums/               # Enums del dominio
├── service/
│   ├── auth/  user/         # Implementados
│   ├── category/  unit/  product/  promotion/  supplier/  purchase/  inventory/
│   ├── sale/  consumption/  shoppinglist/  settings/  dashboard/  ai/
├── security/                # JwtService, JwtAuthenticationFilter, CustomUserDetailsService, CurrentUserProvider
├── exception/               # GlobalExceptionHandler, ApiError, excepciones
├── ai/
│   ├── provider/            # AiProvider, OpenAiCompatibleClient, OpenAiProvider, OpenRouterProvider, FallbackAiProvider
│   ├── model/               # AiMessage, AiToolCall, AiToolDefinition, AiCompletion(Request), AssistantContext
│   └── prompt/              # AssistantPrompts (textos) + AssistantPromptBuilder
└── EbenezerBackendApplication.java
```

Los paquetes aún vacíos existen mediante `package-info.java` (no usar `.gitkeep`). Al agregar la primera clase real, el `package-info.java` puede mantenerse como documentación del paquete.

---

## 5. Agrupación por feature

Cuando una capa empieza a tener varias clases, agruparlas por feature. Aplica a controllers, DTOs, services y repositories.

Evitar:

```text
service/
├── UserService.java
├── ProductService.java
├── InventoryService.java
└── SaleService.java
```

Preferir:

```text
service/
├── user/
│   ├── UserService.java
│   └── UserServiceImpl.java
└── product/
    ├── ProductService.java
    └── ProductServiceImpl.java
```

No crear subcarpetas si solo existe una clase sencilla.

---

## 6. API layer

### Controllers (`api/controller/<feature>/`)

Deben: recibir requests, validar DTOs (`@Valid`), delegar al service, devolver responses y códigos HTTP adecuados.

No deben contener: lógica de negocio, consultas JPA, reglas de inventario, cálculos comerciales, generación de JWT, llamadas a repositories.

Rutas con prefijo `/api` (ej. `@RequestMapping("/api/products")`).

### DTOs (`api/dto/<feature>/`)

- Nombres: `XxxRequest`, `XxxResponse` (ej. `ProductRequest`, `ProductResponse`, `LoginRequest`, `ChatRequest`).
- Preferir `record` para DTOs inmutables.
- Validaciones con Jakarta Validation (`@NotBlank`, `@Positive`, ...).
- **Nunca** exponer una entidad JPA desde un controller.

```text
Correcto:   Entity → Service → Response DTO → Controller
Incorrecto: Entity → Controller → Client
```

---

## 7. Domain layer

### Entities (`domain/entity/`)

Solo entidades JPA. Mapean 1:1 el modelo de [`database.md`](database.md) (nombres físicos en su §1.1) y [`database/001_ddl.sql`](../database/001_ddl.sql):

```text
Role, User, Category, UnitOfMeasure, Product, Promotion, Supplier, Purchase, PurchaseDetail, Lot,
Sale, SaleDetail, SaleHistory, InternalConsumption, InternalConsumptionDetail, InventoryMovement,
ShoppingList, ShoppingListDetail, BusinessSettings
```

- No usar entidades como DTOs.
- No agregar campos definitivos si aún no se definieron en el modelo de datos.
- Tablas y columnas en inglés, `snake_case` (ej. `purchase_detail`, `unit_price`). Si cambia una entidad, actualizar también el DDL y el `.dbml`.
- Lombok `@Getter @Setter @NoArgsConstructor` (no `@Data`). `@ManyToOne(fetch = LAZY)` siempre.
- `@OneToMany` solo cabecera → detalle (`Purchase.details`, `Sale.details`, `InternalConsumption.details`, `ShoppingList.details`) con `PERSIST/MERGE`; nunca `CascadeType.REMOVE`. Al agregar un detalle, setear ambos lados (`detail.setSale(sale)` + `sale.getDetails().add(detail)`).
- `created_at` / `updated_at` los llena Hibernate; las fechas de negocio (`saleDate`, `purchaseDate`, `movementDate`…) las asigna el service.
- Sin stock, FEFO ni conversiones de unidades en entidades.

### Repositories (`domain/repository/`)

Interfaces Spring Data JPA. Solo acceso a datos; sin reglas de negocio. Agrupadas por feature (`domain/repository/product/ProductRepository.java`). El stock se consulta con `InventoryMovementRepository.sumBaseQuantityByProductId` / `sumBaseQuantityByLotId`; agregar nuevas consultas aquí, no en services.

### Enums (`domain/enums/`)

Valores del dominio que no deben ser texto libre, persistidos con `@Enumerated(EnumType.STRING)`: `RoleName`, `UnitType`, `SupplierType`, `PurchaseStatus`, `PaymentMethod`, `SaleStatus`, `SaleHistoryAction`, `InventoryMovementType`, `ShoppingListSource`, `ShoppingListStatus`. Equivalencias con los nombres funcionales en [`database.md`](database.md) §1.1.

---

## 8. Service layer (`service/<feature>/`)

- Interface + implementación: `ProductService` / `ProductServiceImpl`.
- **No** usar prefijo `I` (`IProductService` ✗).
- Responsabilidades: reglas de negocio, coordinar repositories, transacciones (`@Transactional`), transformar entities ↔ DTOs (o mappers si se incorporan después).
- Mantener clases pequeñas: nada de `StoreService` que controle todo.

---

## 9. Dependency injection

Constructor injection. Con Lombok:

```java
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
}
```

Evitar field injection (`@Autowired` sobre campos).

---

## 10. Security (`security/` y `config/SecurityConfig`)

Implementado (`config/SecurityConfig.java` + `security/`):

- API stateless, CSRF deshabilitado, CORS habilitado, sin form login ni HTTP Basic.
- Públicos: `/api/health`, `/api/auth/login`, `/error`, preflight `OPTIONS`. Todo lo demás requiere JWT.
- Errores 401/403 se delegan a `GlobalExceptionHandler` → respuesta `ApiError`. Login fallido o usuario inactivo → 401 `Invalid username or password`.
- `PasswordEncoder` = BCrypt. `@EnableMethodSecurity` activo.

```text
security/
├── JwtProperties.java              # app.jwt.secret (Base64, ≥256 bits) / app.jwt.expiration-ms
├── JwtService.java                 # generar / validar JWT (HS256, subject = username, claim role)
├── JwtAuthenticationFilter.java    # OncePerRequestFilter; recarga el usuario en cada request
├── CustomUserDetailsService.java   # User → UserDetails con authority ROLE_ADMIN / ROLE_CASHIER
├── CurrentUserProvider.java        # User autenticado para los services
└── SecurityConstants.java
```

Endpoints:

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` | público → `{ token, tokenType, expiresIn, user }` |
| GET | `/api/auth/me` | autenticado |
| PUT | `/api/auth/password` | autenticado (cambia su propia contraseña) |
| GET / POST | `/api/users` | ADMIN |
| GET / PUT | `/api/users/{id}` | ADMIN |
| PATCH | `/api/users/{id}/status` | ADMIN (no puede desactivarse a sí mismo) |
| PUT | `/api/users/{id}/password` | ADMIN (reset) |
| GET | `/api/roles` | ADMIN |
| POST | `/api/products`, `/api/categories`, `/api/suppliers` | autenticado (el cajero puede crear; editar y activar/desactivar sigue siendo ADMIN) |
| GET | `/api/inventory` | autenticado (stock y estado por producto activo) |
| GET | `/api/inventory/movements?from=&to=` | ADMIN (kardex del período, ambos días incluidos; hoy por defecto) |
| POST | `/api/inventory/movements` | autenticado (retiro `WASTE` / `RETURN`, FEFO si no se indica lote) |
| POST | `/api/purchases` | autenticado (ingreso: detalle → lote → movimiento `PURCHASE`) |
| GET | `/api/purchases?from=&to=` | ADMIN (compras del período) |
| POST | `/api/sales` | autenticado (precio del producto + promoción vigente; FEFO sin lotes vencidos → movimientos `SALE`; `sale_history` `CREATED`) |
| GET | `/api/sales?date=yyyy-MM-dd` o `?from=&to=` | autenticado (ventas del día o del período, hoy por defecto; todos los estados) |
| GET | `/api/sales/changes?date=yyyy-MM-dd` | autenticado (ediciones y anulaciones del día desde `sale_history`) |
| PUT | `/api/sales/{id}` | autenticado (corrige método de pago y cantidades con motivo: `REVERSAL` de las líneas cambiadas + FEFO de nuevo; estado `EDITED`) |
| PATCH | `/api/sales/{id}/cancel` | autenticado (anula con motivo: `REVERSAL` por lote, estado `CANCELLED`; nunca se borra) |
| POST | `/api/internal-consumptions` | autenticado (mismo FEFO que la venta → movimientos `INTERNAL_CONSUMPTION`, sin ingreso) |
| GET | `/api/internal-consumptions?from=&to=` | ADMIN (consumos del período) |
| GET | `/api/shopping-lists/current` | autenticado (última lista; 204 si no hay o está `CANCELLED`) |
| POST | `/api/shopping-lists/generate` | autenticado (`{ productIds? }`: agrega a la lista abierta —`PENDING`/`PARTIAL`— o crea una: los productos indicados (`MANUAL`) o los activos con stock ≤ mínimo (`SYSTEM`); cantidad sugerida = 2 × mínimo − stock, mín. 1; proveedor y costo de la última compra) |
| PATCH | `/api/shopping-lists/details/{id}` | autenticado (`{ suggestedQuantity?, purchased? }`; recalcula costo y estado `PENDING` / `PARTIAL` / `COMPLETED`) |
| DELETE | `/api/shopping-lists/details/{id}` | autenticado (quita el ítem; una lista vacía pasa a `CANCELLED`) |
| GET | `/api/promotions` | autenticado (promociones activas y vigentes, la más reciente primero) |
| POST | `/api/ai/chat` | autenticado (asistente IA de solo lectura; las herramientas de compras y consumo interno solo se ofrecen al ADMIN — ver [`ai.md`](ai.md)) |

La asignación de lotes de cualquier salida (retiro, venta, consumo) se hace con `service/inventory/StockAllocator`: FEFO, luego lotes sin vencimiento y al final el stock sin lote (ajustes iniciales). Venta y consumo excluyen lotes vencidos; el retiro WASTE/RETURN los incluye. Las conversiones de unidades usan `service/unit/UnitConverter`.

Los usuarios no se eliminan físicamente (están referenciados por ventas/compras): se desactivan.

Al arrancar, `config/DataInitializer` crea los roles `ADMIN` y `CASHIER` si faltan, y el admin inicial (`ADMIN_USERNAME` / `ADMIN_PASSWORD` / `ADMIN_NAME`) solo si la tabla `users` está vacía.

Convenciones para las features:

- Autorización por rol con `@PreAuthorize("hasRole('ADMIN')")` en el controller (clase o método). Sin anotación = cualquier usuario autenticado.
- El usuario que registra una venta, compra, consumo o movimiento se obtiene con `CurrentUserProvider.getCurrentUser()` dentro del service (`@Transactional`); nunca se recibe `userId` desde el cliente.
- Nuevos endpoints públicos se agregan a `PUBLIC_ENDPOINTS` en `SecurityConfig`.
- Sin lógica comercial en `security/`.

---

## 11. Config (`config/`)

- `SecurityConfig` — `SecurityFilterChain`, `PasswordEncoder`, `AuthenticationManager` cuando corresponda.
- `CorsConfig` + `CorsProperties` — orígenes desde `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`, separados por coma).
- `RestClientConfig` + `AiProperties` — `RestClient` con timeouts para los proveedores de IA (`app.ai.*`).

Sin lógica de negocio en `config/`.

---

## 12. Exception handling (`exception/`)

Existente:

- `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `ApiError` — formato uniforme de error
- `ResourceNotFoundException` → 404
- `BusinessException` → 422
- `UnauthorizedException` → 401
- `AiUnavailableException` → 503 (`The assistant is not available right now`; el motivo solo va al log)

Formato de respuesta:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found",
  "path": "/api/products/10",
  "fieldErrors": { "name": "must not be blank" }
}
```

`fieldErrors` solo aparece en errores de validación. Nunca propagar stack traces al cliente; los errores inesperados se loguean y responden 500 con mensaje genérico.

Lanzar las excepciones de dominio desde los services, no construir respuestas de error en los controllers.

---

## 13. AI module (`ai/`)

Implementado. Reglas completas, prompt, herramientas y diagramas en [`ai.md`](ai.md).

- `ai/provider/` — `AiProvider` (abstracción). `OpenAiProvider` y `OpenRouterProvider` comparten `OpenAiCompatibleClient` (misma API `/chat/completions`). `FallbackAiProvider` (`@Primary`) prueba OpenAI y, si no está configurado o falla (401/402/429/5xx/timeout), usa OpenRouter. La aplicación depende solo de `AiProvider`.
- `ai/model/` — modelos internos; no confundir con los DTOs HTTP de `api/dto/ai/`.
- `ai/prompt/` — `AssistantPrompts` guarda **todos** los textos del prompt; `AssistantPromptBuilder` inyecta usuario, rol y fecha. No dispersar prompts hardcodeados.
- `service/ai/` — `AssistantService(Impl)`: bucle de *tool calling* (máx. 4 rondas), sin `@Transactional` para no retener conexiones mientras responde el proveedor.
- `service/ai/tool/` — herramientas de **solo lectura** (`AssistantTool`) que llaman services existentes, nunca repositories. `AssistantToolRegistry` filtra por rol porque los services se llaman sin el `@PreAuthorize` del controller.
- Sin tablas de IA ni pgvector (`database.md` §19). La regla de reposición se comparte con la lista de compra vía `service/shoppinglist/RestockCalculator`.

---

## 14. Resources y configuración

Archivo principal: `src/main/resources/application.yaml`.

- Carga `backend/.env` automáticamente (`spring.config.import: optional:file:.env[.properties]`). Las variables de entorno reales tienen prioridad.
- `spring.jpa.open-in-view: false`.
- La BD es remota (Supabase, ~200 ms por ida y vuelta desde Perú): cada consulta cuenta. HikariCP usa `keepalive-time` y `aliveBypassWindowMs` (fijado en `EbenezerBackendApplication`) para no hacer un ping a la BD en cada request.
- `hibernate.default_batch_fetch_size: 50`: las relaciones lazy se cargan por lotes. Aun así, si un listado siempre lee una relación, traerla en la misma consulta (`@EntityGraph`, proyección o consulta agregada) en vez de consultar por cada fila.
- `ddl-auto: update` solo para desarrollo; revisar antes de producción (migraciones con Flyway se evaluarán cuando el modelo se estabilice).
- `server.error.include-stacktrace: never`.

Variables de entorno (ver `backend/.env.example`):

| Variable | Uso |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión PostgreSQL |
| `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_IDLE` | Conexiones por instancia (default 4 / 1). El pooler de Supabase en session mode admite 15 en total para Render + todos los backends locales |
| `SERVER_PORT` | Puerto (default 8080) |
| `JPA_SHOW_SQL` | Loguea el SQL de Hibernate (default `true`; usar `false` en producción) |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend (default `http://localhost:5173`) |
| `JWT_SECRET`, `JWT_EXPIRATION_MS` | JWT. Secreto Base64 de al menos 256 bits (`openssl rand -base64 32`); sin él la app no arranca |
| `ADMIN_NAME`, `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Admin inicial (solo si no hay usuarios) |
| `OPENAI_API_KEY`, `OPENAI_MODEL` | Proveedor principal del asistente (default `gpt-4.1-mini`) |
| `OPENROUTER_API_KEY`, `OPENROUTER_MODEL` | Fallback del asistente (default `openai/gpt-4.1-mini`) |
| `AI_TIMEOUT_SECONDS` | Espera por proveedor antes de pasar al siguiente (default 25) |

Nunca crear archivos con claves reales dentro de `resources`.

---

## 15. Reglas antes de modificar el backend

1. Inspeccionar la estructura existente.
2. Respetar el paquete `pe.edu.utp.ebenezer`.
3. No duplicar `EbenezerBackendApplication`.
4. No modificar `pom.xml` sin necesidad; no agregar dependencias si Spring Boot ya provee la funcionalidad.
5. Mantener compatibilidad con Java 25 y Spring Boot 4.1.1 (Spring Security 7, Jackson 3).
6. No inventar entidades, campos ni reglas de negocio no definidas.

---

## 16. Flujo para una nueva feature (ej. Products)

```text
api/dto/product/                 ProductRequest, ProductResponse
api/controller/product/          ProductController   (/api/products)
service/product/                 ProductService, ProductServiceImpl
domain/repository/product/       ProductRepository
domain/entity/                   Product
exception/                       usar ResourceNotFoundException / BusinessException
```

Agregar pruebas donde aporten valor (services con reglas de negocio, controllers con `@WebMvcTest`).
