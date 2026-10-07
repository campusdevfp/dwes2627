# Batería de ejercicios — UT6

**24 ejercicios con solución**, en el orden de los temas y de menos a más dentro de cada bloque.

| | |
|:-:|---|
| ● | Cinco minutos, con el tema delante |
| ●● | Hay que juntar dos ideas |
| ●●● | Se escribe, se ejecuta y se mide |

!!! tip "Dos ventanas abiertas todo el rato"
    Una con `show-sql=true` para contar consultas, y otra con la página de prueba del WebSocket. La mitad de los ejercicios de esta unidad se contestan **mirando** lo que pasa, no razonando.

---

# Bloque 1 · WebSockets

> Tema [1. WebSockets](01-websockets.md)

## E1 ● — WebSocket, SSE o *polling*

(a) un panel de stock · (b) un chat · (c) el progreso de un informe de 2 minutos · (d) el precio de unas acciones

??? success "Solución"

    | | Qué usar | Por qué |
    |---|---|---|
    | (a) Panel de stock | WebSocket o **SSE** | Muchos cambios, solo bajada |
    | (b) Chat | **WebSocket** | Hace falta bidireccional |
    | (c) Progreso | **SSE** | Solo bajada, y acaba |
    | (d) Precio de acciones | **WebSocket** | Alta frecuencia |

    | | Dirección | Reconexión | Complejidad |
    |---|---|:-:|---|
    | *Polling* | Cliente pregunta | — | Mínima, muy ineficiente |
    | **SSE** | Servidor → cliente | **Automática, del navegador** | Baja |
    | **WebSocket** | Bidireccional | La haces tú | Media |

    **SSE está infravalorado:** para notificaciones de servidor a cliente —el 80 % de los casos— es más simple, va sobre HTTP normal y el navegador reconecta solo. WebSocket solo hace falta cuando el cliente **también** manda.

## E2 ● — El `101`

¿Qué significa `HTTP/1.1 101 Switching Protocols` y qué consecuencia práctica tiene?

??? success "Solución"

    Que el servidor acepta **cambiar de protocolo** en esa misma conexión TCP: deja de ser HTTP petición-respuesta y pasa a ser un canal abierto en los dos sentidos.

    Dos consecuencias:

    1. **Va por el puerto 80 o 443**, así que atraviesa cortafuegos y *proxies* como tráfico web normal. No hace falta abrir nada.
    2. **La conexión no se cierra**, así que el servidor mantiene recursos por cada cliente. Diez mil conexiones son diez mil sesiones en memoria.

    Es el único código `1xx` que vas a ver en tu vida.

## E3 ●● — La colección de sesiones

```java
private final List<WebSocketSession> sesiones = new ArrayList<>();
```

??? success "Solución"

    **`ArrayList` no es seguro con varios hilos**, y aquí hay varios garantizados: cada conexión entra por un hilo distinto y el *broadcast* recorre la lista mientras alguien se conecta o se desconecta.

    Resultado: `ConcurrentModificationException` (el de la [UT2, E18](../ut2/ejercicios.md)) o corrupción silenciosa.

    ```java
    private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();
    ```

    `CopyOnWriteArraySet` copia el array en cada escritura, así que recorrerlo es seguro siempre. Para **pocas escrituras y muchas lecturas** —exactamente este caso— es la elección correcta.

## E4 ●● — El cliente que se fue

```java
for (var s : sesiones) {
    s.sendMessage(new TextMessage(json));
}
```

??? success "Solución"

    ```
    java.lang.IllegalStateException: The WebSocket session has been closed
    ```

    Y lo peor: la excepción **corta el bucle**, así que los clientes que vienen después **no reciben nada**. Un cliente desconectado deja sin notificación a todos los demás.

    ```java
    for (var s : sesiones) {
        try {
            if (s.isOpen()) s.sendMessage(new TextMessage(json));
            else sesiones.remove(s);
        } catch (IOException e) {
            log.warn("No se pudo enviar a {}: {}", s.getId(), e.getMessage());
            sesiones.remove(s);
        }
    }
    ```

    **`isOpen()` no basta**: entre la comprobación y el envío la conexión puede caerse. Hacen falta las dos cosas.

