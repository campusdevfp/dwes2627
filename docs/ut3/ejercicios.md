# Batería de ejercicios — UT3

**28 ejercicios con solución**, en el **mismo orden que los temas** y de menos a más dentro de cada bloque. Cada uno se apoya en el anterior: si los haces seguidos, el último sale solo.

| | |
|:-:|---|
| ● | Cinco minutos, con el tema delante |
| ●● | Hay que juntar dos ideas del tema |
| ●●● | Programa completo |

!!! tip "Los datos de partida"
    Todo el bloque de CSV y JSON trabaja sobre el mismo fichero. Créalo una vez y no lo toques:

    ```csv title="datos/productos.csv"
    codigo;nombre;categoria;precio;alta
    A1;Casco;seguridad;35,00;2026-01-15
    A2;Bici de paseo;movilidad;450,00;2026-02-03
    A3;Candado;seguridad;25,50;2026-02-20
    A4;Luces LED;seguridad;15,00;2026-03-01
    A5;Patinete;movilidad;120,00;2026-03-14
    ```

    Separador `;`, coma decimal, fechas ISO. **Es un CSV exportado de Excel en español**, que es lo que te vas a encontrar.

---

# Bloque 1 · CSV

> Tema [1. CSV y JSON](01-csv-y-json.md) §1–3

## E1 ● — Leer y contar

Lee `productos.csv` con `Files.lines` y cuenta cuántas líneas de datos hay (sin la cabecera).

??? success "Solución"

    ```java
    import java.nio.file.*;

    try (var lineas = Files.lines(Path.of("datos/productos.csv"))) {
        long n = lineas.skip(1).count();
        System.out.println("Filas de datos: " + n);      // Filas de datos: 5
    }
    ```

    Dos cosas que ya no se negocian:

    - **`try-with-resources`**: `Files.lines` abre el fichero y hay que cerrarlo. Fuera del `try`, el descriptor se queda abierto.
    - **`skip(1)`** para la cabecera. El fallo clásico es contar 6.

## E2 ● — Partir por el separador

Imprime el nombre de cada producto partiendo cada línea por `;`.

??? success "Solución"

    ```java
    try (var lineas = Files.lines(Path.of("datos/productos.csv"))) {
        lineas.skip(1)
              .map(l -> l.split(";"))
              .forEach(c -> System.out.println(c[1]));
    }
    // Casco
    // Bici de paseo
    // Candado
    // Luces LED
    // Patinete
    ```

    Funciona **con este fichero**. En el E4 verás por qué no se puede dejar así.

## E3 ● — La coma decimal

`Double.parseDouble("35,00")` falla. Arréglalo.

??? success "Solución"

    ```java
    Double.parseDouble("35,00");
    // lanza java.lang.NumberFormatException: For input string: "35,00"

    System.out.println(Double.parseDouble("35,00".replace(',', '.')));   // 35.0
    ```

    Java siempre espera **punto** decimal, da igual el idioma del sistema. Y si el fichero además trae separador de miles (`1.250,00`), hay que quitarlo antes:

    ```java
    System.out.println(Double.parseDouble("1.250,00".replace(".", "").replace(',', '.')));
    // 1250.0
    ```

    Para dinero de verdad, `BigDecimal`. Lo ves en el bloque 3.

## E4 ●● — Las tres trampas del `split`

Este CSV rompe la solución del E2. Di qué falla en cada línea.

```csv
codigo;nombre;precio
B1;"Casco rojo; talla M";35,00
B2;Bici;
B3;Luces;15,00;sobra
```

??? success "Solución"

    | Línea | El problema |
    |---|---|
    | `B1` | El nombre **lleva el separador dentro**, entre comillas. `split(";")` devuelve 4 trozos y el nombre sale partido |
    | `B2` | El precio está **vacío**. `c[2]` existe pero es `""`, y `parseDouble("")` lanza `NumberFormatException` |
    | `B3` | Hay **una columna de más**. `c[2]` es `"15,00"`, pero si hubiera faltado una columna sería `ArrayIndexOutOfBoundsException` |

    Y hay una cuarta, invisible: `split(";")` **descarta los campos vacíos del final**, así que una línea que acabe en `;;` devuelve menos trozos de los que crees.

    Ninguna se arregla con un `if`. Por eso existe una librería.

