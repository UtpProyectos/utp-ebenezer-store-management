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
│   │   ├── auth/  user/  product/  inventory/  purchase/  sale/  ai/
│   └── dto/
│       ├── auth/  user/  product/  category/  supplier/
│       ├── inventory/  purchase/  sale/  consumption/  ai/
├── config/                  # SecurityConfig, CorsConfig, CorsProperties
├── domain/
│   ├── entity/              # Entidades JPA
│   ├── repository/          # Spring Data JPA
│   └── enums/               # Enums del dominio
├── service/
│   ├── auth/  user/  product/  category/  supplier/  inventory/
│   ├── purchase/  sale/  consumption/  dashboard/  ai/
├── security/                # JWT, filtro, UserDetailsService
├── exception/               # GlobalExceptionHandler, ApiError, excepciones
├── ai/
│   ├── provider/            # AiProvider + OpenAiProvider / OpenRouterProvider
│   ├── model/               # Modelos internos de IA
│   └── prompt/              # Builders de prompts
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

Solo entidades JPA. Entidades previstas:

```text
User, Role, Category, Product, Supplier, Purchase, PurchaseDetail, Lot,
InventoryMovement, Sale, SaleDetail, InternalConsumption, InternalConsumptionDetail
```

- No usar entidades como DTOs.
- No agregar campos definitivos si aún no se definieron en el modelo de datos.
- Tablas y columnas en inglés, `snake_case` (ej. `purchase_detail`, `unit_price`).

### Repositories (`domain/repository/`)

Interfaces Spring Data JPA. Solo acceso a datos; sin reglas de negocio. Agrupar por feature cuando crezcan (`domain/repository/product/ProductRepository.java`).

### Enums (`domain/enums/`)

Para valores del dominio que no deben ser texto libre: `RoleName`, `PaymentMethod`, `InventoryMovementType`, `ProductStatus`, `PurchaseStatus`, `SaleStatus`.

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

Estado actual (`config/SecurityConfig.java`):

- API stateless, CSRF deshabilitado, CORS habilitado, sin form login ni HTTP Basic.
- Públicos: `/api/health`, `/error`, preflight `OPTIONS`. Todo lo demás requiere autenticación.
- Errores 401/403 se delegan a `GlobalExceptionHandler` → respuesta `ApiError`.
- `PasswordEncoder` = BCrypt.

Pendiente (cuando se implemente autenticación):

```text
security/
├── JwtService.java                 # generar / validar JWT, extraer claims
├── JwtAuthenticationFilter.java    # OncePerRequestFilter
├── CustomUserDetailsService.java   # carga del usuario
└── SecurityConstants.java
```

- Nuevos endpoints públicos (ej. `/api/auth/login`) se agregan a `PUBLIC_ENDPOINTS` en `SecurityConfig`.
- Configuración JWT en `app.jwt.secret` / `app.jwt.expiration-ms` (desde `JWT_SECRET`, `JWT_EXPIRATION_MS`).
- Sin lógica comercial en `security/`.

---

## 11. Config (`config/`)

- `SecurityConfig` — `SecurityFilterChain`, `PasswordEncoder`, `AuthenticationManager` cuando corresponda.
- `CorsConfig` + `CorsProperties` — orígenes desde `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`, separados por coma).
- `RestClientConfig` — (futuro) clientes HTTP para servicios externos (IA).

Sin lógica de negocio en `config/`.

---

## 12. Exception handling (`exception/`)

Existente:

- `GlobalExceptionHandler` (`@RestControllerAdvice`)
- `ApiError` — formato uniforme de error
- `ResourceNotFoundException` → 404
- `BusinessException` → 422
- `UnauthorizedException` → 401

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

Aislado de la lógica normal del negocio.

- `ai/provider/` — `AiProvider` (abstracción), `OpenAiProvider`, `OpenRouterProvider`. La aplicación depende solo de `AiProvider`. Si OpenAI falla o no tiene cuota → fallback a OpenRouter.
- `ai/model/` — modelos internos; no confundir con los DTOs HTTP de `api/dto/ai/`.
- `ai/prompt/` — builders de prompts, prompts reutilizables y contexto del negocio. No dispersar prompts hardcodeados.

**No implementar esta integración hasta que se solicite.**

---

## 14. Resources y configuración

Archivo principal: `src/main/resources/application.yaml`.

- Carga `backend/.env` automáticamente (`spring.config.import: optional:file:.env[.properties]`). Las variables de entorno reales tienen prioridad.
- `spring.jpa.open-in-view: false`.
- `ddl-auto: update` solo para desarrollo; revisar antes de producción (migraciones con Flyway se evaluarán cuando el modelo se estabilice).
- `server.error.include-stacktrace: never`.

Variables de entorno (ver `backend/.env.example`):

| Variable | Uso |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión PostgreSQL |
| `SERVER_PORT` | Puerto (default 8080) |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend (default `http://localhost:5173`) |
| `JWT_SECRET`, `JWT_EXPIRATION_MS` | JWT (pendiente de uso) |
| `OPENAI_API_KEY`, `OPENROUTER_API_KEY` | IA (pendiente de uso) |

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
