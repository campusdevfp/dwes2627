# Batería de ejercicios — UT5

**Dominio: una escuela de música.** Distinto del de clase (la tienda) a propósito.

**30 ejercicios agrupados por tema**, con solución. Los marcados con :material-file-sign: son del tipo que cae en el examen.

| Tema | Ejercicios | Sesiones |
|---|---|:-:|
| 1 · Qué hace JPA por debajo | E1–E5 | S1–S3 |
| 2 · Entidades y mapeo | E6–E10 | S4–S6 |
| 3 · Spring Data JPA | E11–E15 | S7–S11 |
| 4 · Relaciones | E16–E21 | S12–S16 |
| 5 · Transacciones e integridad | E22–E26 | S17–S20 |
| 6 · De H2 a producción | E27–E30 | S21–S23 |

---

# Tema 1 · Qué hace JPA por debajo

### E1 ● — Las cuatro siglas

Coloca cada una en su sitio: JDBC, JPA, Hibernate, Spring Data JPA. ¿Cuál se puede sustituir sin tocar tu código?

??? success "Solución"

    ```
    Tu código  →  Spring Data JPA  →  JPA (especificación)  →  Hibernate  →  JDBC  →  BD
    ```

    - **JDBC**: la API estándar de acceso a bases de datos. Lo más bajo.
    - **JPA**: una **especificación**, solo interfaces y anotaciones.
    - **Hibernate**: la **implementación** de esa especificación.
    - **Spring Data JPA**: genera los repositorios a partir de interfaces.

    Se puede cambiar **Hibernate** por EclipseLink sin tocar tu código, porque programas contra JPA. Esa es toda la gracia de que exista una especificación.


### E2 ●● — JDBC a pelo

Escribe `findByGenero` con `PreparedStatement`, sin JPA. Cuenta las líneas.

??? success "Solución"

    ```java
    public List<Curso> porGenero(String genero) {
        var sql = "select id, nombre, genero, precio from curso where genero = ?";
        var cursos = new ArrayList<Curso>();
        try (var con = dataSource.getConnection();
             var ps = con.prepareStatement(sql)) {
            ps.setString(1, genero);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    cursos.add(new Curso(rs.getLong("id"), rs.getString("nombre"),
                                         rs.getString("genero"), rs.getBigDecimal("precio")));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(e);
        }
        return cursos;
    }
    ```
    Unas 18 líneas. Con Spring Data: `List<Curso> findByGenero(String genero);` — **una**.

    Que hagan las dos y comparen. Y que se fijen en cuántos sitios hay donde equivocarse: el nombre de columna, el tipo, cerrar recursos, el mapeo.


### E3 ●●● — La inyección SQL, en directo

Escribe la misma consulta concatenando y provoca el ataque.

??? success "Solución"

    ```java
    var sql = "select * from curso where genero = '" + genero + "'";   // MAL 
    ```
    ```java
    repositorio.porGenero("' OR '1'='1");
    ```
    Devuelve **la tabla entera**. Y con `"'; drop table curso; --"` en un motor que permita varias sentencias, se pierde la tabla.

    Con `PreparedStatement`, la cadena viaja **como dato**, nunca como código, y el ataque devuelve cero filas.

    Vale la pena hacerlo en clase: se recuerda toda la vida.


### E4 ●● — Los cuatro estados

Identifica en qué estado está el objeto en cada línea.

```java
var c = new Curso("Piano B1", …);      // (1)
repo.save(c);                          // (2)
c.setPrecio(new BigDecimal("200"));    // (3) dentro de @Transactional
                                       // (4) al salir del método
```

??? success "Solución"

    1. **Transitoria** (*transient*): objeto normal, JPA no lo conoce.
    2. **Gestionada** (*managed*): está en el contexto de persistencia.
    3. Sigue **gestionada**, y el cambio queda anotado.
    4. Al confirmar, el *dirty checking* detecta el cambio y **lanza el `UPDATE` solo**. Después, **separada** (*detached*).

    El punto 3-4 es el que sorprende: **no hace falta llamar a `save`**. Y el que rompe cosas: fuera de la transacción, ese `setPrecio` no llega a la base de datos.


