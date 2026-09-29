# Simulacro de test — UT3

**30 preguntas, 55 minutos.** Es el examen de la S10: mismo número, mismo formato y mismo reparto.

| Bloque | De dónde sale | Preguntas |
|---|---|:-:|
| **1** | CSV: leer, las trampas, Commons CSV | 7 |
| **2** | JSON con Jackson | 7 |
| **3** | Fechas, validación y `BigDecimal` | 8 |
| **4** | Base de datos con JDBC | 8 |

!!! success "Todas salen de un ejercicio que ya has hecho"
    Cada pregunta lleva debajo **el ejercicio de la batería del que sale**. No hay ninguna sorpresa: si has hecho la [batería](ejercicios.md), has visto antes todos estos fragmentos en tu pantalla.

    Las opciones incorrectas tampoco son inventadas: son **los errores que se cometen escribiendo ese código**.

!!! tip "Cómo se hace"
    Tápate la solución, contesta, y **solo entonces** despliega. Si aciertas por eliminación, lee igual la explicación: dice por qué las otras tres están mal.

    Y cuando dudes, **ejecútalo**. `jshell` arranca en dos segundos.

!!! info "Las colecciones y los *streams* se evalúan en la UT2"
    Aquí aparecen **dentro** de un lector de CSV o de un informe, porque son la herramienta. Las preguntas sobre `groupingBy` o comparadores están en el [simulacro de la UT2](../ut2/autoevaluacion.md).

Acierto **+1**, fallo **−0,25**, en blanco **0**. Se aprueba con 15.

---

## Bloque 1 — CSV

### 1 · ¿Por qué falla?

``` { .java .numerado }
var lineas = Files.readAllLines(Path.of("productos.csv"));
double total = 0;
for (var l : lineas) {
    total += Double.parseDouble(l.split(";")[3]);
}
```

**a)** No compila · **b)** `NumberFormatException` en la primera vuelta · **c)** Funciona · **d)** `IOException`

??? success "Solución"

    **b)** La primera línea es la **cabecera**, y `"precio"` no es un número.

    ```java
    for (var l : lineas.subList(1, lineas.size())) { … }
    // o con streams:  lineas.stream().skip(1)
    ```

    → [E1](ejercicios.md)

### 2 · ¿Qué imprime?

``` { .java .numerado }
System.out.println(Double.parseDouble("35,00"));
```

**a)** `35.0` · **b)** `3500.0` · **c)** `NumberFormatException` · **d)** `0.35`

??? success "Solución"

    **c)** `NumberFormatException: For input string: "35,00"`.

    Java espera siempre **punto** decimal, da igual el idioma del sistema. Con un CSV español hay que hacer `replace(',', '.')` antes.

    → [E3](ejercicios.md)

### 3 · El nombre lleva el separador dentro

``` { .java .numerado }
var linea = "B1;\"Casco rojo; talla M\";35,00";
System.out.println(linea.split(";").length);
```

**a)** `3` · **b)** `4` · **c)** `2` · **d)** Error

??? success "Solución"

    **b) `4`.** `split` no sabe nada de comillas: parte también el `;` que está dentro del nombre.

    Es la trampa que **no se arregla con un `if`**, y la razón por la que existe Apache Commons CSV.

    → [E4](ejercicios.md)

### 4 · El campo vacío del final

``` { .java .numerado }
System.out.println("a;b;;".split(";").length);
System.out.println("a;b;;".split(";", -1).length);
```

**a)** `4` y `4` · **b)** `2` y `4` · **c)** `4` y `2` · **d)** `2` y `2`

??? success "Solución"

    **b) `2` y `4`.** Sin el segundo parámetro, `split` **descarta los campos vacíos del final**.

    Por eso una fila a la que le falte el último campo devuelve menos trozos de los que crees, y el `ArrayIndexOutOfBoundsException` sale tres columnas más allá de donde miras.

    → [E4](ejercicios.md)

### 5 · La configuración de Commons CSV

¿Qué hace `.setSkipHeaderRecord(true)`?

**a)** Ignora las líneas vacías · **b)** No devuelve la cabecera como fila de datos · **c)** Quita los espacios · **d)** Usa la primera línea como nombres de columna

??? success "Solución"

    **b)** No la devuelve como fila. La que **usa la cabecera como nombres** es `.setHeader()`, la (d).

    Van siempre juntas, y esa es justo la confusión:

    ```java
    .setHeader()               // "la primera línea son los nombres"
    .setSkipHeaderRecord(true) // "y no me la des como dato"
    ```

    Con solo la primera, `fila.get("nombre")` funciona pero la cabecera aparece como una fila más.

    → [E5](ejercicios.md)

