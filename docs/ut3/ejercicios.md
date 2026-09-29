# Batería de ejercicios — UT3

**Dominio: una tienda de bicicletas.** Distinto del de los temas a propósito, para que no valga copiar.

**31 ejercicios agrupados por bloque**, con solución: 25 fragmentos y **6 programas completos** (E26–E31), que son la práctica integradora de la unidad. Todos se ejecutan con `java Fichero.java` salvo los de Jackson y JDBC, que necesitan Maven.

| Bloque | Ejercicios | Sesiones |
|---|---|:-:|
| 1 · CSV: leer y escribir | E1–E7 | S1–S2 |
| 2 · JSON con Jackson | E8–E13 | S3–S5 |
| 3 · Fechas y validación | E14–E18 | S6–S7 |
| **4 · Base de datos con JDBC** | **E19–E24** | **S8–S9** |
| Cierre · escribe tú las preguntas | E25 | S10 |
| 5 · Taller largo: programas completos | E26–E31 | S1–S9 |

!!! info "Las colecciones y los *streams* no están aquí"
    Se explican y se practican en el [tema 4 de la UT2](../ut2/04-colecciones-y-funcional.md), que tiene su propia batería. En esta unidad **se usan** —dentro de un lector de CSV, de un informe o de un `ResultSet`— pero no son materia propia ni se preguntan por separado en el test.

---

# Tema 1 · CSV: leer y escribir

### E1 ● — Leer y contar

Lee un fichero de texto y cuenta líneas, palabras y caracteres.

??? success "Solución"

    ```java
    try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
        var lista = lineas.toList();
        IO.println("Líneas: " + lista.size());
        IO.println("Palabras: " + lista.stream().mapToLong(l -> l.split("\\s+").length).sum());
        IO.println("Caracteres: " + lista.stream().mapToInt(String::length).sum());
    }
    ```
    El `try-with-resources` no es opcional: `Files.lines` mantiene el fichero abierto hasta que se cierra el flujo. Sin él, en Windows no podrás ni borrar el fichero después.


### E2 ●● — CSV con sus trampas

Lee un CSV con separador `;`, decimales con coma y líneas en blanco.

??? success "Solución"

    ```java
    try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
        return lineas.skip(1)
            .filter(l -> !l.isBlank())
            .map(l -> l.split(";", -1))
            .filter(c -> c.length >= 5)
            .map(c -> new Bici(c[0].trim(), c[1].trim(),
                               new BigDecimal(c[3].trim().replace(',', '.'))))
            .toList();
    }
    ```
    Las tres trampas: el separador, el `-1` del `split` —que conserva los campos vacíos del final— y la coma decimal.


### E3 ●● — Contar los descartes

Cuenta cuántas filas se descartan y por qué motivo.

??? success "Solución"

    ```java
    record Resultado(List<Bici> validas, Map<String, Integer> motivos) {}

    var motivos = new TreeMap<String, Integer>();
    // … dentro del bucle:
    if (c.length < 5)        { motivos.merge("columnas insuficientes", 1, Integer::sum); continue; }
    if (c[1].isBlank())      { motivos.merge("sin marca", 1, Integer::sum); continue; }
    if (precio.signum() <= 0){ motivos.merge("precio no válido", 1, Integer::sum); continue; }
    ```
    Una carga que descarta en silencio es una caja negra. Cuando falten registros, sin este mapa no hay forma de explicar por qué.


### E4 ●● — Filtrar y escribir

Lee el catálogo, filtra las bicis de menos de 500 € y escribe el resultado en otro CSV.

??? success "Solución"

    ```java
    var baratas = catalogo.stream()
        .filter(b -> b.precio().compareTo(new BigDecimal("500")) < 0)
        .map(b -> String.join(";", b.bastidor(), b.marca(), b.precio().toString()))
        .toList();

    Files.write(destino,
        Stream.concat(Stream.of("bastidor;marca;precio"), baratas.stream()).toList(),
        StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
    ```
    El `TRUNCATE_EXISTING` es importante: sin él, si el fichero existía y era más largo, quedan restos del anterior al final.


### E5 ●●● — Escritura atómica

Que un fallo a mitad de la escritura no deje el fichero corrupto.

??? success "Solución"

    ```java
    var tmp = Files.createTempFile(destino.getParent(), "tmp", ".csv");
    Files.write(tmp, lineas, StandardCharsets.UTF_8);
    Files.move(tmp, destino, REPLACE_EXISTING, ATOMIC_MOVE);
    ```
    Se escribe en un temporal **en la misma carpeta** —el movimiento atómico solo funciona dentro del mismo sistema de ficheros— y se sustituye de golpe.

    Si el proceso muere a mitad, el fichero original sigue intacto. Es lo que hace cualquier editor de texto al guardar.