## E5 ●● — Notificar antes de confirmar

```java
@Transactional
public FunkoResponse save(FunkoCreateRequest r) {
    var guardado = repositorio.save(mapper.toModel(r));
    notificador.notificar("CREATE", guardado);
    return mapper.toResponse(guardado);
}
```

??? success "Solución"

    Si algo falla después del `notificar` pero antes de confirmar, el `rollback` deshace el `INSERT` y **el mensaje ya está enviado**. Los clientes ven un funko que no existe.

    ```java
    // en el servicio
    eventos.publishEvent(new FunkoCambiadoEvent(Tipo.CREATE, respuesta));

    // en el notificador
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiar(FunkoCambiadoEvent e) { notificar(e); }
    ```

    `AFTER_COMMIT` significa exactamente eso: **se ejecuta solo si la transacción se ha confirmado**.

    Y hay un segundo motivo para los eventos: el servicio deja de conocer al notificador. Mañana puedes añadir un oyente de auditoría sin tocar el servicio.

    **Cómo comprobarlo:** manda un POST con el nombre repetido (409). Si llega notificación, falta el `AFTER_COMMIT`.

## E6 ●● — La entidad en el notificador

¿Por qué se manda el `FunkoResponse` y no el `Funko`?

??? success "Solución"

    Dos razones, y las dos son de la UT5:

    1. **La entidad expone campos** que no quieres publicar.
    2. **Estamos fuera de toda transacción.** Una relación `LAZY` serializada aquí lanza `LazyInitializationException` **dentro del notificador**, donde además es más difícil de diagnosticar: no hay petición HTTP que correlacionar.

    El DTO ya está materializado: son datos planos.

## E7 ●●● — El notificador completo

Escribe el `handler`, la configuración y el notificador.

??? success "Solución"

    ```java title="FunkosWebSocketHandler.java"
    @Component
    public class FunkosWebSocketHandler extends TextWebSocketHandler {

        private static final Logger log = LoggerFactory.getLogger(FunkosWebSocketHandler.class);
        private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession s) {
            sesiones.add(s);
            log.info("WS conectado {} · total: {}", s.getId(), sesiones.size());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession s, CloseStatus st) {
            sesiones.remove(s);
        }

        @Override
        public void handleTransportError(WebSocketSession s, Throwable e) {
            sesiones.remove(s);
        }

        public void enviarATodos(String mensaje) {
            for (var s : sesiones) {
                try {
                    if (s.isOpen()) s.sendMessage(new TextMessage(mensaje));
                    else sesiones.remove(s);
                } catch (IOException e) { sesiones.remove(s); }
            }
        }
    }
    ```

    ```java title="WebSocketConfig.java"
    @Configuration
    @EnableWebSocket
    public class WebSocketConfig implements WebSocketConfigurer {

        private final FunkosWebSocketHandler handler;
        private final String[] origenes;

        public WebSocketConfig(FunkosWebSocketHandler handler,
                               @Value("${cors.origenes-permitidos}") String[] origenes) {
            this.handler = handler;
            this.origenes = origenes;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(handler, "/ws/v1/funkos").setAllowedOrigins(origenes);
        }
    }
    ```

    ```java title="FunkosNotificador.java"
    @Component
    public class FunkosNotificador {

        private final FunkosWebSocketHandler handler;
        private final ObjectMapper mapper;

        public FunkosNotificador(FunkosWebSocketHandler handler, ObjectMapper mapper) {
            this.handler = handler; this.mapper = mapper;
        }

        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void alCambiar(FunkoCambiadoEvent e) throws JsonProcessingException {
            handler.enviarATodos(mapper.writeValueAsString(new Notificacion(
                    "FUNKO", e.tipo().name(), e.funko(), LocalDateTime.now().toString())));
        }

        public record Notificacion(String entidad, String tipo,
                                   FunkoResponse datos, String fecha) {}
    }
    ```

    **`setAllowedOrigins` no es opcional:** los WebSockets **no están sujetos a la política del mismo origen**, así que es la única protección contra que cualquier web del mundo se conecte a escuchar.

