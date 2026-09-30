# Base de datos — Eben-Ezer

## 1. Propósito

Este documento define el modelo de datos y las reglas principales del sistema **Eben-Ezer**, una bodega/minimarket que necesita controlar:

- productos y categorías;
- unidades de medida;
- compras e ingresos de mercadería;
- lotes y fechas de vencimiento;
- ventas;
- consumo interno;
- promociones simples;
- movimientos de inventario;
- mermas y ajustes;
- auditoría de ventas;
- lista de compra/reposición;
- proveedores;
- usuarios y roles;
- configuración general del negocio;
- consultas analíticas realizadas por una IA.

Este archivo debe ser tomado como **fuente de conocimiento funcional** por Claude Code, Codex u otros agentes que trabajen en el repositorio.

## 1.1 Nombres físicos (inglés)

Este documento describe el dominio con los nombres funcionales en español. En el código y en la base de datos **todo está en inglés** (ver `AGENTS.md` §1). Scripts: [`database/001_ddl.sql`](../database/001_ddl.sql), [`database/002_seed.sql`](../database/002_seed.sql), modelo: [`database/eben_ezer.dbml`](../database/eben_ezer.dbml).

| Nombre funcional | Tabla | Entidad JPA |
|---|---|---|
| rol | `role` | `Role` |
| usuario | `users` (`user` es palabra reservada en PostgreSQL) | `User` |
| categoria | `category` | `Category` |
| unidad_medida | `unit_of_measure` | `UnitOfMeasure` |
| producto | `product` | `Product` |
| promocion | `promotion` | `Promotion` |
| proveedor | `supplier` | `Supplier` |
| compra | `purchase` | `Purchase` |
| detalle_compra | `purchase_detail` | `PurchaseDetail` |
| lote | `lot` | `Lot` |
| venta | `sale` | `Sale` |
| detalle_venta | `sale_detail` | `SaleDetail` |
| consumo_interno | `internal_consumption` | `InternalConsumption` |
| detalle_consumo | `internal_consumption_detail` | `InternalConsumptionDetail` |
| movimiento_inventario | `inventory_movement` | `InventoryMovement` |
| historial_venta | `sale_history` | `SaleHistory` |
| lista_compra | `shopping_list` | `ShoppingList` |
| detalle_lista_compra | `shopping_list_detail` | `ShoppingListDetail` |
| configuracion_negocio | `business_settings` | `BusinessSettings` |

Columnas: PK `<tabla>_id`, `cantidad` → `quantity`, `cantidad_base` → `base_quantity`, `fecha` → `<entidad>_date` (`sale_date`, `purchase_date`, `movement_date`…), `fecha_creacion` → `created_at`, `activo` → `active`, `observacion` → `notes`, `motivo` → `reason`. El detalle completo está en el DDL.

Valores de enums (se guardan como texto):

| Funcional | Enum | Valores |
|---|---|---|
| Rol | `RoleName` | ADMINISTRADOR → `ADMIN`, CAJERO → `CASHIER` |
| Tipo de unidad | `UnitType` | PESO → `WEIGHT`, VOLUMEN → `VOLUME`, UNIDAD → `UNIT` |
| Tipo de proveedor | `SupplierType` | `WHOLESALER`, `DISTRIBUTOR`, `SELF_SERVICE`, `MARKET`, `LOCAL`, `OTHER` |
| Estado de compra | `PurchaseStatus` | REGISTRADA → `REGISTERED`, ANULADA → `CANCELLED` |
| Método de pago | `PaymentMethod` | EFECTIVO → `CASH`, `YAPE_PLIN`, TARJETA → `CARD`, OTRO → `OTHER` |
| Estado de venta | `SaleStatus` | CONFIRMADA → `CONFIRMED`, EDITADA → `EDITED`, ANULADA → `CANCELLED` |
| Acción de historial | `SaleHistoryAction` | `CREATED`, `EDITED`, `CANCELLED`, `REACTIVATED` |
| Tipo de movimiento | `InventoryMovementType` | COMPRA → `PURCHASE`, VENTA → `SALE`, CONSUMO_INTERNO → `INTERNAL_CONSUMPTION`, MERMA → `WASTE`, AJUSTE_ENTRADA → `ADJUSTMENT_IN`, AJUSTE_SALIDA → `ADJUSTMENT_OUT`, DEVOLUCION → `RETURN`, REVERSA → `REVERSAL` |
| Origen de lista | `ShoppingListSource` | `MANUAL`, IA → `AI`, SISTEMA → `SYSTEM` |
| Estado de lista | `ShoppingListStatus` | PENDIENTE → `PENDING`, `PARTIAL`, COMPLETADA → `COMPLETED`, CANCELADA → `CANCELLED` |

