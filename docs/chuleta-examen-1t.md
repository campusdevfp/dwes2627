# Chuleta de los exámenes prácticos — 1.er trimestre

**Esta es exactamente la hoja que se te entrega impresa el día del examen.** No hay otra: ni apuntes, ni internet, ni asistentes.

Está publicada desde el primer día para que **entrenes con ella**. Trabaja los ejercicios teniéndola delante: así el día del examen no pierdes tiempo buscando dónde está cada cosa.

!!! danger "Lo que NO vas a encontrar aquí, y es deliberado"
    Esta chuleta trae **sintaxis**, no **criterio**. Te recuerda cómo se escribe algo; no te dice cuándo usarlo.

    En concreto, no está y no lo estará:

    - El orden correcto de las comprobaciones (¿404 antes o después del 409?).
    - Cuándo un error es de campo y cuándo es global.
    - Cómo se detecta y se arregla un N+1.
    - Qué colección elegir para cada problema.
    - Dónde va cada regla de negocio.

    **Eso es justo lo que se evalúa.** Si viniera en la hoja, el examen no mediría nada.

---

# RA3 · Java, ficheros y colecciones

## Leer un fichero

```java
try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
    return lineas.skip(1).filter(l -> !l.isBlank()).map(this::aObjeto).toList();
}
```
`linea.split(";", -1)` · el `-1` conserva los campos vacíos del final
`Charset.forName("ISO-8859-1")` si vienen acentos rotos

## Escribir

```java
Files.writeString(ruta, texto, StandardCharsets.UTF_8);
Files.write(ruta, lineas, CREATE, TRUNCATE_EXISTING);
Files.createDirectories(ruta.getParent());
Files.move(tmp, destino, REPLACE_EXISTING, ATOMIC_MOVE);   // escritura atómica
```

## Streams

```java
.filter(p -> p.precio() > 10)      .map(Producto::nombre)
.sorted(comparing(P::nombre))      .sorted(comparingDouble(P::precio).reversed())
.sorted(comparing(P::categoria).reversed().thenComparing(P::nombre))  // reversed va pegado
.distinct()  .limit(10)  .skip(5)  .count()  .toList()
.anyMatch(...)  .allMatch(...)  .noneMatch(...)  .findFirst()   // Optional
.mapToDouble(P::precio).sum()      .max(comparingDouble(P::precio))   // Optional
```

## Collectors

```java
groupingBy(P::categoria)
groupingBy(P::categoria, counting())
groupingBy(P::categoria, TreeMap::new, counting())              // claves ordenadas
groupingBy(P::categoria, mapping(P::nombre, toList()))
groupingBy(P::categoria, TreeMap::new, summingDouble(P::precio))
joining(", ")   joining(", ", "[", "]")
averagingDouble(P::precio)    summingInt(P::stock)
```

## Números y fechas

```java
new BigDecimal("38.00")                 // SIEMPRE con String
b.add(o) · subtract · multiply · compareTo(o) == 0 · signum()
b.divide(o, 2, RoundingMode.HALF_UP)
BigDecimal.valueOf(entero)              texto.replace(',', '.')

LocalDate.parse("2026-07-10")   LocalTime.parse("22:00")   LocalDateTime · Instant
fecha.plusDays(10)                      // devuelve otra: NO modifica
fecha.isBefore(o) · isAfter(o)          Duration.between(h1, h2).toMinutes()
fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
```

## Jackson

```java
var mapper = JsonMapper.builder()
    .addModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)   // fechas ISO-8601
    .serializationInclusion(JsonInclude.Include.NON_NULL)      // sin nulos
    .build();

mapper.writeValue(fichero, objeto);
mapper.writerWithDefaultPrettyPrinter().writeValue(f, o);
mapper.readValue(fichero, Producto.class);
mapper.readValue(json, new TypeReference<List<Producto>>() {});
```
`@JsonIgnoreProperties(ignoreUnknown = true)` · `@JsonProperty("otro_nombre")` · `@JsonFormat(pattern="dd/MM/yyyy")`

## Validación en Java puro

```java
record Producto(String nombre, BigDecimal precio) {
    Producto {                                     // constructor compacto
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("...");
    }
}
Objects.requireNonNull(x, "mensaje");
Optional: .map() .filter() .orElse() .orElseGet() .orElseThrow() .ifPresent() .isEmpty()
```

---

# RA5 · Spring Boot, capas y REST

## Estereotipos e inyección

```java
@RestController @RequestMapping("/api/v1/productos")
public class ProductoControlador {
    private final ProductoServicio servicio;
    public ProductoControlador(ProductoServicio servicio) { this.servicio = servicio; }
}
```
`@Service` · `@Repository` · `@Component` · `@Configuration` + `@Bean`
`@Value("${app.clave}")` · `@Qualifier("nombre")` · `@Primary` · `@Profile("dev")`

