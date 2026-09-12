# Fragmentos y plantillas base

Tienes doce páginas. Todas llevan la misma cabecera, el mismo menú, el mismo pie y los mismos enlaces a CSS. El día que cambies el logo, ¿tocas doce ficheros?

Este tema es el que convierte un montón de HTML repetido en algo mantenible. Es también el criterio de examen que más separa un trabajo apañado de uno profesional.

## 1. Definir un fragmento

Un fragmento es un trozo de plantilla con nombre.

```html title="templates/fragmentos/comunes.html"
<footer th:fragment="pie" class="pie">
    <p>IES Ejemplo · DWES <span th:text="${anio}">2026</span></p>
</footer>

<nav th:fragment="menu(activo)">
    <a th:href="@{/}"          th:classappend="${activo == 'inicio'}    ? 'sel'">Inicio</a>
    <a th:href="@{/productos}" th:classappend="${activo == 'productos'} ? 'sel'">Catálogo</a>
    <a th:href="@{/pedidos}"   th:classappend="${activo == 'pedidos'}   ? 'sel'">Pedidos</a>
</nav>
```

El menú **recibe un parámetro**, así cada página marca su propia pestaña sin duplicar el menú.

## 2. Insertarlo: `th:insert`, `th:replace` y `th:include`

```html
<div th:insert="~{fragmentos/comunes :: pie}"></div>
<div th:replace="~{fragmentos/comunes :: pie}"></div>
```

La diferencia se ve en el HTML generado:

```html
<!-- th:insert → conserva el div de fuera -->
<div><footer class="pie">…</footer></div>

<!-- th:replace → el div desaparece -->
<footer class="pie">…</footer>
```

| Etiqueta | Qué hace | Cuándo |
|---|---|---|
| `th:replace` | Sustituye la etiqueta anfitriona | **Casi siempre.** Deja el HTML limpio |
| `th:insert` | Mete el fragmento dentro | Cuando el contenedor aporta algo (una clase, una rejilla) |
| `th:include` | Mete solo el *contenido* del fragmento | Obsoleto. No lo uses |

Con parámetros:

```html
<div th:replace="~{fragmentos/comunes :: menu('productos')}"></div>
```

!!! tip "La sintaxis `~{...}`"
    Es la *expresión de fragmento*. Se puede escribir `th:replace="fragmentos/comunes :: pie"` sin ella y funciona, pero la forma con `~{}` es la recomendada desde Thymeleaf 3.1 y la que verás en cualquier proyecto actual.

## 3. La plantilla base con `th:layout` no existe (y no hace falta)

Thymeleaf **no trae herencia de plantillas** al estilo Django o Jinja. Se resuelve de dos maneras, y conviene conocer las dos porque las verás en proyectos distintos.

### Opción A — Fragmento con contenido inyectado (sin dependencias)

```html title="templates/base.html"
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:fragment="pagina(titulo, contenido)">
<head>
    <meta charset="UTF-8">
    <title th:text="${titulo} + ' · Tienda'">Tienda</title>
    <link rel="stylesheet" th:href="@{/css/estilo.css}">
</head>
<body>
    <header><div th:replace="~{fragmentos/comunes :: menu(${seccion})}"></div></header>
    <main>
        <h1 th:text="${titulo}">Título</h1>
        <div th:replace="${contenido}"></div>
    </main>
    <div th:replace="~{fragmentos/comunes :: pie}"></div>
</body>
</html>
```

```html title="templates/productos/lista.html"
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{base :: pagina('Catálogo', ~{::contenido})}">
<body>
    <contenido>
        <table>
            <tr th:each="p : ${productos}">
                <td th:text="${p.nombre}">Teclado</td>
            </tr>
        </table>
    </contenido>
</body>
</html>
```

`~{::contenido}` significa *«el fragmento llamado `contenido` de esta misma plantilla»*. La página hija solo escribe lo suyo.

### Opción B — Thymeleaf Layout Dialect (una dependencia, más cómodo)

```xml title="pom.xml"
<dependency>
    <groupId>nz.net.ultraq.thymeleaf</groupId>
    <artifactId>thymeleaf-layout-dialect</artifactId>
</dependency>
```

```html title="templates/layout.html"
<html xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
<head>
    <title layout:title-pattern="$CONTENT_TITLE · $LAYOUT_TITLE">Tienda</title>
</head>
<body>
    <header>…</header>
    <main layout:fragment="contenido">se sustituye</main>
    <footer>…</footer>
</body>
</html>
```

