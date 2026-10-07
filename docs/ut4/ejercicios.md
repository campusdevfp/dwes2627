# Batería de ejercicios — UT4

**24 ejercicios con solución**, en el orden de los temas y de menos a más dentro de cada bloque.

| | |
|:-:|---|
| ● | Cinco minutos, con el tema delante |
| ●● | Hay que juntar dos ideas |
| ●●● | Se escribe y se ejecuta |

!!! tip "Esto se hace sobre el proyecto, no en abstracto"
    Ten el [proyecto de Funkos](04-proyecto-completo.md) arrancado en una terminal y `curl` en otra. La mitad de los ejercicios se contestan rompiendo algo a propósito y leyendo el error.

---

# Bloque 1 · Spring Boot, beans e inyección

> Tema [1. Introducción a Spring Boot](01-introduccion-spring-boot.md)

## E1 ● — Las cuatro anotaciones de componente

¿Qué diferencia real hay entre `@Component`, `@Service`, `@Repository` y `@Controller`?

??? success "Solución"

    **Para el contenedor, ninguna**: las cuatro registran un bean, y las tres últimas están anotadas con `@Component`.

    Las diferencias:

    - **Semántica**, para quien lee: negocio / datos / web.
    - **Funcional, solo en una**: `@Repository` activa la traducción de excepciones. Las `SQLException` del driver se convierten en la jerarquía `DataAccessException` de Spring, que no es comprobada y no ata tu código a JDBC.

    `@RestController` = `@Controller` + `@ResponseBody`.

## E2 ● — IoC y DI

Una frase para cada uno.

??? success "Solución"

    - **IoC** es el **principio**: el control de la creación de objetos lo tiene el framework, no tu código.
    - **DI** es la **técnica** con la que Spring lo aplica: las dependencias entran por el constructor en lugar de crearse dentro con `new`.

    Es la pregunta de examen del tema. Confundirlas es decir «el motor» cuando te preguntan por «la combustión».

## E3 ●● — Por constructor y no por campo

Da tres razones.

??? success "Solución"

    1. El campo puede ser **`final`**: el objeto nace completo y nadie le cambia la dependencia.
    2. **No se puede construir mal.** Con `@Autowired` en el campo, si falta el bean obtienes un objeto a medias y un `NullPointerException` en la primera petición.
    3. **Se instancia en un test sin Spring**: `new Servicio(mock)`.

    Y una cuarta, de diseño: un constructor con siete parámetros **se ve**. La inyección por campo esconde que la clase hace demasiado.

## E4 ●● — El bean que no aparece

```
Parameter 0 of constructor in es.iesx.funkos...FunkosRestController
required a bean of type '...FunkosService' that could not be found.
```

Tres causas.

??? success "Solución"

    1. **Falta la anotación**: el `FunkosServiceImpl` no lleva `@Service`.
    2. **Está fuera del paquete escaneado.** `@ComponentScan` mira el paquete de la clase `@SpringBootApplication` y los de debajo. Una clase en `es.otro.cosa` no se encuentra.
    3. **Es una interfaz sin implementación anotada.** Y si hay **dos** implementaciones, el error es distinto (`required a single bean, but 2 were found`) y se resuelve con `@Primary` o `@Qualifier`.

    La (2) es la que más tiempo hace perder, porque el código parece correcto.

## E5 ●● — El singleton con estado

```java
@Service
public class CarritoService {
    private List<String> items = new ArrayList<>();
    public void añadir(String i) { items.add(i); }
    public List<String> ver()    { return items; }
}
```

??? success "Solución"

    **Todos los usuarios comparten el mismo carrito**: hay una sola instancia del servicio para toda la aplicación.

    Y además `ArrayList` no es seguro con varios hilos: dos peticiones simultáneas pueden corromperlo.

    El estado por usuario vive **en la base de datos** o en la sesión. Un `@Service` bien escrito es una colección de funciones sin memoria.

## E6 ●● — `@Bean` o `@Component`

Necesitas un bean de `RestClient` con la URL base configurada. ¿Cuál?

??? success "Solución"

    **`@Bean` dentro de una `@Configuration`**, porque `RestClient` es de una librería: no puedes anotar su clase.

    ```java
    @Configuration
    public class ClientesConfig {
        @Bean
        public RestClient apiExterna(@Value("${api.externa.url}") String url) {
            return RestClient.builder().baseUrl(url).build();
        }
    }
    ```

    **La regla:** `@Component` para **tus** clases; `@Bean` cuando el objeto lo construyes tú porque la clase no es tuya o necesita configuración.

