# Batería de ejercicios — UT6

**Dominio: una biblioteca.** El mismo de la UT4 y la UT5, ahora expuesto como API profesional.

**32 ejercicios agrupados por tema**, con solución. Los marcados con :material-file-sign: son del tipo que cae en el examen.

| Tema | Ejercicios | Sesiones |
|---|---|:-:|
| 1 · Diseño de APIs REST | E1–E4 | S1–S2 |
| 2 · Respuestas y errores | E5–E8 | S3–S5 |
| 3 · Paginación y búsqueda | E9–E12 | S6–S8 |
| 4 · Documentación OpenAPI | E13–E16 | S9–S10 |
| 5 · GraphQL | E17–E20 | S11–S14 |
| 6 · WebSockets y tiempo real | E21–E24 | S15–S17 |
| 7 · Testing de APIs | E25–E28 | S18–S19 |
| 8 · Consumir APIs | E29–E32 | S20 |

---

# Tema 1 · Diseño de APIs REST

### E1 ● — Rediseña estas rutas

```
GET  /getLibros
POST /crearLibro
GET  /libro/borrar/5
POST /api/prestarLibro?id=5
GET  /obtenerLibrosDeAutor/3
```

??? success "Solución"

    ```
    GET    /api/v1/libros
    POST   /api/v1/libros
    DELETE /api/v1/libros/5
    POST   /api/v1/libros/5/prestamos
    GET    /api/v1/autores/3/libros
    ```
    Cuatro reglas: **el verbo lo pone HTTP**, los recursos van en **plural**, las jerarquías se expresan con **subrecursos**, y la versión va al principio.

    La cuarta es la más interesante: prestar no es «una acción sobre el libro», es **crear un préstamo**. Convertir acciones en recursos resuelve casi todas las dudas de diseño.


### E2 ●● — Idempotencia en vivo

Demuestra con `curl` cuáles de tus endpoints son idempotentes.

??? success "Solución"

    ```bash
    for i in 1 2 3; do curl -s -o /dev/null -w "%{http_code} " -X POST /api/v1/libros \
        -d '{...}'; done
    # 201 201 201  → tres libros creados: NO idempotente

    for i in 1 2 3; do curl -s -o /dev/null -w "%{http_code} " -X PUT /api/v1/libros/1 \
        -d '{...}'; done
    # 200 200 200  → mismo estado final: idempotente

    for i in 1 2 3; do curl -s -o /dev/null -w "%{http_code} " \
        -X DELETE /api/v1/libros/1; done
    # 204 404 404  → códigos distintos, pero el estado final es el mismo: idempotente
    ```
    El `DELETE` es el que confunde: idempotente **no** significa «misma respuesta», significa «mismo estado final del servidor».


### E3 ●● — Versionado

Añade un campo al DTO sin romper a los clientes que ya consumen la API.

??? success "Solución"

    **Añadir** un campo opcional no rompe nada: los clientes antiguos lo ignoran. Eso es un cambio compatible y **no** exige versión nueva.

    Lo que sí rompe: quitar un campo, renombrarlo, cambiar su tipo o volver obligatorio algo que no lo era. Ahí sí toca `/api/v2`.

    ```java
    @RequestMapping("/api/v1/libros")     // versión en la ruta: la más legible
    ```
    Alternativas: cabecera `Accept: application/vnd.biblioteca.v2+json` o parámetro `?version=2`. La de la ruta gana en claridad y en cacheabilidad, que es lo que suele decidir.


### E4 ●●● — Diseña la API entera

Diseña todos los endpoints de la biblioteca: libros, autores, socios, préstamos y reservas. Con códigos.

??? success "Solución"

    ```
    GET    /api/v1/libros                     200
    GET    /api/v1/libros/{id}                200 · 404
    POST   /api/v1/libros                     201+Location · 400 · 409
    PUT    /api/v1/libros/{id}                200 · 400 · 404
    DELETE /api/v1/libros/{id}                204 · 404 · 409(prestado)
    GET    /api/v1/libros/{id}/prestamos      200 · 404
    POST   /api/v1/prestamos                  201+Location · 400 · 404 · 409
    PATCH  /api/v1/prestamos/{id}/devolucion  200 · 404 · 409
    GET    /api/v1/socios/{id}/prestamos      200 · 404
    GET    /api/v1/autores/{id}/libros        200 · 404
    ```
    Dos decisiones que se evalúan: el préstamo es **recurso propio** (tiene ciclo de vida), y la devolución es un `PATCH` sobre él, no un `POST /devolverLibro`.

    Y el 409 del `DELETE`: no se borra un libro prestado. Un código bien elegido comunica una regla de negocio sin documentación.


