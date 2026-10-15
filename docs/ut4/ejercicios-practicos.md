# Ejercicios prácticos — UT4 (RA5)

Tres enunciados **del mismo tamaño que el examen**: dos tablas, siete endpoints, seis reglas de negocio y tres tests. Pensados para **resolverse en cuatro horas** con el esqueleto dado.

!!! info "Qué se te da y qué escribes tú"
    **Se te da:** `pom.xml`, `application.properties`, `schema.sql`, `data.sql` y la clase de arranque. El proyecto **arranca vacío** y conectado a H2.

    **Escribes tú:** modelo, DTOs, `RowMapper`, repositorio, mapeador, excepciones, servicio, controlador, manejador global y tres tests.

    El examen es exactamente igual con otro dominio. **No hay defensa oral:** se corrige el código y la salida de las peticiones.

| | Enunciado | Tablas | La dificultad |
|:-:|---|:-:|---|
| **EP1** | [Biblioteca](#ep1-biblioteca) | 2 | Regla que depende de **otra tabla** (préstamos por socio) |
| **EP2** | [Reservas de aulas](#ep2-reservas-de-aulas) | 2 | **Detección de solapes** con `<` y no `<=` |
| **EP3** | [Taller mecánico](#ep3-taller-mecanico) | 2 | **Máquina de estados** y transacción de dos tablas |

!!! success "Reparto del tiempo, para que quepa"
    | | Qué | Tiempo |
    |:-:|---|:-:|
    | 1 | Modelo, DTOs y `RowMapper` | 35 min |
    | 2 | Repositorio con `JdbcClient` | 50 min |
    | 3 | Excepciones, mapeador y servicio con las seis reglas | 70 min |
    | 4 | Controlador y `@RestControllerAdvice` | 40 min |
    | 5 | Tres tests | 25 min |
    | 6 | Probar con `curl` y repasar la rúbrica | 20 min |
    | | | **4 h** |

    **Si vas justo, el orden de lo que se abandona** es: el endpoint de estadísticas, el tercer test y el PATCH. Nunca las capas ni la validación: ahí están 4,5 de los 10 puntos.

---

## EP1 · Biblioteca

**Contexto.** Una biblioteca presta libros a sus socios. De cada libro hay varios ejemplares y hay que saber cuántos están fuera. Un socio no puede acumular préstamos sin límite.

### Esquema (se te da)

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS socios (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero    VARCHAR(10)  NOT NULL,
    nombre    VARCHAR(100) NOT NULL,
    email     VARCHAR(150) NOT NULL,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE,
    prestados INT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_socio_numero UNIQUE (numero),
    CONSTRAINT uk_socio_email  UNIQUE (email),
    CONSTRAINT ck_socio_prestados CHECK (prestados BETWEEN 0 AND 3)
);

CREATE TABLE IF NOT EXISTS libros (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn        VARCHAR(13)  NOT NULL,
    titulo      VARCHAR(200) NOT NULL,
    autor       VARCHAR(120) NOT NULL,
    genero      VARCHAR(20)  NOT NULL,
    ejemplares  INT          NOT NULL DEFAULT 1,
    prestados   INT          NOT NULL DEFAULT 0,
    publicacion DATE         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_libro_isbn UNIQUE (isbn),
    CONSTRAINT ck_libro_ejemplares CHECK (ejemplares >= 1),
    CONSTRAINT ck_libro_prestados  CHECK (prestados >= 0 AND prestados <= ejemplares),
    CONSTRAINT ck_libro_genero CHECK (genero IN ('NOVELA','ENSAYO','COMIC','TECNICO'))
);
```

### Endpoints

| # | Método y ruta | Éxito |
|:-:|---|:-:|
| 1 | `GET /api/v1/libros` · filtros opcionales y combinables `genero`, `autor` (contiene, sin distinguir mayúsculas), `disponibles=true` | 200 |
| 2 | `GET /api/v1/libros/{id}` | 200 |
| 3 | `POST /api/v1/libros` | 201 + `Location` |
| 4 | `PATCH /api/v1/libros/{id}` · modifica **solo** lo que llega | 200 |
| 5 | `DELETE /api/v1/libros/{id}` | 204 |
| 6 | `POST /api/v1/libros/{id}/prestamo?socioId=` · **toca las dos tablas** | 200, con el libro |
| 7 | `POST /api/v1/libros/{id}/devolucion?socioId=` | 200, con el libro |
| 8 | `GET /api/v1/libros/estadisticas` *(opcional, si te sobra tiempo)* | 200 |

### Reglas de negocio

| # | Regla | Respuesta |
|:-:|---|:-:|
| R1 | `isbn` exactamente **13 dígitos**, y único | **400** / **409** |
| R2 | `titulo` y `autor` no vacíos; `ejemplares` mínimo 1; `publicacion` **no futura** | **400** |
| R3 | **No se puede prestar** si `libros.prestados == libros.ejemplares` | **409** |
| R4 | **Un socio no puede tener más de 3 préstamos activos** | **409** |
| R5 | **No se puede devolver** si `libros.prestados == 0` o si el socio no tiene préstamos | **409** |
| R6 | **No se puede borrar** un libro con `prestados > 0` | **409** |

### La operación de los puntos 6 y 7

Prestar incrementa **`libros.prestados`** y **`socios.prestados`**. Devolver, al contrario. **Las dos actualizaciones son atómicas**: si falla la segunda, la primera no puede quedarse hecha.

```java
@Transactional
public LibroResponse prestar(Long libroId, Long socioId) {
    var socio = sociosRepository.findById(socioId)
            .orElseThrow(() -> new SocioNotFoundException(socioId));
    if (!socio.isActivo())
        throw new BibliotecaConflictException("El socio está dado de baja");

    if (librosRepository.reservarEjemplar(libroId) == 0)
        throw new BibliotecaConflictException("No quedan ejemplares libres");

    if (sociosRepository.incrementarPrestados(socioId) == 0)
        throw new BibliotecaConflictException("El socio ya tiene 3 préstamos activos");

    return mapper.toResponse(librosRepository.findById(libroId).orElseThrow());
}
```

```java
// R3 · en el repositorio de libros
@Override
public int reservarEjemplar(Long id) {
    return jdbc.sql("""
            UPDATE libros SET prestados = prestados + 1, updated_at = CURRENT_TIMESTAMP
             WHERE id = :id AND prestados < ejemplares
            """).param("id", id).update();
}

// R4 · en el repositorio de socios
@Override
public int incrementarPrestados(Long id) {
    return jdbc.sql("""
            UPDATE socios SET prestados = prestados + 1
             WHERE id = :id AND prestados < 3
            """).param("id", id).update();
}
```

!!! danger "Las dos cosas que se evalúan aquí"
    **1. `UPDATE … WHERE … <` en lugar de leer, comprobar y escribir.** Si afecta a 0 filas, la regla no se cumplía. Es el patrón del [tema 3](03-repositorios-servicios-dtos.md).

    **2. El `@Transactional`.** Si la R4 falla después de haber reservado el ejemplar, el `rollback` tiene que devolver `libros.prestados` a su valor. **Se comprueba apuntando los dos contadores, provocando el fallo y volviendo a mirar.**

### `GET /api/v1/libros/estadisticas` *(opcional)*

```json
{
  "titulos": 12,
  "ejemplaresTotales": 34,
  "prestados": 9,
  "porGenero": { "COMIC": 2, "ENSAYO": 3, "NOVELA": 7 },
  "sinEjemplaresLibres": ["Dune", "Fundación"]
}
```

`porGenero` con un `GROUP BY` en el repositorio, no trayendo todos los libros a Java.

---

## EP2 · Reservas de aulas

**Contexto.** Un centro gestiona las reservas de sus aulas. Cada reserva ocupa un aula un día en una franja horaria, y **dos reservas no pueden solaparse en la misma aula el mismo día**.

### Esquema (se te da)

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS aulas (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(10) NOT NULL,
    nombre          VARCHAR(80) NOT NULL,
    tipo            VARCHAR(20) NOT NULL,
    capacidad       INT         NOT NULL,
    activa          BOOLEAN     NOT NULL DEFAULT TRUE,
    minutos_ocupados INT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_aula_codigo UNIQUE (codigo),
    CONSTRAINT ck_aula_tipo CHECK (tipo IN ('TEORIA','LABORATORIO','TALLER')),
    CONSTRAINT ck_aula_capacidad CHECK (capacidad BETWEEN 1 AND 500)
);

CREATE TABLE IF NOT EXISTS reservas (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    aula_id     BIGINT       NOT NULL,
    solicitante VARCHAR(100) NOT NULL,
    motivo      VARCHAR(200) NOT NULL,
    fecha       DATE         NOT NULL,
    hora_inicio TIME         NOT NULL,
    hora_fin    TIME         NOT NULL,
    asistentes  INT          NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_aula FOREIGN KEY (aula_id) REFERENCES aulas (id),
    CONSTRAINT ck_reserva_horas CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_reserva_asistentes CHECK (asistentes >= 1)
);

CREATE INDEX IF NOT EXISTS idx_reserva_aula_fecha ON reservas (aula_id, fecha);
```

### Endpoints

| # | Método y ruta | Éxito |
|:-:|---|:-:|
| 1 | `GET /api/v1/aulas` · filtros opcionales `tipo`, `capacidadMin`, `activa` | 200 |
| 2 | `GET /api/v1/aulas/{id}` | 200 |
| 3 | `GET /api/v1/aulas/{id}/reservas?fecha=` · ordenadas por hora | 200 |
| 4 | `GET /api/v1/reservas` · filtros opcionales `aulaId`, `fecha`, `solicitante` | 200 |
| 5 | `POST /api/v1/reservas` · **toca las dos tablas** | 201 + `Location` |
| 6 | `DELETE /api/v1/reservas/{id}` · **devuelve los minutos al aula** | 204 |
| 7 | `PATCH /api/v1/aulas/{id}` · modifica **solo** lo que llega | 200 |
| 8 | `GET /api/v1/aulas/{id}/libre?fecha=&desde=&hasta=` → `{"libre": true}` *(opcional)* | 200 |

### Reglas de negocio

| # | Regla | Respuesta |
|:-:|---|:-:|
| R1 | `hora_fin` **estrictamente posterior** a `hora_inicio`; horas entre **08:00 y 22:00**; duración entre **30 y 240 minutos** | **400** |
| R2 | La `fecha` **no puede ser pasada** | **400** |
| R3 | El aula **debe existir** (400) **y estar activa** (409) | **400** / **409** |
| R4 | **`asistentes` no puede superar la `capacidad`** del aula | **409** |
| R5 | **No puede solaparse** con otra reserva de la misma aula el mismo día | **409** |
| R6 | No se puede **desactivar** un aula con reservas futuras | **409** |

### La regla 5, que es el corazón del ejercicio

```java
@Override
public boolean haySolape(Long aulaId, LocalDate fecha,
                         LocalTime inicio, LocalTime fin) {
    return jdbc.sql("""
            SELECT COUNT(*) FROM reservas
             WHERE aula_id = :aulaId
               AND fecha = :fecha
               AND hora_inicio < :fin
               AND :inicio < hora_fin
            """)
        .param("aulaId", aulaId).param("fecha", fecha)
        .param("inicio", inicio).param("fin", fin)
        .query(Integer.class).single() > 0;
}
```

!!! danger "La trampa es `<` y no `<=`"
    Con `<=`, una reserva que acaba a las 10:00 y otra que empieza a las 10:00 se declaran en conflicto, y **pierdes la mitad del horario del centro**.

    Los tres casos que hay que probar:

    | Reserva A | Reserva B | ¿Solapan? |
    |---|---|:-:|
    | 09:00–10:00, aula A12 | 10:00–11:00, aula A12 | **No** |
    | 09:00–10:00, aula A12 | 09:30–10:30, aula A12 | **Sí** |
    | 09:00–10:00, aula A12 | 09:30–10:30, aula **L01** | **No** |

!!! tip "Y el `POST` toca las dos tablas"
    Inserta la reserva **y** suma su duración a `aulas.minutos_ocupados`. El `DELETE` resta. Las dos operaciones, en la misma transacción.

    ```java
    @Transactional
    public ReservaResponse crear(ReservaCreateRequest r) {
        var aula = aulasRepository.findById(r.aulaId())
                .orElseThrow(() -> new AulaNotFoundException(r.aulaId()));
        if (!aula.isActiva())
            throw new ReservaConflictException("El aula " + aula.getCodigo() + " está inactiva");
        if (r.asistentes() > aula.getCapacidad())
            throw new ReservaConflictException("Asistentes (%d) por encima de la capacidad (%d)"
                    .formatted(r.asistentes(), aula.getCapacidad()));
        if (reservasRepository.haySolape(r.aulaId(), r.fecha(), r.desde(), r.hasta()))
            throw new ReservaConflictException("El aula ya está ocupada en esa franja");

        var guardada = reservasRepository.save(mapper.toModel(r));
        aulasRepository.sumarMinutos(r.aulaId(),
                (int) ChronoUnit.MINUTES.between(r.desde(), r.hasta()));
        return mapper.toResponse(guardada);
    }
    ```

---

## EP3 · Taller mecánico

**Contexto.** Un taller lleva el control de sus reparaciones. Cada reparación pasa por una secuencia de estados y consume piezas del almacén, que se descuentan al añadirlas.

### Esquema (se te da)

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS piezas (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20)   NOT NULL,
    nombre VARCHAR(120)  NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock  INT           NOT NULL DEFAULT 0,
    CONSTRAINT uk_pieza_codigo UNIQUE (codigo),
    CONSTRAINT ck_pieza_precio CHECK (precio >= 0),
    CONSTRAINT ck_pieza_stock  CHECK (stock  >= 0)
);

CREATE TABLE IF NOT EXISTS reparaciones (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo      VARCHAR(20)   NOT NULL,
    matricula   VARCHAR(10)   NOT NULL,
    marca       VARCHAR(40)   NOT NULL,
    descripcion VARCHAR(500)  NOT NULL,
    estado      VARCHAR(20)   NOT NULL DEFAULT 'RECIBIDO',
    mano_obra   DECIMAL(10,2) NOT NULL DEFAULT 0,
    total       DECIMAL(10,2) NOT NULL DEFAULT 0,
    entrada     DATE          NOT NULL,
    salida      DATE,
    CONSTRAINT uk_reparacion_codigo UNIQUE (codigo),
    CONSTRAINT ck_reparacion_estado
        CHECK (estado IN ('RECIBIDO','EN_CURSO','TERMINADO','ENTREGADO','CANCELADO')),
    CONSTRAINT ck_reparacion_total CHECK (total >= 0)
);
```

### Endpoints

| # | Método y ruta | Éxito |
|:-:|---|:-:|
| 1 | `GET /api/v1/piezas` · filtros opcionales `nombre`, `conStock` | 200 |
| 2 | `POST /api/v1/piezas` · `PATCH /api/v1/piezas/{id}` | 201 / 200 |
| 3 | `GET /api/v1/reparaciones` · filtros opcionales `estado`, `matricula` | 200 |
| 4 | `GET /api/v1/reparaciones/{id}` | 200 |
| 5 | `POST /api/v1/reparaciones` · estado `RECIBIDO`, **`codigo` generado** | 201 + `Location` |
| 6 | `POST /api/v1/reparaciones/{id}/pieza` · `{"piezaId":4,"cantidad":2}` · **toca las dos tablas** | 200 |
| 7 | `POST /api/v1/reparaciones/{id}/estado` · `{"estado":"EN_CURSO"}` | 200 |
| 8 | `GET /api/v1/reparaciones/estadisticas` *(opcional)* | 200 |

### Reglas de negocio

| # | Regla | Respuesta |
|:-:|---|:-:|
| R1 | `matricula` cumple `\d{4}[A-Z]{3}`; `descripcion` no vacía; `mano_obra` ≥ 0; `entrada` no futura | **400** |
| R2 | El `codigo` lo genera el servidor: `REP-2026-000123`, **correlativo por año** | — |
| R3 | **Transiciones válidas únicamente:** `RECIBIDO → EN_CURSO → TERMINADO → ENTREGADO`, y desde cualquiera salvo `ENTREGADO` a `CANCELADO` | **409** |
| R4 | **Solo se pueden añadir piezas con la reparación en `RECIBIDO` o `EN_CURSO`** | **409** |
| R5 | **Si no hay stock suficiente de la pieza, no se descuenta nada** y el total no cambia | **409** |
| R6 | Pasar a `ENTREGADO` fija `salida = hoy`; `total` y el precio de las piezas los calcula **siempre el servidor** | — |

### Las reglas 3 y 5

```java
private static final Map<Estado, Set<Estado>> TRANSICIONES = Map.of(
        Estado.RECIBIDO,  Set.of(Estado.EN_CURSO,  Estado.CANCELADO),
        Estado.EN_CURSO,  Set.of(Estado.TERMINADO, Estado.CANCELADO),
        Estado.TERMINADO, Set.of(Estado.ENTREGADO, Estado.CANCELADO),
        Estado.ENTREGADO, Set.of(),
        Estado.CANCELADO, Set.of());
```

```java
@Transactional
public ReparacionResponse añadirPieza(Long id, PiezaRequest req) {
    var rep = repositorio.findById(id)
            .orElseThrow(() -> new ReparacionNotFoundException(id));

    if (rep.getEstado() != Estado.RECIBIDO && rep.getEstado() != Estado.EN_CURSO)
        throw new ReparacionConflictException(
                "No se pueden añadir piezas en estado " + rep.getEstado());

    var pieza = piezasRepository.findById(req.piezaId())
            .orElseThrow(() -> new PiezaNotFoundException(req.piezaId()));

    if (piezasRepository.descontarStock(req.piezaId(), req.cantidad()) == 0)
        throw new ReparacionConflictException(
                "Stock insuficiente de %s: piden %d, hay %d"
                        .formatted(pieza.getNombre(), req.cantidad(), pieza.getStock()));

    var importe = pieza.getPrecio().multiply(BigDecimal.valueOf(req.cantidad()));
    repositorio.sumarAlTotal(id, importe);

    return mapper.toResponse(repositorio.findById(id).orElseThrow());
}
```

!!! danger "Lo que se comprueba"
    Se apunta el stock de la pieza **y** el total de la reparación, se manda una petición que falle por falta de stock, y se vuelve a mirar. **Los dos valores tienen que estar intactos.**

    Y el `descontarStock` es el `UPDATE … WHERE stock >= :cantidad` de siempre: nunca «leer, comprobar y escribir».

---

## ¿Cabe en cuatro horas?

**Sí, con este tamaño.** El esqueleto dado quita la media hora de Maven y configuración, y el volumen de código es de unas **400 líneas** repartidas en 12 ficheros pequeños.

| | Ficheros que escribes | Líneas aprox. |
|---|---|:-:|
| Modelo (2 clases con `builder`) | 2 | 90 |
| DTOs (`Response`, `CreateRequest`, `UpdateRequest`) | 3 | 50 |
| `RowMapper` (2) | 2 | 40 |
| Repositorios (interfaz + impl, los dos) | 4 | 120 |
| Excepciones (base + 3) | 4 | 30 |
| Mapeador | 1 | 50 |
| Servicio (interfaz + impl) | 2 | 90 |
| Controlador | 1 | 60 |
| `@RestControllerAdvice` | 1 | 35 |
| Tests | 2 | 60 |

!!! success "Lo que se recortó respecto a versiones anteriores de estos enunciados"
    | | Antes | Ahora |
    |---|:-:|:-:|
    | Tablas | 2–3 + líneas | **2** |
    | Endpoints | 10–11 | **7** (+1 opcional) |
    | Reglas de negocio | 10–12 | **6** |
    | Tests exigidos | 7 | **3** |

    La operación transaccional **se queda** —es el ítem que más distingue— pero sobre **dos tablas**, no sobre una lista de líneas de longitud variable.

---

## Rúbrica de calificación

**La misma para los tres enunciados y para el examen.** Sobre 10 puntos, y cada ítem se comprueba con una acción concreta.

### Bloque 1 · Arquitectura por capas — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 1.1 | `grep -rn "ResponseEntity\|HttpStatus\|@PathVariable" src/main/java/**/services/` → **vacío** | **1,0** |
| 1.2 | `grep -rni "SELECT\|INSERT\|UPDATE" src/main/java/**/controllers/ src/main/java/**/services/` → **vacío** | 0,5 |
| 1.3 | Las tres capas con su anotación (`@RestController`, `@Service`, `@Repository`) y **el repositorio y el servicio detrás de una interfaz** | 0,5 |
| 1.4 | Inyección **por constructor** y campos `final`. `grep -rn "@Autowired" src/main/java/` → **vacío** | 0,5 |

### Bloque 2 · API REST y códigos de estado — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 2.1 | Los **siete endpoints** existen en la ruta indicada, con prefijo de versión | 0,5 |
| 2.2 | `GET` de un id inexistente → **404** (no 500 ni 200 con `null`) | 0,5 |
| 2.3 | `POST` correcto → **201** y cabecera **`Location`** | 0,5 |
| 2.4 | `DELETE` correcto → **204**; de un id inexistente → **404** | 0,25 |
| 2.5 | Los **filtros opcionales** funcionan por separado y combinados, sin distinguir mayúsculas donde el enunciado lo pide | 0,25 |

### Bloque 3 · DTOs y validación — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 3.1 | Ningún controlador devuelve ni recibe el modelo: **solo DTOs** | 0,5 |
| 3.2 | **Tres DTOs distintos**, y el de creación **sin `id` ni fechas** | 0,5 |
| 3.3 | Todos los `@RequestBody` llevan **`@Valid`**, y un POST inválido devuelve **400** | 0,5 |
| 3.4 | El 400 devuelve los errores **campo a campo** | 0,25 |
| 3.5 | Las reglas de **formato** (R1 y R2) están **en el DTO**, no en el servicio | 0,25 |

### Bloque 4 · Reglas de negocio y errores — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 4.1 | **Las seis reglas** se cumplen y devuelven el código indicado. *0,25 por regla* | **1,5** |
| 4.2 | Jerarquía de **excepciones propias** con `@ResponseStatus`. `grep -rn "ResponseStatusException" src/main/java/` → **vacío** | 0,5 |
| 4.3 | `@RestControllerAdvice` con los handlers de validación, JSON ilegible, tipo erróneo y `DataIntegrityViolationException` | 0,25 |
| 4.4 | El **PATCH conserva** los campos que no llegan. Se comprueba con dos peticiones | 0,25 |

### Bloque 5 · Persistencia con JDBC — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 5.1 | **Cero concatenación de valores en SQL**: todo con `:param`. Una sola → **0 en todo el bloque** | 0,3 |
| 5.2 | Los datos **sobreviven** a parar y arrancar, y arranca **dos veces seguidas** sin error | 0,2 |
| 5.3 | La regla que depende del estado usa un **`UPDATE … WHERE … >=`**, no «leer, comprobar, escribir» | 0,2 |
| 5.4 | La operación de dos tablas lleva `@Transactional`, y **se demuestra con el estado antes y después de un fallo** | **0,3** |

### Bloque 6 · Tests — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 6.1 | `./mvnw clean test` acaba en **BUILD SUCCESS**. Si no compila o hay tests comentados → **0 en el bloque** | 0,2 |
| 6.2 | **Un test de servicio** con Mockito de una regla de negocio | 0,3 |
| 6.3 | **Un test de capa web** con `MockMvc` que compruebe el **400 de validación** | 0,3 |
| 6.4 | **Un test del 404** (servicio o capa web, cualquiera de los dos) | 0,2 |

!!! danger "Penalizaciones — se restan del total"
    | | |
    |---|:-:|
    | Concatenar un valor de entrada en una sentencia SQL | **−2,0** |
    | Aceptar del cliente un dato que debe calcular el servidor (total, precio, estado inicial, código) | **−1,0** |
    | Un `GET` que modifica datos | **−1,0** |
    | El `.jar` no se genera o no arranca | **−1,0** |
    | Verbos en las URL (`/crear`, `/listar`, `/borrar/{id}`) | **−0,5** |
    | Credenciales o rutas absolutas escritas en el código | **−0,5** |
    | `System.out.println` en lugar de un `Logger` | **−0,25** |

!!! success "Nota mínima"
    Se aprueba con **5,0**, y **el bloque 1 no puede quedar en 0**: el RA5 es literalmente *«separar el código de presentación de la lógica de negocio»*.

    | Bloque | Peso |
    |---|:-:|
    | 1 · Capas | 25 % |
    | 2 · API REST | 20 % |
    | 3 · DTOs y validación | 20 % |
    | 4 · Reglas y errores | 25 % |
    | 5 · Persistencia | 10 % |
    | 6 · Tests | 10 % |

!!! info "Orden de corrección"
    1. `./mvnw clean test` → bloque 6.1.
    2. **Los cuatro `grep`** del bloque 1 y del 4.2 y 5.1 → **un tercio de la nota en dos minutos**.
    3. `java -jar` y las peticiones del enunciado → bloques 2 y 4.1.
    4. Un POST inválido → bloques 3.3 y 3.4.
    5. Parar, arrancar y volver a listar → bloque 5.2.
    6. Provocar el fallo de la operación transaccional y comparar los dos contadores → bloque 5.4.
    7. Leer los DTOs y el mapeador → bloques 3.1, 3.2, 3.5 y 4.4.

    Se entrega comprimido **sin `target/` ni `datos/`**, con un `README.md` de diez líneas.