### E5 ●●● — Demuestra el *dirty checking*

Escribe un test que modifique una entidad sin llamar a `save` y compruebe que el cambio se guarda.

??? success "Solución"

    ```java
    @Test @Transactional
    void elCambioSeGuardaSinLlamarASave() {
        var curso = repo.save(new Curso("Piano B1", new BigDecimal("150")));
        var id = curso.getId();

        var recuperado = repo.findById(id).orElseThrow();
        recuperado.setPrecio(new BigDecimal("200"));      // sin save

        em.flush(); em.clear();
        assertThat(repo.findById(id).orElseThrow().getPrecio())
            .isEqualByComparingTo("200");
    }
    ```
    El `flush()` fuerza el volcado y el `clear()` vacía el contexto, para que el `findById` siguiente vaya de verdad a la base de datos y no devuelva el objeto en memoria. Sin ese `clear`, el test pasa aunque nada se haya guardado.


---

# Tema 2 · Entidades y mapeo

### E6 ● — Mapea `Clase`

Convierte en entidad: id generado, nombre obligatorio de 100, precio con dos decimales, fecha y nivel.

??? success "Solución"

    ```java
    @Entity
    public class Clase {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 100)
        private String nombre;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal precio;

        private LocalDate fecha;

        @Enumerated(EnumType.STRING)
        private Nivel nivel;

        protected Clase() { }
        public Clase(String nombre, BigDecimal precio, Nivel nivel) { … }
    }
    ```
    El constructor `protected` sin argumentos **no es opcional**: sin él, `No default constructor for entity`.


### E7 ●● — Por qué no puede ser un `record`

Intenta anotar un `record` con `@Entity` y explica el error.

??? success "Solución"

    ```java
    @Entity
    public record Clase(@Id Long id, String nombre) { }   // MAL 
    ```
    Falla al arrancar. Dos razones:

    1. Un `record` es **final** y JPA necesita generar una subclase para el *proxy* de carga perezosa.
    2. Es **inmutable** y no tiene constructor sin argumentos: JPA crea el objeto vacío y lo rellena por reflexión.

    Es la misma familia de problema que en la UT8 con los formularios, pero por un mecanismo distinto: allí es el enlace de datos, aquí el proveedor de persistencia. Los DTO **sí** siguen siendo `record`.


### E8 ●●● — El enumerado traicionero

Mapea `Nivel` con `ORDINAL`, guarda datos, añade un valor en medio y observa el desastre.

??? success "Solución"

    ```java
    enum Nivel { INICIACION, MEDIO, AVANZADO }        // ORDINAL: 0, 1, 2
    ```
    Guardas «Piano MEDIO» → en la base de datos hay un `1`.

    Meses después:
    ```java
    enum Nivel { INICIACION, BASICO, MEDIO, AVANZADO }   // ahora MEDIO es 2
    ```
    Todos los cursos que eran `MEDIO` **pasan a ser `BASICO`**. Sin error, sin aviso, sin rastro.

    ```java
    @Enumerated(EnumType.STRING)     // guarda "MEDIO"
    ```
    Ocupa unos bytes más y es inmune a reordenaciones. No hay ningún caso en el que compense `ORDINAL`.


### E9 ●●● — `equals` y el `HashSet`

Implementa `equals`/`hashCode` solo con el `id` y demuestra que rompe un `HashSet`.

