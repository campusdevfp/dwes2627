# Chuleta de la UT4 — Spring Boot: capas, REST y JDBC

!!! tip ":material-file-pdf-box: Descárgala para el examen"
    **[:material-download: chuleta-ut4.pdf](../pdf/chuleta-ut4.pdf)** — lista para imprimir. Es la única documentación permitida en el examen práctico de RA5.

    También puedes generarla desde esta página con <kbd>Ctrl</kbd>+<kbd>P</kbd> → *Guardar como PDF*.

---

## 1 · Arranque y dependencias

```java
@SpringBootApplication          // = @Configuration + @EnableAutoConfiguration + @ComponentScan
@EnableCaching                  // sin esto, @Cacheable NO hace nada
public class App {
    public static void main(String[] args) { SpringApplication.run(App.class, args); }
}
```

:material-alert: La clase va **en el paquete raíz**: `@ComponentScan` escanea desde ahí hacia abajo.

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
| **`spring-boot-starter-jdbc`** | `DataSource`, HikariCP, **`JdbcClient`**, `@Transactional` |
| `com.h2database:h2` | La base de datos *(scope `runtime`)* |

:material-alert: `starter-jdbc`, **NO** `starter-data-jpa`: en la UT4 el SQL lo escribes tú. JPA es la UT5.
:material-alert: El `spring-boot-maven-plugin` es el que hace el `.jar` ejecutable.

```bash
./mvnw spring-boot:run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw clean package && java -jar target/app-1.0.0.jar
./mvnw spring-boot:run -Dspring-boot.run.arguments=--debug     # ver autoconfiguración
```

## 2 · Componentes e inyección

| Anotación | Capa | Nota |
|---|---|---|
| `@RestController` | Web | `= @Controller + @ResponseBody` |
| `@Service` | Negocio | |
| `@Repository` | Datos | Traduce `SQLException` → `DataAccessException` |
| `@Component` | Genérica | Mappers, `RowMapper`, utilidades |
| `@Configuration` + `@Bean` | Config | Para clases que **no son tuyas** |

```java
@RestController
public class C {
    private final S servicio;                            // final
    public C(S servicio) { this.servicio = servicio; }   // constructor, sin @Autowired
}
```

**Scopes:** `singleton` (defecto) · `prototype` · `request` · `session`

:material-alert: Singleton = **una instancia para toda la app, en paralelo** → los servicios **no guardan estado**.

## 3 · Rutas, peticiones y respuestas

```java
@RestController
@RequestMapping("${api.path:/api}/${api.version:v1}/funkos")
public class FunkosRestController {
    @GetMapping                 // GET  /api/v1/funkos
    @GetMapping("/{id}")        // GET  /api/v1/funkos/1
    @PostMapping                // POST
    @PutMapping("/{id}")        // PUT    reemplaza TODO
    @PatchMapping("/{id}")      // PATCH  cambia solo lo enviado
    @DeleteMapping("/{id}")     // DELETE
}
```

| Anotación | De dónde | Ejemplo |
|---|---|---|
| `@PathVariable` | Ruta | `/funkos/42` |
| `@RequestParam` | Query | `?categoria=disney` |
| `@RequestBody` | Cuerpo JSON | POST / PUT / PATCH |
| `@RequestHeader` | Cabecera | `Accept`, `Authorization` |

```java
@RequestParam(required = false) String q
@RequestParam(defaultValue = "0") int page
@RequestParam Optional<String> categoria            // ← preferido
@RequestParam List<String> tags                     // ?tags=a&tags=b
```

**Criterio:** identifica el recurso → ruta. Filtra, ordena o pagina → query.
**Y la URL nombra el recurso, nunca la acción:** `/funkos/crear` está mal; el verbo HTTP ya lo dice.

```java
ResponseEntity.ok(x);                                            // 200
ResponseEntity.created(URI.create("/api/v1/x/" + id)).body(x);   // 201 + Location
ResponseEntity.noContent().build();                              // 204
ResponseEntity.badRequest().body(e);                             // 400
ResponseEntity.notFound().build();                               // 404
ResponseEntity.status(HttpStatus.CONFLICT).body(e);              // 409
```

