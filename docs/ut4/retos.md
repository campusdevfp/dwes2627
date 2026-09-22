# Retos — UT4

Tres retos. **Los dos primeros vienen resueltos**: se leen, se teclean y se comparan con lo tuyo. **El tercero se entrega**, y es del mismo tamaño y la misma forma que el examen práctico de la unidad.

| | Reto | Sesiones | |
|:-:|---|:-:|---|
| **R1** | La API que no existía | S1–S9 | :material-check-circle: Resuelto |
| **R2** | Que no se caiga con la cara descubierta | S10–S15 | :material-check-circle: Resuelto |
| **R3** | Reserva de aulas | S16–S18 | :material-pencil: **A entregar** |

!!! info "Cómo se usan"
    Los resueltos **no se leen: se hacen**. Escribe tú primero, y solo después despliega la solución para comparar. Leer código ajeno da la sensación de haber aprendido y no deja nada.

---

# R1 · La API que no existía

**Sesiones S1–S9** · Resuelto

## La situación

El instituto lleva el inventario de material informático en una hoja de cálculo que se pasan por correo. Hay tres versiones distintas circulando y nadie sabe cuál es la buena.

## La pregunta

> **Convertid esa hoja en una API que pueda usar cualquier programa, de forma que cambiar de dónde salen los datos no obligue a reescribirlo todo.**

## Restricciones

- **Ningún `new` escrito por ti** para conectar las capas. Si haces `new ProductoRepositorioMemoria()`, el reto está mal resuelto aunque funcione.
- El servicio depende de **la interfaz**, nunca de la implementación.
- Los códigos de estado son los correctos. Un `POST` que devuelve `200` en vez de `201` pierde puntos.
- La lógica **no está en el controlador**. Si ves un `if` de negocio dentro de un `@RestController`, sácalo.

## Criterios de aceptación

- [ ] `GET /api/v1/equipos` → `200` con la lista.
- [ ] `GET /api/v1/equipos/{id}` → `200`, o `404` si no existe.
- [ ] `POST /api/v1/equipos` → `201` con cabecera `Location`.
- [ ] `PATCH /api/v1/equipos/{id}/estado` → `200`, o `409` si la transición no es válida.
- [ ] `DELETE /api/v1/equipos/{id}` → `204`.
- [ ] Existe una segunda implementación del repositorio y **cambiar a ella no toca el servicio**.

??? success "Solución · el modelo y el repositorio"

    ```java
    package es.iesx.inventario.modelo;

    public record Equipo(Long id, String etiqueta, String tipo,
                         Estado estado, String ubicacion) {

        public enum Estado { DISPONIBLE, PRESTADO, REPARACION, BAJA }

        public Equipo con(Estado nuevo) {                      // (1)
            return new Equipo(id, etiqueta, tipo, nuevo, ubicacion);
        }
    }
    ```

    1.  Es un `record`, así que es inmutable: para «cambiar» el estado se devuelve uno nuevo. Ese método ahorra repetir el constructor entero por todas partes.

    ```java
    package es.iesx.inventario.repositorio;

    public interface EquipoRepositorio {
        List<Equipo> listar();
        Optional<Equipo> buscarPorId(Long id);
        Equipo guardar(Equipo equipo);
        boolean borrar(Long id);
    }
    ```

    ```java
    @Repository
    public class EquipoRepositorioMemoria implements EquipoRepositorio {

        private final Map<Long, Equipo> datos = new ConcurrentHashMap<>();
        private final AtomicLong siguienteId = new AtomicLong(1);

        @Override public List<Equipo> listar() {
            return List.copyOf(datos.values());
        }

        @Override public Optional<Equipo> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override public Equipo guardar(Equipo equipo) {
            var id = equipo.id() != null ? equipo.id() : siguienteId.getAndIncrement();
            var guardado = new Equipo(id, equipo.etiqueta(), equipo.tipo(),
                                      equipo.estado(), equipo.ubicacion());
            datos.put(id, guardado);
            return guardado;
        }

        @Override public boolean borrar(Long id) {
            return datos.remove(id) != null;                   // (1)
        }
    }
    ```

    1.  Devolver `boolean` en vez de `void` permite que el controlador distinga entre `204` y `404` sin hacer antes un `buscarPorId`.

    **`ConcurrentHashMap` y no `HashMap`.** Tomcat atiende varias peticiones a la vez: un `HashMap` compartido entre hilos puede corromperse. Es un detalle que se pregunta.