??? success "Solución"

    ```java
    @Test void unaEntidadNuevaSePierdeEnUnSet() {
        var c = new Clase("Piano B1", …);
        Set<Clase> set = new HashSet<>();
        set.add(c);                       // hashCode con id == null
        repo.save(c);                     // ahora id = 1 → cambia el hashCode
        assertThat(set.contains(c)).isFalse();   // está dentro y no lo encuentra
    }
    ```

    Solución aceptada:
    ```java
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Clase c)) return false;
        return id != null && id.equals(c.id);
    }
    @Override public int hashCode() { return getClass().hashCode(); }   // constante
    ```
    Un `hashCode` constante degrada el `HashSet` a lista, pero **es correcto**, que es lo que importa. La alternativa buena es usar una clave de negocio estable.


### E10 ●● — Tipos que se eligen mal

Corrige esta entidad: cinco decisiones de tipo son incorrectas.

```java
@Entity class Alumno {
    @Id Long id;
    String dni;
    double cuotaMensual;
    Date fechaAlta;
    String nivel;
    String observaciones;
}
```

??? success "Solución"

    | Campo | Problema | Corrección |
    |---|---|---|
    | `id` | Sin `@GeneratedValue` | Añadirlo |
    | `dni` | Sin restricción de unicidad | `@Column(unique = true, nullable = false, length = 9)` |
    | `cuotaMensual` | `double` para dinero | `BigDecimal` con `precision`/`scale` |
    | `fechaAlta` | `java.util.Date`, obsoleto | `LocalDate` |
    | `nivel` | Texto libre donde hay valores cerrados | `@Enumerated(STRING)` sobre un `enum` |
    | `observaciones` | Puede ser muy largo | `@Lob` o `@Column(length = 2000)` |

    El `double` es el que más se cuela y el que peor se ve: `0.1 + 0.2` no es `0.3`, y en una cuota mensual eso acaba en una reclamación.


---

# Tema 3 · Spring Data JPA

### E11 ● — El repositorio que no se escribe

Crea `ClaseRepositorio` y comprueba qué métodos tienes sin escribir ninguno.

??? success "Solución"

    ```java
    public interface ClaseRepositorio extends JpaRepository<Clase, Long> { }
    ```
    Ya tienes `save`, `saveAll`, `findById`, `findAll`, `findAllById`, `count`, `existsById`, `delete`, `deleteById`, `deleteAll`, `flush`, `saveAndFlush`, y las versiones con `Pageable` y `Sort`.

    No hace falta `@Repository`: Spring Data registra el bean solo. Y **nadie implementa esa interfaz**: se genera un *proxy* en tiempo de ejecución.


### E12 ●● — Seis consultas derivadas

Escribe: por nivel, por nombre parcial sin distinguir mayúsculas, más baratas que X ordenadas, entre dos fechas, activas de un profesor, y comprobar si existe un nombre.

??? success "Solución"

    ```java
    List<Clase> findByNivel(Nivel nivel);
    List<Clase> findByNombreContainingIgnoreCase(String texto);
    List<Clase> findByPrecioLessThanOrderByPrecioAsc(BigDecimal max);
    List<Clase> findByFechaBetween(LocalDate desde, LocalDate hasta);
    List<Clase> findByProfesorIdAndActivaTrue(Long profesorId);
    boolean existsByNombre(String nombre);
    ```
    La última es la que más se falla: para saber si existe, `existsBy` hace un `count` y devuelve un booleano. `findByNombre(...) != null` se trae la fila entera para nada.


### E13 ●●● — Cuando el nombre ya no cabe

`findByNivelAndActivaTrueAndPrecioLessThanOrderByFechaDesc` funciona pero es ilegible. Reescríbelo.

??? success "Solución"

    ```java
    @Query("""
           select c from Clase c
           where c.nivel = :nivel and c.activa = true and c.precio < :max
           order by c.fecha desc
           """)
    List<Clase> buscar(@Param("nivel") Nivel nivel, @Param("max") BigDecimal max);
    ```
    El criterio práctico: **si el nombre del método pasa de unas 60 letras o necesita agregación, `@Query`**. Los nombres derivados son cómodos hasta que dejan de leerse.


### E14 ●● — Filtros opcionales

Un buscador con tres filtros que pueden venir o no venir.