## E5 ●● — Apache Commons CSV

Reescribe el E2 con Commons CSV: separador `;`, cabecera, sin líneas vacías y con `trim`.

??? success "Solución"

    ```java
    import org.apache.commons.csv.*;

    var formato = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    try (var parser = CSVParser.parse(Path.of("datos/productos.csv"),
                                      java.nio.charset.StandardCharsets.UTF_8, formato)) {
        for (var fila : parser) {
            System.out.println(fila.get("nombre") + " → " + fila.get("precio"));
        }
    }
    // Casco → 35,00
    // Bici de paseo → 450,00
    // ...
    ```

    Lo que has ganado: **las comillas se respetan**, los campos se piden **por nombre** (`fila.get("nombre")`, no `c[1]`) y las tres trampas del E4 desaparecen.

    Y el `StandardCharsets.UTF_8` explícito: sin él, las tildes dependen del sistema operativo de quien ejecute.

## E6 ●● — Contar los descartes

Lee el CSV convirtiendo cada fila a un `record Producto`, **saltando** las filas malas y contando **por qué** se descartó cada una.

??? success "Solución"

    ```java
    record Producto(String codigo, String nombre, String categoria, double precio) {}

    var buenos = new ArrayList<Producto>();
    var descartes = new TreeMap<String, Integer>();

    try (var parser = CSVParser.parse(Path.of("datos/productos.csv"),
                                      java.nio.charset.StandardCharsets.UTF_8, formato)) {
        for (var fila : parser) {
            try {
                var precio = Double.parseDouble(fila.get("precio").replace(',', '.'));
                if (precio < 0) { descartes.merge("precio negativo", 1, Integer::sum); continue; }
                buenos.add(new Producto(fila.get("codigo"), fila.get("nombre"),
                                        fila.get("categoria"), precio));
            } catch (NumberFormatException e) {
                descartes.merge("precio no numérico", 1, Integer::sum);
            } catch (IllegalArgumentException e) {
                descartes.merge("falta una columna", 1, Integer::sum);
            }
        }
    }
    System.out.println("Cargados: " + buenos.size());   // Cargados: 5
    System.out.println("Descartes: " + descartes);      // Descartes: {}
    ```

    **Esto es lo que se pide en un trabajo de verdad.** Un lector que revienta con la primera fila mala es inútil: hay que terminar y **decir cuántas se cayeron y por qué**.

    Prueba a meter a mano una fila con `precio` = `abc` y verás el contador subir.

---

# Bloque 2 · JSON con Jackson

> Tema [1. CSV y JSON](01-csv-y-json.md) §4–8

## E7 ● — De objeto a JSON

Convierte un `Producto` a JSON.

??? success "Solución"

    ```java
    import com.fasterxml.jackson.databind.*;

    var mapper = new ObjectMapper();
    var p = new Producto("A1", "Casco", "seguridad", 35.0);

    System.out.println(mapper.writeValueAsString(p));
    // {"codigo":"A1","nombre":"Casco","categoria":"seguridad","precio":35.0}

    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(p));
    // {
    //   "codigo" : "A1",
    //   ...
    // }
    ```

    Jackson lee los captadores del `record` sin que le digas nada. Cero anotaciones.

## E8 ● — De JSON a objeto

El camino de vuelta.

??? success "Solución"

    ```java
    var json = """
        {"codigo":"A9","nombre":"Timbre","categoria":"seguridad","precio":8.5}""";

    var p = mapper.readValue(json, Producto.class);
    System.out.println(p.nombre());    // Timbre
    ```

    Y para una lista hace falta `TypeReference`, porque en tiempo de ejecución Java no sabe qué hay dentro de un `List`:

    ```java
    import com.fasterxml.jackson.core.type.TypeReference;

    var lista = mapper.readValue("""
        [{"codigo":"A1","nombre":"Casco","categoria":"seguridad","precio":35.0}]""",
        new TypeReference<List<Producto>>() {});
    System.out.println(lista.size());   // 1
    ```

