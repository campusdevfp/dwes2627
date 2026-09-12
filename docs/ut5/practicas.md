# Reto de la unidad — UT5

> **Reto `ut5-jpa-base`**, en GitHub Classroom. Se te entrega una aplicación por capas **que funciona con un repositorio en memoria**, y unos tests de persistencia en rojo. Tu trabajo es ponerle una base de datos debajo sin romper nada.
>
> El reto arranca desde una base que ya funciona, así que **no arrastras lo que te saliera mal en la UT4**.

Debajo, las **fases sugeridas**.

| Fase | Sesiones | Qué haces |
|---|---|---|
| **F1** | S1–S3 | H2 en marcha, primera entidad, JDBC a pelo una vez |
| **F2** | S4–S6 | El modelo pasa a entidad: mapeo, tipos y `equals` |
| **F3** | S7–S11 | `JpaRepository` y **borrar** el repositorio en memoria |
| **F4** | S12–S16 | Pedidos y líneas: relaciones y el N+1 |
| **F5** | S17–S20 | Transacciones, integridad y `@Version` |
| **F6** | S21–S23 | Flyway, PostgreSQL y tests |

---

## F1 — H2 y la primera entidad (S1–S3)

Añade las dependencias, configura H2 con consola y `show-sql`, convierte `Producto` en `@Entity` y arranca.

??? success "Comprobación"

    En <code>/h2-console</code> tiene que existir la tabla <code>productos</code> con las columnas de tu entidad. Copia el <code>create table</code> de la consola de arranque y guárdalo: en la F6 lo vas a reutilizar como primera migración de Flyway.

    **El ejercicio de JDBC no te lo saltes.** Escribe `buscarPorCategoria` con `DriverManager`, `PreparedStatement` y `ResultSet`, cuenta las líneas y apúntalas en un comentario. En la F3 harás lo mismo con una.


## F2 — El modelo se convierte en entidad (S4–S6)

`Producto` deja de ser un `record`. Mapeo completo: `@Id` con generación, `@Column` con restricciones, `BigDecimal` para el precio, `@Enumerated(STRING)` si añades estado, `@PrePersist` para `creadoEn`, y `equals`/`hashCode` por id.

??? success "Claves"

    - El id es <code>Long</code>, no <code>long</code>.
    - Constructor <code>protected</code> sin argumentos, y el público con los campos de negocio.
    - <code>restarStock(int)</code> en vez de <code>setStock</code>: la entidad valida su propia regla.
    - <code>hashCode</code> constante (<code>getClass().hashCode()</code>) y <code>equals</code> por id.

    :material-alert: Los DTO **siguen siendo records**. La entidad no sale nunca del servicio.


## F3 — El repositorio desaparece (S7–S11)

Sustituye la interfaz de la UT4 por `extends JpaRepository<Producto, Long>` y **borra `ProductoRepositorioMemoria`**. Añade las consultas derivadas que necesites y adapta el servicio.

??? success "Comprobación"

    ```bash
    mvn test  # los tests de servicio y de controlador de la UT4 deben seguir en verde
    ```

    Ese es el momento de la unidad: **si separaste bien las capas, no has tocado ni el controlador ni los DTO ni el manejador de errores**. Si has tenido que reescribir medio proyecto, ya sabes qué mejorar la próxima vez.

    Cuenta las líneas borradas y compáralas con las del JDBC de la F1.


!!! tip "El mismo ejercicio, con tests que te corrigen"
    En el proyecto **`spring-base`** tienes `Articulo`, `Categoria` y `ArticuloRepositorio` sin anotar, y `UT5P2Test` esperándote con ocho comprobaciones: clave generada, `BigDecimal`, tres consultas derivadas, una `@Query`, el enumerado como texto y la columna obligatoria.

    ```bash
    cd practicas/spring-base
    ./verificar.sh UT5
    ```

    Empieza por ahí si la migración del proyecto de aula se te atasca: el test te dice exactamente qué falta y en qué orden.

## F4 — Pedidos, líneas y el N+1 (S12–S16)

Crea `Pedido` y `Linea` con la relación bidireccional, `cascade` y `orphanRemoval`, y el método `anadir` que actualiza los dos lados. Después provoca el N+1 y arréglalo.

??? success "El ejercicio que hay que hacer sí o sí"

    1. Carga 50 pedidos con 3 líneas cada uno.
    2. Recorre `findAll()` tocando `getLineas()` y **cuenta los `select` de la consola**: 51.
    3. Cambia a `JOIN FETCH` y vuelve a contar: **1**.
    4. Anota los dos números en el README del proyecto.

    Después provoca la `LazyInitializationException` devolviendo la entidad desde el controlador, y arréglala con DTO. No con `EAGER`, no con `open-in-view`.


## F5 — Transacciones e integridad (S17–S20)

Implementa `vender` con `@Transactional`, añade la restricción `UNIQUE`, captura `DataIntegrityViolationException` y pon `@Version`.

??? success "Los tres experimentos"

    **Sin transacción:** lanza una excepción después de descontar stock y comprueba que queda descontado con el pedido a medias. Añade `@Transactional` y repite.

    **La carrera:** cinco `curl` simultáneos creando el mismo nombre. Sin `UNIQUE`, cinco filas. Con `UNIQUE`, una y cuatro 409.

    **La actualización perdida:** dos ediciones concurrentes del mismo stock. Sin `@Version` se pierde una en silencio; con `@Version`, 409.


## F6 — Flyway, PostgreSQL y tests (S21–S23)

Pasa a `ddl-auto: validate`, escribe las migraciones, levanta PostgreSQL con Docker y escribe los tests de persistencia.

??? success "Comprobación final"

    ```bash
    docker compose up -d
    mvn spring-boot:run -Dspring-boot.run.profiles=prod
    docker compose exec db psql -U tienda -d tienda -c "\dt"
    docker compose exec db psql -U tienda -d tienda -c "SELECT * FROM flyway_schema_history"
    ```

    Y la prueba de fuego: `docker compose down` + `up -d` + arrancar. Los datos siguen.


---

## Estado final

Al terminar tienes la TiendaAPI con: entidades mapeadas, repositorios de Spring Data, relaciones sin N+1, transacciones, restricciones de integridad, bloqueo optimista, migraciones versionadas y PostgreSQL en Docker. **Eso ya es una aplicación de verdad**, no un ejercicio.

Comprueba que **todos los tests del proyecto base están en verde** antes del examen.