| | Éxito | No existe | Datos malos | Choca |
|---|:-:|:-:|:-:|:-:|
| GET lista | 200 | — (200 vacía) | — | — |
| GET uno | 200 | **404** | — | — |
| POST | **201** + `Location` | — | 400 | **409** |
| PUT / PATCH | 200 | **404** | 400 | 409 |
| DELETE | **204** | **404** | — | 409 |

:material-alert: Lista vacía = **200**, no 404 · DELETE de un id inexistente = **404**, no 204
:material-alert: Sin `Content-Type: application/json` en el POST → **415**

## 4 · DTOs

```java
// RESPUESTA: lo que sale
public record FunkoResponse(Long id, String nombre, BigDecimal precio,
                            LocalDateTime createdAt) {}

// CREAR (POST y PUT): SIN id ni fechas, con los obligatorios
public record FunkoCreateRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @NotBlank @Pattern(regexp = "DISNEY|MARVEL|ANIME|OTROS") String categoria) {}

// ACTUALIZAR (PATCH): sin @NotNull/@NotBlank, solo reglas de formato
public record FunkoUpdateRequest(
        @Size(max = 100) String nombre,
        @DecimalMin("0.0") BigDecimal precio,
        @Pattern(regexp = "DISNEY|MARVEL|ANIME|OTROS") String categoria) {}
```

:material-alert: **Nunca devuelvas el modelo.** Filtra datos y desacopla el contrato de la tabla.
:material-alert: El DTO de creación **sin `id`**: si lo lleva, un cliente puede sobrescribir otro registro.
:material-alert: **Nunca aceptes del cliente lo que puedas calcular**: total, precio congelado, estado inicial, código correlativo.

## 5 · Validación

```java
@PostMapping
public ResponseEntity<R> crear(@Valid @RequestBody CreateRequest dto) { … }
//                              ^^^^^^ sin esto NO SE VALIDA NADA, y no avisa
```

| | |
|---|---|
| `@NotNull` | No nulo → números, fechas, booleanos |
| `@NotBlank` | No nulo + no solo espacios → **`String`** |
| `@NotEmpty` | No nulo + `size > 0` → **colecciones** |
| `@Size(min=, max=)` | Longitud o tamaño |
| `@Min` `@Max` | Enteros |
| `@DecimalMin` `@DecimalMax` `@Digits(integer=, fraction=)` | `BigDecimal` |
| `@Positive` `@PositiveOrZero` `@Negative` `@NegativeOrZero` | Numéricos |
| `@Email` `@Pattern(regexp=)` | Texto |
| `@Past` `@PastOrPresent` `@Future` `@FutureOrPresent` | Fechas |
| `@AssertTrue` `@AssertFalse` | Validación **de clase**: compara dos campos |
| **`@Valid`** | **En cascada**: objetos y listas anidadas |

```java
record PedidoRequest(
    @NotNull Long clienteId,
    @NotEmpty @Valid List<LineaRequest> lineas) {}   // ← sin @Valid, las líneas NO se validan
```

```java
// Validación de clase, para reglas que comparan dos campos
record ReservaRequest(@NotNull LocalTime desde, @NotNull LocalTime hasta) {
    @AssertTrue(message = "La hora de fin debe ser posterior")
    public boolean isRangoValido() {
        if (desde == null || hasta == null) return true;   // de eso ya se queja @NotNull
        return hasta.isAfter(desde);
    }
}
```

**Tres niveles, y se ponen los tres:**

| | Dónde | Qué comprueba | Código |
|---|---|---|:-:|
| **Formato** | DTO, anotaciones | Un campo aislado | **400** |
| **Negocio** | **Servicio** | Necesita consultar la BD u otros campos | **409** |
| **Integridad** | **Tabla**: `UNIQUE`, `CHECK`, `FK` | La única que no se puede saltar | 409 vía handler |

## 6 · Excepciones

```java
// Tres clases independientes: extienden RuntimeException DIRECTAMENTE
@ResponseStatus(HttpStatus.NOT_FOUND)
public class FunkoNotFoundException extends RuntimeException {
    public FunkoNotFoundException(Long id) { super("No existe el funko con id: " + id); }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class FunkoConflictException extends RuntimeException {
    public FunkoConflictException(String mensaje) { super(mensaje); }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FunkoBadRequestException extends RuntimeException {
    public FunkoBadRequestException(String mensaje) { super(mensaje); }
}
```