## E9 ●● — La fecha que sale como `[2026,3,14]`

Añade un `LocalDate` al record y arregla la salida.

??? success "Solución"

    ```java
    import java.time.LocalDate;
    record ProductoFecha(String codigo, String nombre, LocalDate alta) {}

    var sinConfigurar = new ObjectMapper();
    System.out.println(sinConfigurar.writeValueAsString(
            new ProductoFecha("A5", "Patinete", LocalDate.of(2026, 3, 14))));
    // {"codigo":"A5","nombre":"Patinete","alta":[2026,3,14]}
    ```

    Un array de números. Ninguna API lo entiende. Se arregla con **el módulo de `java.time`**:

    ```java
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import com.fasterxml.jackson.databind.SerializationFeature;

    var mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    System.out.println(mapper.writeValueAsString(
            new ProductoFecha("A5", "Patinete", LocalDate.of(2026, 3, 14))));
    // {"codigo":"A5","nombre":"Patinete","alta":"2026-03-14"}
    ```

    Hacen falta **las dos líneas**: el módulo enseña a Jackson qué es un `LocalDate`, y el `disable` le dice que lo escriba como texto ISO y no como número.

    Y la dependencia `jackson-datatype-jsr310` en el `pom.xml`.

## E10 ●● — JSON ajeno con campos que sobran

Este JSON viene de una API que ha añadido campos. Haz que no reviente.

```json
{"codigo":"A1","nombre":"Casco","precio":35.0,"promocion":true,"stock":12}
```

??? success "Solución"

    ```java
    mapper.readValue(json, Producto.class);
    // lanza UnrecognizedPropertyException: Unrecognized field "promocion"
    ```

    ```java
    import com.fasterxml.jackson.databind.DeserializationFeature;

    var tolerante = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    System.out.println(tolerante.readValue(json, Producto.class).nombre());   // Casco
    ```

    **Esta línea es obligatoria cuando consumes una API que no es tuya.** El día que añadan un campo, tu programa seguirá funcionando en vez de caerse a las tres de la mañana.

    Y para no escribir `null` al serializar:

    ```java
    mapper.setSerializationInclusion(
            com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    ```

## E11 ●● — Recorrer un JSON sin clase

Saca el nombre y el precio de este JSON **sin crear ningún record**, y que no falle si falta un campo.

```json
{"resultado":{"items":[{"nombre":"Casco","precio":35.0},{"nombre":"Bici"}]}}
```

??? success "Solución"

    ```java
    var raiz = mapper.readTree(json);

    for (var item : raiz.path("resultado").path("items")) {
        System.out.println(item.path("nombre").asText()
                + " → " + item.path("precio").asDouble(0.0));
    }
    // Casco → 35.0
    // Bici → 0.0
    ```

    **`path` frente a `get`, que es la pregunta del examen:**

    | | Si el campo no existe |
    |---|---|
    | `get("x")` | Devuelve `null` → `NullPointerException` en el `.asText()` |
    | `path("x")` | Devuelve un nodo vacío → se puede encadenar sin miedo |

    Con `path` puedes bajar cinco niveles sin un solo `if`.

## E12 ●●● — De CSV a JSON agrupado

Lee `productos.csv` y escribe un JSON con **los productos agrupados por categoría**, fechas ISO y sin nulos.