### E6 ●● — El CSV del ERP

Te pasan un fichero exportado de un programa español: separador `;`, decimales con coma y algún campo entrecomillado con comas dentro. Léelo.

??? success "Solución"

    ```java
    var formato = CSVFormat.DEFAULT.builder()
            .setDelimiter(';')                    // (1)
            .setHeader().setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true).setTrim(true)
            .get();

    var numeros = NumberFormat.getInstance(new Locale("es", "ES"));   // (2)

    try (var lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8);
         var csv = formato.parse(lector)) {

        for (var fila : csv) {
            var precio = numeros.parse(fila.get("precio")).doubleValue();
            …
        }
    }
    ```

    1.  Excel en español exporta con **punto y coma**, porque la coma ya la usa para los decimales.
    2.  `Double.parseDouble("25,90")` lanza `NumberFormatException`. Hay que convertir teniendo en cuenta el idioma.

    Y el campo entrecomillado con comas dentro —`"Cien años de soledad, edición especial"`— lo resuelve la librería sola. A mano, con `split`, ese campo se parte en dos y todo lo que viene detrás se desplaza.

    **Es el motivo por el que se usa Commons CSV y no `split`.**


### E7 ●●● — Errores de E/S bien tratados

Trata por separado: fichero que no existe, sin permisos, y contenido mal formado.

??? success "Solución"

    ```java
    public List<Bici> cargar(Path ruta) {
        if (!Files.exists(ruta))    throw new CatalogoNoEncontradoException(ruta);
        if (!Files.isReadable(ruta)) throw new CatalogoNoLegibleException(ruta);
        try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
            return procesar(lineas);
        } catch (MalformedInputException e) {
            throw new CatalogoCorruptoException("Codificación incorrecta: ¿es UTF-8?", e);
        } catch (IOException e) {
            throw new CatalogoException("Error leyendo " + ruta, e);
        }
    }
    ```
    El `MalformedInputException` es el que aparece con un fichero en ISO-8859-1 leído como UTF-8. Distinguirlo permite dar un mensaje útil en vez de un genérico.


---

# Tema 2 · JSON con Jackson

### E8 ● — De objeto a JSON

Serializa una lista de bicis a un fichero, con formato legible.

??? success "Solución"

    ```java
    var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File("bicis.json"), bicis);
    ```


### E9 ●● — Fechas ISO y sin nulos

Configura el mapeador para que las fechas salgan legibles y no aparezcan campos nulos.

??? success "Solución"

    ```java
    var mapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();
    ```
    Sin la segunda línea, un `LocalDate` sale como `[2026,7,10]` o como un número enorme. Es el fallo que más se ve en las entregas.


### E10 ●● — De JSON a objeto

Lee un JSON de una lista y conviértelo en `List<Bici>`.

??? success "Solución"

    ```java
    List<Bici> bicis = mapper.readValue(fichero, new TypeReference<List<Bici>>() {});
    ```
    El `TypeReference` es obligatorio por el borrado de tipos: `readValue(f, List.class)` devuelve una lista de `LinkedHashMap`, no de bicis, y el error aparece más tarde y en otro sitio.


### E11 ●●● — JSON ajeno con campos que sobran

Consume un JSON que trae campos que no te interesan y que puede cambiar.

??? success "Solución"

    ```java
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaExterna(String id, String nombre, @JsonProperty("precio_eur") BigDecimal precio) {}
    ```
    Sin `ignoreUnknown`, el día que el proveedor añada un campo tu aplicación empieza a fallar sin que hayas tocado nada. Y `@JsonProperty` mapea nombres que no siguen tu convención.


### E12 ●●● — De CSV a JSON agrupado

Lee el CSV y escribe un JSON con las bicis agrupadas por tipo.

??? success "Solución"

    ```java
    Map<String, List<Bici>> agrupadas = catalogo.stream()
        .collect(groupingBy(b -> b.tipo().name(), TreeMap::new, toList()));
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File("catalogo.json"), agrupadas);
    ```
    Un `Map` se serializa como objeto JSON con las claves como propiedades. Con `TreeMap`, además, salen ordenadas — y un JSON con orden estable se puede comparar con `diff` entre ejecuciones.


### E13 ●● — Recorrer un JSON sin clase

Extrae un dato de un JSON del que no quieres crear la clase entera.