??? success "Solución · el servicio, donde viven las reglas"

    ```java
    @Service
    public class EquipoServicio {

        private final EquipoRepositorio repositorio;

        public EquipoServicio(EquipoRepositorio repositorio) {    // (1)
            this.repositorio = repositorio;
        }

        public List<Equipo> listar() { return repositorio.listar(); }

        public Equipo obtener(Long id) {
            return repositorio.buscarPorId(id)
                    .orElseThrow(() -> new EquipoNoEncontradoException(id));
        }

        public Equipo crear(Equipo equipo) {
            return repositorio.guardar(equipo);
        }

        public Equipo cambiarEstado(Long id, Equipo.Estado nuevo) {
            var equipo = obtener(id);                              // (2)

            if (equipo.estado() == Equipo.Estado.BAJA) {           // (3)
                throw new TransicionNoValidaException(
                        "Un equipo dado de baja no vuelve a estar operativo");
            }
            return repositorio.guardar(equipo.con(nuevo));
        }

        public void borrar(Long id) {
            if (!repositorio.borrar(id)) {
                throw new EquipoNoEncontradoException(id);
            }
        }
    }
    ```

    1.  **Inyección por constructor.** Con un solo constructor, Spring no necesita `@Autowired`. Y el campo puede ser `final`, que es la mitad de la ventaja.
    2.  Reutiliza `obtener`, que ya lanza `404` si no existe. **Primero existencia, después estado** — ese orden se evalúa.
    3.  La regla de negocio, en el servicio. No en el controlador, no en el repositorio.

    **Por qué no inyectar por campo.** Esto también funciona:

    ```java
    @Autowired private EquipoRepositorio repositorio;   // NO
    ```

    …y trae tres problemas: el campo no puede ser `final`, no puedes construir la clase en un test sin Spring, y la clase oculta de qué depende. Por constructor, las dependencias están en la firma y a la vista.

??? success "Solución · el controlador, que solo habla HTTP"

    ```java
    @RestController
    @RequestMapping("/api/v1/equipos")
    public class EquipoControlador {

        private final EquipoServicio servicio;

        public EquipoControlador(EquipoServicio servicio) {
            this.servicio = servicio;
        }

        @GetMapping
        public List<Equipo> listar() {
            return servicio.listar();                              // (1)
        }

        @GetMapping("/{id}")
        public Equipo obtener(@PathVariable Long id) {
            return servicio.obtener(id);                           // (2)
        }

        @PostMapping
        public ResponseEntity<Equipo> crear(@RequestBody Equipo equipo) {
            var creado = servicio.crear(equipo);
            var ubicacion = ServletUriComponentsBuilder                // (3)
                    .fromCurrentRequest().path("/{id}")
                    .buildAndExpand(creado.id()).toUri();
            return ResponseEntity.created(ubicacion).body(creado);
        }

        @PatchMapping("/{id}/estado")
        public Equipo cambiarEstado(@PathVariable Long id,
                                    @RequestParam Equipo.Estado estado) {
            return servicio.cambiarEstado(id, estado);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)                      // (4)
        public void borrar(@PathVariable Long id) {
            servicio.borrar(id);
        }
    }
    ```

    1.  Devolver el objeto directamente ya da `200` y lo serializa a JSON. `ResponseEntity` solo hace falta cuando necesitas controlar el código o las cabeceras.
    2.  **No hay `try/catch`.** La excepción sube y la traduce el manejador global del R2. Un controlador lleno de `try/catch` es la señal de que falta ese manejador.
    3.  `ResponseEntity.created(uri)` pone el `201` **y** la cabecera `Location`. Construir la URI a mano —`URI.create("/api/v1/equipos/" + id)`— también vale, pero se rompe si cambias el prefijo.
    4.  `@ResponseStatus` para el `204`: más corto que devolver `ResponseEntity.noContent().build()`.

    **Comprobación con `curl`**, y mirando los **códigos**, no solo el cuerpo:

    ```bash
    curl -s localhost:8080/api/v1/equipos
    curl -i -X POST localhost:8080/api/v1/equipos \
         -H "Content-Type: application/json" \
         -d '{"etiqueta":"PC-042","tipo":"sobremesa","estado":"DISPONIBLE","ubicacion":"Aula 3"}'
    curl -i -X PATCH "localhost:8080/api/v1/equipos/1/estado?estado=PRESTADO"
    curl -i -X DELETE localhost:8080/api/v1/equipos/1
    curl -i localhost:8080/api/v1/equipos/999
    ```

