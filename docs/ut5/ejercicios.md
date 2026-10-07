# Batería de ejercicios — UT5

**26 ejercicios con solución**, en el orden de los temas y de menos a más dentro de cada bloque.

| | |
|:-:|---|
| ● | Cinco minutos, con el tema delante |
| ●● | Hay que juntar dos ideas |
| ●●● | Se escribe, se ejecuta y se mira el log |

!!! tip "Ten `show-sql=true` puesto todo el rato"
    La mitad de los ejercicios de esta unidad se contestan **leyendo el SQL que genera Hibernate**. Es la única forma de ver un N+1, y es lo que se mira en la defensa.

    ```properties
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    ```

---

# Bloque 1 · Configuración y entidades

> Tema [1. Spring Data con JPA y SQL](01-spring-data-jpa-sql.md) §1.1–1.3

## E1 ● — `ddl-auto`

¿Qué hace cada valor y cuál va en cada entorno?

??? success "Solución"

    | Valor | Qué hace | Dónde |
    |---|---|---|
    | `none` | Nada | Producción con migraciones |
    | `validate` | Comprueba el esquema; si no cuadra, **no arranca** | **Producción** |
    | `update` | Añade lo que falta, nunca borra ni modifica | En ningún sitio serio |
    | `create` | Borra y crea al arrancar | Tests |
    | `create-drop` | Borra y crea al arrancar, y borra al parar | **Desarrollo** |

    **`create-drop` en producción borra la base de datos en cada reinicio.**

    Y `update` es el que engaña: parece prudente, pero adivina, no sabe renombrar (crea una columna nueva y deja la vieja con los datos dentro) y no tiene vuelta atrás.

## E2 ● — `Table "FUNKOS" not found`

Pones un `data.sql` y la aplicación falla al arrancar.

??? success "Solución"

    Spring ejecuta `data.sql` **antes** de que Hibernate cree las tablas.

    ```properties
    spring.jpa.defer-datasource-initialization=true
    spring.sql.init.mode=always
    ```

    La primera línea retrasa la inicialización de datos hasta después del DDL. Es el error número uno de este bloque, y el mensaje no da ninguna pista de la causa.

## E3 ● — Por qué una entidad no puede ser un `record`

??? success "Solución"

    Porque JPA construye las entidades **por reflexión** y necesita:

    1. Un **constructor sin argumentos** (al menos `protected`).
    2. Poder **asignar los campos** después de construir el objeto.
    3. Que la clase **no sea `final`**, porque Hibernate crea subclases proxy para el *lazy loading*.

    Un `record` es `final` y sus campos son inmutables, así que falla en las tres.

    **Los DTOs sí son `record`**: esos nunca pasan por JPA. Es la razón técnica de la separación entidad/DTO, además de las de diseño.

## E4 ●● — `@Enumerated`

```java
@Enumerated
private Categoria categoria;
```

¿Qué está mal?

??? success "Solución"

    Por defecto es **`EnumType.ORDINAL`**, que guarda **la posición** del valor: `DISNEY`=0, `MARVEL`=1, `ANIME`=2.

    El día que alguien añada un valor en medio o reordene el `enum`, **todos los registros cambian de significado en silencio**. Los funkos de Disney pasan a ser de Marvel. No hay error, no hay log, no hay forma de detectarlo mirando el programa.

    ```java
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Categoria categoria;
    ```

    Ocupa más y es inmune. **Siempre `STRING`.**

## E5 ●● — `nullable = false` y `@NotBlank`

¿Son lo mismo? ¿Hay que poner los dos?

??? success "Solución"

    **No son lo mismo, y se ponen los dos.**

    | | Dónde actúa | Cuándo falla | Qué ve el cliente |
    |---|---|---|---|
    | `@NotBlank` | En el **DTO** | Al validar la petición | **400** con el campo señalado |
    | `nullable = false` | En la **columna** | Al hacer `INSERT` | **500** con un mensaje de Hibernate |

    El primero da el error limpio; el segundo es la red de seguridad para cuando el dato llega por otra vía (un `data.sql`, un script, otro servicio).

    Solo el del DTO → un camino que no pase por el controlador mete `null` en la tabla.
    Solo el de JPA → el cliente recibe un 500 ilegible.

## E6 ●● — Marcas temporales sin repetirlas

Tienes tres entidades, todas con `createdAt` y `updatedAt`. ¿Cómo lo haces una vez?

