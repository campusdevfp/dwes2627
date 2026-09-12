# Chuleta de Spring Boot (UT4)

## Crear proyecto

[start.spring.io](https://start.spring.io) → Maven · Java 25 · **Spring Web**, **Validation**, **DevTools**

```bash
mvn spring-boot:run     # arrancar
mvn test                # tests
mvn clean package       # generar el .jar
java -jar target/app.jar --spring.profiles.active=prod
```

## Estructura

``` { .java .sinajuste }
src/main/java/es/iesx/daw/tienda/
├── TiendaApplication.java     @SpringBootApplication
├── controlador/               @RestController
├── servicio/                  @Service
├── repositorio/               interfaz + @Repository
├── modelo/                    records del dominio
├── dto/                       records de entrada/salida
└── excepcion/                 excepciones propias + @RestControllerAdvice
src/main/resources/application.yml
```

:material-alert: Todo debe colgar del paquete de la clase

@SpringBootApplication

(component scan).

## Estereotipos e inyección

```java
@Repository  class RepoMemoria implements Repo { }
@Service     class Servicio { }
@RestController class Controlador { }
@Component   class Otro { }

// SIEMPRE por constructor, contra la interfaz, campo final
@Service
public class Servicio {
    private final Repo repo;
    public Servicio(Repo repo) { this.repo = repo; }
}
```

Varias implementaciones: `@Primary` · `@Qualifier("nombre")` · `@Profile("dev")`

## Controlador

```java
@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    @GetMapping                 public List<Dto> listar() { }
    @GetMapping("/{id}")        public Dto uno(@PathVariable Integer id) { }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
                                public Dto crear(@Valid @RequestBody CrearDto dto) { }
    @PutMapping("/{id}")        public Dto sustituir(@PathVariable Integer id, @Valid @RequestBody Dto d) { }
    @PatchMapping("/{id}")      public Dto modificar(@PathVariable Integer id, @RequestParam int v) { }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
                                public void borrar(@PathVariable Integer id) { }
}
```

| Anotación | Origen del dato |
|---|---|
| `@PathVariable` | `/productos/{id}` |
| `@RequestParam` | `?unidades=5` |
| `@RequestBody` | cuerpo JSON |

## Validación (Bean Validation)

```java
public record CrearProductoDto(
    @NotBlank @Size(min=2,max=100) String nombre,
    @NotBlank String categoria,
    @NotNull @Positive Double precio,
    @Min(0) int stock) {}
```

`@NotNull` `@NotBlank` `@NotEmpty` `@Size` `@Min` `@Max` `@Positive` `@Email` `@Pattern` `@Past` `@Future` Se activa con `@Valid` en el parámetro del controlador. Falla → `400` automático.

:material-alert: Para texto obligatorio: **`@NotBlank`**, no `@NotNull`.

## DTO y mapeador

```java
// Entrada: SOLO lo que el cliente puede decidir (sin id, sin campos internos)
public record CrearProductoDto(@NotBlank String nombre, @NotBlank String categoria,
                               @NotNull @Positive Double precio, @Min(0) int stock) {}

// Salida: lo que el cliente debe ver, con campos calculados
public record ProductoDto(Integer id, String nombre, double precio,
                          double precioConIva, boolean disponible) {}

@Component
public class ProductoMapper {
    public Producto aModelo(CrearProductoDto d) {
        return new Producto(null, d.nombre().trim(), d.categoria(), d.precio(), d.stock());
    }
    public ProductoDto aDto(Producto p) {
        return new ProductoDto(p.id(), p.nombre(), p.precio(),
                               p.precio() * 1.21, p.stock() > 0);
    }
}
```

:material-alert:

Mass assignment: si aceptas el modelo en el `@RequestBody`, el cliente puede enviar id o campos internos. Por eso DTO de entrada siempre.

## Errores

```java
public class ProductoNoEncontradoException extends RuntimeException { }

@RestControllerAdvice
public class ManejadorErrores {
    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ProblemDetail noEncontrado(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
```

| Situación | Código |
|---|---|
| OK / creado / sin contenido | 200 / **201** / **204** |
| Datos inválidos | **400** |
| Sin autenticar / sin permiso | 401 / 403 |
| No existe | **404** |
| Conflicto (duplicado, sin stock) | **409** |
| Fallo interno | 500 |

## Configuración

```yaml title="src/main/resources/application.yml"
server:
  port: 8080
tienda:
  iva: 0.21
spring:
  profiles:
    active: dev
```

```java
@ConfigurationProperties(prefix = "tienda")
public record TiendaConfig(double iva) {}
```

Secretos → variables de entorno:

password: ${DB_PASSWORD}

## Logging y observabilidad

```java
private static final Logger log = LoggerFactory.getLogger(MiClase.class);

log.debug("Stock de {}: {} → {}", id, antes, despues);   // placeholders, no concatenar
log.info("Producto creado: id={}", creado.id());
log.error("Fallo al guardar {}", id, excepcion);  // la excepción, ÚLTIMA y sin {}
```

Nunca `System.out.println`. Nunca contraseñas, tokens ni datos personales en el log.

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--debug   # informe de autoconfiguración
curl -s localhost:8080/actuator/health | jq
curl -s localhost:8080/actuator/beans | jq '.contexts.application.beans | keys | length'
```

## Tests

```java
// Unitario del servicio
@ExtendWith(MockitoExtension.class)
class ServicioTest {
    @Mock Repo repo;
    @InjectMocks Servicio servicio;

    @Test void nombreQueDescribeLaRegla() {
        when(repo.buscarPorId(1)).thenReturn(Optional.of(x));
        assertEquals(..., servicio.metodo(1));
        verify(repo).buscarPorId(1);
        verify(repo, never()).guardar(any());
    }
}

// Controlador
@WebMvcTest(ProductoControlador.class)
class ControladorTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean ProductoServicio servicio;

    @Test void devuelve404() throws Exception {
        when(servicio.obtener(9)).thenThrow(new ProductoNoEncontradoException(9));
        mockMvc.perform(get("/api/productos/9")).andExpect(status().isNotFound());
    }
}
```

### Mockito, más allá de lo básico

```java
when(repo.guardar(any())).thenAnswer(i -> i.getArgument(0));   // devuelve lo recibido

var captor = ArgumentCaptor.forClass(Producto.class);          // ¿QUÉ se guardó?
verify(repo).guardar(captor.capture());
assertThat(captor.getValue().nombre()).isEqualTo("Casco");

@ParameterizedTest @CsvSource({"10,5,true", "10,10,true", "10,11,false"})
void hayStock(int stock, int piden, boolean esperado) { ... }
```

:material-alert: Si un argumento es matcher,

todos deben serlo: verify(r).guardar(eq(1), any()).

| Anotación | Levanta |
|---|---|
| Mockito solo | nada (rapidísimo) |
| `@WebMvcTest` | capa web |
| `@DataJpaTest` | capa de datos |
| `@SpringBootTest` | app completa |

## Reglas de oro

1. El controlador no tiene lógica: recibe, delega, responde.
2. El servicio no sabe de HTTP; el repositorio no sabe de negocio.
3. Inyección por constructor, contra interfaces, campos final.
4. DTO
    en la frontera: nunca serialices el modelo interno.
5. Cada excepción de dominio → su código HTTP.
6. Si no puedes testear una clase con un new, está mal diseñada.
7. Los beans son singleton: nada de estado mutable de usuario en un `@Service`.
8. Configuración fuera del código (`application.yml`), secretos fuera del repositorio.