??? success "Solución · el experimento que hay que hacer sí o sí"

    Crea una **segunda** implementación del repositorio:

    ```java
    @Repository
    public class EquipoRepositorioFichero implements EquipoRepositorio { … }
    ```

    Arranca. Falla:

    ```
    Parameter 0 of constructor in EquipoServicio required a single bean,
    but 2 were found:
        - equipoRepositorioMemoria
        - equipoRepositorioFichero
    ```

    Spring encuentra **dos candidatos** para la interfaz y no elige por ti. Hay tres formas de resolverlo:

    | Cómo | Cuándo |
    |---|---|
    | `@Primary` sobre una | Hay una por defecto clara |
    | `@Qualifier("nombre")` en el constructor | Quieres decidir en cada punto de uso |
    | `@Profile("fichero")` en cada una | Depende del entorno |

    Haz que falle **a propósito y ahora**, con el proyecto pequeño. Ese mensaje es el que más horas cuesta la primera vez que aparece en un proyecto grande — y cuando lo has provocado tú, lo reconoces en dos segundos.

---

# R2 · Que no se caiga con la cara descubierta

**Sesiones S10–S15** · Resuelto

## La situación

La API del R1 funciona. Y también hace esto:

```bash
curl -i localhost:8080/api/v1/equipos/999
```

```
HTTP/1.1 500
{"timestamp":"...","status":500,"error":"Internal Server Error",
 "trace":"es.iesx.inventario.EquipoNoEncontradoException: No existe el equipo 999
   at es.iesx.inventario.servicio.EquipoServicio.obtener(EquipoServicio.java:24)
   at ..."}
```

Un `500` con la traza entera. Además, cualquiera puede mandar `{"etiqueta":""}` y se guarda tan contento, y el cliente ve el modelo interno con todos sus campos.

## La pregunta

> **Haced que cada fallo devuelva el código que le toca y un cuerpo que sirva de algo, sin filtrar una sola línea de vuestro código al exterior.**

## Restricciones

- **Ni un `try/catch` en el controlador.** Si aparece uno, falta el manejador global.
- Los datos inválidos **no llegan al servicio**: se rechazan antes.
- El cliente **no ve el modelo interno**. Lo que entra y lo que sale son DTO.
- El IVA, la moneda y los límites **no están escritos en el código**.

## Criterios de aceptación

- [ ] `GET /equipos/999` → `404` con un cuerpo JSON explicativo, **sin traza**.
- [ ] `POST` con etiqueta vacía → `400` con el **mapa de errores por campo**.
- [ ] Una transición no válida → `409`.
- [ ] Un fallo inesperado → `500` genérico, **con la traza en el log** y no en la respuesta.
- [ ] Existe `CrearEquipoDto` y `EquipoRespuestaDto`, y el controlador solo habla de DTO.
- [ ] `inventario.iva` sale de `application.yml`.