---

# Bloque 2 · GraphQL

> Tema [2. GraphQL](02-graphql.md)

## E8 ● — GraphQL frente a REST

Completa la tabla con las siete filas que importan.

??? success "Solución"

    | | REST | GraphQL |
    |---|---|---|
    | Endpoints | Uno por recurso | **Uno**: `/graphql` |
    | Qué datos llegan | Los que decide el servidor | **Los que pide el cliente** |
    | Varios recursos | Varias peticiones | **Una** |
    | Verbos | GET/POST/PUT/PATCH/DELETE | `query` y `mutation`, todo por POST |
    | Códigos de estado | 200/201/400/404/409 | **200 casi siempre** |
    | Caché HTTP | **Nativa** (`ETag`, `Cache-Control`) | Hay que montarla |
    | Coste de una petición | Conocido y fijo | **Lo decide el cliente** |

    **Las dos que deciden en la práctica:** la caché (gratis en REST) y los códigos de estado (la mitad del contrato en REST).

    GraphQL gana cuando el cliente es una app con muchas pantallas distintas sobre los mismos datos. REST gana cuando el consumo es homogéneo y la caché importa.

## E9 ● — Los símbolos del esquema

`String`, `String!`, `[String]`, `[String!]!`

??? success "Solución"

    | | Significa |
    |---|---|
    | `String` | Puede ser `null` |
    | `String!` | No puede ser `null` |
    | `[String]` | La lista puede ser `null`, y sus elementos también |
    | `[String]!` | La lista no es `null`; los elementos sí pueden serlo |
    | `[String!]!` | **Ni la lista ni los elementos** |

    **`[Funko!]!` es lo correcto para un listado:** una lista vacía es `[]`, nunca `null`, y nunca hay un `null` dentro.

    Y el detalle fino: si un campo es `!` y el resolutor devuelve `null`, **GraphQL anula el objeto padre entero** y lo reporta como error. La no-nulabilidad se propaga hacia arriba.

## E10 ●● — `query`, `mutation`, `subscription`

¿Qué hace cada una, con qué se corresponde en REST y en qué se comportan distinto?

??? success "Solución"

    | | GraphQL | REST |
    |---|---|---|
    | Leer | `query` | `GET` |
    | Escribir | `mutation` | `POST`/`PUT`/`PATCH`/`DELETE` |
    | Tiempo real | `subscription` | WebSocket o SSE |

    Dos diferencias de comportamiento:

    - **Las `query` se ejecutan en paralelo**; las `mutation`, **en serie y en orden**. Dos mutaciones en una petición: la segunda ve el efecto de la primera.
    - **Todas van por `POST`** a `/graphql`, también las lecturas. Por eso la caché HTTP no sirve de serie.

    Y `subscription` por debajo va sobre un WebSocket: es el bloque 1 otra vez.

## E11 ●● — El 200 que es un error

Tu monitorización avisa de los 4xx y 5xx. Con GraphQL no avisa nunca.

??? success "Solución"

    Porque **GraphQL devuelve 200 casi siempre**, incluso con una consulta inválida o una excepción en el servicio. Los errores van en el cuerpo:

    ```json
    { "data": { "funkoById": null },
      "errors": [{ "message": "No existe el funko 99",
                   "extensions": { "classification": "NOT_FOUND" } }] }
    ```

    Los 4xx solo salen cuando el fallo es **anterior** a GraphQL: JSON mal formado (400) o autenticación (401).

    **Lo que hay que hacer:** monitorizar **la presencia del campo `errors`**, no el código de estado.

    Y una respuesta GraphQL puede traer **datos y errores a la vez**: lo que se pudo resolver, resuelto.

## E12 ●● — Traducir las excepciones

Haz que tu `FunkoNotFoundException` salga como `NOT_FOUND` en GraphQL.

