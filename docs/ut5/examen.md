# Examen práctico — UT5 (RA6)

!!! danger "Esta página ES el examen"
    **El examen de la UT5 es uno de estos tres enunciados, con otro dominio.** Mismo formato, mismo tamaño, misma rúbrica, y **el mismo material dado**.

    Es **el examen de la UT4 más JPA**: las mismas capas, los mismos códigos de estado y las mismas reglas de negocio, pero con el modelo como entidad y el repositorio generado por Spring Data.

    La [rúbrica](#rubrica-de-calificacion) del final es la misma con la que se corrige. Léela **antes** de escribir la primera entidad: cuatro de los diez puntos se deciden en el `LAZY`, el `@EntityGraph` y el `@Transactional`.

## Qué se te da y qué escribes tú

!!! success "Se te da, ya hecho"
    | | Qué |
    |---|---|
    | **El proyecto** | `pom.xml` con `data-jpa`, H2 y PostgreSQL, los `application*.properties` de los perfiles y la clase de arranque |
    | **Los datos** | `data.sql` (y su versión para PostgreSQL) |
    | **Los mapeadores** | **Completos**: `toResponse`, `toModel`, `reemplazar` y `parchear` |

    **No hay `schema.sql`:** el esquema lo genera Hibernate desde tus entidades.

!!! danger "Escribes tú"
    | | Qué | Por qué está aquí |
    |---|---|---|
    | **1** | **La estructura de carpetas por capas** | Es la base del RA5, y se da por sabida |
    | **2** | **Las entidades**, con sus anotaciones y su relación | **Es lo que se evalúa del RA6** |
    | **3** | **Los DTOs** con sus validaciones | La entrada y la salida |
    | **4** | **Los repositorios JPA**: derivadas, una `@Query` y el `@EntityGraph` | El corazón de la unidad |
    | **5** | **Las excepciones** con `@ResponseStatus` | Los códigos de error |
    | **6** | **El servicio** con las reglas y los `@Transactional` | La mitad de la nota |
    | **7** | **El controlador** con `Pageable` saneado y el `@RestControllerAdvice` | La presentación |
    | **8** | **Dos tests**: uno `@DataJpaTest` y uno de servicio | |

!!! warning "Los mapeadores que recibes SON la especificación de las entidades"
    Vienen escritos, **pero no compilan todavía**, porque usan clases que tú no has creado.

    ```java
    public LibroResponse toResponse(Libro l) {
        return new LibroResponse(l.getId(), l.getIsbn(), …, l.getSocio().getNombre(), …);
    }                            │                           │
    //                           │                           └── hay una RELACIÓN a Socio
    //                           └── la entidad tiene getId(), getIsbn()…
    ```

    **De ahí sale:** cómo se llama cada entidad, qué `getters` y `setters` necesita, **qué relaciones hay y en qué dirección**, y qué DTOs hacen falta con qué componentes.

    Léelos enteros y dibuja el modelo **antes** de escribir una línea. Quince minutos ahí te ahorran una hora de peleas con el compilador.

| | Enunciado | Entidades | La dificultad |
|:-:|---|:-:|---|
| **E1** | [Biblioteca](#e1-biblioteca) | 2 | Relación 1:N, borrado lógico y paginación |
| **E2** | [Instituto](#e2-instituto) | 2 | **El N+1 medido** y el `@EntityGraph` |
| **E3** | [Tienda](#e3-tienda) | 2 | **Transacción de dos entidades** y `@Version` |

!!! success "Reparto de las cuatro horas"
    | | Qué | Tiempo |
    |:-:|---|:-:|
    | 1 | **Leer los mapeadores** y dibujar el modelo | 15 min |
    | 2 | **Las entidades y su relación** | 45 min |
    | 3 | DTOs con validaciones y excepciones | 30 min |
    | 4 | **Los repositorios**: derivadas, `@Query` y `@EntityGraph` | 35 min |
    | 5 | **El servicio con las cinco reglas** y los `@Transactional` | 70 min |
    | 6 | Controlador con `Pageable` saneado y el advice | 30 min |
    | 7 | Dos tests | 25 min |
    | 8 | Probar con `curl` y **contar las consultas** del log | 20 min |
    | | | **4 h 10** |

    **Si vas justo, el orden de lo que se abandona** es: el segundo test, el PATCH y el filtro combinado. Nunca el `LAZY`, el `@EntityGraph` ni el `@Transactional`.

---

## E1 · Biblioteca

Los libros de una biblioteca y los socios que los tienen prestados.

### El mapeador, que se te da

```java title="libros/mappers/LibroMapper.java"
@Component
public class LibroMapper {

    /** Se lee getSocio() DENTRO de la transacción: la relación es LAZY. */
    public LibroResponse toResponse(Libro l) {
        return new LibroResponse(
                l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(),
                l.getEjemplares(),
                l.getSocio() == null ? null : l.getSocio().getNombre(),
                l.getCreatedAt(), l.getUpdatedAt());
    }

    public List<LibroResponse> toResponse(List<Libro> libros) {
        return libros.stream().map(this::toResponse).toList();
    }

    /** POST: entidad nueva, sin socio. */
    public Libro toModel(LibroCreateRequest r) {
        return new Libro(r.isbn().trim(), r.titulo().trim(),
                         r.autor().trim(), r.ejemplares());
    }

    /** PUT: reemplaza los campos editables. */
    public void reemplazar(Libro libro, LibroCreateRequest r) {
        libro.setTitulo(r.titulo().trim());
        libro.setAutor(r.autor().trim());
        libro.setEjemplares(r.ejemplares());
    }

    /** PATCH: solo lo que llega distinto de null. El ISBN no se cambia. */
    public void parchear(Libro libro, LibroUpdateRequest r) {
        if (r.titulo()     != null) libro.setTitulo(r.titulo().trim());
        if (r.autor()      != null) libro.setAutor(r.autor().trim());
        if (r.ejemplares() != null) libro.setEjemplares(r.ejemplares());
    }
}
```

```java title="socios/mappers/SocioMapper.java"
@Component
public class SocioMapper {

    public SocioResponse toResponse(Socio s) {
        return new SocioResponse(s.getId(), s.getNombre(), s.getEmail(), s.getPrestamos());
    }

    public Socio toModel(SocioCreateRequest r) {
        return new Socio(r.nombre().trim(), r.email().trim().toLowerCase());
    }
}
```

!!! success "Lo que el mapeador te está diciendo"
    | Lo que ves | Lo que tienes que escribir |
    |---|---|
    | `new Libro(isbn, titulo, autor, ejemplares)` | Constructor con **esos 4**, más el `protected` sin argumentos |
    | `l.getSocio()` puede ser `null` | `@ManyToOne(optional = true)` hacia `Socio` |
    | `libro.setTitulo/setAutor/setEjemplares` | Esos tres `setters`, y **ningún otro** |
    | No hay `setIsbn` ni `setId` | El ISBN **no se cambia** después de crear |
    | `new Socio(nombre, email)` | Constructor con 2, y `getPrestamos()` arranca en 0 |
    | `LibroResponse(… , socioNombre, …)` | **8 componentes**, y el del socio es el **nombre**, no el id |

### Los endpoints que escribes

```
GET    /api/v1/libros            ?page= &size= &sort= &autor= &disponibles=true
GET    /api/v1/libros/{id}
POST   /api/v1/libros
PATCH  /api/v1/libros/{id}
DELETE /api/v1/libros/{id}        ← borrado lógico
POST   /api/v1/libros/{id}/prestar   { "socioId": 3 }

GET    /api/v1/socios             CRUD
GET    /api/v1/socios/{id}/libros
```

### Las reglas de negocio

| | Regla | Si falla |
|:-:|---|:-:|
| **R1** | `isbn` de 13 caracteres; `titulo` y `autor` no vacíos; `ejemplares` ≥ 0; `email` con formato válido | **400** |
| **R2** | El `isbn` y el `email` **no se repiten** | **409** |
| **R3** | Prestar **descuenta un ejemplar** y **suma 1** al contador del socio, o ninguna de las dos | **409** si no quedan ejemplares |
| **R4** | Un socio no puede tener **más de 3 libros** prestados | **409** |
| **R5** | Un libro con socio **no se puede borrar**; el borrado es **lógico** | **409** |
| **R6** | Un `id` que no existe, en cualquier endpoint | **404** |

### Lo que se evalúa del RA6, concretamente

```java title="Lo que se busca en tu repositorio"
public interface LibrosRepository
        extends JpaRepository<Libro, Long>, JpaSpecificationExecutor<Libro> {

    Optional<Libro> findByIdAndDeletedFalse(Long id);
    boolean         existsByIsbnAndDeletedFalse(String isbn);
    long            countBySocioIdAndDeletedFalse(Long socioId);

    /** Para el listado: el socio en la MISMA consulta. */
    @EntityGraph(attributePaths = {"socio"})
    Page<Libro> findByDeletedFalse(Pageable pageable);

    /** Descuenta un ejemplar solo si queda alguno. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Libro l SET l.ejemplares = l.ejemplares - 1
            WHERE l.id = :id AND l.ejemplares >= 1
           """)
    int descontarEjemplar(@Param("id") Long id);
}
```

!!! danger "Los tres sitios donde se gana o se pierde este enunciado"
    **1 · El `@EntityGraph`.** El mapeador lee `getSocio()`, así que listar 20 libros sin él son **21 consultas**. Se corrige contando las líneas `Hibernate:` del log.

    **2 · El `@Transactional` de la R3.** Descontar el ejemplar y subir el contador del socio van juntos. Se corrige provocando el fallo y comparando **las dos tablas** antes y después.

    **3 · El `== 0`.** El `UPDATE … WHERE ejemplares >= 1` es lo que impide el ejemplar negativo. Si en su lugar lees, compruebas y escribes, el ítem es 0 aunque funcione en tu portátil.

---

## E2 · Instituto

Alumnos matriculados en un curso, con sus notas.

### El mapeador, que se te da

```java title="alumnos/mappers/AlumnoMapper.java"
@Component
public class AlumnoMapper {

    public AlumnoResponse toResponse(Alumno a) {
        return new AlumnoResponse(
                a.getId(), a.getExpediente(), a.getNombre(), a.getApellidos(),
                a.getNota(), a.getCurso().getCodigo(), a.getCurso().getNombre(),
                a.isActivo());
    }

    public List<AlumnoResponse> toResponse(List<Alumno> alumnos) {
        return alumnos.stream().map(this::toResponse).toList();
    }

    /** El curso lo resuelve el servicio a partir del código que manda el cliente. */
    public Alumno toModel(AlumnoCreateRequest r, Curso curso) {
        return new Alumno(r.expediente().trim().toUpperCase(),
                          r.nombre().trim(), r.apellidos().trim(), curso);
    }

    public void parchear(Alumno alumno, AlumnoUpdateRequest r, Curso curso) {
        if (r.nombre()    != null) alumno.setNombre(r.nombre().trim());
        if (r.apellidos() != null) alumno.setApellidos(r.apellidos().trim());
        if (curso         != null) alumno.setCurso(curso);
    }
}
```

```java title="cursos/mappers/CursoMapper.java"
@Component
public class CursoMapper {

    public CursoResponse toResponse(Curso c) {
        return new CursoResponse(c.getId(), c.getCodigo(), c.getNombre(),
                                 c.getPlazas(), c.getMatriculados());
    }

    public Curso toModel(CursoCreateRequest r) {
        return new Curso(r.codigo().trim().toUpperCase(), r.nombre().trim(), r.plazas());
    }
}
```

!!! warning "Fíjate en lo que NO está en el mapeador"
    - **No hay `setNota`.** La nota se pone por su propio endpoint, no por el PATCH.
    - **No hay `setExpediente`.** El expediente es inmutable.
    - **No hay `setMatriculados`.** Ese contador lo lleva el servicio con una `@Query`, no un `setter`.
    - **`toModel` recibe el `Curso` ya resuelto.** El servicio lo busca por el **código** que manda el cliente: `"2DAW"`, no un `id`.

    Y `toResponse` toca `getCurso()` **dos veces**: el `@EntityGraph` aquí no es opcional.

### Los endpoints que escribes

```
GET    /api/v1/alumnos          ?page= &size= &sort= &curso= &apellidos= &notaMin=
GET    /api/v1/alumnos/{id}
POST   /api/v1/alumnos
PATCH  /api/v1/alumnos/{id}
DELETE /api/v1/alumnos/{id}      ← borrado lógico (activo = false)
PUT    /api/v1/alumnos/{id}/nota   { "nota": 7.5 }

GET    /api/v1/cursos            CRUD
GET    /api/v1/cursos/{id}/alumnos
GET    /api/v1/cursos/estadisticas    ← nota media por curso, con una @Query
```

### Las reglas de negocio

| | Regla | Si falla |
|:-:|---|:-:|
| **R1** | `expediente` de 4 a 12 caracteres; `nombre` y `apellidos` no vacíos; `plazas` > 0; `nota` entre 0 y 10 | **400** |
| **R2** | El `expediente` y el `codigo` del curso **no se repiten** | **409** |
| **R3** | Matricular **suma 1** al contador del curso; **no se puede pasar de `plazas`** | **409** |
| **R4** | Un curso con alumnos activos **no se puede borrar** | **409** |
| **R5** | Las estadísticas las calcula **la base de datos**, no un `stream` en Java | — |
| **R6** | Un `id` o un código de curso que no existe | **404** |

### La R5 y el N+1

```java title="La consulta de las estadísticas"
@Query("""
       SELECT a.curso.codigo, count(a), avg(a.nota)
         FROM Alumno a
        WHERE a.activo = true AND a.nota IS NOT NULL
        GROUP BY a.curso.codigo
        ORDER BY a.curso.codigo
       """)
List<Object[]> estadisticasPorCurso();
```

!!! danger "Lo que NO vale"
    ```java
    var alumnos = repositorio.findAll();                       // ❌ trae la tabla entera
    var media = alumnos.stream()
            .collect(groupingBy(a -> a.getCurso().getCodigo(),
                     averagingDouble(Alumno::getNota)));       // ❌ y un N+1 de propina
    ```

    Con 5.000 alumnos eso son **5.001 consultas** y toda la tabla en memoria, para devolver cuatro números.

    **`count` y `avg` los hace la base de datos.** Es el ítem que separa a quien ha entendido el ORM de quien lo usa como un cargador de listas.

!!! success "Cómo se corrige el N+1, exactamente"
    ```bash
    curl -s "localhost:8080/api/v1/alumnos?size=20" > /dev/null
    ```

    Y se cuentan las líneas `Hibernate:` del log:

    | | Nota del ítem |
    |---|---|
    | **2** (la página y el `count`) | Total |
    | **3–4** | Parcial |
    | **21** | **Cero** |

---

## E3 · Tienda

Productos de una categoría, con ventas y concurrencia.

### El mapeador, que se te da

```java title="productos/mappers/ProductoMapper.java"
@Component
public class ProductoMapper {

    public ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getSku(), p.getNombre(), p.getPrecio(), p.getStock(),
                p.getCategoria().getNombre(), p.getCreatedAt(), p.getUpdatedAt());
    }

    public List<ProductoResponse> toResponse(List<Producto> productos) {
        return productos.stream().map(this::toResponse).toList();
    }

    public Producto toModel(ProductoCreateRequest r, Categoria categoria) {
        return new Producto(r.sku().trim().toUpperCase(), r.nombre().trim(),
                            r.precio(), r.stock(), categoria);
    }

    public void reemplazar(Producto p, ProductoCreateRequest r, Categoria categoria) {
        p.setNombre(r.nombre().trim());
        p.setPrecio(r.precio());
        p.setStock(r.stock());
        p.setCategoria(categoria);
    }

    public void parchear(Producto p, ProductoUpdateRequest r, Categoria categoria) {
        if (r.nombre()  != null) p.setNombre(r.nombre().trim());
        if (r.precio()  != null) p.setPrecio(r.precio());
        if (r.stock()   != null) p.setStock(r.stock());
        if (categoria   != null) p.setCategoria(categoria);
    }
}
```

```java title="categorias/mappers/CategoriaMapper.java"
@Component
public class CategoriaMapper {

    public CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getUnidadesVendidas());
    }

    public Categoria toModel(CategoriaCreateRequest r) {
        return new Categoria(r.nombre().trim().toUpperCase());
    }
}
```

### Los endpoints que escribes

```
GET    /api/v1/productos         ?page= &size= &sort= &categoria= &nombre= &conStock=
GET    /api/v1/productos/{id}
POST   /api/v1/productos
PUT    /api/v1/productos/{id}
PATCH  /api/v1/productos/{id}
DELETE /api/v1/productos/{id}     ← borrado lógico
POST   /api/v1/productos/{id}/venta   { "cantidad": 3 }

GET    /api/v1/categorias         CRUD
```

### Las reglas de negocio

| | Regla | Si falla |
|:-:|---|:-:|
| **R1** | `sku` de 4 a 20 caracteres; `nombre` no vacío; `precio` > 0; `stock` ≥ 0; `cantidad` ≥ 1 | **400** |
| **R2** | El `sku` **no se repite** | **409** |
| **R3** | Vender **descuenta stock** y **suma al contador de la categoría**, o ninguna de las dos | **409** si no hay stock |
| **R4** | El stock **nunca** puede quedar negativo, ni con peticiones simultáneas | **409** |
| **R5** | Dos ediciones simultáneas del mismo producto: la segunda **falla** | **409** |
| **R6** | Un `id` o una categoría que no existe | **404** / **400** |

### La R5: el bloqueo optimista

```java title="En la entidad"
@Version
private Long version;
```

```java title="Y el handler que lo traduce"
@ResponseStatus(HttpStatus.CONFLICT)
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public Map<String, Object> conflictoDeEdicion(ObjectOptimisticLockingFailureException ex) {
    return Map.of("status", 409,
            "error", "Otro usuario ha modificado este producto. Recarga y reinténtalo.");
}
```

!!! success "Cómo se comprueba"
    ```bash
    curl -s -X PATCH localhost:8080/api/v1/productos/1 \
      -H "Content-Type: application/json" -d '{"nombre":"A"}' &
    curl -s -X PATCH localhost:8080/api/v1/productos/1 \
      -H "Content-Type: application/json" -d '{"nombre":"B"}' &
    wait
    ```

    **Uno de los dos devuelve 409.** Sin `@Version`, los dos devuelven 200 y uno de los cambios **se pierde en silencio**: eso es un *lost update*.

!!! danger "Y la R4, con el bucle de 20 peticiones"
    ```bash
    # 20 ventas de 1 unidad sobre un producto con 10 de stock
    for i in $(seq 1 20); do
      curl -s -o /dev/null -w "%{http_code} " -X POST \
        localhost:8080/api/v1/productos/1/venta \
        -H "Content-Type: application/json" -d '{"cantidad":1}' &
    done; wait; echo
    ```

    **Diez 200, diez 409, y el stock en 0.** Si sale negativo, has leído-comprobado-escrito en vez de usar `UPDATE … WHERE stock >= :cantidad`.

    Este bucle es parte de la corrección, y en el examen se lanza **contra PostgreSQL**, donde la concurrencia es real.

---

## ¿Cabe en cuatro horas?

**Sí.** Los mapeadores dados quitan unas **90 líneas** y, más importante, **eliminan las dudas de diseño**: no hay que decidir qué DTOs hacer ni qué campos lleva cada uno.

| | Ficheros que escribes | Líneas aprox. |
|---|:-:|:-:|
| Entidades (2, con la relación) | 2 | 110 |
| DTOs (`Response`, `Create`, `Update`, el de la operación) ×2 | 7 | 80 |
| Repositorios (2 interfaces, **sin cuerpo**) | 2 | 30 |
| Excepciones (3) | 3 | 25 |
| Servicios (interfaz + impl) ×2 | 4 | 130 |
| Controladores (2) | 2 | 90 |
| `@RestControllerAdvice` | 1 | 40 |
| Tests (`@DataJpaTest` + servicio) | 2 | 50 |
| | **~23** | **~555** |

!!! success "Lo que se recortó respecto a versiones anteriores de estos enunciados"
    | | Antes | Ahora |
    |---|:-:|:-:|
    | **Los mapeadores** | Los escribía el alumno | **Dados** |
    | Entidades | 3–5 | **2** |
    | Relaciones | 1:N, N:M con datos propios | **Solo 1:N** |
    | Endpoints | 10–11 | **7 + el CRUD de la segunda entidad** |
    | Reglas de negocio | 7 | **5 + el 404** |
    | Tests exigidos | 3 | **2** |

    **Las N:M desaparecen.** Con `@ManyToOne` y `@OneToMany` se demuestra el RA6 entero, y una relación N:M con datos propios son dos 1:N con una entidad en medio: más volumen, el mismo criterio.

---

## Rúbrica de calificación

**La misma para los tres enunciados y para el examen.** Sobre 10 puntos.

### Bloque 1 · Las entidades y el mapeo — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 1.1 | Las entidades **compilan con los mapeadores dados sin tocarlos**: constructores, `getters` y `setters` exactos | **0,75** |
| 1.2 | `@Entity`, `@Table`, `@Id` con `@GeneratedValue(IDENTITY)` y `@Column` con `nullable`, `unique` y `length` donde el enunciado los pide | 0,5 |
| 1.3 | **El constructor `protected` sin argumentos** y uno con los campos del cliente. **Ningún builder** | 0,5 |
| 1.4 | La relación con `@ManyToOne` + `@JoinColumn`, y el `mappedBy` si pones el lado inverso | 0,5 |
| 1.5 | `BigDecimal` con `precision`/`scale` para el dinero, y `@Enumerated(STRING)` si usas un `enum` | 0,25 |

### Bloque 2 · Rendimiento: `LAZY` y el N+1 — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 2.1 | `grep -rn "FetchType.EAGER" src/main/java/` → **vacío** | 0,5 |
| 2.2 | Listar 20 elementos son **2 consultas**, no 21. Se cuentan las líneas `Hibernate:` | **1,0** |
| 2.3 | Si usas `JOIN FETCH` con `Page`, está el **`countQuery`**; si usas `@EntityGraph`, no hace falta | 0,25 |
| 2.4 | `spring.jpa.open-in-view=false` y **ningún** `LazyInitializationException` en el log | 0,25 |

### Bloque 3 · Repositorios y consultas — 1,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 3.1 | Los repositorios son **interfaces sin implementación**. `grep -rln "implements .*Repository" src/main/java/` → **vacío** | 0,5 |
| 3.2 | Los filtros van por **consultas derivadas** o `Specification`, no filtrando en Java con `stream` | 0,5 |
| 3.3 | El agregado (contar, media) se hace con **`@Query`**, no cargando la tabla en memoria | 0,25 |
| 3.4 | El `UPDATE` masivo lleva `@Modifying` **y** `clearAutomatically = true` | 0,25 |

### Bloque 4 · Capas, DTOs y validación — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 4.1 | `grep -rn "ResponseEntity\|HttpStatus" src/main/java/**/services/` → **vacío** | 0,5 |
| 4.2 | Ningún controlador devuelve ni recibe una **entidad**: solo DTOs, y son `record` | 0,5 |
| 4.3 | Todos los `@RequestBody` llevan **`@Valid`**, un POST inválido da **400** y los errores salen **campo a campo** | 0,5 |
| 4.4 | El `Pageable` está **saneado**: lista blanca de campos y tope de tamaño. `?sort=inventado` **no da 500** | 0,5 |

### Bloque 5 · Integridad y transacciones — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 5.1 | La operación de las dos entidades lleva **`@Transactional`**, y se demuestra con el estado de **las dos tablas** antes y después de un fallo | **0,75** |
| 5.2 | El contador no puede quedar negativo: **`UPDATE … WHERE … >=`** y mirar las filas afectadas | 0,5 |
| 5.3 | Las lecturas llevan `@Transactional(readOnly = true)` | 0,25 |
| 5.4 | Excepciones propias con `@ResponseStatus` y un `@RestControllerAdvice` que traduzca `DataIntegrityViolationException` a **409** | 0,5 |

### Bloque 6 · Configuración y persistencia — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 6.1 | `ddl-auto`: **`update`** en dev, **`validate`** en prod, **`create-drop`** en test | 0,4 |
| 6.2 | Los datos **sobreviven** a parar y arrancar, y arranca **dos veces seguidas** sin error | 0,3 |
| 6.3 | **Arranca también con PostgreSQL** (`SPRING_PROFILES_ACTIVE=postgres`) y `git diff src/main/java/` sale **vacío** | 0,3 |

### Bloque 7 · Tests — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 7.1 | `./mvnw clean test` acaba en **BUILD SUCCESS**. Si no compila o hay tests comentados → **0 en el bloque** | 0,3 |
| 7.2 | **Un `@DataJpaTest`** del borrado lógico o de la restricción única, **con `em.clear()`** | 0,4 |
| 7.3 | **Un test de servicio** con Mockito de una de las reglas de negocio | 0,3 |

!!! danger "Penalizaciones — se restan del total"
    | | |
    |---|:-:|
    | Haber modificado los mapeadores dados para que encajen con tus entidades | **−1,5** |
    | Un `FetchType.EAGER` en cualquier relación | **−1,0** |
    | Serializar entidades en lugar de DTOs | **−1,0** |
    | Cargar una tabla entera para filtrar o contar en Java | **−1,0** |
    | `cascade = ALL` o `orphanRemoval` en el lado inverso sin que el enunciado lo pida | **−0,5** |
    | `ddl-auto=create-drop` en `dev` (se pierden los datos) | **−0,5** |
    | `equals`/`hashCode` generados por el IDE en una entidad | **−0,25** |
    | `System.out.println` en lugar de un `Logger` | **−0,25** |

!!! success "Nota mínima"
    Se aprueba con **5,0**, y **el bloque 2 no puede quedar en 0**: el RA6 habla de *«gestionar la persistencia de forma eficiente»*, y una API con un N+1 en el listado principal no lo es.

    | Bloque | Peso |
    |---|:-:|
    | 1 · Entidades y mapeo | 25 % |
    | 2 · `LAZY` y N+1 | 20 % |
    | 3 · Repositorios | 15 % |
    | 4 · Capas, DTOs y validación | 20 % |
    | 5 · Integridad y transacciones | 20 % |
    | 6 · Configuración | 10 % |
    | 7 · Tests | 10 % |

    Suman **12 puntos de criterios sobre un máximo de 10**, lo que permite compensar un bloque flojo.

!!! info "Orden de corrección"
    1. `git diff` de los mapeadores → bloque 1.1 y la penalización gorda.
    2. `./mvnw clean test` → bloque 7.1.
    3. **Los tres `grep`** (`EAGER`, `ResponseEntity` en servicios, `implements …Repository`) → **medio punto por `grep` y treinta segundos cada uno**.
    4. `show-sql=true`, listar 20 elementos y **contar las líneas `Hibernate:`** → bloque 2.2.
    5. Las peticiones del enunciado → bloques 4 y 5.
    6. `?sort=inventado` y `?size=99999` → bloque 4.4.
    7. Provocar el fallo de la operación transaccional y mirar **las dos tablas** → bloque 5.1.
    8. El bucle de 20 peticiones simultáneas → bloque 5.2.
    9. Parar, arrancar, y arrancar con `postgres` → bloque 6.

    Se entrega comprimido **sin `target/` ni `datos/`**, con un `README.md` de diez líneas que diga cómo arrancar con cada motor.