---

# Bloque 2 · Rutas, peticiones y respuestas

> Tema [2. Spring Web REST](02-spring-web-rest.md)

## E7 ● — Los códigos del CRUD

Completa la tabla para `/api/v1/funkos`.

??? success "Solución"

    | Operación | Éxito | No existe | Datos malos | Choca |
    |---|:-:|:-:|:-:|:-:|
    | `GET` lista | 200 | — (200 vacía) | — | — |
    | `GET /{id}` | 200 | **404** | — | — |
    | `POST` | **201** + `Location` | — | 400 | **409** |
    | `PUT /{id}` | 200 | **404** | 400 | 409 |
    | `PATCH /{id}` | 200 | **404** | 400 | 409 |
    | `DELETE /{id}` | **204** | **404** | — | 409 |

    Lo que se falla: **lista vacía es 200** (el recurso colección existe, está vacío) y **el 201 lleva cabecera `Location`** con la URL de lo creado.

## E8 ● — `@PathVariable` o `@RequestParam`

(a) `/funkos/42` · (b) `/funkos?categoria=disney` · (c) `/funkos?page=2&size=20`

??? success "Solución"

    ```java
    // (a) identifica UN recurso
    @GetMapping("/{id}")
    public ResponseEntity<FunkoResponse> uno(@PathVariable Long id) { … }

    // (b) filtra la colección
    @GetMapping
    public ResponseEntity<List<FunkoResponse>> filtrar(
            @RequestParam Optional<String> categoria) { … }

    // (c) cambia cómo se presenta
    @GetMapping
    public ResponseEntity<List<FunkoResponse>> pagina(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) { … }
    ```

    **El criterio:** si identifica el recurso, va en la ruta; si filtra, ordena o pagina, va en la query. `/funkos/disney` estaría mal: `disney` no es un funko.

## E9 ● — El 415

POST con JSON válido → `415 Unsupported Media Type`. ¿Qué falta?

??? success "Solución"

    La cabecera **`Content-Type: application/json`**.

    `@RequestBody` necesita saber en qué formato viene el cuerpo para elegir el convertidor. Sin ella no hay ninguno que acepte la petición, y el cuerpo **no se llega a leer**: el error es anterior a mirarlo.

    En Postman: **Body → raw → JSON**, no «Text».

## E10 ●● — El 500 que debería ser 404

```java
@GetMapping("/{id}")
public ResponseEntity<Funko> uno(@PathVariable Long id) {
    return ResponseEntity.ok(repositorio.findById(id).get());
}
```

??? success "Solución"

    Con un id inexistente, el `.get()` sobre un `Optional` vacío lanza `NoSuchElementException` y Spring responde **500 Internal Server Error**.

    Está mal porque **el servidor no ha fallado**: el recurso no existe, y eso es un **404**.

    ```java
    // opción corta
    return repositorio.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());

    // opción del proyecto: el servicio lanza y la excepción lleva el código
    return ResponseEntity.ok(servicio.findById(id));   // lanza FunkoNotFoundException
    ```

    Es el error número 1 de la unidad y viene directo de no haber interiorizado el `Optional` de la UT2.

## E11 ●● — PUT frente a PATCH

¿Qué debería pasar con un `PUT /funkos/1` que solo manda `{"nombre":"X"}`?

??? success "Solución"

    **Los demás campos deberían quedar vacíos**, porque PUT **reemplaza el recurso entero**.

    - **PUT**: lo que no mandas, se pierde. Es **idempotente**: mandarlo dos veces deja el mismo estado.
    - **PATCH**: lo que no mandas, se conserva.

    Si tu PUT conserva los campos que faltan, **has implementado un PATCH y lo has llamado PUT**. Y por eso en el proyecto el PUT usa `FunkoCreateRequest` (con los `@NotNull`) y el PATCH usa `FunkoUpdateRequest` (sin ellos): el propio DTO impide mandar un PUT incompleto.

## E12 ●● — Versionar

Tu `/api/v1/funkos` está en producción con tres clientes. Hay que cambiar `precio` de número a `{importe, moneda}`.