??? success "Solución"

    ```java
    @Component
    public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

        @Override
        protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
            return switch (ex) {
                case FunkoNotFoundException e -> error(env, ErrorType.NOT_FOUND, e);
                case FunkoConflictException e -> error(env, ErrorType.BAD_REQUEST, e);
                case FunkoBadRequestException e -> error(env, ErrorType.BAD_REQUEST, e);
                default -> null;                 // que lo trate el por defecto
            };
        }

        private GraphQLError error(DataFetchingEnvironment env, ErrorType t, Throwable e) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(t).message(e.getMessage()).build();
        }
    }
    ```

    **Y aquí se ve la decisión de la UT4 cobrando intereses:** la misma excepción, sin tocarla, da un **404** en REST (por su `@ResponseStatus`) y un `NOT_FOUND` en GraphQL.

    Si el servicio lanzara `ResponseStatusException` con `HttpStatus`, aquí no habría nada que traducir: en GraphQL un código HTTP no significa nada.

    El `switch` con patrones es Java 25, el de la UT2.

## E13 ●●● — El N+1 de GraphQL

```graphql
{ funkos { contenido { nombre categoria { nombre } } } }
```

Con 100 funkos, ¿cuántas consultas y cómo se arregla?

??? success "Solución"

    **101.** Una para los funkos y una por cada funko para su categoría.

    Es el problema característico de GraphQL, y es **peor que en REST** porque el cliente decide cuándo ocurre: tú no controlas qué campos piden.

    ```java
    @BatchMapping(typeName = "Funko")
    public Map<FunkoResponse, CategoriaResponse> categoria(List<FunkoResponse> funkos) {
        var nombres = funkos.stream().map(FunkoResponse::categoria).distinct().toList();
        var porNombre = categoriasService.findByNombres(nombres).stream()
                .collect(Collectors.toMap(CategoriaResponse::nombre, c -> c));
        return funkos.stream().collect(Collectors.toMap(f -> f,
                                       f -> porNombre.get(f.categoria())));
    }
    ```

    **`@BatchMapping` recibe la lista entera de golpe** y hace una sola consulta con todos los nombres. De 101 a **2**.

    Y la diferencia con el [E11 de la UT5](../ut5/ejercicios.md): allí se arreglaba con un `JOIN FETCH` fijo. **Aquí no se puede**, porque la consulta depende de lo que pida el cliente. Hace falta el agrupamiento.

    **Cómo medirlo:** `show-sql=true`, pedir la consulta y contar. Es lo que se mira en la defensa.

## E14 ●●● — La consulta que tumba el servidor

```graphql
{ funkos { contenido { categoria { funkos { categoria { funkos { nombre } } } } } } }
```

??? success "Solución"

    **Es legal** con un esquema bidireccional, y puede agotar la memoria o el tiempo del servidor. En REST esto no existe: cada endpoint tiene un coste fijo.

    ```java
    @Bean
    public GraphQlSourceBuilderCustomizer limites() {
        return builder -> builder.configureGraphQl(graphQl ->
                graphQl.instrumentation(List.of(
                        new MaxQueryDepthInstrumentation(10),
                        new MaxQueryComplexityInstrumentation(200))));
    }
    ```

    ```properties
    spring.graphql.schema.introspection.enabled=false    # en prod
    ```

    **Las dos cosas hacen falta:**

    - **Los límites** impiden la consulta abusiva.
    - **Apagar la introspección** impide que alguien descargue tu esquema completo —todos los tipos, campos y mutaciones— y se haga un mapa de por dónde entrar.

    Y con los `@BatchMapping` bien puestos, incluso una consulta anidada se resuelve en un número **fijo y pequeño** de consultas SQL, porque cada nivel se agrupa.

## E15 ●●● — El controlador GraphQL

Escribe el esquema y el controlador para consultar y crear funkos, reutilizando el servicio.

