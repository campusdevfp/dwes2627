# Chuleta REST + Security (UT6)

## Diseño de URLs

```http
GET    /api/v1/productos                  200
GET    /api/v1/productos/42               200 · 404
POST   /api/v1/productos                  201 + Location
PUT    /api/v1/productos/42               200 (reemplazo completo)
PATCH  /api/v1/productos/42               200 (parcial)
DELETE /api/v1/productos/42               204
GET    /api/v1/clientes/7/pedidos         subrecurso
POST   /api/v1/libros/42/prestamo         acción no-CRUD
```

Sustantivos en plural · minúsculas con guiones · sin verbos · versión en la URL · máximo 2 niveles de anidación.

## ResponseEntity

```java
ResponseEntity.ok(dto);
ResponseEntity.created(uri).body(dto);          // 201
ResponseEntity.noContent().build();             // 204
ResponseEntity.status(HttpStatus.CONFLICT).body(x);

var location = ServletUriComponentsBuilder.fromCurrentRequest()
    .path("/{id}").buildAndExpand(creado.id()).toUri();
```

## Códigos

| Código | Cuándo |
|---|---|
| 200 / 201 / 204 | OK / creado / sin contenido |
| 400 | validación, JSON malformado |
| 401 | sin token o inválido |
| 403 | autenticado sin permiso |
| 404 | no existe |
| 405 | método no permitido |
| 409 | conflicto (duplicado, sin stock) |
| 429 | demasiadas peticiones |
| 500 / 502 / 503 | error interno / fallo de servicio externo |

## Problem Details

```java
var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
pd.setTitle("Producto no encontrado");
pd.setType(URI.create("https://api.tienda.com/errores/no-encontrado"));
pd.setInstance(URI.create(req.getRequestURI()));
pd.setProperty("timestamp", Instant.now());
```

## Paginación, orden y filtros

```java
@GetMapping
public Page<Dto> buscar(
    @RequestParam(required = false) String categoria,
    @RequestParam(required = false) Double precioMax,
    @PageableDefault(size = 20, sort = "nombre") Pageable pageable) { }
```

```
?page=0&size=10&sort=precio,desc&sort=nombre,asc&categoria=movilidad
```

Filtro opcional:.filter(p -> categoria == null || p.categoria().equals(categoria)) Respuesta: content, `totalElements`, `totalPages`, number, size, first, last.
:material-alert:

page empieza en 0. Página inexistente → 200 con content vacío, no 404.

## OpenAPI

```
springdoc-openapi-starter-webmvc-ui
```

`/swagger-ui.html` · `/v3/api-docs`

```
@Tag(name="Productos") @Operation(summary="...") @ApiResponses({...})
@Parameter(description="...", example="42")
@Schema(description="...", example="Patinete")
```

## GraphQL

```graphql title="src/main/resources/graphql/schema.graphqls"
type Query { libros: [Libro!]!  libro(id: ID!): Libro }
type Mutation { crearLibro(entrada: LibroEntrada!): Libro! }
type Libro { id: ID!  titulo: String!  autor: Autor }
```
`src/main/resources/graphql/schema.graphqls`

```java
@Controller
public class LibroGraphQlControlador {
    @QueryMapping    public List<LibroDto> libros()                  { … }
    @QueryMapping    public LibroDto libro(@Argument Long id)        { … }
    @MutationMapping public LibroDto crearLibro(@Argument LibroEntrada entrada) { … }
    @SchemaMapping   public Autor autor(Libro libro)                 { … }   // OJO N+1
    @BatchMapping    public Map<Libro, Autor> autor(List<Libro> libros) { … } // BIEN
}
```
```yaml
spring.graphql.graphiql.enabled: true      # playground en /graphiql
```

## Tiempo real

```java
@Configuration @EnableWebSocketMessageBroker
class WsConfig implements WebSocketMessageBrokerConfigurer {
    public void registerStompEndpoints(StompEndpointRegistry r) { r.addEndpoint("/ws").withSockJS(); }
    public void configureMessageBroker(MessageBrokerRegistry r) {
        r.enableSimpleBroker("/tema");
        r.setApplicationDestinationPrefixes("/app");
    }
}
ws.convertAndSend("/tema/prestamos", dto);      // SimpMessagingTemplate
```

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter suscribirse() { … }
```

| | SSE | WebSocket |
|---|---|---|
| Sentido | servidor → cliente | los dos |
| Reconexión | automática | la programas tú |
| Cuándo | notificaciones, paneles | chat, colaboración |

## Tests

```java
@WebMvcTest(ProductoControlador.class)
@Autowired MockMvc mockMvc;  @MockitoBean ProductoServicio servicio;

mockMvc.perform(get("/api/v1/productos/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.nombre").value("Patinete"))
    .andExpect(jsonPath("$.content.length()").value(2))
    .andExpect(header().exists("Location"));

@WithMockUser(roles = "ADMIN")     // simula usuario autenticado
@SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate   // integración
```


## Idempotencia y verbos

| Verbo | Seguro | Idempotente | Notas |
|---|---|---|---|
| GET | :material-check: | :material-check: | Nunca debe modificar nada |
| POST | :material-close: | :material-close: | Crea uno nuevo cada vez |
| PUT | :material-close: | :material-check: | Reemplazo **completo** |
| PATCH | :material-close: | :material-close:* | Idempotente con valores absolutos |
| DELETE | :material-close: | :material-check: | 204 la 1.ª vez, 404 después; mismo **estado** |

Reintentar solo lo idempotente. Para POST críticos: cabecera `Idempotency-Key`.

## Versionado

`/api/v1/...` en el `@RequestMapping` de la clase.
**No rompe** (misma versión): añadir campo, añadir endpoint, añadir parámetro opcional.
**Sí rompe** (nueva versión): quitar/renombrar campo, cambiar tipo, hacer obligatorio un parámetro, cambiar un código.

## Consumir APIs

```java
@Bean RestClient client(RestClient.Builder b) {
    var f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(Duration.ofSeconds(3));      // OJO SIEMPRE timeouts
    f.setReadTimeout(Duration.ofSeconds(5));
    return b.baseUrl("https://api.ejemplo.com").requestFactory(f).build();
}

client.get().uri("/users/{id}", id)                  // parametrizado, no concatenado
      .retrieve()
      .onStatus(s -> s.value() == 404, (rq, rs) -> { throw new NoEncontradoException(); })
      .onStatus(HttpStatusCode::is5xxServerError, (rq, rs) -> { throw new ExternoException(); })
      .body(MiDto.class);
```

`@JsonIgnoreProperties(ignoreUnknown = true)` en los DTO externos · error del proveedor → `502`, nunca 500 · `@Cacheable` para no agotar el límite de peticiones.

## Errores frecuentes que cuestan puntos

1. POST que devuelve 200 sin Location → debe ser 201 + Location.
2. @RequestBody sin `@Valid` → no se valida nada.
3. .anyRequest().permitAll()
    arriba de la cadena → API abierta.
4. roles = "ROLE_ADMIN" en `@WithMockUser` → genera `ROLE_ROLE_ADMIN` → 403.
5. @WebMvcTest sin `@Import(SecurityConfig.class)` → los tests de 401/403 no prueban nada.
6. ?page=99 devolviendo 404 → debe ser 200 con `content: []`.
7. Ordenar por cualquier campo que mande el cliente → lista blanca.
8. Traza de la excepción en la respuesta → al log, nunca al cliente.
