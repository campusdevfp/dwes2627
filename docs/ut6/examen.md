# Examen práctico de la UT6 — cómo es y cómo prepararlo

**100 % de la nota · Desarrollo en ordenador · 3 sesiones (165 min)**

> Segundo examen práctico y el más completo del trimestre. Aquí no construyes una API: construyes una **API profesional**.

!!! tip "Se examina con chuleta"
    En el examen se te entrega impresa la **[chuleta de los prácticos del 1.er trimestre](../chuleta-examen-1t.md)**, y no puedes usar nada más.

    Está publicada desde septiembre: **entrena con ella**. Trae sintaxis, no criterio — lo que se evalúa no viene en la hoja.

## Formato

- Recibes un enunciado y un proyecto de partida: una API básica ya montada por capas (como la que acabaste en la UT4), a la que hay que añadir funcionalidad profesional.
- Puedes consultar la web del curso, tus prácticas y la documentación oficial. No asistentes de IA.
- Entrega: proyecto comprimido o push a tu repositorio.

## Qué se pide (tipo)

!!! example "Enunciado de ejemplo — API de cursos"
    Partiendo del proyecto entregado, amplíalo:

    1. Diseño REST correcto: versiona en `/api/v1`, añade el subrecurso `GET /api/v1/cursos/{id}/lecciones` y la acción `POST /api/v1/cursos/{id}/matriculas` (409 si no hay plazas).
    2. Respuestas: el POST debe devolver 201 con cabecera `Location`; el DELETE, `204`.
    3. Errores: `@RestControllerAdvice` con `ProblemDetail` (type, title, instance) para 404, 409 y 400 de validación.
    4. Paginación, orden y filtros: GET /api/v1/cursos?categoria=&precioMax=&q=&page=&size=&sort= devolviendo Page con metadatos.
    5. Documentación: springdoc operativo, con `@Tag`, `@Operation` y `@ApiResponses` en al menos un controlador.
    6. GraphQL: esquema con Query para listar y para uno por id, y una mutación de alta. Sin N+1 al pedir la relación.
    7. Tiempo real:
        un canal que difunda el alta de un recurso a todos los clientes conectados (WebSocket o SSE, justificando la elección).
    8. Tests: al menos tres con `MockMvc` — un 200 con `jsonPath`, un 404, y uno de paginación (página vacía = 200 con content vacío)).

## Rúbrica

| Criterio | Peso | Qué se valora |
|---|---|---|
| **1. Funciona** | 20 % | Compila, arranca y los endpoints responden lo pedido |
| **2. Diseño REST** | 15 % | Recursos, subrecursos, verbos, versionado, códigos correctos |
| **3. Paginación y filtros** | 15 % | `Pageable` operativo, metadatos, filtros opcionales combinables |
| **4. GraphQL y tiempo real** | 20 % | Esquema y consultas operativas **sin N+1**; un canal en vivo funcionando |
| **5. Documentación y errores** | 15 % | Swagger accesible y anotado; ProblemDetail con tipo e instancia |
| **6. Tests** | 15 % | Tres tests con sentido que pasan, incluido uno de paginación |

Cada criterio se puntúa **0–10**; la nota es la media ponderada. Se supera con **≥ 5**.

!!! danger "Lo que más penaliza"
    - Contraseñas sin hash o el secreto JWT escrito en el `application.yml` → criterio 4 muy penalizado, aunque funcione.
    - Confundir 401 y 403.
    - Devolver 200 en errores o el modelo interno en vez del DTO.
    - Paginación "a mano" devolviendo una List sin metadatos.
    - Tests que no compilan: arrastran el criterio 6 a 0 y pueden impedir que el proyecto arranque.

## Autochequeo — ¿sabes hacerlo sin mirar?

- [ ] Diseñar las URLs de un recurso con subrecursos y versionado.
- [ ] Devolver `ResponseEntity.created(location).body(dto)`.
- [ ] Escribir un manejador con `ProblemDetail` y `setProperty`.
- [ ] Añadir Pageable a un endpoint y devolver Page<Dto>.
- [ ] Encadenar filtros opcionales con el patrón param == null || condición.
- [ ] Escribir un esquema GraphQL y resolverlo con `@QueryMapping` y `@MutationMapping`.
- [ ] Detectar el N+1 en GraphQL contando consultas y arreglarlo con `@BatchMapping`.
- [ ] Difundir un evento a los clientes conectados y justificar SSE frente a WebSocket.
- [ ] Escribir un test con `mockMvc` y `jsonPath`.

## Estrategia durante el examen

1. Arranca el proyecto de partida y compruébalo antes de tocar nada
    (5 min).
2. Ve por orden de peso: seguridad (20 %) y funcionamiento (20 %) primero; documentación y tests después.
3. Prueba cada bloque con curl según lo terminas.
    No dejes la seguridad para el final: es lo que más se atasca.
4. Si la seguridad se te complica, déjala funcionando a medias pero sin romper el resto — más vale una API que arranca con seguridad parcial que una que no compila.
5. Reserva 15 minutos para tests, comprobar que `mvn test` pasa y que el proyecto arranca limpio.

> **Consejo:** el reto de la unidad es el examen. Si has hecho la TiendaAPI completa (paginada, documentada, con login y tests), el enunciado te resultará familiar hasta el aburrimiento — que es justo el objetivo.
