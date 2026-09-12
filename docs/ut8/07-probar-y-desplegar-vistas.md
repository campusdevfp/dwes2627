# Probar y desplegar las vistas

Una plantilla rota no la detecta el compilador: Thymeleaf falla **en tiempo de ejecución**, cuando ya está el usuario delante. Este tema va de adelantarse a eso.

## 1. Probar un controlador de vistas

Mismo `MockMvc` de la UT6, con comprobaciones distintas: en vez de mirar el JSON, se mira **qué plantilla se ha elegido y qué hay en el modelo**.

```java
@WebMvcTest(ProductoVistaControlador.class)
@Import(SecurityConfig.class)
class ProductoVistaControladorTest {

    @Autowired MockMvc mvc;
    @MockitoBean ProductoServicio servicio;

    @Test
    void elListadoUsaLaPlantillaCorrectaYLlevaLosDatos() throws Exception {
        when(servicio.listar()).thenReturn(List.of(
                new ProductoDto(1L, "Teclado", new BigDecimal("29.90"))));

        mvc.perform(get("/productos"))
           .andExpect(status().isOk())
           .andExpect(view().name("productos/lista"))
           .andExpect(model().attributeExists("productos"))
           .andExpect(content().string(containsString("Teclado")));
    }
}
```

`content().string(containsString(...))` es la comprobación que de verdad demuestra que **la plantilla se ha renderizado**: si tuviera un error de sintaxis, el test fallaría aquí y no en producción.

## 2. Probar el formulario, que es donde falla todo

Los tres casos que hay que cubrir siempre:

``` { .java .numerado }
@Test
void datosValidosGuardanYRedirigen() throws Exception {
    mvc.perform(post("/productos").with(csrf())
            .param("nombre", "Teclado")
            .param("precio", "29.90")
            .param("categoria", "PERIFERICO"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrl("/productos"))
       .andExpect(flash().attributeExists("exito"));
    verify(servicio).crear(any());
}

@Test
void datosInvalidosVuelvenAlFormularioConErrores() throws Exception {
    mvc.perform(post("/productos").with(csrf())
            .param("nombre", "")
            .param("precio", "-5"))
       .andExpect(status().isOk())                       // ← 200, NO redirección
       .andExpect(view().name("productos/formulario"))
       .andExpect(model().attributeHasFieldErrors("producto", "nombre", "precio"))
       .andExpect(model().attributeExists("categorias")); // ← el desplegable repoblado
    verifyNoInteractions(servicio);                       // ← y no se ha guardado nada
}

@Test
void sinTokenCsrfSeRechaza() throws Exception {
    mvc.perform(post("/productos").param("nombre", "X"))
       .andExpect(status().isForbidden());
}
```

Los dos `verify` finales son los que separan un test decorativo de uno útil: comprueban no solo lo que se devuelve, sino **que no se ha tocado la base de datos** cuando los datos eran inválidos.

Y el `.with(csrf())` es obligatorio en cualquier POST si tienes Spring Security. Sin él todos tus tests dan 403 y parece que el controlador está roto.

## 3. Probar la seguridad de las vistas

```java
@Test
void elPanelDeAdminRedirigeAlLoginSiNoHaySesion() throws Exception {
    mvc.perform(get("/admin"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrlPattern("**/login"));
}

@Test
@WithMockUser(roles = "USER")
void unUsuarioNormalNoEntraEnAdmin() throws Exception {
    mvc.perform(get("/admin")).andExpect(status().isForbidden());
}

@Test
@WithMockUser(roles = "ADMIN")
void unAdminSiEntra() throws Exception {
    mvc.perform(get("/admin")).andExpect(status().isOk());
}
```

Fíjate en la diferencia con la API: aquí, sin sesión, **no hay un 401, hay una redirección al formulario de login**. Es el comportamiento normal de una aplicación web y hay que probarlo tal cual.

## 4. Un test que caza plantillas rotas antes del examen

Muy barato y muy rentable: recorrer las páginas principales y comprobar que ninguna revienta.

```java
@SpringBootTest
@AutoConfigureMockMvc
class TodasLasPaginasRenderizanTest {

    @Autowired MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/productos", "/productos/nuevo", "/login"})
    @WithMockUser(roles = "ADMIN")
    void ningunaPaginaFalla(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk());
    }
}
```

Un `th:text` mal escrito, un fragmento con la ruta equivocada o una variable que no está en el modelo hacen fallar este test en un segundo. Sin él, te enteras el día de la entrega.

## 5. Los errores de Thymeleaf y qué significan

| Mensaje | Causa habitual |
|---|---|
| `Error resolving template "productos/lista"` | El fichero no está en `templates/`, o le falta `.html`, o hay una errata |
| `Exception evaluating SpringEL expression: "p.nombre"` | La propiedad no existe, o falta el *getter*, o el objeto es `null` |
| `Neither BindingResult nor plain target object available` | `th:object` apunta a algo que no está en el modelo. Casi siempre, volviste al formulario sin volver a añadirlo |
| `Fragment could not be resolved` | Nombre o ruta del fragmento mal escritos |
| `LazyInitializationException` a media página | Metiste una entidad con relaciones perezosas en el modelo. Manda un DTO |