??? success "Solución"

    **No se toca la v1: se crea `/api/v2/funkos`.**

    Cambiar el tipo de un campo es **incompatible hacia atrás**: los tres clientes se rompen el día del despliegue y tú no controlas cuándo se actualizan.

    ```java
    @RestController @RequestMapping("/api/v1/funkos")
    public class FunkosV1Controller { … }   // precio: 35.0

    @RestController @RequestMapping("/api/v2/funkos")
    public class FunkosV2Controller { … }   // precio: {importe, moneda}
    ```

    Las dos usan **el mismo `@Service`**; lo que cambia es el DTO de salida. Por eso el servicio no devuelve entidades.

    Y lo que **sí** se puede hacer sin versionar: **añadir** un campo. Un cliente que no lo conoce lo ignora.

## E13 ●●● — El controlador entero

Escribe `FunkosRestController` con las seis rutas, sin un solo `if` y sin capturar ninguna excepción.

??? success "Solución"

    ```java
    @RestController
    @RequestMapping("${api.path:/api}/${api.version:v1}/funkos")
    public class FunkosRestController {

        private final FunkosService servicio;

        public FunkosRestController(FunkosService servicio) {
            this.servicio = servicio;
        }

        @GetMapping
        public ResponseEntity<List<FunkoResponse>> findAll(
                @RequestParam Optional<String> categoria) {
            return ResponseEntity.ok(servicio.findAll(categoria));
        }

        @GetMapping("/{id}")
        public ResponseEntity<FunkoResponse> findById(@PathVariable Long id) {
            return ResponseEntity.ok(servicio.findById(id));
        }

        @PostMapping
        public ResponseEntity<FunkoResponse> create(
                @Valid @RequestBody FunkoCreateRequest dto) {
            var creado = servicio.save(dto);
            return ResponseEntity
                    .created(URI.create("/api/v1/funkos/" + creado.id()))
                    .body(creado);
        }

        @PutMapping("/{id}")
        public ResponseEntity<FunkoResponse> replace(
                @PathVariable Long id, @Valid @RequestBody FunkoCreateRequest dto) {
            return ResponseEntity.ok(servicio.replace(id, dto));
        }

        @PatchMapping("/{id}")
        public ResponseEntity<FunkoResponse> update(
                @PathVariable Long id, @Valid @RequestBody FunkoUpdateRequest dto) {
            return ResponseEntity.ok(servicio.update(id, dto));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Long id) {
            servicio.deleteById(id);
            return ResponseEntity.noContent().build();
        }
    }
    ```

    Cada método hace **tres cosas**: recibe, delega, envuelve. Las excepciones suben con su `@ResponseStatus` y la validación la dispara el `@Valid`.

---

# Bloque 3 · Servicios, DTOs, excepciones y caché

> Tema [3. Servicios, DTOs y caché](03-servicios-dtos-y-cache.md)

## E14 ● — Tres razones para no devolver la entidad

??? success "Solución"

    1. **Expone datos que no debe**: `passwordHash`, `rol`, el id interno del proveedor.
    2. **Ata el contrato de la API a la tabla**: renombrar una columna rompe a los clientes.
    3. **Explota con las relaciones** (UT5): `Pedido → Cliente → List<Pedido>` hace que Jackson entre en recursión (`StackOverflowError`), y una relación `LAZY` serializada fuera de la transacción lanza `LazyInitializationException`.

    Y una cuarta: **no puedes tener dos vistas del mismo dato**, que es lo que necesitas para versionar (E12).

## E15 ● — Por qué el DTO de creación no lleva `id`

??? success "Solución"

    Porque **el id lo asigna el servidor**.

    Si el DTO lo lleva, un cliente puede mandar `{"id": 1, "nombre": "..."}` y, según cómo esté escrito el `save`, **sobrescribir el registro 1** en lugar de crear uno nuevo. Es una vulnerabilidad, no un detalle de estilo.

    Igual con `createdAt`: las fechas las pone el servidor con `LocalDateTime.now()`, porque el reloj del cliente no es de fiar.

## E16 ●● — `@Valid` olvidado

¿Qué pasa y cómo lo detectas?

