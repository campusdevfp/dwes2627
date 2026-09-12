# Batería de ejercicios — UT4

**Dominio: una biblioteca.** Distinto del de clase (la tienda) a propósito: si puedes hacerlo aquí, es que lo has entendido y no memorizado.

**30 ejercicios agrupados por tema**, con solución. No hace falta hacerlos todos: con tres o cuatro de cada tema vas sobrado. Los marcados con :material-file-sign: son del tipo que cae en el examen.

| Tema | Ejercicios | Sesiones |
|---|---|:-:|
| 1 · Spring Boot | E1–E5 | S1–S3 |
| 2 · Inyección de dependencias | E6–E10 | S4–S6 |
| 3 · Las capas en Spring | E11–E15 | S7–S9 |
| 4 · DTO y validación | E16–E20 | S10–S11 |
| 5 · Errores y configuración | E21–E25 | S12–S13 |
| 6 · Testing en Spring | E26–E30 | S14–S15 |

---

# Tema 1 · Spring Boot

### E1 ● — Primer endpoint y `@Value`

Expón `GET /info` con el nombre y la versión de la aplicación leídos de `application.yml`.

??? success "Solución"

    ```java
    @RestController
    public class InfoControlador {
        private final String nombre;
        public InfoControlador(@Value("${app.nombre}") String nombre) { this.nombre = nombre; }

        record Info(String nombre, Instant arrancadoEn) {}
        @GetMapping("/info") public Info info() { return new Info(nombre, Instant.now()); }
    }
    ```
    `@Value` va **en el constructor**, no en el campo: así el campo puede ser `final` y la clase se prueba con un simple `new`, sin levantar Spring.


### E2 ● — Cuenta tus beans

`CommandLineRunner` que imprima cuántos beans hay registrados y liste los de tu paquete.

??? success "Solución"

    ```java
    @Bean
    CommandLineRunner inspector(ApplicationContext ctx) {
        return args -> {
            var nombres = ctx.getBeanDefinitionNames();
            IO.println("Beans registrados: " + nombres.length);
            Arrays.stream(nombres).filter(n -> n.toLowerCase().contains("libro")).forEach(IO::println);
        };
    }
    ```
    Salen entre 300 y 400. Merece la pena mirarlos: casi todos los pone la autoconfiguración, no tú.


### E3 ●● — El bean que no aparece

Crea un `@Service` en un paquete **fuera** del de la clase principal y explica qué pasa.

??? success "Solución"

    Falla el arranque: `No qualifying bean of type ... available`.

    El escaneo de componentes parte del paquete de la clase anotada con `@SpringBootApplication` y **baja**, nunca sube. Un `@Service` en `es.otro.paquete` cuando la aplicación está en `es.iesx.biblioteca` es invisible.

    Se arregla moviendo la clase, o con `@ComponentScan(basePackages = {...})` — que es la solución mala, porque desordena el proyecto para tapar un error de colocación.


### E4 ●● — Cambiar el puerto sin tocar el código

Arranca la misma aplicación en tres puertos distintos, de tres maneras.

??? success "Solución"

    ```bash
    java -jar app.jar --server.port=9090                  # argumento
    SERVER_PORT=9091 java -jar app.jar                    # variable de entorno
    ```
    ```yaml
    server.port: 9092                                     # application.yml
    ```

    El orden de precedencia es ese: **argumento > variable de entorno > fichero**. Es lo que permite que la misma imagen de Docker sirva para desarrollo y para producción.


### E5 ●●● — Radiografía de la autoconfiguración

Arranca con `--debug` y localiza tres autoconfiguraciones activadas y una descartada. Explica por qué se descartó.

??? success "Solución"

    En el informe salen dos listas: `Positive matches` y `Negative matches`.

    Típicamente activadas: `DispatcherServletAutoConfiguration`, `JacksonAutoConfiguration`, `ErrorMvcAutoConfiguration`.
    Típicamente descartada: `DataSourceAutoConfiguration` — *«did not find class org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType»*.

    La lección: Spring Boot decide **según lo que hay en el classpath**. Añadir una dependencia cambia el comportamiento sin escribir una línea, y por eso conviene saber leer este informe cuando algo «se configura solo» de forma inesperada.


---

# Tema 2 · Inyección de dependencias