??? success "Solución · DTO y validación"

    ```java
    public record CrearEquipoDto(
            @NotBlank(message = "La etiqueta es obligatoria")
            @Pattern(regexp = "^[A-Z]{2,3}-\\d{3,4}$",
                     message = "Formato esperado: PC-042")
            String etiqueta,

            @NotBlank String tipo,
            @NotNull Equipo.Estado estado,
            @NotBlank String ubicacion) {}
    ```

    ```java
    public record EquipoRespuestaDto(Long id, String etiqueta, String tipo,
                                     String estado, String ubicacion) {}
    ```

    ```java
    @Component
    public class EquipoMapeador {

        public Equipo aModelo(CrearEquipoDto dto) {
            return new Equipo(null, dto.etiqueta(), dto.tipo(),
                              dto.estado(), dto.ubicacion());
        }

        public EquipoRespuestaDto aDto(Equipo e) {
            return new EquipoRespuestaDto(e.id(), e.etiqueta(), e.tipo(),
                                          e.estado().name(), e.ubicacion());
        }
    }
    ```

    Y el controlador cambia dos líneas:

    ```java
    @PostMapping
    public ResponseEntity<EquipoRespuestaDto> crear(
            @Valid @RequestBody CrearEquipoDto dto) {       // (1)
        var creado = servicio.crear(mapeador.aModelo(dto));
        …
        return ResponseEntity.created(ubicacion).body(mapeador.aDto(creado));
    }
    ```

    1.  **Sin `@Valid` las anotaciones no hacen nada.** Es el fallo número uno de esta unidad: el DTO está lleno de `@NotBlank` y pasa cualquier cosa, porque nadie dispara la validación.

    **Para qué sirven los DTO, en dos razones concretas:**

    1. **Lo que entra.** Sin DTO, un `@RequestBody Equipo` permite que el cliente mande `{"id": 7}` y te machaque el identificador. Se llama *mass assignment* y es un fallo de seguridad real.
    2. **Lo que sale.** El modelo interno crece: mañana tiene `precioCompra`, `proveedor` o `notaInterna`. Con DTO, decides qué se publica; sin él, se publica todo lo que añadas.

??? success "Solución · el manejador global de errores"

    ```java
    @RestControllerAdvice
    public class ManejadorGlobalErrores {

        private static final Logger log =
                LoggerFactory.getLogger(ManejadorGlobalErrores.class);

        @ExceptionHandler(EquipoNoEncontradoException.class)
        public ProblemDetail noEncontrado(EquipoNoEncontradoException e) {
            var p = ProblemDetail.forStatusAndDetail(
                    HttpStatus.NOT_FOUND, e.getMessage());
            p.setTitle("Equipo no encontrado");
            p.setType(URI.create("https://api.iesx.es/errores/equipo-no-encontrado"));
            return p;                                              // (1)
        }

        @ExceptionHandler(TransicionNoValidaException.class)
        public ProblemDetail conflicto(TransicionNoValidaException e) {
            var p = ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT, e.getMessage());
            p.setTitle("Transición no permitida");
            return p;
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)   // (2)
        public ProblemDetail validacion(MethodArgumentNotValidException e) {
            var errores = e.getBindingResult().getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField,
                                              FieldError::getDefaultMessage,
                                              (a, b) -> a));
            var p = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Hay campos con errores");
            p.setTitle("Datos no válidos");
            p.setProperty("errores", errores);                     // (3)
            return p;
        }

        @ExceptionHandler(Exception.class)                         // (4)
        public ProblemDetail imprevisto(Exception e) {
            log.error("Fallo no controlado", e);                   // (5)
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ha ocurrido un error. Inténtalo más tarde.");
        }
    }
    ```

    1.  `ProblemDetail` es el estándar RFC 7807/9457, y viene en Spring desde la versión 6. No hace falta inventarse una clase de error.
    2.  Esta es **la excepción que lanza `@Valid`** cuando algo no cuadra. Sin este manejador, Spring devuelve un `400` con un cuerpo enorme e ilegible.
    3.  `setProperty` añade campos al JSON estándar. Aquí, el mapa `campo → mensaje`, que es lo que un formulario necesita para pintar los errores donde toca.
    4.  La red de seguridad. **Va la última**: Spring elige siempre el manejador más específico.
    5.  **La traza va al log, no a la respuesta.** Esta línea es la diferencia entre poder depurar y regalarle al atacante los nombres de tus clases y tus versiones.

    Resultado:

    ```bash
    curl -i localhost:8080/api/v1/equipos/999
    ```

    ```json
    {
      "type": "https://api.iesx.es/errores/equipo-no-encontrado",
      "title": "Equipo no encontrado",
      "status": 404,
      "detail": "No existe el equipo 999",
      "instance": "/api/v1/equipos/999"
    }
    ```