??? success "Solución"

    **Nada**: las anotaciones del DTO no se ejecutan. Un POST con el nombre vacío devuelve **201 Created**.

    No hay warning de compilación ni log. Solo datos basura en la base de datos.

    Se detecta con un test de capa web:

    ```java
    @Test
    void postConNombreVacioDa400() throws Exception {
        mockMvc.perform(post("/api/v1/funkos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"nombre":"","precio":10.0,"cantidad":1,"categoria":"ANIME"}"""))
               .andExpect(status().isBadRequest());
    }
    ```

    Sin el `@Valid` falla con «expected 400, was 201», y eso es exactamente lo que quieres que ocurra.

## E17 ●● — Las dos validaciones

(a) nombre no vacío · (b) nombre no repetido · (c) precio positivo · (d) no borrar un funko con pedidos

??? success "Solución"

    | | Dónde | Cómo | Código |
    |---|---|---|:-:|
    | (a) | DTO | `@NotBlank` | 400 |
    | (b) | **Servicio** | `existsByNombre` → `FunkoConflictException` | **409** |
    | (c) | DTO | `@PositiveOrZero` | 400 |
    | (d) | **Servicio** | consulta → excepción | **409** |

    **El criterio:** si para comprobarlo hay que mirar la base de datos u otros campos, no es formato. Las anotaciones solo ven **un campo aislado**.

    Y el código: **400** es «lo que me mandas está mal escrito»; **409 Conflict** es «está bien escrito pero choca con el estado actual».

## E18 ●● — `ResponseStatusException` o excepción propia

??? success "Solución"

    | | `ResponseStatusException` | Excepción propia |
    |---|---|---|
    | Código a escribir | Una línea | Una clase por caso |
    | ¿El servicio sabe de HTTP? | **Sí** | No |
    | Testear | Hay que inspeccionar el `HttpStatus` | `assertThrows(FunkoNotFoundException.class, …)` |
    | Reutilizar en WebSocket / GraphQL | No se puede | Sí |
    | Capturar todo un dominio | No | `catch (FunkoException e)` |

    **Excepción propia**, por la penúltima fila: en la UT6 ese servicio atiende un WebSocket, donde `HttpStatus.NOT_FOUND` no significa nada.

## E19 ●● — La caché que miente

```java
@Cacheable public List<FunkoResponse> findAll() { … }
           public FunkoResponse save(FunkoCreateRequest f) { … }
```

??? success "Solución"

    Creas un funko, pides la lista y **ves la de antes**, sin error ninguno.

    ```java
    @CacheEvict(cacheNames = "funkos", allEntries = true)
    public FunkoResponse save(FunkoCreateRequest f) { … }
    ```

    Tiene que ser `allEntries = true`: un elemento nuevo invalida **todas** las listas cacheadas.

    **La regla:** `save`, `update` y `delete`, los tres con `@CacheEvict`.

## E20 ●● — La caché que no se activa

Pones `@Cacheable` y el método se ejecuta siempre. Dos causas.

??? success "Solución"

    1. **Falta `@EnableCaching`** en la clase `@SpringBootApplication`. Sin ella las anotaciones son decoración.
    2. **La llamada es interna.** `this.findById(id)` desde otro método del mismo bean **no pasa por el proxy**, y la caché vive en el proxy.

    ```java
    public FunkoResponse metodoA(Long id) {
        return findById(id);          // ← NO pasa por la caché
    }
    @Cacheable
    public FunkoResponse findById(Long id) { … }
    ```

    Lo mismo le pasa a `@Transactional` en la UT5, y por el mismo motivo.

## E21 ●●● — El PATCH del mapeador

Escribe el método que aplica un `FunkoUpdateRequest` sobre un `Funko` existente.

??? success "Solución"

    ```java
    public Funko toModelParcheando(Funko original, FunkoUpdateRequest r) {
        return Funko.builder()
                .id(original.getId())
                .nombre(r.nombre()       != null ? r.nombre()       : original.getNombre())
                .precio(r.precio()       != null ? r.precio()       : original.getPrecio())
                .cantidad(r.cantidad()   != null ? r.cantidad()     : original.getCantidad())
                .imagen(r.imagen()       != null ? r.imagen()       : original.getImagen())
                .categoria(r.categoria() != null ? r.categoria()    : original.getCategoria())
                .createdAt(original.getCreatedAt())   // nunca se toca
                .updatedAt(LocalDateTime.now())       // siempre se refresca
                .build();
    }
    ```

    Los `!= null` **son** la semántica del PATCH.

    Y el límite de este diseño: **no se puede poner un campo a `null` a propósito**, porque `null` ya significa «no lo cambies». Resolverlo bien requiere JSON Merge Patch y no entra en el módulo.