El motivo de merma/ajuste (`inventory_movement.reason`) es texto libre.

---

# 2. Principios del modelo

## 2.1 No existe una tabla `inventario`

El sistema **NO debe crear una tabla `inventario` con `stock_actual`**.

El stock es un valor derivado de `movimiento_inventario`.

### Stock total de un producto

```sql
SUM(movimiento_inventario.cantidad_base)
```

agrupado por `id_producto`.

### Stock de un lote

```sql
SUM(movimiento_inventario.cantidad_base)
```

agrupado por `id_lote`.

Esto evita duplicar el estado del stock y previene inconsistencias entre tablas.

---

## 2.2 `movimiento_inventario` es el Kardex

Todo cambio físico de stock debe producir uno o más registros en `movimiento_inventario`.

Tipos principales:

- `COMPRA`
- `VENTA`
- `CONSUMO_INTERNO`
- `MERMA`
- `AJUSTE_ENTRADA`
- `AJUSTE_SALIDA`
- `DEVOLUCION`
- `REVERSA`

Las cantidades usan signo:

```text
COMPRA             +10
VENTA               -2
CONSUMO_INTERNO     -0.500
MERMA               -1
AJUSTE_ENTRADA      +3
AJUSTE_SALIDA       -2
REVERSA             +2
```

`cantidad_base` siempre está expresada en la **unidad base del producto**.

---

# 3. Entidades

## 3.1 Seguridad y usuarios

### `rol`

Define los perfiles del sistema.

Ejemplos:

- `ADMINISTRADOR`
- `CAJERO`

Relación:

```text
ROL 1:N USUARIO
```

### `usuario`

Representa a las personas con acceso al sistema.

Datos principales:

- rol;
- nombre;
- username;
- email;
- password hash;
- activo;
- último acceso.

Un usuario puede registrar ventas, compras, consumos internos, movimientos y listas de compra.

---

# 4. Productos

## 4.1 `categoria`

Agrupa productos.

Ejemplos:

- Bebidas
- Lácteos
- Snacks
- Limpieza
- Librería
- Abarrotes
- Panadería

Relación:

```text
CATEGORIA 1:N PRODUCTO
```

---

## 4.2 `unidad_medida`

Define las unidades en las que se puede registrar una cantidad.

Ejemplos recomendados:

| Unidad | Tipo | Factor |
|---|---|---:|
| KG | PESO | 1 |
| G | PESO | 0.001 |
| L | VOLUMEN | 1 |
| ML | VOLUMEN | 0.001 |
| UND | UNIDAD | 1 |

El factor se usa para convertir una cantidad a una unidad canónica.

Ejemplo:

```text
500 G = 0.500 KG
```

---

## 4.3 `producto`

Es el maestro de productos.

Cada producto tiene:

- categoría;
- unidad base;
- nombre;
- código de barras opcional;
- precio de venta;
- stock mínimo;
- estado activo/inactivo.

### Unidad base

Todo producto tiene una unidad base en la que se controla el stock.

Ejemplos:

```text
Huevo               -> KG
Arroz a granel      -> KG
Pan francés         -> UND
Aceite 1 L botella  -> UND
Arroz bolsa 1 kg    -> UND
```

La unidad usada en una venta o compra puede ser distinta, pero debe convertirse a la unidad base.

