# UT5 — Acceso a datos con JPA

**20 h · 20 sesiones · Trimestre 1.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA6:** Desarrolla aplicaciones web de acceso a almacenes de datos, aplicando medidas para mantener la seguridad y la integridad de la información.

Tu aplicación de la UT4 tiene las capas bien puestas… y pierde todos los datos al apagarla. El repositorio guarda en un `Map` en memoria. Aquí le pones **una base de datos de verdad** debajo, y descubres que ese trabajo de arquitectura que hiciste ahora te sale gratis: el servicio y el controlador **no se tocan**.

!!! success "La prueba de que la UT4 valió la pena"
    En la **S11** borras `ProductoRepositorioMemoria`, enchufas `JpaRepository` y ejecutas `mvn test`. Si separaste bien las capas, cambias una clase y unos nombres. Si no, reescribes medio proyecto.

    Y el repositorio en memoria no se tira: se queda como **doble de test**, que es su papel real en cualquier empresa.

## Lo que la memoria no te deja hacer

Con un `Map` guardando objetos en RAM, estos siete problemas no tienen solución. Cada bloque de la unidad desactiva uno:

| Problema | Cuándo aparece |
|---|---|
| Los datos desaparecen al reiniciar | La primera vez que cierras la aplicación |
| No puedes modelar un pedido con líneas | En cuanto el dominio crece |
| Listar 100 pedidos lanza 101 consultas | Cuando hay datos de verdad |
| Una venta que falla a mitad deja el stock descontado | Al primer error en producción |
| Dos peticiones simultáneas crean el mismo producto | Con dos usuarios a la vez |
| Dos ediciones concurrentes pierden una, sin error | Y no te enteras hasta que el inventario no cuadra |
| `ddl-auto: update` no sabe migrar datos | El día que cambias un campo con datos dentro |


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Explicar qué hace **JPA/Hibernate** por debajo y cuál es el ciclo de vida de una entidad.
- [ ] Mapear una **entidad** con sus tipos, claves, enumerados y restricciones sin caer en las trampas clásicas.
- [ ] Consultar con **Spring Data JPA**: consultas derivadas, `@Query`, JPQL y paginación real.
- [ ] Modelar **relaciones** entre entidades y resolver el problema **N+1**.
- [ ] Delimitar **transacciones** y proteger la integridad frente a accesos concurrentes.
- [ ] Pasar de H2 a **PostgreSQL** con **Flyway**, y probar contra el motor real.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Qué hace JPA por debajo · las cuatro siglas · JDBC a pelo e inyección SQL | [1. Qué hace JPA por debajo](01-que-hace-jpa-por-debajo.md) §1–3 |
| **S2** | El ciclo de vida de la entidad y el *dirty checking* | [1. Qué hace JPA por debajo](01-que-hace-jpa-por-debajo.md) §4 |
| **S3** | `@Entity`, `@Id` y generación de claves | [2. Entidades y mapeo](02-entidades-y-mapeo.md) §1–2 |
| **S4** | Tipos, `@Column`, enumerados y fechas | [2. Entidades y mapeo](02-entidades-y-mapeo.md) §3 |
| **S5** | Por qué una entidad **no** puede ser un `record` | [2. Entidades y mapeo](02-entidades-y-mapeo.md) §4–5 |
| **S6** | `JpaRepository`: el CRUD que no escribes · consultas derivadas | [3. Spring Data JPA](03-spring-data-jpa.md) §1–3 |
| **S7** | `@Query`, JPQL y consultas nativas | [3. Spring Data JPA](03-spring-data-jpa.md) §4 |
| **S8** | Paginación **de verdad** y proyecciones | [3. Spring Data JPA](03-spring-data-jpa.md) §5–6 |
| **S9** | Migrar el proyecto: fuera el repositorio en memoria | — |
| **S10** | `@ManyToOne` y `@OneToMany` · el lado dueño y `mappedBy` | [4. Relaciones](04-relaciones.md) §1–3 |
| **S11** | `LAZY` vs `EAGER` y el problema **N+1** | [4. Relaciones](04-relaciones.md) §4 |
| **S12** | `JOIN FETCH`, `@EntityGraph` y `@ManyToMany` | [4. Relaciones](04-relaciones.md) §5–6 |
| **S13** | `@Transactional`: qué es una transacción · *rollback* y propagación | [5. Transacciones](05-transacciones.md) §1–3 |
| **S14** | Integridad, restricciones y unicidad | [5. Transacciones](05-transacciones.md) §4 |
| **S15** | Concurrencia y bloqueo optimista con `@Version` | [5. Transacciones](05-transacciones.md) §5 |
| **S16** | `ddl-auto` no vale para producción: **Flyway** | [6. De H2 a producción](06-produccion.md) §1–2 |
| **S17** | PostgreSQL con Docker y perfiles · tests con `@DataJpaTest` | [6. De H2 a producción](06-produccion.md) §3–4 |
| **S18** | Laboratorio: la escuela de música de principio a fin | [Batería de ejercicios](ejercicios.md) |
| **S19** | Repaso: las seis trampas y cómo se ven en el *log* | [Preparar el examen](examen.md) |
| **S20** | :material-laptop: **Examen práctico de RA6 (100 %)** | [Preparar el examen](examen.md) |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, 2 sesiones. Se te da un esqueleto con el modelo y un fichero de datos, y tienes que montar la persistencia completa: entidades, relaciones, repositorios, consultas, transacciones y tests. Rúbrica en la [página de preparación](examen.md).

## Material

- [Prácticas guiadas](practicas.md) — migramos la TiendaAPI a base de datos, paso a paso.
- [Batería de ejercicios](ejercicios.md) — 14 ejercicios resueltos, del mapeo al bloqueo optimista.
- [Preparar el examen](examen.md) — formato, rúbrica y los errores que más cuestan.
- [Chuleta de JPA](chuleta.md) — anotaciones y consultas en una página.
- [Comprobar tu trabajo](../comprobar-tu-trabajo.md) — los tests del proyecto `spring-base`.

## Antes de la S1

Instala **Docker Desktop** y comprueba que arranca:

```bash
docker run --rm hello-world
```

Lo vas a necesitar en la S22 para levantar PostgreSQL. Hasta entonces usaremos H2, que no necesita nada.
