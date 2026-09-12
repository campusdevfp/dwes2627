# Reto de la unidad — UT8

> **Reto `ut8-vistas-base`**, en GitHub Classroom. Una API funcionando y tests de vistas en rojo: plantillas, formularios con validación y contenido dinámico.
>
> Arranca desde una base que funciona: lo que te saliera mal en la unidad anterior no te condiciona.

| Fase | Sesiones | Qué haces |
|---|---|---|
| F1 · La primera página | S4–S6 | Dependencia, plantilla base, listado que se ve |
| F2 · El catálogo completo | S7–S9 | Tabla, paginación, filtros, ficha de producto |
| F3 · Todo reutilizable | S10–S13 | *Layout*, fragmentos, páginas de error |
| F4 · Alta y edición | S14–S18 | Formulario, validación, PRG, mensajes |
| F5 · Borrado y CSRF | S19–S20 | Borrado seguro, confirmación |
| F6 · Sin recargar | S21–S24 | Buscador en vivo y carrito con htmx |
| F7 · Con usuarios | S25–S26 | Login, roles en la vista, idiomas |
| F8 · Probado | S27 | Tests de vistas y de formulario |

---

## F1 — La primera página (S4–S6)

Añade la dependencia, crea `layout.html` y un `ProductoVistaControlador` que liste los productos en una tabla mínima.

??? success "Comprobación"

    ```bash
    curl -s localhost:8080/productos | head -20      # debe salir HTML, no JSON
    curl -s localhost:8080/api/v1/productos | head   # y esto debe seguir dando JSON
    ```

    Si por `/productos` sale el texto `productos/lista`, tienes `@RestController`. Es el primer error de la unidad y ya te lo has llevado.

    **Del profesor:** abre `templates/productos/lista.html` con doble clic, directamente desde el explorador de archivos. Debe verse una tabla con los datos de ejemplo. Eso es el *prototipado natural* del tema 1, y es la razón de que Thymeleaf ganara a JSP.


## F2 — El catálogo completo (S7–S9)

Tabla con nombre, categoría, precio formateado y estado de stock. Paginación con el mismo `Page<T>` de la UT6. Filtro por categoría y buscador por nombre (formulario `GET`, todavía sin JavaScript). Ficha de detalle en `/productos/{id}`.

??? success "Comprobación"

    - La lista vacía muestra un mensaje, no una tabla en blanco.
    - El precio sale `1.234,56 €`, no `1234.56`.
    - Los enlaces de paginación conservan el filtro activo.
    - El buscador funciona **con JavaScript desactivado** (mira en el menú del navegador).

    **Trampa habitual:** al pasar de página se pierde el filtro. Se arregla arrastrando los parámetros en el enlace:

    ```html
    <a th:href="@{/productos(page=${pagina.number + 1}, categoria=${categoria}, q=${q})}">Siguiente</a>
    ```


## F3 — Todo reutilizable (S10–S13)

Saca el menú, el pie y la cabecera a `fragmentos/comunes.html`. Crea fragmentos de `estado(valor)`, `paginacion(pagina, url)` y `alerta(tipo, mensaje)`. Añade `error/404.html` y `error/500.html`.

??? success "Comprobación"

    Cuenta las líneas repetidas antes y después. Si el `<head>` sigue estando en cinco ficheros, la fase no está hecha.

    ```bash
    curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/productos/99999   # 404
    curl -s localhost:8080/productos/99999 | grep -c "Whitelabel"             # debe dar 0
    ```

    Ese `Whitelabel Error Page` en una entrega resta, y quitarlo cuesta dos ficheros.


## F4 — Alta y edición (S14–S18)

`ProductoForm` con Bean Validation, formulario con `th:field`, validación de servidor, PRG y mensaje *flash*. El mismo formulario sirve para crear y para editar.

??? success "Comprobación"

    Las cuatro pruebas obligatorias, en este orden:

    1. Envía el formulario vacío → vuelves al formulario, con los errores **debajo de cada campo** y el desplegable de categorías relleno.
    2. Envía datos correctos → redirección, mensaje verde, producto en la lista.
    3. Pulsa **F5** después de guardar → **no** debe preguntar «¿reenviar formulario?».
    4. Salta la validación del navegador y comprueba que el servidor la rechaza igual:

    ```bash
    curl -X POST localhost:8080/productos -d "nombre=&precio=-5"
    ```

    Si esa orden crea un producto, la validación del servidor no existe y en el examen es un cero en ese criterio.


## F5 — Borrado y CSRF (S19–S20)

Borrado con `<form method="post">` y confirmación. Explica en tu propio README por qué no es un enlace.

??? success "Comprobación"

    ```bash
    # sin token → 403
    curl -X POST localhost:8080/productos/1/borrar -w "\n%{http_code}\n"
    ```

    Y una comprobación que sorprende: escribe `action="/productos"` en vez de `th:action` en cualquier formulario y mira cómo empieza a dar 403 sin tocar nada más. Es la causa número uno de «no me funciona».


## F6 — Sin recargar (S21–S24)

Buscador en vivo con htmx devolviendo el fragmento `filas`. Botón de borrado que elimina la fila sin recargar. Contador de carrito que se actualiza al añadir.

??? success "Comprobación"

    - Teclea despacio en el buscador y mira la pestaña **Red** del navegador: debe haber **una** petición por pausa, no una por tecla. Si hay una por tecla, falta `delay:300ms`.
    - La respuesta de `/productos/filas` es HTML suelto, sin `<html>` ni `<head>`.
    - Desactiva JavaScript: el buscador debe seguir funcionando con su `<form method="get">`. Eso es la mejora progresiva.
    - Comprueba el token CSRF en las peticiones de htmx (pestaña Red → Cabeceras).


## F7 — Con usuarios (S25–S26)

Login de formulario reutilizando los usuarios de la UT7. Menú que cambia según haya sesión o no. Sección `/admin` solo para administradores. Textos en `messages.properties` y selector de idioma.

??? success "Comprobación"

    ```bash
    # la protección real, no la visual
    curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/admin      # 302 al login
    ```

    Entra como usuario normal y comprueba que **no ves** el enlace de administración. Después escribe `/admin` a mano en la barra de direcciones: debe dar 403, no la página.

    Si te deja entrar, tenías el enlace oculto pero la ruta abierta. Es el error conceptual más importante del tema 6.


## F8 — Probado (S27)

Tests de vista, de formulario válido, de formulario inválido, de CSRF y de seguridad por rol. Más el test parametrizado que recorre todas las páginas.

??? success "Comprobación"

    ```bash
    mvn test
    ```

    Prueba deliberada: rompe un `th:text` a propósito (escribe `${p.nombreee}`) y comprueba que **el test lo caza**. Si tus tests pasan con la plantilla rota, no están comprobando el renderizado: te falta el `content().string(containsString(...))`.


---

## Al terminar la unidad tienes

``` { .text .sinajuste }
tienda/
├── api/          JSON        ← UT6, intacta
├── web/          HTML        ← UT8, nueva
└── servicio/     compartido  ← UT4, sin tocar
```

Una sola lógica de negocio, dos formas de consumirla. Si has tenido que duplicar reglas entre el controlador de API y el de vistas, algo se coló en la capa equivocada — y ese es justo el criterio 2 del examen.
