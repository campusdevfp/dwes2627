# Examen práctico — UT4 (RA5)

!!! danger "Esta página ES el examen"
    **El examen de la UT4 es uno de estos tres enunciados, con otro dominio.** Mismo formato, mismo tamaño, misma rúbrica, y **el mismo material dado**.

    Resuelve los tres y habrás hecho el examen tres veces. Lo que cambia el día del examen son los nombres de las tablas y las reglas, no lo que hay que escribir.

    La [rúbrica](#rubrica-de-calificacion) del final es **la misma** con la que se te va a corregir. Léela **antes** de empezar.

## Qué se te da y qué escribes tú

!!! success "Se te da, ya hecho y funcionando"
    | | Qué |
    |---|---|
    | **El proyecto** | `pom.xml`, `application.properties` de los tres perfiles y la clase de arranque |
    | **La base de datos** | `schema.sql` y `data.sql`, con las tablas creadas y datos de prueba |
    | **El repositorio** | **La interfaz, la implementación con `JdbcClient` y el `RowMapper`** |
    | **El mapeador** | Completo: `toResponse`, `toModel` y `parchear` |

    El proyecto que recibes **compila y arranca**. Lo que no tiene son las clases que faltan.

!!! danger "Escribes tú"
    | | Qué | Por qué está aquí |
    |---|---|---|
    | **1** | **La estructura de carpetas por capas** | Es el RA5 |
    | **2** | **Los modelos** (`record`) | El repositorio y el mapeador los usan |
    | **3** | **Los DTOs** con sus validaciones | La entrada y la salida de la API |
    | **4** | **Las excepciones** con `@ResponseStatus` | Los códigos de error |
    | **5** | **El servicio** (interfaz + implementación) con las reglas | Es la mitad de la nota |
    | **6** | **El controlador** y el `@RestControllerAdvice` | La capa de presentación |
    | **7** | **Dos tests** | Uno de servicio, uno de capa web |

!!! warning "El repositorio y el mapeador que recibes SON la especificación"
    Vienen escritos, **pero no compilan todavía**, porque usan clases que tú no has creado aún.

    ```java
    public Funko findById(Long id)              // ← te dice que el modelo se llama Funko
    public FunkoResponse toResponse(Funko f)    // ← y el DTO de salida, FunkoResponse
    return new Funko(r.nombre().trim(), …)      // ← y el orden de los campos del record
    ```

    **Lee esos dos ficheros antes de escribir una sola línea.** De ahí sale el nombre de cada clase, el orden de los componentes de cada `record` y qué DTOs hacen falta. El primer error del examen es inventarse nombres y pelearse después con el compilador.

| | Enunciado | Tablas | La dificultad |
|:-:|---|:-:|---|
| **E1** | [Biblioteca](#e1-biblioteca) | 2 | Regla que depende de **otra tabla** |
| **E2** | [Reservas de aulas](#e2-reservas-de-aulas) | 2 | **Detección de solapes** con `<` y no `<=` |
| **E3** | [Taller mecánico](#e3-taller-mecanico) | 2 | **Máquina de estados** |

!!! success "Reparto de las cuatro horas"
    | | Qué | Tiempo |
    |:-:|---|:-:|
    | 1 | **Leer el repositorio y el mapeador** y apuntar los nombres | 15 min |
    | 2 | Carpetas, modelos y DTOs | 40 min |
    | 3 | Excepciones y `@RestControllerAdvice` | 25 min |
    | 4 | **El servicio con las cinco reglas** | 80 min |
    | 5 | El controlador | 35 min |
    | 6 | Dos tests | 25 min |
    | 7 | Probar con `curl` y repasar la rúbrica | 20 min |
    | | | **4 h** |

    **Si vas justo, el orden de lo que se abandona** es: el segundo test, el PATCH y el filtro combinado. Nunca las capas ni la validación: ahí están 4,5 de los 10 puntos.

---

## E1 · Biblioteca

Una API para gestionar los libros de una biblioteca y sus préstamos a socios.

### El esquema, que se te da

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS socios (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    prestamos   INT          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS libros (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn        VARCHAR(20)  NOT NULL UNIQUE,
    titulo      VARCHAR(200) NOT NULL,
    autor       VARCHAR(150) NOT NULL,
    ejemplares  INT          NOT NULL,
    socio_id    BIGINT       NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_libro_socio FOREIGN KEY (socio_id) REFERENCES socios (id)
);
```

### El repositorio, que se te da

```java title="libros/repositories/LibrosRepository.java — la interfaz"
public interface LibrosRepository {
    List<Libro> findAll();
    List<Libro> findByAutorContainsIgnoreCase(String autor);
    Optional<Libro> findById(Long id);
    Optional<Libro> findByIsbn(String isbn);
    Libro save(Libro libro);
    Libro update(Libro libro);
    void deleteById(Long id);

    /** Descuenta un ejemplar SOLO si queda alguno. Devuelve las filas afectadas. */
    int descontarEjemplar(Long id);
    /** Suma uno al contador de préstamos del socio. */
    int sumarPrestamo(Long socioId);
    long contarPrestadosDe(Long socioId);
}
```

```java title="libros/repositories/LibrosRepositoryJdbc.java — la implementación"
@Repository
public class LibrosRepositoryJdbc implements LibrosRepository {

    private static final String SELECT = "SELECT * FROM libros";

    private final JdbcClient jdbc;
    private final RowMapper<Libro> rowMapper = new LibroRowMapper();

    public LibrosRepositoryJdbc(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Libro> findAll() {
        return jdbc.sql(SELECT + " ORDER BY id").query(rowMapper).list();
    }

    @Override
    public List<Libro> findByAutorContainsIgnoreCase(String autor) {
        return jdbc.sql(SELECT + " WHERE UPPER(autor) LIKE UPPER(:autor) ORDER BY id")
                .param("autor", "%" + autor + "%")
                .query(rowMapper).list();
    }

    @Override
    public Optional<Libro> findById(Long id) {
        return jdbc.sql(SELECT + " WHERE id = :id")
                .param("id", id).query(rowMapper).optional();
    }

    @Override
    public Optional<Libro> findByIsbn(String isbn) {
        return jdbc.sql(SELECT + " WHERE isbn = :isbn")
                .param("isbn", isbn).query(rowMapper).optional();
    }

    @Override
    public Libro save(Libro libro) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO libros (isbn, titulo, autor, ejemplares, socio_id,
                                    created_at, updated_at)
                VALUES (:isbn, :titulo, :autor, :ejemplares, :socioId,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)
                .param("isbn", libro.isbn())
                .param("titulo", libro.titulo())
                .param("autor", libro.autor())
                .param("ejemplares", libro.ejemplares())
                .param("socioId", libro.socioId())
                .update(keys);
        return findById(keys.getKey().longValue()).orElseThrow();
    }

    @Override
    public Libro update(Libro libro) {
        jdbc.sql("""
                UPDATE libros
                   SET isbn = :isbn, titulo = :titulo, autor = :autor,
                       ejemplares = :ejemplares, socio_id = :socioId,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE id = :id
                """)
                .param("id", libro.id())
                .param("isbn", libro.isbn())
                .param("titulo", libro.titulo())
                .param("autor", libro.autor())
                .param("ejemplares", libro.ejemplares())
                .param("socioId", libro.socioId())
                .update();
        return findById(libro.id()).orElseThrow();
    }

    @Override
    public void deleteById(Long id) {
        jdbc.sql("DELETE FROM libros WHERE id = :id").param("id", id).update();
    }

    @Override
    public int descontarEjemplar(Long id) {
        return jdbc.sql("""
                UPDATE libros
                   SET ejemplares = ejemplares - 1, updated_at = CURRENT_TIMESTAMP
                 WHERE id = :id AND ejemplares >= 1
                """).param("id", id).update();
    }

    @Override
    public int sumarPrestamo(Long socioId) {
        return jdbc.sql("UPDATE socios SET prestamos = prestamos + 1 WHERE id = :id")
                .param("id", socioId).update();
    }

    @Override
    public long contarPrestadosDe(Long socioId) {
        return jdbc.sql("SELECT count(*) FROM libros WHERE socio_id = :id")
                .param("id", socioId).query(Long.class).single();
    }
}
```

```java title="libros/repositories/LibroRowMapper.java"
@Component
public class LibroRowMapper implements RowMapper<Libro> {
    @Override
    public Libro mapRow(ResultSet rs, int n) throws SQLException {
        return new Libro(
                rs.getLong("id"),
                rs.getString("isbn"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("ejemplares"),
                rs.getObject("socio_id", Long.class),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
```

### El mapeador, que se te da

```java title="libros/mappers/LibroMapper.java"
@Component
public class LibroMapper {

    public LibroResponse toResponse(Libro l) {
        return new LibroResponse(l.id(), l.isbn(), l.titulo(), l.autor(),
                l.ejemplares(), l.socioId(), l.createdAt(), l.updatedAt());
    }

    public List<LibroResponse> toResponse(List<Libro> libros) {
        return libros.stream().map(this::toResponse).toList();
    }

    public Libro toModel(LibroCreateRequest r) {
        return new Libro(null, r.isbn().trim(), r.titulo().trim(), r.autor().trim(),
                r.ejemplares(), null, LocalDateTime.now(), LocalDateTime.now());
    }

    /** PATCH: cambia solo lo que llega distinto de null. */
    public Libro parchear(Libro original, LibroUpdateRequest r) {
        return new Libro(
                original.id(),
                original.isbn(),                                        // el ISBN no se cambia
                r.titulo()     != null ? r.titulo().trim() : original.titulo(),
                r.autor()      != null ? r.autor().trim()  : original.autor(),
                r.ejemplares() != null ? r.ejemplares()    : original.ejemplares(),
                original.socioId(),
                original.createdAt(),
                LocalDateTime.now());
    }
}
```

!!! success "De ahí sale todo lo que tienes que escribir"
    | Lo que ves en el código dado | Lo que tienes que crear |
    |---|---|
    | `new Libro(id, isbn, titulo, autor, ejemplares, socioId, createdAt, updatedAt)` | `record Libro` con **esos 8 componentes en ese orden** |
    | `new LibroResponse(…)` | `record LibroResponse` con los mismos 8 |
    | `r.isbn()`, `r.titulo()`, `r.autor()`, `r.ejemplares()` | `record LibroCreateRequest` con esos 4 |
    | `r.titulo()`, `r.autor()`, `r.ejemplares()` | `record LibroUpdateRequest` con esos 3 |

    El `socioId` del modelo es `Long` y **admite `null`**: un libro sin prestar no tiene socio.

### Los endpoints que escribes

```
GET    /api/v1/libros              ?autor=
GET    /api/v1/libros/{id}
POST   /api/v1/libros
PATCH  /api/v1/libros/{id}
DELETE /api/v1/libros/{id}
POST   /api/v1/libros/{id}/prestar     { "socioId": 3 }
```

### Las reglas de negocio

| | Regla | Si falla |
|:-:|---|:-:|
| **R1** | `isbn` obligatorio y de 13 caracteres; `titulo` y `autor` no vacíos; `ejemplares` ≥ 0 | **400** |
| **R2** | El `isbn` **no puede repetirse** | **409** |
| **R3** | Un libro con `socio_id` **no se puede borrar** | **409** |
| **R4** | Prestar **descuenta un ejemplar**, y solo si queda alguno | **409** si no quedan |
| **R5** | Un socio **no puede tener más de 3 libros** prestados a la vez | **409** |
| **R6** | Un `id` que no existe, en cualquier endpoint | **404** |

### R4 y R5, que son el corazón del ejercicio

```java title="Lo que tienes que escribir en el servicio"
@Override
@Transactional
public LibroResponse prestar(Long id, PrestamoRequest request) {

    var libro = repositorio.findById(id)
            .orElseThrow(() -> new LibroNotFoundException(id));

    // R5 · el tope del socio, ANTES de tocar nada
    if (repositorio.contarPrestadosDe(request.socioId()) >= 3)
        throw new LibroConflictException(
                "El socio %d ya tiene 3 libros prestados".formatted(request.socioId()));

    // R4 · descontar de forma atómica: 0 filas = no quedaban
    if (repositorio.descontarEjemplar(id) == 0)
        throw new LibroConflictException(
                "No quedan ejemplares de " + libro.titulo());

    repositorio.sumarPrestamo(request.socioId());
    // …y marcar el socio_id del libro

    return mapper.toResponse(repositorio.findById(id).orElseThrow());
}
```

!!! danger "Lo que se corrige aquí"
    - **El `@Transactional`**: si el `sumarPrestamo` falla, el ejemplar descontado tiene que volver.
    - **El `== 0`**: el repositorio ya trae el `UPDATE … WHERE ejemplares >= 1`. Lo que se corrige es que **mires el valor devuelto** en vez de hacer «leer, comprobar, escribir».

### Lo que se prueba

```bash
curl -s localhost:8080/api/v1/libros | jq length
curl -s "localhost:8080/api/v1/libros?autor=delibes" | jq '[.[].titulo]'

curl -i -X POST localhost:8080/api/v1/libros -H "Content-Type: application/json" \
  -d '{"isbn":"9788437604947","titulo":"El Jarama","autor":"Sánchez Ferlosio","ejemplares":2}'
# 201 + Location

# R2 · el mismo ISBN otra vez → 409
# R1 · ISBN de 5 caracteres → 400 con el error del campo
# R4 · prestar hasta agotar ejemplares → el último da 409
# R5 · cuatro préstamos al mismo socio → el cuarto da 409
# R3 · borrar un libro prestado → 409
```

---

## E2 · Reservas de aulas

### El esquema, que se te da

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS aulas (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo      VARCHAR(10)  NOT NULL UNIQUE,
    plazas      INT          NOT NULL,
    activa      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS reservas (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    aula_id     BIGINT       NOT NULL,
    profesor    VARCHAR(100) NOT NULL,
    dia         DATE         NOT NULL,
    hora_inicio INT          NOT NULL,
    hora_fin    INT          NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_aula FOREIGN KEY (aula_id) REFERENCES aulas (id)
);
```

### El repositorio, que se te da

La implementación con `JdbcClient` y el `ReservaRowMapper` **vienen escritos**, con la misma estructura que el E1. Esta es la interfaz, que es lo que tienes que leer:

```java title="reservas/repositories/ReservasRepository.java"
public interface ReservasRepository {
    List<Reserva> findAll();
    List<Reserva> findByDia(LocalDate dia);
    List<Reserva> findByAulaId(Long aulaId);
    Optional<Reserva> findById(Long id);
    Reserva save(Reserva reserva);
    Reserva update(Reserva reserva);
    void deleteById(Long id);

    /** Las reservas de esa aula y ese día, para comprobar los solapes. */
    List<Reserva> findByAulaIdAndDia(Long aulaId, LocalDate dia);
    Optional<Aula> findAulaById(Long aulaId);
    long contarDe(String profesor, LocalDate dia);
}
```

### El mapeador, que se te da

```java title="reservas/mappers/ReservaMapper.java"
@Component
public class ReservaMapper {

    public ReservaResponse toResponse(Reserva r, String aulaCodigo) {
        return new ReservaResponse(r.id(), aulaCodigo, r.profesor(), r.dia(),
                r.horaInicio(), r.horaFin(), r.createdAt(), r.updatedAt());
    }

    public Reserva toModel(ReservaCreateRequest r) {
        return new Reserva(null, r.aulaId(), r.profesor().trim(), r.dia(),
                r.horaInicio(), r.horaFin(), LocalDateTime.now(), LocalDateTime.now());
    }

    public Reserva parchear(Reserva original, ReservaUpdateRequest r) {
        return new Reserva(
                original.id(),
                original.aulaId(),
                r.profesor()   != null ? r.profesor().trim() : original.profesor(),
                r.dia()        != null ? r.dia()             : original.dia(),
                r.horaInicio() != null ? r.horaInicio()      : original.horaInicio(),
                r.horaFin()    != null ? r.horaFin()         : original.horaFin(),
                original.createdAt(),
                LocalDateTime.now());
    }
}
```

!!! warning "Fíjate en el `toResponse`"
    Recibe **dos argumentos**: la reserva y el **código del aula**. El `Response` no lleva `aulaId`, lleva `aulaCodigo`.

    Eso significa que **el servicio tiene que buscar el aula** antes de mapear. Es una decisión de diseño que viene dada, y es la misma de siempre: al cliente se le dan datos útiles, no claves ajenas.

### Los endpoints que escribes

```
GET    /api/v1/reservas            ?dia=2026-11-04 &aulaId=
GET    /api/v1/reservas/{id}
POST   /api/v1/reservas
PATCH  /api/v1/reservas/{id}
DELETE /api/v1/reservas/{id}
GET    /api/v1/aulas/{id}/reservas ?dia=
```

### Las reglas de negocio

| | Regla | Si falla |
|:-:|---|:-:|
| **R1** | `profesor` no vacío; `dia` obligatorio; `horaInicio` y `horaFin` entre 8 y 21 | **400** |
| **R2** | `horaFin` **estrictamente mayor** que `horaInicio` | **409** |
| **R3** | El aula tiene que existir y estar **activa** | **404** / **409** |
| **R4** | **No puede haber dos reservas solapadas** en la misma aula y día | **409** |
| **R5** | Un profesor no puede tener **más de 2 reservas** el mismo día | **409** |
| **R6** | Un `id` que no existe | **404** |

### La R4, que es el corazón del ejercicio

Dos tramos `[inicioA, finA)` y `[inicioB, finB)` **se solapan si y solo si**:

```java
inicioA < finB && inicioB < finA
```

| Reserva existente | Nueva | ¿Solapa? |
|---|---|:-:|
| 9–11 | 11–13 | **No**: una acaba cuando la otra empieza |
| 9–11 | 10–12 | **Sí** |
| 9–11 | 8–14 | **Sí**: la contiene |
| 9–11 | 9–11 | **Sí**: idéntica |
| 9–11 | 12–13 | **No** |

!!! danger "El error que cuesta el punto"
    Con `<=` en lugar de `<`, una reserva de 9 a 11 bloquea otra de 11 a 13, y las clases consecutivas en la misma aula dejan de poder reservarse.

    **Prueba los cinco casos de la tabla.** Es el ítem que más suspende este enunciado.

---

## E3 · Taller mecánico

### El esquema, que se te da

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS clientes (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    telefono    VARCHAR(20)  NOT NULL,
    reparadas   INT          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reparaciones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    matricula   VARCHAR(10)  NOT NULL,
    cliente_id  BIGINT       NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    importe     DECIMAL(10,2) NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reparacion_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);
```

### El repositorio, que se te da

```java title="reparaciones/repositories/ReparacionesRepository.java"
public interface ReparacionesRepository {
    List<Reparacion> findAll();
    List<Reparacion> findByEstado(String estado);
    List<Reparacion> findByMatriculaIgnoreCase(String matricula);
    Optional<Reparacion> findById(Long id);
    Reparacion save(Reparacion reparacion);
    Reparacion update(Reparacion reparacion);
    void deleteById(Long id);

    /** Cambia el estado solo si el actual es el esperado. Devuelve filas afectadas. */
    int cambiarEstado(Long id, String estadoActual, String estadoNuevo);
    int sumarReparada(Long clienteId);
    boolean existsClienteById(Long clienteId);
}
```

### El mapeador, que se te da

```java title="reparaciones/mappers/ReparacionMapper.java"
@Component
public class ReparacionMapper {

    public ReparacionResponse toResponse(Reparacion r) {
        return new ReparacionResponse(r.id(), r.matricula(), r.clienteId(),
                r.descripcion(), r.importe(), r.estado(), r.createdAt(), r.updatedAt());
    }

    public List<ReparacionResponse> toResponse(List<Reparacion> reparaciones) {
        return reparaciones.stream().map(this::toResponse).toList();
    }

    /** El estado inicial NO lo decide el cliente: lo pone el mapeador. */
    public Reparacion toModel(ReparacionCreateRequest r) {
        return new Reparacion(null, r.matricula().trim().toUpperCase(), r.clienteId(),
                r.descripcion().trim(), r.importe(), "RECIBIDA",
                LocalDateTime.now(), LocalDateTime.now());
    }

    public Reparacion parchear(Reparacion original, ReparacionUpdateRequest r) {
        return new Reparacion(
                original.id(),
                original.matricula(),
                original.clienteId(),
                r.descripcion() != null ? r.descripcion().trim() : original.descripcion(),
                r.importe()     != null ? r.importe()            : original.importe(),
                original.estado(),                       // el estado NO se parchea
                original.createdAt(),
                LocalDateTime.now());
    }
}
```

!!! success "Dos decisiones ya tomadas que tienes que respetar"
    - **El `"RECIBIDA"` está en el `toModel`**: el estado inicial lo pone el servidor, no el cliente. Si tu `CreateRequest` tiene un campo `estado`, está mal.
    - **El `parchear` no toca el estado**: el estado solo cambia por el endpoint de transición. Un PATCH no es una máquina de estados.

### Los endpoints que escribes

```
GET    /api/v1/reparaciones          ?estado= &matricula=
GET    /api/v1/reparaciones/{id}
POST   /api/v1/reparaciones
PATCH  /api/v1/reparaciones/{id}
DELETE /api/v1/reparaciones/{id}
POST   /api/v1/reparaciones/{id}/estado   { "estado": "EN_TALLER" }
```

### Las reglas de negocio

| | Regla | Si falla |
|:-:|---|:-:|
| **R1** | `matricula` obligatoria y de 6 a 10 caracteres; `descripcion` no vacía; `importe` > 0 | **400** |
| **R2** | El `clienteId` tiene que existir | **404** |
| **R3** | Las transiciones **solo** pueden ser `RECIBIDA → EN_TALLER → REPARADA → ENTREGADA` | **409** |
| **R4** | Al pasar a `REPARADA`, **suma 1** al contador del cliente | **409** si falla |
| **R5** | Una reparación en `EN_TALLER` **no se puede borrar** | **409** |
| **R6** | Un `id` que no existe | **404** |

### La R3 y la R4

```java title="Lo que tienes que escribir en el servicio"
private static final Map<String, String> SIGUIENTE = Map.of(
        "RECIBIDA",  "EN_TALLER",
        "EN_TALLER", "REPARADA",
        "REPARADA",  "ENTREGADA");

@Override
@Transactional
public ReparacionResponse cambiarEstado(Long id, EstadoRequest request) {

    var reparacion = repositorio.findById(id)
            .orElseThrow(() -> new ReparacionNotFoundException(id));

    var esperado = SIGUIENTE.get(reparacion.estado());
    if (!request.estado().equals(esperado))
        throw new ReparacionConflictException(
                "Desde %s solo se puede pasar a %s"
                        .formatted(reparacion.estado(), esperado));

    // el repositorio comprueba el estado actual en el propio UPDATE
    if (repositorio.cambiarEstado(id, reparacion.estado(), request.estado()) == 0)
        throw new ReparacionConflictException("El estado ha cambiado, vuelve a intentarlo");

    if ("REPARADA".equals(request.estado()))
        repositorio.sumarReparada(reparacion.clienteId());

    return mapper.toResponse(repositorio.findById(id).orElseThrow());
}
```

!!! danger "Lo que se corrige aquí"
    - **El mapa de transiciones**, no una cadena de `if`. Y que `ENTREGADA` **no tenga siguiente**: desde ahí no se va a ningún sitio.
    - **El `@Transactional`**: el cambio de estado y el contador del cliente van juntos.
    - **El `== 0`**: el `UPDATE` lleva el estado actual en el `WHERE`, así que **la propia base de datos** impide la transición doble. Mira el valor devuelto.

---

## ¿Cabe en cuatro horas?

**Sí, y con margen.** El repositorio y el mapeador dados quitan de un tirón las dos partes más mecánicas y más largas: unas **170 líneas** de las 400 de antes.

| | Ficheros que escribes | Líneas aprox. |
|---|:-:|:-:|
| Modelos (`record`) | 1–2 | 20 |
| DTOs (`Response`, `CreateRequest`, `UpdateRequest`, el de la operación) | 4 | 55 |
| Excepciones (3) | 3 | 25 |
| Servicio (interfaz + impl con las reglas) | 2 | 95 |
| Controlador | 1 | 60 |
| `@RestControllerAdvice` | 1 | 35 |
| Tests | 2 | 40 |
| | **~14** | **~330** |

!!! success "Lo que se recortó respecto a versiones anteriores de estos enunciados"
    | | Antes | Ahora |
    |---|:-:|:-:|
    | **El repositorio** | Lo escribía el alumno | **Dado**: interfaz, implementación y `RowMapper` |
    | **El mapeador** | Lo escribía el alumno | **Dado** |
    | Endpoints | 7 + 1 opcional | **6** |
    | Reglas de negocio | 6 | **5 + el 404** |
    | Tests exigidos | 3 | **2** |

    **Por qué:** el SQL de un CRUD con `JdbcClient` es repetitivo y se demuestra en los proyectos paso a paso. En cuatro horas vale más la pena medir **las capas, las reglas y los códigos de estado**, que es lo que dice el RA5.

---

## Rúbrica de calificación

**La misma para los tres enunciados y para el examen.** Sobre 10 puntos, y cada ítem se comprueba con una acción concreta.

### Bloque 1 · Arquitectura por capas — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 1.1 | `grep -rn "ResponseEntity\|HttpStatus\|@PathVariable" src/main/java/**/services/` → **vacío** | **1,0** |
| 1.2 | `grep -rni "SELECT\|INSERT\|UPDATE" src/main/java/**/controllers/ src/main/java/**/services/` → **vacío** | 0,5 |
| 1.3 | Las carpetas por capas y cada clase con su anotación (`@RestController`, `@Service`), con **el servicio detrás de una interfaz** | 0,5 |
| 1.4 | Inyección **por constructor** y campos `final`. `grep -rn "@Autowired" src/main/java/` → **vacío** | 0,5 |

### Bloque 2 · El modelo y los DTOs — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 2.1 | Los modelos son `record` **con los componentes y el orden que exige el código dado**, y el proyecto compila sin tocarlo | **0,75** |
| 2.2 | **Tres DTOs distintos**, y el de creación **sin `id`, sin fechas y sin los campos que calcula el servidor** | 0,5 |
| 2.3 | Ningún controlador devuelve ni recibe el modelo: **solo DTOs** | 0,5 |
| 2.4 | Las reglas de **formato** (R1) están **en el DTO** con Bean Validation, no en el servicio | 0,25 |

!!! danger "El 2.1 se corrige de la forma más dura posible"
    Se sustituye tu modelo y tus DTOs en el proyecto original y **se compila**. Si has cambiado el repositorio o el mapeador dados para que encaje con tus clases, **el ítem es 0**: la especificación la ponía el código dado.

### Bloque 3 · API REST y códigos de estado — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 3.1 | Los **seis endpoints** existen en la ruta indicada, con prefijo de versión | 0,5 |
| 3.2 | `GET` de un id inexistente → **404** (no 500 ni 200 con `null`) | 0,5 |
| 3.3 | `POST` correcto → **201** y cabecera **`Location`** | 0,5 |
| 3.4 | `DELETE` correcto → **204**; de un id inexistente → **404** | 0,25 |
| 3.5 | El **filtro opcional** funciona, y sin distinguir mayúsculas donde el enunciado lo pide | 0,25 |

### Bloque 4 · Reglas de negocio y errores — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 4.1 | **Las cinco reglas** se cumplen y devuelven el código indicado. *0,3 por regla* | **1,5** |
| 4.2 | **Excepciones propias** con `@ResponseStatus`. `grep -rn "ResponseStatusException" src/main/java/` → **vacío** | 0,4 |
| 4.3 | `@RestControllerAdvice` con los handlers de validación, JSON ilegible y tipo erróneo, y el 400 **campo a campo** | 0,4 |
| 4.4 | La operación de las dos tablas lleva **`@Transactional`**, y se demuestra con el estado **antes y después de un fallo** | 0,2 |

### Bloque 5 · Uso correcto del código dado — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 5.1 | **Ni una línea modificada** en el repositorio ni en el mapeador. `git diff` de esos ficheros → **vacío** | 0,4 |
| 5.2 | El servicio **mira el valor devuelto** por el método atómico (`== 0` → 409), en vez de leer, comprobar y escribir | 0,4 |
| 5.3 | Se usa el mapeador para **todo** el mapeo: `grep -rn "new .*Response(" src/main/java/**/services/ src/main/java/**/controllers/` → **vacío** | 0,2 |

### Bloque 6 · Tests — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 6.1 | `./mvnw clean test` acaba en **BUILD SUCCESS**. Si no compila o hay tests comentados → **0 en el bloque** | 0,3 |
| 6.2 | **Un test de servicio** con Mockito de una de las reglas de negocio | 0,4 |
| 6.3 | **Un test de capa web** con `MockMvc` del **400 de validación** o del **404** | 0,3 |

!!! danger "Penalizaciones — se restan del total"
    | | |
    |---|:-:|
    | Haber modificado el repositorio o el mapeador dados para que encajen con tus clases | **−1,5** |
    | Aceptar del cliente un dato que debe poner el servidor (estado inicial, contador, fechas) | **−1,0** |
    | Un `GET` que modifica datos | **−1,0** |
    | El `.jar` no se genera o no arranca | **−1,0** |
    | Verbos en las URL (`/crear`, `/listar`, `/borrar/{id}`) | **−0,5** |
    | `System.out.println` en lugar de un `Logger` | **−0,25** |

!!! success "Nota mínima"
    Se aprueba con **5,0**, y **el bloque 1 no puede quedar en 0**: el RA5 es literalmente *«separar el código de presentación de la lógica de negocio»*.

    | Bloque | Peso |
    |---|:-:|
    | 1 · Capas | 25 % |
    | 2 · Modelo y DTOs | 20 % |
    | 3 · API REST | 20 % |
    | 4 · Reglas y errores | 25 % |
    | 5 · Uso del código dado | 10 % |
    | 6 · Tests | 10 % |

!!! info "Orden de corrección"
    1. `git diff` del repositorio y del mapeador → bloque 5.1 y la penalización gorda.
    2. `./mvnw clean test` → bloque 6.1.
    3. **Los tres `grep`** de los bloques 1, 4.2 y 5.3 → **un tercio de la nota en dos minutos**.
    4. `java -jar` y las peticiones del enunciado → bloques 3 y 4.1.
    5. Un POST inválido → bloques 2.4 y 4.3.
    6. Provocar el fallo de la operación transaccional y comparar los dos contadores → bloque 4.4.
    7. Leer los DTOs → bloques 2.2 y 2.3.

    Se entrega comprimido **sin `target/` ni `datos/`**, con un `README.md` de diez líneas.