??? success "Solución"

    ```java
    @Query("""
           select c from Clase c
           where (:nivel    is null or c.nivel = :nivel)
             and (:profesor is null or c.profesor.id = :profesor)
             and (:texto    is null or lower(c.nombre) like lower(concat('%', :texto, '%')))
           """)
    Page<Clase> buscar(@Param("nivel") Nivel nivel, @Param("profesor") Long profesor,
                       @Param("texto") String texto, Pageable pageable);
    ```
    El patrón `:param is null or condición` es la forma sencilla. Para muchos filtros, `Specification` o QueryDSL; pero con tres o cuatro esto es más legible y no hay que aprender otra API.


### E15 ●●● — Proyección y agregado

Devuelve, por nivel: cuántas clases hay, el precio medio y el máximo. **Sin cargar entidades.**

??? success "Solución"

    ```java
    public interface ResumenNivel {
        Nivel getNivel();
        Long getTotal();
        BigDecimal getMedia();
        BigDecimal getMaximo();
    }

    @Query("""
           select c.nivel as nivel, count(c) as total,
                  avg(c.precio) as media, max(c.precio) as maximo
           from Clase c group by c.nivel order by count(c) desc
           """)
    List<ResumenNivel> resumenPorNivel();
    ```
    Los alias del `select` **tienen que coincidir** con los nombres de los *getters* sin el `get`. Si escribes `count(c) as cuenta` y el método es `getTotal()`, no funciona y el mensaje no lo dice claro.


---

# Tema 4 · Relaciones

### E16 ●● — La primera relación

`Profesor` 1—N `Clase`, con el lado dueño correcto.

??? success "Solución"

    ```java
    @Entity public class Profesor {
        @OneToMany(mappedBy = "profesor")
        private List<Clase> clases = new ArrayList<>();
    }

    @Entity public class Clase {
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "profesor_id")
        private Profesor profesor;
    }
    ```
    El **lado dueño** es `Clase`, porque tiene la clave ajena. `mappedBy = "profesor"` dice: *«la relación la gestiona el campo `profesor` de la otra clase»*.

    El `fetch = LAZY` en el `@ManyToOne` es deliberado: por defecto es `EAGER` y arrastra el profesor en cada consulta de clase, la necesites o no.


### E17 ●●● — Los dos lados desincronizados

Añade una clase a la lista del profesor sin asignar el profesor a la clase. Observa qué se guarda.

??? success "Solución"

    ```java
    profesor.getClases().add(clase);      // solo el lado inverso
    repo.save(profesor);
    ```
    La clase se guarda con `profesor_id` **a null**. El lado inverso no manda.

    ```java
    public void anadir(Clase c) { clases.add(c); c.setProfesor(this); }
    public void quitar(Clase c) { clases.remove(c); c.setProfesor(null); }
    ```
    Estos métodos no son cosmética: son la única forma de que el objeto en memoria y la base de datos digan lo mismo. Que lo comprueben quitándolos.


### E18 ●● — Cascada y huérfanos

`Matricula` colgando de `Alumno`: al borrar el alumno se borran sus matrículas, y al quitar una de la lista también.

??? success "Solución"

    ```java
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matricula> matriculas = new ArrayList<>();
    ```
    Son **dos cosas distintas**, y se confunden siempre:

    - `cascade = ALL` propaga las operaciones del padre a los hijos: borrar el alumno borra sus matrículas.
    - `orphanRemoval = true` borra el hijo **cuando se saca de la colección**, aunque el padre siga vivo.

    Sin `orphanRemoval`, `alumno.getMatriculas().remove(m)` no borra nada: deja la fila con la clave ajena colgando.


### E19 ●●● — Cuenta las consultas del N+1

Lista 50 clases con el nombre de su profesor, cuenta las consultas y arréglalo de dos formas.