### 6 · ¿Qué hay que añadir?

``` { .java .numerado }
var parser = CSVParser.parse(Path.of("productos.csv"), ???, formato);
```

**a)** `"UTF-8"` · **b)** `StandardCharsets.UTF_8` · **c)** `Locale.ROOT` · **d)** Nada, se deduce

??? success "Solución"

    **b)** `StandardCharsets.UTF_8`, un `Charset`, no una cadena.

    Y hay que ponerlo: **sin él se usa el del sistema operativo**, así que el mismo programa lee bien las tildes en tu portátil y las rompe en el servidor.

    → [E5](ejercicios.md)

### 7 · Un lector que no se cae

Tu lector encuentra una fila con el precio `"abc"`. ¿Qué debe hacer?

**a)** Lanzar la excepción y parar · **b)** Poner el precio a 0 y seguir · **c)** Saltar la fila, contarla por motivo y seguir · **d)** Ignorarla en silencio

??? success "Solución"

    **c)** Saltarla, **contarla** y seguir.

    La (a) es inútil: con 300 filas nunca terminas. La (b) mete datos falsos en la base de datos, que es peor. La (d) hace que nadie se entere de que faltan 11 productos.

    ```
    Cargadas 283 de 300 filas
    Descartes: {precio no numérico=11, falta el código=2, fecha ilegible=4}
    ```

    → [E6](ejercicios.md)

---

## Bloque 2 — JSON con Jackson

### 8 · ¿Qué imprime?

``` { .java .numerado }
record Producto(String codigo, String nombre, double precio) {}
var mapper = new ObjectMapper();
System.out.println(mapper.writeValueAsString(new Producto("A1", "Casco", 35.0)));
```

**a)** `Producto[codigo=A1, …]` · **b)** `{"codigo":"A1","nombre":"Casco","precio":35.0}` · **c)** Falla: falta anotar el record · **d)** `{}`

??? success "Solución"

    **b)** Jackson lee los captadores del `record` sin ninguna anotación.

    La (d) pasaría con una clase sin captadores: Jackson no ve los campos privados y escribe un objeto vacío. Con `record` no ocurre.

    → [E7](ejercicios.md)

### 9 · La lista que no se deja leer

``` { .java .numerado }
List<Producto> lista = mapper.readValue(json, List.class);
System.out.println(lista.get(0).nombre());
```

**a)** Funciona · **b)** `ClassCastException` en el `get(0)` · **c)** No compila · **d)** Devuelve una lista vacía

??? success "Solución"

    **b)** `List.class` no dice qué hay dentro, así que Jackson rellena la lista de `LinkedHashMap`. El `ClassCastException` salta al usarlo.

    ```java
    var lista = mapper.readValue(json, new TypeReference<List<Producto>>() {});
    ```

    → [E8](ejercicios.md)

### 10 · ¿Qué escribe?

``` { .java .numerado }
record Alta(String codigo, LocalDate fecha) {}
var mapper = new ObjectMapper();
System.out.println(mapper.writeValueAsString(
        new Alta("A5", LocalDate.of(2026, 3, 14))));
```

**a)** `{"codigo":"A5","fecha":"2026-03-14"}` · **b)** `{"codigo":"A5","fecha":[2026,3,14]}` · **c)** Lanza una excepción · **d)** `{"codigo":"A5"}`

??? success "Solución"

    **b)** Un array de números, que ninguna API entiende. Sin configurar, Jackson descompone el `LocalDate` en sus partes.

    → [E9](ejercicios.md)

### 11 · Arreglarlo

¿Qué hace falta para que la fecha salga como `"2026-03-14"`?

**a)** Solo `registerModule(new JavaTimeModule())` · **b)** Solo `disable(WRITE_DATES_AS_TIMESTAMPS)` · **c)** Las dos cosas · **d)** Anotar el campo con `@JsonFormat`

??? success "Solución"

    **c) Las dos.**

    ```java
    var mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    ```

    El módulo **enseña a Jackson qué es un `LocalDate`**; el `disable` le dice que lo escriba como texto ISO y no como número. Con solo el módulo, sigue saliendo el array.

    La (d) funciona campo a campo, pero hay que repetirla en todos.

    → [E9](ejercicios.md)

### 12 · La API ha añadido un campo

``` { .java .numerado }
var json = "{\"codigo\":\"A1\",\"nombre\":\"Casco\",\"promocion\":true}";
var p = new ObjectMapper().readValue(json, Producto.class);
```

**a)** Funciona, ignora el campo · **b)** `UnrecognizedPropertyException` · **c)** No compila · **d)** Pone `promocion` a `null`

