# Prompt para Claude Design — Eben-Ezer Store Management

Diseña el prototipo completo de **Eben-Ezer Store Management**, un sistema web administrativo para una bodega / minimarket.

Usa como guía las skills:
- `emil-design-eng`
- `animate`

Toma las pantallas iniciales compartidas como referencia funcional, pero **no las copies literalmente**. Mejora navegación, jerarquía, UX, consistencia y calidad visual.

## Contexto
El sistema gestionará:
- ventas
- inventario
- ingresos / compras
- productos
- categorías
- proveedores
- lotes y vencimientos
- consumo interno
- usuarios y roles
- dashboard
- alertas
- lista de recompra
- asistente de IA

El usuario principal será el propietario o administrador, aunque también puede haber cajeros.

La aplicación debe sentirse:
- sencilla
- rápida
- práctica
- moderna
- confiable
- agradable
- cercana a un negocio real
- fácil para personas no técnicas

Evita que parezca una fintech, banco o SaaS genérico.

## Idioma
El **idioma original de toda la interfaz será español**.

Todo texto visible debe estar en español:
- títulos
- menús
- botones
- etiquetas
- placeholders
- estados
- alertas
- formularios
- tablas
- mensajes
- asistente IA

## Dirección visual
Define tú mismo la identidad visual.

No quiero fijar una paleta exacta. Puedes explorar **verde** como una opción por su relación con comercio, frescura y cercanía, pero eres libre de elegir otra dirección si funciona mejor.

Evita usar azul como solución automática.

Define:
- color principal
- secundarios
- semánticos
- fondo
- superficies
- tipografía
- radios
- sombras
- spacing
- iconografía
- densidad visual

Prioriza accesibilidad y contraste.

# Layout general

## Sidebar izquierdo
Usa sidebar izquierdo como navegación principal en desktop.

Secciones sugeridas:

### Principal
- Dashboard
- Ventas
- Inventario
- Ingresos / Compras

### Catálogo
- Productos
- Categorías
- Proveedores

### Operaciones
- Consumo interno
- Lista de compra

### Inteligencia
- Asistente IA

### Administración
- Usuarios
- Configuración

Puede ser colapsable.

## Topbar
Debe ser simple e incluir:
- título de pantalla
- búsqueda global si aporta valor
- notificaciones
- caja o sucursal activa
- usuario
- rol
- avatar
- menú de sesión

# Pantallas

## 1. Login
Debe contener:
- logo Eben-Ezer
- nombre del negocio
- mensaje de bienvenida
- usuario o correo
- contraseña
- mostrar / ocultar contraseña
- recordar sesión si aporta valor
- botón Iniciar sesión
- estado de carga
- error de credenciales
- recuperación de contraseña solo si tiene sentido

Debe sentirse cercana y moderna.

## 2. Dashboard
Debe responder:
- ¿cuánto se vendió hoy?
- ¿cuánto ingresó hoy?
- ¿qué productos necesitan reposición?
- ¿qué productos están por vencer?
- ¿qué productos se venden más?
- ¿qué alertas requieren atención?

Debe incluir:
- Ventas de hoy
- Ingresos del día
- Productos vendidos
- Stock crítico
- alertas de stock y vencimientos
- ventas últimos días
- productos más vendidos
- actividad reciente
- acceso visible al Asistente IA

Ejemplos para IA:
- ¿Qué productos debo comprar hoy?
- ¿Qué productos tienen stock crítico?
- ¿Qué se vendió más esta semana?
- ¿Qué productos están próximos a vencer?

## 3. Inventario
Debe incluir:
- búsqueda
- filtros
- categoría
- stock actual
- stock mínimo
- estado
- vencimiento
- selección para recompra
- acciones

Estados:
- Normal
- Bajo
- Crítico
- Próximo a vencer
- Vencido

Resumen superior:
- Productos totales
- Stock crítico
- Stock bajo
- Próximos a vencer

CTA:
- Generar lista de compra

## 4. Ingresos / Compras
Debe permitir:
- seleccionar producto
- proveedor
- canal o lugar de compra
- cantidad
- unidad
- costo total
- costo unitario calculado
- precio de venta
- precio sugerido
- margen estimado
- fecha de compra
- lote
- fecha de vencimiento
- observaciones
- guardar ingreso / lote

Debe sentirse como formulario asistido.

## 5. Ventas / POS
Debe ser rápida y operativa.

Zona de productos:
- búsqueda por nombre
- búsqueda por código
- categorías rápidas
- productos frecuentes
- stock disponible
- precio
- acción agregar

Carrito:
- producto
- cantidad
- precio unitario
- subtotal
- editar cantidad
- eliminar

Resumen:
- subtotal
- total
- Cobrar
- Cancelar venta

Consumo interno:
- opción clara para marcar la operación como consumo interno
- explicar que descuenta stock pero no genera ingreso en caja

## 6. Productos
Debe incluir:
- listado
- búsqueda
- filtros
- categoría
- precio
- costo
- stock
- estado
- acciones

Acciones:
- Nuevo producto
- Editar
- Activar / desactivar
- Ver detalle

Crear / editar:
- Nombre
- Categoría
- Descripción
- Código de barras opcional
- Unidad de medida
- Stock mínimo
- Precio de venta
- Estado

## 7. Categorías
Debe permitir:
- listar
- crear
- editar
- activar / desactivar
- ver cantidad de productos asociados

Ejemplos:
- Bebidas
- Lácteos
- Snacks
- Limpieza
- Librería
- Abarrotes

## 8. Proveedores
Debe permitir:
- nombre
- contacto
- teléfono
- dirección opcional
- tipo
- estado
- observaciones
- última compra
- cantidad de compras
- productos asociados