??? success "Solución"

    ```java
    @MappedSuperclass
    public abstract class Auditable {

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

        @Column(name = "updated_at")
        private LocalDateTime updatedAt = LocalDateTime.now();

        @PreUpdate
        void alActualizar() { this.updatedAt = LocalDateTime.now(); }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    @Entity @Table(name = "funkos")
    public class Funko extends Auditable { … }
    ```

    **`@MappedSuperclass` no crea tabla**: sus columnas se añaden a las de cada hija.

    Y el `updatable = false` en `createdAt` no es decoración: impide que un `setter` llamado por error cambie la fecha de creación.

---

# Bloque 2 · Relaciones

> Tema [1. Spring Data con JPA y SQL](01-spring-data-jpa-sql.md) §1.4

## E7 ● — Dónde va la clave ajena

Una categoría tiene muchos funkos. ¿En qué tabla está la clave ajena y quién es el lado dueño?

??? success "Solución"

    En la tabla **`funkos`**, porque cada funko tiene **una** categoría. Así que el **lado dueño es el `@ManyToOne`**:

    ```java
    // En Funko — DUEÑO
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // En Categoria — INVERSO
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Funko> funkos = new ArrayList<>();
    ```

    **La regla:** en una 1:N, la clave ajena está siempre en el lado **muchos**, y ese es el dueño.

## E8 ● — El `mappedBy` olvidado

```java
@OneToMany
private List<Funko> funkos;
```

??? success "Solución"

    Sin `mappedBy`, Hibernate **no sabe** que la relación ya está mapeada desde el otro lado, así que crea **una tabla intermedia** `categorias_funkos` con dos columnas.

    Resultado: tienes la clave ajena en `funkos` **y** una tabla de unión, los datos se guardan en una y se leen de la otra, y las consultas devuelven listas vacías sin ningún error.

    ```java
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Funko> funkos = new ArrayList<>();
    ```

    `mappedBy = "categoria"` dice: «el dueño es el campo `categoria` de `Funko`».

## E9 ●● — `LAZY` o `EAGER`

¿Cuál es el valor por defecto de cada relación y cuál deberías poner?

??? success "Solución"

    | Relación | Por defecto | Lo correcto |
    |---|---|---|
    | `@OneToOne` | **`EAGER`** | `LAZY` |
    | `@ManyToOne` | **`EAGER`** | `LAZY` |
    | `@OneToMany` | `LAZY` | `LAZY` |
    | `@ManyToMany` | `LAZY` | `LAZY` |

    **Los dos por defecto están mal**, y es el problema de rendimiento número uno de JPA.

    Con `@ManyToOne` en `EAGER`, un `findAll()` de 100 funkos trae 100 categorías. Y si `Categoria` tuviera un `@ManyToOne Proveedor` también en `EAGER`, 100 proveedores más.

    Siempre `LAZY`, y cuando sí necesitas la relación, la pides explícitamente (E11).

## E10 ●● — `cascade = ALL`

```java
@OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
private List<Funko> funkos;
```

¿Qué pasa al borrar la categoría `DISNEY`?

??? success "Solución"

    **Se borran todos los funkos de Disney**, sin preguntar y sin error.

    Casi nunca es lo que quieres. Lo que quieres suele ser **que no se pueda borrar**:

    ```java
    long funkos = funkosRepository.countByCategoriaIdAndDeletedFalse(id);
    if (funkos > 0)
        throw new CategoriaConflictException(
                "No se puede borrar: tiene %d funkos asociados".formatted(funkos));
    ```

    **409 Conflict**, y sin cascada.

    `CascadeType.ALL` + `orphanRemoval` tiene sentido en una **composición** real: un `Pedido` y sus `LineaPedido`, donde una línea no existe sin su pedido. Ahí sí.

## E11 ●●● — El N+1

Listas 20 funkos con la categoría en `LAZY` y el log muestra 21 consultas.

??? success "Solución"

    Una consulta para la lista y **una por cada funko** cuando el mapeador toca `getCategoria()`. Es el problema **N+1**.

    No se arregla con `EAGER` (eso lo convierte en N+1 **siempre**, también donde no hace falta). Se arregla pidiendo el `JOIN`:

    ```java
    @Query(value      = "SELECT f FROM Funko f JOIN FETCH f.categoria WHERE f.deleted = false",
           countQuery = "SELECT count(f) FROM Funko f WHERE f.deleted = false")
    Page<Funko> findAllConCategoria(Pageable pageable);
    ```

    O más limpio:

    ```java
    @EntityGraph(attributePaths = {"categoria"})
    Page<Funko> findByDeletedFalse(Pageable pageable);
    ```

    **El `countQuery` es obligatorio** con `JOIN FETCH` + `Pageable`. Sin él, Hibernate avisa:

    ```
    HHH90003004: firstResult/maxResults specified with collection fetch;
    applying in memory
    ```

    Es decir: **trae la tabla entera y pagina en memoria**, que es exactamente lo que querías evitar.

