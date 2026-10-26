# 1. Spring Data JPA: el ORM en la práctica

**Lo esencial, y nada más.** Con lo que hay en este tema puedes escribir cualquier API de la unidad y aprobar el examen. Lo demás —`@OneToOne`, `@ManyToMany`, `@Embedded`, herencia, el detalle de cada opción de cascada— está en la [referencia del tema 3](03-referencias.md) para consultarlo cuando haga falta.

!!! success "La idea entera, en una frase"
    **Escribes clases Java, y el ORM escribe el SQL.**

    En la UT4 escribiste `INSERT INTO funkos (…) VALUES (…)` a mano. Aquí declaras `class Funko` con unas anotaciones, y las sentencias las genera Hibernate.

| | Quién es | Qué hace |
|---|---|---|
| **JPA** | La **especificación** | Un estándar de Java: solo interfaces y anotaciones |
| **Hibernate** | La **implementación** que trae Spring Boot | **Escribe el SQL** y mantiene los objetos en memoria |
| **Spring Data JPA** | Una capa encima | **Genera los repositorios** a partir del nombre de los métodos |

Cuando en el log sale `Hibernate: select …`, es el de en medio trabajando. Y ese SQL es el que escribiste a mano en la UT4: **compáralos, es el mismo**.

```xml title="pom.xml — lo único que hay que añadir"
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

!!! info "Fuera `spring-boot-starter-jdbc`"
    `data-jpa` ya lo trae dentro. Y el driver de la base de datos (`h2`, `postgresql`) se queda como estaba.

---

## 1.1. La configuración, que son cinco líneas

```properties title="application.properties"
spring.datasource.url=jdbc:h2:file:./datos/funkos;AUTO_SERVER=TRUE
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

**Y ya está: no hay `schema.sql`.** Hibernate crea las tablas leyendo tus clases.

### `ddl-auto`: las opciones y cuál elegir

| Valor | Qué hace | Dónde |
|---|---|---|
| `validate` | **Comprueba** el esquema; si no cuadra, **no arranca** | **prod** |
| `update` | Añade lo que falta, **nunca borra** | **dev** |
| `create-drop` | Borra y crea al arrancar, y borra al parar | **test** |
| `none` | Nada | prod con migraciones |

!!! danger "`update` en `dev`, `create-drop` en `test`"
    En desarrollo la base es un **fichero** y la gracia es que los datos persistan: `create-drop` los tiraría en cada reinicio. En los tests la base es `mem:` y se quiere **limpia**.

    El límite de `update`: **no sabe renombrar ni borrar**. Si cambias un campo de nombre, crea la columna nueva y **deja la vieja con los datos dentro**, sin avisar.

!!! danger "`spring.jpa.open-in-view=false` — ponlo y no lo quites"
    Viene **activado**, y Spring Boot lo avisa al arrancar:

    ```
    WARN  JpaBaseConfiguration : spring.jpa.open-in-view is enabled by default.
    ```

    Mantiene la sesión de Hibernate abierta durante **toda la petición**, incluida la serialización del JSON. Eso **esconde** los errores de carga perezosa, lanzando consultas desde el serializador, donde no las controlas.

    Apagándolo, el error salta **en desarrollo** y lo arreglas donde toca.

### Datos iniciales

```properties
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql
```

!!! warning "Las tres líneas van juntas"
    Sin `defer-datasource-initialization=true`, Spring ejecuta `data.sql` **antes** de que Hibernate cree las tablas y falla con `Table "FUNKOS" not found`. El mensaje no da ninguna pista de la causa.

---

## 1.2. La entidad

```java title="funkos/models/Funko.java"
package es.iesx.funkos.funkos.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "funkos")
public class Funko {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** JPA lo necesita. protected para que no se use desde fuera. */
    protected Funko() {}

    /** El que usa tu código: solo lo que decide el cliente. */
    public Funko(String nombre, BigDecimal precio, Integer cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    @PreUpdate
    void alActualizar() { this.updatedAt = LocalDateTime.now(); }

    // getters de todo, y setters SOLO de lo que puede cambiar
    public Long getId()             { return id; }
    public String getNombre()       { return nombre; }
    public BigDecimal getPrecio()   { return precio; }
    public Integer getCantidad()    { return cantidad; }
    public boolean isDeleted()      { return deleted; }

    public void setNombre(String nombre)      { this.nombre = nombre; }
    public void setPrecio(BigDecimal precio)  { this.precio = precio; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setDeleted(boolean borrado)   { this.deleted = borrado; }
}
```