---

# Bloque 4 · Configuración y tests

> Temas [5. Configuración](05-configuracion.md) y [4. Proyecto completo](04-proyecto-completo.md)

## E22 ● — `${VAR:defecto}`

```properties
a=${PORT:8080}
b=${DB_PASSWORD}
```

??? success "Solución"

    - **(a)** Usa `PORT` si existe; si no, `8080`. **Arranca siempre.**
    - **(b)** Si `DB_PASSWORD` no existe, **la aplicación no arranca**: `Could not resolve placeholder`.

    **El criterio:** valor por defecto en lo inocuo (puertos, rutas, tamaños), para que el proyecto funcione recién clonado. Sin valor por defecto en lo sensible, para que un despliegue mal configurado falle en el arranque.

    Arrancar con una contraseña por defecto es peor que no arrancar.

## E23 ●● — Quién gana

`server.port=8080` en `application.properties`, `9090` en `application-prod.properties`, `SERVER_PORT=7070` en el entorno, y arrancas con `--server.port=6060 --spring.profiles.active=prod`.

??? success "Solución"

    **6060.**

    ```
    1. --server.port=6060              ← gana
    2. SERVER_PORT=7070
    3. application-prod.properties     9090
    4. application.properties          8080
    ```

    **Cuanto más específico de este arranque concreto, más prioridad.**

## E24 ●●● — Los dos tests

Escribe un test de servicio con Mockito y uno de capa web con `MockMvc` para el caso «id que no existe».

??? success "Solución"

    ```java
    // 1 · servicio, sin Spring
    @ExtendWith(MockitoExtension.class)
    class FunkosServiceImplTest {

        @Mock FunkosRepository repositorio;
        @Spy  FunkoMapper mapper = new FunkoMapper();
        @InjectMocks FunkosServiceImpl servicio;

        @Test
        void findByIdQueNoExisteLanzaNotFound() {
            when(repositorio.findById(99L)).thenReturn(Optional.empty());

            var ex = assertThrows(FunkoNotFoundException.class,
                                  () -> servicio.findById(99L));
            assertTrue(ex.getMessage().contains("99"));
            verify(repositorio, times(1)).findById(99L);
        }
    }
    ```

    ```java
    // 2 · capa web, con Spring pero con el servicio mockeado
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    class FunkosRestControllerTest {

        @Autowired MockMvc mockMvc;
        @MockitoBean FunkosService servicio;

        @Test
        void getIdQueNoExisteDevuelve404() throws Exception {
            when(servicio.findById(99L)).thenThrow(new FunkoNotFoundException(99L));

            mockMvc.perform(get("/api/v1/funkos/99"))
                   .andExpect(status().isNotFound());
        }
    }
    ```

    **Dos niveles distintos:**

    | | Qué prueba | Levanta Spring | Tarda |
    |---|---|:-:|---|
    | 1 | Que el servicio **lanza** la excepción | No | ms |
    | 2 | Que esa excepción **se traduce a 404** | Sí | ~2 s |

    El primero no sabe que existe HTTP. El segundo no sabe qué hace el servicio. Entre los dos cubren la unidad, y el `@Valid` olvidado **solo** lo caza el segundo.

---

## Reparto sugerido

| Sesión | Bloque | Ejercicios |
|:-:|---|---|
| **S1** | 1 · Spring Boot y beans | E1 – E3 |
| **S2** | 1 · Inyección y configuración de beans | E4 – E6 |
| **S3** | 2 · Rutas y respuestas | E7 – E10 |
| **S4** | 2 · Verbos y versionado | E11 – E13 |
| **S5** | 3 · DTOs y validación | E14 – E17 |
| **S6** | 3 · Excepciones y caché | E18 – E21 |
| **S7** | 4 · Configuración y tests | E22 – E24 |
| **S8–S9** | — | [Reto 3](retos.md) |
| **S10** | — | Examen práctico |

!!! success "Si vas justo de tiempo"
    El mínimo: **E1, E7, E9, E10, E14, E16, E17, E19, E22**.

    Y los tres que separan un 5 de un 8: **E10** (el 500 que era un 404), **E16** (`@Valid` olvidado) y **E19** (la caché que miente). Los tres son fallos que el programa no te señala.