---

# Tema 2 · Respuestas y errores

### E5 ● — `Location` y seguirlo

Que el POST devuelva 201 con la cabecera, y sigue esa URL.

??? success "Solución"

    ```java
    @PostMapping
    public ResponseEntity<LibroDto> crear(@Valid @RequestBody CrearLibroDto dto) {
        var creado = servicio.crear(dto);
        return ResponseEntity
            .created(URI.create("/api/v1/libros/" + creado.id()))
            .body(creado);
    }
    ```
    ```bash
    L=$(curl -si -X POST … | grep -i ^location | tr -d '\r' | cut -d' ' -f2)
    curl -s "localhost:8080$L" | jq
    ```
    Devolver 200 en vez de 201, o no poner `Location`, son dos criterios distintos de la rúbrica.


### E6 ●● — El catálogo de códigos

Provoca desde `curl` un 200, 201, 204, 400, 404, 409 y 405.

??? success "Solución"

    ```bash
    curl -i localhost:8080/api/v1/libros                          # 200
    curl -i -X POST … -d '{válido}'                               # 201
    curl -i -X DELETE localhost:8080/api/v1/libros/1              # 204
    curl -i -X POST … -d '{"titulo":""}'                          # 400
    curl -i localhost:8080/api/v1/libros/9999                     # 404
    curl -i -X POST … -d '{isbn ya existente}'                    # 409
    curl -i -X DELETE localhost:8080/api/v1/libros                # 405
    ```
    El **405** sale solo: Spring sabe que la colección no admite `DELETE`. Si te devuelve 404, es que has mapeado la ruta mal.


### E7 ●● — Problem Details con campo extra

Que el 409 incluya qué libro y quién lo tiene.

??? success "Solución"

    ```java
    @ExceptionHandler(LibroYaPrestadoException.class)
    public ProblemDetail yaPrestado(LibroYaPrestadoException e) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        pd.setTitle("Libro no disponible");
        pd.setType(URI.create("https://biblioteca.es/errores/ya-prestado"));
        pd.setProperty("libroId", e.getLibroId());
        pd.setProperty("devolucionPrevista", e.getFechaPrevista());
        return pd;
    }
    ```
    Reflexión que se espera: incluir **quién** lo tiene sería una fuga de datos personales. La fecha prevista de devolución es útil y no identifica a nadie.


### E8 ●●● — Negociación de contenido

El mismo endpoint devuelve JSON o CSV según lo que pida el cliente.

??? success "Solución"

    ```java
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LibroDto> exportJson() { … }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=libros.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv.getBytes(StandardCharsets.UTF_8));
    }
    ```
    ```bash
    curl -H "Accept: text/csv" localhost:8080/api/v1/libros/export
    ```
    El `Content-Disposition: attachment` es lo que hace que el navegador **descargue** en vez de mostrar. Sin él, el CSV sale como texto en pantalla.


---

# Tema 3 · Paginación y búsqueda

### E9 ●● — De lista a página

Convierte un listado completo en paginado y demuestra que el `limit` lo hace la base de datos.

??? success "Solución"

    ```java
    @GetMapping
    public Page<LibroDto> listar(@PageableDefault(size = 20, sort = "titulo") Pageable p) {
        return servicio.listar(p);
    }
    ```
    En el log tiene que aparecer `limit ? offset ?`. La prueba que delata al que pagina en memoria: pedir `size=5` con 1.000 filas y ver si el SQL trae las 1.000.


### E10 ●●● — La lista blanca de ordenación

Solo se puede ordenar por `titulo`, `anio` y `autor`. Cualquier otro campo, 400.

??? success "Solución"

    ```java
    private static final Set<String> ORDENABLES = Set.of("titulo", "anio", "autor");

    private void validarOrden(Pageable p) {
        p.getSort().forEach(o -> {
            if (!ORDENABLES.contains(o.getProperty()))
                throw new OrdenNoPermitidoException(o.getProperty(), ORDENABLES);
        });
    }
    ```
    No es una manía: `?sort=passwordHash` ordenaría por un campo interno y, peor, **revela que existe**. La lista blanca es una medida de seguridad, no de estilo.


