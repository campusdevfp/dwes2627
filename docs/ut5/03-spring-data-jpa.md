# Spring Data JPA: el repositorio que no escribes

> Recuerda las 25 líneas de JDBC del tema 1 para hacer un `SELECT ... WHERE categoria = ?`. Aquí van a ser **una**, y no la vas a escribir tú.

## 1. Una interfaz y ya está

```java
public interface ProductoRepositorio extends JpaRepository<Producto, Long> { }
```

Eso es todo. Spring Data **genera la implementación en tiempo de arranque** y te la inyecta. Ya tienes:

| Método | Qué hace |
|---|---|
| `save(entidad)` | `INSERT` si el id es `null`, `UPDATE` si no |
| `saveAll(lista)` | Lo mismo por lotes |
| `findById(id)` | Devuelve `Optional<Producto>` |
| `findAll()` · `findAll(Sort)` · `findAll(Pageable)` | Listar, ordenar, paginar |
| `existsById(id)` · `count()` | Comprobaciones baratas |
| `deleteById(id)` · `delete(entidad)` | Borrar |

Fíjate en que **la interfaz que escribiste a mano en la UT4 tenía casi los mismos métodos**. No es casualidad: estabas reinventando esto sin saberlo, y por eso ahora lo entiendes.

!!! success "La migración es esto"
    ```java
    // UT4
    public interface ProductoRepositorio {
        List<Producto> buscarTodos();
        Optional<Producto> buscarPorId(Integer id);
        Producto guardar(Producto p);
        boolean borrar(Integer id);
    }

    // UT5
    public interface ProductoRepositorio extends JpaRepository<Producto, Long> { }
    ```
    Y **borras** la clase `ProductoRepositorioMemoria` entera. El servicio cambia los nombres de método y punto.

## 2. `save` no es «guardar»

Es la fuente de la mitad de las sorpresas de esta unidad.

```java
var p = repositorio.findById(1L).orElseThrow();
p.setPrecio(new BigDecimal("59.99"));
// ¿hace falta repositorio.save(p)?
```

**No.** Dentro de una transacción, la entidad está *gestionada*: Hibernate detecta el cambio al terminar y lanza el `UPDATE` solo. Es el *dirty checking*.

```java
@Transactional
public void subirPrecio(Long id, BigDecimal nuevo) {
    var p = repositorio.findById(id).orElseThrow(() -> new ProductoNoEncontradoException(id));
    p.setPrecio(nuevo);        // sin save: se persiste igual al cerrar la transacción
}
```

Y al revés: **fuera de transacción, ese mismo código no guarda nada** y no da ningún error. Silencioso y desconcertante hasta que lo entiendes.

Los tres estados de una entidad:

| Estado | Qué significa |
|---|---|
| **Transitoria** | Creada con `new`, sin id, JPA no la conoce |
| **Gestionada** | Dentro de una transacción; los cambios se detectan solos |
| **Separada** | La transacción terminó; los cambios ya no se detectan |

## 3. Consultas derivadas del nombre

Aquí está la magia que más impresiona:

```java
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(String categoria);
    Optional<Producto> findByNombre(String nombre);
    boolean existsByNombre(String nombre);

    List<Producto> findByCategoriaAndStockGreaterThan(String categoria, int minimo);
    List<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max);
    List<Producto> findByNombreContainingIgnoreCase(String texto);
    List<Producto> findByStockLessThanOrderByStockAsc(int minimo);
    List<Producto> findTop5ByOrderByPrecioDesc();
    long countByCategoria(String categoria);
    void deleteByCategoria(String categoria);
}
```

**Spring lee el nombre del método y escribe el SQL.** El vocabulario:

| Palabra | Significa |
|---|---|
| `findBy` · `readBy` · `getBy` | Consulta |
| `countBy` · `existsBy` · `deleteBy` | Contar, comprobar, borrar |
| `And` · `Or` | Combinar condiciones |
| `GreaterThan` · `LessThan` · `Between` | Comparaciones |
| `Like` · `Containing` · `StartingWith` | Texto |
| `IgnoreCase` | Sin distinguir mayúsculas |
| `OrderBy…Asc/Desc` | Ordenación |
| `Top5` · `First3` | Limitar |
| `IsNull` · `IsNotNull` · `In` | Nulos y listas |

!!! bug "Si el nombre del método está mal, la aplicación no arranca"
    `findByCatgoria` (con la errata) falla **al arrancar**, no en ejecución, con un mensaje que dice exactamente qué propiedad no existe. Es una buena noticia: el error aparece en segundos, no en producción.