??? success "Solución"

    ```yaml
    logging.level.org.hibernate.SQL: DEBUG
    ```
    Salen **51**: una del listado y una por cada profesor.

    ```java
    // A · JPQL explícito
    @Query("select c from Clase c join fetch c.profesor")
    List<Clase> todasConProfesor();

    // B · declarativo, reutiliza la consulta derivada
    @EntityGraph(attributePaths = "profesor")
    List<Clase> findByNivel(Nivel nivel);
    ```
    Después: **1**.

    Es el ejercicio más rentable de la unidad. Que peguen en la entrega el recuento antes y después.


### E20 ●●● — `join fetch` con paginación

Intenta paginar un `join fetch` de una colección y explica el aviso.

??? success "Solución"

    ```java
    @Query("select p from Profesor p join fetch p.clases")
    Page<Profesor> todos(Pageable pageable);          // OJO HHH000104
    ```
    El aviso dice: *«firstResult/maxResults specified with collection fetch; applying in memory»*. Traducción: **se trae la tabla entera y pagina en memoria**. Con 100.000 filas, se acabó.

    La solución en dos pasos:
    ```java
    @Query("select p.id from Profesor p")
    Page<Long> idsPaginados(Pageable pageable);

    @Query("select p from Profesor p join fetch p.clases where p.id in :ids")
    List<Profesor> conClases(@Param("ids") List<Long> ids);
    ```
    Se pagina sobre los identificadores y después se cargan las relaciones. Es el patrón estándar y conviene conocerlo.


### E21 ●●● — Cuando `@ManyToMany` deja de valer

Modela alumnos y clases con `@ManyToMany`. Después aparece el requisito «guardar la fecha de matrícula y la nota». Rehazlo.

??? success "Solución"

    ```java
    @Entity
    public class Matricula {
        @EmbeddedId private MatriculaId id;
        @ManyToOne @MapsId("alumnoId") private Alumno alumno;
        @ManyToOne @MapsId("claseId")  private Clase clase;
        private LocalDate fecha;
        private BigDecimal nota;
    }

    @Embeddable
    public record MatriculaId(Long alumnoId, Long claseId) implements Serializable {}
    ```
    La moraleja, dicha en voz alta: **una relación N—M pura casi nunca sobrevive al segundo *sprint***, porque el negocio siempre acaba queriendo guardar algo sobre la relación. Modelarla desde el principio como entidad ahorra una migración.


---

# Tema 5 · Transacciones e integridad

### E22 ●● — La operación que no puede quedar a medias

`matricular(alumnoId, claseId)`: comprueba plazas, crea la matrícula, descuenta la plaza y registra el movimiento.

??? success "Solución"

    ```java
    @Transactional
    public MatriculaDto matricular(Long alumnoId, Long claseId) {
        var clase = claseRepo.findById(claseId).orElseThrow(ClaseNoEncontrada::new);
        if (clase.getPlazasLibres() <= 0) throw new SinPlazasException(claseId);

        clase.setPlazasLibres(clase.getPlazasLibres() - 1);   // dirty checking
        var m = matriculaRepo.save(new Matricula(alumnoId, claseId, LocalDate.now()));
        movimientoRepo.save(new Movimiento(alumnoId, "MATRICULA", claseId));
        return mapper.aDto(m);
    }
    ```
    Prueba obligatoria: forzar el fallo del tercer paso y comprobar que la plaza **no** quedó descontada.

    Dos preguntas que separan el aprobado del notable: ¿por qué no hace falta `save(clase)`? ¿Y qué pasa si `SinPlazasException` extiende `Exception` en vez de `RuntimeException`?


### E23 ●●● — La transacción que no se aplica

Este código no revierte nada. Explica por qué.

```java
@Service
public class MatriculaServicio {
    public void matricularVarias(List<Long> ids) {
        ids.forEach(this::matricularUna);      // OJO
    }
    @Transactional
    public void matricularUna(Long id) { … }
}
```

