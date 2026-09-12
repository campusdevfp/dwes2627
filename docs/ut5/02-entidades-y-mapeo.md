# Entidades y mapeo

> Una entidad es una clase que representa una fila. El mapeo es decirle a JPA **qué columna es cada campo**. Aquí es donde se deciden cosas que luego cuesta mucho cambiar.

## 1. Lo mínimo: `@Entity` y `@Id`

```java
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

Con eso ya es una entidad. Todo lo demás son matices, pero **matices que se pagan**.

## 2. Cómo se genera la clave

| Estrategia | Qué hace | Cuándo |
|---|---|---|
| `IDENTITY` | La base de datos asigna el id al insertar (`AUTO_INCREMENT`) | MySQL, H2, SQL Server |
| `SEQUENCE` | Usa una secuencia de la base de datos | **PostgreSQL, Oracle** |
| `AUTO` | Que decida Hibernate | Prototipos |
| `TABLE` | Simula la secuencia con otra tabla | Nunca, salvo obligación |

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "producto_seq")
@SequenceGenerator(name = "producto_seq", sequenceName = "producto_seq", allocationSize = 50)
private Long id;
```

!!! warning "`IDENTITY` mata las inserciones por lotes"
    Con `IDENTITY`, Hibernate necesita el id **inmediatamente** después de cada `INSERT`, así que no puede agrupar varias inserciones en una sola llamada. Con `SEQUENCE` y `allocationSize = 50` reserva 50 ids de golpe y mete las filas por tandas. Insertando 10.000 productos, la diferencia es de minutos a segundos.

**El tipo del id: `Long`, no `long`.** Un objeto recién creado y aún no guardado tiene el id a `null`; con el primitivo sería `0`, y no podrías distinguir «sin guardar» de «el id es 0».

## 3. Columnas, tipos y nombres

Esta es **la entidad de referencia de la unidad**. Pincha en los números para ver por qué cada línea está como está.

``` { .java .annotate title="Producto.java" hl_lines="14 18" }
@Entity
@Table(name = "productos",
       indexes = @Index(name = "idx_producto_categoria", columnList = "categoria"),  // (1)!
       uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "categoria"}))  // (2)!
public class Producto {

    @Column(nullable = false, length = 100)
    private String nombre;                   // (3)!

    @Column(name = "categoria", nullable = false, length = 40)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;               // (4)!

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;                   // (5)!

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;                // (6)!

    @Lob
    private String descripcion;              // (7)!

    @Transient
    private double precioConIva;             // (8)!
}
```

1.  **Índices donde vayas a filtrar.** Si tu API permite `?categoria=movilidad`, esa columna necesita índice. Sin él, cada llamada recorre la tabla entera: con 200 filas no lo notas, con 200.000 sí.

2.  **La restricción de unicidad vive en la base de datos, no en el `service`.** Comprobar «¿ya existe?» en Java tiene una ventana de carrera entre la consulta y el `INSERT`. La base de datos no la tiene.

3.  `nullable = false` genera `NOT NULL` en la tabla; `length = 100` genera `VARCHAR(100)`. Sin `length`, Hibernate pone 255 por defecto «porque sí».

4.  **Dinero: `BigDecimal`, nunca `double`.** Ya viste en la UT2 que `0.1 + 0.2` no da `0.3`. En una factura eso son céntimos que no cuadran. `precision = 10, scale = 2` = 10 dígitos, 2 de ellos decimales.

5.  **`EnumType.STRING` guarda `"ACTIVO"`; el valor por defecto guardaría `0`.** El día que alguien reordene las constantes del enum, todos los datos existentes cambian de significado **en silencio**. Este es de los errores más caros de reparar de todo el módulo.

6.  `updatable = false` hace que Hibernate excluya la columna del `UPDATE`: la fecha de creación no se puede reescribir ni por accidente.

7.  `@Lob` = texto largo (`CLOB`/`TEXT`), para cuando 255 caracteres se quedan cortos.

8.  **`@Transient` para lo calculado.** El precio con IVA se calcula, no se guarda. Si no lo marcas, Hibernate intenta crear una columna para él.