??? success "Solución"

    ```java
    // CsvAJson.java
    import java.nio.file.*;
    import java.nio.charset.StandardCharsets;
    import java.util.*;
    import java.util.stream.*;
    import org.apache.commons.csv.*;
    import com.fasterxml.jackson.databind.*;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

    record Producto(String codigo, String nombre, String categoria, double precio) {}

    void main() throws Exception {
        var formato = CSVFormat.DEFAULT.builder()
                .setDelimiter(';').setHeader().setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true).setTrim(true).build();

        var productos = new ArrayList<Producto>();
        try (var parser = CSVParser.parse(Path.of("datos/productos.csv"),
                                          StandardCharsets.UTF_8, formato)) {
            for (var f : parser) {
                productos.add(new Producto(f.get("codigo"), f.get("nombre"),
                        f.get("categoria"),
                        Double.parseDouble(f.get("precio").replace(',', '.'))));
            }
        }

        var porCategoria = productos.stream().collect(
                Collectors.groupingBy(Producto::categoria, TreeMap::new,
                                      Collectors.toList()));

        var mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(
                com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(Path.of("salida/productos.json").toFile(), porCategoria);

        IO.println("Escritos " + productos.size() + " productos en "
                + porCategoria.size() + " categorías");
    }
    ```

    ```json
    {
      "movilidad" : [ { "codigo" : "A2", "nombre" : "Bici de paseo", ... } ],
      "seguridad" : [ { "codigo" : "A1", ... } ]
    }
    ```

    Fíjate en el `TreeMap::new`: **`groupingBy` no ordena**, y un JSON cuyas claves cambian de orden en cada ejecución es imposible de comparar entre dos versiones.

---

# Bloque 3 · Fechas y validación

> Tema [2. Fechas y validación](02-fechas-y-validacion.md)

## E13 ● — La fecha que no cambia

```java
import java.time.*;
var f = LocalDate.of(2026, 3, 14);
f.plusDays(7);
System.out.println(f);
```

??? success "Solución"

    **`2026-03-14`.** No ha cambiado.

    `java.time` es **inmutable**: `plusDays` no modifica la fecha, **devuelve una nueva**. Hay que recogerla:

    ```java
    var siguiente = f.plusDays(7);
    System.out.println(siguiente);    // 2026-03-21
    ```

    Es el mismo error que con `String`: `texto.trim();` sin asignar tampoco hace nada.

## E14 ● — Las cuatro clases

¿Cuál usarías para: (a) una fecha de nacimiento · (b) el instante en que se creó un pedido · (c) la hora de apertura de una tienda · (d) la fecha y hora de una cita?

??? success "Solución"

    | | Clase | Por qué |
    |---|---|---|
    | (a) Nacimiento | `LocalDate` | No tiene hora |
    | (b) Pedido | `Instant` | Momento absoluto, sin zona; es lo que se guarda en BD |
    | (c) Apertura | `LocalTime` | No tiene fecha |
    | (d) Cita | `LocalDateTime` | Fecha y hora, en la zona de quien la lee |

    La **(b)** es la que se falla. Un pedido creado en Madrid y leído en México es **el mismo instante**: si lo guardas como `LocalDateTime`, se descuadra en cuanto haya dos zonas o cambie el horario de verano.

## E15 ●● — Cálculos con fechas

Días entre dos fechas, edad en años, y el último día del mes.

??? success "Solución"

    ```java
    import java.time.temporal.ChronoUnit;

    var a = LocalDate.of(2026, 1, 15);
    var b = LocalDate.of(2026, 3, 14);

    System.out.println(ChronoUnit.DAYS.between(a, b));                   // 58
    System.out.println(Period.between(LocalDate.of(1998, 5, 20),
                                      LocalDate.of(2026, 3, 14)).getYears());   // 27
    System.out.println(b.withDayOfMonth(b.lengthOfMonth()));             // 2026-03-31
    System.out.println(b.getDayOfWeek());                                // SATURDAY
    ```

    **`ChronoUnit` para una unidad** (días, meses, horas). **`Period` cuando quieres "2 años, 3 meses y 5 días"**. Restar dos fechas con `getDayOfYear()` es el camino a los bugs de fin de año.

## E16 ●● — Formatear y analizar

Convierte `"14/03/2026"` a `LocalDate` y al revés, y explica por qué falla `"14-03-2026"`.

