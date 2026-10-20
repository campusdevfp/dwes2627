# Examen práctico — UT6 (RA7)

!!! danger "Esta página ES el examen"
    **El examen de la UT6 es uno de estos seis enunciados, con otro dominio.** Tres de **WebSockets** y tres de **GraphQL**, y **todos** llevan su parte de **documentación OpenAPI**, porque el examen también la lleva.

    No sabes cuál de los dos transportes te va a caer, así que hay que **haber hecho al menos uno de cada bloque**. La [rúbrica](#rubrica-de-calificacion) del final es la misma con la que se corrige.

!!! success "Así es el examen"
    **Cae un WebSocket o un GraphQL, más la documentación de la API.** Nunca los dos transportes a la vez: no cabe en cuatro horas y no haría falta para demostrar el RA.

    **Se te da** un proyecto con la API REST y la persistencia **ya hechas y funcionando**. **Añades tú** el transporte que diga el enunciado y la documentación.

| Bloque | | Enunciado | La dificultad |
|---|:-:|---|---|
| **WebSockets** | WS1 | [Notificador de cambios](#ws1-notificador-de-cambios) | Sesiones concurrentes y `AFTER_COMMIT` |
| | WS2 | [Panel por categorías](#ws2-panel-por-categorias) | Sesiones **agrupadas** y leídas de la URL |
| | WS3 | [Monitor con avisos](#ws3-monitor-con-avisos) | `@Scheduled` y **dos formatos** del mismo evento |
| **GraphQL** | GQ1 | [Consultas y mutaciones](#gq1-consultas-y-mutaciones) | **`@BatchMapping`** y el N+1 |
| | GQ2 | [Filtros y campos calculados](#gq2-filtros-y-campos-calculados) | `@SchemaMapping` y `input` de filtro |
| | GQ3 | [Suscripción en vivo](#gq3-suscripcion-en-vivo) | `Sinks.Many` y la cancelación del `Flux` |

!!! danger "Antes de empezar cualquiera de los seis"
    ```bash
    grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/
    ```

    **Si eso devuelve algo, el ejercicio no se puede hacer** sin refactorizar primero. Y lo que estaría mal no es este ejercicio: sería la arquitectura de la UT4.

!!! success "Reparto del tiempo, para que quepa"
    | | Qué | Tiempo |
    |:-:|---|:-:|
    | 1 | Leer el proyecto que te dan y arrancarlo | 20 min |
    | 2 | El evento y la publicación desde el servicio *(WS)* **o** el esquema `.graphqls` *(GQL)* | 35 min |
    | 3 | El `handler` y el notificador *(WS)* **o** el controlador y los resolutores *(GQL)* | 70 min |
    | 4 | La página de prueba o el playground, y depurar | 30 min |
    | 5 | **Documentación OpenAPI y CORS** | 45 min |
    | 6 | Dos tests | 20 min |
    | | | **3 h 40** |

    Sobran veinte minutos para la rúbrica. **Si vas justo, lo que se abandona es el segundo test** — nunca la documentación, que son 2,5 puntos.

---

# Bloque 1 · WebSockets

## WS1 · Notificador de cambios

**Contexto.** La API de productos ya funciona. Un panel de administración abierto en varios navegadores tiene que enterarse **al instante** de cada producto creado, modificado o borrado, sin recargar.

### Lo que hay que construir

| # | Qué | Detalle |
|:-:|---|---|
| 1 | **`WS /ws/v1/productos`** | Recibe un mensaje por cada cambio |
| 2 | `GET /ws-test.html` | Página estática con el log de mensajes en pantalla |
| 3 | `GET /api/v1/ws/conectados` | `{"conectados": 3}` |
| 4 | **La documentación OpenAPI** de los endpoints REST que ya existen | Ver el apartado común de abajo |

### El mensaje

```json
{ "entidad": "PRODUCTO", "tipo": "CREATE",
  "datos": { "id": 7, "nombre": "Teclado", "precio": 79.90, "stock": 10 },
  "fecha": "2026-06-12T10:31:02" }
```

`tipo` es `CREATE`, `UPDATE` o `DELETE`.

### Requisitos

| # | Requisito | Se comprueba |
|:-:|---|---|
| R1 | La colección de sesiones es **concurrente** | Lectura del código |
| R2 | La sesión se quita **al cerrarse, al dar error de transporte y al fallar el envío** | Se cierra una pestaña y el log dice `total: 1` |
| R3 | **`try` por sesión** dentro del bucle: un cliente roto no deja sin mensaje a los demás | Dos pestañas, se cierra una y la otra sigue recibiendo |
| R4 | **`@TransactionalEventListener(AFTER_COMMIT)`**: un POST que devuelve 409 o 400 **no notifica** | POST con nombre repetido → no llega nada |
| R5 | Se manda el **DTO**, no la entidad | Lectura del notificador |
| R6 | `setAllowedOrigins` **por perfiles, sin comodín** | `grep` y los dos `application-*.properties` |
| R7 | **El servicio solo cambia para publicar el evento** | `git diff --stat` de los servicios |

!!! tip "El esqueleto del `handler`"
    ```java
    @Component
    public class ProductosWebSocketHandler extends TextWebSocketHandler {

        private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();

        @Override public void afterConnectionEstablished(WebSocketSession s) { sesiones.add(s); }
        @Override public void afterConnectionClosed(WebSocketSession s, CloseStatus st) { sesiones.remove(s); }
        @Override public void handleTransportError(WebSocketSession s, Throwable e) { sesiones.remove(s); }

        public void enviarATodos(String mensaje) {
            for (var s : sesiones) {
                try {
                    if (s.isOpen()) s.sendMessage(new TextMessage(mensaje));
                    else sesiones.remove(s);
                } catch (IOException e) { sesiones.remove(s); }
            }
        }

        public int conectados() { return sesiones.size(); }
    }
    ```

    Y en el servicio, **dos líneas**:

    ```java
    private final ApplicationEventPublisher eventos;
    eventos.publishEvent(new ProductoCambiadoEvent(Tipo.CREATE, respuesta));
    ```

---

## WS2 · Panel por categorías

**Contexto.** Lo mismo que WS1, pero cada pantalla del panel mira **una categoría**. Un comercial que lleva informática no quiere enterarse de los cambios en papelería.

### Lo que hay que construir

| # | Qué | Detalle |
|:-:|---|---|
| 1 | **`WS /ws/v1/productos`** | Todos los cambios |
| 2 | **`WS /ws/v1/productos/categoria/{nombre}`** | **Solo** los de esa categoría |
| 3 | `GET /api/v1/ws/conectados` | `{"total": 5, "porCategoria": {"INFORMATICA": 3, "PAPELERIA": 2}}` |
| 4 | **La documentación OpenAPI** | Ver el apartado común |

### Requisitos

| # | Requisito |
|:-:|---|
| R1 | R1–R7 de WS1 |
| R2 | La categoría se lee **de la URL** y se guarda en la sesión |
| R3 | Una sesión de `INFORMATICA` **no recibe** mensajes de `PAPELERIA` |
| R4 | El mapa de sesiones es **concurrente en los dos niveles** |
| R5 | Al cerrarse la **última** sesión de una categoría, **se borra la entrada del mapa** |
| R6 | La comparación de la categoría **no distingue mayúsculas** |

!!! tip "Las sesiones agrupadas"
    ```java
    private final Map<String, Set<WebSocketSession>> porCategoria = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession sesion) {
        var cat = categoriaDeLaUri(sesion.getUri()).toUpperCase();
        sesion.getAttributes().put("categoria", cat);
        porCategoria.computeIfAbsent(cat, k -> new CopyOnWriteArraySet<>()).add(sesion);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sesion, CloseStatus st) {
        var cat = (String) sesion.getAttributes().get("categoria");
        var grupo = porCategoria.get(cat);
        if (grupo != null) {
            grupo.remove(sesion);
            if (grupo.isEmpty()) porCategoria.remove(cat);   // ← R5
        }
    }

    private String categoriaDeLaUri(URI uri) {
        var partes = uri.getPath().split("/");
        return partes[partes.length - 1];
    }
    ```

    `ConcurrentHashMap` por fuera y `CopyOnWriteArraySet` por dentro. Y el `computeIfAbsent` es el de la [UT2](../ut2/ejercicios.md).

---

## WS3 · Monitor con avisos

**Contexto.** Un panel de almacén necesita ver **en vivo** qué productos se están quedando sin stock, y que el aviso se refresque solo aunque nadie toque nada.

### Lo que hay que construir

| # | Qué | Detalle |
|:-:|---|---|
| 1 | **`WS /ws/v1/almacen`** | Panel completo: productos bajo mínimos y resumen |
| 2 | **`WS /ws/v1/productos/{id}`** | **Solo** ese producto, con otro formato |
| 3 | `@Scheduled` que **reenvía el panel cada 30 segundos** | Y no hace nada si no hay nadie conectado |
| 4 | **La documentación OpenAPI** | Ver el apartado común |

### Los dos formatos del mismo evento

=== "Canal de almacén"

    ```json
    { "tipo": "PANEL",
      "bajoMinimos": [
        { "sku": "SKU-002", "nombre": "Ratón", "stock": 3, "minimo": 5, "critico": false },
        { "sku": "SKU-003", "nombre": "Monitor", "stock": 0, "minimo": 2, "critico": true }
      ],
      "resumen": { "productos": 24, "bajoMinimos": 2, "sinStock": 1 } }
    ```

=== "Canal de un producto"

    ```json
    { "tipo": "STOCK", "sku": "SKU-002", "stock": 3, "aviso": "Quedan pocas unidades" }
    ```

    **Sin el resumen y sin los demás productos.**

### Requisitos

| # | Requisito |
|:-:|---|
| R1 | R1–R7 de WS1, en los dos canales |
| R2 | Un cambio de stock notifica **a los dos canales**, con **contenido distinto** |
| R3 | El canal de un producto **solo recibe su producto** |
| R4 | Al conectarse al canal de almacén se recibe **el panel completo** |
| R5 | `critico` es `true` cuando `stock == 0`; está `bajoMinimos` cuando `stock < minimo` |
| R6 | **`@Scheduled` no hace nada si `conectados() == 0`** |
| R7 | `@EnableScheduling` en la clase principal |

!!! tip "Un evento, dos mensajes"
    ```java
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarStock(ProductoCambiadoEvent e) {
        almacenHandler.enviarATodos(json(panelCompleto()));            // mensaje A
        productoHandler.enviarAlProducto(e.id(), json(estadoDe(e)));   // mensaje B
    }
    ```

    ```java
    @Scheduled(fixedDelay = 30_000)
    public void refrescarPanel() {
        if (almacenHandler.conectados() == 0) return;     // ← R6
        almacenHandler.enviarATodos(json(panelCompleto()));
    }
    ```

!!! warning "R6 parece trivial y es un criterio de la rúbrica"
    Sin ese `return`, cada 30 segundos consultas la base de datos, montas el JSON y lo tiras a la basura. **Toda la noche, con el almacén cerrado.**

---

# Bloque 2 · GraphQL

## GQ1 · Consultas y mutaciones

**Contexto.** La misma API de productos y categorías, accesible también por GraphQL para que una aplicación móvil pida solo los campos que pinta.

### Lo que hay que construir

| # | Qué |
|:-:|---|
| 1 | El **esquema** `schema.graphqls` con `Producto`, `Categoria`, un `input` y los tipos de página |
| 2 | **Queries**: listado paginado, uno por id, uno por `sku`, y las categorías |
| 3 | **Mutaciones**: crear, actualizar y borrar un producto |
| 4 | **`@BatchMapping`** para el campo `categoria` de `Producto` |
| 5 | Las excepciones del dominio traducidas a **`ErrorType`** |
| 6 | Límites de profundidad y complejidad, e introspección **apagada en `prod`** |
| 7 | **La documentación OpenAPI** de la API REST | Ver el apartado común |

### El esquema de referencia

```graphql title="src/main/resources/graphql/schema.graphqls"
type Producto {
    id: ID!
    sku: String!
    nombre: String!
    precio: Float!
    stock: Int!
    categoria: Categoria!          # ← @BatchMapping
}

type Categoria {
    id: ID!
    nombre: String!
    productos: [Producto!]!
}

type PaginaProductos {
    contenido: [Producto!]!
    total: Int!
    pagina: Int!
    totalPaginas: Int!
}

input ProductoInput {
    nombre: String!
    precio: Float!
    stock: Int!
    categoria: String!
}

type Query {
    productos(pagina: Int = 0, tamano: Int = 20): PaginaProductos!
    productoById(id: ID!): Producto
    productoBySku(sku: String!): Producto
    categorias: [Categoria!]!
}

type Mutation {
    crearProducto(input: ProductoInput!): Producto!
    actualizarProducto(id: ID!, input: ProductoInput!): Producto!
    borrarProducto(id: ID!): Boolean!
}
```

### Requisitos

| # | Requisito | Se comprueba |
|:-:|---|---|
| R1 | Los `!` del esquema son correctos: `[Producto!]!` en los listados | Lectura del `.graphqls` |
| R2 | **`@BatchMapping`** en `Producto.categoria` | **Contando las consultas del log** |
| R3 | Las mutaciones llevan **`@Valid`** | Una mutación inválida devuelve `errors` |
| R4 | `productoById(id: 9999)` devuelve `errors` con **`classification: NOT_FOUND`** | GraphiQL |
| R5 | Una mutación con `sku` repetido devuelve `errors`, **no un 500** | GraphiQL |
| R6 | Límites de profundidad (≤ 10) y complejidad configurados | Una consulta anidada a 15 niveles se rechaza |
| R7 | `graphiql` e `introspection` **apagados en `prod`** | `SPRING_PROFILES_ACTIVE=prod` |
| R8 | **El servicio no cambia** | `git diff --stat` |

!!! danger "R2 es el ítem que más puntúa, y se corrige contando"
    ```graphql
    { productos(tamano: 20) { contenido { nombre categoria { nombre } } } }
    ```

    | | Consultas SQL |
    |---|:-:|
    | Sin `@BatchMapping` | **1 + 20 = 21** |
    | Con `@BatchMapping` | **2** |

    ```java
    @BatchMapping(typeName = "Producto")
    public Map<ProductoResponse, CategoriaResponse> categoria(List<ProductoResponse> productos) {
        var nombres = productos.stream().map(ProductoResponse::categoria).distinct().toList();
        var porNombre = categoriasService.findByNombres(nombres).stream()
                .collect(Collectors.toMap(CategoriaResponse::nombre, c -> c));
        return productos.stream().collect(Collectors.toMap(p -> p,
                                          p -> porNombre.get(p.categoria())));
    }
    ```

    **El `distinct()` importa:** si 15 de los 20 productos son de la misma categoría, sin él pides 20 nombres para 3 categorías.

    Y **no se arregla con un `JOIN FETCH` fijo** como en la UT5: aquí la consulta depende de qué campos pida el cliente.

---

## GQ2 · Filtros y campos calculados

**Contexto.** Lo mismo que GQ1, pero el cliente necesita **filtrar** y recibir **datos derivados** que no están en ninguna columna.

### Lo que hay que construir

| # | Qué |
|:-:|---|
| 1 | El esquema, con un **`input` de filtro** y un `enum` propio |
| 2 | **Queries** con el filtro aplicado sobre la `Specification` que ya tiene el servicio |
| 3 | **Tres campos calculados** con `@SchemaMapping` que no existan en la entidad |
| 4 | **`@BatchMapping`** para la relación |
| 5 | Excepciones traducidas a `ErrorType`, límites, `prod` apagado |
| 6 | **La documentación OpenAPI** | Ver el apartado común |

```graphql
type Producto {
    id: ID!
    sku: String!
    nombre: String!
    precio: Float!
    stock: Int!
    categoria: Categoria!            # @BatchMapping
    valorInventario: Float!          # @SchemaMapping: precio * stock
    disponible: Boolean!             # @SchemaMapping: stock > 0
    nivelStock: NivelStock!          # @SchemaMapping: SIN_STOCK | BAJO | NORMAL
}

enum NivelStock { SIN_STOCK, BAJO, NORMAL }

input ProductoFiltro {
    nombre: String
    categoria: String
    precioMax: Float
    soloDisponibles: Boolean
}

type Query {
    productos(filtro: ProductoFiltro, pagina: Int = 0, tamano: Int = 20): PaginaProductos!
    productoById(id: ID!): Producto
}
```

### Requisitos

| # | Requisito |
|:-:|---|
| R1 | R1–R8 de GQ1 |
| R2 | Los **cuatro filtros** funcionan por separado y combinados |
| R3 | `filtro` **omitido** devuelve todo, sin fallar |
| R4 | Los **tres campos calculados** se resuelven con `@SchemaMapping`, no guardados en la entidad |
| R5 | Pedir **solo** los campos calculados **no** dispara la consulta de la categoría |

!!! tip "Los campos calculados"
    ```java
    @SchemaMapping(typeName = "Producto", field = "valorInventario")
    public Double valorInventario(ProductoResponse p) {
        return p.precio().multiply(BigDecimal.valueOf(p.stock())).doubleValue();
    }

    @SchemaMapping(typeName = "Producto", field = "nivelStock")
    public NivelStock nivelStock(ProductoResponse p) {
        if (p.stock() == 0) return NivelStock.SIN_STOCK;
        return p.stock() < 5 ? NivelStock.BAJO : NivelStock.NORMAL;
    }
    ```

    **Esto es lo que GraphQL hace mejor que REST:** `nivelStock` es una regla expuesta como campo. En REST tendrías que devolverlo siempre aunque nadie lo use, o inventar un endpoint.

!!! warning "R5 es el requisito fino"
    ```graphql
    { productos { contenido { nombre valorInventario } } }     # 1 consulta
    { productos { contenido { nombre categoria { nombre } } } } # 2 consultas
    ```

    Un resolutor solo se ejecuta **si el cliente pide ese campo**. Si tu primera consulta hace 2, es que estás cargando la categoría sin que nadie la haya pedido.

---

## GQ3 · Suscripción en vivo

**Contexto.** Lo mismo que GQ1, y además el cliente quiere **enterarse de los cambios** sin preguntar, por el mismo endpoint de GraphQL.

### Lo que hay que construir

| # | Qué |
|:-:|---|
| 1 | Las queries y mutaciones básicas (menos exhaustivas que en GQ1) |
| 2 | **`@BatchMapping`** para la relación |
| 3 | **`subscription productoCambiado(tipo: TipoCambio)`** con **filtro por argumento** |
| 4 | El **mismo `ApplicationEvent`** alimenta la suscripción |
| 5 | **Cerrar la pestaña cancela la suscripción** y se demuestra en el log |
| 6 | **La documentación OpenAPI** | Ver el apartado común |

```graphql
type Subscription {
    productoCambiado(tipo: TipoCambio): Notificacion!
}

enum TipoCambio { CREATE, UPDATE, DELETE }

type Notificacion {
    tipo: TipoCambio!
    producto: Producto!
    instante: String!
}
```

### Requisitos

| # | Requisito |
|:-:|---|
| R1 | R1–R8 de GQ1 |
| R2 | `productoCambiado(tipo: CREATE)` recibe **solo** las creaciones |
| R3 | `productoCambiado` sin argumento recibe **todos** los cambios |
| R4 | La notificación sale **después de confirmar** la transacción |
| R5 | **`doOnCancel` deja rastro en el log** al cerrar el cliente |
| R6 | Se usa `Sinks.many().multicast()`, no `unicast()` |

!!! danger "R5: un `Flux` que nadie cancela es una fuga de memoria"
    ```java
    private final Sinks.Many<Notificacion> emisor =
            Sinks.many().multicast().onBackpressureBuffer();

    @SubscriptionMapping
    public Flux<Notificacion> productoCambiado(@Argument Optional<TipoCambio> tipo) {
        return emisor.asFlux()
                .filter(n -> tipo.map(t -> t == n.tipo()).orElse(true))
                .doOnCancel(() -> log.info("Suscripción cancelada"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiar(ProductoCambiadoEvent e) {
        emisor.tryEmitNext(new Notificacion(e.tipo(), e.producto(),
                                            LocalDateTime.now().toString()));
    }
    ```

    **Cómo se demuestra:** abre la suscripción en GraphiQL, cierra la pestaña y comprueba que sale `Suscripción cancelada`. Si no sale, el ítem es 0.

!!! info "`multicast()` y no `unicast()`"
    | | Qué hace |
    |---|---|
    | `unicast()` | **Un solo** suscriptor; el segundo recibe un error |
    | `multicast()` | Varios, cada uno recibe lo que se emite desde que se suscribió |

    Y `tryEmitNext` en vez de `emitNext`, para no tener que decidir una estrategia de error en cada emisión.

---

# La parte común: documentación OpenAPI y CORS

**Está en los seis enunciados y en el examen**, y vale **2,5 puntos**.

| # | Qué | Detalle |
|:-:|---|---|
| 1 | La dependencia `springdoc-openapi-starter-webmvc-ui` | — |
| 2 | **Metadatos**: título, versión, descripción, contacto, licencia y servidores | En una `@Configuration` |
| 3 | `@Tag` en cada controlador, con descripción | — |
| 4 | **`@Operation`** con `summary` y `description` en **todos** los endpoints REST | — |
| 5 | **`@ApiResponses` con TODOS los códigos**, incluidos los **409 de negocio y su causa concreta** | — |
| 6 | `@Parameter` con `description` y `example` en todos los parámetros | — |
| 7 | `@Schema` con **ejemplos realistas** en todos los DTOs | No `"string"` ni `0` |
| 8 | **CORS por perfiles**, sin comodín, con **`exposedHeaders("Location")`** | — |
| 9 | Swagger y `/v3/api-docs` **apagados en `prod`** | Comprobado con `curl` |
| 10 | `README.md` con **tres `curl` que funcionen al copiar y pegar** | — |

!!! danger "El punto 5 es el que decide, y es el único que la herramienta no puede deducir"
    `springdoc` deduce las rutas, los tipos y la obligatoriedad. **No puede deducir** que un POST devuelve 409 si el `sku` está repetido.

    ```java
    @ApiResponse(responseCode = "409",
        description = """
            Conflicto. Puede ser por:
            - Ya existe un producto con ese SKU
            - No hay stock suficiente para la venta solicitada
            - Se intenta borrar un producto con stock

            El cuerpo indica cuál de los tres ha sido.
            """)
    ```

    **Tres causas distintas para el mismo código.** Si tu documentación dice solo «Conflicto», el cliente no puede hacer nada con esa información.

!!! tip "CORS, en una clase"
    ```java
    @Configuration
    public class CorsConfig implements WebMvcConfigurer {

        private final String[] origenes;

        public CorsConfig(@Value("${cors.origenes-permitidos}") String[] origenes) {
            this.origenes = origenes;
        }

        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins(origenes)
                    .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                    .allowedHeaders("*")
                    .exposedHeaders("Location")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }
    ```

    ```properties
    # dev
    cors.origenes-permitidos=http://localhost:5173
    # prod
    cors.origenes-permitidos=https://miapp.es
    ```

    Y si el enunciado es de WebSockets, **hacen falta las dos configuraciones**: el `addCorsMappings` para `/api/**` y el `setAllowedOrigins` del `registerWebSocketHandlers` para `/ws/**`. Son mecanismos distintos.

---

## ¿Cabe en cuatro horas?

**Sí, porque la API REST y la persistencia vienen hechas.** Lo que escribes son entre **200 y 300 líneas** en 5 o 6 ficheros, más las anotaciones de documentación.

=== "Si cae WebSocket"

    | Ficheros que escribes | Líneas aprox. |
    |---|:-:|
    | El `record` del evento | 10 |
    | El `handler` | 55 |
    | El notificador con `AFTER_COMMIT` | 35 |
    | La `@Configuration` del WebSocket | 25 |
    | `publishEvent` en el servicio | 4 |
    | La página HTML de prueba | 30 |
    | `@Configuration` de OpenAPI + CORS | 60 |
    | Anotaciones de documentación en el controlador y los DTOs | 70 |
    | Dos tests | 40 |

=== "Si cae GraphQL"

    | Ficheros que escribes | Líneas aprox. |
    |---|:-:|
    | `schema.graphqls` | 55 |
    | El `@Controller` con los resolutores | 80 |
    | El `@BatchMapping` | 15 |
    | El resolutor de excepciones | 25 |
    | La `@Configuration` de los límites | 15 |
    | `@Configuration` de OpenAPI + CORS | 60 |
    | Anotaciones de documentación | 70 |
    | Dos tests con `GraphQlTester` | 40 |

!!! success "Lo que se recortó respecto a versiones anteriores de estos enunciados"
    | | Antes | Ahora |
    |---|:-:|:-:|
    | Enunciados | 9, en tres bloques | **6**, en dos |
    | Transportes por enunciado | 1, y la documentación aparte | **1 + la documentación, siempre** |
    | WS1 | Subasta bidireccional con pujas y `@Scheduled` | **Notificador de cambios**, solo bajada |
    | GQ3 | Informe comparativo medido con tres pantallas | **Una suscripción con filtro** |

    **Lo que se queda, porque es lo que evalúa el RA7:** la reutilización del servicio, el `AFTER_COMMIT`, el `@BatchMapping` y los 409 documentados.

    Los enunciados grandes que se han quitado (la subasta bidireccional y el informe REST frente a GraphQL) siguen siendo buenos **proyectos de ampliación**, pero no exámenes.

---

## Rúbrica de calificación

**Una sola rúbrica para los seis enunciados y para el examen**, con el bloque 2 desglosado según el transporte que toque. Sobre 10 puntos.

### Bloque 1 · Reutilización — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 1.1 | `git diff --stat src/main/java/**/services/` → **solo las líneas del `publishEvent`** *(o vacío si el enunciado es de GraphQL)* | **1,0** |
| 1.2 | `git diff --stat src/main/java/**/repositories/` → **vacío** | 0,5 |
| 1.3 | `grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/` → **vacío** | 0,5 |

!!! danger "Es el 20 % de la nota y se corrige antes de abrir ningún fichero"
    El RA7 dice literalmente *«servicios web **reutilizables**»*. **Este bloque no puede quedar en 0.**

### Bloque 2 · El transporte — 4,0 puntos

=== "Si el enunciado es de WebSockets"

    | | Se comprueba con | Puntos |
    |---|---|:-:|
    | 2.1 | Colección de sesiones **concurrente**. Un `ArrayList` o `HashSet` → **0 en este ítem** | 0,6 |
    | 2.2 | La sesión se quita **en los tres sitios** y al fallar el envío | 0,6 |
    | 2.3 | **`try` por sesión** en el bucle: se cierra una pestaña y las demás siguen recibiendo | 0,6 |
    | 2.4 | **`@TransactionalEventListener(AFTER_COMMIT)`**: un POST que da 409 **no notifica** | **0,8** |
    | 2.5 | Se manda el **DTO**, no la entidad | 0,4 |
    | 2.6 | `setAllowedOrigins` **por perfiles, sin comodín** | 0,4 |
    | 2.7 | **Los requisitos propios del enunciado** (R2–R6 según cuál sea) | 0,6 |

=== "Si el enunciado es de GraphQL"

    | | Se comprueba con | Puntos |
    |---|---|:-:|
    | 2.1 | **`@BatchMapping`, MEDIDO** con `show-sql=true`. 2–3 consultas → total; 1+N → **0** | **1,4** |
    | 2.2 | Esquema correcto: `!` bien puestos, `input` para mutaciones, `enum` propios | 0,7 |
    | 2.3 | Mutaciones con **`@Valid`**: una inválida devuelve `errors` | 0,5 |
    | 2.4 | Excepciones del dominio traducidas a **`ErrorType`** con mensaje útil | 0,6 |
    | 2.5 | **Límites** de profundidad y complejidad configurados | 0,3 |
    | 2.6 | `graphiql` e `introspection` **apagados en `prod`** | 0,4 |
    | 2.7 | **Los requisitos propios del enunciado** (los campos calculados de GQ2, el `doOnCancel` de GQ3) | 0,6 |

### Bloque 3 · Documentación OpenAPI y CORS — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 3.1 | **Todos** los endpoints REST con `@Operation` (`summary` **y** `description`). *Proporcional: documentados / total* | **0,8** |
| 3.2 | **Todos** los códigos en `@ApiResponses`, incluidos los **409 de negocio con su causa concreta** | **0,8** |
| 3.3 | DTOs con `@Schema` y **ejemplos realistas** | 0,3 |
| 3.4 | Metadatos de la API y `@Tag` por controlador | 0,2 |
| 3.5 | **CORS por perfiles**, sin comodín, con `exposedHeaders`. Un origen no permitido → **403** | 0,3 |
| 3.6 | `README.md` con **tres `curl` que funcionan al copiar** | 0,1 |

### Bloque 4 · Configuración por perfiles — 0,8 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 4.1 | Swagger y `/v3/api-docs` devuelven **404** con el perfil `prod` | 0,4 |
| 4.2 | Lo propio del transporte apagado o restringido en `prod` (introspección, orígenes) | 0,4 |

### Bloque 5 · Tests — 0,7 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 5.1 | `./mvnw clean test` acaba en **BUILD SUCCESS**. Si no compila o hay tests comentados → **0 en el bloque** | 0,2 |
| 5.2 | **Un test del transporte**: que la notificación llega *(WS)* o una consulta con `GraphQlTester` *(GQL)* | 0,3 |
| 5.3 | **Un test de error**: que un fallo **no** notifica *(WS)* o que un id inexistente devuelve `NOT_FOUND` *(GQL)* | 0,2 |

!!! danger "Penalizaciones — se restan del total"
    | | |
    |---|:-:|
    | Reescribir lógica de negocio en una clase del transporte | **−2,0** |
    | `ArrayList` o `HashSet` para las sesiones *(WS)* | **−1,0** |
    | Notificar **dentro** de la transacción, sin `AFTER_COMMIT` *(WS)* | **−1,0** |
    | Mandar la **entidad** por el WebSocket o por GraphQL | **−1,0** |
    | `setAllowedOrigins("*")` o sin configurar | **−1,0** |
    | `introspection` de GraphQL activada en `prod` | **−0,5** |
    | Swagger accesible con el perfil `prod` | **−0,5** |
    | Un `Flux` de suscripción sin `doOnCancel` *(GQ3)* | **−0,5** |
    | `@Scheduled` sin comprobar si hay alguien conectado *(WS3)* | **−0,3** |

!!! success "Nota mínima"
    Se aprueba con **5,0**, y **el bloque 1 no puede quedar en 0**.

    | Bloque | Peso |
    |---|:-:|
    | 1 · Reutilización | 20 % |
    | 2 · El transporte | **40 %** |
    | 3 · Documentación y CORS | **25 %** |
    | 4 · Configuración | 8 % |
    | 5 · Tests | 7 % |

!!! info "Orden de corrección"
    1. `./mvnw clean test` → bloque 5.1.
    2. **Los tres comandos del bloque 1.** Dos minutos y un 20 % de la nota.
    3. `java -jar` con `show-sql=true`:
        - **WebSocket:** dos pestañas de la página de prueba, un POST correcto y un POST que dé 409. Se mira qué llega y qué no.
        - **GraphQL:** una consulta con la relación en GraphiQL y **contar las consultas del log**.
    4. Recorrer Swagger endpoint por endpoint, comparando con las reglas de negocio del proyecto → bloque 3.1 y 3.2.
    5. Los `curl` de CORS, con un origen permitido y con uno que no.
    6. `SPRING_PROFILES_ACTIVE=prod` y comprobar que Swagger y lo del transporte están apagados.
    7. Los `curl` del `README`, copiados y pegados tal cual.

    Se entrega comprimido **sin `target/` ni `datos/`**, con un `README.md` que diga cómo arrancarlo, la URL del transporte y los tres `curl`.
