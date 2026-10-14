# UT6 — Servicios web: WebSockets, GraphQL y documentación

**18 h · 18 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA7:** Desarrolla servicios web reutilizables y accesibles mediante protocolos web, verificando su funcionamiento.

Hasta ahora, **el cliente preguntaba y el servidor contestaba**. Siempre, en un solo formato, por una sola puerta y sin documentar.

Aquí se abren tres puertas más a lo mismo: **WebSockets** para que el servidor hable cuando quiera, **GraphQL** para que el cliente pida exactamente lo que necesita, y **OpenAPI** para que alguien pueda usar tu API sin preguntarte nada.

!!! quote "Autoría del material de los temas 1, 2 y 3"
    Los tres primeros temas son una **adaptación del material de [José Luis González Sánchez](https://github.com/joseluisgs)** (repositorio [DesarrolloWebEntornosServidor-02-2025-2026](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-02-2025-2026), licencia [CC BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/)), con el texto, los diagramas y los ejemplos originales.

    Las adaptaciones de este curso: **Maven en vez de Gradle**, **Java 25**, y los apartados de proyectos y ejercicios prácticos. Cada tema lleva su nota de autoría arriba.

!!! success "Un servicio, tres transportes"
    ```mermaid
    graph TB
        REST["REST<br/>/api/v1/funkos"] --> S["FunkosService<br/>@Service"]
        WS["WebSocket<br/>/ws/v1/funkos"] --> S
        GQL["GraphQL<br/>/graphql"] --> S
        S --> R["FunkosRepository"]
        R --> BD[("MySQL")]
        DOC["OpenAPI<br/>/swagger-ui"] -.->|documenta| REST
    ```

    **Esta unidad no va de aprender tres tecnologías.** Va de comprobar que la arquitectura de la UT4 y la UT5 aguanta: que el mismo servicio sirve para tres transportes distintos sin tocarlo.

!!! danger "Lo que esta unidad mide de verdad"
    El criterio que vale más puntos en las tres rúbricas es el mismo:

    ```bash
    git diff --stat src/main/java/**/services/
    ```

    Si el servicio ha cambiado en algo más que un `publishEvent`, la unidad está mal resuelta **aunque todo funcione**. Y lo que estará mal no es esta unidad: es la de hace dos meses.

    Un servicio con `ResponseEntity` dentro no se puede usar desde un WebSocket ni desde GraphQL, porque ahí los códigos HTTP no significan nada.

---

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**.

- [ ] Explicar cuándo toca **WebSocket, SSE o *polling***, y por qué.
- [ ] Montar un **WebSocket** con sesiones concurrentes, limpieza y *broadcast* tolerante a fallos.
- [ ] Notificar **después de confirmar la transacción**, con eventos de Spring.
- [ ] Escribir un **esquema GraphQL** con tipos, entradas, queries, mutaciones y suscripciones.
- [ ] Resolver el **N+1 de GraphQL** con `@BatchMapping`, y **medirlo**.
- [ ] Traducir tus excepciones de dominio a `ErrorType` de GraphQL.
- [ ] Limitar **profundidad y complejidad** de las consultas, y apagar la introspección.
- [ ] Explicar **quién bloquea** una petición por CORS y configurarlo por perfiles.
- [ ] Documentar una API con **OpenAPI**, incluidos los errores de negocio.
- [ ] Testear los tres transportes: `MockMvc`, cliente WebSocket y `GraphQlTester`.

## Los temas

| | | |
|:-:|---|---|
| **1** | [WebSockets](01-websockets.md) | Comunicación bidireccional, sesiones, notificaciones |
| **2** | [GraphQL](02-graphql.md) | Esquema, queries, mutaciones, suscripciones, el N+1 |
| **3** | [CORS y documentación de APIs](03-documentacion-apis.md) | Política de origen, *preflight*, OpenAPI y Swagger |
| **4** | [Proyecto · WebSockets](04-proyecto-websockets.md) | Notificaciones en tiempo real, en 7 pasos |
| **5** | [Proyecto · GraphQL](05-proyecto-graphql.md) | Esquema, resolutores y `@BatchMapping`, en 5 pasos |
| **6** | [Proyecto · CORS y OpenAPI](06-proyecto-openapi.md) | CORS por perfiles y la API documentada, en 4 pasos |

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | HTTP frente a WebSocket · *polling*, SSE y WebSocket · el `101` | [1. WebSockets](01-websockets.md) §1.1 |
| **S2** | Instalar y configurar: el `handler` y las sesiones concurrentes | [1. WebSockets](01-websockets.md) §1.1 |
| **S3** | Enviar notificaciones · eventos y **`AFTER_COMMIT`** | [1. WebSockets](01-websockets.md) §1.2 |
| **S4** | **Proyecto WebSockets**: pasos 1 a 4 (evento, `handler`, notificador) | [4. Proyecto WS](04-proyecto-websockets.md) |
| **S5** | **Proyecto WebSockets**: pasos 5 a 7 (endpoint, prueba y tests) | [4. Proyecto WS](04-proyecto-websockets.md) |
| **S6** | Qué es GraphQL · comparación con REST · sintaxis del esquema | [2. GraphQL](02-graphql.md) §2.1–2.2 |
| **S7** | Instalación · esquema · el controlador y los resolutores | [2. GraphQL](02-graphql.md) §2.3–2.5 |
| **S8** | Consultas, mutaciones y suscripciones · **el N+1 y `@BatchMapping`** | [2. GraphQL](02-graphql.md) §2.6–2.7 |
| **S9** | **Proyecto GraphQL**: pasos 1 a 3 (esquema, controlador, excepciones) | [5. Proyecto GraphQL](05-proyecto-graphql.md) |
| **S10** | **Proyecto GraphQL**: pasos 4 y 5 · **contar las consultas** | [5. Proyecto GraphQL](05-proyecto-graphql.md) |
| **S11** | **CORS**: quién bloquea, el *preflight*, configurar por perfiles | [3. CORS y docs](03-documentacion-apis.md) §3.1 |
| **S12** | **OpenAPI y Swagger**: documentar endpoints y DTOs | [3. CORS y docs](03-documentacion-apis.md) §3.2 |
| **S13** | **Proyecto OpenAPI**: los 4 pasos · cruzar las APIs entre parejas | [6. Proyecto OpenAPI](06-proyecto-openapi.md) |
| **S14** | Repaso: los tres transportes y el criterio de reutilización | [Chuleta](chuleta.md) |
| **S15** | :material-pencil: Laboratorio: un ejercicio del **bloque WebSockets** | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S16** | :material-pencil: Laboratorio: un ejercicio del **bloque GraphQL** | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S17** | :material-pencil: Laboratorio: un ejercicio del **bloque documentación** | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S18** | :material-laptop: **Examen práctico de RA7 (100 %)** | [Chuleta](chuleta.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, en la S18: se da un proyecto con la API REST y la persistencia ya hechas, y hay que **añadirle** un canal de notificaciones, un esquema GraphQL con su resolutor agrupado, y documentarlo.

Se valora, por criterios: que **el servicio no se toque**, las sesiones concurrentes y la limpieza, `AFTER_COMMIT`, el esquema GraphQL, **`@BatchMapping` medido**, las excepciones traducidas, CORS por perfiles, la documentación con los errores de negocio y los tests.

Los **[nueve ejercicios prácticos](ejercicios-practicos.md)** son enunciados del mismo tamaño y con la misma rúbrica: tres por bloque.

!!! success "Las tres rúbricas, resumidas"
    **Hay una rúbrica por bloque**, y las tres empiezan por el mismo criterio:

    | Bloque | Lo que más pesa |
    |---|---|
    | **1 · WebSockets** | **Reutilización (2,0)** · `AFTER_COMMIT` (1,0) · sesiones concurrentes y `try` (2,2) |
    | **2 · GraphQL** | **Reutilización (2,0)** · **`@BatchMapping` medido (2,0)** · esquema (1,0) |
    | **3 · Documentación** | Los **409 de negocio documentados (2,0)** · endpoints (1,5) · CORS (1,0) |

    Están completas, con los ítems comprobables uno a uno y las penalizaciones, en [Ejercicios prácticos](ejercicios-practicos.md).

    **El criterio de reutilización no puede quedar en 0:** el RA7 dice literalmente *«servicios web reutilizables»*.

!!! danger "Dos puntos se corrigen contando consultas en el log"
    No hay defensa oral. Pero el **ítem 2.2 de la rúbrica de GraphQL vale 2 puntos y es todo o nada**: se pone `show-sql=true`, se pide una consulta que atraviese dos relaciones con 20 elementos, y se cuentan las líneas `Hibernate:`.

    | Resultado | Nota del ítem |
    |---|:-:|
    | 2 o 3 consultas | **2,0** |
    | Entre 4 y 10 | 1,0 |
    | Más de 10 (patrón 1+N) | **0** |

    Un `@BatchMapping` que falta no se detecta de ninguna otra manera: el programa funciona.

## Material

| | |
|---|---|
| [**Proyecto · WebSockets**](04-proyecto-websockets.md) | Notificaciones en tiempo real, con el código entero |
| [**Proyecto · GraphQL**](05-proyecto-graphql.md) | Esquema, resolutores y `@BatchMapping` |
| [**Proyecto · CORS y OpenAPI**](06-proyecto-openapi.md) | CORS por perfiles y la API documentada |
| [**Ejercicios prácticos**](ejercicios-practicos.md) | **Nueve enunciados del formato del examen**, tres por bloque, con sus rúbricas |
| [**Chuleta**](chuleta.md) | Los tres transportes y los 10 errores del examen · **[descargable en PDF](../pdf/chuleta-ut6.pdf)** |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pégale tu código y te dice si aguanta la rúbrica |

## Antes de la S1

- [ ] **Uno de los dos proyectos de la [UT5](../ut5/04-proyectos.md)** funcionando: es el punto de partida de los tres de aquí.
- [ ] Comprueba que tus servicios están limpios:

    ```bash
    grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/
    ```

    Si eso devuelve algo, **arréglalo antes de la S1**: toda la unidad depende de ello.

- [ ] Repasa los [códigos de estado y la negociación de contenido](../ut1/index.md) de la UT1: CORS y el `101` vienen de ahí.
- [ ] Node instalado, para `npx wscat` (opcional, pero cómodo).

!!! info "Y con esto se cierra el trimestre"
    ```mermaid
    graph LR
        UT4["UT4 · Capas<br/>RA5"] --> UT5["UT5 · JPA<br/>RA6"] --> UT6["UT6 · Servicios web<br/>RA7"] --> P["2.º trimestre<br/>Proyecto en sprints"]
    ```

    En enero, el proyecto por sprints parte de aquí: **una API con capas, persistencia, tiempo real y documentada**. Lo que se añade es seguridad (UT8), despliegue (UT9) y trabajo en equipo.