!!! danger "Los dos de examen"
    `BigDecimal` para dinero y `@Enumerated(EnumType.STRING)`. Si en el examen aparece un `double precio` o un enum sin anotar, es un fallo grave, no un descuido de estilo.

## 4. Por qué una entidad no puede ser un `record`

Te va a apetecer, porque llevas seis unidades usándolos. No se puede, y las razones son buenas:

| Lo que exige JPA | Lo que hace un `record` |
|---|---|
| Constructor sin argumentos | No lo tiene |
| Poder asignar campos por reflexión | Sus campos son `final` |
| Poder crear *proxies* heredando | Es `final` |
| Identidad estable mientras el id cambia | `equals` compara **todos** los valores |

Ese último punto es el más sutil y el más importante:

```java
var p = new Producto("Casco", "seguridad", ...);   // id = null
conjunto.add(p);
repositorio.save(p);                                // ahora id = 7
conjunto.contains(p);                               // ¿lo encuentra?
```

Si `equals` y `hashCode` dependen del id, el objeto **cambia de hash** al guardarse y desaparece del conjunto. Es un bug clásico y desagradable de encontrar.

**La forma correcta** en una entidad:

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Producto otro)) return false;
    return id != null && id.equals(otro.id);   // sin id, solo igual a sí mismo
}

