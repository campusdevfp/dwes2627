# Retos — UT6

Tres retos. **Los dos primeros vienen resueltos**; **el tercero se entrega**, y es del mismo tamaño y la misma forma que el examen práctico de la unidad.

| | Reto | Sesiones | |
|:-:|---|:-:|---|
| **R1** | Una API que otro pueda usar sin preguntarte | S1–S8 | :material-check-circle: Resuelto |
| **R2** | Que aguante datos de verdad | S9–S15 | :material-check-circle: Resuelto |
| **R3** | La API de incidencias | S16–S18 | :material-pencil: **A entregar** |

!!! info "Con `curl` delante"
    En esta unidad no se mira el navegador: se mira la **respuesta HTTP completa**. `curl -i` en todo momento, y atención a los códigos y a las cabeceras, no solo al cuerpo.

---

# R1 · Una API que otro pueda usar sin preguntarte

**Sesiones S1–S8** · Resuelto

## La situación

Vuestra API del inventario funciona, y cada vez que alguien de otro grupo quiere usarla tiene que preguntaros qué rutas hay, qué devuelven y qué pasa cuando algo falla.

## La pregunta

> **Dejad la API en un estado en el que otro equipo pueda integrarla leyendo solo su documentación, sin hablar con vosotros.**

## Restricciones

- **Sustantivos en plural, nunca verbos** en las rutas. Si aparece `/obtenerEquipos`, el reto está mal.
- La versión en la ruta desde el primer día: `/api/v1/`.
- Los errores, en **Problem Details**. Nada de `{"error": "fallo"}`.
- La documentación **se genera del código**, no se escribe aparte: un documento a mano se queda obsoleto en dos semanas.

## Criterios de aceptación

- [ ] Todas las rutas siguen el convenio y están versionadas.
- [ ] Cada operación devuelve el código correcto, incluido `201` + `Location` y `204`.
- [ ] Los errores salen en `ProblemDetail` con `type`, `title`, `status` y `detail`.
- [ ] `/swagger-ui.html` muestra la API entera, con ejemplos y los códigos de error documentados.
- [ ] Un `curl` con `Accept: application/xml` devuelve `406`, no un `500`.

??? success "Solución · el contrato"

    ```java
    @RestController
    @RequestMapping("/api/v1/equipos")
    @Tag(name = "Equipos", description = "Inventario de material informático")
    public class EquipoControlador {

        @Operation(summary = "Lista los equipos",
                   description = "Admite filtro por tipo y por estado")
        @ApiResponse(responseCode = "200", description = "Lista devuelta")
        @GetMapping
        public List<EquipoDto> listar(
                @RequestParam(required = false) String tipo,
                @RequestParam(required = false) Equipo.Estado estado) { … }

        @Operation(summary = "Obtiene un equipo por su identificador")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        })
        @GetMapping("/{id}")
        public EquipoDto obtener(@PathVariable Long id) { … }

        @Operation(summary = "Da de alta un equipo")
        @ApiResponse(responseCode = "201",
                     headers = @Header(name = "Location",
                                       description = "URI del equipo creado"))
        @PostMapping
        public ResponseEntity<EquipoDto> crear(@Valid @RequestBody CrearEquipoDto dto) { … }
    }
    ```

    Con una dependencia:

    ```xml
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.7.0</version>
    </dependency>
    ```

    …quedan publicados `/v3/api-docs` (el JSON del contrato, que otro equipo puede usar para **generar un cliente**) y `/swagger-ui.html` (la interfaz navegable, donde se pueden lanzar peticiones).

    **Lo importante no son las anotaciones.** Es que la documentación **sale del código**: cuando alguien cambie una firma, la documentación cambia sola. Un `.docx` con la lista de endpoints está obsoleto antes de que lo leas.

    Y las decisiones de diseño que se evalúan:

    | Mal | Bien | Por qué |
    |---|---|---|
    | `/obtenerEquipos` | `/api/v1/equipos` | El verbo lo pone HTTP |
    | `/equipos/tipo/portatil` | `/equipos?tipo=portatil` | El filtro escala; la ruta no |
    | `/equipos` sin versión | `/api/v1/equipos` | Añadirla después rompe a todos |
    | `/equiposAgotados` | `/equipos-agotados` | Minúsculas y guiones |