??? success "Solución"

    **b)** Por defecto Jackson es estricto y falla con un campo que no conoce.

    ```java
    var mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    ```

    **Obligatorio cuando consumes una API ajena.** El día que añadan un campo, tu programa sigue funcionando en vez de caerse.

    → [E10](ejercicios.md)

### 13 · `get` frente a `path`

``` { .java .numerado }
var raiz = mapper.readTree("{\"nombre\":\"Casco\"}");
System.out.println(raiz.get("precio").asDouble(0.0));
System.out.println(raiz.path("precio").asDouble(0.0));
```

**a)** `0.0` y `0.0` · **b)** `NullPointerException` y luego `0.0` · **c)** Las dos fallan · **d)** `null` y `0.0`

??? success "Solución"

    **b)** `get` de un campo que no existe devuelve **`null`**, así que el `.asDouble` explota. `path` devuelve un **nodo vacío**, que se puede encadenar.

    | | Campo que no existe |
    |---|---|
    | `get("x")` | `null` → `NullPointerException` |
    | `path("x")` | Nodo vacío → se encadena sin `if` |

    Con `path` puedes bajar cinco niveles seguidos sin comprobar nada.

    → [E11](ejercicios.md)

### 14 · El JSON que cambia de orden

Tu programa agrupa por categoría con `groupingBy` y escribe el JSON. Cada ejecución sale con las claves en otro orden. ¿Por qué?

**a)** Jackson desordena · **b)** `groupingBy` devuelve un `HashMap` · **c)** Es normal en JSON · **d)** Falta `setSerializationInclusion`

??? success "Solución"

    **b)** `groupingBy` devuelve un `HashMap` y **el orden de sus claves no está garantizado**.

    ```java
    Collectors.groupingBy(Producto::categoria, TreeMap::new, Collectors.toList())
    ```

    Un fichero cuyas claves bailan es imposible de comparar entre dos versiones, y llena de ruido cualquier `diff`.

    → [E12](ejercicios.md)

---

## Bloque 3 — Fechas y validación

### 15 · ¿Qué imprime?

``` { .java .numerado }
var f = LocalDate.of(2026, 3, 14);
f.plusDays(7);
System.out.println(f);
```

**a)** `2026-03-21` · **b)** `2026-03-14` · **c)** No compila · **d)** `null`

??? success "Solución"

    **b) `2026-03-14`.** `java.time` es **inmutable**: `plusDays` no modifica nada, **devuelve una fecha nueva** que aquí se tira.

    ```java
    var siguiente = f.plusDays(7);
    ```

    Es el mismo error que `texto.trim();` sin asignar.

    → [E13](ejercicios.md)

### 16 · Qué clase usar

Guardas el instante en que se crea un pedido, y la aplicación se usa en España y en México. ¿Qué tipo?

**a)** `LocalDateTime` · **b)** `Instant` · **c)** `LocalDate` · **d)** `String` en ISO

??? success "Solución"

    **b) `Instant`.** Un pedido creado a las 10:00 en Madrid y leído en México es **el mismo momento**.

    Con `LocalDateTime` no sabes de qué zona era esa hora, y el descuadre aparece en cuanto cambia el horario de verano.

    → [E14](ejercicios.md)

### 17 · Días entre dos fechas

``` { .java .numerado }
var a = LocalDate.of(2026, 1, 15);
var b = LocalDate.of(2026, 3, 14);
System.out.println(???);
```

**a)** `b - a` · **b)** `ChronoUnit.DAYS.between(a, b)` · **c)** `b.getDayOfYear() - a.getDayOfYear()` · **d)** `Period.between(a, b)`

??? success "Solución"

    **b)** Devuelve `58`.

    La (c) parece funcionar y falla en cuanto las fechas cruzan el año. La (d) devuelve *«1 mes y 27 días»*, que es otra cosa.

    **`ChronoUnit` para una unidad; `Period` cuando quieres años, meses y días juntos.**

    → [E15](ejercicios.md)

### 18 · El patrón que no cuadra

``` { .java .numerado }
var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
LocalDate.parse("14-03-2026", fmt);
```

**a)** Funciona · **b)** `DateTimeParseException` · **c)** Devuelve `null` · **d)** No compila

??? success "Solución"

    **b)** El patrón describe **exactamente** el texto, guiones incluidos.

    Y la letra que más se falla: **`MM` es mes, `mm` son minutos.** Con `"dd/mm/yyyy"` obtienes siempre el mes 0.

    → [E16](ejercicios.md)

### 19 · Dónde se valida

``` { .java .numerado }
record Reserva(String cliente, LocalDate entrada, LocalDate salida) {}
```