## Parámetros del controlador

```java
@GetMapping("/{id}")  public X uno(@PathVariable Long id)
@GetMapping           public X lista(@RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "0") int page)
@PostMapping          public X crear(@Valid @RequestBody CrearDto dto)
@PutMapping("/{id}")  @DeleteMapping("/{id}")  @PatchMapping("/{id}")
```

## Bean Validation

```java
@NotNull  @NotBlank  @NotEmpty
@Size(min = 2, max = 80)   @Min(0)  @Max(100)
@Positive  @PositiveOrZero  @Negative
@DecimalMin("0.01")  @Digits(integer = 8, fraction = 2)
@Email  @Pattern(regexp = "...")
@Past  @Future  @PastOrPresent  @FutureOrPresent
@Valid  // en @RequestBody y en objetos anidados
```
Mensaje propio: `@NotBlank(message = "El nombre es obligatorio")`

## Excepciones de dominio

```java
public abstract class FunkoException extends RuntimeException {
    protected FunkoException(String m) { super(m); }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FunkoNotFoundException extends FunkoException {
    public FunkoNotFoundException(Long id) { super("No existe el funko con id: " + id); }
}
@ResponseStatus(HttpStatus.CONFLICT)    public class FunkoConflictException   extends FunkoException { … }
@ResponseStatus(HttpStatus.BAD_REQUEST) public class FunkoBadRequestException extends FunkoException { … }
```
```java
// En el servicio: lanza, SIN mencionar HTTP
repositorio.findById(id).orElseThrow(() -> new FunkoNotFoundException(id));
```
:material-alert: Con `@ResponseStatus` **no hace falta handler**: Spring las traduce solas.
:material-alert: Nunca `ResponseStatusException` en el servicio: mete HTTP en la lógica y no se puede reutilizar desde GraphQL ni WebSocket.

## Errores centralizados

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return Map.of("status", 400, "error", "Datos inválidos", "campos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)        // JSON roto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)    // /funkos/abc
}
```

## Caché

```java
@Service @CacheConfig(cacheNames = {"funkos"})
public class S {
    @Cacheable(key = "#id")        public R findById(Long id) { … }
    @CacheEvict(allEntries = true) public R save(C dto)       { … }
    @CachePut(key = "#result.id")  public R update(…)         { … }
}
```
:material-alert: Sin `@EnableCaching` en la clase principal, **no hace nada**.
:material-alert: Todo método que **escribe** necesita `@CacheEvict(allEntries = true)`.
:material-alert: Una llamada interna (`this.findById(...)`) **no pasa por la caché**: el proxy solo intercepta lo que entra de fuera.

## Configuración

```yaml title="src/main/resources/application.yml"
server.port: 8080
spring:
  application.name: tienda
  profiles.active: dev
app:
  clave: valor
```

---

# RA6 · Spring Data con JPA y SQL

## Entidad

```java
@Entity
@Table(name = "producto",
       uniqueConstraints = @UniqueConstraint(columnNames = {"codigo"}))
public class Producto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Version private Long version;          // bloqueo optimista

    protected Producto() { }                // OBLIGATORIO para JPA
}
```
`@Transient` · `@Lob` · `@Embedded` / `@Embeddable`

## Relaciones

```java
@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Linea> lineas = new ArrayList<>();

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "pedido_id")
private Pedido pedido;