??? success "Solución"

    **Autoinvocación.** La llamada `this::matricularUna` no pasa por el *proxy* de Spring, así que `@Transactional` **no hace nada**.

    Tres soluciones, de mejor a peor:

    1. Poner `@Transactional` en `matricularVarias` (que además es lo correcto: la operación de negocio es matricular todas).
    2. Extraer `matricularUna` a otro bean e inyectarlo.
    3. Autoinyectarse el propio servicio. Funciona y es feo.

    Es el fallo más silencioso de todo el módulo: no da error, simplemente no protege nada.


### E24 ●●● — La edición que se pierde

Demuestra con un test que dos ediciones simultáneas pierden una, y arréglalo.

??? success "Solución"

    ```java
    @Version private Long version;
    ```
    ```java
    @Test void dosEdicionesConcurrentesNoSePisan() {
        var a = repo.findById(1L).orElseThrow();
        var b = repo.findById(1L).orElseThrow();      // otra copia, misma versión
        a.setPrecio(new BigDecimal("200")); repo.saveAndFlush(a);
        b.setPrecio(new BigDecimal("300"));
        assertThatThrownBy(() -> repo.saveAndFlush(b))
            .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
    ```
    Sin `@Version` el test falla porque **no salta nada**: el precio queda en 300 y el cambio a 200 desaparece sin rastro. Ese silencio es el problema.

    Ampliación: capturarla en el controlador y devolver `409 Conflict` pidiendo al cliente que recargue.


### E25 ●● — La integridad va en la base de datos

Impide dos matrículas del mismo alumno en la misma clase, y explica por qué no basta con comprobarlo en Java.

??? success "Solución"

    ```java
    @Table(name = "matricula",
           uniqueConstraints = @UniqueConstraint(columnNames = {"alumno_id", "clase_id"}))
    ```
    ```java
    catch (DataIntegrityViolationException e) {
        throw new MatriculaDuplicadaException(alumnoId, claseId);   // → 409
    }
    ```
    Entre el `existsBy...` y el `save` cabe otra petición. La comprobación en Java sirve para dar un mensaje decente; **la garantía la da la base de datos**.

    Es el mismo razonamiento que en la UT8 con la unicidad de los formularios.


### E26 ●●● — La baja que no borra

Cancelar una matrícula no la elimina: la marca, guarda quién y cuándo, y libera la plaza.

??? success "Solución"

    ```java
    @Entity public class Matricula {
        @Enumerated(STRING) private Estado estado;      // ACTIVA, CANCELADA
        private Instant canceladaEn;
        private String canceladaPor;
    }
    ```
    ```java
    @Transactional
    public void cancelar(Long id, String usuario) {
        var m = repo.findById(id).orElseThrow(MatriculaNoEncontrada::new);
        if (m.getEstado() == Estado.CANCELADA) throw new YaCanceladaException(id);
        m.cancelar(usuario, Instant.now());
        m.getClase().setPlazasLibres(m.getClase().getPlazasLibres() + 1);
    }
    ```
    ```java
    List<Matricula> findByClaseIdAndEstado(Long claseId, Estado estado);
    ```
    El borrado lógico conserva el histórico, que es lo que pide cualquier auditoría. Y obliga a que **todas** las consultas filtren por estado: si una se olvida, aparecen matrículas fantasma.


---

# Tema 6 · De H2 a producción

### E27 ●● — `ddl-auto` no sabe migrar

Añade una columna obligatoria a una tabla con datos usando `update`, y después hazlo bien.

??? success "Solución"

    Con `ddl-auto: update`, añadir `@Column(nullable = false) private Integer plazasMinimas;` **falla al arrancar**: la columna se crea, las filas existentes quedan a `null` y la restricción no se puede aplicar.

    ```sql
    -- V2__anadir_plazas_minimas.sql
    alter table clase add column plazas_minimas integer;
    update clase set plazas_minimas = 3 where plazas_minimas is null;
    alter table clase alter column plazas_minimas set not null;
    ```
    Esas tres líneas son la lección entera: `update` habría hecho la primera y **jamás** las otras dos, ni sabría que hacen falta.


