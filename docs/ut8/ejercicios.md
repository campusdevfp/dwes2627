# Batería de ejercicios — UT8

**Dominio: una escuela de idiomas.** Distinto del de clase (la tienda) a propósito: si puedes hacerlo aquí, es que lo has entendido y no memorizado.

El modelo de partida ya está hecho: `Curso` (nombre, idioma, nivel, precio, plazas), `Matricula` (alumno, curso, fecha, estado) y sus servicios.

---

### E1 ● — La primera plantilla

Lista los cursos en una tabla con nombre, idioma, nivel y precio formateado en euros.

??? success "Solución"

    ```java
    @Controller
    @RequestMapping("/cursos")
    public class CursoVistaControlador {
        private final CursoServicio servicio;
        public CursoVistaControlador(CursoServicio servicio) { this.servicio = servicio; }

        @GetMapping
        public String listar(Model modelo) {
            modelo.addAttribute("cursos", servicio.listar());
            return "cursos/lista";
        }
    }
    ```
    ```html
    <td th:text="${c.nombre}">Inglés B2</td>
    <td th:text="${#numbers.formatDecimal(c.precio, 1, 'POINT', 2, 'COMMA')} + ' €'">180,00 €</td>
    ```
    El `formatDecimal` con `POINT`/`COMMA` es lo que da formato español. Sin él sale `180.0`, que en una entrega canta mucho.


### E2 ● — Lista vacía y estados

Añade el mensaje de «no hay cursos» y una etiqueta que ponga *Completo* cuando no queden plazas.

??? success "Solución"

    ```html
    <tr th:if="${#lists.isEmpty(cursos)}"><td colspan="4">No hay cursos disponibles.</td></tr>

    <span th:if="${c.plazas > 0}"  class="chip-verde" th:text="|${c.plazas} plazas|">3 plazas</span>
    <span th:unless="${c.plazas > 0}" class="chip-rojo">Completo</span>
    ```


### E3 ●● — Enlaces que no se rompen

Enlaza cada curso con su ficha `/cursos/{id}` y añade un botón de volver. Todo con `@{...}`.

??? success "Solución"

    ```html
    <a th:href="@{/cursos/{id}(id=${c.id})}" th:text="${c.nombre}">Inglés B2</a>
    <a th:href="@{/cursos}">‹ Volver al listado</a>
    ```
    Comprobación: arranca con `--server.servlet.context-path=/escuela`. Con `@{...}` todo sigue funcionando; con `href="/cursos"` se rompen todos los enlaces a la vez.


### E4 ●● — El *layout* y el menú activo

Saca la estructura común a `layout.html` y crea un menú que marque la sección actual.

??? success "Solución"

    ```html
    <nav th:fragment="menu(activo)">
        <a th:href="@{/cursos}"     th:classappend="${activo == 'cursos'} ? 'sel'">Cursos</a>
        <a th:href="@{/matriculas}" th:classappend="${activo == 'matriculas'} ? 'sel'">Matrículas</a>
    </nav>
    ```
    ```html
    <html layout:decorate="~{layout}">
      <main layout:fragment="contenido">…</main>
    </html>
    ```
    Con `modelo.addAttribute("seccion", "cursos")` desde el controlador.


### E5 ●● — Fragmento de paginación reutilizable

Escribe `paginacion(pagina, url)` y úsalo en cursos y en matrículas.

??? success "Solución"

    ```html
    <nav th:fragment="paginacion(pagina, url)" th:if="${pagina.totalPages > 1}">
        <a th:if="${!pagina.first}" th:href="@{${url}(page=${pagina.number - 1})}">‹</a>
        <span th:text="|${pagina.number + 1} / ${pagina.totalPages}|">1 / 1</span>
        <a th:if="${!pagina.last}"  th:href="@{${url}(page=${pagina.number + 1})}">›</a>
    </nav>
    ```
    Es el mismo `Page<T>` que devolvía tu API en la UT6: la paginación se calcula una vez y sirve para JSON y para HTML.


### E6 ●●● — Filtro y buscador que conviven con la paginación

Filtro por idioma y buscador por nombre. Al cambiar de página los filtros deben mantenerse.

??? success "Solución"

    ```java
    @GetMapping
    public String listar(@RequestParam(required = false) String idioma,
                         @RequestParam(required = false) String q,
                         @PageableDefault(size = 10) Pageable pageable, Model modelo) {
        modelo.addAttribute("pagina", servicio.buscar(idioma, q, pageable));
        modelo.addAttribute("idioma", idioma);
        modelo.addAttribute("q", q);
        return "cursos/lista";
    }
    ```
    ```html
    <a th:href="@{/cursos(page=${pagina.number + 1}, idioma=${idioma}, q=${q})}">Siguiente</a>
    ```
    El fallo clásico es olvidar arrastrar `idioma` y `q`: la página 2 sale sin filtrar y parece un error de la consulta.


### E7 ●●● — Formulario de alta con validación

`CursoForm` validado, con errores por campo y desplegable de idiomas.