Ejemplo:

```text
Producto: Arroz a granel
Unidad base: KG

Venta registrada:
500 G

cantidad = 500
cantidad_base = 0.500
```

---

# 5. Promociones

## 5.1 `promocion`

Soporta promociones simples sobre **un solo producto** y con una **unidad de medida explícita**.

Relaciones:

```text
PRODUCTO 1:N PROMOCION
UNIDAD_MEDIDA 1:N PROMOCION
```

Ejemplos:

```text
Pan francés
3 UND por S/ 1.00

Arroz a granel
500 G por S/ 2.50

Huevo
1 KG por S/ 7.00
```

Campos importantes:

- producto;
- unidad de medida;
- cantidad promocional;
- precio promocional;
- repetible;
- fechas de vigencia;
- activo.

`cantidad_promocion` se interpreta en la unidad seleccionada para la promoción.

La unidad de la promoción debe ser compatible con la unidad base del producto.

Ejemplos:

```text
Producto con unidad base KG:
- KG permitido
- G permitido
- UND no permitido
- L no permitido

Producto con unidad base L:
- L permitido
- ML permitido
- KG no permitido

Producto con unidad base UND:
- UND permitido
- KG no permitido
```

La conversión se realiza en la capa de servicio mediante `unidad_medida.factor_conversion`.

Ejemplo:

```text
Producto: Arroz a granel
Unidad base: KG

Promoción:
500 G por S/ 2.50

Conversión:
500 G = 0.500 KG
```

No guardar `cantidad_base` en `promocion`; debe calcularse cuando se evalúe la promoción para evitar datos duplicados.

Si es repetible:

```text
3 UND = S/1
6 UND = S/2
9 UND = S/3
```

o:

```text
500 G = S/2.50
1 KG = S/5.00
```

No implementar por ahora promociones compuestas entre varios productos.

---

# 6. Proveedores y compras

## 6.1 `proveedor`

Guarda los proveedores usados por la bodega.

Puede representar:

- mayorista;
- distribuidor;
- autoservicio;
- mercado;
- local;
- otro.

Datos principales:

- nombre;
- documento;
- teléfono;
- tipo;
- contacto;
- dirección;
- observación;
- activo.

No existe una tabla `producto_proveedor`.

Los productos que un proveedor ha suministrado se obtienen del historial:

```text
PROVEEDOR
   -> COMPRA
      -> DETALLE_COMPRA
         -> PRODUCTO
```

---

## 6.2 `compra`

Representa el ingreso comercial de mercadería.

Relaciones:

```text
PROVEEDOR 1:N COMPRA
USUARIO   1:N COMPRA
COMPRA    1:N DETALLE_COMPRA
```

El proveedor puede ser opcional para compras ocasionales.

Estados iniciales:

- `REGISTRADA`
- `ANULADA`

---

## 6.3 `detalle_compra`

Contiene los productos de una compra.

Guarda:

- producto;
- unidad usada para registrar la compra;
- cantidad;
- cantidad base;
- costo unitario;
- subtotal.

Ejemplo:

```text
Producto: Huevos
Unidad usada: KG
Cantidad: 6.5
Cantidad base: 6.5 KG
```

Una compra confirmada genera movimientos positivos de inventario.

---

# 7. Lotes y vencimientos

## 7.1 `lote`

Un producto puede tener múltiples lotes.

Relación:

```text
PRODUCTO 1:N LOTE
DETALLE_COMPRA 1:N LOTE
```

Cada lote registra:

- producto;
- detalle de compra que lo originó;
- código de lote opcional;
- fecha de ingreso;
- fecha de vencimiento opcional.

**No guardar `stock_actual` en `lote`.**

El stock del lote se calcula a partir de sus movimientos.

### Productos sin vencimiento

También pueden tener lote con:

```text
fecha_vencimiento = null
```

Esto permite mantener trazabilidad del origen del stock.

---

# 8. FEFO

El sistema debe priorizar el stock según **FEFO**:

> First Expired, First Out.