### E11 ●● — Filtros combinables

Cuatro filtros opcionales que funcionen juntos o por separado.

??? success "Solución"

    ```java
    @Query("""
           select l from Libro l
           where (:genero is null or l.genero = :genero)
             and (:autor  is null or l.autor.id = :autor)
             and (:desde  is null or l.anio >= :desde)
             and (:texto  is null or lower(l.titulo) like lower(concat('%', :texto, '%')))
           """)
    Page<Libro> buscar(…, Pageable pageable);
    ```
    Comprueba las 16 combinaciones posibles… o al menos las cuatro individuales y la de los cuatro juntos.


### E12 ●●● — Buscar sin tildes

Que «bandini» encuentre «Rigoberta Bandini» y «cortazar» encuentre «Cortázar».

??? success "Solución"

    ```java
    public static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                         .replaceAll("\\p{M}", "").toLowerCase();
    }
    ```
    Con una columna `titulo_normalizado` que se rellena al guardar, o con `unaccent` en PostgreSQL.

    Lo que **no** funciona: `replace("á","a")` uno a uno. Se olvidan la ü, la ñ y todo lo que no sea castellano.


---

# Tema 4 · Documentación OpenAPI

### E13 ● — Swagger en tres minutos

Añade springdoc y comprueba que la interfaz sale sola.

??? success "Solución"

    ```xml
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.7.0</version>
    </dependency>
    ```
    `/swagger-ui.html` y `/v3/api-docs`. Sin escribir una línea: se genera de los controladores.

    Lo que sale solo está incompleto, y por eso existen los tres ejercicios siguientes.


### E14 ●● — Documenta un controlador

Anota el de libros con descripción y todos sus códigos.

??? success "Solución"

    ```java
    @Tag(name = "Libros", description = "Catálogo de la biblioteca")
    @RestController
    public class LibroControlador {

        @Operation(summary = "Busca un libro por su identificador")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe",
                         content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        @GetMapping("/{id}")
        public LibroDto porId(@Parameter(description = "Id del libro") @PathVariable Long id) { … }
    }
    ```
    Documentar **los errores** es lo que distingue una API usable: quien la consuma necesita saber qué le puede llegar, no solo el caso bueno.


### E15 ●● — Información general

Añade título, versión, contacto y servidores.

??? success "Solución"

    ```java
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("API de la Biblioteca").version("1.0")
                  .description("Catálogo, préstamos y reservas")
                  .contact(new Contact().name("2.º DAW").email("dwes@iesejemplo.es"))
                  .license(new License().name("MIT")))
            .servers(List.of(new Server().url("http://localhost:8080").description("Desarrollo")));
    }
    ```


### E16 ●●● — El contrato como fuente de verdad

Descarga `/v3/api-docs`, cambia un DTO y observa qué pasa en el contrato.

??? success "Solución"

    ```bash
    curl -s localhost:8080/v3/api-docs | jq > contrato-antes.json
    # … renombras un campo del DTO …
    curl -s localhost:8080/v3/api-docs | jq > contrato-despues.json
    diff contrato-antes.json contrato-despues.json
    ```
    El `diff` muestra el cambio **incompatible** que acabas de introducir sin darte cuenta.

    En un equipo real esto se automatiza: el contrato se guarda en el repositorio y la CI falla si cambia sin subir la versión. Se llama *contract testing* y es el argumento de peso para documentar con OpenAPI en vez de a mano.


---

# Tema 5 · GraphQL

### E17 ●● — Tu primera consulta

Esquema, listado y consulta por id, con el *playground* funcionando.

??? success "Solución"

    ```graphql
    # src/main/resources/graphql/schema.graphqls
    type Query { libros: [Libro!]!  libro(id: ID!): Libro }
    type Libro { id: ID!  titulo: String!  anio: Int  autor: Autor }
    type Autor { id: ID!  nombre: String! }
    ```
    ```java
    @Controller
    public class LibroGraphQlControlador {
        @QueryMapping public List<LibroDto> libros()           { return servicio.listar(); }
        @QueryMapping public LibroDto libro(@Argument Long id) { return servicio.porId(id); }
    }
    ```
    ```yaml
    spring.graphql.graphiql.enabled: true      # /graphiql
    ```
    **El servicio no se toca.** Es el tercer controlador sobre la misma lógica, después del REST y antes del de vistas de la UT8.