## 9. Consumo interno
Debe permitir:
- seleccionar productos
- cantidad
- motivo
- observación
- responsable
- fecha

Debe indicar claramente:
- reduce stock
- no genera ingreso de caja

Mostrar historial.

## 10. Lista de compra / Reposición
Debe incluir:
- producto
- stock actual
- stock mínimo
- cantidad sugerida
- cantidad a comprar editable
- proveedor habitual
- prioridad

Acciones:
- Marcar como comprado
- Exportar / imprimir si aporta valor
- Registrar ingreso de mercadería

Debe existir relación visual:
Inventario → Lista de compra → Ingreso de mercadería.

## 11. Asistente IA
Diseña una sección propia para el **Asistente Eben-Ezer**.

No debe parecer un chatbot genérico.

Debe responder preguntas del negocio como:
- ¿Qué productos debo reponer?
- ¿Qué productos están por vencer?
- ¿Cuánto vendí hoy?
- ¿Qué producto se vende más?
- ¿Cuáles fueron mis ventas de esta semana?
- ¿Qué productos están en stock crítico?
- ¿Qué proveedor utilizo más?
- ¿Qué debería comprar mañana?

Debe incluir:
- área de conversación
- input
- preguntas sugeridas
- respuestas claras
- cards/resúmenes cuando convenga
- acciones hacia módulos relacionados

Ejemplo:
“Hay 4 productos con stock crítico”
Acciones:
- Ver inventario
- Generar lista de compra

Puede existir acceso rápido al asistente desde otras pantallas.

## 12. Usuarios y roles
Debe permitir:
- listar
- crear
- editar
- activar / desactivar
- asignar rol

Roles iniciales:
- Administrador
- Cajero

Mostrar:
- nombre
- usuario
- rol
- estado
- último acceso

## 13. Configuración
Puede incluir:
- nombre del negocio
- logo
- moneda
- datos básicos
- stock mínimo predeterminado
- alertas
- preferencias generales

No inventar opciones innecesarias.

# Estados de UI
Diseña:
- normal
- hover
- active
- focus
- disabled
- loading
- empty
- error
- success

Aplicar especialmente a:
- botones
- inputs
- selects
- tablas
- filtros
- modales
- sidebar
- cards
- carrito
- alertas

Usa `emil-design-eng` para pulir interacción y detalle.

# Animaciones
Usa `animate`.

Las animaciones deben ser discretas y funcionales.

Aplicarlas solo para:
- feedback
- orientación
- transición de estado
- percepción de rapidez

Ejemplos:
- selección de producto
- agregar al carrito
- cambio de navegación
- expansión del sidebar
- modal / drawer
- actualización de badges
- confirmar una venta
- guardar un ingreso
- alertas
- interacción con IA

Evita animaciones decorativas constantes.

# Responsive
Desktop first.

También define comportamiento para:
- tablet
- móvil

En móvil:
- sidebar → drawer
- POS reorganiza catálogo y carrito
- tablas → cards o listas cuando convenga
- CTAs principales siempre accesibles

# Página final obligatoria: Sistema de Diseño

Al finalizar todos los mockups, crea una pantalla adicional llamada:

## **Sistema de Diseño**

Debe documentar visualmente el design system usado en Eben-Ezer.

Debe mostrar:

### Identidad
- logo
- marca
- dirección visual

### Colores
- primary
- secondary
- background
- surface
- text
- muted
- border
- success
- warning
- danger
- information

Mostrar nombre, muestra visual y valor elegido.

### Tipografía
- Display
- H1
- H2
- H3
- Body
- Small
- Caption
- Button
- Label

### Spacing
Mostrar escala utilizada.

### Border radius
Mostrar radios definidos.

### Shadows
Mostrar niveles de sombra.

### Buttons
- primary
- secondary
- ghost
- danger
- disabled
- loading

### Inputs
- input
- search
- select
- textarea
- checkbox
- radio

Estados:
- normal
- focus
- error
- disabled

### Cards
Mostrar tipos de cards usados.

### Badges / Chips
Mostrar estados:
- normal
- bajo
- crítico
- éxito
- warning

### Tables
Mostrar ejemplo de tabla.

### Navigation
Mostrar:
- sidebar
- item
- active item
- collapsed state
- topbar

### Icons
Definir estilo de iconografía.

### Feedback
Mostrar:
- toast
- alert
- modal
- empty state
- loading state
- confirmation

### Motion
Documentar:
- durations
- easing
- cuándo se anima
- cuándo NO se anima

Esta página debe servir como referencia visual para futuras pantallas.

# Reglas finales
- No copiar literalmente los mockups actuales.
- Mejorarlos.
- Toda la UI visible debe estar en español.
- Evitar diseño SaaS genérico.
- Evitar exceso de cards.
- Evitar exceso de gráficos.
- No usar azul como color principal por defecto.
- Puedes considerar verde, pero define tú la mejor dirección.
- Priorizar claridad, velocidad y usabilidad.
- Utilizar HeroUI como referencia de componentes para futura implementación.
- Tailwind se usará para layout y personalización.
- No implementar backend.
- Trabajar como prototipo visual y UX.

# Resultado esperado

Flujo principal:

Login
↓
Dashboard
├── Ventas
├── Inventario
│   └── Lista de compra
│       └── Ingreso de mercadería
├── Ingresos / Compras
├── Productos
├── Categorías
├── Proveedores
├── Consumo interno
├── Asistente IA
├── Usuarios
└── Configuración

Y al final:

Sistema de Diseño

Primero define la dirección visual y la arquitectura general.
Después diseña las pantallas.
Al final crea obligatoriamente la página **Sistema de Diseño** con todos los patrones visuales aplicados.
