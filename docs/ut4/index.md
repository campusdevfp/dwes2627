# UT4 — Spring Boot: capas, REST y servicios

**18 h · 18 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA5:** Desarrolla aplicaciones web, identificando y aplicando mecanismos para separar el código de presentación de la lógica de negocio.

Aquí cambia el juego. Dejas de escribir ficheros sueltos y empiezas a construir **aplicaciones de verdad** con el framework más usado del mundo Java. Todo lo que montaste a mano en la UT3 —repositorio, servicio, interfaces— lo hará Spring por ti, y ahora entenderás por qué.

!!! quote "Autoría del material de los temas 1, 2 y 3"
    Los tres primeros temas son una **adaptación del material de [José Luis González Sánchez](https://github.com/joseluisgs)** (repositorio [DesarrolloWebEntornosServidor-02-2025-2026](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-02-2025-2026), licencia [CC BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/)), con el texto, los diagramas y los ejemplos originales.

    Las adaptaciones de este curso: **Maven en vez de Gradle**, **Java 25 en vez de 17**, y los apartados de configuración, proyectos y ejercicios prácticos del final. Cada tema lleva la nota de autoría arriba.

!!! note "Aquí hay base de datos, pero NO hay JPA"
    Hablamos con **H2 a mano, por JDBC**, con el SQL que escribes tú: igual que en la [UT3](../ut3/03-base-de-datos.md), pero con Spring poniendo el pegamento (`DataSource`, pool de conexiones, `JdbcClient`, transacciones).

    **JPA, Hibernate, las entidades y los repositorios automáticos son la [UT5](../ut5/index.md).** Aquí lo que se evalúa es la **arquitectura**, y mezclar un ORM haría que atribuyeras a Hibernate problemas que son de diseño.

    Lo que ganas: cuando en la UT5 Spring Data te genere un `findByCategoriaIgnoreCase` sin escribir nada, sabrás exactamente qué SQL está sustituyendo.

!!! danger "Primera unidad con examen práctico"
    Ya no hay test. La prueba consiste en **construir una aplicación en el ordenador** a partir de un enunciado, y se evalúa con **rúbrica por criterios**: la misma de los [ejercicios prácticos](ejercicios-practicos.md).

    Traducción: hay que programar todos los días, no estudiar la última semana.

---

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Crear un proyecto **Spring Boot con Maven** y explicar qué hace la autoconfiguración por debajo.
- [ ] Aplicar **inyección de dependencias por constructor** y razonar sobre el *scope* de los *beans*.
- [ ] Construir un **CRUD REST completo en tres capas** con los códigos de estado correctos.
- [ ] Separar la entidad del **DTO** y validar la entrada con **Bean Validation**.
- [ ] Devolver **errores homogéneos** con excepciones propias y `@RestControllerAdvice`.
- [ ] Usar **caché** y saber cuándo invalidarla.
- [ ] Configurar la aplicación con **perfiles y variables de entorno**, sin secretos en el repositorio.
- [ ] Escribir **tests de servicio con Mockito** y **de capa web con `MockMvc`**.

## Los temas

| | | |
|:-:|---|---|
| **1** | [Introducción a Spring y Spring Boot](01-introduccion-spring-boot.md) | Qué es Spring, starters, beans, IoC y DI |
| **2** | [Spring Web REST](02-spring-web-rest.md) | `@RestController`, rutas, `ResponseEntity`, versionado |
| **3** | [Servicios, DTOs y caché](03-servicios-dtos-y-cache.md) | `@Service`, excepciones, DTOs, mapeadores, validación, caché |
| **4** | [Configuración y variables de entorno](04-configuracion.md) | `application.properties`, perfiles, `.env` en IntelliJ, `${VAR:defecto}` |
| **5** | [Dos proyectos paso a paso](05-proyectos.md) | **Funkos** (1 tabla) y **tienda** (3 tablas), con H2 por JDBC |

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Qué es Spring · autoconfiguración · starters · primer proyecto | [1. Spring Boot](01-introduccion-spring-boot.md) §1.1–1.2 |
| **S2** | Módulos de Spring · beans y sus *scopes* | [1. Spring Boot](01-introduccion-spring-boot.md) §1.3–1.4 |
| **S3** | IoC y DI: por constructor, por setter, por campo | [1. Spring Boot](01-introduccion-spring-boot.md) §1.5–1.6 |
| **S4** | Spring MVC · el punto de entrada · `application.properties` | [2. Spring Web REST](02-spring-web-rest.md) §2.1–2.2 |
| **S5** | Componentes y capas · `@RestController` y las rutas | [2. Spring Web REST](02-spring-web-rest.md) §2.3–2.4.3 |
| **S6** | `ResponseEntity`, `@PathVariable`, `@RequestParam`, `@RequestBody` | [2. Spring Web REST](02-spring-web-rest.md) §2.4.4–2.4.5 |
| **S7** | Versionado · `curl` y Postman · **auditar un controlador** | [2. Spring Web REST](02-spring-web-rest.md) §2.4.6–2.6 |
| **S8** | La capa de servicio · excepciones de dominio con `@ResponseStatus` | [3. Servicios](03-servicios-dtos-y-cache.md) §3.1–3.2 |
| **S9** | **DTOs** y mapeadores: no expongas tu dominio | [3. Servicios](03-servicios-dtos-y-cache.md) §3.4–3.5 |
| **S10** | Validación con Bean Validation y `@RestControllerAdvice` | [3. Servicios](03-servicios-dtos-y-cache.md) §3.6–3.7 |
| **S11** | **Caché**: `@Cacheable`, `@CacheEvict` y cuándo invalidarla | [3. Servicios](03-servicios-dtos-y-cache.md) §3.3 |
| **S12** | **Configuración**: perfiles, variables de entorno y `.env` en IntelliJ | [4. Configuración](04-configuracion.md) |
| **S13** | **Proyecto A · Funkos**: H2 por JDBC, `JdbcClient` y `RowMapper` | [5. Proyectos](05-proyectos.md) A.1–A.4 |
| **S14** | **Proyecto A · Funkos**: servicio, controlador, errores y tests | [5. Proyectos](05-proyectos.md) A.5–A.8 |
| **S15** | **Proyecto B · Tienda**: tres tablas, claves ajenas y el esquema | [5. Proyectos](05-proyectos.md) B.1–B.3 |
| **S16** | **Proyecto B · Tienda**: la transacción que descuenta stock | [5. Proyectos](05-proyectos.md) B.4–B.6 |
| **S17** | :material-pencil: Laboratorio: uno de los **ejercicios prácticos** | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S18** | :material-laptop: **Examen práctico de RA5 (100 %)** | [Chuleta](chuleta.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, en la S18: desarrollo en el ordenador a partir de un enunciado y un esqueleto de proyecto.

Se valora, por criterios: que **funcione**, que las **capas** estén bien separadas, la **inyección de dependencias** correcta, el uso de **DTOs y validación**, el **manejo de errores**, la **configuración** y los **tests**.

Los **[tres ejercicios prácticos](ejercicios-practicos.md)** son enunciados del mismo tamaño y con **la misma rúbrica**. Si resuelves uno, el examen es el mismo ejercicio con otro dominio.

!!! success "La rúbrica, resumida"
    | | Bloque | Puntos |
    |---|---|:-:|
    | 1 | Arquitectura por capas | **2,5** |
    | 2 | API REST y códigos de estado | 2,0 |
    | 3 | DTOs y validación | 2,0 |
    | 4 | Reglas de negocio y errores | **2,5** |
    | 5 | Persistencia con JDBC | 1,0 |
    | 6 | Tests | 1,0 |

    Está completa, con los ítems comprobables uno a uno y las penalizaciones, en [Ejercicios prácticos](ejercicios-practicos.md#rubrica-de-calificacion).

    **El bloque 1 no puede quedar en 0:** un proyecto que funciona con la lógica en el controlador no demuestra el RA5.

## Material

| | |
|---|---|
| [**Dos proyectos paso a paso**](05-proyectos.md) | La API de Funkos y la de una tienda, con el código entero y H2 por JDBC |
| [**Ejercicios prácticos**](ejercicios-practicos.md) | **Tres enunciados del formato del examen**, con la rúbrica de calificación |
| [**Chuleta**](chuleta.md) | Lo imprescindible y los 15 errores del examen · **[descargable en PDF](../pdf/chuleta-ut4.pdf)** |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pégale tu código y te dice si aguanta la rúbrica |

## Antes de la S1

- [ ] JDK 25 y Maven funcionando (`java -version`, `mvn -version`).
- [ ] IntelliJ IDEA con el plugin de Spring (en Community, vía el Initializr web).
- [ ] Postman instalado, o `curl` y `jq` en la terminal.
- [ ] Repasa el ejemplo largo de interfaces de la [UT2, tema 3](../ut2/03-poo-en-java.md): es el andamio de todo lo que viene.

!!! info "Y lo que viene después"
    ```mermaid
    graph LR
        UT4["UT4 · Capas con Spring Boot<br/>repositorio en memoria"] --> UT5["UT5 · Spring Data JPA<br/>la MISMA API, con BD"]
        UT5 --> UT6["UT6 · WebSockets y GraphQL<br/>el MISMO servicio, otros transportes"]
    ```

    En la UT5 se cambia **solo el repositorio**. En la UT6, **solo el transporte**. Si las capas están bien separadas, las dos unidades son media sesión de fontanería y el resto, materia nueva.