### E18 ●●● — El N+1 vuelve

Pide `{ libros { titulo autor { nombre } } }` con 50 libros, cuenta consultas y arréglalo.

??? success "Solución"

    Con `@SchemaMapping`, **51 consultas**. Con `@BatchMapping`, **2**.

    ```java
    @BatchMapping
    public Map<Libro, Autor> autor(List<Libro> libros) {
        var ids = libros.stream().map(Libro::getAutorId).distinct().toList();
        var autores = autorRepo.findAllById(ids).stream().collect(toMap(Autor::getId, a -> a));
        return libros.stream().collect(toMap(l -> l, l -> autores.get(l.getAutorId())));
    }
    ```
    La diferencia con la UT5: aquí **no puedes prevenirlo con un `JOIN FETCH` fijo**, porque es el cliente quien decide en cada petición qué relaciones pide. Mismo problema, solución distinta — y esa comparación es lo que se evalúa.


### E19 ●● — Mutaciones

Añade el alta de un libro por GraphQL, con validación.

??? success "Solución"

    ```graphql
    input LibroEntrada { titulo: String!  anio: Int  autorId: ID! }
    type Mutation { crearLibro(entrada: LibroEntrada!): Libro! }
    ```
    ```java
    @MutationMapping
    public LibroDto crearLibro(@Argument @Valid LibroEntrada entrada) {
        return servicio.crear(entrada);
    }
    ```
    GraphQL responde **siempre 200**, incluso con errores: el fallo va en el array `errors` del cuerpo. Es una diferencia importante con REST y una crítica habitual, porque rompe el manejo de errores basado en códigos HTTP.


### E20 ●●● — REST o GraphQL, con criterio

Decide para tres casos y justifica: (a) app móvil con conexión mala, (b) integración con Hacienda, (c) panel interno de administración.

??? success "Solución"

    **(a) GraphQL.** Con conexión mala, pedir exactamente los campos necesarios en una sola petición ahorra ancho de banda y latencia.

    **(b) REST.** Los organismos publican contratos REST estables, se cachean por URL y son auditables. Nadie va a aceptar un esquema GraphQL propio.

    **(c) Cualquiera de los dos, probablemente REST.** Pocos usuarios y necesidades cambiantes: gana la simplicidad de operar.

    Lo que se evalúa es que menciones **cacheo HTTP, número de peticiones y estabilidad del contrato**. Responder «GraphQL porque es más moderno» vale cero.


---

# Tema 6 · WebSockets y tiempo real

### E21 ●● — El panel de préstamos

Cuando alguien presta un libro, todos los bibliotecarios conectados lo ven sin recargar.

??? success "Solución"

    ```java
    @Configuration @EnableWebSocketMessageBroker
    class WsConfig implements WebSocketMessageBrokerConfigurer {
        public void registerStompEndpoints(StompEndpointRegistry r) { r.addEndpoint("/ws").withSockJS(); }
        public void configureMessageBroker(MessageBrokerRegistry r) {
            r.enableSimpleBroker("/tema");
            r.setApplicationDestinationPrefixes("/app");
        }
    }
    ```
    ```java
    @Transactional
    public PrestamoDto prestar(Long libroId, String socio) {
        var dto = /* … lógica de siempre … */;
        ws.convertAndSend("/tema/prestamos", dto);
        return dto;
    }
    ```
    ```javascript
    stompClient.subscribe('/tema/prestamos', m => anadirFila(JSON.parse(m.body)));
    ```
    La difusión va **después** de la lógica. Avisar antes de confirmar es anunciar un préstamo que puede no existir.


### E22 ●● — SSE, más simple

El mismo panel, solo de servidor a cliente.

??? success "Solución"

    ```java
    private final Set<SseEmitter> clientes = new CopyOnWriteArraySet<>();

    @GetMapping(value = "/prestamos/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter suscribirse() {
        var e = new SseEmitter(Long.MAX_VALUE);
        clientes.add(e);
        e.onCompletion(() -> clientes.remove(e));
        e.onTimeout(()    -> clientes.remove(e));
        return e;
    }
    ```
    ```javascript
    new EventSource('/prestamos/stream').onmessage = e => anadirFila(JSON.parse(e.data));
    ```
    El `CopyOnWriteArraySet` no es capricho: la lista se modifica mientras se recorre para difundir, y con un `HashSet` normal salta `ConcurrentModificationException`.