## E12 ●●● — `LazyInitializationException`

```java
@GetMapping("/{id}")
public Funko uno(@PathVariable Long id) {
    return repositorio.findById(id).orElseThrow();
}
```

??? success "Solución"

    ```
    org.hibernate.LazyInitializationException: could not initialize proxy - no Session
    ```

    La transacción se cierra al salir del repositorio. Cuando Jackson serializa y toca `categoria`, el proxy perezoso ya no tiene sesión.

    **Tres formas de "arreglarlo", y solo una es buena:**

    | | Qué hace | Veredicto |
    |---|---|---|
    | `fetch = EAGER` | Carga siempre | Mal: N+1 garantizado |
    | `open-in-view=true` | Mantiene la sesión durante la serialización | Mal: esconde el problema y lanza consultas desde el serializador |
    | **Devolver un DTO** | El mapeador lee lo que hace falta **dentro** de la transacción | **Bien** |

    ```java
    @GetMapping("/{id}")
    public ResponseEntity<FunkoResponse> uno(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.findById(id));
    }
    ```

    Y por eso `spring.jpa.open-in-view=false` va puesto desde el primer día: para que el error **salte en desarrollo**.

## E13 ●●● — La recursión infinita

`Funko` tiene `@ManyToOne Categoria` y `Categoria` tiene `@OneToMany List<Funko>`. Devuelves la entidad. Tres soluciones y cuál es la buena.

??? success "Solución"

    ```
    com.fasterxml.jackson.databind.JsonMappingException:
    Infinite recursion (StackOverflowError)
    ```

    1. **DTOs** — `FunkoResponse` lleva `String categoria`, no el objeto. **El bucle no existe.**
    2. `@JsonManagedReference` / `@JsonBackReference` — marca un lado como «no serialices la vuelta». Funciona, pero mete anotaciones de la capa web en la entidad.
    3. `@JsonIgnore` en el lado inverso — lo mismo y más tosco: ese campo desaparece para **todas** las respuestas.

    **La (1).** Las otras dos ponen decisiones de presentación dentro del modelo de datos, que es justo lo que la UT4 separó. Y la (1) resuelve a la vez el E12.

## E14 ●● — `@ManyToMany` o entidad intermedia

Un alumno cursa varios módulos y cada matrícula tiene **nota y fecha**. ¿`@ManyToMany`?

??? success "Solución"

    **No.** En cuanto la relación tiene **datos propios**, deja de ser un `@ManyToMany` y pasa a ser una **entidad**:

    ```java
    @Entity
    @Table(name = "matriculas",
           uniqueConstraints = @UniqueConstraint(
                   columnNames = {"alumno_id", "modulo_id", "curso"}))
    public class Matricula {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "alumno_id") private Alumno alumno;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "modulo_id") private Modulo modulo;

        @Column(nullable = false, length = 9) private String curso;
        @Column(precision = 4, scale = 2)     private BigDecimal nota;
    }
    ```

    Una tabla de unión que Hibernate gestiona **no puede tener columnas extra**, no se puede consultar y no se puede paginar.

    El `@ManyToMany` puro vale para etiquetas: «este artículo tiene estas etiquetas», y nada más.

---

# Bloque 3 · Repositorios y consultas

> Tema [1. Spring Data con JPA y SQL](01-spring-data-jpa-sql.md) §1.5–1.6

## E15 ● — Lo que ya viene hecho

¿Qué métodos tienes al extender `JpaRepository<Funko, Long>`?

??? success "Solución"

    `findAll()` · `findAll(Pageable)` · `findAll(Sort)` · `findById(id)` → **`Optional`** · `findAllById` · `save` · `saveAll` · `saveAndFlush` · `deleteById` · `delete` · `deleteAll` · `existsById` · `count` · `flush` · `getReferenceById`

    **Y nada de esto lo escribes tú.** Spring Data crea un proxy en el arranque.

    `findById` devuelve `Optional` desde el primer día, que es por lo que la UT2 insistía tanto.

## E16 ●● — Consultas derivadas