| La excepción | El código | Cuándo la lanza el servicio |
|---|:-:|---|
| `…NotFoundException` | **404** | El `id` no existe |
| `…ConflictException` | **409** | Una regla de negocio no se cumple |
| `…BadRequestException` | **400** | Un valor de entrada no es válido para el dominio |

```java
// En el servicio: lanza, SIN mencionar HTTP
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
        return Map.of("status", 400, "error", "Datos inválidos", "campos", campos);
    }

    // Un método más por cada uno de estos, con el mismo patrón de tres lineas:
    @ExceptionHandler(HttpMessageNotReadableException.class)         // JSON roto         → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)     // /funkos/abc       → 400
    @ExceptionHandler(DataIntegrityViolationException.class)         // UNIQUE, FK, CHECK → 409
}
```

:material-alert: Las excepciones **con `@ResponseStatus` no necesitan handler**: Spring las traduce solas.
:material-alert: **Nunca `ResponseStatusException` en el servicio**: mete HTTP en la lógica y el servicio deja de poder reutilizarse (UT6).

## 7 · Caché

La caché es **un mapa en memoria**: clave → objeto devuelto. Las lecturas lo rellenan, las escrituras lo vacían.

```java
@Service
@CacheConfig(cacheNames = {"funkos"})       // el nombre del mapa, una vez
public class S {

    // LECTURAS: si la clave esta, NO ejecutan el metodo
    @Cacheable                      public List<R> findAll()      { … }
    @Cacheable(key = "#id")         public R findById(Long id)    { … }

    // ESCRITURAS: vacian el mapa ENTERO. Todas, sin excepcion
    @CacheEvict(allEntries = true)  public R save(C dto)          { … }
    @CacheEvict(allEntries = true)  public R update(Long id, U d) { … }
    @CacheEvict(allEntries = true)  public void deleteById(Long i){ … }
    @CacheEvict(allEntries = true)  public R vender(Long i, V d)  { … }   // ← tambien
}
```

| | Qué hace |
|---|---|
| `@Cacheable(key = "#id")` | Si la clave `id` está guardada, la devuelve **sin ejecutar el método** |
| `@CacheEvict(allEntries = true)` | Ejecuta el método y **vacía el mapa entero** |
| `@CachePut` | Ejecuta **y** actualiza la caché con el resultado. Casi nunca hace falta |

**Se comprueba con el log:** pide el mismo recurso tres veces y solo la primera genera consulta; escribe, y la siguiente lectura vuelve a consultar.

:material-alert: Sin `@EnableCaching` en la clase principal, **no hace nada y no avisa**.
:material-alert: `allEntries = true` y no `key = "#id"`: un elemento nuevo invalida **todas** las listas guardadas.
:material-alert: El método de escritura que se olvida es **el de la operación de negocio** (`vender`, `prestar`, `cambiarEstado`), porque no se llama `save` ni `update`.
:material-alert: Una llamada **interna** (`this.findById(...)`) **no pasa por la caché**: el proxy solo intercepta lo que entra de fuera. Igual que `@Transactional`.

## 8 · H2 y JDBC con `JdbcClient`

```properties
### H2 en FICHERO: los datos sobreviven
spring.datasource.url=${DB_URL:jdbc:h2:file:./datos/funkos;AUTO_SERVER=TRUE}
spring.datasource.username=${DB_USER:sa}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=org.h2.Driver

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

spring.h2.console.enabled=true          # /h2-console — false en PROD
spring.h2.console.path=/h2-console
```

| URL | Dónde viven los datos |
|---|---|
| `jdbc:h2:mem:funkos` | En memoria: se borran al parar |
| **`jdbc:h2:file:./datos/funkos`** | **En fichero: sobreviven** |

:material-alert: `AUTO_SERVER=TRUE` permite que la consola de H2 y tu app abran la base a la vez. Sin él: `Database may be already in use`.
:material-alert: Con fichero, `schema.sql` se ejecuta **en cada arranque** → `CREATE TABLE IF NOT EXISTS` y `MERGE … KEY(id)` en vez de `INSERT`.
:material-alert: Si insertas con ids fijos: `ALTER TABLE t ALTER COLUMN id RESTART WITH n+1`, o el primer POST choca.

