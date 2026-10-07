# UT4 — Spring Boot: capas, REST y servicios

**18 h · 18 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA5:** Desarrolla aplicaciones web, identificando y aplicando mecanismos para separar el código de presentación de la lógica de negocio.

Aquí cambia el juego. Dejas de escribir ficheros sueltos y empiezas a construir **aplicaciones de verdad** con el framework más usado del mundo Java. Todo lo que montaste a mano en la UT3 —repositorio, servicio, interfaces— lo hará Spring por ti, y ahora entenderás por qué.

!!! quote "Autoría del material de los temas 1, 2 y 3"
    Los tres primeros temas son una **adaptación del material de [José Luis González Sánchez](https://github.com/joseluisgs)** (repositorio [DesarrolloWebEntornosServidor-02-2025-2026](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-02-2025-2026), licencia [CC BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/)), con el texto, los diagramas y los ejemplos originales.

    Las adaptaciones de este curso: **Maven en vez de Gradle**, **Java 25 en vez de 17**, y los apartados de ejercicios, proyecto y retos del final. Cada tema lleva la nota de autoría arriba.

!!! note "Aquí todavía no hay base de datos"
    El repositorio guarda en memoria, así que al parar la aplicación los datos desaparecen. Es **deliberado**: esta unidad va de **arquitectura**, no de persistencia. Lo importante es que el servicio y el controlador no sepan **dónde** se guardan los datos, y por eso en la UT5 podrás cambiar memoria por base de datos sin tocarlos.

!!! danger "Primera unidad con examen práctico"
    Ya no hay test. La prueba consiste en **construir una aplicación en el ordenador** a partir de un enunciado, y se evalúa con **rúbrica por criterios**: la misma del [reto 3](retos.md).

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
| **4** | [Proyecto completo paso a paso](04-proyecto-completo.md) | Una API de Funkos entera, en 14 pasos, con Maven |
| **5** | [Configuración y variables de entorno](05-configuracion.md) | `application.properties`, perfiles, `.env`, `${VAR:defecto}` |

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Qué es Spring · autoconfiguración · starters · primer proyecto | [1. Spring Boot](01-introduccion-spring-boot.md) §1.1–1.2 |
| **S2** | Módulos de Spring · beans y sus *scopes* | [1. Spring Boot](01-introduccion-spring-boot.md) §1.3–1.4 |
| **S3** | IoC y DI: por constructor, por setter, por campo | [1. Spring Boot](01-introduccion-spring-boot.md) §1.5–1.6 |
| **S4** | Spring MVC · el punto de entrada · `application.properties` | [2. Spring Web REST](02-spring-web-rest.md) §2.1–2.2 |
| **S5** | Componentes y capas · `@RestController` y las rutas | [2. Spring Web REST](02-spring-web-rest.md) §2.3–2.4.3 |
| **S6** | `ResponseEntity`, `@PathVariable`, `@RequestParam`, `@RequestBody` | [2. Spring Web REST](02-spring-web-rest.md) §2.4.4–2.4.5 |
| **S7** | Versionado · Postman y `curl` · **mi primera API REST** | [2. Spring Web REST](02-spring-web-rest.md) §2.4.6–2.6 |
| **S8** | La capa de servicio · excepciones de dominio con `@ResponseStatus` | [3. Servicios](03-servicios-dtos-y-cache.md) §3.1–3.2 |
| **S9** | **DTOs** y mapeadores: no expongas tu dominio | [3. Servicios](03-servicios-dtos-y-cache.md) §3.4–3.5 |
| **S10** | Validación con Bean Validation y `@RestControllerAdvice` | [3. Servicios](03-servicios-dtos-y-cache.md) §3.6 |
| **S11** | **Caché**: `@Cacheable`, `@CacheEvict` y cuándo invalidarla | [3. Servicios](03-servicios-dtos-y-cache.md) §3.3 |
| **S12** | **Proyecto completo**: pasos 1 a 8 (modelo, repositorio, DTOs) | [4. Proyecto](04-proyecto-completo.md) pasos 1–8 |
| **S13** | **Proyecto completo**: pasos 9 a 14 (servicio, controlador, tests) | [4. Proyecto](04-proyecto-completo.md) pasos 9–14 |
| **S14** | Perfiles, variables de entorno y `.env` | [5. Configuración](05-configuracion.md) |
| **S15** | :material-check-circle: **Reto 1 · Biblioteca**, resuelto en clase | [Reto 1](retos.md) |
| **S16** | :material-check-circle: **Reto 2 · Gimnasio**, con huecos | [Reto 2](retos.md) |
| **S17** | :material-upload: Laboratorio del **reto 3** y defensa | [Reto 3](retos.md) |
| **S18** | :material-laptop: **Examen práctico de RA5 (100 %)** | [Chuleta](chuleta.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, en la S18: desarrollo en el ordenador a partir de un enunciado y un esqueleto de proyecto.

Se valora, por criterios: que **funcione**, que las **capas** estén bien separadas, la **inyección de dependencias** correcta, el uso de **DTOs y validación**, el **manejo de errores**, la **configuración** y los **tests**.

El **[reto 3](retos.md)** es un enunciado del mismo tamaño y con **la misma rúbrica**. Si lo sacas, el examen es el mismo ejercicio con otro dominio.

!!! success "La rúbrica, resumida"
    | | Criterio | Puntos |
    |---|---|:-:|
    | 1 | Capas separadas | 2,0 |
    | 2 | CRUD con los códigos correctos | 1,5 |
    | 3 | DTOs y validación | 1,5 |
    | 4 | Excepciones y `@RestControllerAdvice` | 1,5 |
    | 5 | Reglas de negocio en el servicio | 1,5 |
    | 6 | Tests de servicio y de capa web | 1,0 |
    | 7 | Configuración y perfiles | 0,5 |
    | 8 | PATCH que parchea, PUT que reemplaza | 0,5 |

    Está completa, con los descuentos, en [Retos](retos.md).

## Material

| | |
|---|---|
| [**Proyecto completo**](04-proyecto-completo.md) | Una API REST de Funkos de cero a `.jar`, en 14 pasos con el código entero |
| [**Retos**](retos.md) | Dos **resueltos** en clase y **un tercero que se entrega**, a elegir entre tres dominios |
| [**Batería de ejercicios**](ejercicios.md) | 24 con solución, graduales y en el orden de los temas |
| [Chuleta](chuleta.md) | Anotaciones, códigos de estado y los diez errores del examen, en una página |
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
