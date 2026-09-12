# Testing de APIs REST

> En la UT4 probaste servicios y controladores. Aquí pruebas **la API completa**: paginación, filtros, seguridad y flujos reales de negocio.

## 1. Qué hay que probar en una API

| Aspecto | Qué se comprueba |
|---|---|
| **Contrato** | Códigos de estado, cabeceras (`Location`, `Content-Type`), forma del JSON |
| **Validación** | Cada regla de entrada devuelve 400 con su mensaje |
| **Errores** | Cada excepción de negocio devuelve el código correcto en formato Problem Details |
| **Paginación** | Tamaños, límites, páginas fuera de rango, ordenación |
| **Filtros** | Cada filtro por separado y varios combinados |
| **Seguridad** | 401 sin token, 403 con rol insuficiente, 200 con el rol correcto |
| **Flujos** | Crear → leer → modificar → borrar de extremo a extremo |

## 2. Tests de contrato con `@WebMvcTest`

``` { .java .numerado }
@WebMvcTest(ProductoControlador.class)
class ProductoApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean ProductoServicio servicio;

    @Test
    void postCreadoDevuelve201YLocation() throws Exception {
        when(servicio.crear(any())).thenReturn(new Producto(7, "Casco", "seguridad", 49.99, 10));

        mvc.perform(post("/api/v1/productos")
                .contentType(APPLICATION_JSON)
                .content("""
                    {"nombre":"Casco","categoria":"seguridad","precio":49.99,"stock":10}"""))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/api/v1/productos/7")))
            .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void errorDevuelveProblemDetail() throws Exception {
        when(servicio.obtener(99)).thenThrow(new ProductoNoEncontradoException(99));

        mvc.perform(get("/api/v1/productos/99"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.instance").value("/api/v1/productos/99"));
    }
}
```

### Probar la validación con `@ParameterizedTest`

Un test por cada campo inválido es repetitivo. Mejor una tabla:

```java
@ParameterizedTest(name = "{1} → 400")
@MethodSource("cuerposInvalidos")
void cuerpoInvalidoDevuelve400(String cuerpo, String campoEsperado) throws Exception {
    mvc.perform(post("/api/v1/productos").contentType(APPLICATION_JSON).content(cuerpo))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errores." + campoEsperado).exists());

    verifyNoInteractions(servicio);
}

static Stream<Arguments> cuerposInvalidos() {
    return Stream.of(
        arguments("""{"nombre":"","categoria":"seguridad","precio":10,"stock":1}""", "nombre"),
        arguments("""{"nombre":"ab","categoria":"seguridad","precio":10,"stock":1}""", "nombre"),
        arguments("""{"nombre":"Casco","categoria":"","precio":10,"stock":1}""", "categoria"),
        arguments("""{"nombre":"Casco","categoria":"seguridad","precio":-1,"stock":1}""", "precio"),
        arguments("""{"nombre":"Casco","categoria":"seguridad","precio":0,"stock":1}""", "precio"),
        arguments("""{"nombre":"Casco","categoria":"seguridad","precio":10,"stock":-5}""", "stock"));
}
```

Seis casos, un método, y añadir el séptimo cuesta una línea.

### Probar la paginación

``` { .java .numerado }
@Test
void devuelveLaPaginaSolicitadaConSusMetadatos() throws Exception {
    var contenido = List.of(new Producto(1, "A", "seguridad", 10.0, 1),
                            new Producto(2, "B", "movilidad", 20.0, 2));
    var pageable = PageRequest.of(1, 2, Sort.by("nombre"));
    when(servicio.buscar(any(), any())).thenReturn(new PageImpl<>(contenido, pageable, 10));

    mvc.perform(get("/api/v1/productos?page=1&size=2&sort=nombre,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(2)))
        .andExpect(jsonPath("$.page.number").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(10))
        .andExpect(jsonPath("$.page.totalPages").value(5))
        .andExpect(jsonPath("$.page.first").value(false));
}

@Test
void seRecibenLosParametrosDePaginacionCorrectos() throws Exception {
    when(servicio.buscar(any(), any())).thenReturn(Page.empty());

    mvc.perform(get("/api/v1/productos?page=3&size=7&sort=precio,desc"));

    var captor = ArgumentCaptor.forClass(Pageable.class);
    verify(servicio).buscar(any(), captor.capture());
    assertThat(captor.getValue().getPageNumber()).isEqualTo(3);
    assertThat(captor.getValue().getPageSize()).isEqualTo(7);
    assertThat(captor.getValue().getSort().getOrderFor("precio").getDirection())
        .isEqualTo(Sort.Direction.DESC);
}
```

Ese segundo test comprueba algo que el primero no ve: que Spring **traduce bien la query string** al `Pageable` que recibe tu servicio.

## 3. Tests de seguridad *(se adelanta de la UT7)*

