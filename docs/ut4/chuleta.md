# Chuleta de la UT4 — Spring Boot y REST

Una página. **Es la que se puede imprimir y llevar al examen práctico.**

---

## Arranque

```java
@SpringBootApplication          // = @Configuration + @EnableAutoConfiguration + @ComponentScan
@EnableCaching                  // sin esto, @Cacheable NO hace nada
public class App {
    public static void main(String[] args) { SpringApplication.run(App.class, args); }
}
```

:material-alert: La clase va **en el paquete raíz**: `@ComponentScan` escanea desde ahí hacia abajo.

```bash
./mvnw spring-boot:run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw clean package && java -jar target/app-1.0.0.jar
java -jar -Dspring.profiles.active=prod app.jar
./mvnw spring-boot:run -Dspring-boot.run.arguments=--debug     # ver autoconfiguración
```

## Starters (Maven, sin versión)

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.6</version>
</parent>
<properties><java.version>25</java.version></properties>
```

| Starter | Trae |
|---|---|
| `spring-boot-starter-web` | Tomcat, `@RestController`, Jackson |
| `spring-boot-starter-validation` | `@Valid`, `@NotBlank`… |
| `spring-boot-starter-cache` | `@Cacheable`, `@CacheEvict` |
| `spring-boot-starter-test` | JUnit 5, Mockito, AssertJ, `MockMvc` |
| `spring-boot-devtools` | Reinicio al guardar (solo dev) |

:material-alert: El `spring-boot-maven-plugin` es el que hace el `.jar` ejecutable. Sin él, no arranca.

## Componentes

| Anotación | Capa | Nota |
|---|---|---|
| `@RestController` | Web | `= @Controller + @ResponseBody` |
| `@Service` | Negocio | |
| `@Repository` | Datos | Traduce `SQLException` → `DataAccessException` |
| `@Component` | Genérica | Mappers, utilidades |
| `@Configuration` + `@Bean` | Config | Para clases que **no son tuyas** |

```java
// Inyección por CONSTRUCTOR. Con un solo constructor, @Autowired sobra.
@RestController
public class C {
    private final S servicio;
    public C(S servicio) { this.servicio = servicio; }
}
```

**Scopes:** `singleton` (defecto), `prototype`, `request`, `session`.

:material-alert: Singleton = **una instancia para toda la app, en paralelo**. Los servicios **no guardan estado**.

## Rutas

```java
@RestController
@RequestMapping("${api.path:/api}/${api.version:v1}/funkos")
public class FunkosRestController {

    @GetMapping                       // GET  /api/v1/funkos
    @GetMapping("/{id}")              // GET  /api/v1/funkos/1
    @PostMapping                      // POST
    @PutMapping("/{id}")              // PUT      reemplaza TODO
    @PatchMapping("/{id}")            // PATCH    cambia solo lo enviado
    @DeleteMapping("/{id}")           // DELETE
}
```

| Anotación | De dónde saca el dato | Ejemplo |
|---|---|---|
| `@PathVariable` | Ruta | `/funkos/42` |
| `@RequestParam` | Query | `?categoria=disney` |
| `@RequestBody` | Cuerpo JSON | POST / PUT / PATCH |
| `@RequestHeader` | Cabecera | `Accept`, `Authorization` |

```java
@RequestParam(required = false) String q
@RequestParam(defaultValue = "0") int page
@RequestParam Optional<String> categoria          // ← preferido
@RequestParam List<Alergeno> sinAlergeno          // ?sinAlergeno=A&sinAlergeno=B
```

**Criterio:** identifica el recurso → `@PathVariable`. Filtra / ordena / pagina → `@RequestParam`.

## Respuestas

```java
ResponseEntity.ok(x);                                      // 200
ResponseEntity.created(URI.create("/api/v1/x/" + id)).body(x);  // 201 + Location
ResponseEntity.noContent().build();                        // 204
ResponseEntity.badRequest().body(e);                       // 400
ResponseEntity.notFound().build();                         // 404
ResponseEntity.status(HttpStatus.CONFLICT).body(e);        // 409
```

| | Éxito | No existe | Datos malos | Choca |
|---|:-:|:-:|:-:|:-:|
| GET lista | 200 | — (200 vacía) | — | — |
| GET uno | 200 | **404** | — | — |
| POST | **201** + `Location` | — | 400 | **409** |
| PUT / PATCH | 200 | **404** | 400 | 409 |
| DELETE | **204** | **404** | — | 409 |

:material-alert: Lista vacía = **200**, no 404. · DELETE de un id inexistente = **404**, no 204.

## DTOs

```java
// RESPUESTA: lo que sale
public record FunkoResponse(Long id, String nombre, Double precio,
                            LocalDateTime createdAt) {}