@ManyToMany  @JoinTable(name = "...", joinColumns = ..., inverseJoinColumns = ...)
```

## Repositorio

```java
public interface ProductoRepositorio extends JpaRepository<Producto, Long> { }
```
```
findBy · existsBy · countBy · deleteBy
And · Or · Not · Between · LessThan · GreaterThan · LessThanEqual
Containing · StartingWith · EndingWith · IgnoreCase
IsNull · IsNotNull · True · False · In · OrderByXAsc / Desc
```
```java
List<P> findByCategoriaAndPrecioLessThan(Categoria c, BigDecimal p);
Optional<P> findByCodigo(String codigo);
Page<P> findByNombreContainingIgnoreCase(String t, Pageable pageable);
```

## Consultas escritas

```java
@Query("select p from Producto p where p.precio between :min and :max")
List<P> enRango(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

@Query("select p.categoria as categoria, count(p) as total from Producto p group by p.categoria")
List<Resumen> resumen();                       // proyección por interfaz

@Query(value = "select * from producto", nativeQuery = true)

@Modifying @Transactional
@Query("update Producto p set p.activo = false where p.id = :id")
```
JPQL: `join fetch` · `left join` · `count()` `sum()` `avg()` `min()` `max()` · `group by` · `having`

```java
@EntityGraph(attributePaths = {"cliente", "lineas"})
List<Pedido> findByEstado(Estado e);
```

## Transacciones

```java
@Transactional                       // en el SERVICIO
@Transactional(readOnly = true)
@Transactional(rollbackFor = Exception.class)
@Transactional(propagation = Propagation.REQUIRES_NEW)
```

## Configuración

```yaml title="src/main/resources/application.yml"
spring:
  datasource: { url: "jdbc:h2:file:./datos/app", username: sa, password: "" }
  jpa:
    hibernate.ddl-auto: update          # validate en producción
    show-sql: true
    open-in-view: false
  h2.console.enabled: true
```

## Pruebas

```java
@DataJpaTest                      // solo la capa de datos, con rollback
@SpringBootTest                   // la aplicación entera
@WebMvcTest(X.class)              // solo la capa web
@MockitoBean ProductoServicio servicio;
```

---

# RA7 · Servicios web: WebSockets, GraphQL y docs

## Códigos

| | |
|---|---|
| **200** OK · **201** Created + `Location` · **204** No Content | Bien |
| **400** Bad Request · **401** Unauthorized · **403** Forbidden | Cliente |
| **404** Not Found · **409** Conflict · **422** Unprocessable | Cliente |
| **500** Internal · **503** Unavailable | Servidor |

## ResponseEntity

```java
return ResponseEntity.ok(dto);
return ResponseEntity.created(URI.create("/api/v1/productos/" + dto.id())).body(dto);
return ResponseEntity.noContent().build();
return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=informe.csv").body(bytes);
```

## Paginación

```java
@GetMapping
public Page<ProductoDto> listar(@PageableDefault(size = 20, sort = "nombre") Pageable p) {
    return servicio.listar(p);
}
```
`?page=0&size=20&sort=precio,desc` · `pagina.getContent()` `getTotalElements()` `getTotalPages()` `getNumber()` `isFirst()` `isLast()`

```java
PageRequest.of(0, 20, Sort.by("precio").descending());
```

## Sin tildes

```java
static String normalizar(String s) {
    return Normalizer.normalize(s, Normalizer.Form.NFD)
                     .replaceAll("\\p{M}", "").toLowerCase();
}
```

## OpenAPI

```java
@OpenAPIDefinition(info = @Info(title = "API", version = "1.0"))
@Tag(name = "Productos")
@Operation(summary = "Crea un producto")
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
})
@Parameter(description = "Identificador")
```
```yaml
springdoc.swagger-ui.path: /swagger-ui.html
```

## Consumir otra API

```java
var cliente = RestClient.builder().baseUrl("https://...").build();
var r = cliente.get().uri("/x/{id}", id).retrieve().body(Respuesta.class);
```

## MockMvc

```java
mvc.perform(get("/api/v1/productos/1"))
   .andExpect(status().isOk())
   .andExpect(jsonPath("$.nombre").value("Teclado"))
   .andExpect(jsonPath("$.content.length()").value(3));

mvc.perform(post("/api/v1/productos").contentType(APPLICATION_JSON).content(json))
   .andExpect(status().isCreated())
   .andExpect(header().exists("Location"));
```

---

# Comandos

```bash
mvn clean test                    mvn spring-boot:run
mvn test -Dtest=MiTest            mvn -q test
./mvnw clean test                 # si el proyecto trae wrapper

curl -s localhost:8080/api/v1/productos | jq
curl -i -X POST localhost:8080/api/v1/productos -H "Content-Type: application/json" \
     -d '{...}'
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/v1/productos/999
```

## AssertJ

```java
assertThat(x).isEqualTo(y) · isNotNull() · isEmpty() · isPresent() · hasSize(3)
assertThat(lista).extracting(P::nombre).containsExactly("a","b")
                 .containsExactlyInAnyOrder("b","a")
assertThat(b).isEqualByComparingTo(new BigDecimal("19.99"))   // NO isEqualTo
assertThatThrownBy(() -> x()).isInstanceOf(MiExcepcion.class).hasMessageContaining("...")
```

## WebSockets

```java
@Component
public class H extends TextWebSocketHandler {
    private final Set<WebSocketSession> ses = new CopyOnWriteArraySet<>();   // CONCURRENTE

    @Override public void afterConnectionEstablished(WebSocketSession s) { ses.add(s); }
    @Override public void afterConnectionClosed(WebSocketSession s, CloseStatus st) { ses.remove(s); }
    @Override public void handleTransportError(WebSocketSession s, Throwable e)     { ses.remove(s); }

    public void enviarATodos(String m) {
        for (var s : ses) {
            try { if (s.isOpen()) s.sendMessage(new TextMessage(m)); else ses.remove(s); }
            catch (IOException e) { ses.remove(s); }          // ← try DENTRO del bucle
        }
    }
}
```

```java
@Configuration @EnableWebSocket
public class WsConfig implements WebSocketConfigurer {
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry r) {
        r.addHandler(handler, "/ws/v1/funkos").setAllowedOrigins(origenes);   // NUNCA "*"
    }
}
```

```java
// En el servicio: 2 líneas
eventos.publishEvent(new FunkoCambiadoEvent(Tipo.CREATE, respuesta));