??? success "Solución"

    ```java
    import java.time.format.*;

    var fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    var f = LocalDate.parse("14/03/2026", fmt);
    System.out.println(f);                  // 2026-03-14
    System.out.println(f.format(fmt));      // 14/03/2026

    LocalDate.parse("14-03-2026", fmt);
    // lanza java.time.format.DateTimeParseException: Text '14-03-2026' could not be parsed
    ```

    El patrón describe **exactamente** el texto, guiones incluidos. Y ojo con las letras:

    | | |
    |---|---|
    | `MM` | Mes · `mm` es **minutos** |
    | `dd` | Día del mes · `DD` es día del año |
    | `yyyy` | Año · `YYYY` es el año de la semana ISO, y en diciembre no coinciden |

    `LocalDate.parse("2026-03-14")` sin formateador funciona: **ISO es el formato por defecto**. Por eso los datos se guardan en ISO y solo se formatea al presentarlos.

## E17 ●● — Validar en el constructor

Haz que una `Reserva` no pueda existir con la fecha de salida antes de la de entrada.

??? success "Solución"

    ```java
    record Reserva(String cliente, LocalDate entrada, LocalDate salida) {
        Reserva {
            if (cliente == null || cliente.isBlank())
                throw new IllegalArgumentException("Cliente vacío");
            if (entrada == null || salida == null)
                throw new IllegalArgumentException("Las fechas son obligatorias");
            if (!salida.isAfter(entrada))
                throw new IllegalArgumentException(
                        "La salida (%s) debe ser posterior a la entrada (%s)"
                                .formatted(salida, entrada));
            cliente = cliente.trim();
        }
        long noches() { return ChronoUnit.DAYS.between(entrada, salida); }
    }

    System.out.println(new Reserva("Ana", LocalDate.of(2026,3,1),
                                          LocalDate.of(2026,3,5)).noches());   // 4

    new Reserva("Ana", LocalDate.of(2026,3,5), LocalDate.of(2026,3,1));
    // lanza IllegalArgumentException: La salida (2026-03-01) debe ser posterior...
    ```

    **Un objeto que valida al nacer no existe nunca en estado inválido.** Si validas «más tarde», siempre hay un camino por el que se cuela.

## E18 ●● — Rangos que se solapan

¿Se solapan `[1 marzo, 5 marzo)` y `[5 marzo, 8 marzo)`? Escribe la condición.

??? success "Solución"

    **No se solapan.** La primera termina el 5 y la segunda empieza el 5: el huésped sale por la mañana y entra otro por la tarde.

    ```java
    boolean solapan(Reserva a, Reserva b) {
        return a.entrada().isBefore(b.salida()) && b.entrada().isBefore(a.salida());
    }

    var r1 = new Reserva("Ana",   LocalDate.of(2026,3,1), LocalDate.of(2026,3,5));
    var r2 = new Reserva("Bruno", LocalDate.of(2026,3,5), LocalDate.of(2026,3,8));
    var r3 = new Reserva("Clara", LocalDate.of(2026,3,4), LocalDate.of(2026,3,7));

    System.out.println(solapan(r1, r2));    // false
    System.out.println(solapan(r1, r3));    // true
    ```

    **Todo está en `isBefore` y no `isBefore || isEqual`.** Con el segundo, dos reservas consecutivas se declaran en conflicto y pierdes la mitad de las noches del hotel. Es un bug real, y muy caro.

---

# Bloque 4 · Base de datos con JDBC

> Tema [3. CRUD contra base de datos](03-base-de-datos.md)

## E19 ● — Conectar y crear la tabla

Conéctate a H2 en memoria y crea la tabla `producto`.

??? success "Solución"

    ```java
    import java.sql.*;

    var URL = "jdbc:h2:mem:tienda;DB_CLOSE_DELAY=-1";

    try (var con = DriverManager.getConnection(URL, "sa", "");
         var st  = con.createStatement()) {
        st.execute("""
            CREATE TABLE IF NOT EXISTS producto (
              id     INT AUTO_INCREMENT PRIMARY KEY,
              codigo VARCHAR(10) NOT NULL UNIQUE,
              nombre VARCHAR(80) NOT NULL,
              precio DECIMAL(10,2) NOT NULL
            )""");
        System.out.println("Tabla creada");
    }
    ```

    - **`mem:`** = la base de datos vive en memoria y desaparece al cerrar. Perfecto para probar.
    - **`DB_CLOSE_DELAY=-1`** = no la borres cuando se cierre la primera conexión. Sin esto, la tabla se evapora entre un `try` y el siguiente.
    - **`try-with-resources`** con conexión y sentencia. Una conexión que no se cierra acaba agotando el pool.