??? success "Solución · errores y negociación de contenido"

    ```java
    @RestControllerAdvice
    public class ManejadorErrores {

        @ExceptionHandler(EquipoNoEncontradoException.class)
        public ProblemDetail noEncontrado(EquipoNoEncontradoException e,
                                          HttpServletRequest peticion) {
            var p = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
            p.setTitle("Equipo no encontrado");
            p.setType(URI.create("https://api.iesx.es/errores/equipo-no-encontrado"));
            p.setInstance(URI.create(peticion.getRequestURI()));
            p.setProperty("marcaDeTiempo", Instant.now());          // (1)
            return p;
        }

        @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)  // (2)
        public ResponseEntity<Void> noAceptable() {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
    ```

    1.  `setProperty` añade campos propios al JSON estándar sin romper el formato.
    2.  Sin este manejador, pedir `Accept: application/xml` a una API que solo produce JSON acaba en un `500`. Con él, en el `406` que corresponde.

    **Por qué `type` es el campo que importa.** Es un identificador **estable**: el cliente puede programar contra él.

    ```javascript
    if (error.type.endsWith("/stock-insuficiente")) { mostrarSelectorUnidades(); }
    ```

    Comparar `error.title === "Stock insuficiente"` se rompe el día que alguien corrija una tilde. Por eso `title` es para humanos y `type` para máquinas.

    Comprobación:

    ```bash
    curl -i localhost:8080/api/v1/equipos/999
    curl -i -H "Accept: application/xml" localhost:8080/api/v1/equipos     # 406
    curl -i -X POST localhost:8080/api/v1/equipos \
         -H "Content-Type: text/plain" -d 'hola'                           # 415
    curl -i -X PUT localhost:8080/api/v1/equipos                           # 405
    ```

    Esos cuatro códigos —`404`, `406`, `415`, `405`— son los que distinguen una API cuidada de una que «funciona».

---

# R2 · Que aguante datos de verdad

**Sesiones S9–S15** · Resuelto

## La situación

El inventario tiene ahora 8.000 equipos. `GET /api/v1/equipos` devuelve un JSON de once megas, el navegador se queda pensando y el servidor también.

## La pregunta

> **Haced que el listado siga siendo útil con ocho mil registros, sin romper a los clientes que ya lo usan.**

## Restricciones

- Los parámetros de paginación **no se inventan**: se usa el convenio de Spring (`page`, `size`, `sort`).
- Hay que poder **combinar** filtros, no elegir uno solo.
- La respuesta paginada lleva **metadatos**: total, página actual, si hay más.
- Un cliente que pida la página 900.000 recibe una respuesta vacía, **no un error**.

## Criterios de aceptación

- [ ] `GET /equipos?page=0&size=20` devuelve 20 elementos y los metadatos.
- [ ] `?sort=nombre,asc` y `?sort=precio,desc` funcionan.
- [ ] `?tipo=portatil&estado=DISPONIBLE` combina los dos filtros.
- [ ] El tamaño de página tiene un **máximo** y pedir 10.000 no lo salta.
- [ ] Hay un `ETag` en la respuesta y una segunda petición con `If-None-Match` devuelve `304`.

