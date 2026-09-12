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
.filter(p -> p.precio() > 10)      .map(Producto::nombre)      .flatMap(List::stream)
.sorted(comparing(P::nombre))      .sorted(comparingInt(P::stock).reversed())
.distinct()  .limit(10)  .skip(5)  .count()  .toList()
.anyMatch(...)  .allMatch(...)  .noneMatch(...)  .findFirst()
.reduce(BigDecimal.ZERO, BigDecimal::add)
.mapToInt(P::stock).summaryStatistics()    // getCount getSum getMin getMax getAverage
```

## Collectors

```java
groupingBy(P::categoria)
groupingBy(P::categoria, counting())
groupingBy(P::categoria, TreeMap::new, counting())              // claves ordenadas
groupingBy(P::categoria, mapping(P::nombre, toList()))
partitioningBy(p -> p.stock() > 0)
toMap(P::id, p -> p)          toMap(P::id, p -> p, (a,b) -> a)  // (a,b) evita el choque
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

# RA5 · Capas con Spring Boot

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

## Errores centralizados

```java
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(NoEncontradoException.class)
    public ProblemDetail noEncontrado(NoEncontradoException e) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Recurso no encontrado");
        pd.setType(URI.create("https://ejemplo.es/errores/no-encontrado"));
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalido(MethodArgumentNotValidException e) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setProperty("errores", e.getBindingResult().getFieldErrors().stream()
            .collect(toMap(FieldError::getField, FieldError::getDefaultMessage, (a,b) -> a)));
        return pd;
    }
}
```

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

# RA6 · JPA

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

# RA7 · Servicios web

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