!!! info "Por qué está aquí si la seguridad es de la UT7"
    Las herramientas —`@WithMockUser`, probar un 401 frente a un 403— son **de testing**, y este es el tema de testing. Conviene conocerlas ahora aunque tu API todavía no esté protegida.

    En la **UT7**, cuando le pongas Spring Security, volverás a esta página y todo esto se usará de verdad. **En el examen de esta unidad no entra**: aquí se evalúan tests de estado, `jsonPath`, paginación y filtros.


``` { .java .numerado }
@WebMvcTest(ProductoControlador.class)
@Import(SecurityConfig.class)  // sin esto, la seguridad NO se aplica en el slice
class ProductoSeguridadTest {

    @Autowired MockMvc mvc;
    @MockitoBean ProductoServicio servicio;
    @MockitoBean JwtServicio jwtServicio;
    @MockitoBean UserDetailsService userDetailsService;

    @Test
    void getEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    @Test
    void deleteSinTokenDevuelve401() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "luis", roles = "USER")
    void deleteConRolUserDevuelve403() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ana", roles = "ADMIN")
    void deleteConRolAdminDevuelve204() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isNoContent());
        verify(servicio).borrar(1);
    }
}
```

!!! bug "Dos trampas de `@WithMockUser`"
    1. Si olvidas `@Import(SecurityConfig.class)`, `@WebMvcTest` usa una configuración de seguridad por defecto y tus tests de 401/403 pasan por casualidad o fallan sin motivo.
    2. @WithMockUser(roles = "ADMIN") genera la autoridad `ROLE_ADMIN`. Si escribes roles = "ROLE_ADMIN", obtendrás `ROLE_ROLE_ADMIN` y un 403 que te volverá loco.

### Probar el login de verdad

```java
@SpringBootTest
@AutoConfigureMockMvc
class CatalogoIntegracionTest {

    @Autowired MockMvc mvc;

    @Test
    void elFlujoCompletoDeAltaYConsulta() throws Exception {
        var respuesta = mvc.perform(post("/api/v1/productos").contentType(APPLICATION_JSON)
                .content("""{"nombre":"Teclado","precio":29.90,"categoria":"PERIFERICO"}"""))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(respuesta))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.nombre").value("Teclado"));
    }
}
```

## 4. Test de extremo a extremo con servidor real

`MockMvc` no arranca un servidor: simula el `DispatcherServlet`. Para probar con HTTP de verdad (conversión de cabeceras, cliente real):