## E20 ● — `executeQuery` o `executeUpdate`

¿Cuál usas en cada caso, y qué devuelve?

??? success "Solución"

    | Sentencia | Método | Devuelve |
    |---|---|---|
    | `SELECT` | `executeQuery()` | `ResultSet` |
    | `INSERT`, `UPDATE`, `DELETE` | `executeUpdate()` | `int`: **filas afectadas** |
    | `CREATE`, `DROP` | `execute()` | `boolean` |

    ```java
    int filas = ps.executeUpdate();
    if (filas == 0) System.out.println("No existía ese id");
    ```

    **Ese `int` es la respuesta a «¿se ha borrado?».** Un `DELETE` de un id que no existe no lanza nada: devuelve `0`. Quien no lo mira, informa al usuario de un borrado que no ocurrió.

## E21 ●● — Insertar con `PreparedStatement`

Inserta tres productos y recupera el id generado.

??? success "Solución"

    ```java
    var sql = "INSERT INTO producto (codigo, nombre, precio) VALUES (?, ?, ?)";

    try (var con = DriverManager.getConnection(URL, "sa", "");
         var ps  = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setString(1, "A1");
        ps.setString(2, "Casco");
        ps.setBigDecimal(3, new java.math.BigDecimal("35.00"));
        ps.executeUpdate();

        try (var claves = ps.getGeneratedKeys()) {
            if (claves.next()) System.out.println("id generado: " + claves.getInt(1));
        }
        // id generado: 1
    }
    ```

    Dos detalles que caen en el test: **los `?` se numeran desde 1**, no desde 0, y `RETURN_GENERATED_KEYS` va en el `prepareStatement`, no en el `executeUpdate`.

## E22 ●● — La inyección SQL, provocada

Escribe la búsqueda concatenando, pásale `x' OR '1'='1` y cuenta las filas.

??? success "Solución"

    ```java
    // NUNCA hagas esto
    var entrada = "x' OR '1'='1";
    var sql = "SELECT * FROM producto WHERE codigo = '" + entrada + "'";
    ```

    La consulta que llega a la base de datos es:

    ```sql
    SELECT * FROM producto WHERE codigo = 'x' OR '1'='1'
    ```

    `'1'='1'` es siempre cierto: **devuelve la tabla entera**. Con `; DROP TABLE producto; --` la borra.

    ```java
    // BIEN
    try (var ps = con.prepareStatement("SELECT * FROM producto WHERE codigo = ?")) {
        ps.setString(1, entrada);
        try (var rs = ps.executeQuery()) {
            int n = 0; while (rs.next()) n++;
            System.out.println("Filas: " + n);    // Filas: 0
        }
    }
    ```

    El `?` **no es una plantilla de texto**: el valor viaja aparte y la base de datos nunca lo interpreta como SQL. Por eso `PreparedStatement` no es «la forma elegante», es **la única forma**.

## E23 ●● — Dejar que la base de datos valide

Antes de insertar, ¿compruebas con un `SELECT` si el código existe?

??? success "Solución"

    **No.** Entre tu `SELECT` y tu `INSERT` cabe otro programa haciendo lo mismo, y los dos creen que el código está libre.

    ```java
    try {
        ps.executeUpdate();
    } catch (SQLIntegrityConstraintViolationException e) {
        System.out.println("Ya existe un producto con ese código");
    }
    ```

    La restricción `UNIQUE` de la tabla es la única comprobación que **no se puede saltar**, porque la hace la base de datos en el momento de escribir. Se intenta insertar y se captura el fallo.

    Vale igual para la clave ajena: si borras una categoría que tiene productos, salta la misma excepción y eso es exactamente lo que quieres.

## E24 ●●● — El CRUD completo contra H2