!!! danger "Cuándo dejar de usarlas"
    `findByCategoriaAndStockGreaterThanAndPrecioLessThanOrderByNombreAsc` es un nombre ilegible. Cuando pases de **tres condiciones**, usa `@Query`. La regla: si tienes que leerlo dos veces para entenderlo, ya no compensa.

## 4. `@Query`: JPQL y SQL nativo

``` { .java .numerado }
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    // JPQL: se escribe sobre ENTIDADES y sus campos, no sobre tablas y columnas
    @Query("SELECT p FROM Producto p WHERE p.stock < :minimo ORDER BY p.stock")
    List<Producto> bajoMinimos(@Param("minimo") int minimo);

    @Query("""
           SELECT p FROM Producto p
           WHERE (:categoria IS NULL OR p.categoria = :categoria)
             AND (:precioMax IS NULL OR p.precio <= :precioMax)
           """)
    Page<Producto> buscar(@Param("categoria") String categoria,
                          @Param("precioMax") BigDecimal precioMax,
                          Pageable pageable);

    @Modifying
    @Query("UPDATE Producto p SET p.precio = p.precio * :factor WHERE p.categoria = :cat")
    int subirPrecios(@Param("cat") String categoria, @Param("factor") BigDecimal factor);

    // SQL nativo: solo si JPQL no llega
    @Query(value = "SELECT * FROM productos WHERE MOD(id, 2) = 0", nativeQuery = true)
    List<Producto> idsPares();
}
```

Ese filtro opcional con `:categoria IS NULL OR ...` es **el mismo patrón** que usaste a mano en la UT6 con streams. Ahora lo resuelve la base de datos.

| | JPQL | SQL nativo |
|---|---|---|
| Habla de | Entidades y campos | Tablas y columnas |
| Portable entre motores | :material-check: | :material-close: |
| Funciones específicas del motor | :material-close: | :material-check: |
| Lo valida Hibernate al arrancar | :material-check: | :material-close: |

**Usa JPQL por defecto.** El nativo solo cuando necesites algo que JPQL no expresa.

:material-alert: `@Modifying` requiere `@Transactional` en quien lo llame, y **salta el caché de primer nivel**: las entidades que ya tuvieras cargadas quedan desactualizadas.

## 5. Paginación de verdad

En la UT6 paginaste **a mano sobre una lista en memoria**: cargabas todo y recortabas. Aquí la base de datos solo devuelve lo que pides.

```java
@GetMapping
public Page<ProductoDto> buscar(
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) BigDecimal precioMax,
        @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
    return repositorio.buscar(categoria, precioMax, pageable).map(mapper::aDto);
}
```

Hibernate genera **dos** consultas: una con `LIMIT`/`OFFSET` para los datos y otra `COUNT` para el total. Con 200.000 filas y `size=20`, trae 20.

!!! danger "El límite de ordenación sigue haciendo falta"
    `Pageable` acepta `?sort=cualquierCampo`, y ahora eso **llega a la base de datos**. Un `?sort=usuario.password` en una entidad con relaciones puede filtrar información. La **lista blanca** de la UT6 sigue siendo obligatoria:
    ```java
    private static final Set<String> ORDENABLES = Set.of("nombre", "precio", "stock");

    if (pageable.getSort().stream().anyMatch(o -> !ORDENABLES.contains(o.getProperty())))
        throw new DatosInvalidosException("No se puede ordenar por ese campo");
    ```

## 6. Proyecciones: traer solo lo que necesitas

Para un desplegable no hace falta la entidad entera:

```java
// Interfaz: Spring genera la implementación
public interface ProductoResumen {
    Long getId();
    String getNombre();
    BigDecimal getPrecio();
}

List<ProductoResumen> findByCategoria(String categoria);
```

Hibernate ejecuta `SELECT id, nombre, precio FROM productos WHERE categoria = ?` en vez de traer todas las columnas. Con tablas anchas o con `@Lob`, la diferencia es notable.

También con un `record` y constructor en JPQL:

```java
public record ProductoResumenDto(Long id, String nombre, BigDecimal precio) {}

@Query("SELECT new es.iesx.daw.tienda.dto.ProductoResumenDto(p.id, p.nombre, p.precio) FROM Producto p")
List<ProductoResumenDto> resumen();
```

---

## Pruébalo ahora (35 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1 — Borra código.** Sustituye tu `ProductoRepositorioMemoria` por la interfaz que extiende `JpaRepository` y **elimina la clase**. Cuenta las líneas que has borrado.

**Parte 2 — Consultas derivadas.** Añade `findByCategoria`, `existsByNombre`, `findByPrecioBetween` y `findTop5ByOrderByPrecioDesc`, y pruébalas por la API mirando el SQL en la consola.

