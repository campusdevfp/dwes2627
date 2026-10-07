# UT6 — Servicios web: WebSockets, GraphQL y documentación

**18 h · 18 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA7:** Desarrolla servicios web reutilizables y accesibles mediante protocolos web, verificando su funcionamiento.

Hasta ahora, **el cliente preguntaba y el servidor contestaba**. Siempre, en un solo formato, por una sola puerta y sin documentar.

Aquí se abren tres puertas más a lo mismo: **WebSockets** para que el servidor hable cuando quiera, **GraphQL** para que el cliente pida exactamente lo que necesita, y **OpenAPI** para que alguien pueda usar tu API sin preguntarte nada.

!!! quote "Autoría del material de los temas 1, 2 y 3"
    Los tres primeros temas son una **adaptación del material de [José Luis González Sánchez](https://github.com/joseluisgs)** (repositorio [DesarrolloWebEntornosServidor-02-2025-2026](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-02-2025-2026), licencia [CC BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/)), con el texto, los diagramas y los ejemplos originales.

    Las adaptaciones de este curso: **Maven en vez de Gradle**, **Java 25**, y los apartados de proyecto, retos y ejercicios. Cada tema lleva su nota de autoría arriba.

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
| **4** | [Proyecto completo paso a paso](04-proyecto-completo.md) | Los tres sobre el proyecto de la UT5, en 14 pasos |

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | HTTP frente a WebSocket · *polling*, SSE y WebSocket · el `101` | [1. WebSockets](01-websockets.md) §1.1 |
| **S2** | Instalar y configurar: el `handler` y las sesiones concurrentes | [1. WebSockets](01-websockets.md) §1.1 |
| **S3** | Enviar notificaciones · eventos y **`AFTER_COMMIT`** | [1. WebSockets](01-websockets.md) §1.2 |
| **S4** | :material-check-circle: **Reto 1.1 · Notificador de funkos** | [4. Proyecto](04-proyecto-completo.md) pasos 1–6 |
| **S5** | :material-check-circle: **Reto 1.2 · Panel de stock**, con huecos | [Retos · bloque 1](retos.md) |
| **S6** | Qué es GraphQL · comparación con REST · sintaxis del esquema | [2. GraphQL](02-graphql.md) §2.1–2.2 |
| **S7** | Instalación · esquema de productos y categorías · el controlador | [2. GraphQL](02-graphql.md) §2.3–2.5 |
| **S8** | Consultas, mutaciones y **suscripciones** · el **N+1** y `@BatchMapping` | [2. GraphQL](02-graphql.md) §2.6–2.7 |
| **S9** | :material-check-circle: **Reto 2.1 · GraphQL de funkos** | [4. Proyecto](04-proyecto-completo.md) pasos 7–10 |
| **S10** | :material-check-circle: **Reto 2.2 · GraphQL del instituto**, con huecos | [Retos · bloque 2](retos.md) |
| **S11** | **CORS**: quién bloquea, el *preflight*, configurar por perfiles | [3. CORS y docs](03-documentacion-apis.md) §3.1 |
| **S12** | **OpenAPI y Swagger**: documentar endpoints y DTOs | [3. CORS y docs](03-documentacion-apis.md) §3.2 |
| **S13** | :material-check-circle: **Retos 3.1 y 3.2** · cruzar APIs entre parejas | [4. Proyecto](04-proyecto-completo.md) pasos 11–13 |
| **S14** | **Testear los tres transportes** | [4. Proyecto](04-proyecto-completo.md) paso 14 |
| **S15** | :material-upload: Laboratorio de los retos que se entregan (1.ª parte) | [Retos](retos.md) |
| **S16** | :material-upload: Laboratorio de los retos que se entregan (2.ª parte) | [Retos](retos.md) |
| **S17** | :material-upload: Defensa de los retos · repaso de la rúbrica | [Retos](retos.md) |
| **S18** | :material-laptop: **Examen práctico de RA7 (100 %)** | [Chuleta](chuleta.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, en la S18: se da un proyecto con la API REST y la persistencia ya hechas, y hay que **añadirle** un canal de notificaciones, un esquema GraphQL con su resolutor agrupado, y documentarlo.

Se valora, por criterios: que **el servicio no se toque**, las sesiones concurrentes y la limpieza, `AFTER_COMMIT`, el esquema GraphQL, **`@BatchMapping` medido**, las excepciones traducidas, CORS por perfiles, la documentación con los errores de negocio y los tests.

Los **[retos 1.3, 2.3 y 3.3](retos.md)** son enunciados del mismo tamaño y con la misma rúbrica.

!!! success "Las tres rúbricas, resumidas"
    | Bloque | Lo que más pesa |
    |---|---|
    | **1 · WebSockets** | El servicio no cambia (2,0) · `AFTER_COMMIT` (1,5) · sesiones y `try` (3,0) |
    | **2 · GraphQL** | El servicio no cambia (2,0) · **`@BatchMapping` medido (2,0)** · esquema (1,5) |
    | **3 · Documentación** | Endpoints documentados (2,0) · **los 409 de negocio (2,0)** · CORS (1,5) |

    Están completas en [Retos](retos.md).

!!! danger "En la defensa se cuentan las consultas"
    Como en la UT5: se pide una consulta GraphQL anidada **con `show-sql=true` en pantalla** y se cuentan las consultas SQL. Un `@BatchMapping` que falta se ve en dos segundos, y un programa que funciona no lo delata de ninguna otra manera.

## Material

| | |
|---|---|
| [**Proyecto completo**](04-proyecto-completo.md) | Los tres transportes sobre el proyecto de la UT5, con el código entero |
| [**Retos**](retos.md) | **Nueve enunciados**: tres por bloque, dos resueltos y uno que se entrega |
| [**Batería de ejercicios**](ejercicios.md) | 24 con solución, graduales y en el orden de los temas |
| [Chuleta](chuleta.md) | Los tres transportes y los diez errores del examen, en una página |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pégale tu código y te dice si aguanta la rúbrica |

## Antes de la S1

- [ ] El [proyecto de la UT5](../ut5/03-proyecto-completo.md) funcionando con MySQL en Docker.
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
