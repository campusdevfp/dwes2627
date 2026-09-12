# Chuleta — Páginas dinámicas con Thymeleaf

## Los cuatro símbolos

| | Para qué | Ejemplo |
|:-:|---|---|
| `${...}` | variable del modelo | `${producto.nombre}` |
| `*{...}` | campo del `th:object` | `*{nombre}` |
| `@{...}` | **URL** (siempre) | `@{/productos/{id}(id=${p.id})}` |
| `#{...}` | texto traducido | `#{producto.titulo}` |

## Pintar

```html
<h1 th:text="${p.nombre}">ejemplo</h1>
<p th:text="|Quedan ${p.stock} uds|">…</p>
<span>[[${p.precio}]] €</span>
<td th:text="${p.notas} ?: 'Sin notas'">—</td>
```
:material-alert: `th:utext` NO escapa → XSS si viene del usuario.

## Repetir y decidir

```html
<tr th:each="p, i : ${productos}" th:class="${i.odd} ? 'gris'">
    <td th:text="${i.count}">1</td>
</tr>
<tr th:if="${#lists.isEmpty(productos)}"><td>Sin resultados</td></tr>

<span th:if="${p.stock > 0}">Disponible</span>
<span th:unless="${p.stock > 0}">Agotado</span>

<div th:switch="${estado}">
    <span th:case="'ENVIADO'">Enviado</span>
    <span th:case="*">—</span>
</div>
```

## Atributos

```html
<a  th:href="@{/productos/{id}(id=${p.id})}">Ver</a>
<img th:src="@{/img/logo.svg}" th:alt="${p.nombre}">
<button th:disabled="${p.stock == 0}">Comprar</button>
<tr class="fila" th:classappend="${p.stock == 0} ? 'agotado'">
```

## Utilidades

```html
${#temporals.format(f, 'dd/MM/yyyy')}
${#numbers.formatDecimal(n, 1, 'POINT', 2, 'COMMA')}
${#strings.abbreviate(t, 60)}     ${#strings.isEmpty(t)}
${#lists.size(l)}                 ${#lists.isEmpty(l)}
```

## Fragmentos

```html
<footer th:fragment="pie">…</footer>
<nav th:fragment="menu(activo)">…</nav>

<div th:replace="~{fragmentos/comunes :: menu('productos')}"></div>
```

| | Resultado |
|---|---|
| `th:replace` | **sustituye** la etiqueta anfitriona ← usa esta |
| `th:insert` | la mete dentro |

Con *layout dialect*:
```html
<html layout:decorate="~{layout}">
  <main layout:fragment="contenido">…</main>
</html>
```

## Formularios

```html
<form th:action="@{/productos}" th:object="${producto}" method="post">
    <label for="nombre">Nombre</label>
    <input type="text" id="nombre" th:field="*{nombre}">
    <p th:if="${#fields.hasErrors('nombre')}" th:errors="*{nombre}"></p>
    <button type="submit">Guardar</button>
</form>
```

```java
@PostMapping
public String crear(@Valid @ModelAttribute("producto") ProductoForm form,
                    BindingResult errores,        // ← INMEDIATAMENTE después
                    Model modelo, RedirectAttributes flash) {
    if (errores.hasErrors()) {
        modelo.addAttribute("categorias", Categoria.values());  // repoblar
        return "productos/formulario";            // 200, sin redirigir
    }
    servicio.crear(form);
    flash.addFlashAttribute("exito", "Creado");
    return "redirect:/productos";                 // PRG
}
```

Error global (no de un campo): `errores.reject("codigo", "mensaje")`
Error de campo: `errores.rejectValue("nombre", "codigo", "mensaje")`

## Reglas de oro

| Regla | Por qué |
|---|---|
| `@Controller`, no `@RestController` | o verás el nombre de la plantilla como texto |
| `th:action`, no `action` | o no se inyecta el CSRF → 403 |
| `BindingResult` justo detrás de `@Valid` | o lanza excepción en vez de recoger errores |
| Redirigir tras un POST correcto | F5 no duplica (PRG) |
| DTO al modelo, nunca la entidad | evita N+1 y `LazyInitializationException` |
| Validar en el servidor **siempre** | el HTML se salta con el inspector |
| Borrar con `<form method="post">` | los GET deben ser seguros |

## htmx

```html
<input hx-get="/productos/filas" hx-trigger="keyup changed delay:300ms"
       hx-target="#cuerpo" hx-swap="outerHTML" name="nombre">

<button hx-delete="/productos/5" hx-target="closest tr"
        hx-swap="outerHTML" hx-confirm="¿Seguro?">Borrar</button>
```

```java
return "productos/lista :: filas";   // devolver SOLO el fragmento
```

## Seguridad en la vista

```html
<div sec:authorize="isAuthenticated()">
    <span sec:authentication="name">usuario</span>
</div>
<a sec:authorize="hasRole('ADMIN')" th:href="@{/admin}">Admin</a>
```
:material-alert: Esto solo decide **qué se pinta**. Proteger = cadena de filtros o `@PreAuthorize`.

Login: campos `username` y `password`. Logout: **POST**.

## Idiomas

```properties
carrito.total=Total: {0} €
```
```html
<p th:text="#{carrito.total(${total})}">Total: 0 €</p>
```

## Tests

```java
.andExpect(view().name("productos/lista"))
.andExpect(model().attributeExists("productos"))
.andExpect(model().attributeHasFieldErrors("producto", "nombre"))
.andExpect(content().string(containsString("Teclado")))
.andExpect(redirectedUrl("/productos"))
.andExpect(flash().attributeExists("exito"))
```
POST en los tests: `.with(csrf())`