??? success "Solución"

    ```graphql title="src/main/resources/graphql/schema.graphqls"
    type Funko {
        id: ID!
        nombre: String!
        precio: Float!
        cantidad: Int!
        categoria: Categoria!
    }

    type Categoria { id: ID!  nombre: String!  funkos: [Funko!]! }

    input FunkoInput  { nombre: String!  precio: Float!  cantidad: Int!  categoria: String! }
    input FunkoFiltro { categoria: String  precioMin: Float  nombre: String }

    type Query {
        funkos(filtro: FunkoFiltro, pagina: Int = 0, tamano: Int = 20): [Funko!]!
        funkoById(id: ID!): Funko
    }

    type Mutation {
        crearFunko(input: FunkoInput!): Funko!
        borrarFunko(id: ID!): Boolean!
    }
    ```

    ```java
    @Controller
    public class FunkosGraphQlController {

        private final FunkosService servicio;        // ← EL MISMO que usa REST

        public FunkosGraphQlController(FunkosService servicio) { this.servicio = servicio; }

        @QueryMapping
        public List<FunkoResponse> funkos(@Argument Optional<FunkoFiltro> filtro,
                                          @Argument int pagina, @Argument int tamano) {
            var f = filtro.orElse(FunkoFiltro.vacio());
            return servicio.findAll(
                    Optional.ofNullable(f.categoria()),
                    Optional.ofNullable(f.precioMin()),
                    Optional.empty(),
                    Optional.ofNullable(f.nombre()),
                    PageRequest.of(pagina, Math.min(tamano, 100))).getContent();
        }

        @QueryMapping
        public FunkoResponse funkoById(@Argument Long id) { return servicio.findById(id); }

        @MutationMapping
        public FunkoResponse crearFunko(@Argument("input") @Valid FunkoCreateRequest input) {
            return servicio.save(input);
        }

        @MutationMapping
        public Boolean borrarFunko(@Argument Long id) {
            servicio.deleteById(id);
            return true;
        }
    }
    ```

    **Lo importante es lo que NO hay: ni una línea de lógica de negocio.** Son cuatro métodos de fontanería que llaman a los mismos métodos del servicio que el `@RestController`.

    Eso es lo que compraste con las capas en la UT4: el tercer transporte sale casi gratis.

---

# Bloque 3 · CORS y documentación

> Tema [3. CORS y documentación de APIs](03-documentacion-apis.md)

## E16 ● — Quién bloquea

Tu API funciona con `curl` y falla desde el navegador con un error de CORS.

??? success "Solución"

    **El navegador**, no tu servidor.

    La petición **sí llega**, se procesa y se responde con un 200. Lo que hace el navegador es **negarle el resultado al JavaScript** porque la respuesta no trae `Access-Control-Allow-Origin` con su origen.

    `curl` no implementa la política del mismo origen, así que no le afecta. Eso explica el *«pero si funciona en Postman»*.

    **Y la consecuencia de fondo:** CORS **no es seguridad del servidor**. No protege tu API de nada: cualquiera puede llamarla desde fuera de un navegador. Protege **al usuario** de que una web maliciosa use su sesión en otro sitio.

## E17 ● — El `OPTIONS`

En el log aparece un `OPTIONS /api/v1/funkos` antes de cada POST.

??? success "Solución"

    Es el ***preflight***: antes de una petición «no simple», el navegador **pregunta primero**.

    Lo disparan:

    - Verbos distintos de `GET`, `HEAD` y `POST`.
    - **`Content-Type: application/json`** — sí, esto solo ya lo dispara.
    - Cabeceras propias como `Authorization`.

    Es decir: **casi cualquier petición de una API REST**.

    ```
    OPTIONS /api/v1/funkos
    Origin: http://localhost:5173
    Access-Control-Request-Method: POST

    → 200
    Access-Control-Allow-Origin: http://localhost:5173
    Access-Control-Allow-Methods: POST
    Access-Control-Max-Age: 3600
    ```

    El **`Max-Age`** es el que importa en rendimiento: sin él, **cada** POST son dos viajes de ida y vuelta.

## E18 ●● — El comodín con credenciales

```java
.allowedOrigins("*")
.allowCredentials(true)
```