Escribe la firma para: (a) por categoría ordenados por precio descendente · (b) precio entre dos valores · (c) nombre que contenga un texto, sin distinguir mayúsculas · (d) contar los de una categoría · (e) los 5 más caros no borrados

??? success "Solución"

    ```java
    List<Funko> findByCategoriaOrderByPrecioDesc(String categoria);
    List<Funko> findByPrecioBetween(BigDecimal min, BigDecimal max);
    List<Funko> findByNombreContainingIgnoreCase(String texto);
    long        countByCategoria(String categoria);
    List<Funko> findTop5ByDeletedFalseOrderByPrecioDesc();
    ```

    Y la ventaja grande: si escribes mal un campo, **la aplicación no arranca**:

    ```
    PropertyReferenceException: No property 'categoría' found for type 'Funko'
    ```

    Mucho mejor que un error en producción tres semanas después.

## E17 ●● — Navegar por la relación

Quieres los funkos cuya **categoría se llame** `ANIME`, sin distinguir mayúsculas. Sin escribir SQL.

??? success "Solución"

    ```java
    List<Funko> findByCategoriaNombreIgnoreCaseAndDeletedFalse(String nombre);
    ```

    ```sql
    select f1_0.* from funkos f1_0
      join categorias c1_0 on c1_0.id = f1_0.categoria_id
     where upper(c1_0.nombre) = upper(?) and f1_0.is_deleted = false
    ```

    **Spring Data ha escrito el `JOIN`** leyendo el nombre del método: `Categoria` → `Nombre`.

    Y cuando el nombre se hace ilegible (`findByCategoriaNombreIgnoreCaseAndPrecioBetweenAndDeletedFalseOrderByPrecioDesc`), es la señal de que toca `@Query` o `Specification`.

## E18 ●● — JPQL o SQL nativo

¿Cuándo cada uno?

??? success "Solución"

    ```java
    // JPQL: entidades y campos Java
    @Query("SELECT f FROM Funko f WHERE f.categoria.nombre = :cat AND f.cantidad > 0")
    List<Funko> conStock(@Param("cat") String cat);

    // Nativo: tablas y columnas
    @Query(value = "SELECT * FROM funkos WHERE categoria_id = :id", nativeQuery = true)
    List<Funko> porCategoriaNativo(@Param("id") Long id);
    ```

    | | JPQL | Nativo |
    |---|---|---|
    | Habla de | Entidades | Tablas |
    | Portable | **Sí** | No |
    | Se valida al arrancar | **Sí** | No: falla al ejecutar |
    | Funciones propias del motor | No | Sí |

    **JPQL por defecto.** Nativo solo para lo que JPQL no expresa: una función del motor, una ventana (`OVER`), un `UPSERT`, una búsqueda de texto completo.

    Y en los dos: **nunca concatenes**. Los `:param` son parámetros preparados; concatenar es la inyección SQL de la [UT3, E22](../ut3/ejercicios.md).

## E19 ●● — `@Modifying`

```java
@Query("UPDATE Funko f SET f.deleted = true WHERE f.id = :id")
int borradoLogico(@Param("id") Long id);
```

¿Qué falta?

??? success "Solución"

    **`@Modifying`**, o salta:

    ```
    org.hibernate.query.IllegalMutationQueryException:
    Not supported for DML operations
    ```

    Y **`@Transactional`** en quien lo llama, o salta `TransactionRequiredException`.

    ```java
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Funko f SET f.deleted = true, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :id")
    int borradoLogico(@Param("id") Long id);
    ```

    El `clearAutomatically` es el detalle fino: un `UPDATE` por JPQL **no pasa por la caché de primer nivel**, así que si en la misma transacción tenías el funko cargado, ese objeto sigue con `deleted = false` en memoria.

## E20 ●●● — `Specification` para filtros combinables

`?categoria=&precioMin=&precioMax=&nombre=`, todos opcionales. ¿Con `if` o con `Specification`?

