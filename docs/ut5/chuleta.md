# Chuleta de la UT5 — Spring Data JPA

!!! tip ":material-file-pdf-box: Descárgala para el examen"
    **[:material-download: chuleta-ut5.pdf](../pdf/chuleta-ut5.pdf)** — cuatro caras, lista para imprimir. Es la única documentación permitida en el examen práctico de RA6.

    También puedes generarla desde esta página con <kbd>Ctrl</kbd>+<kbd>P</kbd> → *Guardar como PDF*.

La lista **completa** de anotaciones, con sus valores por defecto y sus trampas, está en [3. Referencia de JPA y validaciones](03-referencias.md).

---

## Dependencias y configuración

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId>
  <scope>runtime</scope></dependency>
<dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId>
  <scope>runtime</scope></dependency>
```

=== "dev · H2 en fichero"

    ```properties
    spring.datasource.url=jdbc:h2:file:./datos/funkos;AUTO_SERVER=TRUE
    spring.datasource.username=sa
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    spring.h2.console.enabled=true          # /h2-console
    spring.jpa.defer-datasource-initialization=true
    spring.sql.init.mode=always
    spring.jpa.open-in-view=false
    ```

=== "postgres"

    ```properties
    spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/funkos}
    spring.datasource.username=${DB_USER:dwes}
    spring.datasource.password=${DB_PASSWORD:dwes2026}
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.defer-datasource-initialization=true
    spring.sql.init.data-locations=classpath:data-postgres.sql
    spring.h2.console.enabled=false
    spring.jpa.open-in-view=false
    ```

    ```bash
    docker compose up -d
    SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
    ```

=== "prod"

    ```properties
    spring.datasource.url=${DB_URL}
    spring.datasource.username=${DB_USER}
    spring.datasource.password=${DB_PASSWORD}
    spring.jpa.hibernate.ddl-auto=validate
    spring.jpa.show-sql=false
    spring.h2.console.enabled=false
    spring.sql.init.mode=never
    spring.jpa.open-in-view=false
    ```

| `ddl-auto` | Qué hace | Dónde |
|---|---|---|
| `update` | Añade lo que falta, **nunca borra** | **dev** |
| `create-drop` | Borra y crea al arrancar y al parar | **test** |
| `create` | Borra y crea al arrancar | — |
| `validate` | Comprueba; si no cuadra, **no arranca** | **prod** |
| `none` | Nada | prod con migraciones |

:material-alert: `create-drop` en producción **borra la base de datos** en cada reinicio. Y en `dev`, con H2 en fichero, también.
:material-alert: `update` **no sabe renombrar ni borrar**: crea la columna nueva y deja la vieja con los datos dentro.

| Sentencia del `data.sql` | H2 | PostgreSQL |
|---|---|---|
| Insertar sin duplicar | `MERGE … KEY(id)` | `INSERT … ON CONFLICT (id) DO NOTHING` |
| Recolocar el contador | `ALTER TABLE … ALTER COLUMN id RESTART WITH 7` | `SELECT setval('tabla_id_seq', …)` |

:material-alert: Sin el `setval`, el primer `POST` contra PostgreSQL falla con `duplicate key value`.
:material-alert: `open-in-view` viene **activado**: apágalo o esconderás los N+1.
:material-alert: Sin `defer-datasource-initialization=true`, `data.sql` falla con `Table not found`.

## Entidad

```java
@Entity
@Table(name = "funkos",
       indexes = @Index(name = "idx_funko_nombre", columnList = "nombre"),
       uniqueConstraints = @UniqueConstraint(columnNames = {"titulo", "genero_id"}))
public class Funko {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)   // dinero
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)                           // NUNCA ORDINAL
    @Column(length = 20)
    private Categoria categoria;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Version private Long version;                         // bloqueo optimista

    protected Funko() {}                                   // JPA lo necesita

    @PreUpdate void alActualizar() { this.updatedAt = LocalDateTime.now(); }
}
```

:material-alert: Constructor sin argumentos obligatorio → **una entidad no puede ser `record`**.
:material-alert: `@Enumerated` por defecto es `ORDINAL`: guarda la **posición**, y reordenar el `enum` corrompe todos los datos en silencio.
:material-alert: Dinero con `BigDecimal` + `precision`/`scale`, nunca `double`.

| `@Id` | Cómo genera | Cuándo |
|---|---|---|
| `IDENTITY` | Autoincremental de la BD | **H2, PostgreSQL** |
| `SEQUENCE` | Secuencia con caché | PostgreSQL, inserciones masivas |
| `AUTO` | Que elija Hibernate | — |
| `UUID` | En Java, antes de insertar | Ids no enumerables |

## Relaciones

```java
// 1:N — el DUEÑO es el lado MUCHOS (ahí está la FK)
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "categoria_id", nullable = false)
private Categoria categoria;