### E6 ● — Inyección por constructor

Monta `LibroServicio` que dependa de `LibroRepositorio`, con inyección por constructor y campo `final`.

??? success "Solución"

    ```java
    @Service
    public class LibroServicio {
        private final LibroRepositorio repositorio;
        public LibroServicio(LibroRepositorio repositorio) { this.repositorio = repositorio; }
    }
    ```
    Sin `@Autowired`: desde Spring 4.3, si hay **un solo constructor**, se usa ese. Escribirlo es ruido.


### E7 ●● — Por qué no `@Autowired` en el campo

Escribe la misma clase con `@Autowired` sobre el campo y enumera tres problemas.

??? success "Solución"

    ```java
    @Service
    public class LibroServicio {
        @Autowired private LibroRepositorio repositorio;   // MAL 
    }
    ```

    1. **El campo no puede ser `final`**, así que nada impide que alguien lo cambie.
    2. **No se puede probar con `new`**: hay que levantar Spring o usar reflexión.
    3. **Oculta las dependencias**: una clase con ocho `@Autowired` parece inofensiva; con ocho parámetros en el constructor, el problema se ve.

    El tercero es el importante: el constructor largo es **una señal de diseño**, no una molestia. Taparla es el error.


### E8 ●● — Dos implementaciones y `@Profile`

`NotificadorEmail` y `NotificadorConsola`, que se elijan según el perfil activo.

