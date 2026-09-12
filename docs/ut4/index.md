# UT4 — Spring Boot: arquitectura por capas

**18 h · 18 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA5:** Desarrolla aplicaciones web, identificando y aplicando mecanismos para separar el código de presentación de la lógica de negocio.

Aquí cambia el juego. Dejas de escribir ficheros sueltos y empiezas a construir **aplicaciones de verdad** con el framework más usado del mundo Java. Todo lo que montaste a mano en la UT3 (repositorio, servicio, interfaces) lo hará Spring por ti — y ahora entenderás por qué.

!!! note "Aquí todavía no hay base de datos"
    El repositorio guarda en memoria, así que al parar la aplicación los datos desaparecen. Es **deliberado**: esta unidad va de **arquitectura**, no de persistencia. Lo importante es que el servicio y el controlador no sepan **dónde** se guardan los datos — y por eso, en la UT5, podrás cambiar memoria por base de datos sin tocarlos.

!!! danger "Primera unidad con examen práctico"
    Ya no hay test. La prueba consiste en **construir una aplicación en el ordenador** a partir de un enunciado. Se evalúa con [rúbrica por criterios](examen/). Traducción: hay que programar todos los días, no estudiar la última semana.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Crear un proyecto **Spring Boot** y explicar qué hace la autoconfiguración por debajo.
- [ ] Aplicar **inyección de dependencias por constructor** y razonar sobre el ciclo de vida de los *beans*.
- [ ] Construir un **CRUD completo en tres capas**: controlador, servicio y repositorio.
- [ ] Separar la entidad del **DTO** y validar la entrada con Bean Validation.
- [ ] Devolver **errores homogéneos** con `ProblemDetail` y configurar la aplicación por perfiles.
- [ ] Escribir **tests unitarios y de integración** de cada capa.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Repaso de capas · qué es Spring Boot y cómo arranca | [1. Spring Boot](01-spring-boot/) §1–2 |
| **S2** | Crear el proyecto con Initializr · estructura y primer endpoint | [1. Spring Boot](01-spring-boot/) §3–5 |
| **S3** | IoC: el contenedor y los *beans* | [2. Inyección de dependencias](02-inyeccion-dependencias/) §1–2 |
| **S4** | Inyección por constructor y estereotipos | [2. Inyección de dependencias](02-inyeccion-dependencias/) §3–4 |
| **S5** | Varias implementaciones: `@Qualifier`, `@Primary`, `@Profile` | [2. Inyección de dependencias](02-inyeccion-dependencias/) §5–6 |
| **S6** | Capa de dominio y repositorio | [3. Las capas en Spring](03-capas-en-spring/) §1–2 |
| **S7** | Capa de servicio: las reglas de negocio | [3. Las capas en Spring](03-capas-en-spring/) §3 |
| **S8** | Capa de presentación: el controlador | [3. Las capas en Spring](03-capas-en-spring/) §4 |
| **S9** | **DTO** y mapeadores: no expongas tu dominio | [4. DTO y validación](04-dto-y-validacion/) §1–2 |
| **S10** | Validación con Bean Validation | [4. DTO y validación](04-dto-y-validacion/) §3–4 |
| **S11** | Excepciones de dominio y `@RestControllerAdvice` | [5. Errores y configuración](05-errores-y-configuracion/) §1–2 |
| **S12** | Configuración, perfiles y `application.yml` | [5. Errores y configuración](05-errores-y-configuracion/) §3–4 |
| **S13** | Tests unitarios con Mockito | [6. Testing en Spring](06-testing-en-spring/) §1–2 |
| **S14** | Tests de rodaja y de integración | [6. Testing en Spring](06-testing-en-spring/) §3–5 |
| **S15** | Laboratorio: la batería sobre otro dominio (biblioteca) | [Batería de ejercicios](ejercicios/) |
| **S16** | Proyecto integrador: montar la aplicación completa | [Reto con tests](practicas/) |
| **S17** | Repaso, dudas y laboratorio libre | [Preparar el examen](examen/) |
| **S18** | :material-laptop: **Examen práctico de RA5 (100 %)** | [Preparar el examen](examen/) |

## Cómo se evalúa: examen práctico

Desarrollo en el ordenador a partir de un enunciado y un esqueleto de proyecto. Se valora, por criterios: que **funcione**, que las **capas** estén bien separadas, la **inyección de dependencias** correcta, el uso de **DTO y validación**, el **manejo de errores** y los **tests**.

Enunciado de ejemplo, rúbrica completa y consejos: **[preparación del examen](examen/)**.

## Material

- Reto con tests
    — construimos una aplicación completa, sesión a sesión.
- Batería de ejercicios — 12 ejercicios resueltos sobre otro dominio (una biblioteca), para comprobar que lo sabes hacer tú y no solo copiar.
- [Comprobar tu trabajo](../comprobar-tu-trabajo/) — los tests del reto de tu unidad.
    — pégale tu código y te dice si tus capas aguantan.
- Chuleta de Spring
    — anotaciones y estructura en una página.

## Antes de la S1

- [ ] JDK 25 y Maven funcionando (`mvn -version`).
- [ ] IntelliJ IDEA con el plugin de Spring (viene en Community vía Initializr web).
- [ ] Repasa la página de capas de la UT3: es el andamio de todo lo que viene.