Las cuatro operaciones sobre `producto`, cada una en su método.

??? success "Solución"

    ```java
    // Crud.java
    import java.sql.*;
    import java.math.BigDecimal;
    import java.util.*;

    record Producto(int id, String codigo, String nombre, BigDecimal precio) {}

    static final String URL = "jdbc:h2:./datos/tienda";

    Connection con() throws SQLException { return DriverManager.getConnection(URL, "sa", ""); }

    int crear(String codigo, String nombre, BigDecimal precio) throws SQLException {
        var sql = "INSERT INTO producto (codigo, nombre, precio) VALUES (?, ?, ?)";
        try (var c = con(); var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, codigo); ps.setString(2, nombre); ps.setBigDecimal(3, precio);
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    Optional<Producto> leer(int id) throws SQLException {
        try (var c = con();
             var ps = c.prepareStatement("SELECT * FROM producto WHERE id = ?")) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    List<Producto> listar() throws SQLException {
        var lista = new ArrayList<Producto>();
        try (var c = con(); var st = c.createStatement();
             var rs = st.executeQuery("SELECT * FROM producto ORDER BY codigo")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    boolean actualizarPrecio(int id, BigDecimal precio) throws SQLException {
        try (var c = con();
             var ps = c.prepareStatement("UPDATE producto SET precio = ? WHERE id = ?")) {
            ps.setBigDecimal(1, precio); ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    boolean borrar(int id) throws SQLException {
        try (var c = con();
             var ps = c.prepareStatement("DELETE FROM producto WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(rs.getInt("id"), rs.getString("codigo"),
                            rs.getString("nombre"), rs.getBigDecimal("precio"));
    }
    ```

    Tres decisiones que se repiten en toda la asignatura:

    - **`leer` devuelve `Optional`**: que no exista un id es normal, no un error.
    - **`actualizar` y `borrar` devuelven `boolean`**, leído de las filas afectadas.
    - **`mapear` está una sola vez.** Cuando la tabla gane una columna, se toca un método.

## E25 ●●● — El mismo código, contra MySQL

Levanta MySQL con Docker y haz que el E24 funcione sin tocar la lógica.

??? success "Solución"

    ```yaml title="compose.yaml"
    services:
      db:
        image: mysql:8.4
        environment:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: tienda
        ports: ["3306:3306"]
        volumes: [dbdata:/var/lib/mysql]
        healthcheck:
          test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-proot"]
          interval: 5s
          retries: 10
    volumes:
      dbdata:
    ```

    ```bash
    docker compose up -d
    docker compose logs -f db      # espera al "ready for connections"
    ```

    En el programa, **solo cambian tres constantes**:

    ```java
    static final String URL = "jdbc:mysql://localhost:3306/tienda";
    static final String USER = "root";
    static final String PASS = "root";
    ```

    Y en el `pom.xml`, `com.mysql:mysql-connector-j`.

    **Nada más.** Ni un `SELECT`, ni un `PreparedStatement`, ni un `ResultSet`. Eso es lo que significa que JDBC sea un estándar: el `Connection` es una interfaz y cada base de datos trae su implementación.

    Lo que sí cambia es el **dialecto SQL**: `AUTO_INCREMENT` existe en los dos, pero `LIMIT`, las funciones de fecha y los tipos no siempre. Por eso en la UT5 aparecerá JPA, que también abstrae eso.

    El `healthcheck` no es adorno: sin él, tu programa intenta conectar mientras MySQL todavía está arrancando y falla con un `Communications link failure`.

---

# Bloque 5 · Programas integradores

## E26 ●●● — Del CSV al informe

Lee `productos.csv` e imprime: el total por categoría ordenado, el producto más caro y cuántas filas se descartaron.