### Las cuatro anotaciones que hacen falta

| | Qué hace |
|---|---|
| `@Entity` | Esta clase es una tabla |
| `@Table(name = …)` | Cómo se llama la tabla |
| `@Id` + `@GeneratedValue` | La clave primaria y quién la genera |
| `@Column(…)` | Nombre, obligatoriedad, longitud y precisión de la columna |

!!! success "Dos constructores, ni builder ni Lombok"
    | | Para qué |
    |---|---|
    | `protected Funko() {}` | **Lo necesita Hibernate**: construye por reflexión |
    | `Funko(nombre, precio, cantidad)` | **El que usa tu código.** Solo los campos del cliente |

    El `id` lo pone la base de datos, las fechas se inicializan en la declaración y `deleted` arranca en `false`. **No hacen falta en el constructor.**

    Y para modificar están los `setters`: dentro de una transacción, **un `setter` ya persiste**.

!!! danger "Una entidad NO puede ser un `record`"
    Un `record` es `final` y sus campos inmutables, y Hibernate necesita un constructor sin argumentos y poder escribir los campos. **Los DTOs sí siguen siendo `record`.**

| | UT4 | UT5 |
|---|---|---|
| El modelo | `record` | **Clase con dos constructores** |
| Los DTOs | `record` | **`record`**, sin cambios |