```html title="templates/productos/lista.html"
<html layout:decorate="~{layout}">
<body>
    <main layout:fragment="contenido">
        <table>…</table>
    </main>
</body>
</html>
```

Más legible, y la hija sigue siendo HTML abrible con doble clic.

!!! note "Cuál usamos en clase"
    **La opción B.** Es la que se encuentra en la mayoría de proyectos Spring con Thymeleaf y la que menos ruido mete en las plantillas hijas. La opción A entra en el examen como pregunta de comprensión, no de escritura.

## 4. Fragmentos con lógica de presentación reutilizable

Aquí es donde los fragmentos dejan de ser «el menú y el pie» y empiezan a valer de verdad.

```html title="templates/fragmentos/ui.html"

<span th:fragment="estado(valor)" th:switch="${valor}" class="chip">
    <span th:case="'PENDIENTE'" class="chip-amarillo">Pendiente</span>
    <span th:case="'ENVIADO'"   class="chip-azul">Enviado</span>
    <span th:case="'ENTREGADO'" class="chip-verde">Entregado</span>
    <span th:case="*"           class="chip-gris">—</span>
</span>

<nav th:fragment="paginacion(pagina, url)" th:if="${pagina.totalPages > 1}">
    <a th:if="${!pagina.first}"
       th:href="@{${url}(page=${pagina.number - 1})}">‹ Anterior</a>
    <span th:text="|Página ${pagina.number + 1} de ${pagina.totalPages}|">1 de 1</span>
    <a th:if="${!pagina.last}"
       th:href="@{${url}(page=${pagina.number + 1})}">Siguiente ›</a>
</nav>
```

Y se usan así, desde cualquier página:

```html
<td th:replace="~{fragmentos/ui :: estado(${pedido.estado})}"></td>
...
<div th:replace="~{fragmentos/ui :: paginacion(${pagina}, '/productos')}"></div>
```

Ese fragmento de paginación es el mismo `Page<T>` que devolvía tu API en la UT6. **La paginación del servidor sirve igual para JSON que para HTML**: cambia quién la pinta, no quién la calcula.

## 5. Organización de carpetas que aguanta

``` { .text .sinajuste }
templates/
├── layout.html                 la plantilla base
├── fragmentos/
│   ├── comunes.html            menú, pie, cabecera
│   ├── ui.html                 chips, paginación, alertas, tabla vacía
│   └── formularios.html        campo con etiqueta y error
├── productos/
│   ├── lista.html
│   ├── detalle.html
│   └── formulario.html
├── pedidos/
│   └── …
└── error/
    ├── 404.html
    └── 500.html
```

Un criterio simple para decidir si algo merece ser fragmento: **si aparece en tres sitios, o si aparece en dos y es probable que cambie, es un fragmento.**

!!! tip "La carpeta `error/`"
    Spring Boot busca automáticamente `templates/error/404.html` y `templates/error/500.html`. No hay que configurar nada.

    Es un detalle pequeño con mucho efecto: la página blanca de error por defecto de Spring («Whitelabel Error Page») en una entrega da muy mala impresión, y evitarla cuesta dos ficheros.

## 6. Lo que no debe estar en un fragmento

- **Consultas.** Un fragmento no llama al servicio. Recibe lo que necesita como parámetro.
- **Decisiones de negocio.** «Si el pedido lleva más de 30 días y el cliente es premium…» se calcula antes y llega resuelto.
- **Estado.** Un fragmento no recuerda nada entre invocaciones.

Si un fragmento necesita datos que la página no tiene, hay dos salidas correctas: pasárselos como parámetro, o —si de verdad son globales— publicarlos con un `@ModelAttribute` en un `@ControllerAdvice`, como viste en el tema anterior.

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Monta la plantilla base y dos páginas que la usen. Son cuatro ficheros:

``` { .html .numerado title="templates/base.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:fragment="pagina(titulo, contenido)">
<head>
    <meta charset="UTF-8">
    <title th:text="|${titulo} · Tienda|">Tienda</title>
</head>
<body>
    <nav th:replace="~{fragmentos/comunes :: menu(${seccion})}"></nav>
    <main>
        <h1 th:text="${titulo}">Título</h1>
        <div th:replace="${contenido}"></div>
    </main>
    <footer th:replace="~{fragmentos/comunes :: pie}"></footer>
</body>
</html>
```

