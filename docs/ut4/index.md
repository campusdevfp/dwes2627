# UT4 — Spring Boot: capas, REST y JDBC

**18 h · 18 sesiones · Trimestre 1.º** · **RA5** · Evaluación: :material-laptop: **examen práctico (100 %)**

> Desarrolla aplicaciones web, identificando y aplicando mecanismos para separar el código de presentación de la lógica de negocio.

---

## Temas

| | | Qué se aprende |
|:-:|---|---|
| **1** | [Introducción a Spring Boot](01-introduccion-spring-boot.md) | Autoconfiguración, starters, *beans* y sus *scopes*, IoC y DI |
| **2** | [Spring Web REST](02-spring-web-rest.md) | `@RestController`, rutas, `ResponseEntity`, códigos de estado, versionado |
| **3** | [Repositorios, servicios y DTOs](03-repositorios-servicios-dtos.md) | `JdbcClient`, `RowMapper`, `@Service`, excepciones, DTOs, validación, caché |
| **4** | [Configuración y variables de entorno](04-configuracion.md) | `application.properties`, perfiles, `${VAR:defecto}`, `.env` en IntelliJ |
| **5** | [Dos proyectos paso a paso](05-proyectos.md) | **Funkos** (1 tabla) y **tienda** (3 tablas con transacción), con H2 por JDBC |

| | |
|---|---|
| [**Examen práctico**](examen.md) | Tres enunciados del formato del examen, con la rúbrica |
| [**Chuleta**](chuleta.md) | Referencia en una página · **[PDF para el examen](../pdf/chuleta-ut4.pdf)** |

!!! note "Base de datos sí, JPA no"
    Se habla con **H2 por JDBC**, con el SQL que escribes tú. **JPA e Hibernate son la [UT5](../ut5/index.md)**: aquí lo que se evalúa es la arquitectura.

---

## Cómo se evalúa

:material-laptop: **Examen práctico, 100 % de la nota de la unidad.** Con el ordenador y **la [chuleta](../pdf/chuleta-ut4.pdf) impresa** como única documentación.

**Se te da** el proyecto que arranca (`pom.xml`, los `application*.properties`, `schema.sql`, `data.sql` y la clase de arranque) **y, ya escritos, el repositorio —interfaz, implementación con `JdbcClient` y `RowMapper`— y el mapeador**.

**Escribes tú** las carpetas por capas, los modelos, los DTOs, las excepciones, el servicio con las reglas, el controlador, el manejador global y dos tests.

!!! success "El código dado es la especificación"
    El repositorio y el mapeador **no compilan** al abrir el proyecto, porque usan clases que tú no has creado. De ellos sale **el nombre de cada clase y el orden de los componentes de cada `record`**.

    Léelos antes de escribir nada. Y no los toques: si los modificas para que encajen con tus clases, hay **−1,5** de penalización.

Es un enunciado **del mismo formato y tamaño** que los del [examen práctico](examen.md), con otro dominio.

### La rúbrica

| | Bloque | Puntos |
|---|---|:-:|
| 1 | **Arquitectura por capas** | **2,5** |
| 2 | **El modelo y los DTOs** | 2,0 |
| 3 | API REST y códigos de estado | 2,0 |
| 4 | **Reglas de negocio y errores** | **2,5** |
| 5 | Uso correcto del código dado | 1,0 |
| 6 | Tests | 1,0 |

Se aprueba con **5,0**. Cada ítem se comprueba con un comando o una petición concreta: la rúbrica completa, con las penalizaciones, está en [Examen práctico](examen.md).

!!! danger "El bloque 1 no puede quedar en 0"
    El RA5 es literalmente *«separar el código de presentación de la lógica de negocio»*. Un proyecto que funciona con la lógica en el controlador **no demuestra el resultado de aprendizaje**, por bien que responda.

    Los dos comandos que lo deciden:

    ```bash
    grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/     # vacío
    grep -rni "SELECT\|INSERT\|UPDATE" src/main/java/**/controllers/     # vacío
    ```

---

## Antes de empezar

- [ ] JDK 25 y Maven (`java -version`, `mvn -version`).
- [ ] IntelliJ IDEA con el plugin de Spring, o el Initializr web.
- [ ] `curl` y `jq`, o Postman.