??? success "Solución · paginación, orden y filtros combinados"

    ```java
    @GetMapping
    public PaginaDto<EquipoDto> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Equipo.Estado estado,
            @RequestParam(required = false) String buscar,
            @PageableDefault(size = 20, sort = "etiqueta")           // (1)
            Pageable pageable) {

        var pagina = servicio.buscar(tipo, estado, buscar, pageable);
        return PaginaDto.de(pagina.map(mapeador::aDto));
    }
    ```

    1.  `Pageable` lo construye Spring leyendo `page`, `size` y `sort`. No hay que parsear nada, y `@PageableDefault` pone los valores por defecto.

    **El tope, que es obligatorio:**

    ```yaml
    spring.data.web.pageable:
      default-page-size: 20
      max-page-size: 100          # (1)
      one-indexed-parameters: false
    ```

    1.  Sin esto, `?size=1000000` te trae la tabla entera y tumba el servidor. Es un fallo de disponibilidad, no una molestia.

    **Filtros combinables sin escribir cuatro métodos**, con `Specification`:

    ```java
    public Page<Equipo> buscar(String tipo, Equipo.Estado estado,
                               String texto, Pageable pageable) {

        Specification<Equipo> spec = Specification.where(null);      // (1)

        if (tipo != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("tipo"), tipo));
        }
        if (estado != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("estado"), estado));
        }
        if (texto != null && !texto.isBlank()) {
            spec = spec.and((r, q, cb) -> cb.like(
                    cb.lower(r.get("nombre")), "%" + texto.toLowerCase() + "%"));
        }
        return repositorio.findAll(spec, pageable);
    }
    ```

    1.  Se empieza con una especificación vacía y se van encadenando solo las que llegan. Con tres filtros opcionales hay **ocho** combinaciones posibles: escribir ocho métodos `findByTipoAndEstado…` no es una opción.

    **La respuesta, con metadatos propios y no el `Page` de Spring:**

    ```java
    public record PaginaDto<T>(List<T> contenido, int pagina, int tamano,
                               long totalElementos, int totalPaginas, boolean ultima) {

        public static <T> PaginaDto<T> de(Page<T> p) {
            return new PaginaDto<>(p.getContent(), p.getNumber(), p.getSize(),
                    p.getTotalElements(), p.getTotalPages(), p.isLast());
        }
    }
    ```

    **Por qué no devolver el `Page` directamente:** su JSON incluye la estructura interna de Spring (`pageable`, `sort.sorted`, `sort.unsorted`…), que es ruido para el cliente y **cambia entre versiones de Spring**. Estarías publicando un detalle de implementación como contrato.

??? success "Solución · caché con ETag y tests de la API"

    ```java
    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> etag() {   // (1)
        var registro = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registro.addUrlPatterns("/api/v1/*");
        return registro;
    }
    ```

    1.  Calcula el `ETag` como hash del cuerpo. Es *shallow*: el servidor hace igual el trabajo, y lo que se ahorra es la **descarga**. Para ahorrarse el trabajo hace falta un `ETag` calculado a partir de la fecha de modificación.

    ```bash
    curl -i localhost:8080/api/v1/equipos | grep -i etag
    curl -i -H 'If-None-Match: "0a1b2c..."' localhost:8080/api/v1/equipos
    # → 304 Not Modified, sin cuerpo
    ```

    **Los tests de la API**, que son lo que se evalúa:

    ```java
    @WebMvcTest(EquipoControlador.class)
    class EquipoControladorTest {

        @Autowired MockMvc mockMvc;
        @MockitoBean EquipoServicio servicio;

        @Test
        void devuelveLosMetadatosDePaginacion() throws Exception {
            given(servicio.buscar(any(), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(unEquipo()),
                                PageRequest.of(0, 20), 8000));

            mockMvc.perform(get("/api/v1/equipos?page=0&size=20"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.totalElementos").value(8000))
                   .andExpect(jsonPath("$.contenido", hasSize(1)))
                   .andExpect(jsonPath("$.ultima").value(false));
        }

        @Test
        void devuelve406SiPideXml() throws Exception {
            mockMvc.perform(get("/api/v1/equipos").accept(MediaType.APPLICATION_XML))
                   .andExpect(status().isNotAcceptable());
        }
    }
    ```

    Y uno de extremo a extremo, para comprobar que las piezas encajan:

    ```java
    @SpringBootTest(webEnvironment = RANDOM_PORT)
    class EquipoApiIT {

        @Autowired TestRestTemplate cliente;

        @Test
        void elFlujoCompletoFunciona() {
            var respuesta = cliente.postForEntity("/api/v1/equipos",
                    new CrearEquipoDto("PC-900", "portátil",
                                       Equipo.Estado.DISPONIBLE, "Aula 1"),
                    EquipoDto.class);

            assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(respuesta.getHeaders().getLocation()).isNotNull();    // (1)

            var creado = cliente.getForObject(
                    respuesta.getHeaders().getLocation(), EquipoDto.class);
            assertThat(creado.etiqueta()).isEqualTo("PC-900");
        }
    }
    ```

    1.  **El test sigue la cabecera `Location`** en lugar de construir la URL. Así comprueba de paso que esa cabecera existe y apunta a donde debe — que es justo lo que se te olvida poner.

---

# R3 · La API de incidencias