??? success "Solución"

    **No funciona.** El navegador rechaza la combinación:

    ```
    The value of the 'Access-Control-Allow-Origin' header must not be the
    wildcard '*' when the request's credentials mode is 'include'.
    ```

    Y está prohibido **a propósito**: `allowCredentials(true)` hace que el navegador mande cookies y cabeceras de autenticación. Con `*`, **cualquier web de internet podría hacer peticiones autenticadas con la sesión de tu usuario**. Es exactamente el ataque CSRF que CORS existe para impedir.

    Si necesitas comodín con credenciales: `allowedOriginPatterns("https://*.miapp.es")`, que Spring resuelve al origen concreto en cada respuesta.

    **La regla:** `allowedOrigins("*")` solo para una API pública **sin autenticación**.

## E19 ●● — La cabecera que el cliente no ve

Tu POST devuelve 201 con `Location`, y el JavaScript del cliente dice que `Location` es `null`.

??? success "Solución"

    Por defecto, el JavaScript **solo puede leer seis cabeceras** de una respuesta con CORS: `Cache-Control`, `Content-Language`, `Content-Type`, `Expires`, `Last-Modified` y `Pragma`.

    `Location` no está entre ellas.

    ```java
    .exposedHeaders("Location", "X-Total-Count")
    ```

    **La cabecera sí llega** —se ve en las DevTools, en la pestaña de red— pero el navegador no deja que el código la lea. Es de los errores más desconcertantes de CORS, porque todo *parece* correcto.

## E20 ●● — CORS por perfiles

Frontend en `http://localhost:5173` en desarrollo y en `https://miapp.es` en producción.

??? success "Solución"

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

    ```properties title="application-dev.properties"
    cors.origenes-permitidos=http://localhost:5173,http://localhost:3000
    ```

    ```properties title="application-prod.properties"
    cors.origenes-permitidos=https://miapp.es
    ```

    Es el [tema 5 de la UT4](../ut4/05-configuracion.md) aplicado: **el mismo `.jar` sirve en los dos entornos**.

    `@CrossOrigin` en el controlador vale para probar, pero reparte la política por el código y hay que acordarse en cada clase nueva.

## E21 ●● — Qué deduce `springdoc` y qué no

??? success "Solución"

    | Lo deduce de tu código | Hay que escribirlo |
    |---|---|
    | Ruta, verbo, parámetros | **Para qué sirve** el endpoint |
    | Tipos y obligatoriedad (de `@NotBlank`, `@Min`…) | **Qué errores** devuelve y cuándo |
    | Esquema completo de los DTOs | **Ejemplos** de valores reales |
    | Los valores de un `enum` | Las **reglas de negocio** |

    La columna de la izquierda sale sola y hace Swagger útil desde el minuto uno. La de la derecha convierte «una lista de endpoints» en **documentación**.

    Y lo que ninguna herramienta podrá deducir: que un POST devuelve **409** si el nombre está repetido. Eso es una regla de negocio y hay que escribirla.

## E22 ●● — Swagger en producción

¿Se puede dejar abierto?

??? success "Solución"

    **No.** Swagger UI **no es un visor: es un cliente HTTP completo** con un botón «Try it out» que manda peticiones de verdad.

    Dejarlo abierto publica:

    - La lista completa de tus endpoints, incluidos los que no documentas en ningún sitio.
    - El esquema de tus DTOs, que dice qué campos tienes.
    - Un formulario para llamarlos a todos.

    ```properties title="application-prod.properties"
    springdoc.api-docs.enabled=false
    springdoc.swagger-ui.enabled=false
    ```

    Es el mismo criterio que la consola de H2 de la UT5 y la introspección de GraphQL: **todo lo cómodo en desarrollo se apaga en producción**.

    El **JSON** de la especificación sí puede publicarse si tu API es pública: con él se generan clientes. Lo que no debe quedarse es la interfaz con el botón de disparar.

## E23 ●●● — Documentar un endpoint de verdad

Documenta `POST /funkos` con sus tres respuestas posibles.

