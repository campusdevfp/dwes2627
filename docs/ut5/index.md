# UT5 — Spring Data con JPA y SQL

**28 h · 28 sesiones · Trimestre 1.º** · **RA6** · Evaluación: :material-laptop: **examen práctico (100 %)**

> Desarrolla aplicaciones web de acceso a almacenes de datos, aplicando medidas para mantener la seguridad y la integridad de la información.

---

## Temas

| | | Qué se aprende |
|:-:|---|---|
| **1** | [Spring Data y Spring Data JPA](01-spring-data-jpa-sql.md) | Conexión, entidades, identificadores, relaciones, `LAZY`, repositorios, JPQL y testing |
| **2** | [Negociación de contenido y paginación](02-resultados-avanzados.md) | `Page`, `Pageable`, `Sort`, enlaces de paginación y criterios con `Specification` |
| **3** | [Referencia de Spring Data JPA](03-referencias.md) | **Para consultar**: JPA, Spring Data, Bean Validation y `@Transactional`, anotación a anotación |
| **4** | [Dos proyectos paso a paso](04-proyectos.md) | Los **dos de la UT4 migrados a Spring Data JPA**, con **paginación**, y H2 o PostgreSQL |

| | |
|---|---|
| [**Examen práctico**](examen.md) | Tres enunciados del formato del examen, con la rúbrica |
| [**Chuleta**](chuleta.md) | Referencia en una página · **[PDF para el examen](../pdf/chuleta-ut5.pdf)** |

!!! info "Cómo se usan los tres primeros temas"
    | | |
    |---|---|
    | **Temas 1 y 2** | La teoría, con los ejemplos del material de referencia del curso |
    | **Tema 3** | El diccionario de anotaciones, **para tenerlo abierto mientras programas** |
    | **Tema 4** | Donde se escribe de verdad: los dos proyectos, paso a paso |

!!! success "Lo primero que se hace en la unidad"
    Borrar `FunkosRepositoryJdbc` y `FunkoRowMapper` de la [UT4](../ut4/05-proyectos.md) y sustituirlos por una interfaz **sin cuerpo**. Y comprobar que el controlador, el servicio, los DTOs y los tests de capa web **no se tocan**.

    | | Líneas del repositorio |
    |---|:-:|
    | UT3 · JDBC a mano | ~120 |
    | UT4 · `JdbcClient` + `RowMapper` | ~95 |
    | **UT5 · Spring Data JPA** | **0** |

---

## Cómo se evalúa

:material-laptop: **Examen práctico, 100 % de la nota de la unidad.** Cuatro horas, con el ordenador y **la [chuleta](../pdf/chuleta-ut5.pdf) impresa** como única documentación.

**Se te da** el proyecto que arranca (`pom.xml`, los `application*.properties`, el `data.sql` y la clase de arranque), **los mapeadores completos**, un **`GlobalExceptionHandler` a medias** y una **colección de Bruno** empezada.

**Escribes tú** las entidades, los DTOs, los repositorios JPA, las excepciones, el servicio, los controladores **con paginación**, los mensajes y el manejador que faltan en el handler, y las pruebas de Bruno que pide el enunciado.

!!! success "Los mapeadores dados son la especificación de tus entidades"
    De ellos salen los constructores, los `getters`, los `setters` que hacen falta y **qué relaciones hay y en qué dirección**. Léelos antes de escribir nada: si los modificas para que encajen con tus clases, hay **−1,5** de penalización.

!!! warning "No hay `schema.sql`"
    El esquema lo genera Hibernate desde tus entidades, así que **el modelo de datos lo diseñas tú** y es lo primero que se corrige.

### La rúbrica

| | Bloque | Puntos |
|---|---|:-:|
| 1 | **Las entidades y el mapeo** | **2,0** |
| 2 | **Paginación** | 1,5 |
| 3 | **`LAZY` y el N+1** | 1,5 |
| 4 | Repositorios y consultas | 1,5 |
| 5 | Capas, DTOs y validación | 1,5 |
| 6 | **Integridad y transacciones** | 1,5 |
| 7 | El `GlobalExceptionHandler` | 0,5 |
| 8 | **Pruebas con Bruno** | 1,0 |
| 9 | Configuración y persistencia | 1,0 |

Se aprueba con **5,0**. Suman 12 puntos de criterios sobre un máximo de 10, lo que permite compensar un bloque flojo. La rúbrica completa está en [Examen práctico](examen.md).

!!! info "Las pruebas se entregan con Bruno"
    No se piden tests de JUnit: se entrega una **colección de [Bruno](https://www.usebruno.com/)** —ficheros `.bru` que viajan con el proyecto— con las peticiones y comprobaciones que diga el enunciado. Se corrige ejecutando `bru run --env local` con tu API levantada.

!!! danger "Dos ítems no se pueden corregir leyendo el código"
    **El N+1 (1,0 pt)** se corrige con `show-sql=true`, pidiendo un listado paginado y **contando las líneas `Hibernate:`**:

    | Consultas | Nota |
    |---|:-:|
    | 2 | **1,0** |
    | 3–4 | 0,5 |
    | 1 + tamaño de página | **0** |

    **El *rollback* (0,75 pts)** se corrige apuntando el estado de las filas implicadas, provocando el fallo de la operación transaccional y volviendo a mirar: **los dos listados tienen que ser idénticos**.

    **Y el cambio de motor (0,3 pts)** arrancando con `SPRING_PROFILES_ACTIVE=postgres` y comprobando que `git diff src/main/java/` sale vacío.

    Un programa con N+1 o sin *rollback* funciona perfectamente y pasa cualquier prueba manual.

**Mínimos:** el bloque 1 no puede quedar en 0, y tampoco el ítem de `LAZY`.

---

## Antes de empezar

- [ ] Uno de los [proyectos de la UT4](../ut4/05-proyectos.md) funcionando: es el punto de partida literal.
- [ ] Repasa el [tema 4 de la UT4](../ut4/04-configuracion.md): los perfiles se dan por sabidos.
- [ ] Repasa [`Optional`](../ut2/05-excepciones-y-optional.md): `findById` devuelve uno.
- [ ] **Docker funcionando**, para la parte de PostgreSQL: `docker compose version`.
- [ ] **Bruno instalado**, y la línea de comandos: `npm install -g @usebruno/cli` y `bru --version`.
