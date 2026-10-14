# UT5 — Spring Data con JPA y SQL

**20 h · 20 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA6:** Desarrolla aplicaciones web de acceso a almacenes de datos, aplicando medidas para mantener la seguridad y la integridad de la información.

En la UT3 hablaste con la base de datos **a mano**: `DriverManager`, `PreparedStatement`, `ResultSet`, mapeo fila a fila. En la UT4 montaste las capas y escribiste el SQL tú, con `JdbcClient` y un `RowMapper`.

Aquí desaparece el SQL: **las mismas dos APIs de la UT4**, con las consultas generadas por Spring Data y el esquema generado desde tus entidades.

!!! quote "Autoría del material de los temas 1 y 2"
    Los dos primeros temas son una **adaptación del material de [José Luis González Sánchez](https://github.com/joseluisgs)** (repositorio [DesarrolloWebEntornosServidor-02-2025-2026](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-02-2025-2026), licencia [CC BY-NC-SA 4.0](http://creativecommons.org/licenses/by-nc-sa/4.0/)), con el texto, los diagramas y los ejemplos originales.

    Las adaptaciones de este curso: **Maven en vez de Gradle**, **Java 25**, y los apartados de proyecto, referencias, retos y ejercicios. Cada tema lleva su nota de autoría arriba.

!!! success "La promesa de la UT4, cobrada"
    Lo primero que vas a hacer es borrar `FunkosRepositoryJdbc` y `FunkoRowMapper` y sustituirlos por una interfaz sin cuerpo. Y comprobar que el **controlador, el servicio, los DTOs, el mapeador, las excepciones y los tests de capa web no se tocan**.

    | | Líneas del repositorio |
    |---|:-:|
    | UT3 · JDBC a mano | ~120 |
    | UT4 · `JdbcClient` + `RowMapper` | ~95 |
    | **UT5 · Spring Data JPA** | **0** |

!!! danger "Lo que de verdad se evalúa aquí no es escribir código"
    Es **tomar bien cuatro decisiones de modelo de datos**: dónde va la clave ajena, qué se carga en `LAZY`, qué se borra en cascada y qué no, y qué consulta evita el N+1.

    Los cuatro errores correspondientes **compilan y arrancan**, y solo se notan cuando la tabla tiene 10.000 filas o hay dos usuarios a la vez. Por eso están en la rúbrica y por eso se comprueban con `show-sql=true` en la defensa.

---

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**.

- [ ] Configurar **H2 en fichero** y tres perfiles (`dev`, `prod`, `test`) con el mismo `.jar`.
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
| **1** | [Spring Data con JPA y SQL](01-spring-data-jpa-sql.md) | Conexión, entidades, relaciones, repositorios, consultas y testing. **Es la teoría de la unidad** |
| **2** | [Resultados avanzados](02-resultados-avanzados.md) | Paginación, ordenación, criterios de búsqueda y negociación de contenido |
| **3** | [Referencia de JPA y validaciones](03-referencias.md) | Todas las anotaciones con sus valores por defecto y sus trampas. **Página de consulta** |
| **4** | [Dos proyectos paso a paso](04-proyectos.md) | Los **dos proyectos de la UT4** migrados a JPA: funkos (2 entidades) y tienda (4), con H2 en fichero |

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
| **S10** | **Proyecto A**: migrar el repositorio de la UT4 · entidades y relación 1:N | [4. Proyectos](04-proyectos.md) A.1–A.3 |
| **S11** | **Proyecto A**: borrado lógico, paginación y `Specification` | [4. Proyectos](04-proyectos.md) A.4–A.6 |
| **S12** | **Proyecto B**: cuatro entidades, cascada y `orphanRemoval` | [4. Proyectos](04-proyectos.md) B.1 |
| **S13** | **Proyecto B**: la transacción, el N+1 y `@Version` | [4. Proyectos](04-proyectos.md) B.2–B.3 |
| **S14** | **Proyecto B**: los tres niveles de test | [4. Proyectos](04-proyectos.md) B.4 |
| **S15** | Laboratorio: cazar un N+1 y un `LazyInitializationException` reales | [3. Referencias](03-referencias.md) §3.4 |
| **S16** | :material-pencil: **EP1 · Biblioteca**: el modelo de datos y los repositorios | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S17** | :material-pencil: **EP1 · Biblioteca**: reglas de negocio y tests | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S18** | :material-pencil: Laboratorio: **EP2 o EP3** a elección | [Ejercicios prácticos](ejercicios-practicos.md) |
| **S19** | Repaso de la rúbrica · los cuatro `grep` y el recuento de consultas | [Chuleta](chuleta.md) |
| **S20** | :material-laptop: **Examen práctico de RA6 (100 %)** | [Chuleta](chuleta.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, en la S20: desarrollo en el ordenador a partir de un enunciado y un esqueleto con el `pom.xml` y la configuración ya puestos.

Se valora, por criterios: el **modelo de datos**, que las relaciones sean **`LAZY`** y no haya **N+1**, los **repositorios** (derivadas, `@Query` y `Specification`), las **reglas de negocio** con `@Transactional`, la **paginación** saneada, el **borrado lógico**, los **DTOs** y los **tests**.

Los **[tres ejercicios prácticos](ejercicios-practicos.md)** son enunciados del mismo tamaño y con **la misma rúbrica**.

!!! success "La rúbrica, resumida"
    | | Bloque | Puntos |
    |---|---|:-:|
    | 1 | **Modelo de datos y relaciones** | **2,5** |
    | 2 | **`LAZY` y N+1** | **2,0** |
    | 3 | Repositorios y consultas | 1,5 |
    | 4 | **Validaciones en tres niveles** | **2,0** |
    | 5 | **Integridad y transacciones** | **2,0** |
    | 6 | Tests | 1,0 |
    | 7 | Configuración y persistencia | 1,0 |

    Está completa, con los ítems comprobables uno a uno y las penalizaciones, en [Ejercicios prácticos](ejercicios-practicos.md).

    **Dos mínimos:** el bloque 1 no puede quedar en 0, y tampoco el ítem 2.1 (`EAGER` por todas partes).

!!! danger "Parte de la nota se corrige con el log en pantalla"
    No hay defensa oral. Pero el **ítem 2.2 de la rúbrica (0,8 puntos)** se comprueba poniendo `show-sql=true`, pidiendo un listado paginado y **contando las líneas `Hibernate:`**.

    Con `JOIN FETCH` o `@EntityGraph`: **2 consultas**. Sin ellos: **1 + tamaño de página**.

    Un programa con N+1 funciona perfectamente y pasa cualquier prueba manual. **No hay otra forma de detectarlo.**

## Material

| | |
|---|---|
| [**Dos proyectos paso a paso**](04-proyectos.md) | Los dos de la UT4 migrados a JPA, con el código entero y H2 en fichero |
| [**Ejercicios prácticos**](ejercicios-practicos.md) | **Tres enunciados del formato del examen**, con la rúbrica de calificación |
| [**Referencia de JPA**](03-referencias.md) | Todas las anotaciones, con valores por defecto y trampas |
| [**Chuleta**](chuleta.md) | Lo imprescindible y los 10 errores del examen · **[descargable en PDF](../pdf/chuleta-ut5.pdf)** |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pégale tu código y te dice si aguanta la rúbrica |

## Antes de la S1

- [ ] **Los dos proyectos de la [UT4](../ut4/05-proyectos.md)** funcionando: son el punto de partida literal de los de aquí.
- [ ] Repasa el [tema 4 de la UT4](../ut4/04-configuracion.md): los perfiles y el `.env` se dan por sabidos.
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