``` { .html .numerado title="templates/fragmentos/comunes.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

<nav th:fragment="menu(activa)">
    <a th:href="@{/productos}" th:classappend="${activa == 'productos'} ? 'activa'">Productos</a>
    <a th:href="@{/pedidos}"   th:classappend="${activa == 'pedidos'} ? 'activa'">Pedidos</a>
</nav>

<footer th:fragment="pie">
    <small th:text="|© ${#temporals.year(#temporals.createNow())} · Tienda|">© 2026</small>
</footer>

</body>
</html>
```

``` { .html .numerado title="templates/productos/lista.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{base :: pagina('Catálogo', ~{::contenido})}">
<body>
    <contenido>
        <p>Aquí irá la tabla de productos.</p>
    </contenido>
</body>
</html>
```

Y el controlador, que solo tiene que poner `seccion` en el modelo:

```java
@GetMapping("/productos")
public String lista(Model modelo) {
    modelo.addAttribute("seccion", "productos");
    return "productos/lista";
}
```

Ahora, tres cosas que hacer y mirar el código fuente cada vez:

1. Copia `lista.html` a `pedidos/lista.html` cambiando el título, y crea su controlador con `seccion = "pedidos"`. **La pestaña activa cambia sola.**
2. Cambia el `th:replace` del pie por `th:insert` y compara el HTML generado.
3. Borra el `<contenido>` de la página hija. Verás que `main` sale vacío pero la página **no falla**: el fragmento no encontrado se resuelve a nada.

---

## Ejercicios (con solución)

### Ejercicio 1 — `th:insert` o `th:replace`

Diferencia exacta entre los dos, con el HTML resultante de cada uno.

??? success "Solución"

    Partiendo de este fragmento:

    ```html
    <footer th:fragment="pie" class="pie">© 2026</footer>
    ```

    Y de esta llamada:

    ```html
    <div th:insert="~{fragmentos/comunes :: pie}"></div>
    <div th:replace="~{fragmentos/comunes :: pie}"></div>
    ```

    Sale esto:

    ```html
    <!-- th:insert: el div anfitrión SE QUEDA y el fragmento va dentro -->
    <div><footer class="pie">© 2026</footer></div>

    <!-- th:replace: el div anfitrión DESAPARECE -->
    <footer class="pie">© 2026</footer>
    ```

    **Usa `th:replace` casi siempre**: deja el HTML sin capas de `<div>` que no pintan nada. `th:insert` solo cuando el contenedor aporte algo, por ejemplo una celda de una rejilla CSS.

    `th:include` mete solo el *contenido* del fragmento, sin su etiqueta. Está obsoleto: no lo uses.

### Ejercicio 2 — El fragmento `alerta`

Escribe un fragmento `alerta(tipo, mensaje)` que pinte un aviso con una clase distinta según el tipo (`exito`, `error`, `aviso`), y que **no pinte nada** si el mensaje viene vacío.

??? success "Solución"

    ```html title="templates/fragmentos/ui.html"
    <div th:fragment="alerta(tipo, mensaje)"
         th:if="${!#strings.isEmpty(mensaje)}"
         th:class="|alerta alerta-${tipo}|"
         role="alert">
        <span th:text="${mensaje}">Mensaje</span>
    </div>
    ```

    Y se usa así:

    ```html
    <div th:replace="~{fragmentos/ui :: alerta('exito', ${mensajeExito})}"></div>
    <div th:replace="~{fragmentos/ui :: alerta('error', ${mensajeError})}"></div>
    ```

    Dos detalles:

    - El `th:if` va **en la misma etiqueta que el `th:fragment`**. Así, si no hay mensaje, no queda ni un `<div>` vacío en el HTML.
    - `role="alert"` es accesibilidad: hace que un lector de pantalla anuncie el aviso al aparecer. Se puntúa en la rúbrica.

### Ejercicio 3 — La pestaña activa

¿Cómo marca cada página su pestaña activa en el menú, sin duplicar el menú en cada plantilla?