@Override
public int hashCode() {
    return getClass().hashCode();              // constante: estable siempre
}
```

Parece raro —un `hashCode` constante— pero es lo recomendado por el equipo de Hibernate: garantiza que el objeto no se «pierde» de una colección al recibir su id. El coste es que todas las entidades de la misma clase caen en el mismo cubo del `HashSet`, algo irrelevante con las cantidades que se manejan en memoria.

!!! success "Dónde sí siguen los `records`"
    En los **DTO**. La entidad es mutable y vive atada a la base de datos; el DTO es inmutable y es lo que sale por la API. Mantén la frontera: **nunca devuelvas una entidad desde un controlador**. Si lo haces, además de exponer campos internos, Jackson intentará serializar relaciones perezosas y te encontrarás con errores que no entenderás.

## 5. La entidad completa del proyecto

``` { .java .numerado }
@Entity
@Table(name = "productos")
public class Producto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String categoria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int stock;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected Producto() { }

    public Producto(String nombre, String categoria, BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    @PrePersist
    void alCrear() { creadoEn = Instant.now(); }     // se ejecuta antes del INSERT

    // getters, y setters SOLO de lo que puede cambiar
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setStock(int stock) { this.stock = stock; }

    public void restarStock(int unidades) {          // mejor que un setter suelto
        if (stock < unidades)
            throw new StockInsuficienteException(id, stock, unidades);
        this.stock -= unidades;
    }
}
```

Dos detalles que separan una entidad correcta de una buena: `@PrePersist` para rellenar la fecha de creación sin acordarte, y **métodos de negocio en vez de setters sueltos**. `restarStock` valida; `setStock` deja pasar cualquier cosa.

---

## Pruébalo ahora (30 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1.** Escribe la entidad `Producto` completa y arranca con `show-sql`. Copia el `create table` generado y comprueba que cada anotación tiene su reflejo: `nullable`, `length`, `unique`, el índice.

**Parte 2 — Rompe el enum.** Crea un `Estado { ACTIVO, DESCATALOGADO }` con `@Enumerated` **por defecto** (ordinal), guarda dos productos, y en la consola H2 mira qué hay en la columna: `0` y `1`. Ahora **reordena el enum** en el código, reinicia y vuelve a consultar por la API. Los productos han cambiado de estado sin que nadie los tocara. Ponle `EnumType.STRING` y repite.

**Parte 3 — El dinero.** Declara el precio como `double`, guarda `0.1` y `0.2`, y súmalos en una consulta. Cámbialo a `BigDecimal` con `scale = 2` y compara.

**Parte 4 — `@Transient`.** Añade `precioConIva` sin anotar y arranca: Hibernate crea la columna. Anótalo con `@Transient` y comprueba que desaparece.

**Parte 5 — El bug del `HashSet`.** Implementa `equals`/`hashCode` usando **todos** los campos, mete un producto sin guardar en un `HashSet`, guárdalo y comprueba `contains`. Después cámbialo a la versión con `id` y `getClass().hashCode()`.

---

## Ejercicios (con solución)

### Ejercicio 1 — `Long` o `long`
¿Por qué el id debe ser `Long` y no `long`?

??? success "Solución"

    Porque una entidad recién creada y todavía no persistida <b>no tiene id</b>, y eso se representa con <code>null</code>. Con el primitivo valdría <code>0</code>, que es un valor válido y no distinguible de «sin guardar». Spring Data usa precisamente ese <code>null</code> para decidir si <code>save()</code> hace <code>INSERT</code> o <code>UPDATE</code>.


### Ejercicio 2 — El enum reordenado
Un compañero añade un valor en medio del enum y de repente los pedidos «entregados» aparecen como «cancelados». Explica qué pasó.

??? success "Solución"

    El enum estaba mapeado con el valor por defecto, <code>EnumType.ORDINAL</code>, así que en la base de datos hay <b>números</b>: 0, 1, 2. Al insertar un valor nuevo en medio, todos los ordinales posteriores se desplazan y las filas ya guardadas pasan a significar otra cosa. Los datos no cambiaron; cambió su interpretación.<br>
    Solución: <code>@Enumerated(EnumType.STRING)</code> desde el primer día. Ocupa unos bytes más y es legible al mirar la tabla, que además ayuda al depurar.


### Ejercicio 3 — `IDENTITY` o `SEQUENCE`
Vas a importar 50.000 filas de un CSV en PostgreSQL. ¿Qué estrategia eliges y por qué?

??? success "Solución"

    <b><code>SEQUENCE</code></b> con <code>allocationSize</code> alto (50 o 100). Con <code>IDENTITY</code>, Hibernate necesita el id devuelto por cada <code>INSERT</code> individual, así que <b>no puede agrupar</b> las inserciones por lotes y hace 50.000 viajes a la base de datos. Con <code>SEQUENCE</code> reserva bloques de ids y envía las filas en tandas; la importación pasa de minutos a segundos.<br>
    Además hay que activar el lote: <code>spring.jpa.properties.hibernate.jdbc.batch_size: 50</code>.


### Ejercicio 4 — El `hashCode` constante
Devolver siempre `getClass().hashCode()` parece un error. Justifícalo.

??? success "Solución"

    El contrato de <code>hashCode</code> exige que <b>no cambie</b> mientras el objeto está en una colección basada en hash. El id de una entidad <b>sí cambia</b>: es <code>null</code> antes de guardar y un número después. Si el hash dependiera del id, el objeto se «perdería» dentro del <code>HashSet</code> al persistirlo.<br>
    Un hash constante cumple el contrato siempre. El precio es que todas las instancias de esa clase caen en el mismo cubo, lo que degradaría un <code>HashSet</code> enorme; en la práctica manejas decenas de entidades en memoria, no millones, así que no se nota. Es la recomendación oficial del equipo de Hibernate.


### Ejercicio 5 — Diseña la entidad
Modela `Pedido` con id, número de referencia único, fecha, estado (enum), total en euros y cliente (texto por ahora). Añade índice por fecha y que la fecha de creación se rellene sola.

??? success "Solución"

    ```java
    @Entity
    @Table(name = "pedidos", indexes = @Index(name = "idx_pedido_fecha", columnList = "fecha"))
    public class Pedido {

        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true, length = 20)
        private String referencia;

        @Column(nullable = false)
        private LocalDate fecha;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private EstadoPedido estado;

        @Column(nullable = false, precision = 12, scale = 2)
        private BigDecimal total;

        @Column(nullable = false, length = 120)
        private String cliente;

        @Column(name = "creado_en", nullable = false, updatable = false)
        private Instant creadoEn;

        @PrePersist
        void alCrear() {
            creadoEn = Instant.now();
            if (estado == null) estado = EstadoPedido.PENDIENTE;
        }
    }
    ```
    Los cuatro puntos evaluables: <code>unique</code> en la referencia, <code>EnumType.STRING</code>, <code>BigDecimal</code> para el total con <code>updatable = false</code> en la fecha de creación, y el índice en la columna por la que se filtrará.

