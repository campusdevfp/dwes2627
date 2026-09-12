# Chuleta de JPA (UT5)

## Configuración

```yaml title="src/main/resources/application.yml"
spring:
  datasource:
    url: jdbc:h2:mem:tienda          # o jdbc:h2:file:./datos/tienda
    username: sa
  h2.console.enabled: true            # /h2-console
  jpa:
    hibernate.ddl-auto: update        # dev · validate en producción
    show-sql: true
    properties.hibernate.format_sql: true
    open-in-view: false               # OJO desactívalo
```

## Entidad

```java
@Entity
@Table(name = "productos",
       indexes = @Index(name = "idx_cat", columnList = "categoria"),
       uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class Producto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)   // SEQUENCE en PostgreSQL
    private Long id;                                          // Long, NO long

    @Column(nullable = false, length = 100) private String nombre;
    @Column(precision = 10, scale = 2)      private BigDecimal precio;   // dinero
    @Enumerated(EnumType.STRING)            private Estado estado;  // NUNCA ordinal
    @Column(updatable = false)              private Instant creadoEn;
    @Transient                              private double calculado;  // no se persiste
    @Version                                private Long version;  // bloqueo optimista

    protected Producto() { }            // JPA lo necesita
    @PrePersist void alCrear() { creadoEn = Instant.now(); }
}
```

`equals` por id · `hashCode` → `getClass().hashCode()`

## Relaciones

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)  // OJO EAGER por defecto: cámbialo
@JoinColumn(name = "pedido_id", nullable = false)
private Pedido pedido;                                     // ← LADO DUEÑO

@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Linea> lineas = new ArrayList<>();

public void anadir(Linea l) { lineas.add(l); l.setPedido(this); }   // los DOS lados
```

## Repositorio

```java
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoria(String categoria);
    Optional<Producto> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
    List<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max);
    List<Producto> findByNombreContainingIgnoreCase(String txt);
    List<Producto> findTop5ByOrderByPrecioDesc();
    long countByCategoria(String categoria);

    @Query("SELECT p FROM Producto p WHERE (:cat IS NULL OR p.categoria = :cat)")
    Page<Producto> buscar(@Param("cat") String cat, Pageable pageable);

    @EntityGraph(attributePaths = "lineas")
    List<Pedido> findByFechaAfter(LocalDate fecha);
}
```

`findBy` `countBy` `existsBy` `deleteBy` · `And` `Or` · `GreaterThan` `LessThan` `Between` · `Containing` `StartingWith` `IgnoreCase` · `OrderBy…Asc/Desc` · `Top5` `First3` · `In` `IsNull`

## El N+1

```java
// MAL  1 + N consultas
repositorio.findAll().forEach(p -> p.getLineas().size());

// BIEN 1 consulta
@Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.lineas")
```
Detéctalo con `show-sql: true`. `JOIN FETCH` de colección **no** se combina bien con `Pageable`.

## Transacciones

```java
@Service
@Transactional(readOnly = true)          // por defecto en la clase
public class Servicio {
    // solo revierte con unchecked sin esto
    @Transactional(rollbackFor = Exception.class)
    public void vender(...) { }
}
```

Trampas: **checked no revierte** · **llamada interna no pasa por el proxy** · **método privado no se intercepta** · nada de llamadas externas dentro.

Dentro de transacción **no hace falta `save`**: el *dirty checking* persiste los cambios.

## Integridad y concurrencia

```java
try { return repositorio.save(p); }
// 409
catch (DataIntegrityViolationException e) { throw new ProductoDuplicadoException(...); }
```

`@Version` → `ObjectOptimisticLockingFailureException` → **409**

## Producción

```
V1__esquema_inicial.sql   →  src/main/resources/db/migration/
```
`ddl-auto: validate` · Flyway · secretos en variables de entorno · **nunca editar una migración aplicada**

## Tests

```java
@DataJpaTest                    // solo persistencia, en memoria, rollback automático
@Autowired TestEntityManager em;

@SpringBootTest @Testcontainers // base de datos real
@Container @ServiceConnection
static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17-alpine");
```

## Errores que verás

| Mensaje | Causa |
|---|---|
| `LazyInitializationException` | Relación perezosa fuera de transacción → DTO + `JOIN FETCH` |
| `could not execute statement` + `unique` | Restricción violada → 409 |
| `ObjectOptimisticLockingFailureException` | Otro modificó la fila → 409 |
| `Migration checksum mismatch` | Editaste una migración aplicada |
| `Schema-validation: missing column` | La entidad y el esquema no cuadran |
