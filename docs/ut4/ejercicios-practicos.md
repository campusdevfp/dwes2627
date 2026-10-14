# Ejercicios prácticos — UT4 (RA5)

Tres enunciados **del mismo formato y la misma extensión que el examen**. Se resuelven igual que los [dos proyectos paso a paso](05-proyectos.md): Spring Web, H2 por JDBC, capas, DTOs, validación, excepciones y tests.

!!! success "Para qué sirve esta página"
    El examen de la S18 es **un enunciado de este tipo, con otro dominio y la misma rúbrica**. Si resuelves uno de estos en el tiempo marcado, el examen es el mismo ejercicio con otros nombres.

    No hay defensa oral: **se corrige el código y la salida de las peticiones**, con la rúbrica del final.

| | Enunciado | Tablas | Tiempo | Dificultad característica |
|:-:|---|:-:|:-:|---|
| **EP1** | [Gestión de una biblioteca](#ep1-gestion-de-una-biblioteca) | 2 | 3 h | Operación de negocio sobre un contador |
| **EP2** | [Reservas de aulas](#ep2-reservas-de-aulas) | 2 | 3 h | Detección de solapes |
| **EP3** | [Taller mecánico](#ep3-taller-mecanico) | 3 | 4 h | Máquina de estados y transacción multi-tabla |

!!! info "Qué se te da y qué escribes tú"
    **Se te da:** el `pom.xml`, el `application.properties`, el `schema.sql`, el `data.sql` y la clase de arranque. El proyecto **arranca vacío** y se conecta a H2.

    **Escribes tú:** modelos, DTOs, `RowMapper`, repositorio, mapeador, excepciones, servicio, controlador, manejador global de errores y los tests.

    En el examen real es exactamente igual. Lo que se evalúa es la arquitectura y las reglas, no pelearse con Maven.

---

## EP1 · Gestión de una biblioteca

**Contexto.** La biblioteca de un centro necesita una API para su catálogo. Cada libro tiene varios ejemplares físicos, y la aplicación tiene que saber **cuántos están prestados** en cada momento. Los libros se agrupan por género, y un género se da de alta antes de poder asignarle libros.

### Esquema (se te da)

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS generos (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL,
    CONSTRAINT uk_genero_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS libros (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn        VARCHAR(13)   NOT NULL,
    titulo      VARCHAR(200)  NOT NULL,
    autor       VARCHAR(120)  NOT NULL,
    genero_id   BIGINT        NOT NULL,
    ejemplares  INT           NOT NULL DEFAULT 1,
    prestados   INT           NOT NULL DEFAULT 0,
    publicacion DATE          NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_libro_isbn   UNIQUE (isbn),
    CONSTRAINT fk_libro_genero FOREIGN KEY (genero_id) REFERENCES generos (id),
    CONSTRAINT ck_libro_ejemplares CHECK (ejemplares >= 1),
    CONSTRAINT ck_libro_prestados  CHECK (prestados >= 0 AND prestados <= ejemplares)
);

CREATE INDEX IF NOT EXISTS idx_libro_titulo ON libros (titulo);
CREATE INDEX IF NOT EXISTS idx_libro_genero ON libros (genero_id);
```

### Endpoints que hay que implementar

| # | Método y ruta | Qué hace | Éxito |
|:-:|---|---|:-:|
| 1 | `GET /api/v1/libros` | Lista todos. Admite los filtros **opcionales y combinables** `genero` (nombre, sin distinguir mayúsculas), `autor` (contiene, sin distinguir mayúsculas) y `disponibles=true` (solo los que tienen algún ejemplar libre) | 200 |
| 2 | `GET /api/v1/libros/{id}` | Uno, con el **nombre** de su género, no el id | 200 |
| 3 | `POST /api/v1/libros` | Crea. `prestados` arranca siempre en 0 | 201 + `Location` |
| 4 | `PUT /api/v1/libros/{id}` | **Reemplaza** todos los campos editables | 200 |
| 5 | `PATCH /api/v1/libros/{id}` | **Modifica solo** los campos que llegan | 200 |
| 6 | `DELETE /api/v1/libros/{id}` | Borra | 204 |
| 7 | `POST /api/v1/libros/{id}/prestamo` | Incrementa `prestados` en 1 | 200, con el libro |
| 8 | `POST /api/v1/libros/{id}/devolucion` | Decrementa `prestados` en 1 | 200, con el libro |
| 9 | `GET /api/v1/libros/estadisticas` | Resumen (abajo) | 200 |
| 10 | `GET /api/v1/generos` · `POST /api/v1/generos` · `DELETE /api/v1/generos/{id}` | CRUD de géneros | 200 / 201 / 204 |

### Reglas de negocio

| # | Regla | Respuesta si se incumple |
|:-:|---|:-:|
| R1 | El ISBN son **exactamente 13 dígitos** | **400** |
| R2 | El ISBN **no se repite** | **409** |
| R3 | `ejemplares` mínimo **1**; `titulo` y `autor` no vacíos, máximo 200 y 120 caracteres | **400** |
| R4 | `publicacion` **no puede ser futura** | **400** |
| R5 | El `genero` indicado **debe existir** | **400** |
| R6 | **No se puede prestar** si `prestados == ejemplares` | **409** |
| R7 | **No se puede devolver** si `prestados == 0` | **409** |
| R8 | **No se puede borrar** un libro con `prestados > 0` | **409** |
| R9 | **No se puede borrar** un género que tenga libros | **409** |
| R10 | Al bajar `ejemplares` por PUT o PATCH, **no puede quedar por debajo de `prestados`** | **409** |

### `GET /api/v1/libros/estadisticas`

```json
{
  "totalTitulos": 12,
  "ejemplaresTotales": 34,
  "prestados": 9,
  "disponibles": 25,
  "porcentajePrestado": 26.47,
  "porGenero": { "COMIC": 2, "ENSAYO": 3, "NOVELA": 7 },
  "masAntiguo": "El Quijote",
  "sinEjemplaresLibres": ["Dune", "Fundación"]
}
```

!!! tip "Pistas de implementación"
    - **`porGenero`** se resuelve con un `GROUP BY` en el repositorio, no trayendo todos los libros a Java.
    - **R6 y R7** se hacen con un `UPDATE` condicionado, no con «leer, comprobar y escribir»:

    ```sql
    UPDATE libros SET prestados = prestados + 1, updated_at = CURRENT_TIMESTAMP
     WHERE id = :id AND prestados < ejemplares
    ```

    Si afecta a **0 filas**, es que no había ejemplares libres → 409. Es el patrón del `descontarStock` del [proyecto B](05-proyectos.md#b3-el-repositorio-de-pedidos).

    - **R10** es la regla que se olvida: un PATCH que baje `ejemplares` a 1 cuando hay 3 prestados dejaría la tabla en un estado imposible. El `CHECK` del esquema lo impediría con un 500 feo; tú tienes que devolver un **409** con un mensaje útil.
    - **R1** con `@Pattern(regexp = "\\d{13}")`.

---

## EP2 · Reservas de aulas

**Contexto.** Un centro gestiona las reservas de sus aulas y laboratorios. Cada reserva ocupa un aula un día concreto en una franja horaria, y **dos reservas no pueden solaparse en la misma aula el mismo día**.

### Esquema (se te da)

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS aulas (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo    VARCHAR(10)  NOT NULL,
    nombre    VARCHAR(80)  NOT NULL,
    tipo      VARCHAR(20)  NOT NULL,
    capacidad INT          NOT NULL,
    activa    BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_aula_codigo UNIQUE (codigo),
    CONSTRAINT ck_aula_tipo CHECK (tipo IN ('TEORIA','LABORATORIO','TALLER','SALON')),
    CONSTRAINT ck_aula_capacidad CHECK (capacidad BETWEEN 1 AND 500)
);

CREATE TABLE IF NOT EXISTS reservas (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    aula_id      BIGINT       NOT NULL,
    solicitante  VARCHAR(100) NOT NULL,
    motivo       VARCHAR(200) NOT NULL,
    fecha        DATE         NOT NULL,
    hora_inicio  TIME         NOT NULL,
    hora_fin     TIME         NOT NULL,
    asistentes   INT          NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_aula FOREIGN KEY (aula_id) REFERENCES aulas (id),
    CONSTRAINT ck_reserva_horas CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_reserva_asistentes CHECK (asistentes >= 1)
);

CREATE INDEX IF NOT EXISTS idx_reserva_aula_fecha ON reservas (aula_id, fecha);
```

### Endpoints que hay que implementar

| # | Método y ruta | Qué hace | Éxito |
|:-:|---|---|:-:|
| 1 | `GET /api/v1/aulas` | Lista. Filtros opcionales `tipo`, `capacidadMin`, `activa` | 200 |
| 2 | `GET /api/v1/aulas/{id}` | Una | 200 |
| 3 | `POST /api/v1/aulas` · `PATCH /api/v1/aulas/{id}` · `DELETE /api/v1/aulas/{id}` | CRUD | 201 / 200 / 204 |
| 4 | `GET /api/v1/aulas/{id}/reservas?fecha=` | Reservas de un aula, ordenadas por hora | 200 |
| 5 | `GET /api/v1/aulas/{id}/libre?fecha=&desde=&hasta=` | `{"libre": true/false}` | 200 |
| 6 | `GET /api/v1/reservas` | Lista. Filtros `aulaId`, `fecha`, `solicitante` | 200 |
| 7 | `GET /api/v1/reservas/{id}` | Una, con el **código y nombre del aula** | 200 |
| 8 | `POST /api/v1/reservas` | Crea, comprobando todas las reglas | 201 + `Location` |
| 9 | `PATCH /api/v1/reservas/{id}` | Cambia fecha u horas, **volviendo a comprobar el solape** | 200 |
| 10 | `DELETE /api/v1/reservas/{id}` | Anula | 204 |
| 11 | `GET /api/v1/reservas/ocupacion?fecha=` | Cuadrante del día (abajo) | 200 |

### Reglas de negocio

| # | Regla | Respuesta |
|:-:|---|:-:|
| R1 | `hora_fin` **estrictamente posterior** a `hora_inicio` | **400** |
| R2 | Las horas están **entre 08:00 y 22:00** | **400** |
| R3 | La duración es **entre 30 y 240 minutos** | **400** |
| R4 | La `fecha` **no puede ser pasada** | **400** |
| R5 | El aula **debe existir y estar activa** | **400** si no existe, **409** si está inactiva |
| R6 | **`asistentes` no puede superar la `capacidad`** del aula | **409** |
| R7 | **No puede solaparse** con otra reserva de la misma aula el mismo día | **409** |
| R8 | No se puede **desactivar** un aula con reservas futuras | **409** |
| R9 | No se puede **borrar** un aula con reservas | **409** |
| R10 | Al hacer PATCH, **la propia reserva no cuenta** como solape consigo misma | — |

!!! danger "R7 y R10 son el corazón del ejercicio"
    **El solape** es el [E18 de la UT3](../ut3/ejercicios.md) con `LocalTime`:

    ```sql
    SELECT COUNT(*) FROM reservas
     WHERE aula_id = :aulaId
       AND fecha = :fecha
       AND id <> :idActual            -- ← R10: excluye la propia al editar
       AND hora_inicio < :horaFin
       AND :horaInicio < hora_fin
    ```

    Si el resultado es mayor que 0, hay solape → **409**.

    **La trampa es `<` y no `<=`.** Con `<=`, una reserva que acaba a las 10:00 y otra que empieza a las 10:00 se declaran en conflicto, y pierdes la mitad del horario del centro. Tres casos que hay que probar:

    | Reserva A | Reserva B | ¿Solapan? |
    |---|---|:-:|
    | 09:00–10:00, aula A12 | 10:00–11:00, aula A12 | **No** |
    | 09:00–10:00, aula A12 | 09:30–10:30, aula A12 | **Sí** |
    | 09:00–10:00, aula A12 | 09:30–10:30, aula **L01** | **No** |

    Y el **R10**: sin el `id <> :idActual`, un PATCH que solo cambie el motivo **se declara en conflicto consigo mismo** y nunca se puede editar nada.

### `GET /api/v1/reservas/ocupacion?fecha=2026-03-02`

```json
{
  "fecha": "2026-03-02",
  "aulas": [
    { "codigo": "A12", "nombre": "Aula 12", "capacidad": 30,
      "minutosOcupados": 180, "porcentajeJornada": 21.4,
      "reservas": [
        { "desde": "09:00", "hasta": "11:00", "solicitante": "Iván", "motivo": "DWES" },
        { "desde": "16:00", "hasta": "17:00", "solicitante": "Lucía", "motivo": "Tutoría" }
      ]},
    { "codigo": "L01", "nombre": "Laboratorio 1", "capacidad": 20,
      "minutosOcupados": 0, "porcentajeJornada": 0.0, "reservas": [] }
  ]
}
```

!!! tip "Pistas de implementación"
    - Las aulas **sin reservas también aparecen**, con la lista vacía. Eso significa que la consulta es un `LEFT JOIN`, no un `JOIN`.
    - La jornada es de 08:00 a 22:00, o sea **840 minutos**. El porcentaje se redondea a dos decimales.
    - `minutosOcupados` se puede calcular en SQL (`DATEDIFF('MINUTE', hora_inicio, hora_fin)`) o en Java con `ChronoUnit.MINUTES.between(...)`. Las dos valen.
    - Las horas salen como `"09:00"`, no como `"09:00:00"`: configura el formato en el DTO o formatea en el mapeador.

---

## EP3 · Taller mecánico

**Contexto.** Un taller lleva el control de sus reparaciones. Cada reparación es de un vehículo de un cliente, pasa por una secuencia de estados y consume piezas del almacén. Al presupuestar hay que **reservar las piezas**; al cancelar, **devolverlas**.

### Esquema (se te da)

```sql title="schema.sql"
CREATE TABLE IF NOT EXISTS clientes (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nif    VARCHAR(9)   NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    CONSTRAINT uk_cliente_nif UNIQUE (nif)
);

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
    cliente_id  BIGINT        NOT NULL,
    matricula   VARCHAR(10)   NOT NULL,
    marca       VARCHAR(40)   NOT NULL,
    modelo      VARCHAR(40)   NOT NULL,
    descripcion VARCHAR(500)  NOT NULL,
    estado      VARCHAR(20)   NOT NULL DEFAULT 'RECIBIDO',
    mano_obra   DECIMAL(10,2) NOT NULL DEFAULT 0,
    total       DECIMAL(10,2) NOT NULL DEFAULT 0,
    entrada     DATE          NOT NULL,
    salida      DATE,
    CONSTRAINT uk_reparacion_codigo UNIQUE (codigo),
    CONSTRAINT fk_reparacion_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT ck_reparacion_estado CHECK (estado IN
        ('RECIBIDO','PRESUPUESTADO','EN_CURSO','TERMINADO','ENTREGADO','CANCELADO'))
);

CREATE TABLE IF NOT EXISTS lineas_reparacion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    reparacion_id   BIGINT        NOT NULL,
    pieza_id        BIGINT        NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_linea_reparacion FOREIGN KEY (reparacion_id)
        REFERENCES reparaciones (id) ON DELETE CASCADE,
    CONSTRAINT fk_linea_pieza FOREIGN KEY (pieza_id) REFERENCES piezas (id),
    CONSTRAINT ck_linea_cantidad CHECK (cantidad > 0)
);
```

### Endpoints que hay que implementar

| # | Método y ruta | Qué hace | Éxito |
|:-:|---|---|:-:|
| 1 | `GET /api/v1/piezas` | Lista. Filtros `nombre`, `conStock` | 200 |
| 2 | `POST /api/v1/piezas` · `PATCH /api/v1/piezas/{id}` · `DELETE /api/v1/piezas/{id}` | CRUD | 201 / 200 / 204 |
| 3 | `GET /api/v1/clientes` · `/{id}` · `POST` · `PATCH` · `DELETE` | CRUD | — |
| 4 | `GET /api/v1/clientes/{id}/reparaciones` | Historial del cliente | 200 |
| 5 | `GET /api/v1/reparaciones` | Lista. Filtros `estado`, `matricula`, `clienteId` | 200 |
| 6 | `GET /api/v1/reparaciones/{id}` | Una, con cliente y líneas de piezas | 200 |
| 7 | `POST /api/v1/reparaciones` | Alta. Estado `RECIBIDO`, `codigo` generado | 201 + `Location` |
| 8 | `POST /api/v1/reparaciones/{id}/presupuesto` | **Reserva las piezas**, fija la mano de obra, calcula el total y pasa a `PRESUPUESTADO` | 200 |
| 9 | `POST /api/v1/reparaciones/{id}/estado` | Cambia de estado respetando las transiciones | 200 |
| 10 | `POST /api/v1/reparaciones/{id}/cancelar` | Cancela y **devuelve las piezas** | 200 |
| 11 | `GET /api/v1/reparaciones/estadisticas` | Resumen (abajo) | 200 |

### El presupuesto

```json
POST /api/v1/reparaciones/3/presupuesto
{
  "manoObra": 85.00,
  "piezas": [
    { "piezaId": 4, "cantidad": 2 },
    { "piezaId": 7, "cantidad": 1 }
  ]
}
```

El servidor: comprueba que hay stock de **todas**, lo descuenta, copia los precios en las líneas, calcula `total = manoObra + Σ(precio × cantidad)` y pasa el estado a `PRESUPUESTADO`.

### Reglas de negocio

| # | Regla | Respuesta |
|:-:|---|:-:|
| R1 | La `matricula` cumple `\d{4}[A-Z]{3}` | **400** |
| R2 | El `nif` cumple `\d{8}[A-Z]` | **400** |
| R3 | El `codigo` lo genera el servidor: `REP-2026-000123`, **correlativo por año** | — |
| R4 | `entrada` no puede ser futura; `salida`, si existe, no anterior a `entrada` | **400** |
| R5 | El cliente debe existir | **400** |
| R6 | **Transiciones válidas únicamente**: `RECIBIDO → PRESUPUESTADO → EN_CURSO → TERMINADO → ENTREGADO`, y desde cualquiera salvo `ENTREGADO` a `CANCELADO` | **409** |
| R7 | Solo se puede presupuestar desde `RECIBIDO`, y **una sola vez** | **409** |
| R8 | **Si falta stock de una pieza, no se reserva ninguna** y el estado no cambia | **409** |
| R9 | Cancelar **devuelve todas las piezas** al almacén. Solo desde `PRESUPUESTADO`, `EN_CURSO` o `TERMINADO` | **409** |
| R10 | Pasar a `ENTREGADO` fija `salida = hoy` | — |
| R11 | `total` y `precio_unitario` los calcula **siempre el servidor** | — |
| R12 | No se puede borrar una pieza usada en alguna reparación, ni un cliente con reparaciones | **409** |

!!! danger "R8 es el resultado de aprendizaje del ejercicio"
    Un presupuesto con tres piezas donde falta stock de la tercera **no puede dejar reservadas las dos primeras**.

    ```java
    @Transactional
    public ReparacionResponse presupuestar(Long id, PresupuestoRequest req) { … }
    ```

    Y dentro, el `UPDATE` condicionado:

    ```sql
    UPDATE piezas SET stock = stock - :cantidad
     WHERE id = :id AND stock >= :cantidad
    ```

    **Cómo se comprueba, y cómo se corrige:** se apunta el stock de todas las piezas, se manda un presupuesto que falle en la última, y se vuelve a mirar. **Los dos listados tienen que ser idénticos.**

!!! tip "Pistas de implementación"
    - **R6** se modela con un `Map<Estado, Set<Estado>>` de transiciones permitidas, o con un `switch` con patrones de Java 25. Un `if` encadenado de seis ramas funciona pero se vuelve inmanejable.
    - **R3**: `SELECT COALESCE(MAX(CAST(SUBSTRING(codigo, 10) AS BIGINT)), 0) FROM reparaciones WHERE codigo LIKE 'REP-2026-%'`, como en el [proyecto B](05-proyectos.md#b3-el-repositorio-de-pedidos).
    - **R7**: «una sola vez» se consigue con la propia transición: solo se puede presupuestar desde `RECIBIDO`, y presupuestar te saca de `RECIBIDO`.
    - **R12** lo impiden ya las claves ajenas del esquema: captura `DataIntegrityViolationException` en el `@RestControllerAdvice` y devuelve 409.

### `GET /api/v1/reparaciones/estadisticas`

```json
{
  "total": 24,
  "porEstado": { "RECIBIDO": 3, "PRESUPUESTADO": 5, "EN_CURSO": 4,
                 "TERMINADO": 2, "ENTREGADO": 9, "CANCELADO": 1 },
  "facturadoEntregadas": 4820.50,
  "presupuestoMedio": 214.25,
  "diasMedioEnTaller": 4.8,
  "piezaMasUsada": { "codigo": "P-0045", "nombre": "Filtro de aceite", "unidades": 31 },
  "piezasSinStock": 2
}
```

---

## Rúbrica de calificación

**La misma para los tres enunciados y para el examen.** Sobre 10 puntos, y cada ítem se comprueba con una acción concreta: no hay margen de interpretación.

### Bloque 1 · Arquitectura por capas — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 1.1 | `grep -rn "ResponseEntity\|HttpStatus\|@PathVariable\|@RequestParam" src/main/java/**/services/` → **sin resultados** | **1,0** |
| 1.2 | `grep -rni "SELECT\|INSERT\|UPDATE\|DELETE FROM" src/main/java/**/controllers/ src/main/java/**/services/` → **sin resultados** | 0,5 |
| 1.3 | Existen las tres capas con su anotación: `@RestController`, `@Service`, `@Repository`, y el servicio se usa **a través de una interfaz** | 0,5 |
| 1.4 | Inyección **por constructor** y campos `final`. `grep -rn "@Autowired" src/main/java/` → **sin resultados** | 0,5 |

### Bloque 2 · API REST y códigos de estado — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 2.1 | Todos los endpoints de la tabla del enunciado existen y responden en la ruta indicada, **con prefijo de versión** | 0,5 |
| 2.2 | `GET` de un id inexistente → **404** (no 500 ni 200 con `null`) | 0,5 |
| 2.3 | `POST` correcto → **201** y cabecera **`Location`** con la URL del recurso | 0,5 |
| 2.4 | `DELETE` correcto → **204**; de un id inexistente → **404** (no 204) | 0,25 |
| 2.5 | Los **filtros opcionales** del endpoint 1 funcionan por separado y combinados, sin distinguir mayúsculas donde el enunciado lo pide | 0,25 |

### Bloque 3 · DTOs y validación — 2,0 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 3.1 | Ningún método de ningún controlador devuelve o recibe el modelo: **solo DTOs** | 0,5 |
| 3.2 | **Tres DTOs distintos** (respuesta / creación / actualización), y el de creación **sin `id` ni fechas** | 0,5 |
| 3.3 | Todos los `@RequestBody` llevan **`@Valid`**, y un POST inválido devuelve **400** | 0,5 |
| 3.4 | El 400 devuelve **los errores campo a campo**, no un mensaje genérico | 0,25 |
| 3.5 | Las reglas de formato del enunciado (R1–R4 según el caso) están **en el DTO** con anotaciones, no en el servicio | 0,25 |

### Bloque 4 · Reglas de negocio y errores — 2,5 puntos

| | Se comprueba con | Puntos |
|---|---|:-:|
| 4.1 | **Cada regla de negocio del enunciado** se cumple y devuelve el código indicado. *0,15 por regla, hasta 1,5* | **1,5** |
| 4.2 | Jerarquía de **excepciones propias** con `@ResponseStatus`. `grep -rn "ResponseStatusException" src/main/java/` → **sin resultados** | 0,5 |
| 4.3 | `@RestControllerAdvice` con handlers de validación, JSON ilegible, tipo de parámetro erróneo y `DataIntegrityViolationException` | 0,25 |
| 4.4 | **PATCH conserva** los campos que no llegan y **PUT los reemplaza**. Se comprueba con dos peticiones y comparando la respuesta | 0,25 |

### Bloque 5 · Persistencia con JDBC — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 5.1 | **Cero concatenación de valores en SQL**: todo con `:param` o `?`. Una sola concatenación de un valor → **0 en todo el bloque** | 0,4 |
| 5.2 | Los datos **sobreviven** a parar y arrancar la aplicación | 0,2 |
| 5.3 | Arranca **dos veces seguidas** sin error (`IF NOT EXISTS` y `MERGE` respetados) | 0,2 |
| 5.4 | Las operaciones que tocan **más de una tabla o fila** llevan `@Transactional`, y **se demuestra con el estado antes y después de un fallo** | 0,2 |

### Bloque 6 · Tests — 1,0 punto

| | Se comprueba con | Puntos |
|---|---|:-:|
| 6.1 | `./mvnw clean test` acaba en **BUILD SUCCESS**. Si no compila o hay tests comentados → **0 en todo el bloque** | 0,2 |
| 6.2 | Al menos **4 tests de servicio** con Mockito, uno por regla de negocio distinta | 0,4 |
| 6.3 | Al menos **3 tests de capa web** con `MockMvc`, incluido uno que compruebe el **400 de validación** | 0,4 |

!!! danger "Penalizaciones — se restan del total"
    | | |
    |---|:-:|
    | Concatenar un valor de entrada en una sentencia SQL | **−2,0** |
    | El `.jar` no se genera o no arranca | **−1,0** |
    | Aceptar del cliente un dato que debe calcular el servidor (total, precio congelado, estado inicial, código) | **−1,0** |
    | Un `GET` que modifica datos | **−1,0** |
    | Verbos en las URL (`/crear`, `/listar`, `/borrar/{id}`) | **−0,5** |
    | Credenciales o rutas absolutas escritas en el código | **−0,5** |
    | `System.out.println` en lugar de un `Logger` | **−0,25** |

!!! success "Nota mínima y cómo se reparte"
    Se aprueba con **5,0**, y hay un mínimo por bloque: **el bloque 1 no puede quedar en 0**. Un proyecto que funciona pero tiene la lógica en el controlador no demuestra el RA5, que es literalmente *«separar el código de presentación de la lógica de negocio»*.

    | Bloque | Peso | Qué mide del RA5 |
    |---|:-:|---|
    | 1 · Capas | 25 % | **El resultado de aprendizaje en sí** |
    | 2 · API REST | 20 % | El contrato con el cliente |
    | 3 · DTOs y validación | 20 % | La frontera entre presentación y dominio |
    | 4 · Reglas y errores | 25 % | Que la lógica de negocio está y funciona |
    | 5 · Persistencia | 10 % | Que se habla con la base de datos sin agujeros |
    | 6 · Tests | 10 % | Que se puede comprobar |

!!! info "Cómo se corrige, paso a paso"
    1. `./mvnw clean package` → bloque 6.1 y la penalización del `.jar`.
    2. Los cinco `grep` del bloque 1 y el 4.2 y 5.1 → **se decide un tercio de la nota en dos minutos**.
    3. `java -jar` y la colección de peticiones del enunciado → bloques 2 y 4.1.
    4. Un POST inválido → bloque 3.3 y 3.4.
    5. Parar, arrancar y volver a listar → bloque 5.2 y 5.3.
    6. Provocar el fallo de la operación transaccional y comparar el estado → bloque 5.4.
    7. Leer los DTOs y el mapeador → bloques 3.1, 3.2, 3.5 y 4.4.

    Se entrega el proyecto comprimido **sin `target/` ni `datos/`**, con un `README.md` de diez líneas que diga cómo arrancarlo.