```java
// El modelo es un record: constructor, captadores, equals y toString gratis
public record Funko(Long id, String nombre, BigDecimal precio, Integer cantidad,
                    String categoria, LocalDateTime createdAt, LocalDateTime updatedAt) {}
```

```java
@Component
public class FunkoRowMapper implements RowMapper<Funko> {
    @Override public Funko mapRow(ResultSet rs, int n) throws SQLException {
        return new Funko(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("cantidad"),
                rs.getString("categoria"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
```

```java
@Repository
public class R {
    private final JdbcClient jdbc;
    public R(JdbcClient jdbc) { this.jdbc = jdbc; }

    // LISTA
    jdbc.sql("SELECT * FROM funkos ORDER BY id").query(rowMapper).list();

    // UNO: devuelve Optional
    jdbc.sql("SELECT * FROM funkos WHERE id = :id")
        .param("id", id).query(rowMapper).optional();

    // UN ESCALAR
    jdbc.sql("SELECT COUNT(*) FROM funkos WHERE nombre = :n")
        .param("n", nombre).query(Integer.class).single();

    // INSERT con id generado. El modelo es un record: NO hay setId
    var keys = new GeneratedKeyHolder();
    jdbc.sql("INSERT INTO funkos (nombre, precio) VALUES (:nombre, :precio)")
        .param("nombre", f.nombre()).param("precio", f.precio())
        .update(keys, "ID");                       // ← nombre de la columna, MAYUSCULAS
    Number id = (Number) keys.getKeys().get("ID");
    return findById(id.longValue()).orElseThrow();   // se relee con su id

    // UPDATE / DELETE: devuelven filas afectadas
    int filas = jdbc.sql("DELETE FROM funkos WHERE id = :id").param("id", id).update();

    // VARIOS PARÁMETROS DE GOLPE
    jdbc.sql(sql).params(mapaDeParametros).query(rowMapper).list();
}
```

**Filtros opcionales y combinables:**

```java
var sql = new StringBuilder("SELECT * FROM funkos WHERE 1 = 1");
var params = new HashMap<String, Object>();

categoria.ifPresent(c -> {
    sql.append(" AND UPPER(categoria) = UPPER(:categoria)");   // ✅ texto FIJO
    params.put("categoria", c);                                 // ✅ valor parametrizado
});
return jdbc.sql(sql.toString()).params(params).query(rowMapper).list();
```

:material-alert: **NUNCA** `sql.append(" AND categoria = '" + c + "'")` → inyección SQL.
:material-alert: El `WHERE 1 = 1` evita llevar la cuenta de cuál es el primer filtro.

**Comprobar y escribir en la MISMA sentencia (condición de carrera):**

```sql
UPDATE productos SET stock = stock - :cantidad
 WHERE id = :id AND stock >= :cantidad
```

```java
if (repositorio.descontarStock(id, cantidad) == 0)
    throw new StockInsuficienteException(...);      // afectó a 0 filas → no había
```

:material-alert: «Leer, comprobar con un `if`, y después escribir» deja hueco para que otra petición se cuele en medio y el stock quede negativo.

| | UT3, a mano | UT4, `JdbcClient` |
|---|---|---|
| Conexión | `DriverManager` + `try-with-resources` | **Spring**, de un pool |
| Parámetros | `ps.setString(1, …)` desde 1 | `:nombre` |
| `ResultSet` | `while (rs.next())` | `RowMapper` + `.list()` |
| Cero o uno | `rs.next() ? … : null` | **`.optional()`** |
| Id generado | `RETURN_GENERATED_KEYS` | `GeneratedKeyHolder` |
| Cerrar | A mano | **Spring** |

## 9 · `@Transactional`

```java
@Transactional(readOnly = true)              // lecturas
@Transactional                               // escrituras
@Transactional(rollbackFor = Exception.class)
```

**Va en el `@Service`, no en el repositorio.** Una operación de negocio que toca varias tablas o filas tiene que ser atómica: o todo o nada.