**Parte 3 — Provoca el error.** Escribe a propósito `findByCatgoria`. La aplicación **no arranca** y el mensaje te dice qué propiedad no existe. Ese fallo rápido es una característica, no un defecto.

**Parte 4 — El `save` que no hace falta.** Escribe esto y comprueba que el precio **sí** cambia en la base de datos:
```java
@Transactional
public void subir(Long id) {
    var p = repositorio.findById(id).orElseThrow();
    p.setPrecio(p.getPrecio().multiply(new BigDecimal("1.10")));
}
```
Ahora **quita `@Transactional`** y repite. No cambia nada y no salta ningún error. Ese silencio es lo que hay que conocer.

**Parte 5 — Paginación real.** Carga 50.000 productos con un `CommandLineRunner`, pide `?page=0&size=10` y mira el SQL: verás el `LIMIT`. Compáralo con lo que hacía tu paginación en memoria de la UT6.

**Parte 6 — Proyección.** Crea `ProductoResumen` y comprueba en la consola que el `SELECT` solo trae tres columnas.

---

## Ejercicios (con solución)

### Ejercicio 1 — Escribe los métodos
Sin `@Query`: productos de una categoría con stock mayor que N; buscar por nombre parcial ignorando mayúsculas; los 3 más caros; contar los de una categoría.

??? success "Solución"

    ```java
    List<Producto> findByCategoriaAndStockGreaterThan(String categoria, int stock);
    List<Producto> findByNombreContainingIgnoreCase(String texto);
    List<Producto> findTop3ByOrderByPrecioDesc();
    long countByCategoria(String categoria);
    ```
    Ojo con `Containing`: genera `LIKE %texto%`, que **no usa el índice**. Con tablas grandes hay que ir a búsqueda de texto completo del motor.


### Ejercicio 2 — El `save` fantasma
Un compañero modifica una entidad y no se guarda, pero no hay ningún error. Da dos explicaciones posibles.

??? success "Solución"

    (1) <b>No hay transacción</b>: fuera de <code>@Transactional</code>, la entidad está separada y Hibernate no detecta los cambios. Nadie avisa.<br>
    (2) Está modificando <b>una copia</b>: un DTO, o el resultado de un <code>map</code>, en vez de la entidad gestionada que devolvió el repositorio.<br>
    Y la tercera, más rara: el método es <code>@Transactional</code> pero se llama <b>desde dentro de la misma clase</b>, así que el proxy de Spring no interviene y la transacción no existe.


### Ejercicio 3 — Derivada o `@Query`
¿Cuándo dejar el nombre largo y pasar a `@Query`?

??? success "Solución"

    A partir de <b>tres condiciones</b>, o cuando haya filtros <b>opcionales</b>. Un <code>findByCategoriaAndStockGreaterThanAndPrecioLessThanOrderByNombreAsc</code> es ilegible y, sobre todo, no admite que el cliente omita un filtro: harían falta cuatro métodos distintos. Con <code>@Query</code> y <code>(:param IS NULL OR ...)</code> se resuelve en uno.


### Ejercicio 4 — JPQL o nativo
Necesitas una consulta con una función de ventana de PostgreSQL. ¿Cuál usas y qué pierdes?

??? success "Solución"

    <b>Nativo</b>, porque JPQL no tiene funciones de ventana. Pierdes <b>portabilidad</b> —esa consulta ya no funciona en H2, y tus tests con H2 fallarán— y pierdes la <b>validación al arrancar</b>: un error de sintaxis aparecerá en ejecución.<br>
    Mitigación habitual: usar la misma base de datos en tests que en producción, con Testcontainers.


### Ejercicio 5 — Diseña el repositorio
Para `Pedido`, escribe los métodos que necesitaría una pantalla de administración: buscar por referencia, listar por estado paginado, pedidos de un rango de fechas, y el total facturado en un mes.

??? success "Solución"

    ```java
    public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {

        Optional<Pedido> findByReferencia(String referencia);

        Page<Pedido> findByEstado(EstadoPedido estado, Pageable pageable);

        List<Pedido> findByFechaBetweenOrderByFechaDesc(LocalDate desde, LocalDate hasta);

        @Query("""
               SELECT COALESCE(SUM(p.total), 0) FROM Pedido p
               WHERE p.fecha BETWEEN :desde AND :hasta AND p.estado <> 'CANCELADO'
               """)
        BigDecimal facturado(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
    }
    ```
    Dos detalles de nota: <code>COALESCE(..., 0)</code> para que un mes sin pedidos devuelva 0 y no <code>null</code>, y excluir los cancelados, que es la regla de negocio que casi todos olvidan.