### E28 ●● — Flyway desde cero

Pasa el proyecto a migraciones versionadas con `validate`.

??? success "Solución"

    ``` { .text .sinajuste }
    src/main/resources/db/migration/
    ├── V1__esquema_inicial.sql
    └── V2__anadir_plazas_minimas.sql
    ```
    ```yaml
    spring:
      jpa.hibernate.ddl-auto: validate
      flyway.enabled: true
    ```
    Flyway crea `flyway_schema_history` con la suma de verificación de cada fichero. **Modificar una migración ya aplicada rompe el arranque**, y es deliberado: los entornos que ya la ejecutaron no la volverían a pasar.


### E29 ●● — PostgreSQL con Docker

Levanta PostgreSQL, arranca la aplicación contra él y comprueba que las migraciones se aplican.

??? success "Solución"

    ```yaml
    # compose.yaml
    services:
      db:
        image: postgres:17-alpine
        environment:
          POSTGRES_DB: escuela
          POSTGRES_PASSWORD: ${DB_PASSWORD}
        ports: ["5432:5432"]
        volumes: ["datos:/var/lib/postgresql/data"]
    volumes: { datos: }
    ```
    ```yaml
    # application-prod.yml
    spring.datasource:
      url: jdbc:postgresql://localhost:5432/escuela
      username: postgres
      password: ${DB_PASSWORD}
    ```
    El `volumes` es lo que hace que los datos sobrevivan a `docker compose down`. Sin él, cada reinicio empieza de cero — que está bien en desarrollo y es una catástrofe en producción.


### E30 ●●● — Tests con `@DataJpaTest`

Tres tests: una consulta derivada, una relación cargada sin N+1 y una restricción que debe fallar.

??? success "Solución"

    ```java
    @DataJpaTest
    class ClaseRepositorioTest {

        @Autowired ClaseRepositorio repo;
        @Autowired TestEntityManager em;

        @Test void buscaPorNivel() {
            em.persist(new Clase("Piano B1", Nivel.MEDIO, …));
            em.persist(new Clase("Violín A1", Nivel.INICIACION, …));
            assertThat(repo.findByNivel(Nivel.MEDIO)).hasSize(1);
        }

        @Test void cargaElProfesorEnUnaSolaConsulta() {
            // … con @EntityGraph; se comprueba con statistics o contando en el log
            assertThat(repo.todasConProfesor()).allSatisfy(
                c -> assertThat(c.getProfesor().getNombre()).isNotNull());
        }

        @Test void elDniDuplicadoFalla() {
            em.persist(new Alumno("12345678Z", "Ana"));
            assertThatThrownBy(() -> em.persistAndFlush(new Alumno("12345678Z", "Luis")))
                .isInstanceOf(PersistenceException.class);
        }
    }
    ```
    `@DataJpaTest` levanta **solo** la capa de datos y **revierte al terminar**, así que los tests no se contaminan entre sí. El `persistAndFlush` es necesario en el tercero: sin `flush`, la restricción no se comprueba hasta el final.


---

## Cómo usarlos en clase

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E6 · E11 · E16 · E22 · E27 |
| Taller de la sesión, 25-30 min | E2 · E10 · E12 · E18 · E25 · E29 |
| Los que hay que hacer sí o sí | **E8 · E9 · E17 · E19 · E23** |
| Para quien va sobrado | E5 · E20 · E21 · E24 · E26 · E30 |
| Repaso antes del examen | E17 · E19 · E22 · E23 · E25 |

!!! tip "Los tres que hay que hacer aunque no dé tiempo a nada más"
    **E19** (contar el N+1), **E23** (la transacción que no se aplica) y **E8** (el enumerado traicionero).

    Los tres comparten una característica: **el código funciona y está mal**. No hay excepción, no hay aviso, no hay pista. Solo se ven si sabes qué mirar, y eso es exactamente lo que distingue a alguien que sabe JPA de alguien que ha copiado anotaciones.