### E23 ●● — Elegir con criterio

Decide para: (a) chat entre socios, (b) contador de plazas libres, (c) editor colaborativo, (d) avisos de mantenimiento.

??? success "Solución"

    | Caso | Elección | Por qué |
    |---|---|---|
    | (a) Chat | **WebSocket** | Los dos extremos escriben |
    | (b) Contador | **SSE** | Solo baja información |
    | (c) Editor colaborativo | **WebSocket** | Bidireccional y con mucha frecuencia |
    | (d) Avisos | **SSE** | Unidireccional y esporádico |

    La regla: **si el cliente no necesita responder por el mismo canal, SSE**. Es HTTP normal, reconecta solo y no da guerra con proxies.


### E24 ●●● — Qué pasa con varias instancias

Tu panel funciona. Despliegas dos réplicas y deja de funcionar para la mitad de los usuarios. Explica por qué.

??? success "Solución"

    El *broker* simple de Spring (`enableSimpleBroker`) vive **en memoria de cada instancia**. Si el préstamo lo procesa la réplica A, solo los clientes conectados a A se enteran; los de B no ven nada.

    Soluciones:
    1. **Broker externo** (RabbitMQ, ActiveMQ) con `enableStompBrokerRelay`.
    2. **Sesiones pegajosas** en el balanceador — funciona a medias y no escala.
    3. **Redis pub/sub** para propagar entre instancias.

    No hace falta implementarlo: basta con **saber que el problema existe** y poder nombrar la solución. Es exactamente el tipo de pregunta que separa a alguien que ha hecho un tutorial de alguien que ha pensado en producción.


---

# Tema 7 · Testing de APIs

### E25 ●● — Test de contrato

Prueba el 200 y el 404 con `@WebMvcTest`.

??? success "Solución"

    ```java
    @WebMvcTest(LibroControlador.class)
    class LibroControladorTest {
        @Autowired MockMvc mvc;
        @MockitoBean LibroServicio servicio;

        @Test void devuelveElLibro() throws Exception {
            when(servicio.porId(1L)).thenReturn(new LibroDto(1L, "Rayuela", …));
            mvc.perform(get("/api/v1/libros/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.titulo").value("Rayuela"));
        }
    }
    ```


### E26 ●●● — El test que caza el bug de paginación

Comprueba que una página fuera de rango devuelve 200 con lista vacía, no 404.

??? success "Solución"

    ```java
    @Test void paginaFueraDeRangoDevuelve200ConListaVacia() throws Exception {
        mvc.perform(get("/api/v1/libros?page=9999"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content").isArray())
           .andExpect(jsonPath("$.content.length()").value(0))
           .andExpect(jsonPath("$.totalElements").isNumber());
    }
    ```
    !!! danger "El bug del `jsonPath` que hay que conocer"
        Si por error el endpoint devuelve una **lista** en vez de una `Page`, `$.content.length()` no falla: JSONPath aplica el acceso a cada elemento y devuelve `0`. **El test pasa con la API rota.**

        Por eso está el `totalElements`: obliga a que la respuesta sea realmente paginada.


### E27 ●● — Tests parametrizados

Prueba diez combinaciones de filtros sin escribir diez tests.

??? success "Solución"

    ```java
    @ParameterizedTest
    @CsvSource({
        "genero=NOVELA,      3",
        "autor=1,            2",
        "texto=ray,          1",
        "genero=NOVELA&autor=1, 1",
        "genero=INEXISTENTE, 0"
    })
    void losFiltrosDevuelvenLoEsperado(String query, int esperados) throws Exception {
        mvc.perform(get("/api/v1/libros?" + query))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.content.length()").value(esperados));
    }
    ```


### E28 ●●● — Test de extremo a extremo

Levanta la aplicación con servidor real y prueba el flujo completo de alta y préstamo.

