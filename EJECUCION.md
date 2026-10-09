instalar codegraph

irm https://raw.githubusercontent.com/colbymchenry/codegraph/main/install.ps1 | iex

codegraph install

codegraph init

# Ejecución del proyecto

Guía rápida para levantar **backend** y **frontend** en local (Windows, PowerShell).

**Requisitos:** Java 25, Node.js 24+ y PostgreSQL (local o remoto).

Se usan **dos terminales**: una para el backend y otra para el frontend.

---

## Terminal 1: Backend

```powershell
cd D:\Repositorios\utp-ebenezer-store-management\backend

pega el .env

.\mvnw.cmd spring-boot:run
```

Backend disponible en `http://localhost:8080/api`.

## Terminal 2: Frontend

```powershell
cd D:\Repositorios\utp-ebenezer-store-management\frontend

pega el .env.local

npm install

npm run dev
```

Abrir `http://localhost:5173` e iniciar sesión con `admin` y la contraseña definida en `ADMIN_PASSWORD`.

> El usuario admin se crea automáticamente solo si la tabla de usuarios está vacía y `ADMIN_PASSWORD` tiene valor.

## Comandos útiles

| Dónde | Comando | Para qué |
|---|---|---|
| backend | `.\mvnw.cmd compile` | Compilar |
| backend | `.\mvnw.cmd test` | Pruebas (requiere BD) |
| frontend | `npm run build` | Type-check + build |
| frontend | `npm run lint` | Linter (oxlint) |

---

# Prompt plantilla para crear un módulo

Copiar el texto y reemplazar `<MÓDULO>` (Categories, Products, Suppliers…) y `<PANTALLA>` por el archivo del prototipo que corresponda.

> Recomendación: activar el **modo plan** (Shift+Tab en Claude Code) antes de enviar el prompt. Así el agente solo lee y propone, sin modificar nada hasta aprobar el plan.

```text
Voy a crear el módulo <MÓDULO> (backend + frontend).

Antes de hacer cualquier cosa:
1. Lee AGENTS.md y las reglas de cada área: AGENTS/backend.md, AGENTS/frontend.md y AGENTS/database.md.
2. Revisa el modelo de datos en database/ (eben_ezer.dbml, 001_ddl.sql y 002_seed.sql).
   Usa solo las tablas, columnas y relaciones que existen ahí. No inventes campos ni reglas de negocio.
3. Para las vistas, usa siempre el prototipo como referencia visual y funcional:
   - prototype/<PANTALLA>.dc.html (por ejemplo, prototype/EECategorias.dc.html)
   - prototype/SistemaDeDiseno.dc.html (colores, tipografía y componentes)
   - prototype/screenshots/ (capturas, si hay alguna de este módulo)
   - prototype/ee-data.js (datos de ejemplo y estructura que espera la UI)
4. Revisa cómo están hechos los módulos que ya existen (auth, users) y sigue los mismos
   patrones, paquetes y convenciones. No dupliques código ni helpers.

Después, NO implementes nada todavía. Crea un plan que incluya:
- Archivos que vas a CREAR (backend: DTOs, entity, repository, service + impl, controller;
  frontend: types, xxxApi.ts, hook, components, page, ruta).
- Archivos existentes que vas a MODIFICAR y por qué (rutas, menú, SecurityConfig, etc.).
- Endpoints con su método, ruta /api/..., request y response.
- Reglas de validación y permisos por rol.
- Cómo se ve cada vista según el prototipo (tabla, filtros, formulario, modales).
- Dudas o datos que falten en database/ o en el prototipo. Pregúntamelas, no las asumas.
- Cómo vas a verificar que funciona (compilar con mvnw y npm run build, y probar los endpoints).

Todo el código en inglés. Espera mi aprobación antes de empezar.
```

## Pantallas del prototipo por módulo

| Módulo | Archivo en `prototype/` |
|---|---|
| Dashboard | `EEDashboard.dc.html` |
| Users | `EEUsuarios.dc.html` |
| Categories | `EECategorias.dc.html` |
| Products | `EEProductos.dc.html` |
| Suppliers | `EEProveedores.dc.html` |
| Purchases (ingresos) | `EEIngresos.dc.html` |
| Inventory | `EEInventario.dc.html` |
| Sales | `EEVentas.dc.html`, `EEHistVentas.dc.html` |
| Internal consumption | `EEConsumo.dc.html` |
| History | `EEHistoriales.dc.html` |
| AI Assistant | `EEAsistente.dc.html` |
| Settings | `EEConfig.dc.html` |