??? success "Solución"

    ```java
    @Operation(
        summary = "Crea un funko",
        description = """
            Crea un funko nuevo. El id y las fechas los asigna el servidor.
            La categoría debe existir previamente.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201",
            description = "Creado. Devuelve la cabecera Location con la URL del recurso",
            content = @Content(schema = @Schema(implementation = FunkoResponse.class))),
        @ApiResponse(responseCode = "400",
            description = """
                Datos inválidos. El cuerpo lleva el detalle por campo.
                También sale si la categoría indicada no existe.
                """,
            content = @Content),
        @ApiResponse(responseCode = "409",
            description = "Ya existe un funko con ese nombre",
            content = @Content)
    })
    @PostMapping
    public ResponseEntity<FunkoResponse> create(
            @RequestBody(description = "Datos del funko", required = true)
            @Valid @org.springframework.web.bind.annotation.RequestBody
            FunkoCreateRequest funko) { … }
    ```

    ```java
    @Schema(description = "Datos para crear un funko")
    public record FunkoCreateRequest(
            @Schema(description = "Nombre, único", example = "Mickey Mouse")
            @NotBlank @Size(max = 100) String nombre,
            @Schema(description = "Precio en euros", example = "15.99")
            @NotNull @PositiveOrZero BigDecimal precio,
            @Schema(description = "Categoría, debe existir", example = "DISNEY")
            @NotBlank String categoria) {}
    ```

    **Los 409 son la parte que importa.** Las rutas y los tipos los deduce la herramienta; «devuelve 409 si el nombre está repetido» es justo el dato que necesita quien va a consumir tu API, y solo lo sabes tú.

## E24 ●●● — La prueba definitiva de la documentación

¿Cómo sabes si tu documentación es suficiente?

??? success "Solución"

    **Se la das a otra pareja y les pides que hagan cinco operaciones con tu API, incluida una que provoque un 409. Si tienen que preguntarte algo, falta documentación.**

    Es literalmente lo que se hace en la S13: se cruzan los proyectos.

    Lo que siempre falta, en este orden:

    1. **Los códigos de error y en qué caso sale cada uno.** Lo más frecuente con diferencia.
    2. **Ejemplos con valores reales.** `"string"` y `0` no sirven: hay que saber que la categoría es `DISNEY` y no `disney` o `1`.
    3. **Las reglas de negocio.** Que no se puede borrar una categoría con funkos no está en ninguna anotación.
    4. **Qué devuelve exactamente el 400.** Un cliente necesita saber que el cuerpo lleva `campos` con los errores por campo para poder pintarlos.

    Y la comprobación mecánica, antes de entregar:

    ```bash
    # Los tres curl del README deben funcionar al copiar y pegar
    bash -c "$(grep -A2 '```bash' README.md | grep curl)"
    ```

---

## Reparto sugerido

| Sesión | Bloque | Ejercicios |
|:-:|---|---|
| **S1** | 1 · HTTP frente a WebSocket | E1 – E2 |
| **S2** | 1 · Sesiones y *broadcast* | E3 – E4 |
| **S3** | 1 · Eventos y `AFTER_COMMIT` | E5 – E7 |
| **S6** | 2 · Esquema y comparación | E8 – E9 |
| **S7** | 2 · Operaciones y errores | E10 – E12 |
| **S8** | 2 · El N+1 y los límites | E13 – E15 |
| **S11** | 3 · CORS | E16 – E20 |
| **S12** | 3 · OpenAPI | E21 – E24 |
| **S15–S17** | — | [Retos](retos.md) |
| **S18** | — | Examen práctico |

!!! success "Si vas justo de tiempo"
    El mínimo: **E1, E3, E5, E8, E11, E13, E16, E19, E22**.

    Y los cuatro que separan un 5 de un 9, porque son los que **no se notan probando a mano**:

    - **E5** — notificar antes de confirmar: solo falla cuando una transacción falla.
    - **E13** — el N+1 de GraphQL: solo se ve contando consultas en el log.
    - **E19** — `exposedHeaders`: todo parece correcto y el cliente no lee la cabecera.
    - **E22** — Swagger en producción: funciona perfectamente, y ahí está el problema.