¿Dónde se comprueba que la salida sea posterior a la entrada?

**a)** En el método que crea la reserva · **b)** En el constructor compacto del record · **c)** En un método `esValida()` · **d)** Al guardar en la base de datos

??? success "Solución"

    **b)** En el constructor compacto.

    ```java
    record Reserva(String cliente, LocalDate entrada, LocalDate salida) {
        Reserva {
            if (!salida.isAfter(entrada))
                throw new IllegalArgumentException("Salida anterior a la entrada");
        }
    }
    ```

    Así **no existe ninguna forma de crear una reserva inválida**. Con las otras tres siempre hay un camino que se salta la comprobación: alguien crea el objeto por otro sitio y ya está dentro.

    → [E17](ejercicios.md)

### 20 · ¿Se solapan?

`[1 marzo, 5 marzo)` y `[5 marzo, 8 marzo)`.

**a)** Sí, comparten el día 5 · **b)** No · **c)** Depende de la hora · **d)** No se puede saber

??? success "Solución"

    **b) No.** Uno sale el día 5 por la mañana y otro entra el 5 por la tarde: la habitación no está ocupada dos veces.

    ```java
    a.entrada().isBefore(b.salida()) && b.entrada().isBefore(a.salida())
    ```

    → [E18](ejercicios.md)

### 21 · El `<=` que arruina el hotel

Si en la condición anterior usas `!isAfter` en vez de `isBefore`, ¿qué pasa?

**a)** Nada, es igual · **b)** Dos reservas consecutivas se declaran en conflicto · **c)** Nunca detecta solapes · **d)** No compila

??? success "Solución"

    **b)** `!isAfter` incluye la igualdad, así que el día de salida de una y el de entrada de otra cuentan como conflicto.

    Resultado: **rechazas la mitad de las reservas** de un hotel que funciona perfectamente. Es un bug real y muy caro, y todo está en un solo operador.

    → [E18](ejercicios.md)

### 22 · Dinero

``` { .java .numerado }
System.out.println(0.1 + 0.2);
System.out.println(new BigDecimal("0.1").add(new BigDecimal("0.2")));
```

**a)** `0.3` y `0.3` · **b)** `0.30000000000000004` y `0.3` · **c)** Las dos con error · **d)** `0.3` y `0.30`

??? success "Solución"

    **b)** `double` es binario y esos decimales no tienen representación exacta.

    Y el detalle que cae: **`BigDecimal` se construye con `String`**. `new BigDecimal(0.1)` con un `double` arrastra el mismo error desde el principio.

    → [E3](ejercicios.md)

---

## Bloque 4 — Base de datos con JDBC

### 23 · La tabla que desaparece

``` { .java .numerado }
var URL = "jdbc:h2:mem:tienda";
// crear tabla en un try...
// insertar en OTRO try → "Table PRODUCTO not found"
```

**a)** Falta el driver · **b)** Falta `DB_CLOSE_DELAY=-1` · **c)** H2 no admite `CREATE TABLE` · **d)** Falta hacer `commit`

??? success "Solución"

    **b)** Con `mem:`, la base de datos se destruye **cuando se cierra la última conexión**. Al salir del primer `try`, la tabla se evapora.

    ```java
    var URL = "jdbc:h2:mem:tienda;DB_CLOSE_DELAY=-1";
    ```

    Con `jdbc:h2:./datos/tienda` (fichero) no pasa: persiste en disco.

    → [E19](ejercicios.md)

### 24 · Qué método

Vas a ejecutar un `DELETE`. ¿Qué método y qué devuelve?

**a)** `executeQuery()`, un `ResultSet` · **b)** `executeUpdate()`, las filas afectadas · **c)** `execute()`, un `boolean` · **d)** `executeDelete()`

??? success "Solución"

    **b)** `executeUpdate()` devuelve un `int` con las **filas afectadas**.

    Y ese número es la respuesta a *«¿se ha borrado?»*: un `DELETE` de un id que no existe **no lanza nada**, devuelve `0`. Quien no lo mira le dice al usuario que se borró algo que no se borró.

    → [E20](ejercicios.md)

### 25 · Los `?` se numeran desde…

``` { .java .numerado }
var ps = con.prepareStatement("INSERT INTO producto (codigo, nombre) VALUES (?, ?)");
ps.setString(?, "A1");
```

**a)** `0` · **b)** `1` · **c)** Da igual · **d)** Por nombre

??? success "Solución"

    **b) Desde 1.** Con `0` sale `Invalid value for parameter index`.

    Es la única cosa de JDBC que se numera desde 1, y por eso se falla siempre. También `rs.getString(1)` para la primera columna.

    → [E21](ejercicios.md)