:material-alert: **Solo revierte con `RuntimeException`.** Una comprobada **confirma** la transacción a medias.
:material-alert: Si **capturas** la excepción dentro, **no hay rollback**: tiene que salir del método.
:material-alert: Funciona **por proxy**: `this.metodo()` no abre transacción.

## 10 · Configuración

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
@Value("${api.version:v1}") private String version;             // un valor

@ConfigurationProperties(prefix = "funkos")                      // un grupo
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
| `h2.console.enabled` | `true` | **`false`** |
| `include-stacktrace` | `on_param` | **`never`** |
| `logging.level.root` | `DEBUG` | `WARN` |
| URL de H2 | fichero local | variable de entorno |

**`.env`** → `.gitignore`. **`.env.example`** con las claves vacías → sí a Git.
**IntelliJ:** *Edit Configurations → Environment variables*, o el plugin **EnvFile** apuntando al `.env`.

## 11 · `curl`

```bash
curl -i localhost:8080/api/v1/funkos
curl -s "localhost:8080/api/v1/funkos?categoria=disney&precioMax=20" | jq
curl -i -X POST localhost:8080/api/v1/funkos \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Casco","precio":35.00,"cantidad":1,"categoria":"OTROS"}'
curl -i -X PATCH localhost:8080/api/v1/funkos/1 \
     -H "Content-Type: application/json" -d '{"precio":40.00}'
curl -i -X DELETE localhost:8080/api/v1/funkos/1
```

## 12 · En el examen te dan esto hecho

| Entregado | Lo escribes tú |
|---|---|
| `pom.xml`, los `.properties`, `schema.sql`, `data.sql` y la clase de arranque | Los paquetes **por capas** |
| **El repositorio**: interfaz, implementación con `JdbcClient` y `RowMapper` | **El modelo** (`record`) |
| **El mapeador**: `toResponse`, `toModel` y `parchear` | **Los DTOs** con sus validaciones |
| | **Las excepciones** con `@ResponseStatus` |
| | **El servicio** con las reglas |
| | **El controlador** y el `@RestControllerAdvice` |

:material-alert: El proyecto **no compila al abrirlo**: el repositorio y el mapeador usan el modelo y los DTOs, que no existen aún. **Eso es la especificación**: de ahí sale el nombre de cada clase y el orden de los componentes de cada `record`.
:material-alert: **No se toca ni una línea** del repositorio ni del mapeador. Si los cambias para que encajen con tus clases, la penalización es de **1,5 puntos**.
:material-alert: Lo primero que se hace es **leerlos** y apuntar: nombres de clase, componentes y su orden, y qué DTOs hacen falta.

---

## Los dieciséis errores del examen

| | Síntoma | Causa |
|:-:|---|---|
| 1 | 500 en vez de 404 | `findById(id).get()` sobre un `Optional` vacío |
| 2 | 201 con datos basura | **`@Valid` olvidado** |
| 3 | `cantidad: -5` dentro de una lista pasa la validación | Falta el `@Valid` **anidado** |
| 4 | 204 al borrar algo que no existe | El `delete` no mira las filas afectadas |
| 5 | 415 en el POST | Falta `Content-Type: application/json` |
| 6 | `@Cacheable` no cachea | Falta `@EnableCaching` |
| 7 | Datos viejos en el listado | Falta `@CacheEvict(allEntries = true)` |
| 8 | Bean no encontrado | Clase fuera del paquete escaneado, o sin `@Service` |
| 9 | PATCH que borra campos | PUT y PATCH usando el mismo método del mapeador |
| 10 | Contador compartido entre usuarios | Estado mutable en un bean singleton |
| 11 | `Table "X" already exists` al segundo arranque | Falta `CREATE TABLE IF NOT EXISTS` |
| 12 | El primer POST choca con un id existente | Falta `ALTER TABLE … RESTART WITH n` |
| 13 | `Database may be already in use` | Falta `AUTO_SERVER=TRUE` en la URL de H2 |
| 14 | Stock negativo con dos peticiones a la vez | «Leer, comprobar, escribir» en vez de `UPDATE … WHERE stock >= :n` |
| 15 | Stock descontado sin la segunda tabla actualizada | Falta `@Transactional`, o la excepción se capturó dentro |
| 16 | «He tenido que cambiar el repositorio para que compile» | El modelo o los DTOs no respetan el código entregado |