??? success "Solución"

    Con cuatro filtros opcionales hay **16 combinaciones**. Un método por cada una es inviable.

    ```java
    public interface FunkosRepository
            extends JpaRepository<Funko, Long>, JpaSpecificationExecutor<Funko> { }
    ```

    ```java
    Specification<Funko> spec = (root, q, cb) -> cb.isFalse(root.get("deleted"));

    spec = categoria.map(c -> spec.and((root, q, cb) ->
            cb.equal(cb.upper(root.get("categoria").get("nombre")), c.toUpperCase())))
            .orElse(spec);

    spec = precioMin.map(p -> spec.and((root, q, cb) ->
            cb.greaterThanOrEqualTo(root.get("precio"), p))).orElse(spec);

    spec = precioMax.map(p -> spec.and((root, q, cb) ->
            cb.lessThanOrEqualTo(root.get("precio"), p))).orElse(spec);

    spec = nombre.map(n -> spec.and((root, q, cb) ->
            cb.like(cb.lower(root.get("nombre")), "%" + n.toLowerCase() + "%")))
            .orElse(spec);

    return repositorio.findAll(spec, pageable).map(mapper::toResponse);
    ```

    Cada filtro **se añade solo si viene**, y el SQL final solo lleva las condiciones presentes. Los `Optional` son los de la UT2.

    Y el `.map(mapper::toResponse)` del final: `Page` tiene `map`, así que la paginación se conserva y salen DTOs.

## E21 ●●● — `@DataJpaTest`

Escribe un test que compruebe que los borrados lógicamente no aparecen.

??? success "Solución"

    ```java
    @DataJpaTest
    @ActiveProfiles("test")
    class FunkosRepositoryTest {

        @Autowired FunkosRepository repositorio;
        @Autowired TestEntityManager em;

        @Test
        void noDevuelveLosBorradosLogicamente() {
            var categoria = em.persistAndFlush(new Categoria("ANIME"));
            em.persistAndFlush(funko("Goku",   categoria, false));
            em.persistAndFlush(funko("Naruto", categoria, true));

            var resultado = repositorio.findByDeletedFalse();

            assertThat(resultado).extracting(Funko::getNombre)
                                 .containsExactly("Goku");
        }
    }
    ```

    **`@DataJpaTest` hace tres cosas:**

    1. Levanta **solo** la capa JPA: ni controladores ni servicios. Menos de un segundo.
    2. Usa la base de datos en memoria del perfil de test.
    3. Cada test va en **una transacción que se deshace al acabar**: no hay que limpiar nada.

    Y el `persistAndFlush`: sin el `flush`, el `INSERT` se queda en la caché de primer nivel y la consulta del repositorio no lo ve.

---

# Bloque 4 · Paginación, transacciones y producción

> Tema [2. Resultados avanzados](02-resultados-avanzados.md) y [3. Proyecto](03-proyecto-completo.md)

## E22 ● — `Page`, `Slice` o `List`

¿Qué devuelve cada uno y cuál cuesta más?

??? success "Solución"

    | | Qué trae | Consultas |
    |---|---|:-:|
    | `List<T>` | Solo los elementos | **1** |
    | `Slice<T>` | Elementos + «¿hay más?» | **1** (pide `size+1`) |
    | `Page<T>` | Elementos + `totalElements` + `totalPages` | **2** |

    **`Page` lanza un `SELECT count(*)` extra**, y sobre una tabla grande no es gratis.

    Si la interfaz tiene «página 3 de 47», necesitas `Page`. Si es un *scroll* infinito con «cargar más», `Slice` basta y cuesta la mitad.

## E23 ●● — Los dos peligros de `?sort=` y `?size=`

??? success "Solución"

    **`?size=1000000`** carga la tabla entera: has escrito la paginación para nada.

    ```properties
    spring.data.web.pageable.max-page-size=100
    spring.data.web.pageable.default-page-size=20
    ```

    **`?sort=cualquierCampo`** permite ordenar por **cualquier propiedad de la entidad**, incluidas las que no publicas. Con `?sort=passwordHash`, no recibes el hash pero sí **el orden relativo** de los hashes.

    Y `?sort=inventado` provoca un `PropertyReferenceException` → **500**, cuando debería ser 400.

    ```java
    private Pageable sanear(Pageable p) {
        var ok = Set.of("id", "nombre", "precio", "cantidad", "createdAt");
        var orden = p.getSort().stream()
                     .filter(o -> ok.contains(o.getProperty())).toList();
        return PageRequest.of(p.getPageNumber(),
                              Math.min(p.getPageSize(), 100),
                              orden.isEmpty() ? Sort.by("id") : Sort.by(orden));
    }
    ```

    **Lista blanca, no negra**: un campo nuevo en la entidad no debe quedar ordenable por omisión.

## E24 ●● — `@Transactional` que no revierte

```java
@Transactional
public void procesar() {
    try {
        repositorio.save(algo);
        otroServicio.fallar();
    } catch (Exception e) {
        log.error("ups", e);
    }
}
```