// CREAR (POST y PUT): SIN id ni fechas, con los obligatorios
public record FunkoCreateRequest(
        @NotBlank(message = "...") @Size(max = 100) String nombre,
        @NotNull @PositiveOrZero Double precio,
        @NotNull Categoria categoria) {}

// ACTUALIZAR (PATCH): sin @NotNull/@NotBlank, solo reglas de formato
public record FunkoUpdateRequest(
        @Size(max = 100) String nombre,
        @PositiveOrZero Double precio,
        Categoria categoria) {}
```

:material-alert: **Nunca devuelvas la entidad.** Filtra datos, desacopla el contrato de la tabla, y en la UT5 evita `StackOverflowError` y `LazyInitializationException`.

## Validación

```java
@PostMapping
public ResponseEntity<R> crear(@Valid @RequestBody CreateRequest dto) { … }
//                              ^^^^^^ sin esto NO SE VALIDA NADA, y no avisa
```

| | |
|---|---|
| `@NotNull` | No nulo |
| `@NotBlank` | No nulo + no solo espacios → **`String`** |
| `@NotEmpty` | No nulo + `length > 0` → **colecciones** |
| `@Size(min=, max=)` | Longitud / tamaño |
| `@Min` `@Max` | Enteros |
| `@Positive` `@PositiveOrZero` | `@Negative` `@NegativeOrZero` |
| `@DecimalMin` `@DecimalMax` `@Digits` | Decimales |
| `@Email` `@Pattern(regexp=)` | Texto |
| `@Past` `@PastOrPresent` `@Future` `@FutureOrPresent` | Fechas |
| `@AssertTrue` `@AssertFalse` | Booleanos y validación de clase |

**Dos niveles:**

| | Dónde | Código |
|---|---|:-:|
| **Formato** (campo aislado) | DTO, con anotaciones | 400 |
| **Negocio** (necesita BD u otros campos) | **Servicio** | 409 |

## Excepciones

```java
public abstract class FunkoException extends RuntimeException {
    protected FunkoException(String m) { super(m); }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FunkoNotFoundException extends FunkoException {
    public FunkoNotFoundException(Long id) { super("No existe el funko con id: " + id); }
}

@ResponseStatus(HttpStatus.CONFLICT)   public class FunkoConflictException   extends FunkoException { … }
@ResponseStatus(HttpStatus.BAD_REQUEST) public class FunkoBadRequestException extends FunkoException { … }
```

```java
// En el servicio: lanza, sin mencionar HTTP
repositorio.findById(id).orElseThrow(() -> new FunkoNotFoundException(id));
```

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return Map.of("status", 400, "campos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)       // JSON roto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)   // /funkos/abc
}
```

:material-alert: Las excepciones **con `@ResponseStatus` no necesitan handler**: Spring las traduce solas. El advice es para los errores de HTTP (validación, JSON ilegible, tipo malo).

**Alternativa rápida, pero mete HTTP en el servicio:**

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe");
```

## Caché

```java
@Service
@CacheConfig(cacheNames = {"funkos"})
public class S {

    @Cacheable(key = "#id")                     // si está, no ejecuta
    public R findById(Long id) { … }

    @CacheEvict(allEntries = true)              // invalida TODO al escribir
    public R save(C dto) { … }

