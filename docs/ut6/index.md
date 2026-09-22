# UT6 — Servicios web: REST, GraphQL y WebSockets

**18 h · 18 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA7:** Desarrolla servicios web reutilizables y accesibles mediante protocolos web, verificando su funcionamiento.

La unidad más larga y la más "de empresa" del curso. Entre la UT4 y la UT5 montaste una aplicación por capas con datos reales detrás; aquí la conviertes en una **API profesional**: paginada, filtrable, documentada y probada. Y añades las dos cosas que hoy pide cualquier empresa además de REST: **GraphQL** y **tiempo real con WebSockets**.

!!! info "Continuamos el proyecto"
    Seguimos con la *TiendaAPI*, ya persistida en la UT5. No se empieza de cero: se amplía. La paginación de esta unidad es la de verdad, contra la base de datos — no cortar una lista en memoria.

!!! note "Y la seguridad, ¿cuándo?"
    En la **UT7**, ya en el segundo trimestre. Proteger endpoints se entiende mejor cuando la API está terminada y sabes exactamente qué hay que proteger.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Diseñar una **API REST** con rutas, verbos y códigos de estado correctos.
- [ ] Construir respuestas con las **cabeceras** adecuadas y errores en formato **RFC 7807**.
- [ ] Implementar **paginación, ordenación y búsqueda** con criterios combinables.
- [ ] Documentar la API con **OpenAPI** y publicar su interfaz interactiva.
- [ ] Exponer los mismos datos con **GraphQL** y evitar el N+1 con `@BatchMapping`.
- [ ] Empujar datos al cliente con **WebSockets** o SSE cuando la petición-respuesta no basta.
- [ ] **Probar** la API a todos los niveles y **consumir** APIs de terceros con `RestClient`.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Repaso REST · diseño de recursos, jerarquías y versionado | [1. Diseño de APIs REST](01-diseno-rest.md) §1–4 |
| **S2** | `ResponseEntity` y control fino de la respuesta | [2. Respuestas y errores](02-respuestas-y-errores.md) §1–2 |
| **S3** | Cabeceras, `Location` y negociación de contenido | [2. Respuestas y errores](02-respuestas-y-errores.md) §3 |
| **S4** | Errores con **Problem Details** (RFC 7807) | [2. Respuestas y errores](02-respuestas-y-errores.md) §4–5 |
| **S5** | Paginación con `Pageable` y ordenación | [3. Paginación y búsqueda](03-paginacion-y-busqueda.md) §1–3 |
| **S6** | Filtrado y búsqueda por criterios combinables | [3. Paginación y búsqueda](03-paginacion-y-busqueda.md) §4–5 |
| **S7** | OpenAPI: documentar la API · Swagger UI y anotaciones | [4. Documentar con OpenAPI](04-documentacion-openapi.md) |
| **S8** | Lo que REST no resuelve: *over-* y *under-fetching* | [5. GraphQL](05-graphql.md) §1–2 |
| **S9** | Esquema, `@QueryMapping` y el *playground* | [5. GraphQL](05-graphql.md) §3–4 |
| **S10** | Mutaciones y `@BatchMapping`: el N+1 otra vez · cuándo REST y cuándo GraphQL | [5. GraphQL](05-graphql.md) §5–6 |
| **S11** | Tiempo real: *polling*, SSE y WebSockets | [6. WebSockets](06-websockets.md) §1–2 |
| **S12** | STOMP: canales, suscripciones y difusión | [6. WebSockets](06-websockets.md) §3–4 |
| **S13** | Un chat y un panel de stock en vivo | [6. WebSockets](06-websockets.md) §5 |
| **S14** | Tests de API con `MockMvc` y de integración | [7. Testing de APIs](07-testing-apis.md) |
| **S15** | Consumir otras APIs con `RestClient` | [8. Consumir APIs](08-consumir-apis.md) |
| **S16** | Laboratorio: la API de la biblioteca, paginada y filtrable | [Batería de ejercicios](ejercicios.md) |
| **S17** | Repaso integrador, documentación y dudas | [Reto R3, a entregar](retos.md) |
| **S18** | :material-laptop: **Examen práctico de RA7 (100 %)** | [Reto R3, a entregar](retos.md) |

## Cómo se evalúa

**Examen práctico (100 %)**: ampliar una API existente con paginación, filtrado, documentación, una consulta GraphQL y tests. Rúbrica de seis criterios, la misma del **[reto R3](retos.md)**.

## Material

| | |
|---|---|
| [**Retos**](retos.md) | Dos retos **resueltos** que se construyen en clase y **un tercero que se entrega**, con los mismos criterios que el examen |
| [**Batería de ejercicios**](ejercicios.md) | 32 ejercicios con solución sobre otro dominio, para comprobar que lo sabes hacer tú |
| [Chuleta](chuleta.md) | Anotaciones y estructura en una página |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pégale tu código y te dice si aguanta la rúbrica |