??? success "Solución"

    **Pasándole al fragmento cuál es la sección activa**, como parámetro:

    ```html
    <nav th:fragment="menu(activa)">
        <a th:href="@{/productos}" th:classappend="${activa == 'productos'} ? 'activa'">Productos</a>
        <a th:href="@{/pedidos}"   th:classappend="${activa == 'pedidos'} ? 'activa'">Pedidos</a>
    </nav>
    ```

    El valor lo pone el controlador en el modelo:

    ```java
    modelo.addAttribute("seccion", "productos");
    ```

    Fíjate en que es **`th:classappend` y no `th:class`**: si usaras `th:class` machacarías las clases que el enlace ya tuviera.

    La alternativa —un `th:if` por página con un menú distinto en cada una— es exactamente lo que los fragmentos vienen a evitar.

### Ejercicio 4 — `~{::contenido}`

Explica qué hace exactamente `~{::contenido}`.

??? success "Solución"

    Es una **expresión de fragmento sin nombre de plantilla**. Los dos puntos dobles con nada delante significan «en esta misma plantilla».

    Así que `~{::contenido}` es: *«el elemento `<contenido>` de este mismo fichero»*.

    Sirve para el patrón de plantilla base: la página hija le pasa su propio cuerpo a `base.html`, que lo coloca donde toca.

    ```html
    <html th:replace="~{base :: pagina('Catálogo', ~{::contenido})}">
    <body>
        <contenido>  <!-- esto es lo que se pasa -->
            <table>…</table>
        </contenido>
    </body>
    </html>
    ```

    `<contenido>` no es una etiqueta HTML real, y da igual: solo existe para que Thymeleaf la localice. Nunca llega al navegador.

### Ejercicio 5 — Dónde va el `404.html`

¿Dónde colocas la página de error 404 para que Spring Boot la use sin configurar nada?

??? success "Solución"

    ``` { .text .sinajuste }
    src/main/resources/templates/error/404.html
    ```

    Y la de error del servidor en `templates/error/500.html`. Spring Boot las busca ahí por convenio: no hay que tocar ni una línea de configuración.

    Si solo quieres una para todo, `templates/error.html` sirve de comodín.

    No es un detalle menor: la pantalla blanca de Spring («Whitelabel Error Page») en una entrega da muy mala impresión, y evitarla cuesta dos ficheros.

### Ejercicio 6 — El fragmento que necesita saber algo

Tienes un fragmento de tarjeta de producto que necesita saber si el usuario lo tiene en favoritos. ¿Cómo se lo haces llegar?

??? success "Solución"

    **Como parámetro.** Un fragmento no consulta nada: recibe todo lo que necesita.

    ```html
    <article th:fragment="tarjeta(producto, esFavorito)">
        <h3 th:text="${producto.nombre}">Nombre</h3>
        <span th:if="${esFavorito}" title="En favoritos">★</span>
        <span th:unless="${esFavorito}">☆</span>
    </article>
    ```

    ```html
    <div th:each="p : ${productos}"
         th:replace="~{fragmentos/ui :: tarjeta(${p}, ${favoritos.contains(p.id)})}"></div>
    ```

    Y mejor todavía: que el DTO ya traiga el dato resuelto desde el servicio, `producto.favorito`, y el fragmento reciba un parámetro menos.

    **Lo que no se hace:** que el fragmento llame al servicio de favoritos. Eso mete una consulta a la base de datos dentro del renderizado, por cada tarjeta.

### Ejercicio 7 — ¿Merece ser fragmento?

Decide, con el criterio del tema, cuáles de estos merecen ser fragmento:

(a) el pie de página · (b) la tabla de productos, que solo aparece en una página · (c) el bloque «campo de formulario con su etiqueta y su mensaje de error» · (d) el título `<h1>` de cada página · (e) el chip de estado de un pedido, que sale en la lista y en el detalle

??? success "Solución"

    El criterio: **si aparece en tres sitios, o si aparece en dos y es probable que cambie, es un fragmento.**

    | | ¿Fragmento? | Por qué |
    |---|:-:|---|
    | (a) el pie | **Sí** | Está en todas las páginas |
    | (b) la tabla de productos | **No** | Aparece una vez. Un fragmento aquí solo añade un salto que seguir al leer |
    | (c) el campo de formulario | **Sí** | Es el caso de libro: se repite en cada campo de cada formulario y su marcado es fácil de equivocar |
    | (d) el `<h1>` | **No** como fragmento; sí como **parámetro de la plantilla base** | Cambia en cada página; lo que se comparte es el sitio donde va |
    | (e) el chip de estado | **Sí** | Dos sitios y con lógica de presentación (`th:switch`) que no conviene duplicar |