??? success "Solución · configuración fuera del código"

    ```yaml
    # application.yml
    inventario:
      iva: 21.0
      moneda: EUR
      maximo-por-aula: 30

    spring:
      application:
        name: inventario
    ```

    ```java
    @ConfigurationProperties(prefix = "inventario")
    public record InventarioProperties(double iva, String moneda, int maximoPorAula) {}
    ```

    ```java
    @SpringBootApplication
    @EnableConfigurationProperties(InventarioProperties.class)
    public class InventarioApplication { … }
    ```

    Y se inyecta como cualquier otra cosa:

    ```java
    public EquipoServicio(EquipoRepositorio repositorio, InventarioProperties props) { … }
    ```

    **Frente a `@Value("${inventario.iva}")`:** `@ConfigurationProperties` agrupa, valida y se puede inyectar entero. Con `@Value` acabas con quince cadenas sueltas repartidas por el proyecto y ningún sitio donde ver qué se configura.

    Y los **perfiles**, que es lo que hace que esto valga la pena:

    ```yaml
    # application-dev.yml   →  se activa con --spring.profiles.active=dev
    inventario:
      maximo-por-aula: 5
    ```

??? success "Solución · los tres tests que hay que tener"

    ```java
    @ExtendWith(MockitoExtension.class)
    class EquipoServicioTest {

        @Mock EquipoRepositorio repositorio;
        @InjectMocks EquipoServicio servicio;

        @Test
        void devuelveElEquipoCuandoExiste() {
            var equipo = new Equipo(1L, "PC-042", "sobremesa",
                                    Equipo.Estado.DISPONIBLE, "Aula 3");
            given(repositorio.buscarPorId(1L)).willReturn(Optional.of(equipo));

            assertThat(servicio.obtener(1L).etiqueta()).isEqualTo("PC-042");
        }

        @Test
        void lanzaCuandoNoExiste() {
            given(repositorio.buscarPorId(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> servicio.obtener(999L))
                    .isInstanceOf(EquipoNoEncontradoException.class);
        }

        @Test
        void noGuardaSiLaTransicionNoEsValida() {
            var baja = new Equipo(1L, "PC-042", "sobremesa",
                                  Equipo.Estado.BAJA, "Almacén");
            given(repositorio.buscarPorId(1L)).willReturn(Optional.of(baja));

            assertThatThrownBy(() ->
                    servicio.cambiarEstado(1L, Equipo.Estado.DISPONIBLE))
                    .isInstanceOf(TransicionNoValidaException.class);

            verify(repositorio, never()).guardar(any());           // (1)
        }
    }
    ```

    1.  **El `verify(..., never())` es la mitad del test.** Comprobar que lanza la excepción no demuestra que no haya guardado antes de lanzarla.

    Y uno de controlador, que prueba la capa HTTP **sin levantar la aplicación entera**:

    ```java
    @WebMvcTest(EquipoControlador.class)
    class EquipoControladorTest {

        @Autowired MockMvc mockMvc;
        @MockitoBean EquipoServicio servicio;                       // (1)

        @Test
        void devuelve404CuandoNoExiste() throws Exception {
            given(servicio.obtener(999L))
                    .willThrow(new EquipoNoEncontradoException(999L));

            mockMvc.perform(get("/api/v1/equipos/999"))
                   .andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.title").value("Equipo no encontrado"));
        }

        @Test
        void rechazaLaEtiquetaVacia() throws Exception {
            mockMvc.perform(post("/api/v1/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"etiqueta":"","tipo":"portátil",
                             "estado":"DISPONIBLE","ubicacion":"Aula 1"}"""))
                   .andExpect(status().isBadRequest())
                   .andExpect(jsonPath("$.errores.etiqueta").exists());
        }
    }
    ```

    1.  `@MockitoBean` sustituye a `@MockBean`, que está obsoleto desde Spring Boot 3.4.

    `@WebMvcTest` levanta **solo** la capa web: controladores, manejadores de error y conversión JSON. Ni repositorios ni base de datos. Por eso tarda menos de un segundo, y por eso hay que darle el servicio como doble.

---

# R3 · Reserva de aulas

**Sesiones S16–S18** · :material-pencil: **A entregar**

!!! reto "Este no lleva solución"
    Tiene el mismo tamaño, la misma forma y los mismos criterios que el **examen práctico de la UT4**. Si lo sacas, el examen es el mismo ejercicio con otro dominio.