!!! tip "Leer la traza de Thymeleaf"
    La excepción trae **el nombre de la plantilla y el número de línea**. Está enterrado entre veinte líneas de `at org.thymeleaf...`, pero está. Búscalo antes de empezar a tocar cosas al azar.

## 6. Recursos estáticos y caché

En producción, el CSS y el JavaScript deben cachearse en el navegador… hasta que cambian. Spring Boot lo resuelve con huellas en el nombre:

```yaml title="src/main/resources/application.yml"
spring:
  web.resources.chain:
    strategy.content:
      enabled: true
      paths: /**
```

Con eso, `th:href="@{/css/estilo.css}"` genera `/css/estilo-9a3f2b.css`. Si el fichero cambia, cambia el nombre, y el navegador se lo descarga otra vez. Si no cambia, lo sirve de su caché.

Es la solución al clásico *«he arreglado el CSS pero al profesor le sigue saliendo mal, dile que borre la caché»*.

## 7. Antes de entregar: la lista

- [ ] `spring.thymeleaf.cache: true` en producción (déjalo a `false` solo en el perfil `dev`).
- [ ] Ninguna `System.out.println` ni traza de depuración en las plantillas.
- [ ] Páginas `error/404.html`, `error/500.html` y la de acceso denegado.
- [ ] Todos los `href` y `src` con `@{...}`.
- [ ] Todos los formularios con `th:action` (o el CSRF no se inyecta).
- [ ] Ningún `th:utext` sobre datos que vengan del usuario.
- [ ] Los textos, en `messages.properties`.
- [ ] `mvn test` en verde, incluidos los tests de formulario y de seguridad.
- [ ] La aplicación arranca con `java -jar` y no solo desde el IDE.

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Los tres tests que hay que saber escribir de memoria, sobre el formulario de contacto del tema 4.

``` { .java .numerado title="ContactoControllerTest.java" }
@WebMvcTest(ContactoController.class)
class ContactoControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("GET /contacto pinta el formulario vacío")
    void muestraElFormulario() throws Exception {
        mvc.perform(get("/contacto"))
           .andExpect(status().isOk())
           .andExpect(view().name("contacto"))
           .andExpect(model().attributeExists("contacto", "asuntos"))
           .andExpect(content().string(containsString("<form")));
    }

    @Test
    @DisplayName("POST con datos malos vuelve al formulario con errores")
    void datosInvalidos() throws Exception {
        mvc.perform(post("/contacto")
                   .with(csrf())
                   .param("nombre", "")
                   .param("correo", "esto-no-es-un-correo"))
           .andExpect(status().isOk())                                  // 200, no redirige
           .andExpect(view().name("contacto"))                          // vuelve a la vista
           .andExpect(model().attributeHasFieldErrors("contacto",       // con errores
                                                      "nombre", "correo"))
           .andExpect(model().attributeExists("asuntos"));              // y repoblado
    }

    @Test
    @DisplayName("POST con datos buenos redirige")
    void datosValidos() throws Exception {
        mvc.perform(post("/contacto")
                   .with(csrf())
                   .param("nombre", "Ana")
                   .param("correo", "ana@iesx.es")
                   .param("asunto", "Consulta"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/contacto"))
           .andExpect(flash().attributeExists("exito"));
    }
}
```

Y el que caza plantillas rotas, que es el que más disgustos evita:

``` { .java .numerado title="PlantillasTest.java" }
@SpringBootTest
@AutoConfigureMockMvc
class PlantillasTest {

    @Autowired MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/contacto", "/productos", "/buscador"})
    @DisplayName("Todas las páginas se renderizan sin reventar")
    void seRenderizan(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk());
    }
}
```

Ahora **rómpelo a propósito**, que es de donde se aprende:

1. Cambia `${asuntos}` por `${asunttos}` en la plantilla y ejecuta. Verás el error de Thymeleaf del apartado 5.
2. Quita el `.with(csrf())` del segundo test. Pasa a 403.
3. Quita el `modelo.addAttribute("asuntos", ...)` del bloque de errores del controlador. **Solo falla el segundo test**, en la última línea. Ese test existe justamente para eso.

---

## Ejercicios (con solución)

### Ejercicio 1 — Las tres comprobaciones

¿Qué comprueban `view().name(...)` y `model().attributeExists(...)`, y por qué añadir además `content().string(containsString(...))`?

??? success "Solución"

    - **`view().name("contacto")`** comprueba que el controlador ha devuelto el nombre de plantilla correcto. Nada más: no llega a mirar si esa plantilla existe.
    - **`model().attributeExists("contacto", "asuntos")`** comprueba que el modelo lleva lo que la plantilla va a necesitar.

    Con esos dos, un controlador correcto que apunta a una plantilla **con una errata dentro** pasa los tests igualmente. El fallo aparecería en clase, delante de todos.

    **`content().string(containsString("<form"))`** obliga a que la plantilla se renderice de verdad. Si tiene una expresión mal escrita, Thymeleaf lanza la excepción durante el renderizado y el test se pone rojo.

    Los tres juntos cubren tres cosas distintas: **la decisión** del controlador, **los datos** que entrega y **que la plantilla funciona**.