// y en Categoria, el lado INVERSO
@OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
private List<Funko> funkos = new ArrayList<>();

```

**En la unidad y en el examen solo se usan estas dos.** `@OneToOne`, `@ManyToMany` y `@Embedded` están en el tema 3 de referencia.

| | `fetch` por defecto | Lo correcto |
|---|---|---|
| `@ManyToOne` | **`EAGER`** | **`LAZY`** |
| `@OneToMany` | `LAZY` | `LAZY` |

:material-alert: **El `EAGER` por defecto del `@ManyToOne` es el problema de rendimiento nº 1 de JPA.** Siempre `LAZY`.
:material-alert: `@OneToMany` sin `mappedBy` → Hibernate crea una tabla intermedia fantasma.
:material-alert: `cascade = ALL` en un `@OneToMany` hace que borrar el padre **borre los hijos**. Casi nunca es lo que quieres: suele ser un 409.
:material-alert: Una relación N:M **con datos propios** (nota, cantidad, fecha) no es un `@ManyToMany`: son **dos 1:N con una entidad en medio**.

| `cascade` | Propaga |
|---|---|
| `PERSIST` `MERGE` `REMOVE` `REFRESH` `DETACH` | guardar / actualizar / **borrar** / recargar / desvincular |
| `ALL` | los cinco |
| `orphanRemoval = true` | sacar un hijo de la colección **lo borra** |

## Repositorio

```java
public interface FunkosRepository
        extends JpaRepository<Funko, Long>, JpaSpecificationExecutor<Funko> {

    Optional<Funko> findByIdAndDeletedFalse(Long id);
    List<Funko>     findByCategoriaNombreIgnoreCaseAndDeletedFalse(String cat);
    boolean         existsByNombreIgnoreCase(String nombre);
    long            countByCategoriaId(Long id);
    List<Funko>     findTop5ByDeletedFalseOrderByPrecioDesc();
    List<Funko>     findByPrecioBetween(BigDecimal min, BigDecimal max);
}
```

**Ya vienen hechos:** `findAll()` · `findAll(Pageable)` · `findById` → `Optional` · `save` · `saveAll` · `deleteById` · `existsById` · `count` · `flush`

| Palabra | SQL |
|---|---|
| `findBy` `countBy` `existsBy` `deleteBy` | `SELECT` `COUNT` `EXISTS` `DELETE` |
| `And` `Or` | `AND` `OR` |
| `Between` `LessThan(Equal)` `GreaterThan(Equal)` `After` `Before` | comparaciones |
| `Like` `Containing` `StartingWith` `EndingWith` | `LIKE` |
| `IgnoreCase` | `UPPER(…)` |
| `In` `NotIn` `IsNull` `IsNotNull` `True` `False` | |
| `OrderBy…Asc/Desc` | `ORDER BY` |
| `Top3` `First5` `Distinct` | `LIMIT` `DISTINCT` |

:material-alert: Un nombre de campo mal escrito → **`PropertyReferenceException` al ARRANCAR**, no en producción.

## `@Query`

```java
// JPQL: entidades y campos Java. Se valida al arrancar. Portable.
@Query("SELECT f FROM Funko f WHERE f.categoria.nombre = :cat AND f.cantidad > 0")
List<Funko> conStock(@Param("cat") String cat);

// Evitar el N+1 al paginar: JOIN FETCH + countQuery OBLIGATORIO
@Query(value      = "SELECT f FROM Funko f JOIN FETCH f.categoria WHERE f.deleted = false",
       countQuery = "SELECT count(f) FROM Funko f WHERE f.deleted = false")
Page<Funko> findAllConCategoria(Pageable pageable);

// o, más limpio
@EntityGraph(attributePaths = {"categoria"})
Page<Funko> findAll(Pageable pageable);

// Proyección directa a DTO
@Query("SELECT new es.iesx.dto.FunkoResumen(f.nombre, f.precio) FROM Funko f")
List<FunkoResumen> resumen();

// SQL nativo: tablas y columnas. No portable. No se valida al arrancar.
@Query(value = "SELECT * FROM funkos WHERE …", nativeQuery = true)