``` { .java .numerado }
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TiendaE2ETest {

    @Autowired TestRestTemplate rest;

    @Test
    void flujoCompletoConAutenticacion() {
        // 1. Login
        var login = rest.postForEntity("/auth/login",
            new LoginPeticion("ana", "1234"), LoginRespuesta.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);

        var cabeceras = new HttpHeaders();
        cabeceras.setBearerAuth(login.getBody().token());

        // 2. Crear
        var nuevo = new CrearProductoDto("Patinete E2E", "movilidad", 299.0, 4);
        var creado = rest.exchange("/api/v1/productos", HttpMethod.POST,
            new HttpEntity<>(nuevo, cabeceras), ProductoDto.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(creado.getHeaders().getLocation()).isNotNull();

        var uri = creado.getHeaders().getLocation().getPath();

        // 3. Leer siguiendo el Location
        var leido = rest.getForEntity(uri, ProductoDto.class);
        assertThat(leido.getBody().nombre()).isEqualTo("Patinete E2E");

        // 4. Borrar
        var borrado = rest.exchange(uri, HttpMethod.DELETE,
            new HttpEntity<>(cabeceras), Void.class);
        assertThat(borrado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 5. Ya no existe
        assertThat(rest.getForEntity(uri, String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

Este test recorre **todo**: seguridad, validación, mapeo, persistencia y códigos. Si pasa, la API funciona.

## 5. Estrategia y buenas prácticas

- Nombres que describen comportamiento: `deleteConRolUserDevuelve403`, no test3.
- Patrón AAA: Arrange (prepara), Act (ejecuta), Assert (comprueba). Una sola acción por test.
- Tests independientes: nada de que el test B dependa de que el A creara un producto. Si comparten estado, usa `@DirtiesContext` o reinicia los datos en `@BeforeEach`.
- Deterministas: sin `LocalDateTime.now()` sin control ni datos aleatorios. Un test que falla una vez de cada diez es peor que no tener test.
- `.andDo(print())`
    cuando algo no cuadra: imprime petición y respuesta completas.

```bash
mvn test
mvn test -Dtest="*ApiTest"
mvn verify                       # tests + empaquetado
```

---

## Pruébalo ahora (40 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1 — Contrato.** Escribe `ProductoApiTest` cubriendo los seis códigos: 200, 201+Location, 204, 400, 404, 409.

**Parte 2 — Validación parametrizada.** Convierte tus tests de validación a un `@ParameterizedTest` con `@MethodSource` y al menos 6 casos inválidos.

**Parte 3 — Paginación.** Dos tests: uno que comprueba los metadatos de la respuesta y otro que, con `ArgumentCaptor`, verifica que el `Pageable` que llega al servicio es el que pidió el cliente.

**Parte 4 — Seguridad.** La matriz completa: GET público 200 · DELETE sin token 401 · DELETE con USER 403 · DELETE con ADMIN 204. Recuerda `@Import(SecurityConfig.class)`.

**Parte 5 — E2E.** Un `@SpringBootTest(webEnvironment = RANDOM_PORT)` con el flujo login → crear → leer → borrar → 404.

**Parte 6 — Rompe y comprueba.** Verifica que tus tests sirven de algo:

| Rotura deliberada | Test que debe ponerse rojo |
|---|---|
| Cambia `.hasRole("ADMIN")` por `.permitAll()` en el DELETE | el de 403 |
| Quita `@Valid` del POST | los parametrizados de 400 |
| Devuelve `ok()` en vez de `created()` | el de 201 + Location |
| Pon `@PageableDefault(size = 5)` fijo ignorando `size` | el del `ArgumentCaptor` |
| Quita la caducidad del JWT | ninguno… ⇒ **te falta ese test** |

---

## Ejercicios (con solución)

### Ejercicio 1 — Elige la herramienta

(a) el descuento se calcula bien · (b) el POST devuelve 201 con `Location` · (c) un USER no puede borrar · (d) todo el flujo con servidor HTTP real · (e) los parámetros de paginación llegan bien al servicio.

??? success "Solución"

    (a) test unitario con Mockito · (b) `@WebMvcTest` · (c) `@WebMvcTest` + `@Import(SecurityConfig)` + `@WithMockUser` · (d) `@SpringBootTest(webEnvironment = RANDOM_PORT)` con `TestRestTemplate` · (e) `@WebMvcTest` con ArgumentCaptor<Pageable>.


### Ejercicio 2 — Test de seguridad

Escribe el test que verifica que un usuario autenticado **no** puede ver los pedidos de otro.

??? success "Solución"

    ```java
    @Test
    @WithMockUser(username = "luis", roles = "USER")
    void noPuedeVerPedidosDeOtroUsuario() throws Exception {
        mvc.perform(get("/api/v1/socios/ana/pedidos"))
           .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "luis", roles = "USER")
    void siPuedeVerLosPropios() throws Exception {
        when(servicio.pedidosDe("luis")).thenReturn(List.of());
        mvc.perform(get("/api/v1/socios/luis/pedidos")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ana", roles = "ADMIN")
    void elAdminPuedeVerLosDeCualquiera() throws Exception {
        when(servicio.pedidosDe("luis")).thenReturn(List.of());
        mvc.perform(get("/api/v1/socios/luis/pedidos")).andExpect(status().isOk());
    }
    ```

    Hacen falta los tres: comprobar solo que se deniega dejaría pasar una regla que deniega siempre, incluidos los casos legítimos.


### Ejercicio 3 — Encuentra los errores

```java
@WebMvcTest(ProductoControlador.class)
class T {
    @Autowired MockMvc mvc;
    @Autowired ProductoServicio servicio;

    @Test @WithMockUser(roles = "ROLE_ADMIN")
    void t() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isOk());
    }
}
```

??? success "Solución"

    (1) `@Autowired ProductoServicio` → en un slice ese bean no existe: debe ser `@MockitoBean`. (2) roles = "ROLE_ADMIN" produce `ROLE_ROLE_ADMIN` → 403. Debe ser roles = "ADMIN". (3) Un DELETE correcto devuelve `204`, no 200. (4) Falta `@Import(SecurityConfig.class)`, así que la seguridad real ni se aplica. (5) Nombres T y `t()` sin ningún significado.


### Ejercicio 4 — MockMvc o TestRestTemplate

¿Cuándo merece la pena arrancar un servidor real?

??? success "Solución"

    `MockMvc` para casi todo: es mucho más rápido porque no abre puertos ni sockets, y llega hasta el `DispatcherServlet`, que es donde vive tu código. `TestRestTemplate` (o `RestClient`) cuando necesitas HTTP real: comprobar la serialización completa a través de la red, el comportamiento de un cliente externo, filtros de servlet a bajo nivel, compresión, o probar contra la aplicación empaquetada. Se usan pocos, para los flujos críticos.


### Ejercicio 5 — Diseña la batería completa

Enumera los tests mínimos de `POST /api/v1/prestamos` (crear préstamo, autenticado, descuenta disponibilidad).

??? success "Solución"

    Unitarios del servicio: (1) préstamo válido → se crea con fecha límite correcta y el libro pasa a no disponible; (2) libro inexistente → 404; (3) libro ya prestado → 409; (4) socio con multa → 409; (5) socio en el máximo de préstamos → 409. `@WebMvcTest`: (6) 201 + Location; (7) 400 con `libroId` nulo, sin llegar al servicio; (8) 401 sin token; (9) 403 si intenta crear un préstamo a nombre de otro socio; (10) cada excepción de negocio produce su ProblemDetail con el código correcto. E2E: (11) login → crear préstamo → el libro aparece como no disponible en `GET /libros/42` → devolución → vuelve a estar disponible. El (11) es el que de verdad demuestra que el sistema funciona: comprueba el efecto lateral a través de la API, no del código interno.