??? success "Solución"

    ```java
    public interface Notificador { void enviar(String destino, String mensaje); }

    @Service @Profile("prod")           class NotificadorEmail   implements Notificador { … }
    @Service @Profile({"dev","test"})   class NotificadorConsola implements Notificador { … }
    ```
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```
    Si no hay ningún perfil activo, **ninguno de los dos se registra** y el arranque falla. Se arregla con `@Profile("default")` en uno, o marcándolo `@Primary` sin perfil.


### E9 ●● — `@Qualifier` frente a `@Primary`

Dos implementaciones sin perfiles. Haz que una sea la de por defecto y que un servicio concreto use la otra.

??? success "Solución"

    ```java
    @Service @Primary            class NotificadorEmail   implements Notificador { … }
    @Service("consola")          class NotificadorConsola implements Notificador { … }

    @Service
    public class AvisoServicio {
        public AvisoServicio(@Qualifier("consola") Notificador notificador) { … }
    }
    ```
    `@Primary` responde a *«¿cuál si no digo nada?»*. `@Qualifier` a *«quiero este en concreto»*. Sin ninguno de los dos: `NoUniqueBeanDefinitionException`.


### E10 ●●● — Inyectar una lista de estrategias

Notifica por **todos** los canales disponibles sin tocar el servicio al añadir uno nuevo.

??? success "Solución"

    ```java
    @Service
    public class AvisoServicio {
        private final List<Notificador> canales;
        public AvisoServicio(List<Notificador> canales) { this.canales = canales; }

        public void avisar(String destino, String mensaje) {
            canales.forEach(c -> c.enviar(destino, mensaje));
        }
    }
    ```
    Spring inyecta **todos** los beans de ese tipo. Añadir `NotificadorSms` es crear una clase; el servicio no se toca.

    Es el principio abierto/cerrado de SOLID funcionando de verdad, y una respuesta excelente si te preguntan por él en una entrevista.

    Para fijar el orden, `@Order(1)` sobre cada implementación.


---

# Tema 3 · Las capas en Spring

### E11 ● — El repositorio en memoria

`LibroRepositorio` como **interfaz** y `LibroRepositorioMemoria` con `ConcurrentHashMap` y `AtomicLong`.

??? success "Solución"

    ```java
    public interface LibroRepositorio {
        List<Libro> buscarTodos();
        Optional<Libro> buscarPorId(Long id);
        Optional<Libro> buscarPorIsbn(String isbn);
        Libro guardar(Libro libro);
        boolean borrar(Long id);
    }

    @Repository
    public class LibroRepositorioMemoria implements LibroRepositorio {
        private final Map<Long, Libro> datos = new ConcurrentHashMap<>();
        private final AtomicLong secuencia = new AtomicLong();

        @Override public Libro guardar(Libro l) {
            var conId = l.id() == null ? l.conId(secuencia.incrementAndGet()) : l;
            datos.put(conId.id(), conId);
            return conId;
        }
    }
    ```
    **La interfaz es el punto del ejercicio.** En la UT5 la implementación se sustituye por JPA, y si el servicio dependía de la interfaz no se entera.


### E12 ●● — `buscarTodos` que no se puede modificar

Haz que quien reciba la lista no pueda alterar el almacén interno. Demuéstralo con un test.

??? success "Solución"

    ```java
    @Override public List<Libro> buscarTodos() { return List.copyOf(datos.values()); }
    ```
    ```java
    @Test void laListaDevueltaNoSeToca() {
        var lista = repositorio.buscarTodos();
        assertThatThrownBy(() -> lista.add(new Libro(...)))
            .isInstanceOf(UnsupportedOperationException.class);
    }
    ```
    Devolver `datos.values()` directamente expone el interior: cualquiera puede vaciarte el repositorio desde fuera. Es encapsulación, y se corrige en el examen.


### E13 ●● — Reglas de negocio en el servicio

`prestar(libroId, socio)`: 404 si el libro no existe, 409 si ya está prestado, y registra la fecha.

??? success "Solución"

    ```java
    public PrestamoDto prestar(Long libroId, String socio) {
        var libro = repositorio.buscarPorId(libroId)
            .orElseThrow(() -> new LibroNoEncontradoException(libroId));
        if (libro.prestado()) throw new LibroYaPrestadoException(libroId);
        repositorio.guardar(libro.conPrestado(true));
        return new PrestamoDto(libroId, socio, LocalDate.now());
    }
    ```
    **El orden importa y se evalúa:** primero existencia, después estado. Al revés, un libro inexistente devolvería 409 en vez de 404.


### E14 ●● — Lógica con streams en el servicio

`Map<String, Long> librosPorGenero()`, `List<Libro> masPrestados(int n)` y `double porcentajePrestados()`.

??? success "Solución"

    ```java
    public Map<String, Long> librosPorGenero() {
        return repositorio.buscarTodos().stream()
            .collect(groupingBy(Libro::genero, TreeMap::new, counting()));
    }

    public List<Libro> masPrestados(int n) {
        return repositorio.buscarTodos().stream()
            .sorted(comparingInt(Libro::vecesPrestado).reversed())
            .limit(n).toList();
    }

    public double porcentajePrestados() {
        var todos = repositorio.buscarTodos();
        if (todos.isEmpty()) return 0;
        return todos.stream().filter(Libro::prestado).count() * 100.0 / todos.size();
    }
    ```
    El `if (todos.isEmpty())` no es paranoia: sin él, la división entera por cero rompe la página el día que la biblioteca esté vacía.


### E15 ●●● — Detecta la fuga de capas

Este controlador tiene **cuatro** problemas. Encuéntralos y arréglalos.

```java
@RestController
public class LibroControlador {
    private LibroRepositorioMemoria repositorio = new LibroRepositorioMemoria();