// En el notificador
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void alCambiar(FunkoCambiadoEvent e) { handler.enviarATodos(json(e)); }
```

:material-alert: Sin `AFTER_COMMIT`, un `rollback` deja el mensaje enviado: los clientes ven algo que no existe.
:material-alert: Se manda el **DTO**: fuera de la transacción, la entidad lanza `LazyInitializationException`.
:material-alert: Los WebSockets **no** están sujetos a la política del mismo origen → `setAllowedOrigins` es la única protección.

`101 Switching Protocols` · *polling* < **SSE** (reconexión automática) < **WebSocket** (bidireccional)

## GraphQL

```graphql
type Funko { id: ID!  nombre: String!  categoria: Categoria! }
input FunkoInput { nombre: String!  precio: Float!  categoria: String! }
type Query    { funkos(filtro: FunkoFiltro): [Funko!]!   funkoById(id: ID!): Funko }
type Mutation { crearFunko(input: FunkoInput!): Funko! }
type Subscription { funkoCambiado: Notificacion! }
```

`String` nulo · `String!` no nulo · `[String!]!` **ni lista ni elementos nulos** ← lo correcto

```java
@Controller
public class C {
    @QueryMapping    public List<R> funkos(@Argument Optional<F> filtro) { … }
    @MutationMapping public R crearFunko(@Argument("input") @Valid Req in) { … }
    @SchemaMapping(typeName = "Matricula", field = "aprobada")
                     public Boolean aprobada(MatriculaResponse m) { … }
    @BatchMapping(typeName = "Funko")     // ← evita el N+1: recibe la lista ENTERA
                     public Map<R, CR> categoria(List<R> funkos) { … }
    @SubscriptionMapping public Flux<N> funkoCambiado() { return emisor.asFlux(); }
}
```

```java
@Component
public class GqlErrors extends DataFetcherExceptionResolverAdapter {
    @Override protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        return switch (ex) {
            case FunkoNotFoundException e -> error(env, ErrorType.NOT_FOUND, e);
            case FunkoConflictException e -> error(env, ErrorType.BAD_REQUEST, e);
            default -> null;
        };
    }
}
```

```properties
spring.graphql.graphiql.enabled=true                 # /graphiql — SOLO dev
spring.graphql.schema.introspection.enabled=false    # false en PROD
```

:material-alert: **GraphQL devuelve 200 casi siempre.** Los errores van en `errors`, no en el código.
:material-alert: El **N+1** es su problema característico, y **no** se arregla con `JOIN FETCH`: hace falta `@BatchMapping`.
:material-alert: Todo por **`POST` a `/graphql`** → la caché HTTP no funciona.
:material-alert: Sin `MaxQueryDepthInstrumentation`, **el cliente decide el coste** de tu consulta.

## CORS

```java
registry.addMapping("/api/**")
        .allowedOrigins(origenes)                 // de application-{perfil}.properties
        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Location")               // sin esto el JS no lee el 201
        .allowCredentials(true)
        .maxAge(3600);
```

:material-alert: **Bloquea el NAVEGADOR, no tu servidor**: la petición llega, se procesa y se responde 200. Por eso funciona con `curl`.
:material-alert: El *preflight* (`OPTIONS`) lo dispara ya `Content-Type: application/json`.
:material-alert: `allowedOrigins("*")` + `allowCredentials(true)` → **el navegador lo rechaza**. Usa `allowedOriginPatterns`.
:material-alert: Sin `exposedHeaders`, el JS solo lee seis cabeceras, y `Location` no está entre ellas.

## OpenAPI

```java
@Operation(summary = "…", description = "…")
@ApiResponses({
  @ApiResponse(responseCode = "201", description = "Creado, con cabecera Location"),
  @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
  @ApiResponse(responseCode = "409", description = "Nombre repetido",  content = @Content)
})
@Parameter(description = "Id del funko", example = "1", required = true)
@Schema(description = "Nombre", example = "Mickey Mouse")
```

```properties
springdoc.api-docs.enabled=false        # PROD
springdoc.swagger-ui.enabled=false      # PROD
```

`/v3/api-docs` (el JSON) · `/swagger-ui/index.html` (la interfaz, **con botón de disparar**)

:material-alert: `springdoc` deduce rutas, tipos y obligatoriedad. **No puede deducir los 409 de negocio**, que es justo lo que necesita quien consume tu API.
:material-alert: Swagger UI es un **cliente HTTP completo**: en producción, apagado.
