# Reto de la unidad — UT6

> **Reto `ut6-api-base`**, en GitHub Classroom. Una aplicación con capas y base de datos que expone una API mínima, y tests en rojo que piden convertirla en una API profesional: paginada, filtrable, documentada, con GraphQL y con un canal en vivo.
>
> Arranca desde una base que funciona: lo de la UT5 no te condiciona.

Debajo, las **fases sugeridas**.

| Fase | Sesiones | Qué añades | Página |
|---|---|---|---|
| **F1** | S1–S2 | Rediseño REST: `/api/v1`, subrecursos, acciones | [1](../01-diseno-rest/) |
| **F2** | S3–S5 | `ResponseEntity`, `Location`, Problem Details | [2](../02-respuestas-y-errores/) |
| **F3** | S6–S8 | Paginación, ordenación y filtros | [3](../03-paginacion-y-busqueda/) |
| **F4** | S9–S10 | Documentación OpenAPI | [4](../04-documentacion-openapi/) |
| **F5** | S11–S14 | GraphQL: esquema, consultas y el N+1 | [5](../05-graphql/) |
| **F6** | S15–S17 | Tiempo real: WebSockets y SSE | [6](../06-websockets/) |
| **F7** | S18–S20 | Tests de API y consumo de terceros | [7](../07-testing-apis/) · [8](../08-consumir-apis/) |
| **F8** | S21–S24 | Laboratorio libre y repaso | — |

---

## F1 — Rediseño REST (S1–S2)

Renombra las rutas a `/api/v1/productos`. Añade el subrecurso `GET /api/v1/categorias/{cat}/productos` y la acción `POST /api/v1/productos/{id}/reposicion` (añade stock).

??? success "Comprobación"

    Todas las URLs con sustantivos en plural, versión `v1`, sin verbos. La reposición como subrecurso de acción, no como `/reponerProducto/{id}`. Revisa que los códigos sigan siendo correctos tras el cambio.


## F2 — Respuestas y errores (S3–S5)

1. El POST devuelve `201` con cabecera `Location`.
2. Todos los errores pasan por `@RestControllerAdvice` con `ProblemDetail` enriquecido (type, instance, timestamp).
3. Añade `SinStockException` → `409`.

??? success "Comprobación con curl"

    ```bash
    curl -i -X POST localhost:8080/api/v1/productos -H "Content-Type: application/json" \
      # 201 + Location
      -d '{"nombre":"Candado","categoria":"seguridad","precio":25.0,"stock":10}'
    curl -s localhost:8080/api/v1/productos/999 | jq        # 404 con type/title/instance
    curl -i -X PATCH "localhost:8080/api/v1/productos/1/stock?unidades=-9999"       # 409
    ```


## F3 — Paginación y filtros (S6–S8)

Convierte el listado en `Page<ProductoDto>` con `@PageableDefault(size=20, sort="nombre")` y añade los filtros opcionales `categoria`, `precioMin`, `precioMax` y `q`.

??? success "Claves"

    - Los filtros se combinan con el patrón `param == null || condición`.
    - La respuesta debe traer `content`, `totalElements`, `totalPages`.
    - Prueba `?page=99`: **200 con content vacío**, no 404.
    - Prueba dos criterios de orden a la vez: `?sort=categoria,asc&sort=precio,desc`.


## F4 — Documentación (S9–S10)

Añade springdoc, comprueba Swagger UI y anota el controlador de productos con `@Tag`, `@Operation` y `@ApiResponses`, y el DTO con `@Schema`.

??? success "Comprobación"

    `http://localhost:8080/swagger-ui.html` muestra tus endpoints agrupados por `@Tag`, con descripciones y los códigos documentados. Prueba el botón Try it out en el GET por id con un valor inexistente: debe verse tu ProblemDetail.


## F5 — GraphQL (S11–S14)

La misma API, otra puerta. Por orden:

1. Añade `spring-boot-starter-graphql` y activa el *playground* en `/graphiql`.
2. Escribe el esquema en `src/main/resources/graphql/schema.graphqls`.
3. `@QueryMapping` para el listado y para uno por id.
4. `@MutationMapping` para crear un producto.
5. Pide `{ productos { nombre categoria { nombre } } }` con 50 productos y **cuenta las consultas del log**.
6. Arréglalo con `@BatchMapping`.

??? success "Flujo de comprobación"

    ```bash
    curl -s localhost:8080/graphql -H "Content-Type: application/json" \
      -d '{"query":"{ productos { id nombre } }"}' | jq
    ```

    El paso 5 es el que da la lección: **el N+1 vuelve, y aquí no puedes prevenirlo con un `JOIN FETCH` fijo** porque es el cliente quien decide qué campos pide. Antes de `@BatchMapping` verás 51 consultas; después, 2.

    Pregunta de cierre: ¿en qué se parece esto al problema de la UT5 y en qué se diferencia?


## F6 — Tiempo real (S15–S17)

Un panel que se actualiza solo cuando alguien compra.

1. Configura STOMP sobre WebSocket con `@EnableWebSocketMessageBroker`.
2. Difunde desde el servicio con `SimpMessagingTemplate`, **después** de que la transacción haya confirmado.
3. Página mínima que se suscriba a `/tema/ventas` y añada una fila.
4. Repite lo mismo con **SSE** y compara.

??? success "Comprobación"

    Abre dos pestañas. Compra en una y comprueba que **la otra se actualiza sola**, sin recargar.

    Y responde por escrito en tu README: para este panel, ¿SSE o WebSocket? La respuesta correcta es SSE —el cliente no necesita responder— y saber justificarlo vale más que tenerlo funcionando.


## F7 — Tests y consumo externo (S18–S20)

Cinco tests mínimos: 200 con `jsonPath` · 404 con `$.title` · 400 de validación · página vacía con 200 · orden no permitido con 400. Y un endpoint que consuma una API pública con `RestClient`, *timeouts* y `onStatus`.

??? success "Comprobación"

    mvn test

    en verde con los cinco. Para el consumo externo, prueba también el caso de error (id inexistente en la API remota): tu API debe responder un código coherente, no un 500.


---

## Estado final del proyecto

Al terminar deberías tener una API con: versionado, CRUD completo con códigos correctos, subrecursos, paginación con filtros, errores estandarizados, documentación interactiva, login con JWT y autorización por roles, y una batería de tests. **Eso es un proyecto de portfolio**, no un ejercicio de clase: enséñalo en una entrevista.

Pásalo por el [comprobar tu trabajo con los tests](../comprobar-tu-trabajo/) para una revisión contra la rúbrica antes del examen.
