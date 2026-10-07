# UT5 — Spring Data con JPA y SQL

**20 h · 20 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA6:** Desarrolla aplicaciones web de acceso a almacenes de datos, aplicando medidas para mantener la seguridad y la integridad de la información.

En la UT3 hablaste con la base de datos **a mano**: `DriverManager`, `PreparedStatement`, `ResultSet`, mapeo fila a fila. En la UT4 montaste las capas, pero el repositorio guardaba en un `HashMap` y los datos se perdían al parar.

Aquí se juntan las dos cosas: **la misma API de la UT4, con persistencia de verdad**, y escrita en una fracción del código de la UT3.

!!! quote "Autoría del material de los temas 1 y 2"
    Los dos primeros temas son una **adaptación del material de [José Luis González Sánchez](https://github.com/joseluisgs)** (repositorio [DesarrolloWebEntornosServidor-02-2025-2026](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-02-2025-2026), licencia [CC BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/)), con el texto, los diagramas y los ejemplos originales.

    Las adaptaciones de este curso: **Maven en vez de Gradle**, **Java 25**, y los apartados de proyecto, referencias, retos y ejercicios. Cada tema lleva su nota de autoría arriba.

!!! success "La promesa de la UT4, cobrada"
    Lo primero que vas a hacer es cambiar `FunkosRepositoryImpl` por una interfaz de tres líneas. Y comprobar que el **controlador, el servicio, los DTOs, el mapeador, las excepciones y los tests de capa web no se tocan**.

    | | Líneas del repositorio |
    |---|:-:|
    | UT3 · JDBC a mano | ~120 |
    | UT4 · `ConcurrentHashMap` | ~45 |
    | **UT5 · Spring Data JPA** | **0** |

!!! danger "Lo que de verdad se evalúa aquí no es escribir código"
    Es **tomar bien cuatro decisiones de modelo de datos**: dónde va la clave ajena, qué se carga en `LAZY`, qué se borra en cascada y qué no, y qué consulta evita el N+1.

    Los cuatro errores correspondientes **compilan y arrancan**, y solo se notan cuando la tabla tiene 10.000 filas o hay dos usuarios a la vez. Por eso están en la rúbrica y por eso se comprueban con `show-sql=true` en la defensa.

---

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**.

- [ ] Configurar **H2 en desarrollo y MySQL en producción** con el mismo `.jar`, por perfiles.
- [ ] Mapear una **entidad** con `@Entity`, `@Column`, identificadores y marcas temporales.
- [ ] Modelar las **tres relaciones** (1:1, 1:N, N:M) sabiendo en qué tabla está la clave ajena.
- [ ] Decidir `LAZY` o `EAGER`, `cascade` y `orphanRemoval`, y **justificar cada elección**.
- [ ] Escribir repositorios con **consultas derivadas del nombre**, `@Query` con JPQL y `Specification`.
- [ ] **Paginar y ordenar** con tope de tamaño y lista blanca de campos.
- [ ] Implementar **borrado lógico** consistente en toda la API.
- [ ] Usar **`@Transactional`** y saber cuándo no hace rollback.
- [ ] Detectar y arreglar un **N+1**, un `LazyInitializationException` y una recursión infinita.
- [ ] Escribir los **tres niveles de test**: `@DataJpaTest`, Mockito y `@SpringBootTest`.

## Los temas

| | | |
|:-:|---|---|
| **1** | [Spring Data con JPA y SQL](01-spring-data-jpa-sql.md) | Conexión, entidades, relaciones, repositorios, consultas y testing. **Es la referencia de la unidad** |
| **2** | [Resultados avanzados](02-resultados-avanzados.md) | Paginación, ordenación, criterios de búsqueda y negociación de contenido |
| **3** | [Proyecto completo paso a paso](03-proyecto-completo.md) | La API de Funkos con dos entidades, H2 y MySQL en Docker, en 12 pasos |
| **4** | [Referencia de JPA y validaciones](04-referencias.md) | Todas las anotaciones con sus valores por defecto y sus trampas. Página de consulta |

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Qué es Spring Data · conexión · `ddl-auto` · datos iniciales | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.1–1.2 |
| **S2** | **Entidades**: `@Entity`, `@Column`, identificadores, marcas temporales | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.3 |
| **S3** | **Relaciones** 1:1, 1:N, N:M y embebidas | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.4.1–1.4.5 |
| **S4** | Cascada, `orphanRemoval`, **`LAZY` frente a `EAGER`** | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.4.6–1.4.8 |
| **S5** | Borrado físico y lógico · **la recursión infinita** y por qué existen los DTOs | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.4.9–1.4.10 |
| **S6** | **Repositorios**: consultas derivadas, `@Query`, JPQL y SQL nativo | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.5 |
| **S7** | Testing del repositorio: `@DataJpaTest` y `TestEntityManager` | [1. JPA y SQL](01-spring-data-jpa-sql.md) §1.6 |
| **S8** | **Paginación y ordenación**: `Page`, `Slice`, `Pageable` | [2. Resultados avanzados](02-resultados-avanzados.md) §2.1 |
| **S9** | **Criterios de búsqueda** con `Specification` · negociación de contenido | [2. Resultados avanzados](02-resultados-avanzados.md) §2.2 |
| **S10** | **Proyecto**: pasos 1 a 5 (configuración, entidades, repositorios) | [3. Proyecto](03-proyecto-completo.md) pasos 1–5 |
| **S11** | **Proyecto**: pasos 6 a 9 (borrado lógico, transacciones, controlador) | [3. Proyecto](03-proyecto-completo.md) pasos 6–9 |
| **S12** | **Proyecto**: pasos 10 a 12 (MySQL en Docker, tests, pruebas) | [3. Proyecto](03-proyecto-completo.md) pasos 10–12 |
| **S13** | Laboratorio: cazar un N+1 y un `LazyInitializationException` reales | [Batería](ejercicios.md) bloque 4 |
| **S14** | :material-check-circle: **Reto 1 · Videoclub**, resuelto en clase (1.ª parte) | [Reto 1](retos.md) |
| **S15** | :material-check-circle: **Reto 1 · Videoclub**, resuelto en clase (2.ª parte) | [Reto 1](retos.md) |
| **S16** | :material-check-circle: **Reto 2 · Instituto**, con huecos: la N:M con datos propios | [Reto 2](retos.md) |
| **S17** | :material-upload: Laboratorio del **reto 3**: modelo de datos y repositorios | [Reto 3](retos.md) |
| **S18** | :material-upload: Laboratorio del **reto 3**: reglas de negocio y tests | [Reto 3](retos.md) |
| **S19** | :material-upload: Defensa del **reto 3** · repaso de la rúbrica | [Reto 3](retos.md) |
| **S20** | :material-laptop: **Examen práctico de RA6 (100 %)** | [Chuleta](chuleta.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, en la S20: desarrollo en el ordenador a partir de un enunciado y un esqueleto con el `pom.xml` y la configuración ya puestos.

Se valora, por criterios: el **modelo de datos**, que las relaciones sean **`LAZY`** y no haya **N+1**, los **repositorios** (derivadas, `@Query` y `Specification`), las **reglas de negocio** con `@Transactional`, la **paginación** saneada, el **borrado lógico**, los **DTOs** y los **tests**.

El **[reto 3](retos.md)** es un enunciado del mismo tamaño y con **la misma rúbrica**.

!!! success "La rúbrica, resumida"
    | | Criterio | Puntos |
    |---|---|:-:|
    | 1 | Modelo de datos y relaciones correctas | 2,0 |
    | 2 | `LAZY` en todas las relaciones y sin N+1 | 1,5 |
    | 3 | Repositorios: derivadas, `@Query` y `Specification` | 1,5 |
    | 4 | Reglas de negocio con `@Transactional` y 409 | 1,5 |
    | 5 | Paginación con tope y lista blanca | 1,0 |
    | 6 | Borrado lógico consistente | 0,5 |
    | 7 | DTOs, sin `LazyInitializationException` | 1,0 |
    | 8 | Los tres niveles de test | 1,0 |
    | 9 | Perfiles y Docker, los datos persisten | 0,5 |

    Está completa, con los descuentos, en [Retos](retos.md).

!!! danger "En la defensa se mira el log"
    Parte de la nota se decide con `show-sql=true` delante: se pide un listado y **se cuentan las consultas**. Un N+1 se ve ahí en dos segundos, y no hay forma de que un programa que funciona lo delate de otra manera.

## Material

| | |
|---|---|
| [**Proyecto completo**](03-proyecto-completo.md) | La API de Funkos con dos entidades, H2, MySQL en Docker y los tres niveles de test |
| [**Retos**](retos.md) | Dos **resueltos** en clase y **un tercero que se entrega**, con tres entidades, a elegir entre tres dominios |
| [**Batería de ejercicios**](ejercicios.md) | 26 con solución, graduales y en el orden de los temas |
| [**Referencia de JPA**](04-referencias.md) | Todas las anotaciones, con valores por defecto y trampas |
| [Chuleta](chuleta.md) | Lo imprescindible y los diez errores del examen, en una página |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pégale tu código y te dice si aguanta la rúbrica |

## Antes de la S1

- [ ] El [proyecto de la UT4](../ut4/04-proyecto-completo.md) funcionando: es el punto de partida.
- [ ] **Docker Desktop instalado y arrancado** (`docker --version`). Se usa desde la S10.
- [ ] Repasa el [tema 3 de la UT3](../ut3/03-base-de-datos.md): el SQL que vas a ver en el log es ese.
- [ ] Repasa [`Optional`](../ut2/05-excepciones-y-optional.md): `findById` devuelve uno, y se usa en cada método.

!!! info "Y lo que viene después"
    ```mermaid
    graph LR
        UT3["UT3 · JDBC a mano<br/>~120 líneas"] --> UT4["UT4 · Capas<br/>HashMap"]
        UT4 --> UT5["UT5 · Spring Data JPA<br/>0 líneas de repositorio"]
        UT5 --> UT6["UT6 · WebSockets, GraphQL y OpenAPI<br/>el mismo servicio, otros transportes"]
    ```

    En la UT6 **no se toca la persistencia**: se añaden un WebSocket, un esquema GraphQL y la documentación OpenAPI, y los tres llaman a los servicios que escribes aquí.