??? success "Solución"

    ```java
    JsonNode raiz = mapper.readTree(json);
    String titulo = raiz.path("datos").path(0).path("titulo").asText("desconocido");
    int total = raiz.path("meta").path("total").asInt(0);
    ```
    `path()` frente a `get()`: `path` devuelve un nodo vacío si no existe, así que se puede encadenar sin comprobar nulos. `get()` devuelve `null` y encadenarlo revienta.


---

# Tema 3 · Fechas y validación

### E14 ● — La fecha que no cambia

Explica qué imprime y por qué.

```java
var fecha = LocalDate.of(2026, 7, 10);
fecha.plusDays(10);
IO.println(fecha);
```

??? success "Solución"

    Imprime **2026-07-10**. `java.time` es **inmutable**: `plusDays` devuelve una fecha nueva y no toca la original.

    ```java
    fecha = fecha.plusDays(10);      // 
    ```
    Casi toda la clase falla esta pregunta la primera vez.


### E15 ●● — Cálculos con fechas

Días entre dos fechas, si una revisión está vencida, y la próxima revisión a seis meses.

??? success "Solución"

    ```java
    long dias = ChronoUnit.DAYS.between(compra, hoy);
    boolean vencida = ultimaRevision.plusMonths(12).isBefore(LocalDate.now());
    LocalDate proxima = ultimaRevision.plusMonths(6);
    ```
    `Period.between` da años, meses y días por separado; `ChronoUnit.DAYS.between` da el total. Confundirlos es el error del tema.


### E16 ●● — Formatear y analizar

Muestra la fecha como `10/07/2026` y lee una escrita así.

??? success "Solución"

    ```java
    var f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String texto = fecha.format(f);
    LocalDate leida = LocalDate.parse("10/07/2026", f);
    ```
    Ojo con `MM` (mes) y `mm` (minutos): `dd/mm/yyyy` da un mes disparatado y ningún error.


### E17 ●● — Validar en el constructor

`Bici` que se valide sola: bastidor de 17 caracteres, marca obligatoria, precio positivo.

??? success "Solución"

    ```java
    record Bici(String bastidor, String marca, BigDecimal precio) {
        Bici {
            if (bastidor == null || bastidor.length() != 17)
                throw new IllegalArgumentException("El bastidor son 17 caracteres");
            if (marca == null || marca.isBlank())
                throw new IllegalArgumentException("La marca es obligatoria");
            if (precio == null || precio.signum() <= 0)
                throw new IllegalArgumentException("El precio debe ser positivo");
        }
    }
    ```
    El constructor compacto valida **antes** de asignar. Un objeto que existe es un objeto válido, y eso elimina comprobaciones repartidas por todo el código.


### E18 ●●● — Expresiones regulares útiles

Valida matrícula, correo y código postal, y explica por qué el correo es un caso especial.

??? success "Solución"

    ```java
    static final Pattern MATRICULA = Pattern.compile("^\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");
    static final Pattern CP        = Pattern.compile("^(0[1-9]|[1-4]\\d|5[0-2])\\d{3}$");
    static final Pattern CORREO    = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    ```
    El correo **no se valida bien con una expresión regular**: la especificación admite cosas que ninguna regex razonable cubre. La comprobación de verdad es **enviar un mensaje** y que el usuario confirme. Una regex sencilla evita erratas obvias y ya está.

    Compilar el patrón una vez como constante importa: dentro de un bucle, `Pattern.compile` en cada iteración es coste puro.


---

# Tema 4 · Base de datos con JDBC

### E19 ● — Las cuatro piezas

Empareja cada clase de JDBC con lo que hace, y di cuáles hay que cerrar.

`Connection` · `PreparedStatement` · `ResultSet` · `DriverManager`

??? success "Solución"

    | Pieza | Qué hace | ¿Cerrar? |
    |---|---|:-:|
    | `DriverManager` | Fabrica conexiones a partir de la URL | No |
    | `Connection` | La conexión abierta con la base de datos | **Sí** |
    | `PreparedStatement` | La consulta con huecos `?` | **Sí** |
    | `ResultSet` | El resultado, fila a fila | **Sí** |

    Las tres se cierran, y por eso van siempre en un `try-with-resources`:

    ```java
    try (var con = DriverManager.getConnection(url, usuario, clave);
         var ps  = con.prepareStatement(sql);
         var rs  = ps.executeQuery()) { … }
    ```

    Se cierran **en orden inverso** —`rs`, `ps`, `con`— y **todas**, aunque una falle al cerrarse.

### E20 ●● — `executeQuery` o `executeUpdate`

Di cuál se usa en cada caso y qué devuelve.

(a) `SELECT * FROM producto` · (b) `INSERT INTO producto …` · (c) `UPDATE producto SET stock = ?` · (d) `DELETE FROM producto WHERE id = ?` · (e) `CREATE TABLE …`

