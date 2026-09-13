# Preparar el examen — UT8

**:material-laptop: Examen práctico · 100 % · 2 sesiones**

## Formato

Se te entrega una **API funcionando** sobre un dominio que no has usado en clase, con su servicio, su repositorio y sus datos ya cargados. Tu trabajo es ponerle una interfaz web completa.

No hay que tocar la capa de negocio. Si te ves modificando el servicio, casi seguro estás resolviendo en el sitio equivocado.

## Qué se pide

1. **Plantilla base** con menú, pie y CSS, y al menos tres páginas que la usen.
2. **Listado paginado** con filtro y buscador, con mensaje propio cuando no hay resultados.
3. **Ficha de detalle** en su propia ruta.
4. **Alta y edición** con el mismo formulario, validación de servidor y errores por campo.
5. **Post-Redirect-Get** con mensaje *flash* tras guardar.
6. **Borrado** por POST y con confirmación.
7. **Una parte que se actualice sin recargar** (buscador en vivo, borrado de fila o contador).
8. **Login de formulario**, menú que cambia según la sesión y una sección solo para administradores.
9. **Páginas de error propias** para 404 y acceso denegado.
10. **Cuatro tests**: vista, formulario válido, formulario inválido y seguridad por rol.

## Rúbrica

| Criterio | Peso | Qué se mira |
|---|---:|---|
| 1. Arranca y las páginas cargan *(eliminatorio)* | 10 % | `mvn spring-boot:run` y las rutas responden 200 |
| 2. Separación y reutilización | 15 % | `@Controller` aparte del de API, servicio compartido sin duplicar reglas, DTO al modelo |
| 3. Plantillas y fragmentos | 15 % | *Layout* base, fragmentos con parámetros, nada de `<head>` repetido |
| 4. **Formulario y validación** | 25 % | `th:field`, `BindingResult` bien colocado, errores por campo, desplegables repoblados, **el servidor rechaza lo que el HTML deja pasar** |
| 5. PRG, *flash* y borrado seguro | 10 % | Redirección tras POST, F5 no duplica, borrado por POST |
| 6. Contenido dinámico | 10 % | Fragmento devuelto por AJAX o htmx, con *debounce* |
| 7. Seguridad de la vista | 10 % | Login funcional, `sec:authorize` **y** ruta protegida de verdad |
| 8. Tests | 5 % | Los cuatro, en verde y con sentido |

**Eliminatorio:** si la aplicación no arranca o las páginas dan error 500, la nota máxima es 4.

## Cómo se corrige el criterio 4

Se comprueba con dos órdenes, y no admiten interpretación:

```bash
# 1. El servidor rechaza lo que el navegador dejaría pasar
curl -X POST localhost:8080/productos -d "nombre=&precio=-5" -w "\n%{http_code}\n"
#    → debe volver el formulario con errores, y NO crear nada

# 2. Los errores llegan a la plantilla
curl -s -X POST localhost:8080/productos -d "nombre=" | grep -c "obligatorio"
#    → distinto de 0
```

Si la primera orden crea un registro, el criterio 4 es **cero**, aunque en el navegador el formulario se comporte perfectamente.

## Errores que más cuestan

1. **`@RestController` en el controlador de vistas.** Sale el nombre de la plantilla como texto en pantalla. Un minuto de arreglo, media hora de desconcierto.
2. **`action="..."` en vez de `th:action="@{...}"`.** No se inyecta el token CSRF y todos los POST dan 403.
3. **`BindingResult` mal colocado.** Tiene que ir *inmediatamente* después del parámetro `@Valid`. Si metes algo en medio, se lanza una excepción en lugar de recoger los errores.
4. **Validar solo en HTML.** `required` y `min` no protegen nada: se borran desde el inspector.
5. **Devolver la vista tras guardar** en vez de redirigir. F5 duplica el registro.
6. **No repoblar los desplegables** al volver con errores. El `select` sale vacío y parece un fallo de la aplicación.
7. **Meter la entidad en el modelo.** `LazyInitializationException` a media página, o el N+1 de la UT5 otra vez.
8. **Ocultar el enlace y dejar la ruta abierta.** `sec:authorize` no protege; se comprueba pidiendo la URL con `curl`.
9. **Borrado con `<a href>`.** Un rastreador o el precargador del navegador puede vaciarte la base de datos.
10. **`th:utext` sobre un campo de formulario.** Es XSS servido en bandeja, y se penaliza aunque no lo explote nadie.
11. **Tests sin `.with(csrf())`.** Todos dan 403 y parece que el controlador está roto.
12. **Dejar la `Whitelabel Error Page`.** Dos ficheros de trabajo, y da muy mala impresión.

## Cómo prepararte

La semana antes, en este orden:

1. Rehaz la **F4** del reto sobre otro dominio, de memoria y sin mirar. Es el 25 % del examen.
2. Repasa la [chuleta](chuleta.md) hasta que los cuatro símbolos te salgan solos.
3. Ejecuta las dos órdenes `curl` de arriba contra tu proyecto. Si alguna falla, ahí tienes el trabajo.
4. Rompe una plantilla a propósito y comprueba que tus tests lo detectan.