??? success "Solución"

    ```java
    @SpringBootTest(webEnvironment = RANDOM_PORT)
    class BibliotecaE2ETest {
        @Autowired TestRestTemplate rest;

        @Test void altaYPrestamo() {
            var r = rest.postForEntity("/api/v1/libros", new CrearLibroDto(…), LibroDto.class);
            assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            var location = r.getHeaders().getLocation();

            var p = rest.postForEntity("/api/v1/prestamos",
                        new CrearPrestamoDto(r.getBody().id(), "ana"), PrestamoDto.class);
            assertThat(p.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            var repetido = rest.postForEntity("/api/v1/prestamos",
                        new CrearPrestamoDto(r.getBody().id(), "luis"), ProblemDetail.class);
            assertThat(repetido.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }
    ```
    `RANDOM_PORT` evita que el test falle si tienes la aplicación arrancada en el 8080, que pasa constantemente.


---

# Tema 8 · Consumir APIs

### E29 ●● — `RestClient` con timeouts

Consulta Open Library por ISBN, con tiempos límite.

??? success "Solución"

    ```java
    @Bean
    RestClient openLibrary() {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(3));
        f.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
            .baseUrl("https://openlibrary.org")
            .defaultHeader("User-Agent", "BibliotecaIES/1.0 (dwes@iesejemplo.es)")
            .requestFactory(f).build();
    }
    ```
    Sin `readTimeout`, una API que no responde deja el hilo esperando indefinidamente y, con suficientes usuarios, tumba tu aplicación por culpa de un tercero.


### E30 ●● — DTO propio y respuesta vacía

Mapea a tu propio DTO y trata el caso «no existe».

??? success "Solución"

    ```java
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaOpenLibrary(String title, Integer number_of_pages) {}

    public Optional<LibroExterno> porIsbn(String isbn) {
        try {
            var r = cliente.get().uri("/isbn/{i}.json", isbn).retrieve()
                           .body(RespuestaOpenLibrary.class);
            return (r == null || r.title() == null) ? Optional.empty() : Optional.of(convertir(r));
        } catch (RestClientException e) {
            log.warn("Open Library no responde para {}", isbn);
            return Optional.empty();
        }
    }
    ```
    Trampa real: Open Library devuelve **`{}` con 200** cuando el ISBN no existe, no un 404. Quien espere el 404 dará por bueno un objeto vacío.


### E31 ●●● — Que un fallo ajeno no sea tuyo

La ficha del libro debe mostrarse aunque Open Library esté caído.

??? success "Solución"

    ```java
    @GetMapping("/{id}")
    public LibroDetalleDto detalle(@PathVariable Long id) {
        var libro = servicio.porId(id);
        var extra = openLibrary.porIsbn(libro.isbn()).orElse(null);
        return new LibroDetalleDto(libro, extra);
    }
    ```
    Prueba de fuego:
    ```bash
    sudo sh -c 'echo "127.0.0.1 openlibrary.org" >> /etc/hosts'
    ```
    La ficha debe seguir cargando, sin el bloque de información adicional. Si sale un 500, una fuente **secundaria** está tumbando tu página, y eso es un error de diseño.

    Recuerda quitar la línea después.


### E32 ●● — Caché y cuota

Cachea la respuesta y justifica el tiempo elegido.

??? success "Solución"

    ```java
    @Cacheable(value = "libros-externos", key = "#isbn")
    public Optional<LibroExterno> porIsbn(String isbn) { … }
    ```
    ```yaml
    spring.cache.caffeine.spec: maximumSize=2000,expireAfterWrite=7d
    ```
    Una semana es defendible: el título y el autor de un libro publicado no cambian. Y el porqué va en el README, que es criterio de rúbrica en la UT9.

    Comprobación: pedir el mismo ISBN diez veces y ver **una** llamada en el log, no diez.


---

## Cómo usarlos en clase

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E5 · E9 · E13 · E17 · E21 · E25 · E29 |
| Taller de la sesión, 25-30 min | E4 · E7 · E11 · E14 · E19 · E22 · E27 · E30 |
| Los que hay que hacer sí o sí | **E1 · E10 · E18 · E26 · E31** |
| Para quien va sobrado | E8 · E16 · E20 · E24 · E28 |
| Repaso antes del examen | E4 · E6 · E10 · E12 · E18 |

!!! tip "Los dos que más enseñan"
    **E18** (el N+1 en GraphQL) y **E26** (el bug del `jsonPath`).

    En los dos, el código funciona y el resultado parece correcto. El primero da la respuesta buena con 51 consultas; el segundo es un test **en verde** sobre una API rota. Aprender a desconfiar de lo que parece bien es la mitad de esta unidad.