El lote con fecha de vencimiento más cercana y stock disponible debe utilizarse primero.

Ejemplo:

```text
Yogurt

Lote A
vence 05/10
stock disponible = 2

Lote B
vence 20/10
stock disponible = 10
```

Venta:

```text
3 unidades
```

Resultado esperado:

```text
Movimiento 1:
Lote A
VENTA
-2

Movimiento 2:
Lote B
VENTA
-1
```

La asignación lote por lote se guarda mediante `movimiento_inventario`.

No se necesita una tabla `detalle_venta_lote`.

---

# 9. Ventas

## 9.1 `venta`

Representa la cabecera de una venta.

Datos principales:

- usuario;
- fecha;
- subtotal;
- descuento;
- total;
- método de pago;
- estado;
- motivo de anulación;
- fechas de creación y actualización.

Métodos de pago iniciales:

- `EFECTIVO`
- `YAPE_PLIN`
- `TARJETA`
- `OTRO`

Estados:

- `CONFIRMADA`
- `EDITADA`
- `ANULADA`

---

## 9.2 `detalle_venta`

Contiene los productos vendidos.

Guarda:

- producto;
- unidad utilizada;
- cantidad;
- cantidad base;
- precio unitario;
- descuento;
- subtotal.

Ejemplo:

```text
Arroz a granel

cantidad = 500
unidad = G
cantidad_base = 0.500 KG
```

Una venta confirmada genera movimientos negativos de inventario.

---

# 10. Edición y anulación de ventas

Las ventas **NO se eliminan físicamente**.

No utilizar:

```sql
DELETE FROM venta
```

Una venta eliminada funcionalmente pasa a:

```text
estado = ANULADA
```

Si una venta cambia o se anula, los movimientos anteriores deben revertirse mediante movimientos `REVERSA` y luego, si corresponde, generarse los nuevos movimientos.

Esto mantiene el Kardex completo.

---

# 11. Auditoría de ventas

## 11.1 `historial_venta`

Registra cambios importantes realizados sobre una venta.

Acciones iniciales:

- `CREADA`
- `EDITADA`
- `ANULADA`
- `REACTIVADA`

Guarda:

- venta;
- usuario;
- acción;
- datos anteriores;
- datos nuevos;
- motivo;
- fecha.

`datos_anteriores` y `datos_nuevos` pueden almacenarse como JSON.

Esta tabla es de auditoría y no reemplaza a `movimiento_inventario`.

---

# 12. Consumo interno

## 12.1 `consumo_interno`

Representa productos retirados de la tienda para uso interno/familiar.

Debe estar separado de las ventas.

Relaciones:

```text
USUARIO 1:N CONSUMO_INTERNO
CONSUMO_INTERNO 1:N DETALLE_CONSUMO
```

---

## 12.2 `detalle_consumo`

Guarda:

- producto;
- unidad usada;
- cantidad;
- cantidad base.

Ejemplo:

```text
2 panes
0.250 KG de huevo
1 leche
```

Estos registros generan movimientos:

```text
tipo_movimiento = CONSUMO_INTERNO
```

y cantidades negativas.

Esto permite distinguir claramente:

```text
VENTA
vs
CONSUMO INTERNO
```

---

# 13. Mermas y ajustes

No existe una tabla específica `merma`.

Las mermas se registran como movimientos:

```text
tipo_movimiento = MERMA
```

y pueden tener un motivo como:

- `VENCIDO`
- `DAÑADO`
- `PERDIDA`
- `ROTURA`
- `OTRO`

Ejemplo:

```text
Queso fresco
MERMA
-3 UND
motivo = VENCIDO
```

El costo de la pérdida puede derivarse del lote y su detalle de compra.

---

# 14. Lista de compra

## 14.1 `lista_compra`

Representa una lista de reposición.

Puede originarse por:

- `MANUAL`
- `IA`
- `SISTEMA`

Estados:

- `PENDIENTE`
- `PARCIAL`
- `COMPLETADA`
- `CANCELADA`

Guarda además el costo estimado total.

Relación:

```text
USUARIO 1:N LISTA_COMPRA
LISTA_COMPRA 1:N DETALLE_LISTA_COMPRA
```

---

## 14.2 `detalle_lista_compra`

Representa los productos sugeridos o seleccionados para comprar.

Guarda:

- producto;
- proveedor sugerido opcional;
- unidad de medida;
- cantidad sugerida;
- cantidad comprada;
- costo estimado;
- comprado.

El proveedor sugerido puede derivarse del historial de compras.

No existe una tabla `producto_proveedor`.

---

# 15. Configuración del negocio

## 15.1 `configuracion_negocio`

Guarda la configuración general de Eben-Ezer.

Datos:

- nombre del negocio;
- RUC;
- dirección;
- teléfono;
- moneda;
- logo;
- stock mínimo predeterminado;
- días de anticipación para vencimiento;
- alerta de stock crítico;
- alerta de vencimiento;
- impresión automática de ticket.

Para este proyecto se espera normalmente **una sola configuración activa**.

El tamaño de letra de la interfaz no necesita persistirse en esta tabla; puede mantenerse como preferencia local del frontend.

---

# 16. Relación general

```text
ROL
 └── 1:N USUARIO

CATEGORIA
 └── 1:N PRODUCTO

UNIDAD_MEDIDA
 ├── 1:N PRODUCTO
 └── 1:N PROMOCION

PRODUCTO
 ├── 1:N PROMOCION
 ├── 1:N DETALLE_COMPRA
 ├── 1:N LOTE
 ├── 1:N DETALLE_VENTA
 ├── 1:N DETALLE_CONSUMO
 ├── 1:N MOVIMIENTO_INVENTARIO
 └── 1:N DETALLE_LISTA_COMPRA

PROVEEDOR
 ├── 1:N COMPRA
 └── 1:N DETALLE_LISTA_COMPRA (opcional)

COMPRA
 └── 1:N DETALLE_COMPRA

DETALLE_COMPRA
 └── 1:N LOTE

VENTA
 ├── 1:N DETALLE_VENTA
 └── 1:N HISTORIAL_VENTA

CONSUMO_INTERNO
 └── 1:N DETALLE_CONSUMO

LOTE
 └── 1:N MOVIMIENTO_INVENTARIO

USUARIO
 ├── 1:N COMPRA
 ├── 1:N VENTA
 ├── 1:N CONSUMO_INTERNO
 ├── 1:N MOVIMIENTO_INVENTARIO
 ├── 1:N HISTORIAL_VENTA
 └── 1:N LISTA_COMPRA
```

---

# 17. Flujos principales

## 17.1 Registrar compra

```text
COMPRA
  -> DETALLE_COMPRA
     -> LOTE
        -> MOVIMIENTO_INVENTARIO (+)
```

Ejemplo:

```text
Compra 10 Yogurt
Lote vence 15/10
Movimiento COMPRA +10
```

---

## 17.2 Registrar venta

```text
VENTA
  -> DETALLE_VENTA
     -> seleccionar lote(s) FEFO
        -> MOVIMIENTO_INVENTARIO (-)
```

---

## 17.3 Registrar consumo interno

```text
CONSUMO_INTERNO
  -> DETALLE_CONSUMO
     -> seleccionar lote(s)
        -> MOVIMIENTO_INVENTARIO (-)
```

---

## 17.4 Registrar merma

```text
MOVIMIENTO_INVENTARIO
tipo = MERMA
cantidad_base < 0
```

Debe asociarse al lote cuando sea posible.

---

## 17.5 Anular venta

```text
VENTA -> ANULADA

MOVIMIENTOS originales
     -> generar REVERSA (+)

HISTORIAL_VENTA
     -> ANULADA
```

No borrar registros.

---

# 18. Cálculos derivados

## Stock actual

No almacenar.

```text
SUM(movimiento_inventario.cantidad_base)
```

---

## Productos por reponer

Comparar:

```text
stock_actual <= producto.stock_minimo
```

---

## Stock crítico