??? success "Solución"

    ```java
    public class CursoForm {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 60) private String nombre;
        @NotNull private Idioma idioma;
        @NotNull @DecimalMin("0.01") private BigDecimal precio;
        @Min(1) @Max(30) private int plazas;
        // getters y setters
    }
    ```
    ```java
    @PostMapping
    public String crear(@Valid @ModelAttribute("curso") CursoForm form,
                        BindingResult errores, Model modelo, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            modelo.addAttribute("idiomas", Idioma.values());
            return "cursos/formulario";
        }
        servicio.crear(form);
        flash.addFlashAttribute("exito", "Curso creado");
        return "redirect:/cursos";
    }
    ```
    Las dos líneas que se olvidan: repoblar `idiomas` y devolver la vista **sin** redirigir cuando hay errores.


### E8 ●●● — El error que no es de un campo

No puede haber dos cursos con el mismo nombre y nivel. Añádelo como error global.

??? success "Solución"

    ```java
    if (servicio.existe(form.getNombre(), form.getNivel())) {
        errores.reject("duplicado", "Ya existe un curso de " + form.getNivel() + " con ese nombre");
    }
    ```
    ```html
    <div class="alerta" th:if="${#fields.hasGlobalErrors()}">
        <p th:each="e : ${#fields.globalErrors()}" th:text="${e}"></p>
    </div>
    ```
    Ojo: `reject` es global, `rejectValue` es de campo. Confundirlos hace que el mensaje no se pinte en ningún sitio.


### E9 ●● — Editar reutilizando el formulario

Un solo `formulario.html` para crear y editar.

??? success "Solución"

    ```html
    <form th:object="${curso}" method="post"
          th:action="${curso.id} != null ? @{/cursos/{id}(id=${curso.id})} : @{/cursos}">
    ```
    El controlador de edición carga un `CursoForm` desde el servicio, no la entidad. Si pasas la entidad, cualquiera puede añadir un campo oculto y escribir en propiedades que no pusiste.


### E10 ●●● — Buscador en vivo con htmx

Que la tabla se filtre al teclear, sin recargar y sin duplicar la plantilla de las filas.

??? success "Solución"

    ```java
    @GetMapping("/filas")
    public String filas(@RequestParam(required = false) String q, Model modelo) {
        modelo.addAttribute("cursos", servicio.buscar(q));
        return "cursos/lista :: filas";
    }
    ```
    ```html
    <input type="search" name="q" hx-get="/cursos/filas"
           hx-trigger="keyup changed delay:300ms" hx-target="#cuerpo" hx-swap="outerHTML">
    <tbody id="cuerpo" th:fragment="filas">…</tbody>
    ```
    Sin `delay:300ms` lanzas una petición por tecla. Se ve en la pestaña Red del navegador y es el fallo que más se repite.


### E11 ●● — Matricular sin recargar

Botón «Matricularme» que descuenta una plaza y actualiza el contador de la fila.

??? success "Solución"

    ```html
    <button hx-post="/matriculas" th:hx-vals="|{&quot;cursoId&quot;: ${c.id}}|"
            hx-target="closest tr" hx-swap="outerHTML">Matricularme</button>
    ```
    ```java
    @PostMapping("/matriculas")
    public String matricular(@RequestParam Long cursoId, Model modelo) {
        servicio.matricular(cursoId);
        modelo.addAttribute("c", servicio.porId(cursoId));
        return "cursos/lista :: fila";
    }
    ```
    La transacción y el control de plazas viven en el servicio, de la UT5. Aquí solo se devuelve el trozo de HTML actualizado.


### E12 ●●● — Vistas con roles y test que lo demuestre

`/admin/cursos` solo para administradores, enlace oculto para el resto, y un test que pruebe las tres situaciones.

??? success "Solución"

    ```java
    .requestMatchers("/admin/**").hasRole("ADMIN")
    ```
    ```html
    <a sec:authorize="hasRole('ADMIN')" th:href="@{/admin/cursos}">Gestión</a>
    ```
    ```java
    @Test void sinSesionRedirigeAlLogin() throws Exception {
        mvc.perform(get("/admin/cursos")).andExpect(redirectedUrlPattern("**/login")); }

    @Test @WithMockUser(roles = "USER") void usuarioNormalRecibe403() throws Exception {
        mvc.perform(get("/admin/cursos")).andExpect(status().isForbidden()); }

    @Test @WithMockUser(roles = "ADMIN") void adminEntra() throws Exception {
        mvc.perform(get("/admin/cursos")).andExpect(status().isOk()); }
    ```
    Los tres tests juntos son la única prueba de que ocultaste el enlace **y** cerraste la ruta. Con uno solo no se demuestra nada.


---

## Reparto sugerido

| Ejercicio | Nivel | Sesión |
|---|:-:|:-:|
| E1 · Primera plantilla | ● | S6 |
| E2 · Lista vacía y estados | ● | S8 |
| E3 · Enlaces con `@{...}` | ●● | S9 |
| E4 · Layout y menú activo | ●● | S11 |
| E5 · Fragmento de paginación | ●● | S12 |
| E6 · Filtro con paginación | ●●● | S13 |
| E7 · Formulario con validación | ●●● | S16 |
| E8 · Error global | ●●● | S17 |
| E9 · Editar reutilizando | ●● | S19 |
| E10 · Buscador htmx | ●●● | S23 |
| E11 · Matricular sin recargar | ●● | S24 |
| E12 · Roles y tests | ●●● | S27 |