??? success "Solución"

    **La transacción se confirma**, con `algo` guardado y el resto del trabajo sin hacer.

    Dos razones, y las dos hay que saberlas:

    1. **La excepción se captura dentro.** Para que haya *rollback*, tiene que **salir** del método transaccional.
    2. **Por defecto solo revierte con `RuntimeException`.** Si `fallar()` lanzara una `IOException` y no la capturaras, la transacción **también se confirmaría**. Para eso: `@Transactional(rollbackFor = Exception.class)`.

    Y la tercera trampa, que no está en este código: `@Transactional` funciona **por proxy**, así que `this.procesar()` llamado desde otro método del mismo bean **no abre transacción**. Igual que `@Cacheable` en la UT4.

## E25 ●● — `@Version`

¿Para qué sirve y qué excepción lanza?

??? success "Solución"

    ```java
    @Version
    private Long version;
    ```

    Hibernate añade `AND version = ?` a cada `UPDATE` e incrementa el número. Si otro ya lo había cambiado, el `UPDATE` afecta a **0 filas** y lanza:

    ```
    org.springframework.orm.ObjectOptimisticLockingFailureException
    ```

    **Sin esto, el último en guardar pisa los cambios del primero** y nadie se entera. Se llama *lost update* y es el bug silencioso clásico de un panel con dos personas trabajando.

    Se traduce a un **409 Conflict** con un «alguien ha modificado este registro, recarga y vuelve a intentarlo».

## E26 ●●● — De H2 a MySQL

Tu proyecto funciona con H2. Pásalo a MySQL en Docker. ¿Qué tocas?

??? success "Solución"

    **El perfil y el `compose.yaml`. Ni una línea de código.**

    ```properties title="application-prod.properties"
    spring.datasource.url=${DB_URL}
    spring.datasource.username=${DB_USER}
    spring.datasource.password=${DB_PASSWORD}
    spring.jpa.hibernate.ddl-auto=validate
    ```

    ```yaml title="compose.yaml"
    services:
      api:
        environment:
          SPRING_PROFILES_ACTIVE: prod
          DB_URL: jdbc:mysql://db:3306/funkos     # ← "db", no localhost
        depends_on:
          db: { condition: service_healthy }      # ← sin esto, falla al arrancar
      db:
        image: mysql:8.4
        healthcheck:
          test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
          interval: 5s
          retries: 10
    ```

    **Los dos errores de este paso:**

    1. **`localhost` en vez de `db`.** Dentro de la red de Compose, la API ve la base de datos por el **nombre del servicio**. `localhost` desde el contenedor de la API es el propio contenedor de la API.
    2. **Sin `condition: service_healthy`**, `depends_on` solo espera a que el contenedor arranque, no a que MySQL acepte conexiones. Resultado: `Communications link failure`.

    Es el mismo aviso de la [UT3, E25](../ut3/ejercicios.md).

    Y la comprobación de que la unidad ha servido para algo:

    ```bash
    curl -X POST … -d '{"nombre":"Pikachu",…}'
    docker compose restart api && sleep 15
    curl -s "localhost:8080/api/v1/funkos?nombre=pika" | jq '.totalElements'
    # 1   ← en la UT4 esto era 0
    ```

---

## Reparto sugerido

| Sesión | Bloque | Ejercicios |
|:-:|---|---|
| **S1** | 1 · Configuración | E1 – E2 |
| **S2** | 1 · Entidades | E3 – E6 |
| **S3** | 2 · Relaciones | E7 – E8 |
| **S4** | 2 · `LAZY` y cascada | E9 – E10 |
| **S5** | 2 · N+1, *lazy* y recursión | E11 – E14 |
| **S6** | 3 · Repositorios y consultas | E15 – E20 |
| **S7** | 3 · Testing | E21 |
| **S8** | 4 · Paginación | E22 – E23 |
| **S9** | 4 · Transacciones y producción | E24 – E26 |
| **S14–S19** | — | [Retos](retos.md) |
| **S20** | — | Examen práctico |

!!! success "Si vas justo de tiempo"
    El mínimo: **E1, E4, E7, E9, E11, E12, E15, E19, E23, E24**.

    Y los cuatro que separan un 5 de un 9, porque son los que **no fallan hasta que hay volumen**: **E11** (el N+1), **E12** (`LazyInitializationException`), **E10** (la cascada que borra) y **E24** (la transacción que no revierte).

    Los cuatro compilan, arrancan y pasan cualquier prueba manual.
