# UT5 — Spring Data con JPA y SQL

**20 h · 20 sesiones · Trimestre 1.º** · **RA6** · Evaluación: :material-laptop: **examen práctico (100 %)**

> Desarrolla aplicaciones web de acceso a almacenes de datos, aplicando medidas para mantener la seguridad y la integridad de la información.

---

## Temas

| | | Qué se aprende |
|:-:|---|---|
| **1** | [Spring Data con JPA y SQL](01-spring-data-jpa-sql.md) | Entidades, identificadores, relaciones, `LAZY`, cascada, repositorios, JPQL, testing |
| **2** | [Resultados avanzados](02-resultados-avanzados.md) | Paginación, ordenación, `Specification` y negociación de contenido |
| **3** | [Referencia de JPA y validaciones](03-referencias.md) | Todas las anotaciones con sus valores por defecto y sus trampas |
| **4** | [Dos proyectos paso a paso](04-proyectos.md) | Los **dos de la UT4 migrados a JPA**: funkos (2 entidades) y tienda (4), con H2 en fichero |

| | |
|---|---|
| [**Ejercicios prácticos**](ejercicios-practicos.md) | Tres enunciados del formato del examen, con la rúbrica |
| [**Chuleta**](chuleta.md) | Referencia en una página · **[PDF para el examen](../pdf/chuleta-ut5.pdf)** |

!!! info "El tema 3 es de consulta, no de estudio"
    La teoría está en el tema 1. El tema 3 es la lista completa de anotaciones **para tenerla abierta mientras programas**.

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

**Se te da** el `pom.xml`, los `application*.properties` de los tres perfiles, el `data.sql` y la clase de arranque. **Escribes tú** las entidades, los DTOs, los repositorios, los mapeadores, las excepciones, los servicios, los controladores, el manejador global y los tests.

!!! warning "No hay `schema.sql`"
    El esquema lo genera Hibernate desde tus entidades, así que **el modelo de datos lo diseñas tú** y es lo primero que se corrige.

### La rúbrica

| | Bloque | Puntos |
|---|---|:-:|
| 1 | **Modelo de datos y relaciones** | **2,5** |
| 2 | **`LAZY` y el N+1** | **2,0** |
| 3 | Repositorios y consultas | 1,5 |
| 4 | **Validaciones en tres niveles** | **2,0** |
| 5 | **Integridad y transacciones** | **2,0** |
| 6 | Tests | 1,0 |
| 7 | Configuración y persistencia | 1,0 |

Se aprueba con **5,0**. Suman 12 puntos de criterios sobre un máximo de 10, lo que permite compensar un bloque flojo. La rúbrica completa está en [Ejercicios prácticos](ejercicios-practicos.md).

!!! danger "Dos ítems no se pueden corregir leyendo el código"
    **El N+1 (0,8 pts)** se corrige con `show-sql=true`, pidiendo un listado paginado y **contando las líneas `Hibernate:`**:

    | Consultas | Nota |
    |---|:-:|
    | 2 | **0,8** |
    | 1 + tamaño de página | **0** |

    **El *rollback* (0,6 pts)** se corrige apuntando el estado de las filas implicadas, provocando el fallo de la operación transaccional y volviendo a mirar: **los dos listados tienen que ser idénticos**.

    Un programa con N+1 o sin *rollback* funciona perfectamente y pasa cualquier prueba manual.

**Mínimos:** el bloque 1 no puede quedar en 0, y tampoco el ítem de `LAZY`.

---

## Antes de empezar

- [ ] Uno de los [proyectos de la UT4](../ut4/05-proyectos.md) funcionando: es el punto de partida literal.
- [ ] Repasa el [tema 4 de la UT4](../ut4/04-configuracion.md): los perfiles se dan por sabidos.
- [ ] Repasa [`Optional`](../ut2/05-excepciones-y-optional.md): `findById` devuelve uno.