// Escritura
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE Funko f SET f.deleted = true WHERE f.id = :id")
int borradoLogico(@Param("id") Long id);
```

:material-alert: **Nunca concatenes** en un `@Query`, ni en el nativo. Usa `:param` o `?1`.
:material-alert: `@Modifying` necesita **además** `@Transactional` en quien lo llama.
:material-alert: `JOIN FETCH` + `Pageable` **sin `countQuery`** → Hibernate pagina **en memoria** y avisa con `applying in memory`.

## Paginación

```java
Page<Funko>  findAll(Pageable p);            // + SELECT count(*): 2 consultas
Slice<Funko> findByDeletedFalse(Pageable p); // 1 consulta, solo "¿hay más?"
List<Funko>  findByCategoria(String c, Sort s);
```

```java
@GetMapping
public ResponseEntity<Page<FunkoResponse>> findAll(
        @PageableDefault(size = 20, sort = "id") Pageable pageable) {
    return ResponseEntity.ok(servicio.findAll(sanear(pageable)));
}

private Pageable sanear(Pageable p) {                   // lista BLANCA + tope
    var ok = Set.of("id", "nombre", "precio", "createdAt");
    var orden = p.getSort().stream()
                 .filter(o -> ok.contains(o.getProperty())).toList();
    return PageRequest.of(p.getPageNumber(), Math.min(p.getPageSize(), 100),
                          orden.isEmpty() ? Sort.by("id") : Sort.by(orden));
}
```

```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100
```

`?page=0&size=20&sort=precio,desc&sort=nombre,asc` · **`page` empieza en 0**

`Page`: `getContent()` `getTotalElements()` `getTotalPages()` `getNumber()` `hasNext()` `isLast()` **`map(fn)`**

```java
return repositorio.findAll(spec, pageable).map(mapper::toResponse);   // Page<DTO>
```

**El JSON que sale:**

```json
{ "content": [ … ], "totalElements": 6, "totalPages": 3,
  "number": 0, "size": 2, "first": true, "last": false, "numberOfElements": 2 }
```

| Situación | Respuesta correcta |
|---|:-:|
| `?page=500` fuera de rango | **200** con `content: []`, no 404 |
| `?sort=inventado` | **200** ordenando por `id` (o **400**), nunca **500** |
| `?size=99999` | Se recorta al tope |

:material-alert: Sin tope de `size`, `?size=1000000` carga la tabla entera: has paginado para nada.
:material-alert: Sin lista blanca de `sort`, se puede ordenar por **cualquier** campo de la entidad.
:material-alert: Sin sanear, `?sort=inventado` lanza `PropertyReferenceException` → **500**.

## `Specification` — criterios combinables

```java
public interface R extends JpaRepository<Funko,Long>, JpaSpecificationExecutor<Funko> {}
```

```java
Specification<Funko> spec = (root, q, cb) -> cb.isFalse(root.get("deleted"));

spec = categoria.map(c -> spec.and((root, q, cb) ->
        cb.equal(cb.upper(root.get("categoria").get("nombre")), c.toUpperCase())))
        .orElse(spec);

spec = precioMin.map(p -> spec.and((root, q, cb) ->
        cb.greaterThanOrEqualTo(root.get("precio"), p))).orElse(spec);

spec = nombre.map(n -> spec.and((root, q, cb) ->
        cb.like(cb.lower(root.get("nombre")), "%" + n.toLowerCase() + "%")))
        .orElse(spec);