??? success "Solución"

    | | Método | Devuelve |
    |---|---|---|
    | (a) `SELECT` | `executeQuery()` | Un `ResultSet` |
    | (b) `INSERT` | `executeUpdate()` | Filas insertadas (1) |
    | (c) `UPDATE` | `executeUpdate()` | **Filas modificadas** |
    | (d) `DELETE` | `executeUpdate()` | **Filas borradas** |
    | (e) `CREATE TABLE` | `execute()` | `boolean` (aquí, `false`) |

    Y lo útil de verdad: **el número que devuelve `executeUpdate` dice si existía**, sin hacer antes un `SELECT`.

    ```java
    public boolean actualizarStock(String codigo, int stock) {
        …
        return ps.executeUpdate() == 1;      // false si ese código no está
    }
    ```

### E21 ●●● — La inyección SQL, provocada

Escribe la versión insegura de una búsqueda por código, pásale `x' OR '1'='1` y cuenta las filas. Después arréglala.

??? success "Solución"

    ```java
    // INSEGURA · solo para verlo
    var sql = "SELECT * FROM producto WHERE codigo = '" + codigo + "'";
    try (var con = conectar(); var st = con.createStatement();
         var rs = st.executeQuery(sql)) {
        int n = 0; while (rs.next()) n++;
        System.out.println("Filas: " + n);      // ← la tabla ENTERA
    }
    ```

    La consulta que llega a la base de datos es:

    ```sql
    SELECT * FROM producto WHERE codigo = 'x' OR '1'='1'
    ```

    `'1'='1'` es siempre cierto. Y con un poco más: `x'; DROP TABLE producto; --`.

    ```java
    // SEGURA
    try (var con = conectar();
         var ps = con.prepareStatement("SELECT * FROM producto WHERE codigo = ?")) {
        ps.setString(1, codigo);
        …
    }
    ```

    **La estructura de la consulta viaja primero y el valor va aparte.** Cuando la base de datos recibe el dato ya tiene decidido qué es una consulta y qué es un valor, así que el dato no puede cambiar nada: busca un código llamado literalmente `x' OR '1'='1` y no lo encuentra.

    La regla, sin excepciones: **ningún valor que venga de fuera se concatena en un SQL. Nunca.**

### E22 ●● — La comprobación con carrera

```java
if (dao.buscarPorCodigo(p.codigo()).isPresent()) {
    throw new CodigoDuplicadoException(p.codigo());
}
dao.insertar(p);
```

Además de hacer dos consultas, ¿qué problema tiene?

??? success "Solución"

    **Hay un hueco entre la comprobación y la inserción.** Si dos procesos hacen esto a la vez:

    ```
    Proceso A: ¿existe SEG-01? → no
    Proceso B: ¿existe SEG-01? → no
    Proceso A: INSERT SEG-01   → ok
    Proceso B: INSERT SEG-01   → ¡duplicado!
    ```

    Se llama **condición de carrera**, y no se arregla con más comprobaciones: se arregla dejando que lo garantice la base de datos.

    ```sql
    codigo VARCHAR(20) NOT NULL UNIQUE
    ```

    ```java
    try {
        …
        ps.executeUpdate();
    } catch (SQLIntegrityConstraintViolationException e) {
        throw new CodigoDuplicadoException(p.codigo(), e);
    }
    ```

    La restricción `UNIQUE` es **atómica**: no tiene hueco. Es una consulta en vez de dos, y además es correcta.

    Este mismo razonamiento reaparece en la UT5 con las entidades JPA.

### E23 ●● — La conexión que se queda abierta

```java
public List<Producto> listar() throws SQLException {
    var con = DriverManager.getConnection(url, usuario, clave);
    var ps = con.prepareStatement("SELECT * FROM producto");
    var rs = ps.executeQuery();
    var lista = new ArrayList<Producto>();
    while (rs.next()) lista.add(aProducto(rs));
    return lista;
}
```

Funciona en las pruebas y revienta en producción. ¿Por qué?

??? success "Solución"

    **No se cierra nada.** Cada llamada deja una conexión abierta, y las bases de datos tienen un límite:

    ```
    com.mysql.cj.jdbc.exceptions.CJCommunicationsException: Too many connections
    ```

    Lo traicionero es que el síntoma aparece **lejos de la causa y solo bajo carga**: en tu máquina, llamando cinco veces, nunca falla.

    ```java
    try (var con = conectar();
         var ps  = con.prepareStatement("SELECT * FROM producto");
         var rs  = ps.executeQuery()) {

        var lista = new ArrayList<Producto>();
        while (rs.next()) lista.add(aProducto(rs));
        return List.copyOf(lista);
    }
    ```

    Y un aviso para la UT4: abrir una conexión es **caro**. Por eso las aplicaciones de verdad usan un *pool* —HikariCP, que trae Spring Boot— que las reutiliza. El `try-with-resources` sigue haciendo falta: lo que hace `close()` entonces es devolverla al *pool*, no cerrarla.