**Sesiones S16–S18** · :material-pencil: **A entregar**

!!! reto "Este no lleva solución"
    Mismo tamaño y mismos criterios que el **examen práctico de la UT6**.

## La situación

El departamento de informática del instituto recibe las averías por correo, por WhatsApp y por los pasillos. No hay forma de saber cuántas hay abiertas ni quién las está atendiendo.

## La pregunta

> **Construid la API de incidencias, documentada y paginada, de forma que otro equipo pueda escribir la aplicación cliente sin preguntaros nada.**

## Lo que hay que entregar

| Recurso | Operaciones |
|---|---|
| **Incidencia** | Crear, listar con filtros y paginación, ver una, asignar técnico, cerrar |
| **Comentario** | Añadir a una incidencia, listar los de una incidencia |

Estados: `ABIERTA` → `ASIGNADA` → `RESUELTA` → `CERRADA`. Y `RECHAZADA` desde cualquiera salvo `CERRADA`.

## Restricciones

- Rutas en plural, versionadas, con los filtros en la consulta.
- Paginación con `page`, `size` y `sort`, y **tope de tamaño**.
- Errores en `ProblemDetail`, con `type` estable.
- **Ni una traza** en ninguna respuesta.
- OpenAPI generado del código, con los errores documentados.
- Una transición no válida es **`409`**, no `400`.

## Criterios de aceptación

| # | Qué | Peso |
|:-:|---|:-:|
| **1** | Diseño REST correcto: plural, versión, filtros en la consulta, códigos exactos | 1,5 |
| **2** | Listado **paginado y ordenable**, con metadatos y tope de tamaño | 2,0 |
| **3** | Filtros **combinables**: por estado, por técnico y por texto | 1,5 |
| **4** | Errores en `ProblemDetail`, incluido `406` y `415`, sin trazas | 1,5 |
| **5** | OpenAPI navegable con ejemplos y respuestas de error documentadas | 1,5 |
| **6** | Al menos **cuatro tests**: paginación, filtro, un `409` y uno de extremo a extremo | 2,0 |

## Cómo sabrás que está bien

```bash
# 1 · crear
curl -i -X POST localhost:8080/api/v1/incidencias \
     -H "Content-Type: application/json" \
     -d '{"titulo":"Proyector del aula 3 sin señal","aula":"3","prioridad":"ALTA"}'
# → 201 + Location

# 2 · listar filtrando y paginando
curl -s "localhost:8080/api/v1/incidencias?estado=ABIERTA&page=0&size=5&sort=creada,desc" | jq
# → 5 elementos + totalElementos, totalPaginas, ultima

# 3 · combinar filtros
curl -s "localhost:8080/api/v1/incidencias?estado=ASIGNADA&tecnico=jlopez&buscar=proyector" | jq length

# 4 · transición imposible
curl -i -X POST localhost:8080/api/v1/incidencias/1/cierre     # sobre una ya CERRADA
# → 409 con type y detail

# 5 · formato no admitido
curl -i -H "Accept: application/xml" localhost:8080/api/v1/incidencias
# → 406

# 6 · el tope
curl -s "localhost:8080/api/v1/incidencias?size=100000" | jq '.contenido | length'
# → como mucho el máximo configurado
```

!!! tip "Las tres trampas"
    **Cerrar no es un `PUT`.** Cambiar de estado tiene reglas propias: o `PATCH` con el estado, o un subrecurso `/cierre`. Un `PUT` sobre la incidencia entera deja que el cliente cambie el título de paso.

    **El `sort` por un campo que no existe.** `?sort=inventado,asc` lanza una `PropertyReferenceException` que acaba en `500`. Hay que validar los campos admitidos y devolver `400`.

    **El filtro de texto y el índice.** `like '%texto%'` no usa índice y con muchas filas se arrastra. No hace falta resolverlo, pero **hay que saber decirlo** si se pregunta.

## Cómo se entrega

Un `.zip` sin `target`, o el repositorio de Classroom. Antes de entregar:

- [ ] `mvn test` en verde. **Si no compila, es un cero.**
- [ ] Las seis órdenes de arriba dan lo que dicen.
- [ ] `/swagger-ui.html` abre y se puede lanzar una petición desde ahí.
- [ ] Ninguna respuesta contiene la palabra `at es.iesx`.