    @GetMapping("/libros")
    public List<Libro> listar() {
        return repositorio.buscarTodos().stream()
            .filter(l -> !l.prestado())
            .sorted(comparing(Libro::titulo))
            .toList();
    }
}
```

??? success "Solución"

    1. **`new` a mano** en vez de inyección: no es un bean, no se puede sustituir, no se puede probar.
    2. **Depende de la clase concreta**, no de la interfaz.
    3. **Lógica de negocio en el controlador**: el filtrado y el orden son del servicio.
    4. **Devuelve la entidad**, no un DTO: expone el modelo interno.

    ```java
    @RestController
    public class LibroControlador {
        private final LibroServicio servicio;
        public LibroControlador(LibroServicio servicio) { this.servicio = servicio; }

        @GetMapping("/libros")
        public List<LibroDto> listar() { return servicio.listarDisponibles(); }
    }
    ```
    Los cuatro son criterios de la rúbrica del examen. Este ejercicio vale por toda la explicación teórica de las capas.


---

# Tema 4 · DTO y validación

### E16 ● — DTO de entrada y de salida

`CrearLibroDto` y `LibroDto`, con el mapeador.

??? success "Solución"

    ```java
    public record CrearLibroDto(String isbn, String titulo, String autor, String genero) {}
    public record LibroDto(Long id, String isbn, String titulo, String autor,
                           String genero, boolean disponible) {}

    @Component
    public class LibroMapeador {
        public Libro aEntidad(CrearLibroDto d) { return new Libro(null, d.isbn(), …); }
        public LibroDto aDto(Libro l)          { return new LibroDto(l.id(), …, !l.prestado()); }
    }
    ```
    El de entrada **no lleva `id`**; el de salida lleva `disponible`, que es calculado. Ninguno de los dos es la entidad.


### E17 ●● — La asignación masiva, en vivo

Usa la entidad como cuerpo de la petición y demuestra el agujero con `curl`.

??? success "Solución"

    ```java
    @PostMapping public Libro crear(@RequestBody Libro libro) { … }   // MAL 
    ```
    ```bash
    curl -X POST localhost:8080/libros -H "Content-Type: application/json" \
      -d '{"titulo":"X","id":9999,"vecesPrestado":5000}'
    ```
    El cliente acaba de escribir el `id` y un contador interno. Con un DTO de entrada que solo tiene los campos permitidos, esos valores **se descartan en silencio**.

    Se llama *mass assignment* y es un fallo de seguridad, no de estilo.


### E18 ●● — Validación completa

Valida `CrearLibroDto`: ISBN de 13 dígitos, título de 2 a 200, autor obligatorio, género dentro de una lista.

??? success "Solución"

    ```java
    public record CrearLibroDto(
        @NotBlank @Pattern(regexp = "\\d{13}", message = "El ISBN son 13 dígitos") String isbn,
        @NotBlank @Size(min = 2, max = 200) String titulo,
        @NotBlank String autor,
        @NotNull Genero genero) {}
    ```
    ```java
    @PostMapping public ResponseEntity<LibroDto> crear(@Valid @RequestBody CrearLibroDto dto) { … }
    ```
    Sin `@Valid` en el parámetro, **las anotaciones no hacen nada**. Es el fallo silencioso más común del tema: el código parece correcto y no valida.


### E19 ●●● — Validador propio

Crea `@IsbnValido` que compruebe el dígito de control del ISBN-13.

??? success "Solución"

    ```java
    @Target(FIELD) @Retention(RUNTIME)
    @Constraint(validatedBy = IsbnValidador.class)
    public @interface IsbnValido {
        String message() default "ISBN no válido";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public class IsbnValidador implements ConstraintValidator<IsbnValido, String> {
        @Override public boolean isValid(String isbn, ConstraintValidatorContext c) {
            if (isbn == null || !isbn.matches("\\d{13}")) return false;
            int suma = 0;
            for (int i = 0; i < 12; i++)
                suma += (isbn.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
            return (10 - suma % 10) % 10 == isbn.charAt(12) - '0';
        }
    }
    ```
    Los tres métodos `message`, `groups` y `payload` son obligatorios aunque no los uses: sin ellos no compila.


### E20 ●●● — Validación cruzada entre campos

La fecha de devolución prevista debe ser posterior a la de préstamo. Esa regla afecta a **dos** campos.

??? success "Solución"

    ```java
    @Target(TYPE) @Retention(RUNTIME)
    @Constraint(validatedBy = FechasCoherentesValidador.class)
    public @interface FechasCoherentes { … }

    public class FechasCoherentesValidador implements ConstraintValidator<FechasCoherentes, PrestamoDto> {
        @Override public boolean isValid(PrestamoDto p, ConstraintValidatorContext c) {
            if (p.inicio() == null || p.fin() == null) return true;   // lo cubre @NotNull
            return p.fin().isAfter(p.inicio());
        }
    }
    ```
    La anotación va **sobre el tipo**, no sobre un campo, porque necesita ver los dos. Y devolver `true` cuando algún campo es nulo evita duplicar mensajes de error: de los nulos ya se encarga `@NotNull`.


---

# Tema 5 · Errores y configuración

### E21 ● — Excepciones propias del dominio

Crea la jerarquía: una base y dos concretas.

??? success "Solución"

    ```java
    public abstract class BibliotecaException extends RuntimeException {
        protected BibliotecaException(String mensaje) { super(mensaje); }
    }
    public class LibroNoEncontradoException extends BibliotecaException {
        public LibroNoEncontradoException(Long id) { super("No existe el libro " + id); }
    }
    public class LibroYaPrestadoException extends BibliotecaException {
        public LibroYaPrestadoException(Long id) { super("El libro " + id + " ya está prestado"); }
    }
    ```
    No comprobadas, y **una base común**: permite capturarlas todas juntas en el manejador si algún día hace falta.


### E22 ●● — Manejador global con ProblemDetail

Traduce las excepciones a 404 y 409 con RFC 7807.

??? success "Solución"

    ```java
    @RestControllerAdvice
    public class ManejadorErrores {

        @ExceptionHandler(LibroNoEncontradoException.class)
        public ProblemDetail noEncontrado(LibroNoEncontradoException e) {
            var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
            pd.setTitle("Libro no encontrado");
            pd.setType(URI.create("https://biblioteca.es/errores/no-encontrado"));
            return pd;
        }

        @ExceptionHandler(LibroYaPrestadoException.class)
        public ProblemDetail conflicto(LibroYaPrestadoException e) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        }
    }
    ```
    Comprueba con `curl -i` que la respuesta lleva `Content-Type: application/problem+json`.


### E23 ●● — Los errores de validación, campo a campo

Que un 400 devuelva un mapa `campo → mensaje`, no una traza.

??? success "Solución"

    ```java
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalido(MethodArgumentNotValidException e) {
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Datos no válidos");
        pd.setProperty("errores", e.getBindingResult().getFieldErrors().stream()
            .collect(toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a)));
        return pd;
    }
    ```
    El `(a, b) -> a` evita que reviente si un campo acumula dos errores. Sin él, `IllegalStateException: Duplicate key`.


### E24 ●● — Configuración con `@ConfigurationProperties`

Agrupa la configuración de la biblioteca en una clase tipada.

??? success "Solución"

    ```yaml
    biblioteca:
      dias-prestamo: 15
      max-por-socio: 3
      nombre: "Biblioteca Municipal"
    ```
    ```java
    @ConfigurationProperties(prefix = "biblioteca")
    public record BibliotecaProps(int diasPrestamo, int maxPorSocio, String nombre) {}
    ```
    Con `@EnableConfigurationProperties(BibliotecaProps.class)` o `@ConfigurationPropertiesScan`.

    Mejor que diez `@Value` sueltos: está tipado, se valida al arrancar y se inyecta como un objeto.


### E25 ●●● — Tres perfiles, tres comportamientos

`dev`, `test` y `prod` con configuración distinta, y un test que compruebe cuál está activo.

??? success "Solución"

    ```
    application.yml            común
    application-dev.yml        H2 en memoria, log DEBUG, datos de ejemplo
    application-test.yml       H2, sin datos precargados
    application-prod.yml       PostgreSQL, log INFO, sin consola H2
    ```
    ```java
    @Component @Profile("dev")
    class DatosDeEjemplo implements CommandLineRunner { … }
    ```
    ```bash
    java -jar app.jar --spring.profiles.active=prod
    ```
    Regla que se evalúa: **ninguna contraseña real en ningún `application-*.yml` versionado**. En producción, variables de entorno: `password: ${DB_PASSWORD}`.


---

# Tema 6 · Testing en Spring

### E26 ● — Test del servicio con dobles

Prueba `prestar` sin levantar Spring.

??? success "Solución"

    ```java
    class LibroServicioTest {
        private final LibroRepositorio repo = mock(LibroRepositorio.class);
        private final LibroServicio servicio = new LibroServicio(repo);

        @Test void prestaUnLibroDisponible() {
            when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Libro(1L, "…", false)));
            var p = servicio.prestar(1L, "ana");
            assertThat(p.socio()).isEqualTo("ana");
            verify(repo).guardar(argThat(Libro::prestado));
        }
    }
    ```
    Sin anotaciones de Spring: **un `new` y un doble**. Milisegundos en vez de segundos, y es la recompensa directa de haber inyectado por constructor.


### E27 ●● — Los tres caminos

Prueba el camino bueno, el 404 y el 409.

??? success "Solución"

    ```java
    @Test void siNoExisteLanzaNoEncontrado() {
        when(repo.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> servicio.prestar(99L, "ana"))
            .isInstanceOf(LibroNoEncontradoException.class);
        verifyNoInteractions(repo);          // OJO ver la nota
    }

    @Test void siYaEstaPrestadoLanzaConflicto() {
        when(repo.buscarPorId(1L)).thenReturn(Optional.of(new Libro(1L, "…", true)));
        assertThatThrownBy(() -> servicio.prestar(1L, "ana"))
            .isInstanceOf(LibroYaPrestadoException.class);
        verify(repo, never()).guardar(any());
    }
    ```
    Ojo con el `verifyNoInteractions` del primero: **falla**, porque sí hubo una interacción (`buscarPorId`). Lo correcto ahí es `verify(repo, never()).guardar(any())`. Es un error de test muy típico y conviene cometerlo una vez.


### E28 ●● — Test del controlador con `@WebMvcTest`

Prueba el 200 y el 404 sin tocar el servicio real.

??? success "Solución"

    ```java
    @WebMvcTest(LibroControlador.class)
    class LibroControladorTest {
        @Autowired MockMvc mvc;
        @MockitoBean LibroServicio servicio;

        @Test void devuelveElLibro() throws Exception {
            when(servicio.porId(1L)).thenReturn(new LibroDto(1L, "…", "Rayuela", …));
            mvc.perform(get("/api/v1/libros/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.titulo").value("Rayuela"));
        }

        @Test void devuelve404SiNoExiste() throws Exception {
            when(servicio.porId(99L)).thenThrow(new LibroNoEncontradoException(99L));
            mvc.perform(get("/api/v1/libros/99"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.title").value("Libro no encontrado"));
        }
    }
    ```
    `@WebMvcTest` **solo levanta la capa web**: el servicio es un doble. Si el segundo test da 500 en vez de 404, falta importar el `@RestControllerAdvice`.


### E29 ●●● — El test que caza el 500

Comprueba que una excepción no controlada **no** filtra la traza al cliente.

??? success "Solución"

    ```java
    @Test void unErrorInesperadoNoFiltraLaTraza() throws Exception {
        when(servicio.porId(1L)).thenThrow(new RuntimeException("fallo interno de la BD"));
        mvc.perform(get("/api/v1/libros/1"))
           .andExpect(status().isInternalServerError())
           .andExpect(content().string(not(containsString("fallo interno"))))
           .andExpect(content().string(not(containsString("at es.iesx"))));
    }
    ```
    Con un manejador genérico que registre el detalle en el log y devuelva un mensaje neutro. Filtrar trazas al cliente es un fallo de seguridad: revela versiones, rutas y estructura interna.


### E30 ●●● — Test de integración completo

Levanta la aplicación entera y prueba el flujo de alta y préstamo.

??? success "Solución"

    ```java
    @SpringBootTest
    @AutoConfigureMockMvc
    @DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
    class BibliotecaIntegracionTest {

        @Autowired MockMvc mvc;

        @Test void altaYPrestamo() throws Exception {
            var location = mvc.perform(post("/api/v1/libros").contentType(APPLICATION_JSON)
                    .content("""{"isbn":"9788401352836","titulo":"Rayuela","autor":"Cortázar","genero":"NOVELA"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

            mvc.perform(post(location + "/prestamos").param("socio", "ana"))
               .andExpect(status().isCreated());

            mvc.perform(post(location + "/prestamos").param("socio", "luis"))
               .andExpect(status().isConflict());       // ya prestado
        }
    }
    ```
    El `@DirtiesContext` reinicia el contexto entre tests: sin él, el repositorio en memoria arrastra los libros de un test a otro y empiezan a fallar **según el orden en que se ejecuten**, que es la peor clase de fallo.


---

## Cómo usarlos en clase

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E6 · E11 · E16 · E21 · E26 |
| Taller de la sesión, 25-30 min | E3 · E8 · E13 · E18 · E23 · E28 |
| Los que hay que hacer sí o sí | **E10 · E15 · E17 · E18 · E28** |
| Para quien va sobrado | E5 · E19 · E20 · E25 · E29 · E30 |
| Repaso antes del examen | E15 · E18 · E22 · E27 |

!!! tip "El más rentable de la unidad"
    El **E15** (detectar las cuatro fugas de capas). Sus cuatro errores son cuatro criterios de la rúbrica del examen, y verlos juntos en un código real explica el tema mejor que cualquier diagrama.