### E24 ●●● — El mismo código, otra base de datos

Tienes el CRUD funcionando con H2 y te piden pasarlo a MySQL en Docker. ¿Qué tocas exactamente?

??? success "Solución"

    Dos cosas, y ninguna está en el DAO:

    1. **La dependencia del driver** en el `pom.xml`:

    ```xml
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <version>9.1.0</version>
    </dependency>
    ```

    2. **La cadena de conexión**:

    ```java
    // antes
    new ProductoDao("jdbc:h2:./datos/tienda", "sa", "");
    // después
    new ProductoDao("jdbc:mysql://localhost:3306/tienda?serverTimezone=Europe/Madrid",
                    "alumno", "alumno");
    ```

    Y el `compose.yaml`, con las dos cosas que se olvidan:

    ```yaml
    services:
      mysql:
        image: mysql:8.4
        environment:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: tienda
          MYSQL_USER: alumno
          MYSQL_PASSWORD: alumno
        ports: ["3306:3306"]
        volumes:
          - datos-mysql:/var/lib/mysql       # (1)
        healthcheck:                         # (2)
          test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-proot"]
          interval: 5s
          retries: 10

    volumes:
      datos-mysql:
    ```

    1.  **Sin el volumen los datos desaparecen** con el contenedor. Y `docker compose down -v` sí los borra: esa `-v` es la peligrosa.
    2.  Un contenedor arrancado **no es** una base de datos lista. El `healthcheck` dice cuándo acepta conexiones de verdad.

    Que el DAO no se toque es exactamente lo que aporta JDBC: es un estándar, y el *driver* traduce.

    El matiz profesional: **desarrolla contra el mismo motor que usarás en producción**. Probar con H2 y desplegar en MySQL es la forma más segura de descubrir las diferencias el día del despliegue.

---

# Cierre

### E25 ●●● — Convierte tus propios ejercicios en preguntas

Coge tres ejercicios que ya hayas resuelto y **escribe una pregunta de test sobre cada uno**, con sus cuatro opciones y los tres distractores plausibles.

??? success "Solución"

    No hay una única respuesta; hay un método. Para convertir un ejercicio en pregunta, cambia **una sola cosa** de tu código correcto y pregunta por el resultado.

    **Del E5 (doble agrupación):**

    ```java
    Map<String, Map<String, Long>> r = bicis.stream()
            .collect(groupingBy(Bici::marca, TreeMap::new,
                     groupingBy(Bici::tipo, counting())));
    ```

    > ¿Qué cambia si se quita el `TreeMap::new`?
    >
    > **A.** Nada, el resultado es el mismo · **B.** Las marcas dejan de salir ordenadas · **C.** Los tipos dejan de salir ordenados · **D.** No compila

    El distractor bueno es la **C**: es el fallo real, confundir en qué nivel actúa el `TreeMap`.

    **Del E2 (CSV con trampas):**

    ```java
    String[] campos = linea.split(";");
    ```

    > Con la línea `B-004;Orbea;;;;` ¿cuántos elementos tiene `campos`?
    >
    > **A.** 6 · **B.** 2 · **C.** 5 · **D.** 3

    La respuesta es **B**: `split` sin `-1` descarta los vacíos finales. Es el error que más `ArrayIndexOutOfBounds` provoca.

    **Del E9 (fechas ISO):**

    Enseña la salida `"fecha": [2026,3,14]` y pregunta qué falta configurar.

    ---

    **Por qué este ejercicio es el más rentable de la unidad:** escribir el distractor te obliga a saber **por qué** alguien se equivocaría. Y ese «por qué» es justo lo que se pregunta en el examen.

    Hazlo en parejas: cada uno escribe tres preguntas y se las pasa al otro. Las que no sabéis resolver son las que hay que repasar.


---

## Taller largo: seis programas completos

!!! reto "Del E26 al E31 se entrega un programa que funciona"
    Los veinticinco primeros son fragmentos; estos son **programas enteros**, uno por bloque del temario. Eran el antiguo «reto con tests»: ahora están aquí, resueltos, porque en esta unidad lo que hay que entrenar es **reconocer**, no entregar.

    El dominio es distinto a propósito: un catálogo de productos, para que no valga copiar del de bicis.