## La situación

En el instituto hay seis aulas de informática y se reservan por WhatsApp. Cada semana hay al menos un choque: dos grupos en la misma aula a la misma hora.

## La pregunta

> **Construid una API que impida que dos reservas se solapen, y que diga con claridad por qué rechaza una reserva cuando la rechaza.**

## Lo que hay que entregar

Un proyecto Spring Boot con repositorio en memoria, capas separadas, DTO, validación, errores controlados y tests.

| Recurso | Operaciones |
|---|---|
| **Aula** | Listar, ver una |
| **Reserva** | Crear, listar (con filtro por aula y por día), ver una, anular |

## Restricciones

- **Nada de `new`** para conectar capas; todo por inyección de constructor.
- **Ni un `try/catch`** en los controladores.
- El cliente **no ve el modelo interno**: DTO de entrada y de salida.
- La comprobación de solape vive en **el servicio**, no en el controlador.
- La configuración —horario de apertura y cierre, duración máxima— va en `application.yml`.

## Criterios de aceptación

Cada uno se comprueba con una orden. Los seis puntos son los del examen.

| # | Qué | Peso |
|:-:|---|:-:|
| **1** | Las tres capas separadas, con la interfaz del repositorio y sin ningún `new` propio | 1,5 |
| **2** | CRUD completo con los códigos correctos: `200`, `201` + `Location`, `204`, `404` | 2,0 |
| **3** | DTO de entrada y salida, con validación que devuelve `400` y el mapa de errores por campo | 2,0 |
| **4** | Solapes: crear una reserva que pise a otra devuelve **`409`** con un cuerpo que explica con cuál choca | 1,5 |
| **5** | Manejador global con `ProblemDetail`; ninguna traza en la respuesta y sí en el log | 1,0 |
| **6** | Al menos **cuatro tests**: caso feliz, caso de error, un `verify(..., never())` y uno de `@WebMvcTest` | 2,0 |

## Cómo sabrás que está bien

```bash
# 1 · crear un aula y una reserva
curl -i -X POST localhost:8080/api/v1/reservas \
     -H "Content-Type: application/json" \
     -d '{"aulaId":1,"dia":"2027-01-19","desde":"10:00","hasta":"12:00","grupo":"2DAW"}'
# → 201 + Location

# 2 · la misma franja otra vez
curl -i -X POST localhost:8080/api/v1/reservas \
     -H "Content-Type: application/json" \
     -d '{"aulaId":1,"dia":"2027-01-19","desde":"11:00","hasta":"13:00","grupo":"1DAM"}'
# → 409, y el cuerpo dice con qué reserva choca

# 3 · datos inválidos
curl -i -X POST localhost:8080/api/v1/reservas \
     -H "Content-Type: application/json" \
     -d '{"aulaId":1,"dia":"2027-01-19","desde":"13:00","hasta":"11:00","grupo":""}'
# → 400, con errores.grupo y errores.hasta

# 4 · no existe
curl -i localhost:8080/api/v1/reservas/9999
# → 404 sin traza
```

!!! tip "Las dos trampas"
    **El solape.** Dos franjas se solapan si `desde < otraHasta` **y** `hasta > otroDesde`. Con `<=` te cuelas: una reserva de 10 a 12 y otra de 12 a 14 **no** chocan, se tocan. Prueba ese caso exacto antes de entregar.

    **El orden de las comprobaciones.** Primero que el aula exista (`404`), después que la franja esté libre (`409`). Si lo haces al revés, pedir una reserva en un aula inexistente te devuelve un `409` incomprensible.

## Cómo se entrega

Un `.zip` del proyecto **sin la carpeta `target`**, o el repositorio de GitHub Classroom. Antes de entregar:

- [ ] `mvn test` termina en verde. **Un proyecto que no compila es un cero**, por bien que esté lo demás.
- [ ] `mvn spring-boot:run` arranca y las cuatro órdenes de arriba dan lo que dicen.
- [ ] No hay ninguna traza de excepción en ninguna respuesta.
- [ ] `grep -rn "new .*Repositorio" src/main` no devuelve nada.