??? success "Solución"

    Es la unión del E6 (lectura con descartes) y de los streams de la UT2:

    ```java
    var porCategoria = buenos.stream().collect(
            Collectors.groupingBy(Producto::categoria, TreeMap::new,
                                  Collectors.summingDouble(Producto::precio)));
    porCategoria.forEach((c, v) -> IO.println("  %-12s %9.2f €".formatted(c, v)));

    IO.println("Más caro: " + buenos.stream()
            .max(Comparator.comparingDouble(Producto::precio))
            .map(Producto::nombre).orElse("—"));
    IO.println("Descartes: " + descartes);
    ```

    ```
      movilidad     570,00 €
      seguridad      75,50 €
    Más caro: Bici de paseo
    Descartes: {}
    ```

    Los streams no se explican aquí: **son la UT2**. Aquí solo se usan.

## E27 ●●● — Consumir una API de verdad

Descarga un JSON con `HttpClient`, léelo con `readTree` y saca tres campos.

??? success "Solución"

    ```java
    import java.net.http.*;
    import java.net.URI;

    var cliente = HttpClient.newHttpClient();
    var peticion = HttpRequest.newBuilder(URI.create("https://api.ejemplo.es/productos"))
            .header("Accept", "application/json")
            .GET().build();

    var respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    IO.println("Estado: " + respuesta.statusCode());

    if (respuesta.statusCode() == 200) {
        var raiz = new ObjectMapper().readTree(respuesta.body());
        for (var item : raiz.path("items")) {
            IO.println(item.path("nombre").asText("(sin nombre)")
                     + " → " + item.path("precio").asDouble(0.0));
        }
    }
    ```

    Aquí se junta **toda la UT1** (códigos de estado, cabecera `Accept`) con **toda la UT3** (JSON, `path`). Y con `path` y los valores por defecto, un campo que falte no tira el programa.

    **Comprueba siempre el `statusCode` antes de parsear el cuerpo.** Un 404 devuelve una página de error, no tu JSON.

## E28 ●●● — El taller de bicis

`datos/bicis.csv`, 300 filas sucias. Cárgalo contando descartes por motivo, expórtalo a JSON con fechas ISO y vuélcalo en H2 con el CRUD funcionando.

??? success "Solución"

    No hay solución nueva: **es el E6 + el E12 + el E24, uno detrás de otro**.

    ```
    Cargadas 283 de 300 filas
    Descartes: {fecha ilegible=4, falta el código=2, precio no numérico=11}
    Escrito salida/bicis.json (283 registros, 4 categorías)
    Insertadas 283 filas en H2
    ```

    El orden de trabajo, que es el que importa:

    1. **Primero el lector**, y que imprima los descartes. Sin esto no sabes qué datos tienes.
    2. **Después el JSON**, con el módulo de `java.time` puesto desde el principio.
    3. **Al final la base de datos**, con `PreparedStatement` y capturando la violación de `UNIQUE`.

    Si lo haces al revés, depuras tres cosas a la vez y no avanzas.

    !!! success "Esto es literalmente la primera tarea de un becario"
        «Toma este fichero que nos ha mandado el cliente y métemelo en la base de datos, y dime qué filas están mal». Si lo sabes hacer, ya sabes trabajar.

---

## Reparto sugerido

| Sesión | Bloque | Ejercicios |
|:-:|---|---|
| **S1** | 1 · CSV a mano y sus trampas | E1 – E4 |
| **S2** | 1 · Commons CSV | E5 – E6 |
| **S3** | 2 · Jackson ida y vuelta | E7 – E8 |
| **S4** | 2 · Configuración y `readTree` | E9 – E11 |
| **S5** | 2 · CSV → JSON | E12 · E26 |
| **S6** | 3 · `java.time` | E13 – E16 |
| **S7** | 3 · Validación y rangos | E17 – E18 |
| **S8** | 4 · JDBC y H2 | E19 – E24 |
| **S9** | 4 · MySQL en Docker | E25 · E27 |
| **S10** | — | [Simulacro de test](autoevaluacion.md) |

E28 es el reto largo de la unidad y se hace en pareja, fuera de la sesión.

!!! success "Si vas justo de tiempo"
    El mínimo: **E1, E4, E5, E9, E10, E13, E18, E20, E22**.

    Y los dos que más se preguntan: **E22** (por qué el `?` no es una plantilla) y **E18** (`isBefore` y no `isBefore || isEqual`).
