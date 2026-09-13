# Preparar el examen práctico — UT5

**:material-laptop: Examen práctico · 100 % de la nota del RA6 · 2 sesiones (110 min)**

!!! tip "Se examina con chuleta"
    En el examen se te entrega impresa la **[chuleta de los prácticos del 1.er trimestre](../chuleta-examen-1t.md)**, y no puedes usar nada más.

    Está publicada desde septiembre: **entrena con ella**. Trae sintaxis, no criterio — lo que se evalúa no viene en la hoja.

## Formato

Se te entrega un **esqueleto** con:

- Los `record` del modelo de dominio y las firmas de los repositorios y servicios.
- Un `compose.yaml` con PostgreSQL listo.
- Un `pom.xml` con las dependencias puestas.
- Un fichero de datos de partida.
- `autocomprobar.sh`.

Entregas un `.zip` **sin la carpeta `target`**. El dominio será distinto al de clase y al de la batería.

## Qué se te va a pedir

| Bloque | Qué |
|---|---|
| **Mapeo** | Convertir el modelo en entidades: tipos, restricciones, enums, fechas automáticas |
| **Relaciones** | Al menos una `@ManyToOne` / `@OneToMany` bidireccional con cascada |
| **Consultas** | Derivadas, una `@Query` con filtros opcionales y una agregada |
| **Rendimiento** | Resolver un N+1 que el enunciado te pide detectar |
| **Transacciones** | Una operación de negocio con varias escrituras y sus códigos de error |
| **Integridad** | Restricción en la base de datos + captura de la violación |
| **Producción** | Migración de Flyway y `ddl-auto: validate` |
| **Tests** | Al menos tres `@DataJpaTest` |

## Rúbrica

| # | Criterio | Peso | Qué se mira |
|---|---|---:|---|
| 1 | **Arranca y crea el esquema** *(eliminatorio)* | 10 % | La aplicación levanta y las tablas existen |
| 2 | Mapeo de entidades | 20 % | Tipos correctos, `EnumType.STRING`, `BigDecimal`, restricciones, `equals` por id |
| 3 | Relaciones | 20 % | Bidireccional con los dos lados, `LAZY`, cascada y `orphanRemoval` donde toca |
| 4 | Consultas | 20 % | Derivadas correctas, filtros opcionales, agregada sin traer entidades |
| 5 | Transacciones e integridad | 20 % | `@Transactional` en el sitio, 404 antes que 409, restricción en la BD |
| 6 | Producción y tests | 10 % | Flyway con `validate`, tres tests en verde |

**Aprobado a partir de 5.** El criterio 1 es eliminatorio: si no arranca, es un 0.

## Los ocho errores que más cuestan

1. **Devolver la entidad desde el controlador.** `LazyInitializationException` al serializar, y campos internos expuestos. Siempre DTO.
2. **Dejar `@ManyToOne` en `EAGER`** (es el valor por defecto). Trae media base de datos en cada consulta.
3. **No resolver el N+1** que pide el enunciado. Se comprueba contando los `select`, así que no se puede disimular.
4. **Actualizar solo un lado** de la relación bidireccional: no se guarda el vínculo.
5. **`double` para el dinero** en vez de `BigDecimal`.
6. **`@Enumerated` por defecto** (ordinal) en vez de `STRING`.
7. **Comprobar el duplicado solo en el servicio**, sin restricción en la base de datos.
8. **Dejar `ddl-auto: update`** con Flyway puesto: se pisan y el esquema queda impredecible.

## Cómo prepararte

1. Haz los **14 ejercicios** de la [batería](ejercicios.md), sobre todo el **E9** (contar consultas), el **E11** (transacción con códigos) y el **E14**, que es un simulacro.
2. Termina el [proyecto de aula](practicas.md): si migraste la TiendaAPI entera, el examen es lo mismo con otro dominio.
3. Deja `show-sql: true` mientras practicas. **Saber leer el SQL que genera Hibernate es la mitad de esta unidad.**
4. Ten la [chuleta](chuleta.md) a mano: en 110 minutos no da tiempo a buscar la sintaxis de `@JoinColumn`.

## Simulacro

!!! example "Enunciado de muestra · gestión de una clínica dental"
    Entidades: `Paciente`, `Dentista`, `Tratamiento` y `Cita` (paciente, dentista, tratamiento, fecha, estado, importe).

    1. Mapea las cuatro con sus restricciones e índices.
    2. `Cita` con relaciones a las otras tres, todas `LAZY`.
    3. Consultas: citas de un paciente; búsqueda por rango de fechas y estado, paginada y con filtros opcionales; y la facturación por dentista en un mes.
    4. `pedirCita(pacienteId, dentistaId, tratamientoId, fecha)`: 404 si algo no existe, **409 si el dentista ya tiene cita a esa hora**, y el importe se copia del tratamiento.
    5. Restricción de unicidad `(dentista, fecha)` en la base de datos.
    6. Una migración de Flyway con el esquema completo.
    7. Tres `@DataJpaTest`: una consulta, la restricción y el N+1 resuelto.

??? tip "Pistas (no la solución)"

    - La regla del solapamiento de citas se comprueba con `existsByDentistaAndFecha`, y **además** con la restricción `UNIQUE`: entre la comprobación y el `save` cabe otra petición.
    - El importe se **copia** del tratamiento al crear la cita. Si lo lees del tratamiento al facturar, subir la tarifa cambiaría las facturas del pasado.
    - Para la facturación por dentista: `SELECT new ...Dto(d.id, d.nombre, COALESCE(SUM(c.importe),0)) ... LEFT JOIN ... GROUP BY`. El `LEFT JOIN` y el `COALESCE` son para que salgan también los dentistas sin citas.
    - El N+1 aparecerá al listar citas y tocar el paciente de cada una. `JOIN FETCH` o `@EntityGraph`.
    - Empieza por el mapeo y **arranca en cuanto tengas una entidad**. Si dejas el arranque para el final y algo no valida, te quedas sin el criterio eliminatorio.