### E26 ●● — Lector de CSV

Crea `datos/productos.csv` con cabecera y cinco productos. Escribe `List<Producto> leer(Path)` que lo convierta en objetos, saltando la cabecera y las líneas en blanco.

??? success "Solución"

    ```java
    List<Producto> leer(Path ruta) throws IOException {
        try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {   // (1)
            return lineas
                    .skip(1)                                             // (2)
                    .filter(l -> !l.isBlank())                           // (3)
                    .map(l -> l.split(",", -1))                          // (4)
                    .map(c -> new Producto(
                            Integer.parseInt(c[0].trim()),
                            c[1].trim(),
                            c[2].trim(),
                            Double.parseDouble(c[3].trim())))
                    .toList();
        }
    }
    ```

    1.  `Files.lines` devuelve un stream que **hay que cerrar**: mantiene el fichero abierto. De ahí el *try-with-resources*. Y el juego de caracteres se dice siempre: si no, usa el del sistema y en Windows salen los acentos rotos.
    2.  `skip(1)` para la cabecera. Sin él, `Integer.parseInt("id")` revienta.
    3.  Las líneas en blanco del final son la causa número uno de `ArrayIndexOutOfBounds`.
    4.  El `-1` conserva los campos vacíos del final. Sin él, `"4,Casco,,"` devuelve **dos** elementos y no cuatro.

    Esos cuatro detalles son, literalmente, cuatro preguntas del test.

### E27 ●● — Escribir y capturar el fallo

Escribe los productos de más de 100 € en `caros.csv` conservando la cabecera. Después provoca y captura un `NoSuchFileException`.

??? success "Solución"

    ```java
    void escribir(Path ruta, List<Producto> productos) throws IOException {
        var sb = new StringBuilder("id,nombre,categoria,precio\n");
        for (var p : productos) {
            sb.append("%d,%s,%s,%.2f%n"
                    .formatted(p.id(), p.nombre(), p.categoria(), p.precio()));
        }
        Files.writeString(ruta, sb.toString(), StandardCharsets.UTF_8);
    }

    void main() throws IOException {
        var todos = leer(Path.of("datos/productos.csv"));
        escribir(Path.of("datos/caros.csv"),
                 todos.stream().filter(p -> p.precio() > 100).toList());

        try {
            Files.readString(Path.of("datos/fantasma.csv"));
        } catch (NoSuchFileException e) {
            IO.println("No existe: " + e.getFile());       // (1)
        }
    }
    ```

    1.  `NoSuchFileException` trae `getFile()` con la ruta. Capturar `IOException` a secas también funciona y pierdes esa información.

    Dos cosas sobre el formato: `%.2f` usa la coma decimal si la configuración regional es española, y eso **rompe un CSV separado por comas**. Para ficheros de intercambio conviene `Locale.ROOT`:

    ```java
    String.format(Locale.ROOT, "%.2f", 12.5)   // "12.50" siempre
    ```

    Y `%n` es el salto de línea **del sistema**; `\n` es siempre `\n`. Para un fichero que se va a leer en otra máquina, `\n` es más predecible.

### E28 ●● — De CSV a JSON

Convierte tu CSV en un `productos.json` legible, con Jackson.

??? success "Solución"

    ```java
    var mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter()
          .writeValue(Path.of("datos/productos.json").toFile(), leer(csv));
    ```

    Tres líneas. Abre el JSON generado: cada `record` se ha convertido en un objeto y la lista en un array, **sin escribir nada de conversión**.

    Lo que hay que saber para el test:

    | Quiero | Se hace con |
    |---|---|
    | Objeto → JSON | `writeValue` / `writeValueAsString` |
    | JSON → objeto | `readValue` |
    | Que salga con sangría | `writerWithDefaultPrettyPrinter()` |
    | Fechas en ISO, no como números | `registerModule(new JavaTimeModule())` + desactivar `WRITE_DATES_AS_TIMESTAMPS` |
    | Que no salgan los nulos | `@JsonInclude(NON_NULL)` |

    Sin el módulo de `java.time`, un `LocalDate` sale como `[2026,3,14]`. Es la pregunta de Jackson que más cae.

### E29 ●● — Consumir JSON ajeno

Te llega esto de una API. Diseña el record y deserialízalo sin que falle por el campo `stock`, que no te interesa.

```json
[{"id":1,"nombre":"Patinete","precio":120.0,"stock":8},
 {"id":2,"nombre":"Casco","precio":35.0,"stock":40}]
```