!!! info "`@GeneratedValue`: usa `IDENTITY`"
    `IDENTITY` delega en la columna autoincremental del motor, que es lo que quieres con H2 y PostgreSQL. Las otras estrategias (`SEQUENCE`, `TABLE`, `AUTO`) están en la [referencia](03-referencias.md#32-identificadores).

!!! warning "`equals` y `hashCode`: NO los generes con el IDE"
    El que genera IntelliJ compara **todos** los campos. En una entidad eso rompe en cuanto la metes en un `Set`: antes de guardar el `id` es `null`, después no, el `hashCode` cambia y **el elemento se pierde dentro del `HashSet`**.

    Aquí no hacen falta: trabajamos con `List` y comparamos DTOs, que son `record` y lo traen bien hecho.

---

## 1.3. La relación que de verdad se usa: `@ManyToOne`

Muchos funkos pertenecen a una categoría. **La clave ajena vive en la tabla de los muchos.**

```java title="El lado dueño: en Funko"
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "categoria_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_funko_categoria"))
private Categoria categoria;
```

```java title="El lado inverso: en Categoria. OPCIONAL"
@OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
private List<Funko> funkos = new ArrayList<>();
```

!!! info "Y el constructor pasa a tener cuatro parámetros"
    Al añadir la relación, la categoría es obligatoria (`optional = false`), así que entra en el constructor del punto anterior:

    ```java
    public Funko(String nombre, BigDecimal precio, Integer cantidad, Categoria categoria) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.categoria = categoria;
    }
    ```

    **Quien resuelve la categoría es el servicio**, que busca la entidad por su nombre antes de construir el funko. El cliente manda `"ANIME"`, no un `id`.

### Las tres decisiones de una relación

=== "1 · `LAZY`, siempre"

    ```java
    @ManyToOne(fetch = FetchType.LAZY)      // ← NO es el valor por defecto
    ```

    | | Cuándo trae la categoría |
    |---|---|
    | `EAGER` (**el defecto en `@ManyToOne`**) | **Siempre**, aunque nadie la mire |
    | `LAZY` | Solo cuando llamas a `getCategoria()` |

    Listar 20 funkos con `EAGER` son **21 consultas** sin que lo hayas pedido. Ponlo `LAZY` en **todas** las relaciones y resuelve la carga cuando la necesites (es el [tema 2](02-resultados-avanzados.md)).

=== "2 · ¿Hace falta el lado inverso?"

    **No siempre.** La clave ajena está en `funkos`, así que la relación funciona **solo con el `@ManyToOne`**. El `@OneToMany` es una comodidad para navegar de la categoría a sus funkos.

    | | Cuándo ponerlo |
    |---|---|
    | **Sin `@OneToMany`** | Si para listar los funkos de una categoría consultas el repositorio de funkos. **Es lo normal** |
    | **Con `@OneToMany`** | Si necesitas `categoria.getFunkos()` dentro de la lógica |

    Y si lo pones, **`mappedBy` es obligatorio**: sin él Hibernate crea una tabla intermedia `categorias_funkos` que no quieres, y los datos se escriben en un sitio y se leen de otro **sin ningún error**.

=== "3 · Nunca `cascade = ALL`"

    ```java
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)   // ❌
    ```

    Borrar la categoría `DISNEY` borraría **todos** los funkos de Disney, sin preguntar.

    Lo que quieres es lo contrario: que **no se pueda** borrar una categoría con funkos. Eso es un **409 comprobado en el servicio**, no una opción de la anotación.

    El resto de opciones de cascada y `orphanRemoval` están en la [referencia](03-referencias.md#34-relaciones).

!!! danger "El `@JsonIgnore` que evita el bucle infinito"
    Si pones las dos direcciones **y serializas entidades**, Jackson entra en bucle: funko → categoría → funkos → categoría → … hasta un `StackOverflowError`.

    **La solución de verdad no es `@JsonIgnore`: es no serializar entidades nunca.** Los controladores devuelven DTOs, y un DTO no tiene ciclos. Es la misma regla de la UT4, y aquí además te protege de esto.

!!! info "Las otras relaciones"
    `@OneToOne`, `@ManyToMany`, `@Embedded` y la herencia de entidades están en la [referencia del tema 3](03-referencias.md#34-relaciones) con su ejemplo.

    **En esta unidad no se usan**, y en el examen tampoco: con `@ManyToOne` y `@OneToMany` se modela todo lo que piden los enunciados. Una relación N:M siempre se puede escribir como dos 1:N con una entidad en medio, que además es lo que querrás el día que esa relación tenga datos propios.

---

## 1.4. El repositorio: aquí desaparece el código

```java title="funkos/repositories/FunkosRepository.java"
public interface FunkosRepository extends JpaRepository<Funko, Long> {

    // ── 1 · derivadas del nombre: ni una línea de implementación ──
    Optional<Funko> findByIdAndDeletedFalse(Long id);
    List<Funko>     findByDeletedFalse();
    boolean         existsByNombreIgnoreCaseAndDeletedFalse(String nombre);
    List<Funko>     findByCategoriaNombreIgnoreCaseAndDeletedFalse(String categoria);
    long            countByCategoriaIdAndDeletedFalse(Long categoriaId);

    // ── 2 · @Query con JPQL, cuando la derivada no llega ──
    @Query("""
           SELECT f.categoria.nombre, count(f)
             FROM Funko f
            WHERE f.deleted = false
            GROUP BY f.categoria.nombre
           """)
    List<Object[]> contarPorCategoria();

    // ── 3 · @Modifying para UPDATE y DELETE ──
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Funko f
              SET f.deleted = true, f.updatedAt = CURRENT_TIMESTAMP
            WHERE f.id = :id AND f.deleted = false
           """)
    int borradoLogico(@Param("id") Long id);
}
```

!!! success "Lo que trae `JpaRepository` gratis"
    | | |
    |---|---|
    | `findAll()` · `findAll(Pageable)` · `findAll(Sort)` | Listar, paginar y ordenar |
    | `findById(id)` → `Optional<T>` | Buscar por clave |
    | `save(e)` · `saveAll(…)` · `saveAndFlush(e)` | Insertar **y** actualizar |
    | `deleteById(id)` · `delete(e)` | Borrar |
    | `count()` · `existsById(id)` | Contar y comprobar |

    **`save` hace `INSERT` si el `id` es `null` y `UPDATE` si no lo es.** No hay dos métodos.

### Las tres formas de consultar, y cuándo cada una

| | Cómo | Cuándo |
|---|---|---|
| **1 · Derivada del nombre** | `findByCategoriaNombreIgnoreCaseAndDeletedFalse` | **Por defecto.** Mientras el nombre se lea |
| **2 · `@Query` con JPQL** | `GROUP BY`, `JOIN FETCH`, `UPDATE` | Cuando la derivada no llega |
| **3 · `Specification`** | Filtros opcionales y combinables | 3 o más filtros que se combinan. **[Tema 2](02-resultados-avanzados.md)** |

### Cómo se lee el nombre de un método

```
findBy  Categoria  Nombre  IgnoreCase  And  Deleted  False
  │         │        │         │        │      │       │
 verbo   campo de  campo de  sin      une   campo   valor
         Funko     Categoria  mayús.        de Funko
```

| Palabra | Qué genera |
|---|---|
| `findBy` / `existsBy` / `countBy` / `deleteBy` | `SELECT` / `SELECT count` / `DELETE` |
| `And` · `Or` | `AND` · `OR` |
| `IgnoreCase` | `UPPER(campo) = UPPER(?)` |
| `Containing` · `StartingWith` | `LIKE '%?%'` · `LIKE '?%'` |
| `GreaterThan` · `LessThanEqual` · `Between` | `>` · `<=` · `BETWEEN` |
| `IsNull` · `True` / `False` | `IS NULL` · `= true` / `= false` |
| `OrderBy<Campo>Desc` | `ORDER BY campo DESC` |
| **Un punto de navegación** (`CategoriaNombre`) | **`JOIN`** con la otra tabla |

!!! success "Compara con lo que escribiste en la UT4"
    ```java
    // UT4 · ~15 líneas
    @Override
    public List<Funko> findByCategoria(String categoria) {
        return jdbc.sql("""
                SELECT f.* FROM funkos f
                  JOIN categorias c ON c.id = f.categoria_id
                 WHERE UPPER(c.nombre) = UPPER(:categoria) AND f.is_deleted = false
                """)
            .param("categoria", categoria).query(rowMapper).list();
    }

    // UT5 · 1 línea, sin cuerpo
    List<Funko> findByCategoriaNombreIgnoreCaseAndDeletedFalse(String categoria);
    ```

    Y el SQL generado es **el mismo**:

    ```sql
    select f1_0.* from funkos f1_0
      join categorias c1_0 on c1_0.id = f1_0.categoria_id
     where upper(c1_0.nombre) = upper(?) and f1_0.is_deleted = false
    ```

    | | Líneas del repositorio |
    |---|:-:|
    | UT3 · JDBC a mano | ~120 |
    | UT4 · `JdbcClient` + `RowMapper` | ~95 |
    | **UT5 · Spring Data JPA** | **0** |

!!! danger "Y un nombre mal escrito impide ARRANCAR"
    ```
    PropertyReferenceException: No property 'categoría' found for type 'Funko'
    ```

    En la UT4, una columna mal escrita fallaba **al ejecutar esa consulta**, quizá en producción. Aquí falla **en tu pantalla, antes del primer `curl`**. Es la mejor propiedad de Spring Data y no se suele contar.

!!! warning "`@Modifying` necesita tres cosas"
    1. **`@Modifying`**, o salta `Not supported for DML operations`.
    2. **`@Transactional`** en quien lo llama, o `TransactionRequiredException`.
    3. **`clearAutomatically = true`** si en la misma transacción tenías la entidad cargada: un `UPDATE` por JPQL **no actualiza la caché de primer nivel**, y el objeto en memoria se queda con el valor viejo.

---

## 1.5. El servicio: el `save` que desaparece

```java title="funkos/services/FunkosServiceImpl.java"
@Override
@Transactional
public FunkoResponse update(Long id, FunkoUpdateRequest request) {
    var funko = repositorio.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new FunkoNotFoundException(id));

    mapper.parchear(funko, request);        // aplica los setters

    return mapper.toResponse(funko);        // ← sin save(): ya está persistido
}
```

!!! success "El *dirty checking*"
    Dentro de una transacción las entidades están **gestionadas**: Hibernate compara el estado al confirmar y lanza el `UPDATE` él solo. Llamar a `save()` no hace daño, pero **es innecesario**.

    Esto sorprende viniendo de la UT4, donde cada cambio era un `repositorio.update(...)` explícito. Y tiene su cara b: **un `setter` llamado por error dentro de una transacción persiste el cambio sin que nadie lo pida.**

| | `@Transactional` | Por qué |
|---|---|---|
| Lecturas | `@Transactional(readOnly = true)` | Hibernate se ahorra el control de cambios |
| Escrituras | `@Transactional` | Y **revierte entero** si algo falla |

!!! danger "Las tres trampas de `@Transactional`"
    | | Qué pasa |
    |---|---|
    | **Solo revierte con `RuntimeException`** | Una comprobada (`IOException`) **confirma** la transacción a medias. Si la capturas: `@Transactional(rollbackFor = Exception.class)` |
    | **Si capturas la excepción dentro, no hay *rollback*** | Tiene que **salir** del método transaccional |
    | **Funciona por proxy** | `this.metodo(...)` desde otro método del mismo bean **no abre transacción**. Igual que `@Cacheable` en la UT4 |

!!! info "El mapeador cambia; el resto de la capa, no"
    | | UT4 (`record`) | UT5 (`@Entity`) |
    |---|---|---|
    | Modificar | Crear otro objeto y guardarlo | **`setters` sobre el que ya está** |
    | `parchear` | Devolvía un `Funko` nuevo | **`void`** |
    | Guardar | `repositorio.update(nuevo)` | **Nada**: Hibernate lo detecta |

    **El controlador, los DTOs y el manejador de errores de la UT4 no se tocan.** Eso es lo que compraste con las capas.

---

## 1.6. Probar el repositorio

```java title="src/test/java/.../FunkosRepositoryTest.java"
@DataJpaTest
@ActiveProfiles("test")
class FunkosRepositoryTest {

    @Autowired FunkosRepository repositorio;
    @Autowired TestEntityManager em;

    private Categoria anime;

    @BeforeEach
    void preparar() {
        anime = em.persistAndFlush(new Categoria("ANIME"));
    }

    @Test
    void noDevuelveLosBorradosLogicamente() {
        em.persistAndFlush(new Funko("Goku", new BigDecimal("20.00"), 1, anime));

        var borrado = new Funko("Naruto", new BigDecimal("18.00"), 1, anime);
        borrado.setDeleted(true);
        em.persistAndFlush(borrado);
        em.clear();                                 // ← fuerza ir a la base de datos

        assertThat(repositorio.findByDeletedFalse())
                .extracting(Funko::getNombre)
                .containsExactly("Goku");
    }

    @Test
    void elNombreUnicoLoImponeLaBaseDeDatos() {
        em.persistAndFlush(new Funko("Goku", new BigDecimal("20.00"), 1, anime));

        assertThatThrownBy(() -> em.persistAndFlush(
                        new Funko("Goku", new BigDecimal("21.00"), 1, anime)))
                .isInstanceOf(PersistenceException.class);
    }
}
```

!!! success "Qué hace `@DataJpaTest`"
    - Levanta **solo** la capa de persistencia: ni controladores ni servicios. Tarda ~1 segundo.
    - Usa la base **en memoria** del perfil `test`.
    - **Cada test va en su transacción y se revierte al acabar**: no hay que limpiar nada.
    - Da el `TestEntityManager` para preparar datos sin pasar por tu repositorio.

!!! danger "El `em.clear()` no es decorativo"
    Sin él lees el objeto de la **caché de primer nivel** —el que acabas de guardar, en memoria— y el test pasa aunque el mapeo esté mal. `em.clear()` vacía esa caché y fuerza una consulta de verdad.

    Es el fallo típico: un test en verde con una entidad mal mapeada.

!!! info "Los tres niveles de test de la unidad"
    | | Qué prueba | Tarda | Caza |
    |---|---|---|---|
    | **Mockito** | Reglas de negocio | ms | Un 409 que falta |
    | **`@DataJpaTest`** | Mapeo, relaciones y consultas | ~1 s | Un `mappedBy` mal, un `AndDeletedFalse` olvidado |
    | **`@SpringBootTest`** | Todo junto | ~3 s | El cableado y **las transacciones** |

---

## Casos especiales

### `LazyInitializationException`

```
could not initialize proxy [Categoria#1] - no Session
```

Has llamado a `funko.getCategoria().getNombre()` **fuera de la transacción**: la sesión ya estaba cerrada y el proxy no puede ir a buscar el dato.

| Solución | Veredicto |
|---|---|
| Mapear a DTO **dentro** del servicio `@Transactional` | ✅ **Es la correcta** |
| `JOIN FETCH` o `@EntityGraph` en la consulta | ✅ Cuando necesitas la relación para una lista |
| Poner `EAGER` | ❌ Arregla un caso y empeora todos los demás |
| `open-in-view=true` | ❌ Lo esconde: el error vuelve en producción |

**Por eso el mapeador se llama desde el servicio y nunca desde el controlador.**

### El borrado lógico, y lo que cuesta

| | Cómo | Consecuencia |
|---|---|---|
| **Físico** | `repositorio.deleteById(id)` | El dato desaparece; las filas que lo referencian se rompen |
| **Lógico** | `deleted = true` | El dato sigue. **Todas** las consultas llevan `AndDeletedFalse` |

**Olvidar un `AndDeletedFalse` en una sola consulta** hace que los registros borrados reaparezcan en ese endpoint, y el programa **no falla**: nadie se entera hasta que un cliente ve algo que ya no existe.

Por eso **todos** los métodos del repositorio llevan el sufijo en el nombre: es feo y es deliberado, porque un método que no lo lleve **se ve a simple vista**.

Y lo que no cubre: con datos personales, el borrado lógico **no cumple el derecho al olvido**. Ahí hay que borrar de verdad.

### El `enum` guardado como número

```java
@Enumerated(EnumType.ORDINAL)      // ❌ el valor por defecto
private Categoria categoria;
```

Guarda **la posición** del valor en el `enum`: 0, 1, 2. El día que alguien reordene el `enum` o inserte un valor en medio, **todos los registros existentes pasan a significar otra cosa**, sin un solo error.

```java
@Enumerated(EnumType.STRING)       // ✅ siempre
private Estado estado;
```

Guarda `"ACTIVO"`. Ocupa más y es legible desde cualquier cliente SQL.

### Dos ediciones a la vez: `@Version`

```java
@Version
private Long version;
```

Hibernate añade `AND version = ?` a cada `UPDATE` e incrementa el número. Si otro ya lo había cambiado, el `UPDATE` afecta a **0 filas** y lanza `ObjectOptimisticLockingFailureException`, que se traduce a un **409**.

**Sin esto, el último en guardar pisa los cambios del primero y nadie se entera.** Se llama *lost update*, y es el clásico de un panel con dos personas trabajando.

### `findById` frente a `getReferenceById`

| | Qué devuelve | Cuándo |
|---|---|---|
| `findById(id)` | `Optional<T>`, **va a la base de datos** | **Casi siempre** |
| `getReferenceById(id)` | Un **proxy** sin consultar | Solo para asignar una relación: `funko.setCategoria(repo.getReferenceById(3L))` |

Con `getReferenceById`, si el id no existe el error no salta en esa línea, sino **después**, al usar el proxy, y con un `EntityNotFoundException` difícil de situar. En la duda, `findById`.

### `@Query` con JPQL o con SQL nativo

```java
// JPQL: habla de ENTIDADES y CAMPOS Java
@Query("SELECT f FROM Funko f WHERE f.categoria.nombre = :c")
List<Funko> porCategoria(@Param("c") String categoria);

// Nativo: habla de TABLAS y COLUMNAS
@Query(value = "SELECT * FROM funkos WHERE categoria_id = :id", nativeQuery = true)
List<Funko> porCategoriaId(@Param("id") Long id);
```

| | JPQL | `nativeQuery = true` |
|---|---|---|
| Nombres | Entidad y campos Java | Tabla y columnas SQL |
| Portable entre motores | **Sí** | No |
| Se valida al arrancar | **Sí** | No: falla al ejecutarse |
| Funciones propias del motor | No | **Sí** |

**Usa JPQL.** El nativo, solo para algo que JPQL no sepa expresar, y sabiendo que pierdes la portabilidad y la validación al arrancar — que es justo lo que has venido a buscar al ORM.