### 26 · La inyección

``` { .java .numerado }
var sql = "SELECT * FROM producto WHERE codigo = '" + entrada + "'";
```

Con `entrada` = `x' OR '1'='1`, ¿qué devuelve?

**a)** Nada · **b)** Un error de sintaxis · **c)** La tabla entera · **d)** Solo el producto `x`

??? success "Solución"

    **c) La tabla entera.** La consulta que llega es:

    ```sql
    SELECT * FROM producto WHERE codigo = 'x' OR '1'='1'
    ```

    y `'1'='1'` es siempre cierto. Con `; DROP TABLE producto; --` la borra.

    → [E22](ejercicios.md)

### 27 · Por qué el `?` sí funciona

**a)** Escapa las comillas del texto · **b)** El valor viaja aparte y nunca se interpreta como SQL · **c)** Comprueba el tipo · **d)** Es igual de inseguro, solo más cómodo

??? success "Solución"

    **b)** La consulta se envía **una vez, sin los valores**, y la base de datos la compila. Los valores llegan después, por otro canal, y ya no pueden cambiar su estructura.

    La (a) es la respuesta que parece correcta y es la peligrosa: si fuera solo escapado, habría combinaciones que se colarían. Por eso `PreparedStatement` no es «la forma elegante», es **la única**.

    → [E22](ejercicios.md)

### 28 · Comprobar antes de insertar

Antes de insertar un código, ¿haces un `SELECT` para ver si existe?

**a)** Sí, siempre · **b)** No: pones `UNIQUE` y capturas la excepción · **c)** Da igual · **d)** Sí, pero dentro del mismo `try`

??? success "Solución"

    **b)** Entre tu `SELECT` y tu `INSERT` cabe otro programa haciendo exactamente lo mismo, y los dos creen que el código está libre.

    ```java
    try {
        ps.executeUpdate();
    } catch (SQLIntegrityConstraintViolationException e) {
        System.out.println("Ya existe un producto con ese código");
    }
    ```

    La restricción de la tabla es **la única comprobación que no se puede saltar**.

    → [E23](ejercicios.md)

### 29 · Qué devuelve `leer(int id)`

En el CRUD, ¿qué debe devolver el método que busca por id?

**a)** `Producto`, y `null` si no está · **b)** `Optional<Producto>` · **c)** `List<Producto>` con 0 o 1 elemento · **d)** Lanzar excepción si no está

??? success "Solución"

    **b) `Optional<Producto>`.** Que un id no exista **es un resultado normal**, no un error.

    La (a) devuelve el `null` del que quieres escapar. La (d) usa una excepción para un caso corriente, y quien llame tendrá que envolver todo en `try`.

    → [E24](ejercicios.md)

### 30 · De H2 a MySQL

Pasas el mismo CRUD a MySQL en Docker. ¿Qué tocas?

**a)** Todos los `PreparedStatement` · **b)** La URL, el usuario, la contraseña y la dependencia del driver · **c)** Solo la URL · **d)** Hay que reescribir el `ResultSet`

??? success "Solución"

    **b)** Tres constantes y una dependencia en el `pom.xml`. **Ni una línea de lógica.**

    ```java
    static final String URL  = "jdbc:mysql://localhost:3306/tienda";
    static final String USER = "root";
    static final String PASS = "root";
    ```

    Eso es lo que significa que JDBC sea un estándar: `Connection`, `PreparedStatement` y `ResultSet` son **interfaces**, y cada base de datos trae su implementación.

    Lo que sí puede cambiar es el **dialecto SQL** de las consultas concretas. Por eso en la UT5 aparece JPA.

    → [E25](ejercicios.md)

---

## Cómo se corrige

| Aciertos (de 30) | Nota aproximada | Qué significa |
|:-:|:-:|---|
| 27 – 30 | 9 – 10 | Dominas la unidad |
| 21 – 26 | 7 – 8 | Sólido; repasa los fallos concretos |
| 15 – 20 | 5 – 6 | Aprobado justo: rehaz la batería del bloque que peor te fue |
| < 15 | — | Vuelve a los temas con `jshell` abierto, no releyendo |

Con penalización: **nota = (aciertos − fallos/4) / 3**.

!!! tip "Lo que hay que hacer después"
    Apunta **el número de bloque** de cada fallo, no la pregunta. Si tres de tus fallos son del bloque 4, lo que hay que repasar no son tres preguntas: es JDBC entero, y se repasa **haciendo el E24 otra vez desde cero**.

    Releer la solución de una pregunta que fallaste da la sensación de haberlo arreglado. No lo arregla.