??? success "Solución"

    ```java
    record ProductoApi(int id, String nombre, double precio) {}

    var mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);   // (1)

    List<ProductoApi> productos =
            mapper.readValue(json, new TypeReference<List<ProductoApi>>() {});      // (2)
    ```

    1.  Sin esto: `UnrecognizedPropertyException: "stock"`. Al consumir APIs de terceros se desactiva **siempre**: el día que añadan un campo, tu programa se cae en producción sin que hayas tocado nada.
    2.  Para una **lista** hace falta `TypeReference`. Con `readValue(json, List.class)` obtienes una lista de `LinkedHashMap`, no de tus objetos: el tipo genérico se pierde al compilar.

    La alternativa a desactivarlo globalmente es `@JsonIgnoreProperties(ignoreUnknown = true)` sobre el record. Hace lo mismo, acotado a esa clase.

### E30 ●●● — Reservas validadas

Crea `record Reserva(String cliente, String email, LocalDate entrada, LocalDate salida)` que valide en el constructor: cliente no vacío, correo con formato, entrada no pasada y salida posterior a la entrada. Añade `noches()`.

??? success "Solución"

    ```java
    record Reserva(String cliente, String email, LocalDate entrada, LocalDate salida) {

        private static final Pattern CORREO =
                Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");   // (1)

        Reserva {
            if (cliente == null || cliente.isBlank()) {
                throw new IllegalArgumentException("Cliente obligatorio");
            }
            if (email == null || !CORREO.matcher(email).matches()) {
                throw new IllegalArgumentException("Correo no válido: " + email);
            }
            if (entrada.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La entrada no puede ser pasada");
            }
            if (!salida.isAfter(entrada)) {                              // (2)
                throw new IllegalArgumentException("La salida debe ser posterior");
            }
        }

        long noches() { return ChronoUnit.DAYS.between(entrada, salida); }
    }
    ```

    1.  El `Pattern` es **`static final`**: compilarlo en cada validación es caro y no cambia nunca.
    2.  `!salida.isAfter(entrada)` y no `salida.isBefore(entrada)`: así también se rechaza que sean el mismo día. Es la diferencia entre `<` y `<=`, y es donde viven los fallos.

    Y el recordatorio permanente de `java.time`: **todo es inmutable**.

    ```java
    var f = LocalDate.of(2026, 3, 14);
    f.plusDays(7);              // no hace nada: se pierde
    var nueva = f.plusDays(7);  // así sí
    ```

    `ChronoUnit.DAYS.between` da **noches**, no días de estancia: del 1 al 3 son 2 noches. Ese `±1` es el fallo clásico de cualquier aplicación de reservas.

### E31 ●●● — El CRUD entero, de CSV a base de datos

Monta un proyecto Maven que **cargue el CSV del E26, lo vuelque en una base de datos H2 y ofrezca las cuatro operaciones**: insertar, listar, actualizar el stock y borrar. Después repítelo contra MySQL en Docker sin tocar el DAO.

??? success "Solución"

    El esqueleto:

    ```
    crud/
    ├── pom.xml                      ← h2, mysql-connector-j, commons-csv
    ├── compose.yaml
    ├── datos/productos.csv
    └── src/main/java/es/iesx/crud/
        ├── Producto.java            ← record con validación y BigDecimal
        ├── ProductoDao.java         ← las cinco operaciones
        ├── AccesoDatosException.java
        ├── CodigoDuplicadoException.java
        └── Main.java
    ```

    **La carga inicial**, que es lo que junta los dos temas:

    ```java
    var dao = new ProductoDao("jdbc:h2:./datos/tienda", "sa", "");
    dao.crearTabla();

    int cargados = 0, duplicados = 0, descartados = 0;

    for (var fila : leerCsv(Path.of("datos/productos.csv"))) {        // (1)
        try {
            dao.insertar(fila);
            cargados++;
        } catch (CodigoDuplicadoException e) {
            duplicados++;                                            // (2)
        }
    }
    System.out.printf("Cargados %d · duplicados %d · descartados %d%n",
                      cargados, duplicados, descartados);
    ```

    1.  El lector del E26, con Commons CSV y contando los descartes por formato.
    2.  **El duplicado no se comprueba antes: se deja fallar** y se traduce. Es una consulta en vez de dos y, sobre todo, no tiene la carrera del E22.

    Las cuatro operaciones, con lo que se evalúa de cada una:

    | Operación | Clave |
    |---|---|
    | `insertar` | `PreparedStatement` con `?` y `RETURN_GENERATED_KEYS` |
    | `listar` | `try-with-resources` con las **tres** piezas |
    | `actualizarStock` | Devolver `executeUpdate() == 1` en vez de hacer antes un `SELECT` |
    | `borrar` | Lo mismo |
    | `buscarPorCodigo` | Devolver **`Optional`**, no `null` |

    Y la comprobación que cierra la unidad:

    ```java
    System.out.println(dao.buscarPorCodigo("x' OR '1'='1").isPresent());   // false
    ```

    **El paso a MySQL: dos cambios y ninguno en el DAO.**

    ```java
    var dao = new ProductoDao(
            "jdbc:mysql://localhost:3306/tienda?serverTimezone=Europe/Madrid",
            "alumno", "alumno");
    ```

    ```bash
    docker compose up -d
    docker compose ps          # esperar a (healthy)
    mvn exec:java
    ```

    Si al cambiar de motor has tenido que tocar el DAO, mira qué has tocado: casi siempre es un tipo concreto de H2 o un SQL que no era estándar. **Ese descubrimiento es el objetivo del ejercicio.**

    !!! success "Lo que demuestra este ejercicio"
        Que sabes llevar un dato desde un fichero que te dan hasta una base de datos consultable, contando lo que se pierde por el camino y sin dejar un agujero de seguridad.

        Es, literalmente, la primera tarea que se le encarga a alguien que entra en un equipo de desarrollo.

