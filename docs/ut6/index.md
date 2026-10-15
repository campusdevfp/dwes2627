# UT6 — Servicios web: WebSockets, GraphQL y documentación

**18 h · 18 sesiones · Trimestre 1.º** · **RA7** · Evaluación: :material-laptop: **examen práctico (100 %)**

> Desarrolla servicios web reutilizables y accesibles mediante protocolos web, verificando su funcionamiento.

---

## Temas

| | | Qué se aprende |
|:-:|---|---|
| **1** | [WebSockets](01-websockets.md) | El `101`, sesiones concurrentes, *broadcast*, eventos y `AFTER_COMMIT` |
| **2** | [GraphQL](02-graphql.md) | Esquema, *queries*, mutaciones, suscripciones y **el N+1** |
| **3** | [CORS y documentación de APIs](03-documentacion-apis.md) | Política de origen, *preflight*, OpenAPI y Swagger |
| **4** | [Proyecto · WebSockets](04-proyecto-websockets.md) | Notificaciones en tiempo real, en 7 pasos |
| **5** | [Proyecto · GraphQL](05-proyecto-graphql.md) | Esquema, resolutores y `@BatchMapping`, en 5 pasos |
| **6** | [Proyecto · CORS y OpenAPI](06-proyecto-openapi.md) | CORS por perfiles y la API documentada, en 4 pasos |

| | |
|---|---|
| [**Ejercicios prácticos**](ejercicios-practicos.md) | Seis enunciados del formato del examen, con las rúbricas |
| [**Chuleta**](chuleta.md) | Referencia en una página · **[PDF para el examen](../pdf/chuleta-ut6.pdf)** |

!!! success "Un servicio, tres transportes"
    ```mermaid
    flowchart LR
        REST["REST<br/>/api/v1/…"] --> S["@Service"]
        WS["WebSocket<br/>/ws/v1/…"] --> S
        GQL["GraphQL<br/>/graphql"] --> S
        S --> R["@Repository"]
        DOC["OpenAPI"] -.->|documenta| REST
    ```

    No se aprenden tres tecnologías: se comprueba que **el mismo servicio de la UT5 sirve para los tres**, sin tocarlo.

---

## Cómo se evalúa

:material-laptop: **Examen práctico, 100 % de la nota de la unidad.** Cuatro horas, con el ordenador y **la [chuleta](../pdf/chuleta-ut6.pdf) impresa** como única documentación.

**Se te da** un proyecto con la API REST y la persistencia ya hechas. **Añades tú** uno de los dos transportes —**un WebSocket o un esquema GraphQL**, según el enunciado— **y la documentación OpenAPI** de la API.

### La rúbrica

| | Bloque | Puntos |
|---|---|:-:|
| 1 | **Reutilización: el servicio no cambia** | **2,0** |
| 2 | **El transporte que toque** (WebSocket o GraphQL) | **4,0** |
| 3 | **Documentación OpenAPI y CORS** | **2,5** |
| 4 | Configuración por perfiles | 0,8 |
| 5 | Tests | 0,7 |

Se aprueba con **5,0**. La rúbrica completa, con los dos desgloses del bloque 2 y las penalizaciones, está en [Ejercicios prácticos](ejercicios-practicos.md).

!!! danger "El bloque 1 no puede quedar en 0"
    El RA7 dice literalmente *«servicios web **reutilizables**»*. Se comprueba con tres comandos, antes de abrir ningún fichero:

    ```bash
    git diff --stat src/main/java/**/services/        # solo el publishEvent
    git diff --stat src/main/java/**/repositories/    # vacío
    grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/   # vacío
    ```

    Si el enunciado ha obligado a reescribir lógica de negocio, no se ha demostrado el RA.

!!! warning "Y 2 puntos se corrigen contando consultas"
    Si el examen es de GraphQL, el `@BatchMapping` se comprueba con `show-sql=true`: se pide una consulta con una relación y se cuentan las líneas `Hibernate:`. **2–3 consultas → total; patrón 1+N → cero.**

---

## Antes de empezar

- [ ] Uno de los [proyectos de la UT5](../ut5/04-proyectos.md) funcionando: es el punto de partida.
- [ ] Comprueba que tus servicios están limpios. Si esto devuelve algo, arréglalo antes de la primera sesión:

    ```bash
    grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/
    ```

- [ ] Repasa los [códigos de estado y la negociación de contenido](../ut1/index.md): CORS y el `101` vienen de ahí.