    @CachePut(key = "#result.id")               // ejecuta y guarda
    public R update(…) { … }
}
```

:material-alert: **Tres trampas:**

1. Sin `@EnableCaching` en la clase principal, no hace nada.
2. Todo método que **escribe** necesita `@CacheEvict(allEntries = true)`, o `findAll()` devuelve datos viejos.
3. Una llamada **interna** (`this.findById(...)`) **no pasa por la caché**: el proxy solo intercepta lo que entra de fuera.

## Configuración

```properties
server.port=${PORT:8080}                   # variable, o 8080 si no existe
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
api.path=/api
api.version=v1
server.error.include-message=always        # never en prod
server.error.include-stacktrace=never      # SIEMPRE never en prod
logging.level.es.iesx=DEBUG
```

**Prioridad (gana el de arriba):**

```
1. --server.port=9000         (línea de comandos)
2. SERVER_PORT=9000           (variable de entorno)
3. application-{perfil}.properties
4. application.properties
5. defectos de Spring Boot
```

`server.port` → `SERVER_PORT` · `spring.datasource.url` → `SPRING_DATASOURCE_URL`

```java
@Value("${api.version:v1}") private String version;            // un valor

@ConfigurationProperties(prefix = "funkos")                     // un grupo
public record Props(Upload upload) {
    public record Upload(String directorio, long tamanoMaximo) {}
}
// + @EnableConfigurationProperties(Props.class) en la clase principal
```

```java
@Bean @Profile("dev")
public CommandLineRunner datos(S servicio) { return args -> { … }; }
```

| | dev | prod |
|---|---|---|
| `ddl-auto` | `create-drop` | **`validate`** |
| `include-stacktrace` | `on_param` | **`never`** |
| `h2.console.enabled` | `true` | **`false`** |
| `logging.level.root` | `DEBUG` | `WARN` |

**`.env`** → `.gitignore`. **`.env.example`** con las claves vacías → sí a Git.

## Tests

```java
// 1 · Servicio, sin Spring: milisegundos
@ExtendWith(MockitoExtension.class)
class STest {
    @Mock Repo repo;
    @Spy  Mapper mapper = new Mapper();
    @InjectMocks ServiceImpl servicio;

    @Test void noExisteLanza404() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicio.findById(99L));
        verify(repo, times(1)).findById(99L);
        verify(repo, never()).save(any());
    }
}
```

```java
// 2 · Capa web, con MockMvc: caza el @Valid olvidado
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean Service servicio;          // NO @MockBean: obsoleta desde 3.4

    @Test void postInvalidoDa400() throws Exception {
        mockMvc.perform(post("/api/v1/funkos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"nombre":"","precio":-1}"""))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.campos.nombre").exists());
    }
}
```

```java
status().isOk()  isCreated()  isNoContent()  isBadRequest()  isNotFound()  isConflict()
jsonPath("$.nombre").value("Casco")   jsonPath("$").isArray()   jsonPath("$", hasSize(3))
header().string("Location", "/api/v1/funkos/1")
```

## `curl`

```bash
curl -i localhost:8080/api/v1/funkos
curl -s "localhost:8080/api/v1/funkos?categoria=disney" | jq
curl -i -X POST localhost:8080/api/v1/funkos \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Casco","precio":35.0,"cantidad":1,"categoria":"OTROS"}'
curl -i -X PATCH localhost:8080/api/v1/funkos/1 \
     -H "Content-Type: application/json" -d '{"precio":40.0}'
curl -i -X DELETE localhost:8080/api/v1/funkos/1
```

:material-alert: Sin `-H "Content-Type: application/json"` → **415 Unsupported Media Type**.

---

## Los diez errores del examen

| | Síntoma | Causa |
|:-:|---|---|
| 1 | 500 en vez de 404 | `findById(id).get()` sobre un `Optional` vacío |
| 2 | 201 con datos basura | **`@Valid` olvidado** |
| 3 | `@Cacheable` no cachea | Falta `@EnableCaching` |
| 4 | Datos viejos en la lista | Falta `@CacheEvict(allEntries = true)` |
| 5 | 415 en el POST | Falta `Content-Type: application/json` |
| 6 | Bean no encontrado | Clase fuera del paquete escaneado, o sin `@Service` |
| 7 | `StackOverflowError` al serializar | Se devuelve la entidad con relación bidireccional |
| 8 | PATCH que borra campos | PUT y PATCH usando el mismo mapeador |
| 9 | 204 al borrar algo inexistente | El `delete` no comprueba antes |
| 10 | Contador que se comparte entre usuarios | Estado mutable en un bean singleton |