---

## Del ejercicio a la pregunta de test

El examen de esta unidad es un **test práctico sobre fragmentos de código** ([batería de test aquí](autoevaluacion.md)). No se pide escribir un programa: se pide leer código y saber qué hace.

La correspondencia es directa. Cada bloque de ejercicios alimenta un bloque de preguntas:

| Ejercicios | Preguntas del examen | Qué se pregunta exactamente |
|---|:-:|---|
| **E1–E7** · CSV | 8 preguntas | Por qué falla un `split` sin `-1`; qué pasa sin `skip(1)`; `Files.lines` sin cerrar; el decimal con coma que rompe el fichero |
| **E8–E13** · JSON | 8 preguntas | La fecha que sale como `[2026,3,14]`; `get` frente a `path`; el campo desconocido que revienta; `TypeReference` |
| **E14–E18** · Fechas y validación | 6 preguntas | La inmutabilidad de `java.time`; solapes de rangos; `BigDecimal` frente a `double` |
| **E19–E24** · Base de datos | 8 preguntas | `executeQuery` o `executeUpdate`; la inyección SQL; la conexión sin cerrar; `UNIQUE` frente a comprobarlo tú |

!!! reto "Las tres cosas que de verdad transfieren"
    Hacer ejercicios **no** prepara para un test por sí solo. Lo que transfiere es esto, y se puede practicar desde la primera sesión:

    1. **Predecir antes de ejecutar.** Antes de darle a *Run*, escribe en un papel qué va a salir. Si aciertas, entendiste; si no, acabas de encontrar tu hueco. **Esta es la única costumbre que hay que coger**, y es exactamente lo que pide el tipo de pregunta 1.

    2. **Romper tu propia solución.** Cuando un ejercicio te salga bien, quítale el `-1` al `split`, cambia el `TreeMap` por un `HashMap`, borra el `skip(1)`. Mira el error que sale y **apúntalo**. El tipo de pregunta 2 —«¿por qué falla?»— es literalmente eso.

    3. **Escribir la pregunta tú (E25).** Inventar los tres distractores te obliga a saber por qué alguien se equivocaría. Es el paso que convierte «sé hacerlo» en «sé reconocerlo».

    Después de cada ejercicio de esta batería, dedica **dos minutos** a los puntos 1 y 2. Son 60 minutos en toda la unidad y valen más que cualquier repaso de la víspera.

---

## Cómo usarlos en clase

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E8 · E14 · E19 |
| Taller de la sesión, 25-30 min | E2 · E3 · E9 · E17 · E21 |
| Los que hay que hacer sí o sí | **E2 · E3 · E9 · E14 · E21** |
| Para quien va sobrado | E5 · E7 · E12 · E18 · E24 · E31 |
| Repaso antes del test | E2 · E9 · E14 · E21 · E25 + [batería de test](autoevaluacion.md) |

!!! tip "Los dos que más caen"
    El **E2** (CSV con sus trampas) y el **E9** (fechas ISO y sin nulos) concentran entre los dos **seis de las treinta preguntas**. Si vas justo de tiempo, esos dos antes que ninguno.

    Y de la última parte, el **E21** (la inyección SQL provocada): es la pregunta que nadie falla después de haberla visto, y la que casi todos fallan si solo la han leído.

    Y el **E25** hazlo siempre: es el puente entre haber resuelto los ejercicios y saber contestar sobre ellos.