return repositorio.findAll(spec, pageable).map(mapper::toResponse);
```

`cb.equal` `notEqual` `like` `greaterThan(OrEqualTo)` `lessThan(OrEqualTo)` `between` `isNull` `isTrue` `isFalse` `in` `and` `or` `not` · `cb.upper` `cb.lower`

:material-alert: Con 4 filtros opcionales hay **16 combinaciones**. Con `if` anidados es inmantenible.

## `@Transactional`

```java
@Transactional(readOnly = true)              // lecturas
@Transactional                               // escrituras
@Transactional(rollbackFor = Exception.class)
```

:material-alert: **Solo revierte con `RuntimeException`.** Una comprobada **confirma** la transacción a medias.
:material-alert: Funciona **por proxy**: `this.save(...)` no abre transacción. Igual que `@Cacheable`.
:material-alert: Si **capturas** la excepción dentro, **no hay rollback**: tiene que salir del método.
:material-alert: Va en el **servicio**, no en el repositorio: dos llamadas a `JpaRepository` son **dos** transacciones.

```java
@Version private Long version;    // → ObjectOptimisticLockingFailureException → 409
```

Dentro de una transacción las entidades están **gestionadas**: un `setter` ya persiste, sin `save()`.

## Bruno · pruebas de la API

Ficheros `.bru` que viajan con el proyecto y se versionan en Git.

```
bruno/
├── bruno.json
├── environments/local.bru      → vars { baseUrl: http://localhost:8080 }
└── 01-listar.bru  02-crear.bru  …
```

```
meta {
  name: Listar paginado
  type: http
  seq: 1
}

get {
  url: {{baseUrl}}/api/v1/funkos?page=0&size=3&sort=id,asc
  body: none
  auth: none
}

assert {
  res.status: eq 200
  res.body.totalElements: gte 1
}

tests {
  test("pagina de 3 como maximo", function() {
    expect(res.getStatus()).to.equal(200);
    expect(res.getBody().content.length).to.be.at.most(3);
  });
}
```

**Encadenar peticiones:**

```
script:post-response {
  bru.setVar("creadoId", res.getBody().id);
}
```

…y después `url: {{baseUrl}}/api/v1/funkos/{{creadoId}}`

| Bloque | Para qué |
|---|---|
| `meta` | Nombre y **orden** (`seq`) |
| `get` / `post` / `patch` / `delete` | Verbo y URL |
| `body:json` | El cuerpo |
| `assert` | Comprobaciones de una línea: `eq` `neq` `gt` `gte` `isNumber` |
| `tests` | Comprobaciones con código y `expect(...)` |
| `script:post-response` | `bru.setVar("x", res.getBody().campo)` |

```bash
npm install -g @usebruno/cli
cd bruno && bru run --env local          # la coleccion entera
bru run 03-crear.bru --env local         # una sola
```

| Dentro de `tests` | Devuelve |
|---|---|
| `res.getStatus()` | El código de estado |
| `res.getBody()` | El cuerpo ya convertido |
| `res.getHeader("location")` | Una cabecera |
| `bru.getVar("creadoId")` | Una variable guardada antes |

:material-alert: Comprobar solo `res.status: eq 200` **no puntúa**: hay que mirar también algo del cuerpo.
:material-alert: Ids fijos (`/funkos/1`) rompen la colección en cuanto cambian los datos: usa `bru.setVar`.

## Docker

```yaml
services:
  api:
    build: .
    ports: ["8080:8080"]
    env_file: .env
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://db:5432/funkos   # ← "db", NO localhost
    depends_on:
      db: { condition: service_healthy }         # ← sin esto, falla al arrancar
  db:
    image: postgres:17-alpine
    environment:
      POSTGRES_DB: funkos
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports: ["5432:5432"]
    volumes: [dbdata:/var/lib/postgresql/data]
    healthcheck:
      test: ["CMD-SHELL","pg_isready -U ${DB_USER} -d funkos"]
      interval: 5s
      retries: 10
volumes: { dbdata: }
```

```bash
docker compose up -d                  # levantar
docker compose ps                     # healthy
docker compose exec db psql -U dwes -d funkos -c '\dt'
docker compose down -v                # el -v BORRA los datos
```

---

## Los catorce errores del examen

| | Síntoma | Causa |
|:-:|---|---|
| 1 | `LazyInitializationException` | Se devuelve la entidad en vez del DTO |
| 2 | 100 funkos → 101 consultas | Falta el `@EntityGraph` (o el `JOIN FETCH`) |
| 3 | `StackOverflowError` al serializar | Relación bidireccional devuelta tal cual |
| 4 | Tabla intermedia que no pediste | `@OneToMany` sin `mappedBy` |
| 5 | Borrar un género borra sus películas | `cascade = CascadeType.ALL` |
| 6 | Los datos de Disney pasan a ser de Marvel | `@Enumerated` sin `EnumType.STRING` |
| 7 | `Table "FUNKOS" not found` al arrancar | Falta `defer-datasource-initialization=true` |
| 8 | `TransactionRequiredException` | `@Modifying` sin `@Transactional` |
| 9 | La transacción se confirma tras un fallo | Excepción comprobada, o capturada dentro |
| 10 | Base de datos vacía tras reiniciar | `ddl-auto=create-drop` en `dev` o `prod` |
| 11 | `duplicate key value` en el primer POST con PostgreSQL | Falta el `setval` de la secuencia |
| 12 | `?sort=inventado` → 500 | `Pageable` sin lista blanca de campos |
| 13 | El listado devuelve una `List` con toda la tabla | Falta `Pageable` en el repositorio y el controlador |
| 14 | `bru run` falla sin llegar a lanzar peticiones | Falta `bruno.json`, o el entorno no se pasa con `--env local` |
