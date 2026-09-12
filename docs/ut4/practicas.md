# Reto de la unidad — UT4

> **Reto `ut4-capas-base`**, en GitHub Classroom. Proyecto con las capas esbozadas, los métodos vacíos y **los tests escritos**. Tu trabajo es ponerlos en verde.
>
> En paralelo, en clase se construye la *TiendaAPI* en directo: es el [proyecto del profesor](../comprobar-tu-trabajo/#6-el-proyecto-del-profesor), y lo tienes publicado con un *tag* por sesión para consultarlo cuando te atasques.

Debajo, las **fases sugeridas** para resolver el reto.

## El proyecto: TiendaAPI

Una API REST para gestionar el catálogo de una tienda, con arquitectura por capas, DTO, validación, errores controlados y tests.

``` { .java .sinajuste }
tienda/
├── modelo/Producto.java
├── repositorio/ProductoRepositorio.java            (interfaz)
│              /ProductoRepositorioMemoria.java     @Repository
├── servicio/ProductoServicio.java                  @Service
├── controlador/ProductoControlador.java            @RestController
├── dto/CrearProductoDto.java · ProductoRespuestaDto.java
├── excepcion/ProductoNoEncontradoException.java · StockInsuficienteException.java
│           /ManejadorGlobalErrores.java            @RestControllerAdvice
└── test/ProductoServicioTest.java · ProductoControladorTest.java
```

## Hoja de ruta

| Fase | Sesiones | Qué construyes | Página |
|---|---|---|---|
| **F1** | S1–S3 | Proyecto creado y primer endpoint funcionando | [1](../01-spring-boot/) |
| **F2** | S4–S6 | Las tres capas conectadas por inyección | [2](../02-inyeccion-dependencias/) |
| **F3** | S7–S9 | CRUD completo con códigos HTTP correctos | [3](../03-capas-en-spring/) |
| **F4** | S10–S11 | DTO de entrada/salida y validación | [4](../04-dto-y-validacion/) |
| **F5** | S12–S13 | Errores globales y configuración externa | [5](../05-errores-y-configuracion/) |
| **F6** | S14–S15 | Tests de servicio y de controlador | [6](../06-testing-en-spring/) |

---

## F1 — Arranque (S1–S3)

**Objetivo:** proyecto creado, corriendo y con un endpoint que responde.

1. Genera el proyecto en Initializr (Web, Validation, DevTools).
2. Crea `SaludoControlador` con `GET /saludo`.
3. Comprueba con `curl -i` el código y el Content-Type.
4. Añade `GET /producto` que devuelva un record y verifica que llega `JSON`.

??? success "Comprobación"

    Debes ver `200 OK` con text/plain en el saludo y application/json al devolver el record. Si da 404, revisa que el controlador cuelgue del paquete de la clase principal.


## F2 — Las tres capas (S4–S6)

**Objetivo:** modelo, repositorio (interfaz + memoria), servicio y controlador conectados **sin un solo `new`** por tu parte.

??? success "Claves de la solución"

    El código completo está en la página 2. Lo que se evalúa:
    - El servicio recibe `ProductoRepositorio` (la **interfaz**) por constructor.
    - El repositorio va anotado con `@Repository`, el servicio con `@Service`.
    - Ningún `new ProductoRepositorioMemoria()` escrito por ti: lo hace Spring.

    **Experimento obligatorio:** crea una segunda implementación del repositorio y arranca → error de bean duplicado → resuélvelo con `@Primary`. Entender ese error te ahorrará horas.


## F3 — CRUD completo (S7–S9)

**Objetivo:** los cinco endpoints con sus códigos correctos y la lógica en el servicio.

| Método | Ruta | Código correcto |
|---|---|---|
| GET | `/api/productos` | 200 |
| GET | `/api/productos/{id}` | 200 · 404 si no existe |
| POST | `/api/productos` | **201** |
| PATCH | `/api/productos/{id}/stock?unidades=` | 200 · 409 si no hay stock |
| DELETE | `/api/productos/{id}` | **204** |

??? success "Comprobación con curl"

    ```bash
    curl -s http://localhost:8080/api/productos
    curl -i -X POST http://localhost:8080/api/productos -H "Content-Type: application/json" \
      -d '{"nombre":"Candado","categoria":"seguridad","precio":25.0,"stock":15}'
    curl -i -X PATCH "http://localhost:8080/api/productos/1/stock?unidades=-3"
    curl -i -X DELETE http://localhost:8080/api/productos/2
    ```

    Comprueba **los códigos**, no solo el cuerpo. Un POST que devuelve 200 en vez de 201 pierde puntos en el examen.


## F4 — DTO y validación (S10–S11)

**Objetivo:** que el cliente no vea el modelo interno y que los datos inválidos ni lleguen a tu código.

??? success "Claves"

    - `CrearProductoDto` con `@NotBlank`, `@Positive`, `@Min(0)`; activado con `@Valid`.
    - `ProductoRespuestaDto` **sin** el campo `stock` (o el que decidas ocultar).
    - Un `@Component` mapeador con `aDto()` y `aModelo()`.
    - El controlador ya **solo** habla de DTO; el servicio, de modelo.

    Prueba con un nombre vacío y un precio negativo: deben dar **400** sin que se ejecute tu servicio.


## F5 — Errores y configuración (S12–S13)

**Objetivo:** que cada fallo devuelva su código y un cuerpo útil; y que el IVA no esté escrito en el código.

??? success "Claves"

    - `ProductoNoEncontradoException` → **404**; `StockInsuficienteException` → **409**.
    - `@RestControllerAdvice` con `ProblemDetail`, incluido el manejador de `MethodArgumentNotValidException` que devuelve el mapa de errores por campo.
    - Un manejador genérico que **loguea** y responde 500 sin traza.
    - `tienda.iva` en `application.yml` + `@ConfigurationProperties`.

    Verifica: `curl -i /api/productos/999` debe dar un 404 con JSON explicativo, **no** una traza.


## F6 — Tests (S14–S15)

**Objetivo:** al menos tres tests que aporten valor.

??? success "Los tres tests mínimos"

    1. **Servicio, caso feliz:** `obtener(1)` devuelve el producto (con `when(...)`).
    2. **Servicio, caso error:** `obtener(999)` lanza `ProductoNoEncontradoException`; y en el de stock, `verify(repo, never()).guardar(any())`.
    3. **Controlador con `@WebMvcTest`:** `GET /api/productos/999` → 404.

    `mvn test` debe terminar en verde. Un test que no compila arrastra el criterio 6 a 0 en el examen.


## F7 — La persistencia se hace real (S16–S18)

**Objetivo:** que los datos sobrevivan al reinicio y que el resto del proyecto no se entere.

??? success "Claves y comprobación"

    1. Dependencias de `spring-boot-starter-data-jpa` y `h2`, con `ddl-auto: update` y `show-sql: true`.
    2. `Producto` pasa de `record` a `@Entity`, con `Long id` y las restricciones en `@Column`.
    3. `ProductoRepositorio extends JpaRepository<Producto, Long>` y **se borra la implementación en memoria**.
    4. El servicio adapta los nombres de método. **Nada más se toca.**

    ```bash
    curl -X POST localhost:8080/api/v1/productos -H "Content-Type: application/json" \
      -d '{"nombre":"Casco","categoria":"seguridad","precio":49.99,"stock":10}'
    # Ctrl+C, volver a arrancar
    curl -s localhost:8080/api/v1/productos | jq length     # sigue ahí
    ```

    **La comprobación que importa:** `mvn test` debe seguir en verde sin tocar los tests del controlador. Si has tenido que reescribirlos, el acceso a datos estaba filtrado hacia arriba.

    Guarda el repositorio en memoria como **doble de test**: es su papel en cualquier proyecto profesional.


---

## Entrega y autoevaluación

El proyecto de aula **no puntúa** (la nota es 100 % el examen), pero es su preparación directa. Cuando lo tengas, pásalo por el [comprobar tu trabajo con los tests](../comprobar-tu-trabajo/): te dirá si tus capas aguantarían la rúbrica.

Y repasa el [autochequeo de la página de examen](../examen/): si puedes hacer las ocho cosas de esa lista sin mirar, vas preparado.
