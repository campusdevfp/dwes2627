# UT8 — Páginas dinámicas en el servidor

**26 h · 26 sesiones · Trimestre 2.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA8:** Genera páginas web dinámicas analizando y utilizando tecnologías y frameworks del servidor web que añadan código al lenguaje de marcas.

Llevas todo el curso devolviendo JSON. Se lee estupendamente con Bruno y no lo entiende **ni una sola persona**. En esta unidad tu aplicación deja de hablar solo con otros programas y empieza a tener cara: formularios, tablas, mensajes, login y una interfaz que puede usar alguien que no sabe qué es un `curl`.

!!! info "La misma aplicación, otra puerta"
    No se empieza de cero ni se tira nada. El servicio, el repositorio y las entidades son los de siempre. Se añade un **segundo controlador** que, en vez de serializar a JSON, rellena una plantilla.

    Al terminar tendrás la misma aplicación con dos entradas: `/api/v1/productos` para programas y `/productos` para personas. Que es exactamente como está montada cualquier aplicación real.


!!! reto "Esta unidad se da con el proyecto del trimestre"
    Desde enero no hay prácticas sueltas: hay **un producto**, [Pulso de Madrid](../proyecto/index.md), que se construye en equipos de tres. Esta unidad son los retos **«esto no lo entiende nadie»** y **«que lo use alguien que no seáis vosotros»**.

    Lee primero **[el proyecto](../proyecto/index.md)** y **[los seis retos](../proyecto/retos.md)**: el calendario de abajo son las sesiones de R3 y R4.

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Decidir qué se renderiza **en el servidor** y qué **en el cliente**, y por qué.
- [ ] Pintar vistas con **Thymeleaf**: expresiones, iteración, condicionales y URLs de contexto.
- [ ] Reutilizar **fragmentos y una plantilla base** para no repetir el HTML.
- [ ] Construir **formularios con validación**, mensajes de error y el patrón Post-Redirect-Get.
- [ ] Actualizar parte de la página **sin recargarla**.
- [ ] Adaptar las vistas a la **seguridad** (qué ve cada rol) y a varios **idiomas**.
- [ ] **Probar** las vistas y desplegarlas.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | De la API a la interfaz: SSR frente a CSR · por qué hoy todo es híbrido | [1. Servidor o cliente](01-servidor-o-cliente.md) §1–2 |
| **S2** | `@Controller` frente a `@RestController` · qué se ejecuta dónde | [1. Servidor o cliente](01-servidor-o-cliente.md) §5–6 |
| **S3** | Arranque: dependencia, carpetas y primera plantilla | [2. Thymeleaf esencial](02-thymeleaf-esencial.md) §1 |
| **S4** | Los cuatro símbolos: `${}`, `*{}`, `@{}`, `#{}` | [2. Thymeleaf esencial](02-thymeleaf-esencial.md) §2 |
| **S5** | Pintar valores: `th:text`, `th:utext` y el riesgo de XSS | [2. Thymeleaf esencial](02-thymeleaf-esencial.md) §3 |
| **S6** | Repetir con `th:each` · listas vacías | [2. Thymeleaf esencial](02-thymeleaf-esencial.md) §4 |
| **S7** | Decidir: `th:if`, `th:switch` · atributos y utilidades | [2. Thymeleaf esencial](02-thymeleaf-esencial.md) §5–7 |
| **S8** | El modelo: qué se manda a la vista (y qué nunca) | [2. Thymeleaf esencial](02-thymeleaf-esencial.md) §8–9 |
| **S9** | Fragmentos: `th:replace` frente a `th:insert` | [3. Fragmentos y layouts](03-fragmentos-y-layouts.md) §1–2 |
| **S10** | Plantilla base con *layout dialect* | [3. Fragmentos y layouts](03-fragmentos-y-layouts.md) §3 |
| **S11** | Fragmentos con parámetros · organizar `templates/` y páginas de error | [3. Fragmentos y layouts](03-fragmentos-y-layouts.md) §4 |
| **S12** | El ciclo de un formulario · el objeto de formulario | [4. Formularios](04-formularios-y-validacion.md) §1–2 |
| **S13** | `th:object`, `th:field`, `th:errors` | [4. Formularios](04-formularios-y-validacion.md) §3 |
| **S14** | `@Valid` y `BindingResult`: la trampa del orden | [4. Formularios](04-formularios-y-validacion.md) §4 |
| **S15** | Las dos validaciones y por qué solo cuenta una | [4. Formularios](04-formularios-y-validacion.md) §5 |
| **S16** | Post-Redirect-Get y mensajes *flash* | [4. Formularios](04-formularios-y-validacion.md) §6 |
| **S17** | Editar y borrar · por qué el borrado es un POST | [4. Formularios](04-formularios-y-validacion.md) §7 |
| **S18** | CSRF: qué ataque evita y por qué te da 403 | [4. Formularios](04-formularios-y-validacion.md) §8 |
| **S19** | Sin recargar: `fetch`, sus riesgos y devolver fragmentos | [5. Contenido dinámico](05-contenido-dinamico.md) §1–2 |
| **S20** | htmx: buscador en vivo y borrado sin recarga | [5. Contenido dinámico](05-contenido-dinamico.md) §3–4 |
| **S21** | Caso completo: el carrito · mejora progresiva | [5. Contenido dinámico](05-contenido-dinamico.md) §5–6 |
| **S22** | Login de formulario y `sec:authorize` | [6. Seguridad e idiomas](06-vistas-seguridad-e-idiomas.md) §1–3 |
| **S23** | Textos en varios idiomas · accesibilidad mínima | [6. Seguridad e idiomas](06-vistas-seguridad-e-idiomas.md) §4–5 |
| **S24** | Probar vistas y formularios con MockMvc · repaso | [7. Probar y desplegar](07-probar-y-desplegar-vistas.md) |
| **S25** | Laboratorio: la batería de vistas sobre otro dominio | [Batería de ejercicios](ejercicios.md) |
| **S26** | :material-laptop: **Examen práctico de RA8 (100 %)** | [Preparación](examen.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, 2 sesiones. Se te entrega una API funcionando y tienes que ponerle una interfaz web completa: listado paginado, alta y edición con validación, borrado, login y una parte que se actualice sin recargar la página. Rúbrica completa en la [página de preparación](examen.md).

## Lo que más se falla

Esta unidad tiene cuatro trampas que se repiten curso tras curso, y las cuatro dan 403 o un error que no dice nada:

| Síntoma | Causa |
|---|---|
| El navegador muestra `productos/lista` como texto | `@RestController` en vez de `@Controller` |
| Todos los POST dan 403 | `action="..."` en vez de `th:action="@{...}"`: falta el CSRF |
| Excepción rara al enviar el formulario | `BindingResult` no va justo detrás del objeto `@Valid` |
| «No me guarda pero tampoco da error» | Validaste solo en HTML; el servidor rechaza en silencio |

## Material

- [Reto con tests](practicas.md) — la *TiendaAPI* estrena interfaz, fase a fase.
- [Batería de ejercicios](ejercicios.md) — 12 ejercicios resueltos sobre otro dominio.
- [Chuleta](chuleta.md) — Thymeleaf, formularios y htmx en una página.
- [Preparar el examen](examen.md) — rúbrica y errores que más cuestan.

## Antes de la S1

- [ ] Tu proyecto de la UT7 arranca y la API responde.
- [ ] Repasa qué es un DTO ([UT4 tema 4](../ut4/04-dto-y-validacion.md)): aquí vuelven a ser la pieza clave.
- [ ] Ten a mano las llaves de tu base de datos: vamos a mirar mucho el HTML generado.