### Ejercicio 2 — Probar el envío inválido

¿Qué tres cosas hay que verificar al probar un envío con datos inválidos?

??? success "Solución"

    1. **Que devuelve 200 y NO redirige.** Una redirección se llevaría por delante los errores y lo escrito. `status().isOk()` y `view().name("...")`.
    2. **Que los errores están en el campo que toca.** `model().attributeHasFieldErrors("contacto", "nombre", "correo")`. No basta con que haya errores: tienen que estar en el campo correcto, o el usuario ve el mensaje bajo el campo equivocado.
    3. **Que el modelo se ha repoblado.** `model().attributeExists("asuntos")`. Es el fallo del desplegable vacío del tema 4, y sin este test no lo caza nadie.

    Una cuarta que suma: que **no se ha guardado nada**. Con un doble de Mockito, `verify(servicio, never()).crear(any())`.

### Ejercicio 3 — `csrf()` y `@Import`

¿Por qué hacen falta `.with(csrf())` y `@Import(SecurityConfig.class)`?

??? success "Solución"

    **`.with(csrf())`** porque Spring Security exige el testigo en todo POST. En el navegador lo pone `th:action` solo, pero `MockMvc` construye la petición a mano y no lo lleva. Sin él, el test recibe un 403 y parece que el controlador está roto.

    **`@Import(SecurityConfig.class)`** porque `@WebMvcTest` **no carga tu configuración de seguridad**: solo levanta la capa web y las clases que le digas. Sin importarla, el test corre con la seguridad por defecto de Spring Boot, que no se parece a la tuya. Consecuencia: pruebas que las rutas de administrador están protegidas y el test pasa por un motivo que no es el tuyo.

    Con `@SpringBootTest` no hace falta importarla, porque se levanta el contexto entero. A cambio, el test tarda mucho más.

### Ejercicio 4 — ¿401 o redirección?

Sin sesión, una ruta protegida de la web ¿devuelve 401 o redirige? ¿Y la de la API?

??? success "Solución"

    **La web redirige** al formulario de login, con un **302**. Es lo que espera una persona con un navegador delante: la mandas a la pantalla de entrar.

    **La API devuelve 401**, sin cuerpo HTML. Es lo que espera un programa: un código que pueda interpretar. Redirigir a `/login` a un cliente que espera JSON solo consigue que se le atragante una página de HTML.

    En el test se distingue así:

    ```java
    mvc.perform(get("/admin/productos"))
       .andExpect(status().is3xxRedirection())
       .andExpect(redirectedUrlPattern("**/login"));

    mvc.perform(get("/api/productos"))
       .andExpect(status().isUnauthorized());
    ```

    Cuando la aplicación tiene las dos cosas —que es el caso de la UT9— se configuran **dos cadenas de filtros** con `@Order`: una para `/api/**` que responde 401, y otra para el resto con `formLogin`.

### Ejercicio 5 — `Neither BindingResult nor plain target object available`

Te sale ese error. ¿Qué miras primero?

??? success "Solución"

    Que **el nombre del `th:object` coincida con el del atributo del modelo**. Es la causa en nueve de cada diez casos.

    ```java
    modelo.addAttribute("contacto", new ContactoForm());   // aquí «contacto»
    ```
    ```html
    <form th:object="${contacto}">                          <!-- aquí también -->
    ```

    Si en el controlador pones `"contactoForm"` y en la plantilla `${contacto}`, Thymeleaf no encuentra el objeto y lanza justo ese mensaje.

    Las otras dos causas, por orden:

    2. **El POST devuelve la vista sin que el objeto esté en el modelo.** Con `@ModelAttribute("contacto")` en la firma, Spring lo repone solo; sin el nombre explícito, lo llama `contactoForm` y la plantilla vuelve a no encontrarlo.
    3. **Un `th:field` fuera del `th:object`**, en otra etiqueta o en un fragmento que se incluye desde fuera del formulario.

### Ejercicio 6 — La huella en los recursos estáticos

Explica para qué sirve.

??? success "Solución"

    Para poder decirle al navegador *«guárdate este fichero un año»* sin quedarte atrapado con la versión vieja.

    El problema: si `estilo.css` se cachea un año, el día que cambies el diseño los usuarios seguirán viendo el antiguo hasta que fuercen la recarga. Y si no lo cacheas, se descarga en cada visita.

    La huella lo resuelve metiendo un resumen del contenido **en el nombre del fichero**:

    ```yaml
    spring:
      web:
        resources:
          chain:
            strategy:
              content:
                enabled: true
                paths: /**
    ```

    Ahora `th:href="@{/css/estilo.css}"` genera `/css/estilo-9f8a2c.css`. Cuando el CSS cambia, cambia el resumen, cambia el nombre, y el navegador se lo baja **porque para él es otro fichero**.

    Lo importante para el examen: esto **solo funciona si los recursos se enlazan con `@{...}`**. Un `href="/css/estilo.css"` escrito a mano no se reescribe, y se queda con el fichero cacheado para siempre.