Puede considerarse crítico, inicialmente, cuando:

```text
stock_actual <= stock_minimo / 2
```

La regla puede mantenerse configurable en el servicio.

---

## Productos próximos a vencer

Usar:

```text
lote.fecha_vencimiento <= hoy + configuracion_negocio.dias_aviso_vencimiento
```

y verificar que el lote todavía tenga stock mayor a cero.

---

## Ventas del período

Usar:

```text
venta + detalle_venta
```

No utilizar movimientos de inventario para calcular facturación.

---

## Consumo interno del período

Usar:

```text
consumo_interno + detalle_consumo
```

---

# 19. Inteligencia artificial

La IA **no tiene tablas propias**.

No guardar:

- conversaciones;
- mensajes;
- prompts;
- respuestas;
- memoria del chat.

La IA solo analiza el estado actual e histórico del negocio.

Arquitectura prevista:

```text
Frontend
   -> Asistente IA
      -> Backend
         -> Tools / servicios de consulta
            -> Repositories
               -> PostgreSQL
```

El LLM **NO debe recibir credenciales directas de PostgreSQL** y no debe ejecutar SQL libre.

Debe acceder mediante herramientas controladas por el backend.

Ejemplos futuros:

```text
consultarStock(producto)
consultarVentas(fechaInicio, fechaFin)
consultarConsumoInterno(fechaInicio, fechaFin)
consultarProductosPorVencer(dias)
consultarProductosMasVendidos(fechaInicio, fechaFin)
consultarMovimientos(producto, fechaInicio, fechaFin)
consultarUltimosProveedores(producto)
sugerirReposicion()
```

Para la primera versión, la IA debe ser **solo lectura**.

No debe:

- crear ventas;
- cambiar stock;
- modificar productos;
- eliminar compras;
- modificar configuraciones directamente.

---

# 20. Decisiones intencionales

No crear estas tablas salvo que el alcance cambie explícitamente:

```text
INVENTARIO
PRODUCTO_PROVEEDOR
NOTIFICACION
CONVERSACION_IA
MENSAJE_IA
CONSULTA_IA
DETALLE_VENTA_LOTE
MERMA
```

Motivos:

### `INVENTARIO`

El stock se deriva de movimientos.

### `PRODUCTO_PROVEEDOR`

La relación histórica se obtiene desde compras.

### `NOTIFICACION`

Los avisos de stock/vencimiento pueden calcularse dinámicamente.

### Tablas de IA

No se guarda conversación.

### `DETALLE_VENTA_LOTE`

`movimiento_inventario` ya relaciona detalle de venta y lote.

### `MERMA`

Es un tipo de movimiento.

---

# 21. Reglas técnicas

Cuando se implemente el modelo:

- usar tipos decimales exactos para dinero y cantidades;
- no usar `float`/`double` para importes;
- nunca eliminar físicamente ventas auditadas;
- evitar `CascadeType.REMOVE` peligroso;
- mantener relaciones `ManyToOne` lazy cuando sea razonable;
- las entidades no deben exponerse directamente como contratos API;
- requests/responses deben usar DTOs;
- las conversiones de unidades pertenecen a servicios, no a entities;
- las promociones deben validar compatibilidad entre la unidad de la promoción y la unidad base del producto;
- FEFO pertenece a servicios de inventario, no a entities;
- el cálculo de stock pertenece a consultas/repositories/services;
- las operaciones de compra/venta/consumo deben ser transaccionales.

---

# 22. Fuente de verdad

Ante dudas de implementación:

1. `movimiento_inventario` es la fuente de verdad del stock.
2. `venta` / `detalle_venta` son la fuente de verdad comercial de ventas.
3. `compra` / `detalle_compra` son la fuente de verdad comercial de ingresos.
4. `consumo_interno` / `detalle_consumo` son la fuente de verdad del uso interno.
5. `lote` representa el origen y vencimiento de la mercadería.
6. `historial_venta` conserva la auditoría de cambios de una venta.
7. La IA consume los datos; no define ni reemplaza las reglas del dominio.
